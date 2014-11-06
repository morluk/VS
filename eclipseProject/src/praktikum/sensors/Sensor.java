package praktikum.sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

public class Sensor extends Thread {
	private static final String HOUSESERVER_ADRESS = "localhost";
	private static final int PORT = 9998;
	// milliseconds
	private static final int INTERVAL = 1;

	private byte data[];

	private DatagramSocket socket;

	private Calendar currentTime;
	
	private RandValue randValue = null;
	private String name;

	public Sensor(String name) {
		this.name = name;
		currentTime = getDate();
		randValue = RandValue.getInstance();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		start();
	}

	@Override
	public void run() {
		while (true) {		//repeat forever
			try {
				setRandomNo();
				sendData();
				//System.out.println("sent:'" + (new String(data)) + "'");
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendData() {
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
	 * Format Payload: name#power&temp#
	 */
	public void setRandomNo() {
		String data = name;
		data += "#";
		data += String.valueOf(randValue.getRandomPower());
		data += "&";
		data += String.valueOf(randValue.getRandomTemp(getDate().get(Calendar.MONTH)));
		data += "&";
		data += String.valueOf(Calendar.getInstance().getTimeInMillis());
		data += "#";
		this.data = data.getBytes();

		currentTime.add(Calendar.SECOND, 1);
	}

	private Calendar getDate() {
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
	 * @param args -room int sets No of rooms, Default: 3
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int rooms = 3;
		if (args.length == 2) {
			if (args[0].equals("-room")) {
				rooms = Integer.parseInt(args[1]);
			}
		}
		for (int i=0; i<rooms; i++) {
			Integer name = new Integer(i);
			new Sensor(name.toString());
		}
	}
}
