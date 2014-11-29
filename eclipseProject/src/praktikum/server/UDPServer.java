package praktikum.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Listens on port 9998 for UDP packets from Sensor
 * @author moritz
 * 
 */
public class UDPServer extends Thread {
	private static final int PORT = 9998;
	
	private static final int BUFFER_SIZE = 1024;
	// milliseconds
	private static final int INTERVAL = 1000;

	private List<Room> rooms;

	private byte data[];

	private boolean running;

	private Timer timer = new Timer();

	private int received = 0;

	private int delaySum = 0;

	private int idRecievedCounter = 0;

	private int idLostCounter = 0;

	public UDPServer(List<Room> rooms) {
		this.rooms = rooms;
		running = false;
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
//				System.out.println("Packets per Second received: "
//						+ received
//						+ " Average Delay per Second: "
//						+ (received > 0 ? delaySum / received
//								: delaySum));
//				System.out.println("IDRecieved: " + idRecievedCounter
//						+ " IDLost: " + idLostCounter);
				resetCounter();
			}
		}, 0, INTERVAL);
		start();
	}

	public void resetCounter() {
		received = 0;
		delaySum = 0;
		idLostCounter = 0;
		idRecievedCounter = 0;
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
				String dataStr = new String(data);
				name = dataStr.split("#")[0];
				room = getRoom(name, address);
				// set counters corresponding to lastIdRecieved
				String currIdStr = dataStr.split("#")[2];
				int currIdInt = Integer.parseInt(currIdStr);
				int lastId = new Integer(room.getLastIdRecieved());
				if (currIdInt > lastId) {
					// recieved in order
					room.setLastIdRecieved(currIdInt);
					idRecievedCounter++;
					if (currIdInt > (lastId + 1)) {
						// packet in between lost
						int difference = Math.abs(currIdInt - lastId - 1);
						idLostCounter += difference;
					}
					// extract data
					stringData = dataStr.split("#")[1];
					power = Integer.valueOf(stringData.split("&")[0]);
					temp = Integer.valueOf(stringData.split("&")[1]);
					room.setPower(power);
					room.setTemperature(temp);
					delaySum += Calendar.getInstance().getTimeInMillis()
							- Long.valueOf(stringData.split("&")[2]);
				} else if (currIdInt < lastId) {
					// recieved out of order
					idRecievedCounter++;
					idLostCounter--;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns reference to room or creates new room with parameters
	 * @param name
	 * @param address
	 * @return
	 */
	private Room getRoom(String name, String address) {
		for (Room room : this.rooms) {
			if (room.getName().equals(name) && room.getAddress().equals(address)) {
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
