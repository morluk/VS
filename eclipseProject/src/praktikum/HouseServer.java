package praktikum;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
		for (int i = 0; i < getRoomCount(); i++) {
			Room currentRoom = getRoom(i);
			currentRoom.setPower(generator.nextInt(10)); // Range 0 <-> 9
			double decission = generator.nextDouble();
			if (decission <= 0.8) {
				//regular temperature more likely
				currentRoom.setTemperature(generator.nextInt(16) +10 ); // Range 10 <-> 25
			}
			else {
				//winter low temperature, summer high temperature (extrem high not possible yet)
				int month = getDate().get(Calendar.MONTH);
				double factor = (double)(2.0 * 3.1415926535897932 * (month / 11.0)); //MONTH in range  0 to 2*PI
				double m = -1.0 * Math.cos(factor * 3.1415926535897932 / 180);	//(-cos(MONTH) winter minus possible)
				int temp = (int)(generator.nextInt(25) * m); //Range 0 <->25 * m
				currentRoom.setTemperature(temp);
			}
		}
	}
	
	private Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		//Clear all fields
	    cal.clear();
	    //set time
	    int year = 2014;
	    int month = 9; //October
	    int date = 1;
	    cal.set(Calendar.YEAR, year);
	    cal.set(Calendar.MONTH, month);
	    cal.set(Calendar.DATE, date);
		return cal;
	}

	public static void main(String[] args) {
		new HouseServer();
	}

}

class Room {
	String name;
	int temperature;
	int power;

	Room() {
	};

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
