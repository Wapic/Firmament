package moe.nea.firmament.repo.recipes

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.text.Text
import moe.nea.firmament.repo.SBItemStack

interface RecipeLayouter {
	enum class SlotKind {
		SMALL_INPUT,
		SMALL_OUTPUT,

		/**
		 * Create a bigger background and mark the slot as output. The coordinates should still refer the upper left corner of the item stack, not of the bigger background.
		 */
		BIG_OUTPUT,
	}

	fun createItemSlot(
		x: Int, y: Int,
		content: SBItemStack?,
		slotKind: SlotKind,
	)

	fun createTooltip(rectangle: Rectangle, label: Text)

	fun createLabel(
		x: Int, y: Int,
		text: Text
	)

	fun createArrow(x: Int, y: Int): Rectangle

	fun createMoulConfig(x: Int, y: Int, w: Int, h: Int, component: GuiComponent)
	fun createFire(ingredientsCenter: Point, animationTicks: Int)
}

