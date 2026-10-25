package com.maliboost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MaliBoostConfig {
	public static final Logger LOG = LoggerFactory.getLogger("MaliBoost");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("maliboost.json");

	public static MaliBoostConfig INSTANCE = new MaliBoostConfig();

	public String preset = "Balanced";
	public boolean governor = true;
	public boolean overlay = false;
	public boolean adaptParticles = true;
	public boolean adaptEntityDistance = true;
	public boolean adaptSimulationDistance = true;
	public int targetFps = 60;
	public int minRenderDistance = 5;
	public int maxRenderDistance = 10;
	public int minSimulationDistance = 5;
	public int maxSimulationDistance = 8;
	public double minEntityDistance = 0.75;

	public static void load() {
		if (Files.exists(FILE)) {
			try (Reader r = Files.newBufferedReader(FILE)) {
				MaliBoostConfig c = GSON.fromJson(r, MaliBoostConfig.class);
				if (c != null) INSTANCE = c;
			} catch (Exception e) {
				LOG.warn("[MaliBoost] maliboost.json unreadable, using defaults", e);
			}
		}
		INSTANCE.clamp();
		save();
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(INSTANCE));
		} catch (Exception e) {
			LOG.warn("[MaliBoost] could not save maliboost.json", e);
		}
	}

	// JSON is hand-editable, so keep every value inside ranges vanilla options accept
	void clamp() {
		targetFps = clampI(targetFps, 20, 120);
		minRenderDistance = clampI(minRenderDistance, 2, 32);
		maxRenderDistance = clampI(maxRenderDistance, minRenderDistance, 32);
		minSimulationDistance = clampI(minSimulationDistance, 5, 32);
		maxSimulationDistance = clampI(maxSimulationDistance, minSimulationDistance, 32);
		minEntityDistance = Math.max(0.5, Math.min(1.0, minEntityDistance));
		if (preset == null) preset = "Custom";
	}

	public void applyPreset(String name) {
		switch (name) {
			case "Potato" -> set(2, 5, 5, 5, 0.5);
			case "Max FPS" -> set(3, 8, 5, 6, 0.5);
			default -> {
				name = "Balanced";
				set(5, 10, 5, 8, 0.75);
			}
		}
		preset = name;
		governor = true;
		adaptParticles = true;
		adaptEntityDistance = true;
		adaptSimulationDistance = true;
	}

	private void set(int minRd, int maxRd, int minSim, int maxSim, double minEnt) {
		minRenderDistance = minRd;
		maxRenderDistance = maxRd;
		minSimulationDistance = minSim;
		maxSimulationDistance = maxSim;
		minEntityDistance = minEnt;
	}

	private static int clampI(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
