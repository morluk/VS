package praktikum.adminServer;

import java.io.IOException;
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

	private MqController mqController;

	private String progStatus;

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

	public MqController getMqController() {
		return mqController;
	}

	public AdminServer(String[] ips, String[] servers, String name,
			String mqIp, String mqPort, int nrOfListeners) {
		this.name = name;
		this.PingMe = ips;
		this.Servers = servers;
		serverCount = ips.length * servers.length;
		// List with rooms for each server
		rooms = new ArrayList<List<Room>>(serverCount);
		for (int i = 0; i < serverCount; i++) {
			rooms.add(new ArrayList<Room>());
		}
		initLogger();
		mqController = new MqController(nrOfListeners, name, mqIp, mqPort);
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
	 */
	public void terminalOutput(String state) {
		if (state.equals("none"))
			return;
		// iterate over lists
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			if (currList.size() == 0)
				break;
			System.out.println("--------------");
			System.out.println("Flat No. " + i + ": ");
			System.out.println("--------------");
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
		}
	}

	public void publishToMom() {
		try {
			mqController.publishAlert(rooms);
			mqController.publishStatus(rooms);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// xmlRpc Server port
		int startPort = 8000;
		// xmlRpc Server port
		int endPort = 8001;
		// for Sleeping until next RPCCall
		int INTERVAL = 5000;
		// for Logging XMLCalls
		int TIMESPAN = 10;
		// XMLRPC ServerIp
		String serverIp = "localhost";
		// default Ausgabe
		String output = "yesAlert";
		// name == 1/2/3/4/n
		String name = "1";
		// brokerIp
		String mqIp = "localhost";
		// brokerPort
		String mqPort = "61616";
		// how many other AdminServers are there?
		int noOfListeners = 3;

		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-sp")) {
				startPort = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-ep")) {
				endPort = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-t")) {
				INTERVAL = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-ip")) {
				serverIp = args[i + 1];
			} else if (args[i].equals("-o")) {
				output = args[i + 1];
			} else if (args[i].equals("-n")) {
				name = args[i + 1];
			} else if (args[i].equals("-mqIp")) {
				mqIp = args[i + 1];
			} else if (args[i].equals("-mqPort")) {
				mqPort = args[i + 1];
			} else if (args[i].equals("-li")) {
				noOfListeners = Integer.parseInt(args[i + 1]);
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
				mqPort, noOfListeners);
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
			// subscribe, detach Listeners and Output
			case "alert":
				output = "yesAlert";
				break;
			case "status":
				output = "yesAll";
				break;
			case "none":
				output = "none";
				break;
			default:
				// changeMode Command has to be: "+1/+2../+n"
				if (actProgStatus.startsWith("+")) {
					actProgStatus = actProgStatus.substring(1);
					adminServer.getMqController().changeListenerMode(
							actProgStatus);
				}
				break;
			}
			// fuer naechsten Durchlauf ProgStatus ungueltig machen
			adminServer.setProgStatus("xxx");
			// XMLRPC Call to HouseServer
			adminServer.callRpcClients();
			adminServer.terminalOutput(output);
			adminServer.publishToMom();
			adminServer.getMqController().checkListeners();
			counter++;
			// TIMESPAN for counting RPC/MQCalls reached?
			if (Calendar.getInstance().getTimeInMillis() > cal
					.getTimeInMillis()) {
				String msg = "RPC Calls in " + TIMESPAN + " seconds:\t"
						+ counter;
				msg += " (RPC Server: " + (PingMe.length * Servers.length);
				msg += " / Call each every " + INTERVAL + " milliSeconds)";
				Logger.getLogger(AdminServer.class.getName()).log(Level.INFO,
						msg);
				msg = "";
				int publisherCounter = adminServer.getMqController().getAlertPublisherCounter() + adminServer.getMqController().getStatusPublisherCounter();
				int listenerCounter = adminServer.getMqController().getAlertListenerCounter() + adminServer.getMqController().getStatusListenerCounter();
				msg += "ActiveMq Published Messages: " + publisherCounter + " - ActiveMq Received Messages: " + listenerCounter;
				msg += " (Published to StatusQueue: " + adminServer.getMqController().getStatusPublisherCounter() +" / to AlertQueue: " + adminServer.getMqController().getAlertPublisherCounter() + ")";
				Logger.getLogger(AdminServer.class.getName()).log(Level.INFO,
						msg);
				counter = 0;
				adminServer.getMqController().resetAllCounter();
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
