package praktikum.sensors;

import java.util.Random;

public class RandValue {			//Singleton
	private static RandValue instance = null;
	Random generator;
	static boolean instanciated = false;

	protected RandValue() {
		generator = new Random();
	};

	public static RandValue getInstance() {
		if (null == instance) {
			instance = new RandValue();
			instanciated = true;
		}
		return instance;
	}

	public int getRandomTemp(int month) {
		int result = -1;
		if (instanciated) {
			double decission = generator.nextDouble();
			if (decission <= 0.8) {
				// regular temperature more likely
				result = generator.nextInt(21) + 10; // Range 10 <-> 30
			} else {
				// winter low temperature, summer high temperature (extrem high
				// not possible yet)
				double factor = (double) (2.0 * 3.1415926535897932 * (month / 11.0)); // MONTH
																						// in
																						// range
																						// 0
																						// to
																						// 2*PI
				double m = -1.0 * Math.cos(factor * 3.1415926535897932 / 180); // (-cos(MONTH)
																				// winter
																				// minus
																				// possible)
				result = (int) (generator.nextInt(11) * m); // Range 0 <-> 10 * m
			}
		}
		return result;
	}

	public int getRandomPower() {
		int result = -1;
		if (instanciated) {
			double decission = generator.nextDouble();
			if (decission <= 0.8) {
				result = generator.nextInt(3); // Range 0 <-> 2
			} else {
				result = generator.nextInt(8) + 3; // Range 3 <-> 10
			}
		}
		return result;
	}

}
