package com.maliboost;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicTest {
	@Test
	void percentileAndAverage() {
		FrameStats.reset();
		float[] out = new float[2];
		assertFalse(FrameStats.compute(out));
		for (int i = 0; i < 99; i++) FrameStats.record(16f);
		FrameStats.record(100f);
		assertTrue(FrameStats.compute(out));
		assertEquals(16.84f, out[0], 0.01f);
		assertEquals(100f, out[1], 0.001f);
	}

	@Test
	void hysteresisNeedsStreaks() {
		Hysteresis h = new Hysteresis();
		float t = 1000f / 60f;
		assertEquals(0, h.update(60f, 60f, t));
		assertEquals(-1, h.update(60f, 60f, t));

		for (int i = 1; i < Hysteresis.GOOD_WINDOWS; i++) assertEquals(0, h.update(16.7f, 20f, t));
		assertEquals(1, h.update(16.7f, 20f, t));

		// a dead-zone window breaks the good streak
		for (int i = 1; i < Hysteresis.GOOD_WINDOWS; i++) h.update(16.7f, 20f, t);
		assertEquals(0, h.update(19f, 20f, t));
		assertEquals(0, h.update(16.7f, 20f, t));
	}
}
