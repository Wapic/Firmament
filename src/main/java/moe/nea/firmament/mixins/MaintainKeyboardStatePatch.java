package moe.nea.firmament.mixins;

import moe.nea.firmament.keybindings.FirmamentKeyboardState;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MaintainKeyboardStatePatch {
	@Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/InactivityFpsLimiter;onInput()V"))
	private void onKeyInput(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		FirmamentKeyboardState.INSTANCE.maintainState(key, scancode, action, modifiers);
	}
}
