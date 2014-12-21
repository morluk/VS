package praktikum.adminServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import praktikum.server.Room;

public class MqController {
	private MqPublisher statusPublisher, alertPublisher;
	private List<MqListener> statusListener, alertListener;
	private String adminServerName;
	private InetAddress myIP;

	public MqController(int nrOfListeners, String adminServerName, String mqIp,
			String mqPort) {
		try {
			statusPublisher = new MqPublisher(mqIp, mqPort, "status_"
					+ adminServerName);
			alertPublisher = new MqPublisher(mqIp, mqPort, "alert_"
					+ adminServerName);
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
		// Namedroping MqListener
		// Names have to be Numbers from 1 to n
		// for i<nrOfListeners; if i.contains(name) skip
		statusListener = new ArrayList<MqListener>(nrOfListeners);
		alertListener = new ArrayList<MqListener>(nrOfListeners);
		for (int i = 1; i <= nrOfListeners + 1; i++) {
			String iStr = new Integer(i).toString();
			if (!adminServerName.contains(iStr)) {
				try {
					MqListener newStatusListener = new MqListener(mqIp, mqPort,
							"status_" + iStr);
					statusListener.add(newStatusListener);
					MqListener newAlertListener = new MqListener(mqIp, mqPort,
							"alert_" + iStr);
					alertListener.add(newAlertListener);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		// save own IP
		myIP = null;
		try {
			myIP = InetAddress.getLocalHost();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Publish Status Messages into statusPublisher
	 * 
	 * @param rooms
	 * @throws JMSException
	 */
	public void publishStatus(List<List<Room>> rooms) throws JMSException {
		statusPublisher.publishMessage("START Status From AdminServer: "
				+ this.adminServerName + ": " + myIP);
		// iterate over lists
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			if (currList.size() == 0)
				break;
			// Send Message to MoM
			statusPublisher.publishMessage("--------------");
			statusPublisher.publishMessage("Flat No. " + i + ": ");
			statusPublisher.publishMessage("--------------");
			int highestTemp = currList.get(0).getTemperature();
			int lowestTemp = currList.get(0).getTemperature();
			int totalPower = 0;
			// iterate over rooms
			for (int j = 0; j < currList.size(); j++) {
				// Send Status Message to MoM always
				statusPublisher.publishMessage("Room " + j + " - Temp:\t"
						+ currList.get(j).getTemperature() + "\t- Power:\t"
						+ currList.get(j).getPower());
				totalPower += currList.get(j).getPower();
				if (highestTemp < currList.get(j).getTemperature())
					highestTemp = currList.get(j).getTemperature();
				if (lowestTemp > currList.get(j).getTemperature())
					lowestTemp = currList.get(j).getTemperature();
			}
			// Send Status Message to MoM always
			statusPublisher.publishMessage("HighestTemp of House No. " + i
					+ " :\t" + highestTemp);
			statusPublisher.publishMessage("LowestTemp of House No. " + i
					+ " :\t" + lowestTemp);
			statusPublisher.publishMessage("TotalPower of House No. " + i
					+ " :\t" + totalPower);
		}
		statusPublisher.publishMessage("STOP Status From AdminServer "
				+ this.adminServerName + ": " + myIP);
		statusPublisher.publishMessage("OVER");
	}

	/**
	 * Publish Alert Messages into alertPublisher
	 * 
	 * @param rooms
	 * @throws JMSException
	 */
	public void publishAlert(List<List<Room>> rooms) throws JMSException {
		alertPublisher.publishMessage("START Alert From AdminServer: "
				+ this.adminServerName + ": " + myIP);
		// iterate over lists
		for (int i = 0; i < rooms.size(); i++) {
			List<Room> currList = rooms.get(i);
			if (currList.size() == 0)
				break;
			// Send Message to MoM
			alertPublisher.publishMessage("--------------");
			alertPublisher.publishMessage("Flat No. " + i + ": ");
			alertPublisher.publishMessage("--------------");
			// iterate over rooms
			for (int j = 0; j < currList.size(); j++) {
				// Mom Output
				if (currList.get(j).getTemperature() < 10
						|| currList.get(j).getTemperature() > 30
						|| currList.get(j).getPower() > 2) {
					// Send Alert Message to MoM
					alertPublisher.publishMessage("Room " + j + " - Temp:\t"
							+ currList.get(j).getTemperature() + "\t- Power:\t"
							+ currList.get(j).getPower());
				}
			}
		}
		alertPublisher.publishMessage("STOP Alert From AdminServer "
				+ this.adminServerName + ": " + myIP);
		alertPublisher.publishMessage("OVER");
	}

	/**
	 * finds MqListener whos destinations matches given name
	 * 
	 * @param name
	 * @param list
	 * @return
	 */
	private MqListener getListener(String name, List<MqListener> list) {
		MqListener result = null;
		for (MqListener listener : list) {
			if (listener.getDestination().contains(name)) {
				result = listener;
				break;
			}
		}
		return result;
	}

	/**
	 * find MqListener in List and change isListening 
	 * from status to alert to none and again to status
	 * for each invocation
	 * 
	 * @param mode
	 */
	public void changeListenerMode(String name) {
		// find Listener where destination contains name
		MqListener currAlertListener = getListener(name, alertListener);
		MqListener currStatusListener = getListener(name, statusListener);
		if (currAlertListener == null || currStatusListener == null)
			return;
		// subscribe/unsubscribe
		try {
			if (!currStatusListener.isConnected() && !currAlertListener.isConnected()) {
				//Listen to status
				currAlertListener.close();
				currStatusListener.reopen();
			}
			else if (currStatusListener.isConnected() && !currAlertListener.isConnected()) {
				//Listen to alert
				currStatusListener.close();
				currAlertListener.reopen();
			}
			else {
				//listen to nothing
				currStatusListener.close();
				currAlertListener.close();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check if MqListener isConnected == true and run
	 */
	public void checkListeners() {
		for (MqListener listener : statusListener) {
			if (listener.isConnected())
				listener.run();
		}
		for (MqListener listener : alertListener) {
			if (listener.isConnected())
				listener.run();
		}
	}
}
