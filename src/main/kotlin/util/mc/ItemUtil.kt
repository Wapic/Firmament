package moe.nea.firmament.util.mc

import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.text.Text
import moe.nea.firmament.util.MC

fun ItemStack.appendLore(args: List<Text>) {
	if (args.isEmpty()) return
	modifyLore {
		val loreList = loreAccordingToNbt.toMutableList()
		for (arg in args) {
			loreList.add(arg)
		}
		loreList
	}
}

fun ItemStack.modifyLore(update: (List<Text>) -> List<Text>) {
	val loreList = loreAccordingToNbt
	loreAccordingToNbt = update(loreList)
}

fun loadItemFromNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup = MC.defaultRegistries): ItemStack? {
	return ItemStack.CODEC.decode(RegistryOps.of(NbtOps.INSTANCE, registries), nbt).result().getOrNull()?.first
}
