package praktikum.server;

// 22.10. 10

/**
 *
 * @author Peter Altenberd
 * (Translated into English by Ronald Moore)
 * Computer Science Dept.                   Fachbereich Informatik
 * Darmstadt Univ. of Applied Sciences      Hochschule Darmstadt
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EchoService extends Thread {
	Socket client;
	HouseServer houseServer;

	EchoService(Socket client, HouseServer home) {
		this.client = client;
		this.houseServer = home;
	}

	@Override
	public void run() {
		String line;
		BufferedReader fromClient;
		DataOutputStream toClient;
		boolean verbunden = true;
		System.out.println("Thread started: " + this); // Display Thread-ID
		try {
			fromClient = new BufferedReader // Datastream FROM Client
			(new InputStreamReader(client.getInputStream()));
			toClient = new DataOutputStream(client.getOutputStream()); // TO
																		// Client
			while (verbunden) { // repeat as long as connection exists
				line = ".";
				while (!line.equals("")) { // read & ignore HTTP Requests until
											// empty line
					line = fromClient.readLine();
					System.out.println("Received: " + line);
				}
				toClient.writeBytes("HTTP/1.1 200 OK\n");
				System.out.println("Sent: HTTP/1.1 200 OK");
				DateFormat df = new SimpleDateFormat(
						"EEE, dd MMM yyyy HH:mm:ss zzz"); // Date
				Date date = Calendar.getInstance().getTime();
				String reportDate = df.format(date);
				toClient.writeBytes(reportDate + "\n");
				System.out.println("Sent: " + reportDate);
				toClient.writeBytes("Server: Own\n");
				System.out.println("Sent: Server: Own");
				toClient.writeBytes("Content-Type: text/html\n");
				System.out.println("Sent: Content-Type: text/html");
				// ContentLength optional
				toClient.writeBytes("\n"); // blank row
				System.out.println("Sent: " + "");
				// write htmlbody into Stream
				String htmlBody = new String(createHtmlBody());
				toClient.writeBytes(htmlBody + "\n");
				System.out.println("Sent: " + htmlBody);
				// Close Connection
				verbunden = false;
			}
			fromClient.close();
			toClient.close();
			client.close(); // End
			System.out.println("Thread ended: " + this);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// read Htmlfile
	private String createHtmlBody() throws Exception {
		String result = new String();
		File htmlFile = new File("resource/index.html");
		if (!htmlFile.exists()) {
			htmlFile = new File("../resource/index.html");
		}
		BufferedReader htmlReader = new BufferedReader(new FileReader(htmlFile));
		String htmlLine;
		while ((htmlLine = htmlReader.readLine()) != null) {
			htmlLine = htmlLine.trim();
			if (htmlLine.contains("placeholder_Article")) {
				for (int i = 0; i < houseServer.getRoomCount(); i++) {
					Room currentRoom = houseServer.getRoom(i);
					result += "<article>";
					result += "<h2>" + currentRoom.getName() + "</h2>";
					result += "<table>";
					result += "<tr><td>Address</td><td></td><td>"
							+ currentRoom.getAddress()
							+ "</td></tr>";
					result += "<tr><td>Room Temperature</td><td></td><td>"
							+ currentRoom.getTemperature()
							+ " Degree Celsius</td></tr>";
					result += "<tr><td>Power Consumption</td><td></td><td>"
							+ currentRoom.getPower() + " KW</td></tr>";
					result += "</table>";
					result += "</article>";
				}
			} else {
				result += htmlLine;
			}
		}
		htmlReader.close();
		return result;
	}
}
