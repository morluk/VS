package praktikum.adminServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.jms.JMSException;

import praktikum.server.Room;

public class AdminServer {

	String name;

	// Liste aller URLs deren Server aufgerufen werden sollen
	private String PingMe[];
	// Liste aller Servernamen auf einem Port
	private String Servers[];

	private int serverCount;

	private List<List<Room>> rooms;

	private String progStatus, listen_1 = "status",listen_2 = "status",listen_3 = "status";

	private InetAddress myIP;

	private MqPublisher statusPublisher;

	private MqPublisher alertPublisher;

	private MqListener statusListener_1, alertListener_1, statusListener_2,
			alertListener_2, statusListener_3, alertListener_3;

	String mqIp;

	String mqPort;

	private static Logger myLogger = Logger.getLogger(AdminServer.class
			.getName());
	private static FileHandler myFilehandler;

	/**
	 * progStatus is Set from Input Thread and read from AminServer main() to
	 * change Published and Listened Messages
	 * 
	 * @return
	 */
	public synchronized String getProgStatus() {
		return progStatus;
	}

	/**
	 * progStatus is Set from Input Thread and read from AminServer main() to
	 * change Published and Listened Messages
	 * 
	 * @return
	 */
	public synchronized void setProgStatus(String progStatus) {
		this.progStatus = progStatus;
	}

