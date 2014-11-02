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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}