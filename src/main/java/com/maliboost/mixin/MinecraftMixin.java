package com.maliboost.mixin;

import com.maliboost.FrameStats;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	// No captured args so a changed runTick signature still matches; require=0 = skip if missing
	@Inject(method = "runTick", at = @At("HEAD"), require = 0)
	private void maliboost$onFrame(CallbackInfo ci) {
		FrameStats.onFrame();
	}
}
