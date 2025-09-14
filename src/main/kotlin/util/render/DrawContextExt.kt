package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import org.joml.Vector3f
import util.render.CustomRenderLayers
import kotlin.math.abs
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return matrices.m00 == 1F && matrices.m11 == 1f && matrices.m01 == 0F && matrices.m10 == 0F && matrices.m20 == 0F && matrices.m21 == 0F
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

data class LineRenderState(
	override val x1: Int,
	override val x2: Int,
	override val y1: Int,
	override val y2: Int,
	override val scale: Float,
	override val bounds: ScreenRect,
	val lineWidth: Float,
	val w: Int,
	val h: Int,
	val color: Int,
	val direction: LineDirection,
) : MultiSpecialGuiRenderState() {
	enum class LineDirection {
		TOP_LEFT_TO_BOTTOM_RIGHT,
		BOTTOM_LEFT_TO_TOP_RIGHT,
	}

	override fun createRenderer(vertexConsumers: VertexConsumerProvider.Immediate): MultiSpecialGuiRenderer<out MultiSpecialGuiRenderState> {
		return LineRenderer(vertexConsumers)
	}

	override val scissorArea = null
}

class LineRenderer(vertexConsumers: VertexConsumerProvider.Immediate) :
	MultiSpecialGuiRenderer<LineRenderState>(vertexConsumers) {
	override fun getElementClass(): Class<LineRenderState> {
		return LineRenderState::class.java
	}

	override fun getYOffset(height: Int, windowScaleFactor: Int): Float {
		return height / 2F
	}

	override fun render(
		state: LineRenderState,
		matrices: MatrixStack
	) {
		val gr = MC.instance.gameRenderer
		val client = MC.instance
		gr.globalSettings
			.set(
				state.bounds.width,
				state.bounds.height,
				client.options.glintStrength.getValue(),
				client.world?.time ?: 0L,
				client.renderTickCounter,
				client.options.menuBackgroundBlurrinessValue
			)

		RenderSystem.lineWidth(state.lineWidth)
		val buf = vertexConsumers.getBuffer(CustomRenderLayers.LINES)
		val matrix = matrices.peek()
		val wh = state.w / 2F
		val hh = state.h / 2F
		val lowX = -wh
		val lowY = if (state.direction == LineRenderState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT) hh else -hh
		val highX = wh
		val highY = -lowY
		val norm = Vector3f(highX - lowX, highY - lowY, 0F).normalize()
		buf.vertex(matrix, lowX, lowY, 0F).color(state.color)
			.normal(matrix, norm)
		buf.vertex(matrix, highX, highY, 0F).color(state.color)
			.normal(matrix, norm)
		vertexConsumers.draw()
		gr.globalSettings
			.set(
				client.window.framebufferWidth,
				client.window.framebufferHeight,
				client.options.glintStrength.getValue(),
				client.world?.getTime() ?: 0L,
				client.renderTickCounter,
				client.options.menuBackgroundBlurrinessValue
			)

	}

	override fun getName(): String? {
		return "Firmament Line Renderer"
	}
}


fun DrawContext.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color, lineWidth: Float = 1F) {
	if (toY < fromY) {
		drawLine(toX, toY, fromX, fromY, color)
		return
	}
	val originalRect = ScreenRect(
		minOf(fromX, toX), minOf(toY, fromY),
		abs(toX - fromX), abs(toY - fromY)
	).transform(matrices)
	val expansionFactor = 3
	val rect = ScreenRect(
		originalRect.left - expansionFactor,
		originalRect.top - expansionFactor,
		originalRect.width + expansionFactor * 2,
		originalRect.height + expansionFactor * 2
	)
	// TODO: expand the bounds so that the thickness of the line can be used
	// TODO: fix this up to work with scissorarea
	state.addSpecialElement(
		LineRenderState(
			rect.left, rect.right, rect.top, rect.bottom, 1F, rect, lineWidth,
			originalRect.width, originalRect.height, color.color,
			if (fromX < toX) LineRenderState.LineDirection.TOP_LEFT_TO_BOTTOM_RIGHT else LineRenderState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT
		)
	)
}

