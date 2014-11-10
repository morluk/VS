package praktikum.server;

class Room {
	private String name;

	private int temperature;

	private int power;
	
	private String address;
	
	public Room(String name, String address) {
		this.name = name;
		this.address = address;
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