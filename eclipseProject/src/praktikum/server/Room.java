package praktikum.server;

/**
 * Critical section between TCPServer and UDPServer
 * @author moritz
 *
 */

class Room {
	private String name;

	private int temperature;

	private int power;
	
	private String address;
	
	private int lastIdRecieved = 0;
	
	public Room(String name, String address) {
		this.name = name;
		this.address = address;
	}

	public synchronized int getLastIdRecieved() {
		return lastIdRecieved;
	}

	public synchronized void setLastIdRecieved(int lastIdRecieved) {
		this.lastIdRecieved = lastIdRecieved;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized int getTemperature() {
		return temperature;
	}

	public synchronized void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public synchronized int getPower() {
		return power;
	}

	public synchronized void setPower(int power) {
		this.power = power;
	}

	public String getName() {
		return name;
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized void setAddress(String address) {
		this.address = address;
	}
}