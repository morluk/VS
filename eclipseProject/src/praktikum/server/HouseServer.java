package praktikum.server;

import java.util.ArrayList;
import java.util.List;

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
