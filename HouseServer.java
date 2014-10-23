package praktikum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HouseServer {
	List<Room> rooms;
	MultithreadedTCPServer tcpServer;
	
	HouseServer() {
		rooms = new ArrayList<Room>();
		rooms.add(new Room("Wohnzimmer", 22, 22));
		rooms.add(new Room("Schlafzimmer", 22, 22));
		rooms.add(new Room("Kueche", 22, 22));
		setRandomNo();
		tcpServer = new MultithreadedTCPServer(this);
	}
	
	int getRoomCount() {
		return rooms.size();
	}
	
	Room getRoom(int pos) {
		return rooms.get(pos);
	}
	
	public void setRandomNo() {
		Random generator = new Random();
		for (int i=0; i< getRoomCount(); i++) {
			Room currentRoom = getRoom(i);
			currentRoom.setPower(generator.nextInt(150)); // Range 0 <-> 149
			currentRoom.setTemperature(generator.nextInt(70)-20); // Range -20 <-> 49
		}
	}

	public static void main(String[] args) {
		HouseServer home = new HouseServer();
	}

}

class Room {
	String name;
	int temperature;
	int power;
	
	Room() {};
	Room(String newName, int newTemp, int newPower) {
		this.name = newName;
		this.temperature = newTemp;
		this.power = newPower;
	}
	public int getTemperature() {
		return temperature;
	}
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	public String getName() {
		return name;
	}
}
