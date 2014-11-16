package praktikum.sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Sends for each room one UDP packet every interval seconds
 * launch: 1)adjust HOUSESERVER_ADRESS to server IP 2) Adjust INTERVAL in
 * milliseconds 3) Adjust number of rooms 
 * CALL:
 * java Sensor -r int -i interval -ip ip-adress
 * 
 * @author moritz
 * 
 */

public class Sensor implements Runnable {
	private String HOUSESERVER_ADRESS;

	private static final int PORT = 9998;
	// milliseconds
	private static int INTERVAL;

	private DatagramSocket socket;

	private RandValue randValue = null;

	private byte data[];

	public Sensor(String ip) {
		HOUSESERVER_ADRESS = ip;
		randValue = RandValue.getInstance();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Sensor is sending to " + HOUSESERVER_ADRESS + ":" + PORT + " ...");
		int packetId = 0;
		while (true) {
			// repeat forever
			sendData(++packetId, Thread.currentThread().getName());
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send UDP Packet
	 * @param packetId
	 * @param name
	 */
	private synchronized void sendData(int packetId, String name) {
		setRandomNo(packetId, name);
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length,
					InetAddress.getByName(HOUSESERVER_ADRESS), PORT);
			socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Format Payload: name#power&temp#packetId#
	 */
	private synchronized void setRandomNo(int packetId, String name) {
		String data = name;
		data += "#";
		data += String.valueOf(randValue.getRandomPower());
		data += "&";
		data += String.valueOf(randValue.getRandomTemp(getDate().get(
				Calendar.MONTH)));
		data += "&";
		data += String.valueOf(Calendar.getInstance().getTimeInMillis());
		data += "#";
		data += String.valueOf(packetId);
		data += "#";
		this.data = data.getBytes();
	}

	/**
	 * Get Calendar instance with customized date
	 * @return
	 */
	private synchronized Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		// Clear all fields
		cal.clear();
		// set time
		int year = 2014;
		int month = 9; // October
		int date = 1;
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, date);
		return cal;
	}

	/**
	 * 
	 * @param args
	 *            -r int sets No of rooms, Default: 1; -ip ip sets IP of Server,
	 *            Default: localhost; -i interval in milliseconds, Default: 1000
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int rooms = 1;
		String ip = "localhost";
		int interval = 1000;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-r")) {
				rooms = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-ip")) {
				ip = args[i + 1];
			}
			if (args[i].equals("-i")) {
				interval = Integer.parseInt(args[i + 1]);
			}
		}
		// boost speed because only one Sensor object is created
		// Caution avoid race condition between members and methods!
		Sensor.INTERVAL = interval;
		Sensor sensor = new Sensor(ip);
		List<Thread> threads = new ArrayList<Thread>(rooms);
		for (int i = 0; i < rooms; i++) {
			Integer name = new Integer(i);
			Thread t = new Thread(sensor, name.toString());
			t.start();
			threads.add(t);
		}
		// wait until threads return
		for (int i = 0; i < rooms; i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// never reached
	}
}
