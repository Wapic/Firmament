package moe.nea.firmament.util.render

import com.mojang.blaze3d.vertex.VertexFormat
import org.joml.Matrix3x2f
import util.render.CustomRenderLayers
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.collections.nonNegligibleSubSectionsAlignedWith
import moe.nea.firmament.util.math.Projections
import moe.nea.firmament.util.mc.CustomRenderPassHelper

object RenderCircleProgress {

	fun renderCircularSlice(
		drawContext: DrawContext,
		layer: RenderLayer.MultiPhase,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
		angleRadians: ClosedFloatingPointRange<Float>,
		color: Int = -1,
		innerCutoutRadius: Float = 0F
	) { // TODO: this is fixed by adding a special gui element renderer
		val sections = angleRadians.nonNegligibleSubSectionsAlignedWith((τ / 8f).toFloat())
			.zipWithNext().toList()
		BufferAllocator(layer.vertexFormat.vertexSize * sections.size * 3).use { allocator ->

			val bufferBuilder = BufferBuilder(allocator, VertexFormat.DrawMode.TRIANGLES, layer.vertexFormat)
			val matrix: Matrix3x2f = drawContext.matrices

			for ((sectionStart, sectionEnd) in sections) {
				val firstPoint = Projections.Two.projectAngleOntoUnitBox(sectionStart.toDouble())
				val secondPoint = Projections.Two.projectAngleOntoUnitBox(sectionEnd.toDouble())
				fun ilerp(f: Float): Float =
					ilerp(-1f, 1f, f)

				bufferBuilder
					.vertex(matrix, secondPoint.x, secondPoint.y, 0F)
					.texture(lerp(u1, u2, ilerp(secondPoint.x)), lerp(v1, v2, ilerp(secondPoint.y)))
					.color(color)
					
				bufferBuilder
					.vertex(matrix, firstPoint.x, firstPoint.y, 0F)
					.texture(lerp(u1, u2, ilerp(firstPoint.x)), lerp(v1, v2, ilerp(firstPoint.y)))
					.color(color)
					
				bufferBuilder
					.vertex(matrix, 0F, 0F, 0F)
					.texture(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
					.color(color)
					
			}

			bufferBuilder.end().use { buffer ->
				if (innerCutoutRadius <= 0) {
					layer.draw(buffer)
					return
				}
				CustomRenderPassHelper(
					{ "RenderCircleProgress" },
					VertexFormat.DrawMode.TRIANGLES,
					layer.vertexFormat,
					MC.instance.framebuffer,
					false,
				).use { renderPass ->
					renderPass.uploadVertices(buffer)
					renderPass.setPipeline(layer.pipeline)
					renderPass.setUniform("InnerCutoutRadius", 4) {
						it.putFloat(innerCutoutRadius)
					}
					renderPass.draw()
				}
			}
		}
	}

	fun renderCircle(
		drawContext: DrawContext,
		texture: Identifier,
		progress: Float,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
	) {
		renderCircularSlice(
			drawContext,
			CustomRenderLayers.GUI_TEXTURED_NO_DEPTH_TRIS.apply(texture),
			u1, u2, v1, v2,
			(-τ / 4).toFloat()..(progress * τ - τ / 4).toFloat()
		)
	}
}
