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

	public MomController(List<List<Room>> rooms, String ownName) {
		this.readingQueueThreads = new HashMap<String, MomController.ReadingQueueThread>();

		this.ownName = ownName;

		this.rooms = rooms;

		try {
			this.momHandler = new MomHandler();
		} catch (JMSException e) {
			e.printStackTrace();
		}
//		ReadingQueueThread thread = new ReadingQueueThread("critical",
//				momHandler);
//		thread.start();
//		readingQueueThreads.put("critical", thread);

//		thread = new ReadingQueueThread(ownName, momHandler);
//		thread.start();
//		readingQueueThreads.put(ownName, thread);
		
//		thread = new ReadingQueueThread("house0", momHandler);
//		thread.start();
//		readingQueueThreads.put(ownName, thread);

	}

	@Override
	public void run() {
		super.run();
		while (true) {
			sendValues();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendValues() {
		// iterate over lists
//		boolean isCritical = false;
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
//				isCritical = true;
			}

			builder.append("HighestTemp of House No. " + i + " :\t"
					+ highestTemp + "\n");
			builder.append("LowestTemp of House No. " + i + " :\t" + lowestTemp
					+ "\n");
			builder.append("TotalPower of House No. " + i + " :\t" + totalPower
					+ "\n");
		}
		try {
//			if (isCritical) {
//				momHandler.sendToQueue("critical", builder.toString());
//			}
			momHandler.sendToQueue(ownName, builder.toString());
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
					OutputInputHandler.write(text);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void stopThread() {
			running = false;
		}
	}
	
	public void deleteQueue(String queue) {
		for (Entry<String, ReadingQueueThread> entry : readingQueueThreads.entrySet()) {
			if (entry.getKey().equals(queue)) {
				entry.getValue().stopThread();
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
}
