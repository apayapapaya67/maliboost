package com.maliboost;

import java.util.Arrays;

/** Fixed-size ring of frame intervals; render thread only, zero allocation. */
public final class FrameStats {
	private static final int CAP = 300; // ~5 s at 60 FPS
	private static final float[] RING = new float[CAP];
	private static final float[] SCRATCH = new float[CAP];
	private static int head;
	private static int count;
	private static long last;

	private FrameStats() {}

	public static void onFrame() {
		long now = System.nanoTime();
		if (last != 0) record((now - last) / 1_000_000f);
		last = now;
	}

	static void record(float ms) {
		RING[head] = ms;
		head = (head + 1) % CAP;
		if (count < CAP) count++;
	}

	public static void reset() {
		head = 0;
		count = 0;
		last = 0; // so the gap before the next frame is not counted as a spike
	}

	/** out[0] = average ms, out[1] = 99th percentile ms (the "1% low"). False if too few samples. */
	public static boolean compute(float[] out) {
		int n = count;
		if (n < 20) return false;
		float sum = 0f;
		for (int i = 0; i < n; i++) {
			SCRATCH[i] = RING[i];
			sum += RING[i];
		}
		Arrays.sort(SCRATCH, 0, n);
		out[0] = sum / n;
		out[1] = SCRATCH[Math.min(n - 1, (int) (n * 0.99f))];
		return true;
	}
}
