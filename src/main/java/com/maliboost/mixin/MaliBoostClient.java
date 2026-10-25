package com.maliboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MaliBoostClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MaliBoostConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(Governor::onEndTick);
	}
}
