package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import util.render.CustomRenderLayers
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return matrices.m00 == 1F && matrices.m11 == 1f && matrices.m01 == 0F && matrices.m10 == 0F && matrices.m20 == 0f && matrices.m21 == 0F
}

@Deprecated("Use the other drawGuiTexture")
fun DrawContext.drawGuiTexture(
	x: Int, y: Int, z: Int, width: Int, height: Int, sprite: Identifier
) = this.drawGuiTexture(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun DrawContext.drawGuiTexture(
	sprite: Identifier,
	x: Int, y: Int, width: Int, height: Int
) = this.drawGuiTexture(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun DrawContext.drawTexture(
	sprite: Identifier,
	x: Int,
	y: Int,
	u: Float,
	v: Float,
	width: Int,
	height: Int,
	textureWidth: Int,
	textureHeight: Int
) {
	this.drawTexture(
		RenderPipelines.GUI_TEXTURED,
		sprite,
		x,
		y,
		u,
		v,
		width,
		height,
		width,
		height,
		textureWidth,
		textureHeight
	)
}

fun DrawContext.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color) {
	// TODO: push scissors
	// TODO: use matrix translations and a different render layer
	if (toY < fromY) {
		drawLine(toX, toY, fromX, fromY, color)
		return
	}
	val rect = ScreenRect(fromX, fromY, toX - fromX, toY - fromY).transform(matrices)
	RenderSystem.lineWidth(MC.window.scaleFactor.toFloat())
	// TODO: this also requires adding a List<SpecialGuiElementRenderer<?>> entry in guirenderer
	// TODO: 	state.addSpecialElement(object : SpecialGuiElementRenderState {
	//		override fun x1(): Int {
	//			return fromX
	//		}
	//
	//		override fun x2(): Int {
	//			return toY
	//		}
	//
	//		override fun y1(): Int {
	//			return fromY
	//		}
	//
	//		override fun y2(): Int {
	//			return toY
	//		}
	//
	//		override fun scale(): Float {
	//			return 1f
	//		}
	//
	//		override fun scissorArea(): ScreenRect? {
	//			return rect
	//		}
	//
	//		override fun bounds(): ScreenRect? {
	//			return rect
	//		}
	//
	//	})
//	draw { vertexConsumers ->
//		val buf = vertexConsumers.getBuffer(CustomRenderLayers.LINES)
//		val matrix = this.matrices.peek()
//		buf.vertex(matrix, fromX.toFloat(), fromY.toFloat(), 0F).color(color.color)
//			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
//		buf.vertex(matrix, toX.toFloat(), toY.toFloat(), 0F).color(color.color)
//			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
//	}
}

