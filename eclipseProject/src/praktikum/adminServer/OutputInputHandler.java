package praktikum.adminServer;

import java.util.Scanner;

public class OutputInputHandler {
	private static Scanner scanner;
	
//	public static synchronized void start() {
//		scanner = new Scanner(System.in);
//		System.out.print("Eingabe: ");
//	}
	
	public static void write(String text) {
//		System.out.print("\r");
//		System.out.print("\033[2K");
		System.out.println(text);
//		System.out.print("Eingabe: ");
	}
	
	public static String read() {
		String text = scanner.nextLine();
		System.out.print("\033[1A");
		System.out.print("\033[2K");
		System.out.print("Eingabe: ");
		return text;
	}
	
	public static synchronized void stop() {
		scanner.close();
	}
}
