package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.LivingEntity
import moe.nea.firmament.gui.entity.EntityRenderer

class EntityWidget(override val rect: Rectangle, val entity: LivingEntity) : RecipeWidget() {
	override fun render(
		guiGraphics: GuiGraphics,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
		EntityRenderer.renderEntity(
			entity, guiGraphics,
			rect.x, rect.y,
			rect.width.toDouble(), rect.height.toDouble(),
			mouseX.toDouble(), mouseY.toDouble()
		)
	}
}
