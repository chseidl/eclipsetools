package de.christophseidl.util.java;

import java.util.Random;

public class Random2 extends Random {
	private static final long serialVersionUID = 1L;

	public Random2() {
		super();
	}

	public Random2(long seed) {
		super(seed);
	}

	public boolean percentualChance(double percentValid) {
		double actualPercent = nextDouble();
		
		return (percentValid > actualPercent);
	}
	
	public int nextIntBetween(int average, int variance) {
		return nextIntBetween(average, variance, 1.0);
	}

	public int nextIntBetween(int average, int variance, double varianceProbability) {
		if (percentualChance(varianceProbability)) {
			int lower = Math.max(0, average - variance);
			int effectiveVariance = Math.max(0,  2 * variance);
			
			return lower + nextInt(effectiveVariance + 1);
		} else {
			return average;
		}
	}
}
