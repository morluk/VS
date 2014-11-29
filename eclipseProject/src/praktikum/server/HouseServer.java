package praktikum.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * HouseServer has TCP Server listening on port 9999 for 
 * HTTP requests and UDP Server listening on port 9998 for
 * custom Sensor packets.
 * @author moritz
 *
 */

public class HouseServer {
	private static List<Room> rooms = new ArrayList<Room>();
	//ports for XmlRpcServers. 
	//TODO: Multiple Server ueber versch. Ports
	private static final int port[] = { 8080 };

	public HouseServer() {
		new MultithreadedTCPServer(this);
		new UDPServer(rooms);
	}

	public static int getRoomCount() {
		return rooms.size();
	}

	public static Room getRoom(int pos) {
		return rooms.get(pos);
	}
	
	public static void main(String[] args) {
		new HouseServer();
		for (int i = 0; i < port.length; i++) {
			try {
				WebServer webServer = new WebServer(port[i]);

				XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
				PropertyHandlerMapping phm = new PropertyHandlerMapping();

				//TODO: Multiple Server auf einem Rechner ueber versch. Namen
				phm.addHandler("MyXmlRpcServer", MyXmlRpcServer.class);
				// Integer zahl = 1;
				// String server = "MyXmlRpcServer_" + zahl.toString();
				// phm.addHandler(server, MyXmlRpcServer.class);
				xmlRpcServer.setHandlerMapping(phm);

				 XmlRpcServerConfigImpl serverConfig =
				 (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
				 serverConfig.setEnabledForExtensions(true);
				 serverConfig.setContentLengthOptional(true);

				webServer.start();

				System.out.println("The MyXmlRpc Server has been started on port " + port[i] + " ...");

			} catch (Exception exception) {
				System.err.println("JavaServer: " + exception);
			}
		}
	}
}
