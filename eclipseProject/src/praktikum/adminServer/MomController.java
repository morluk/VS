package praktikum.adminServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;

import praktikum.server.Room;

public class MomController extends Thread {

	private MomHandler momHandler;

	private List<List<Room>> rooms;

	private String ownName;

	private Map<String, MomController.ReadingQueueThread> readingQueueThreads;

	private int sleepTime;

	private boolean running = false;

	private boolean output;

	private Integer sendCounter = 0;

	private Map<String, Integer> recievedCounter;

	public MomController(List<List<Room>> rooms, String ownName, int sleepTime,
			boolean output, int listenerCount) {
		this.readingQueueThreads = new HashMap<String, MomController.ReadingQueueThread>();

		this.ownName = ownName;

		this.rooms = rooms;

		try {
			this.momHandler = new MomHandler();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		this.output = output;

		this.sleepTime = sleepTime;

		this.recievedCounter = new HashMap<String, Integer>();

		for (int i = 0; i < listenerCount; i++) {
			recievedCounter.put(String.valueOf(i), 0);
		}

		// ReadingQueueThread thread = new ReadingQueueThread("critical",
		// momHandler);
		// thread.start();
		// readingQueueThreads.put("critical", thread);

		// thread = new ReadingQueueThread(ownName, momHandler);
		// thread.start();
		// readingQueueThreads.put(ownName, thread);

		// thread = new ReadingQueueThread("house0", momHandler);
		// thread.start();
		// readingQueueThreads.put(ownName, thread);

	}

	@Override
	public void run() {
		super.run();
		running = true;
		while (running) {
			sendValues();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopThread() {
		for (Entry<String, ReadingQueueThread> thread : readingQueueThreads
				.entrySet()) {
			thread.getValue().stopThread();
			try {
				thread.getValue().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.running = false;
	}

	private void sendValues() {
		// iterate over lists
		// boolean isCritical = false;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			if (currList.size() == 0)
				break;
			builder.append("HouseAdmin " + ownName + "\n");
			builder.append("---------------\n");
			builder.append("House No. " + i + ": \n");
			builder.append("---------------\n");
			int highestTemp = currList.get(0).getTemperature();
			int lowestTemp = currList.get(0).getTemperature();
			int totalPower = 0;
			// iterate over rooms
			for (int j = 0; j < currList.size(); j++) {
				builder.append("Room " + j + " - Temp:\t"
						+ currList.get(j).getTemperature() + "\t- Power:\t"
						+ currList.get(j).getPower() + "\n");
				totalPower += currList.get(j).getPower();
				if (highestTemp < currList.get(j).getTemperature())
					highestTemp = currList.get(j).getTemperature();
				if (lowestTemp > currList.get(j).getTemperature())
					lowestTemp = currList.get(j).getTemperature();
			}

			if (highestTemp > 30) {
				// isCritical = true;
			}

			builder.append("HighestTemp of House No. " + i + " :\t"
					+ highestTemp + "\n");
			builder.append("LowestTemp of House No. " + i + " :\t" + lowestTemp
					+ "\n");
			builder.append("TotalPower of House No. " + i + " :\t" + totalPower
					+ "\n");
		}
		try {
			// if (isCritical) {
			// momHandler.sendToQueue("critical", builder.toString());
			// }
			momHandler.sendToQueue(ownName, builder.toString());
			synchronized (sendCounter) {
				sendCounter++;
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private class ReadingQueueThread extends Thread {
		private String queue;

		private MomHandler momHandler;

		private boolean running;

		public ReadingQueueThread(String queue, MomHandler momHandler) {
			this.queue = queue;
			this.momHandler = momHandler;
		}

		@Override
		public void run() {
			super.run();
			running = true;
			while (running) {
				try {
					String text = momHandler.recieveFromQueue(queue);
					if (!text.equals("")) {
						synchronized (recievedCounter) {
							recievedCounter.put(queue,
									recievedCounter.get(queue) + 1);
						}
					}
					if (output && !text.equals("")) {
						OutputInputHandler.write(text);
					}
					Thread.sleep(sleepTime);
				} catch (JMSException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			running = false;
		}
	}

	public void deleteQueue(String queue) {
		for (Entry<String, ReadingQueueThread> entry : readingQueueThreads
				.entrySet()) {
			if (entry.getKey().equals(queue)) {
				entry.getValue().stopThread();
				try {
					entry.getValue().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				readingQueueThreads.remove(entry.getKey());
				return;
			}
		}
	}

	public void addQueue(String queue) {
		ReadingQueueThread thread = new ReadingQueueThread(queue, momHandler);
		thread.start();
		readingQueueThreads.put(ownName, thread);
	}

	public int getSendCounter() {
		int counter = -1;
		synchronized (sendCounter) {
			counter = sendCounter;
		}
		return counter;
	}

	public int getRecievedCounter(String queue) {
		int counter = -1;
		synchronized (recievedCounter) {
			counter = recievedCounter.get(queue);
		}
		return counter;
	}
}
