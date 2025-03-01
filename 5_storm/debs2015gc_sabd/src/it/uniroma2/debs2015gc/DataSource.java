package it.uniroma2.debs2015gc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.gson.Gson;

import it.uniroma2.debs2015gc.operator.Constants;
import it.uniroma2.debs2015gc.utils.LinesBatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class DataSource implements Runnable {

	/*
	 * This datasource reads data from the debs2015 grand challenge
	 * and sends them trying to preserve inter-arrival times.
	 * 
	 * We define as real time, the time experienced by the data source
	 * while replying the data set. We define as event time, the time
	 * associated with each event stored in the data set, which should
	 * cover the whole 2013. 
	 * 
	 * We read the dataset with a granularity of TIMESPAN minutes at once, 
	 * i.e., we read all the events in TIMESPAN minutes of the simulated 
	 * time at once. 
	 * 
	 * Relying on TIMESPAN (expressed in minutes), it is possible to 
	 * accelerate the dataset by reading multiple events at once. 
	 * 
	 * Relying on SPEEDUP (expressed in milliseconds), it is possible to 
	 * define the (real) time interval between two consecutive minutes of 
	 * the simulated time. Using -1 as value, the dataset is reproduced
	 * with waiting times.
	 *  
	 */
	
	private static final int TIMESPAN = 1; 		// expressed in mins 
	private static final int SPEEDUP = 1000; 	// expressed in ms 
	private static int SHORT_SLEEP = 10;		// expressed in ms 
	
	private Jedis jedis;
	private SimpleDateFormat sdf ;
	private String filename;
	private int redisTimeout;
	private Gson gson;
	
	public DataSource(String filename, String redisUrl, int redisPort){

		this.filename = filename;
		this.jedis = new Jedis(redisUrl, redisPort, redisTimeout);
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.gson = new Gson();

		initialize();
	}
	
	private void initialize(){
		jedis.del(Constants.REDIS_CONSUMED);
		jedis.del(Constants.REDIS_DATA);
	}
	
	@Override
	public void run() {

		BufferedReader br = null;
		LinesBatch linesBatch = null;
	    
		try {
			System.out.println("Initializing... ");
			br = new BufferedReader(new FileReader(filename));
		
			String line = br.readLine();
		    linesBatch = new LinesBatch();
		    long batchInitialTime 	= roundToCompletedMinute(getDropoffDatatime(line));
		    long batchFinalTime 	= computeBatchFinalTime(batchInitialTime);
		    long latestSendingTime 	= System.currentTimeMillis();
			System.out.println(" batch init  " + sdf.format(new Date(batchInitialTime)));
			System.out.println(" batch final " + sdf.format(new Date(batchFinalTime)));

			System.out.println("Read: " + line);
		    linesBatch.addLine(line);
		    
		    while ((line = br.readLine()) != null) {

			    long eventTime = getDropoffDatatime(line);
			    
			    if (eventTime < batchFinalTime){
			    	linesBatch.addLine(line);
			    	continue;
			    }
			    
			    System.out.println("Sending " + linesBatch.getLines().size() + " lines");

			    /* batch is concluded and has to be sent */
			    send(linesBatch);					
			    
			    /* sleep if needed */
			    if (SPEEDUP != -1){
				    long nextBatchInitTime = roundToCompletedMinute(eventTime);
				    long completeIntervalToSkip = SPEEDUP * (int) Math.floor(((double) (nextBatchInitTime - batchFinalTime) / (TIMESPAN * 60 * 1000)));
				    long deltaIntervalToSkip 	= SPEEDUP - (System.currentTimeMillis() - latestSendingTime);
				    
					System.out.println(" sleep for d:" + deltaIntervalToSkip + " + c:" +completeIntervalToSkip);

					if (deltaIntervalToSkip < 0){
						System.out.println("WARNING: consumer is slower than source. A backpressure mechanism has been activated.");
						deltaIntervalToSkip = 0;
					}
					
				    try {
						Thread.sleep(deltaIntervalToSkip + completeIntervalToSkip);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			    	
			    }
			    
			    /* update batch */
			    linesBatch = new LinesBatch();
			    linesBatch.addLine(line);
			    batchInitialTime = roundToCompletedMinute(eventTime);
			    batchFinalTime 	= computeBatchFinalTime(batchInitialTime);
			    latestSendingTime 	= System.currentTimeMillis();

				System.out.println(" batch init  " + sdf.format(new Date(batchInitialTime)));
				System.out.println(" batch final " + sdf.format(new Date(batchFinalTime)));
		    	
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (br != null){
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void send(LinesBatch linesBatch) throws JedisConnectionException{

		String consumed = jedis.get(Constants.REDIS_CONSUMED);
		String data = jedis.get(Constants.REDIS_DATA);

		/* Check erroneous situation */
		if (data != null && consumed != null){

			jedis.del(Constants.REDIS_CONSUMED);
			jedis.del(Constants.REDIS_DATA);
			
		}

		/* Wait if the consumer is still reading data */
		if (data != null && consumed == null){

			while (consumed == null){
				
				try {
					Thread.sleep(SHORT_SLEEP);
				} catch (InterruptedException e) { }
			
				consumed = jedis.get(Constants.REDIS_CONSUMED);
				
			}

		}

		/* Remove lock from Redis */
		jedis.del(Constants.REDIS_CONSUMED);

		/* Send data */
		String serializedBatch = gson.toJson(linesBatch);
		jedis.set(Constants.REDIS_DATA, serializedBatch);

	}
		
	private long getDropoffDatatime(String line){

		long ts = 0;
		
    	try {
    		String[] tokens	=	line.split(",");
			Date dropoff = sdf.parse(tokens[3]);
			ts = dropoff.getTime();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	return ts;
    	
	}

    private long roundToCompletedMinute(long timestamp) {

		Date d = new Date(timestamp);
		Calendar date = new GregorianCalendar();
		date.setTime(d);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime().getTime();
	
	}
    
	private long computeBatchFinalTime(long initialTime){
		
		return initialTime + TIMESPAN * 60 * 1000;
	}
	
	
	/**
	 * This component reads data from the debs dataset
	 * and feeds the Storm topology by publishing data
	 * on Redis. 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* TODO: 
		 * Usage: 
		 * java -jar debs2015gc-1.0.jar it.uniroma2.debs2016gc.DataSource [debs dataset] [redis ip] 
		 */
		
		String file = "/data/debs2015/debs2015_sample.csv";
		DataSource fill = new DataSource(file, "128.130.172.207", 6379);
//		DataSource fill = new DataSource(file, "localhost", 6379);
		Thread th1 = new Thread(fill);
		th1.start();
	}

}
