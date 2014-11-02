package praktikum.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * running erlaubt nur 1 Thread
 * Thread sinnvoll weil sonst HouseServer nicht weiter läuft.
 * @author moritz
 *
 */
public class UDPServer extends Thread {
	private static final int PORT = 9998;
	private static final int BUFFER_SIZE = 1024;

	private List<Room> rooms;
	
	private byte data[];

	private boolean running;
	
	private Timer timer = new Timer();
	private int received = 0;
	private int receivedCounter = 0;

	public UDPServer(List<Room> rooms) {
		this.rooms = rooms;
		running = false;
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				resetCounter();
				System.out.println("Packets per Second received: " + receivedCounter);
			}
		}, 0, 1000);
		start();
	}
	
	public void resetCounter() {
		receivedCounter = received;
		received = 0;
	}

	public void stopListening() {
		running = false;
	}

	@Override
	public void run() {
		String stringData;
		DatagramSocket socket;
		DatagramPacket packet;
		String address, name;
		Room room;
		int power;
		int temp;

		if (running) {
			System.out.println("Allready running");
			return;
		}
		running = true;
		System.out.println("UDPServer starts on Port: " + PORT);
		try {
			socket = new DatagramSocket(PORT);
			while (running) {
				clearData();
				packet = new DatagramPacket(data, data.length);
				socket.receive(packet);
				received++;
				address = packet.getAddress().toString();
				name = new String(data).split("#")[0];
				room = getRoom(name, address);
//				System.out.println("data:'" + new String(data) + "'");
				stringData = new String(data).split("#")[1];
				power = Integer.valueOf(stringData.split("&")[0]);
				temp = Integer.valueOf(stringData.split("&")[1]);
				room.setPower(power);
				room.setTemperature(temp);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Room getRoom(String name, String address) {
		for (Room room : this.rooms) {
			if (room.getName().equals(name)) {
				return room;
			}
		}
		Room room = new Room(name, address);
		rooms.add(room);
		return room;
	}
	
	private void clearData() {
		data = new byte[BUFFER_SIZE];
	}
}
