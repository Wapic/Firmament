package moe.nea.firmament.util.render

import org.joml.Matrix3x2f
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.SpecialGuiElementRenderer
import net.minecraft.client.gui.render.state.GuiRenderState
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState
import net.minecraft.client.render.VertexConsumerProvider

abstract class MultiSpecialGuiRenderState : SpecialGuiElementRenderState {
	// I wish i had manifolds @Self type here... Maybe i should switch to java after all :(
	abstract fun createRenderer(vertexConsumers: VertexConsumerProvider.Immediate): MultiSpecialGuiRenderer<out MultiSpecialGuiRenderState>
	abstract val x1: Int
	abstract val x2: Int
	abstract val y1: Int
	abstract val y2: Int
	abstract val scale: Float
	abstract val bounds: ScreenRect?
	abstract val scissorArea: ScreenRect?
	override fun x1(): Int = x1

	override fun x2(): Int = x2

	override fun y1(): Int = y1

	override fun y2(): Int = y2

	override fun scale(): Float = scale

	override fun scissorArea(): ScreenRect? = scissorArea

	override fun bounds(): ScreenRect? = bounds

}

abstract class MultiSpecialGuiRenderer<T : MultiSpecialGuiRenderState>(
	vertexConsumers: VertexConsumerProvider.Immediate
) : SpecialGuiElementRenderer<T>(vertexConsumers) {
	var wasUsedThisFrame = false
	fun consumeRender(): Boolean {
		return wasUsedThisFrame.also { wasUsedThisFrame = false }
	}

	override fun renderElement(element: T, state: GuiRenderState) {
		wasUsedThisFrame = true
		super.renderElement(element, state)
	}
}
