package praktikum.sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Random;

public class Sensor {
	private static final String HOUSESERVER_ADRESS = "10.5.23.180";
	private static final int PORT = 9998;
	private static final int INTERVAL = 1;
	private static final int BUFFER_SIZE = 1024;

	private byte data[];

	private DatagramSocket socket;

	private Calendar currentTime;

	public Sensor() {
		data = new byte[BUFFER_SIZE];
		currentTime = getDate();
		try {
			socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void startSimulation() {
		while (true) {
			try {
				setRandomNo();
				sendData();
				System.out.println("sent:'" + (new String(data)) + "'");
				Thread.sleep(1000 * INTERVAL);
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

	public void setRandomNo() {
		Random generator = new Random();
		String data = String.valueOf(generator.nextInt(10));
		data += "&";
		double decission = generator.nextDouble();
		if (decission <= 0.8) {
			data += String.valueOf(generator.nextInt(16) + 10);
		} else {
			int month = currentTime.get(Calendar.MONTH);
			double factor = (double) (2.0 * 3.1415926535897932 * (month / 11.0));
			double m = -1.0 * Math.cos(factor * 3.1415926535897932 / 180);
			int temp = (int) (generator.nextInt(25) * m);
			data += String.valueOf(temp);
		}
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

	public static void main(String[] args) {
		Sensor sensor = new Sensor();
		sensor.startSimulation();
	}
}
