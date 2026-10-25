package com.maliboost;

/** Streak counter: -1 = lower quality, +1 = raise quality, 0 = hold. */
public final class Hysteresis {
	static final int BAD_WINDOWS = 2;
	static final int GOOD_WINDOWS = 15;
	private int bad;
	private int good;

	public int update(float avgMs, float p99Ms, float targetMs) {
		if (avgMs > targetMs * 1.2f || p99Ms > targetMs * 3f) {
			good = 0;
			if (++bad >= BAD_WINDOWS) {
				bad = 0;
				return -1;
			}
		} else if (avgMs < targetMs * 1.1f && p99Ms < targetMs * 2f) {
			bad = 0;
			if (++good >= GOOD_WINDOWS) {
				good = 0;
				return 1;
			}
		} else {
			// dead zone between the thresholds: hold, this is what stops flicker
			bad = 0;
			good = 0;
		}
		return 0;
	}

	public void reset() {
		bad = 0;
		good = 0;
	}
}
