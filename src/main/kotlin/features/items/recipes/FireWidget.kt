package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.GuiGraphics

class FireWidget(val point: Point, val animationTicks: Int) : RecipeWidget() {
	override val rect: Rectangle
		get() = Rectangle(point, Dimension(10, 10))

	override fun render(
		guiGraphics: GuiGraphics,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
		TODO("Not yet implemented")
	}
}
