package praktikum.adminServer;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

public class MqPublisher {
	
	private MessageProducer producer;
	private Session session;
	private Connection connection;
	private int counter = 0;
	
	public MqPublisher(String hostIp, String hostPort, String destination) throws JMSException {
        String mqUser = env("ACTIVEMQ_USER", "admin");
        String mqPassword = env("ACTIVEMQ_PASSWORD", "password");
        String mqHost = env("ACTIVEMQ_HOST", hostIp);
        int mqPort = Integer.parseInt(env("ACTIVEMQ_PORT", hostPort));
        String mqDestination = destination;
        
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://" + mqHost + ":" + mqPort);

        connection = factory.createConnection(mqUser, mqPassword);
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = new ActiveMQTopic(mqDestination);
        producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}
	
	public int getCounter() {
		return counter;
	}
	
	public void resetCounter() {
		counter = 0;
	}

	public void publishMessage(String message) throws JMSException {
        TextMessage msg = session.createTextMessage(message);
//        msg.setIntProperty("id", id);
        producer.send(msg);
        counter++;
	}
	
	public void shutdownConnection() throws JMSException {
        producer.send(session.createTextMessage("SHUTDOWN"));
        connection.close();
	}

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null )
            return defaultValue;
        return rc;
    }
}
