package rollingcount;

import org.apache.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import utils.RabbitMQManager;
import utils.Rankable;
import utils.Rankings;

import java.util.Map;

/*
   To better visualize the results,
   we include an auxiliary operator
   that exports results on a message queue,
   implemented with rabbitMQ.
 */

public class RollingCountExporter extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	private OutputCollector collector;

	private static final Logger LOG = Logger.getLogger(RollingCountExporter.class);



	private RabbitMQManager rabbitmq;
	private String rabbitMqHost;
	private String rabbitMqUsername;
	private String rabbitMqPassword;
	private String defaultQueue;

    public RollingCountExporter(String rabbitMqHost, String rabbitMqUsername, String rabbitMqPassword,
								String defaultQueue) {
		super();
		this.rabbitMqHost = rabbitMqHost;
		this.rabbitMqUsername = rabbitMqUsername;
		this.rabbitMqPassword = rabbitMqPassword;
		this.defaultQueue = defaultQueue;
	}

	@Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector;
        this.rabbitmq = new RabbitMQManager(rabbitMqHost, rabbitMqUsername, rabbitMqPassword, defaultQueue);

    }
    
    @Override
	public void execute(Tuple tuple) {

    	LOG.info("Exp: rec = 0:" + tuple.getValue(0));
        Rankings rankings  = (Rankings) tuple.getValue(0);

		LOG.info("Exp: rec2 = " + rankings);
		LOG.info("Exp: rec3 = " + rankings.getRankings());

		String res = "[";
		for (Rankable r : rankings.getRankings()){
			res += "(" + r.getObject() + ", " + r.getCount() + "); ";
		}
		res+="]";

		LOG.info("Exp: rec4 = " + res);
        rabbitmq.send(res);

    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields("word"));
    
    }

}