package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import me.shedaniel.math.Point
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class MoulConfigFragment(
	context: GuiContext,
	val position: Point,
	val dismiss: () -> Unit
) : MoulConfigScreenComponent(Text.empty(), context, null) {
	init {
		this.init(MC.instance, MC.screen!!.width, MC.screen!!.height)
	}

	override fun createContext(drawContext: DrawContext?): GuiImmediateContext {
		val oldContext = super.createContext(drawContext)
		return oldContext.translated(
			position.x,
			position.y,
			guiContext.root.width,
			guiContext.root.height,
		)
	}


	override fun render(drawContext: DrawContext, i: Int, j: Int, f: Float) {
		val ctx = createContext(drawContext)
		val m = drawContext.matrices
		m.pushMatrix()
		m.translate(position.x.toFloat(), position.y.toFloat())
		guiContext.root.render(ctx)
		m.popMatrix()
		ctx.renderContext.renderExtraLayers()
	}

	override fun close() {
		dismiss()
	}
}
