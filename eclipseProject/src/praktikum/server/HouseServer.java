package praktikum.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * HouseServer has TCP Server listening on port 9999 for HTTP requests and UDP
 * Server listening on port 9998 for custom Sensor packets.
 * 
 * @author moritz
 *
 */

public class HouseServer {

	private static List<Room> rooms = new ArrayList<Room>();
	// ports for XmlRpcServers.
	// TODO: Multiple Server ueber versch. Ports
	private List<Integer> port;

	public List<Integer> getPort() {
		return this.port;
	}

	public HouseServer(int startPort, int endPort) {
		port = new ArrayList<Integer>();
		for (int i = startPort; i < endPort; i++) {
			port.add(i);
		}
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
		int startport = 8000;
		int endport = 8001;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-sp")) {
				startport = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-ep")) {
				endport = Integer.parseInt(args[i + 1]);
			}
		}

		HouseServer houseServer = new HouseServer(startport, endport);
		for (int i = 0; i < houseServer.getPort().size(); i++) {
			try {
				WebServer webServer = new WebServer(houseServer.getPort()
						.get(i));

				XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
				PropertyHandlerMapping phm = new PropertyHandlerMapping();

				// TODO: Multiple Server auf einem Rechner ueber versch. Namen
				phm.addHandler("MyXmlRpcServer", MyXmlRpcServer.class);
				// Integer zahl = 1;
				// String server = "MyXmlRpcServer_" + zahl.toString();
				// phm.addHandler(server, MyXmlRpcServer.class);
				xmlRpcServer.setHandlerMapping(phm);

				XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer
						.getConfig();
				serverConfig.setEnabledForExtensions(true);
				serverConfig.setContentLengthOptional(true);

				webServer.start();

				System.out
						.println("The MyXmlRpc Server has been started on port "
								+ houseServer.getPort().get(i) + " ...");

			} catch (Exception exception) {
				System.err.println("JavaServer: " + exception);
			}
		}
	}
}
