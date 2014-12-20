package praktikum.adminServer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

public class MqListener {

	private Connection connection;
	private MessageConsumer consumer;
	private String destination, hostIp, hostPort;
	boolean connected;

	public MqListener(String hostIp, String hostPort, String destination)
			throws JMSException {
		this.destination = destination;
		this.hostIp = hostIp;
		this.hostPort = hostPort;
		setConnected(false);
	}

	public synchronized boolean isConnected() {
		return connected;
	}


	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void close() throws JMSException {
		if (isConnected()) {
			consumer.close();
			connection.close();
			setConnected(false);
		}
	}

	public void reopen() throws JMSException {
		if (!isConnected()) {
			String user = env("ACTIVEMQ_USER", "admin");
			String password = env("ACTIVEMQ_PASSWORD", "password");
			String host = env("ACTIVEMQ_HOST", hostIp);
			int port = Integer.parseInt(env("ACTIVEMQ_PORT", hostPort));

			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
					"tcp://" + host + ":" + port);

			connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination dest = new ActiveMQTopic(destination);

			consumer = session.createConsumer(dest);
			System.out.println(getClass().getName() + " Waiting for messages...");
			setConnected(true);
		}
	}
	
	public void run() {
		try {
			this.reopen();
			this.receiveMessage();
//			this.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void receiveMessage() throws JMSException {
		while (isConnected()) {
			Message msg = consumer.receiveNoWait();
			if (msg == null) 
				return;
			if (msg instanceof TextMessage) {
				String body = ((TextMessage) msg).getText();
				if ("SHUTDOWN".equals(body)) {
					System.out.println(String.format("Shutting down Listener"));
					connection.close();
					break;
				} else {
					if ("OVER".equals(body)) {
						this.close();
//						break;
				}
					else
						System.out.println(body);
				}

			} else {
				System.out
						.println("Unexpected message type: " + msg.getClass());
			}
		}
	}

	private static String env(String key, String defaultValue) {
		String rc = System.getenv(key);
		if (rc == null)
			return defaultValue;
		return rc;
	}
}
