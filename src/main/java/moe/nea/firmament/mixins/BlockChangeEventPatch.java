package moe.nea.firmament.mixins;

import moe.nea.firmament.events.BlockChangeEvent;
import moe.nea.firmament.util.MC;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientWorld.class)
public class BlockChangeEventPatch {

	@Inject(method = "handleBlockUpdate", at = @At("HEAD"))
	public void onBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci){
		BlockChangeEvent.Companion.publish(new BlockChangeEvent(pos, state, Objects.requireNonNull(MC.INSTANCE.getWorld()).getBlockState(pos)));
	}
}
