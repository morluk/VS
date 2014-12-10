package praktikum.adminServer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import praktikum.server.Room;

public class AdminServer {

	// Liste aller URLs deren Server aufgerufen werden sollen
	private String PingMe[];
	// Liste aller Servernamen auf einem Port
	private String Servers[];

	private int serverCount;

	private List<List<Room>> rooms;

	public AdminServer(String[] ips, String[] servers) {
		this.PingMe = ips;
		this.Servers = servers;
		serverCount = ips.length * servers.length;
		// List with rooms for each server
		rooms = new ArrayList<List<Room>>(serverCount);
		for (int i = 0; i < serverCount; i++) {
			rooms.add(new ArrayList<Room>());
		}
	}

	/**
	 * start Clients for each Combination of ip and Servername
	 */
	public void startClients() {
		for (int i = 0; i < PingMe.length; i++) {
			for (int j = 0; j < Servers.length; j++) {
				MyXmlRpcClient myXmlRpcClient = new MyXmlRpcClient(PingMe[i],
						Servers[j], rooms.get(i * (Servers.length) + j));
				myXmlRpcClient.run();
			}
		}
	}

	/**
	 * put Results on screen. Includes Calculation of Max/Min values
	 */
	public void terminalOutput() {
		// iterate over lists
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			System.out.println("---------------");
			System.out.println("House No. " + i + ": ");
			System.out.println("---------------");
			int highestTemp = currList.get(0).getTemperature();
			int lowestTemp = currList.get(0).getTemperature();
			int totalPower = 0;
			// iterate over rooms
			for (int j = 0; j < currList.size(); j++) {
				System.out.println("Room " + j + " - Temp:\t"
						+ currList.get(j).getTemperature() + "\t- Power:\t"
						+ currList.get(j).getPower());
				totalPower += currList.get(j).getPower();
				if (highestTemp < currList.get(j).getTemperature())
					highestTemp = currList.get(j).getTemperature();
				if (lowestTemp > currList.get(j).getTemperature())
					lowestTemp = currList.get(j).getTemperature();
			}
			System.out.println("HighestTemp of House No. " + i + " :\t"
					+ highestTemp);
			System.out.println("LowestTemp of House No. " + i + " :\t"
					+ lowestTemp);
			System.out.println("TotalPower of House No. " + i + " :\t"
					+ totalPower);
		}
	}

	public static void main(String[] args) {

		int startPort = Integer.parseInt(args[0]);

		int endPort = Integer.parseInt(args[1]);

		int TIMESPAN = Integer.parseInt(args[2]);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, TIMESPAN);

		int counter = 0;

		String PingMe[] = new String[endPort - startPort];

		int INTERVALL = 60000;
		// TODO: Multiple Server auf versch. IPs/Ports
		for (int i = startPort; i < endPort; i++) {
			PingMe[i - startPort] = "http://" + args[3] + ":" + i + "/xmlrpc";
		}

		// TODO: Multiple Server auf einem Port
		String Servers[] = new String[] { "MyXmlRpcServer" };

		AdminServer adminServer = new AdminServer(PingMe, Servers);

		while (Calendar.getInstance().getTimeInMillis() < cal.getTimeInMillis()) {
			adminServer.startClients();
			// adminServer.terminalOutput();
			counter++;
		}

		System.out.println("Counter: " + counter);

	}
}
