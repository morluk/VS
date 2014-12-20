package praktikum.adminServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Input implements Runnable {

	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

	AdminServer caller;

	public Input(AdminServer caller) {
		this.caller = caller;
	}

	public String input() throws IOException {
		return stdIn.readLine();
	}

	public void output(String out) {
		System.out.print(out);
	}

	@Override
	public void finalize() throws IOException {
		stdIn.close();
	}

	@Override
	public void run() {
		while (true) {
			try {
				String userInput = this.input();
				caller.setProgStatus(userInput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
