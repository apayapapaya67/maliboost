package com.maliboost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class Governor {
	private static final int WARMUP_WINDOWS = 5; // seconds ignored after join / screen / own change
	private static final float[] STATS = new float[2];
	private static final Hysteresis HYST = new Hysteresis();

	private static int ticks;
	private static int warmup = WARMUP_WINDOWS;
	private static int noData;
	private static boolean clamped;
	private static boolean governorBroken;
	private static boolean overlayBroken;

	private Governor() {}

	/** Re-apply min/max limits on the next evaluation (after config changes). */
	public static void markDirty() {
		clamped = false;
	}

	public static void onEndTick(Minecraft mc) {
		if (++ticks < 20) return; // evaluate once per second
		ticks = 0;

		if (mc.level == null || mc.gui.screen() != null) {
			if (mc.level == null) clamped = false;
			HYST.reset();
			warmup = WARMUP_WINDOWS;
			return;
		}

		MaliBoostConfig c = MaliBoostConfig.INSTANCE;
		boolean hasData = FrameStats.compute(STATS);
		if (hasData) {
			noData = 0;
			if (c.overlay && !overlayBroken) {
				try {
					showOverlay(mc);
				} catch (RuntimeException | LinkageError e) {
					overlayBroken = true;
					MaliBoostConfig.LOG.warn("[MaliBoost] overlay disabled after error", e);
				}
			}
		} else if (++noData == 15) {
			MaliBoostConfig.LOG.warn("[MaliBoost] no frame samples, Minecraft#runTick hook probably not applied");
		}

		if (warmup > 0) {
			if (--warmup == 0) FrameStats.reset(); // drop loading / reload spikes
			return;
		}
		if (!hasData || !c.governor || governorBroken) return;

		try {
			govern(mc.options, c);
		} catch (RuntimeException | LinkageError e) {
			governorBroken = true;
			MaliBoostConfig.LOG.warn("[MaliBoost] governor disabled after error", e);
		}
	}

	private static void govern(Options o, MaliBoostConfig c) {
		boolean changed;
		if (!clamped) {
			clamped = true;
			changed = clampAll(o, c);
		} else {
			int d = HYST.update(STATS[0], STATS[1], 1000f / c.targetFps);
			changed = d < 0 ? downgrade(o, c) : d > 0 && upgrade(o, c);
			if (changed) {
				MaliBoostConfig.LOG.info("[MaliBoost] {} -> RD {} SIM {} particles {} entity {}",
						d < 0 ? "down" : "up", o.renderDistance().get(), o.simulationDistance().get(),
						particleName(o), o.entityDistanceScaling().get());
			}
		}
		if (changed) {
			// the change itself triggers chunk rebuilds; don't react to our own spike
			HYST.reset();
			warmup = WARMUP_WINDOWS;
		}
	}

	private static boolean clampAll(Options o, MaliBoostConfig c) {
		boolean ch = stepInt(o.renderDistance(), 0, c.minRenderDistance, c.maxRenderDistance);
		if (c.adaptSimulationDistance) ch |= stepInt(o.simulationDistance(), 0, c.minSimulationDistance, c.maxSimulationDistance);
		if (c.adaptEntityDistance) ch |= stepEntity(o, 0, c.minEntityDistance);
		return ch;
	}

	// Cheapest-to-lose first; render distance last because it is the most visible
	private static boolean downgrade(Options o, MaliBoostConfig c) {
		if (c.adaptParticles && stepParticles(o, 1)) return true;
		if (c.adaptEntityDistance && stepEntity(o, -0.25, c.minEntityDistance)) return true;
		if (c.adaptSimulationDistance && stepInt(o.simulationDistance(), -1, c.minSimulationDistance, c.maxSimulationDistance)) return true;
		return stepInt(o.renderDistance(), -1, c.minRenderDistance, c.maxRenderDistance);
	}

	private static boolean upgrade(Options o, MaliBoostConfig c) {
		if (stepInt(o.renderDistance(), 1, c.minRenderDistance, c.maxRenderDistance)) return true;
		if (c.adaptSimulationDistance && stepInt(o.simulationDistance(), 1, c.minSimulationDistance, c.maxSimulationDistance)) return true;
		if (c.adaptEntityDistance && stepEntity(o, 0.25, c.minEntityDistance)) return true;
		return c.adaptParticles && stepParticles(o, -1);
	}

	private static boolean stepInt(OptionInstance<Integer> opt, int delta, int min, int max) {
		int cur = opt.get();
		int next = Math.max(min, Math.min(max, cur + delta));
		if (next == cur) return false;
		opt.set(next);
		return true;
	}

	// Vanilla entity distance slider moves in 0.25 steps; cap at 1.0 (100%)
	private static boolean stepEntity(Options o, double delta, double min) {
		OptionInstance<Double> opt = o.entityDistanceScaling();
		double cur = opt.get();
		double next = Math.max(min, Math.min(1.0, cur + delta));
		if (Math.abs(next - cur) < 1e-3) return false;
		opt.set(next);
		return true;
	}

	// Raw enum access avoids importing ParticleStatus, whose package has moved between versions.
	// Assumes the vanilla order ALL, DECREASED, MINIMAL.
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean stepParticles(Options o, int delta) {
		OptionInstance opt = o.particles();
		Enum<?> cur = (Enum<?>) opt.get();
		Object[] all = cur.getDeclaringClass().getEnumConstants();
		int next = cur.ordinal() + delta;
		if (next < 0 || next >= all.length) return false;
		opt.set(all[next]);
		return true;
	}

	private static String particleName(Options o) {
		return ((Enum<?>) o.particles().get()).name();
	}

	// shortcut: uses the action bar and allocates one String per second; move to a HUD element if it clashes with server messages
	private static void showOverlay(Minecraft mc) {
		Options o = mc.options;
		String s = String.format(Locale.ROOT, "%.0f FPS | %.1f ms | 1%%: %.0f FPS | RD %d SIM %d | %s | E %.2f%s",
				1000f / STATS[0], STATS[0], 1000f / STATS[1],
				o.renderDistance().get(), o.simulationDistance().get(), particleName(o),
				o.entityDistanceScaling().get(), governorBroken ? " | GOV ERR" : "");
		mc.gui.hud.setOverlayMessage(Component.literal(s), false);
	}
}
