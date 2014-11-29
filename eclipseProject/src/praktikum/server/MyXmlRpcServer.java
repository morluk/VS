package praktikum.server;

import java.util.ArrayList;
import java.util.List;

public class MyXmlRpcServer {
	public List<Integer> getList(int no) {
//		int offset = 0;
//		if (no == 1) {
//			offset = 10;
//		}
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < HouseServer.getRoomCount(); i++) {
			Room currRoom = HouseServer.getRoom(i);
			result.add(new Integer(currRoom.getTemperature()));
			result.add(new Integer(currRoom.getPower()));
		}
		return result;
	}
}
