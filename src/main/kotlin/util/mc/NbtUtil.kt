package moe.nea.firmament.util.mc

import net.minecraft.component.type.NbtComponent
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import moe.nea.firmament.mixins.accessor.AccessorNbtComponent

fun Iterable<NbtElement>.toNbtList() = NbtList().also {
	for (element in this) {
		it.add(element)
	}
}

@Suppress("CAST_NEVER_SUCCEEDS")
val NbtComponent.unsafeNbt get() = (this as AccessorNbtComponent).unsafeNbt_firmament
