package com.maliboost;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
	private final Screen parent;

	public ConfigScreen(Screen parent) {
		super(Component.literal("MaliBoost"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		MaliBoostConfig c = MaliBoostConfig.INSTANCE;
		int w = 150;
		int x1 = this.width / 2 - w - 4;
		int x2 = this.width / 2 + 4;
		int y = 10;

		int pw = 100;
		int px = this.width / 2 - 154;
		presetButton("Potato", px, y, pw);
		presetButton("Balanced", px + 104, y, pw);
		presetButton("Max FPS", px + 208, y, pw);
		y += 24;

		addRenderableWidget(Button.builder(label("Governor", c.governor), b -> {
			c.governor = !c.governor;
			b.setMessage(label("Governor", c.governor));
		}).bounds(x1, y, w, 20).build());
		addRenderableWidget(Button.builder(label("Overlay", c.overlay), b -> {
			c.overlay = !c.overlay;
			b.setMessage(label("Overlay", c.overlay));
		}).bounds(x2, y, w, 20).build());
		y += 24;

		addRenderableWidget(Button.builder(label("Adapt particles", c.adaptParticles), b -> {
			c.adaptParticles = !c.adaptParticles;
			b.setMessage(label("Adapt particles", c.adaptParticles));
		}).bounds(x1, y, w, 20).build());
		addRenderableWidget(Button.builder(label("Adapt entity dist", c.adaptEntityDistance), b -> {
			c.adaptEntityDistance = !c.adaptEntityDistance;
			b.setMessage(label("Adapt entity dist", c.adaptEntityDistance));
		}).bounds(x2, y, w, 20).build());
		y += 24;

		addRenderableWidget(Button.builder(label("Adapt sim dist", c.adaptSimulationDistance), b -> {
			c.adaptSimulationDistance = !c.adaptSimulationDistance;
			b.setMessage(label("Adapt sim dist", c.adaptSimulationDistance));
		}).bounds(x1, y, w, 20).build());
		addRenderableWidget(Button.builder(fpsLabel(c.targetFps), b -> {
			c.targetFps = c.targetFps >= 60 ? 30 : c.targetFps + 15;
			b.setMessage(fpsLabel(c.targetFps));
		}).bounds(x2, y, w, 20).build());
		y += 28;

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
				.bounds(this.width / 2 - 100, y, 200, 20).build());
	}

	private void presetButton(String name, int x, int y, int w) {
		String text = name.equals(MaliBoostConfig.INSTANCE.preset) ? "> " + name : name;
		addRenderableWidget(Button.builder(Component.literal(text), b -> {
			MaliBoostConfig.INSTANCE.applyPreset(name);
			MaliBoostConfig.save();
			Governor.markDirty();
			this.minecraft.gui.setScreen(new ConfigScreen(parent));
		}).bounds(x, y, w, 20).build());
	}

	private static Component label(String name, boolean on) {
		return Component.literal(name + ": " + (on ? "ON" : "OFF"));
	}

	private static Component fpsLabel(int fps) {
		return Component.literal("Target FPS: " + fps);
	}

	@Override
	public void onClose() {
		MaliBoostConfig.save();
		Governor.markDirty();
		this.minecraft.gui.setScreen(parent);
	}
}
