package praktikum;

import java.net.*;

/**
 *
 * @author Peter Altenberd
 * (Translated into English by Ronald Moore)
 * Computer Science Dept.                   Fachbereich Informatik
 * Darmstadt Univ. of Applied Sciences      Hochschule Darmstadt
 */

public class MultithreadedTCPServer extends Thread{
	HouseServer houseServer;
	
	public MultithreadedTCPServer(HouseServer home) {
		this.houseServer = home;
		this.start();
	}
	@Override
    public void run(){
        int port = 9999;
        try {
        ServerSocket listenSocket = new ServerSocket(port);
        System.out.println("Multithreaded Server starts on Port "+port);
        while (true){
            Socket client = listenSocket.accept();
            System.out.println("Connection with: " +     // Output connection
                    client.getRemoteSocketAddress());   // (Client) address
            houseServer.setRandomNo();
            new EchoService(client, houseServer).start();
        }
        } catch (Exception e) {
        	e.toString();
        }
    }
}
