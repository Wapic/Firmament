package moe.nea.firmament.util

import com.google.auto.service.AutoService
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.compatloader.CompatLoader

interface HoveredItemStackProvider : Comparable<HoveredItemStackProvider> {
	fun provideHoveredItemStack(screen: AbstractContainerScreen<*>): ItemStack?
	override fun compareTo(other: HoveredItemStackProvider): Int {
		return compareValues(this.prio, other.prio)
	}

	val prio: Int get() = 0

	companion object : CompatLoader<HoveredItemStackProvider>(HoveredItemStackProvider::class) {
		val sorted = HoveredItemStackProvider.allValidInstances.sorted()
	}
}

@AutoService(HoveredItemStackProvider::class)
class VanillaScreenProvider : HoveredItemStackProvider {

	override fun provideHoveredItemStack(screen: AbstractContainerScreen<*>): ItemStack? {
		screen as AccessorHandledScreen
		val vanillaSlot = screen.focusedSlot_Firmament?.item
		return vanillaSlot
	}

	override val prio: Int
		get() = -1
}

val AbstractContainerScreen<*>.focusedItemStack: ItemStack?
	get() =
		HoveredItemStackProvider.sorted
			.firstNotNullOfOrNull { it.provideHoveredItemStack(this)?.takeIf { !it.isEmpty } }
