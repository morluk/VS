package praktikum.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Peter Altenberd (Translated into English by Ronald Moore) Computer
 *         Science Dept. Fachbereich Informatik Darmstadt Univ. of Applied
 *         Sciences Hochschule Darmstadt
 */

public class MultithreadedTCPServer extends Thread {
	HouseServer houseServer;
	int PORT;

	public MultithreadedTCPServer(HouseServer home, int port) {
		setDaemon(true);
		this.houseServer = home;
		this.PORT = port;
		this.start();
	}

	@Override
	public void run() {
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(PORT);
			System.out.println("Multithreaded Server starts on Port " + PORT);
			while (true) {
				Socket client = listenSocket.accept();
				System.out.println("Connection with: " + // Output connection
						client.getRemoteSocketAddress()); // (Client) address
				new EchoService(client, houseServer).start();
			}
		} catch (Exception e) {
			e.toString();
		} finally {
			try {
				listenSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
