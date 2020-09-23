package it.uniroma2.debs2015gc.utils;

import com.rabbitmq.client.*;
import it.uniroma2.debs2015gc.operator.Constants;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class RabbitMQManager {

	private String host;
	private String username;
	private String password;
	private ConnectionFactory factory;
	private Connection connection;
	
	public RabbitMQManager(String host, String username, String password) {
		super();
		this.host = host;
		this.username = username;
		this.password = password;
		
		this.factory = null;
		this.connection = null;
		
		this.initialize();

		this.initializeQueue();

	}

	public void initializeQueue(){

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(username);
		factory.setPassword(password);
		Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			boolean durable = false;
			boolean exclusive = false; 
			boolean autoDelete = false;
            
		    channel.queueDeclare(Constants.RABBITMQ_RESULTS, durable, exclusive, autoDelete, null);

		    channel.close();
            connection.close();
            
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initialize(){
		
		factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(username);
		factory.setPassword(password);
		try {
		
			connection = factory.newConnection();
		            
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void terminate(){
		
		if (connection != null && connection.isOpen()){
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private boolean reopenConnectionIfNeeded(){

		try {
			
			if (connection == null){
				connection = factory.newConnection();
				return true;
			}
			
			if (!connection.isOpen()){
				connection = factory.newConnection();
			}
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	public boolean send(String message){
		
		return send(Constants.RABBITMQ_RESULTS, message);
		
	}
	
	public boolean send(String queue, String message){
		
		try {
		
			boolean c = reopenConnectionIfNeeded();
			
			if (c == false)
				throw new IOException("Unable to open a connection to RabbitMQ");
			
			Channel channel = connection.createChannel();
			channel.basicPublish("", queue, null, message.getBytes());
			channel.close();

			return true;

		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}

		return false;
		
	}

	public boolean createDetachedReader(String queue) {

		try {

			reopenConnectionIfNeeded();

			Channel channel = connection.createChannel();

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
										   byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					message = System.currentTimeMillis() + "," + message;
					System.out.println(message);
				}
			};
			channel.basicConsume(queue, true, consumer);

			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;

	}

}
