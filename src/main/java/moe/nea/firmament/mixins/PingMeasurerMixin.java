package moe.nea.firmament.mixins;

import moe.nea.firmament.util.NetworkUtils;
import net.minecraft.client.network.PingMeasurer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PingMeasurer.class)
public class PingMeasurerMixin {
	@ModifyArg(method = "onPingResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;push(J)V"))
	private long onPingResult(long ping){
		if(NetworkUtils.INSTANCE.isPinging()) {
			NetworkUtils.INSTANCE.setPing(ping);
		}
		return ping;
	}
}
