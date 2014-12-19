package praktikum.adminServer;

public class MenuController extends Thread {

	private MomController momController;

	public void handleInput(String input) {
		StringBuilder builder = new StringBuilder();

		if (input.equals("show")) {
			// TODO:
		} else if (input.equals("subscribe")) {
			String queue = OutputInputHandler.read();
			momController.addQueue(queue);
		} else if (input.equals("dissubscribe")) {
			String queue = OutputInputHandler.read();
			momController.deleteQueue(queue);
		} else {
			builder.append("HELP MENU\n\n");
			builder.append("help: print this\n");
			builder.append("subscribe: subscribe a houseServer\n");
			builder.append("dissubscribe dissubscribe a houseServer\n");
			builder.append("show: show houseServer\n");
		}

		OutputInputHandler.write(builder.toString());
	}

	@Override
	public void run() {
		super.run();
		while (true) {
			handleInput(OutputInputHandler.read());
		}
	}

	public void setMomController(MomController momController) {
		this.momController = momController;
	}
}
