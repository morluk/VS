package praktikum.adminServer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import praktikum.server.Room;

/**
 * Adjust CLASSPATH with: export
 * CLASSPATH=<PATH-TO-LIBS>/xmlrpc-3.1/lib/\*:$CLASSPATH
 * 
 * @author moritz
 * 
 */

public class MyXmlRpcClient {
	private List<Room> rooms;

	private String ip;

	private String serverName;

	MyXmlRpcClient(String ip, String serverName, List<Room> rooms) {
		this.rooms = rooms;
		this.ip = new String(ip);
		this.serverName = new String(serverName);
	}

	/**
	 * call Remote Procedure and fill List<Room>
	 */
	public void run() {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(ip));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// Param ohne Gebrauch
			Object[] param = new Object[] { new Integer(1) };

			Object[] realList;
			realList = (Object[]) client
					.execute(serverName + ".getList", param);
			synchronized (rooms) {
				rooms.clear();
			}

			for (int i = 0; i < realList.length; i += 2) {
				// System.out.println("Result: " + (Integer) realList[i]);
				synchronized (rooms) {
					rooms.add(new Room((Integer) realList[i],
							(Integer) realList[i + 1]));
				}

			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
}
