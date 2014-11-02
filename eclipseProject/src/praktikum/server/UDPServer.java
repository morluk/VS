package praktikum.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class UDPServer {
	private static final int PORT = 9998;
	private static final int BUFFER_SIZE = 1024;

	private List<Room> rooms;

	private boolean running;

	public UDPServer(List<Room> rooms) {
		this.rooms = rooms;
		running = false;
		startListening();
	}

	public void stopListening() {
		running = false;
	}

	public void startListening() {
		byte data[] = new byte[BUFFER_SIZE];
		String stringData;
		DatagramSocket socket;
		DatagramPacket packet;
		String address;
		Room room;
		int power;
		int temp;

		if (running) {
			System.out.println("Allready running");
			return;
		}
		running = true;

		try {
			socket = new DatagramSocket(PORT);
			while (running) {
				packet = new DatagramPacket(data, data.length);
				socket.receive(packet);
				address = packet.getAddress().toString();
				room = getRoom(address);
				stringData =  new String(data);
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

	public Room getRoom(String address) {
		for (Room room : this.rooms) {
			if (room.getAddress().equals(address)) {
				return room;
			}
		}
		Room room = new Room("KA", address);
		rooms.add(room);
		return room;

	}
}
