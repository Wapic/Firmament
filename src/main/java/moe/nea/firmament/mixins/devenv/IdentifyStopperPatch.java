
package moe.nea.firmament.mixins.devenv;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class IdentifyStopperPatch {
	@Shadow
	private volatile boolean running;

	@Inject(method = "scheduleStop", at = @At("HEAD"))
	private void onStop(CallbackInfo ci) {
		if (this.running)
			Thread.dumpStack();
	}
}
