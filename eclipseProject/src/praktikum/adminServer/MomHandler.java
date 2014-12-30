package praktikum.adminServer;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

public class MomHandler {
	private static final String user = "admin";

	private static final String password = "password";

	private static final String host = "localhost";

	private static final int port = 61616;

	private Session session;

	public MomHandler() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
				"tcp://" + host + ":" + port);

		Connection connection = factory.createConnection(user, password);
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public void sendToQueue(String queue, String text) throws JMSException {
		MessageProducer producer = session.createProducer(new ActiveMQTopic(
				queue));
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		TextMessage msg = session.createTextMessage(text);
		producer.send(msg);
	}

	public String recieveFromQueue(String queue) throws JMSException {
		MessageConsumer consumer = session.createConsumer(new ActiveMQTopic(
				queue));
		Message msg = consumer.receiveNoWait();
		
		if (msg == null) {
			return "";
		}

		return ((TextMessage) msg).getText();
	}
}
