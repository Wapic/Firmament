package moe.nea.firmament.mixins.accessor;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface AccessorScreenHandler {
	@Accessor("type")
	ScreenHandlerType<?> getType_firmament();
}
