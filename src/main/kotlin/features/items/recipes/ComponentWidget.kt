package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import moe.nea.firmament.repo.recipes.RecipeLayouter
import moe.nea.firmament.util.MC

class ComponentWidget(val point: Point, var text: Component) : RecipeWidget(), RecipeLayouter.Updater<Component> {
	override fun update(newValue: Component) {
		this.text = newValue
	}

	override val rect: Rectangle
		get() = Rectangle(point, Dimension(MC.font.width(text), MC.font.lineHeight))

	override fun render(
		guiGraphics: GuiGraphics,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
		guiGraphics.drawString(MC.font, text, point.x, point.y, -1)
	}
}
