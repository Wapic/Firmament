package moe.nea.firmament.mixins.accessor;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtComponent.class)
public interface AccessorNbtComponent {
	@Accessor("nbt")
	NbtCompound getUnsafeNbt_firmament();
}
