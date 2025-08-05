package moe.nea.firmament.util

import com.google.auto.service.AutoService
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.compatloader.CompatLoader

interface HoveredItemStackProvider : Comparable<HoveredItemStackProvider> {
	fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack?
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

	override fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack? {
		screen as AccessorHandledScreen
		val vanillaSlot = screen.focusedSlot_Firmament?.stack
		return vanillaSlot
	}

	override val prio: Int
		get() = -1
}

val HandledScreen<*>.focusedItemStack: ItemStack?
	get() =
		HoveredItemStackProvider.sorted
			.firstNotNullOfOrNull { it.provideHoveredItemStack(this)?.takeIf { !it.isEmpty } }
