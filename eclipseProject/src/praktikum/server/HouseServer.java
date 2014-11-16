package praktikum.server;

import java.util.ArrayList;
import java.util.List;

/**
 * HouseServer has TCP Server listening on port 9999 for 
 * HTTP requests and UDP Server listening on port 9998 for
 * custom Sensor packets.
 * @author moritz
 *
 */

public class HouseServer {
	private List<Room> rooms;

	public HouseServer() {
		rooms = new ArrayList<Room>();
		new MultithreadedTCPServer(this);
		new UDPServer(rooms);
	}

	public int getRoomCount() {
		return rooms.size();
	}

	public Room getRoom(int pos) {
		return rooms.get(pos);
	}

	public static void main(String[] args) {
		new HouseServer();
	}
}