	public AdminServer(String[] ips, String[] servers, String name,
			String mqIp, String mqPort) {
		this.name = name;
		this.PingMe = ips;
		this.Servers = servers;
		serverCount = ips.length * servers.length;
		this.mqIp = mqIp;
		this.mqPort = mqPort;
		// List with rooms for each server
		rooms = new ArrayList<List<Room>>(serverCount);
		for (int i = 0; i < serverCount; i++) {
			rooms.add(new ArrayList<Room>());
		}
		initLogger();
		try {
			statusPublisher = new MqPublisher(mqIp, mqPort, "status_" + name);
			alertPublisher = new MqPublisher(mqIp, mqPort, "alert_" + name);
			// verschiedene destinations fuer versch Haeuser. name == 1/2/3/4
			if (name.contains("1")) {
				statusListener_1 = new MqListener(mqIp, mqPort, "status_2");
				alertListener_1 = new MqListener(mqIp, mqPort, "alert_2");
				statusListener_2 = new MqListener(mqIp, mqPort, "status_3");
				alertListener_2 = new MqListener(mqIp, mqPort, "alert_3");
				statusListener_3 = new MqListener(mqIp, mqPort, "status_4");
				alertListener_3 = new MqListener(mqIp, mqPort, "alert_4");
			} else if (name.contains("2")) {
				statusListener_1 = new MqListener(mqIp, mqPort, "status_1");
				alertListener_1 = new MqListener(mqIp, mqPort, "alert_1");
				statusListener_2 = new MqListener(mqIp, mqPort, "status_3");
				alertListener_2 = new MqListener(mqIp, mqPort, "alert_3");
				statusListener_3 = new MqListener(mqIp, mqPort, "status_4");
				alertListener_3 = new MqListener(mqIp, mqPort, "alert_4");
			} else if (name.contains("3")) {
				statusListener_1 = new MqListener(mqIp, mqPort, "status_1");
				alertListener_1 = new MqListener(mqIp, mqPort, "alert_1");
				statusListener_2 = new MqListener(mqIp, mqPort, "status_2");
				alertListener_2 = new MqListener(mqIp, mqPort, "alert_2");
				statusListener_3 = new MqListener(mqIp, mqPort, "status_4");
				alertListener_3 = new MqListener(mqIp, mqPort, "alert_4");
			} else if (name.contains("4")) {
				statusListener_1 = new MqListener(mqIp, mqPort, "status_1");
				alertListener_1 = new MqListener(mqIp, mqPort, "alert_1");
				statusListener_2 = new MqListener(mqIp, mqPort, "status_2");
				alertListener_2 = new MqListener(mqIp, mqPort, "alert_2");
				statusListener_3 = new MqListener(mqIp, mqPort, "status_3");
				alertListener_3 = new MqListener(mqIp, mqPort, "alert_3");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		// save own IP
		myIP = null;
		try {
			myIP = InetAddress.getLocalHost();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initializes Logger for Exceptionhandling
	 */
	private void initLogger() {
		myLogger.setUseParentHandlers(false);
		Handler[] handlers = myLogger.getHandlers();
		for (Handler handler : handlers) {
			if (handler.getClass() == ConsoleHandler.class)
				myLogger.removeHandler(handler);
		}
		try {
			myFilehandler = new FileHandler("%t/AdminServer_" + name
					+ "_LogFile.log", true);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		myLogger.addHandler(myFilehandler);
		myLogger.setLevel(Level.ALL);
		SimpleFormatter formatter = new SimpleFormatter();
		myFilehandler.setFormatter(formatter);
	}

	/**
	 * start Clients for each Combination of ip and Servername and fills
	 * List<Room>
	 */
	public void callRpcClients() {
		for (int i = 0; i < PingMe.length; i++) {
			for (int j = 0; j < Servers.length; j++) {
				MyXmlRpcClient myXmlRpcClient = new MyXmlRpcClient(PingMe[i],
						Servers[j], rooms.get(i * (Servers.length) + j));
				myXmlRpcClient.callRpc();
			}
		}
	}

	/**
	 * put Results on screen. Includes Calculation of Max/Min values
	 * 
	 * @throws JMSException
	 */
	public void terminalOutput(String state) throws JMSException {
		// Receive Messages from MoM
		switch (this.getListen_1()) {
		case "status":
			alertListener_1.close();
			statusListener_1.run();
			break;
		case "alert":
			alertListener_1.run();
			break;
		case "":
			statusListener_1.close();
			break;
		default:
		}
		switch (this.getListen_2()) {
		case "status":
			alertListener_2.close();
			statusListener_2.run();
			break;
		case "alert":
			alertListener_2.run();
			break;
		case "":
			statusListener_2.close();
			break;
		default:
		}
		switch (this.getListen_3()) {
		case "status":
			alertListener_3.close();
			statusListener_3.run();
			break;
		case "alert":
			alertListener_3.run();
			break;
		case "":
			statusListener_3.close();
			break;
		default:
		}
		statusPublisher.publishMessage("START Status From AdminServer: "
				+ this.name + ": " + myIP);
		alertPublisher.publishMessage("START Alert From AdminServer: "
				+ this.name + ": " + myIP);
		// iterate over lists
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			if (currList.size() == 0)
				break;
			if (!state.equals("none")) {
				System.out.println("---------------");
				System.out.println("House No. " + i + ": ");
				System.out.println("---------------");
			}
			// Send Message to MoM
			statusPublisher.publishMessage("---------------");
			statusPublisher.publishMessage("House No. " + i + ": ");
			statusPublisher.publishMessage("---------------");
			alertPublisher.publishMessage("---------------");
			alertPublisher.publishMessage("House No. " + i + ": ");
			alertPublisher.publishMessage("---------------");
			int highestTemp = currList.get(0).getTemperature();
			int lowestTemp = currList.get(0).getTemperature();
			int totalPower = 0;
			// iterate over rooms
			for (int j = 0; j < currList.size(); j++) {
				// terminalOutput
				if (state.equals("yesAll")) {
					System.out.println("Room " + j + " - Temp:\t"
							+ currList.get(j).getTemperature() + "\t- Power:\t"
							+ currList.get(j).getPower());
				} else {
					// Output only if values are critical
					if (currList.get(j).getTemperature() < 10
							|| currList.get(j).getTemperature() > 30
							|| currList.get(j).getPower() > 2) {
						if (state.equals("yesAlert")) {
							System.out.println("Room " + j + " - Temp:\t"
									+ currList.get(j).getTemperature()
									+ "\t- Power:\t"
									+ currList.get(j).getPower());
						}
					}
				}
				// Mom Output
				if (currList.get(j).getTemperature() < 10
						|| currList.get(j).getTemperature() > 30
						|| currList.get(j).getPower() > 2) {
					// Send Alert Message to MoM
					alertPublisher.publishMessage("Room " + j + " - Temp:\t"
							+ currList.get(j).getTemperature() + "\t- Power:\t"
							+ currList.get(j).getPower());
				}
				// Send Status Message to MoM always
				statusPublisher.publishMessage("Room " + j + " - Temp:\t"
						+ currList.get(j).getTemperature() + "\t- Power:\t"
						+ currList.get(j).getPower());
				totalPower += currList.get(j).getPower();
				if (highestTemp < currList.get(j).getTemperature())
					highestTemp = currList.get(j).getTemperature();
				if (lowestTemp > currList.get(j).getTemperature())
					lowestTemp = currList.get(j).getTemperature();
			}
			if (state.equals("yesAll")) {
				System.out.println("HighestTemp of House No. " + i + " :\t"
						+ highestTemp);
				System.out.println("LowestTemp of House No. " + i + " :\t"
						+ lowestTemp);
				System.out.println("TotalPower of House No. " + i + " :\t"
						+ totalPower);
			}
			// Send Status Message to MoM always
			statusPublisher.publishMessage("HighestTemp of House No. " + i
					+ " :\t" + highestTemp);
			statusPublisher.publishMessage("LowestTemp of House No. " + i
					+ " :\t" + lowestTemp);
			statusPublisher.publishMessage("TotalPower of House No. " + i
					+ " :\t" + totalPower);
		}
		statusPublisher.publishMessage("STOP Status From AdminServer "
				+ this.name + ": " + myIP);
		statusPublisher.publishMessage("OVER");
		alertPublisher.publishMessage("STOP Alert From AdminServer "
				+ this.name + ": " + myIP);
		alertPublisher.publishMessage("OVER");
	}

	public synchronized String getListen_1() {
		return listen_1;
	}

	public synchronized void setListen_1(String listen_1) {
		this.listen_1 = listen_1;
	}

	public synchronized String getListen_2() {
		return listen_2;
	}

	public synchronized void setListen_2(String listen_2) {
		this.listen_2 = listen_2;
	}

	public synchronized String getListen_3() {
		return listen_3;
	}

	public synchronized void setListen_3(String listen_3) {
		this.listen_3 = listen_3;
	}

	public static void main(String[] args) {

		int startPort = 8000;

		int endPort = 8001;
		// for Sleeping until next RPCCall
		int INTERVAL = 5000;
		// for Logging XMLCalls
		int TIMESPAN = 10;
		//XMLRPC ServerIp
		String serverIp = "localhost";
		// default Ausgabe
		String output = "none";
		// name == 1/2/3/4
		String name = "1";
		//brokerIp
		String mqIp = "localhost";

		String mqPort = "61616";

		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-sp")) {
				startPort = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-ep")) {
				endPort = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-t")) {
				INTERVAL = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-ip")) {
				serverIp = args[i + 1];
			}
			if (args[i].equals("-o")) {
				output = args[i + 1];
			}
			if (args[i].equals("-n")) {
				name = args[i + 1];
			}
			if (args[i].equals("-mqIp")) {
				mqIp = args[i + 1];
			}
			if (args[i].equals("-mqPort")) {
				mqPort = args[i + 1];
			}
		}

		String PingMe[] = new String[endPort - startPort];

		// Multiple Server auf versch. IPs/Ports
		for (int i = startPort; i < endPort; i++) {
			PingMe[i - startPort] = "http://" + serverIp + ":" + i + "/xmlrpc";
		}

		// Multiple Server auf einem Port
		String Servers[] = new String[] { "MyXmlRpcServer" };

		AdminServer adminServer = new AdminServer(PingMe, Servers, name, mqIp,
				mqPort);
		adminServer.setProgStatus(output); // default
		// start Input als Thread
		Thread t = new Thread(new Input(adminServer));
		t.setDaemon(true);
		t.start();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, TIMESPAN);

		int counter = 0;

		while (true) {
			String actProgStatus = adminServer.getProgStatus();
			switch (actProgStatus) {
			// TODO: hier kommen subscribe, detach hin!!!
			case "alert":
				output = "yesAlert";
				break;
			case "status":
				output = "yesAll";
				break;
			case "none":
				output = "none";
				break;
			case "+1":
				if (adminServer.getListen_1().equals(""))
					adminServer.setListen_1("alert");
				else if (adminServer.getListen_1().equals("alert"))
					adminServer.setListen_1("status");
				else if (adminServer.getListen_1().equals("status"))
					adminServer.setListen_1("");
				break;
			case "+2":
				if (adminServer.getListen_2().equals(""))
					adminServer.setListen_2("alert");
				else if (adminServer.getListen_2().equals("alert"))
					adminServer.setListen_2("status");
				else if (adminServer.getListen_2().equals("status"))
					adminServer.setListen_2("");
				break;
			case "+3":
				if (adminServer.getListen_3().equals(""))
					adminServer.setListen_3("alert");
				else if (adminServer.getListen_3().equals("alert"))
					adminServer.setListen_3("status");
				else if (adminServer.getListen_3().equals("status"))
					adminServer.setListen_3("");
				break;
			default:
			}
			// fuer naechsten Durchlauf ProgStatus ungueltig machen
			adminServer.setProgStatus("xxx");
			// XMLRPC Call to HouseServer
			adminServer.callRpcClients();
			try {
				// terminal and MQ Output
				adminServer.terminalOutput(output);
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
			counter++;
			// TIMESPAN for counting RPC/MQCalls reached?
			if (Calendar.getInstance().getTimeInMillis() > cal
					.getTimeInMillis()) {
				String msg = "RPC Calls in " + TIMESPAN + " seconds (calling"
						+ " each of " + (PingMe.length * Servers.length)
						+ " Host every " + INTERVAL / 1000 + " seconds):\t"
						+ counter;
				Logger.getLogger(AdminServer.class.getName()).log(Level.INFO,
						msg);
				counter = 0;
				cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, TIMESPAN);
			}
			// Wait INTERVAL for next Calls
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
