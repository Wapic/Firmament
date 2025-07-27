package moe.nea.firmament.util.mc

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.OptionalDouble
import java.util.OptionalInt
import org.joml.Vector4f
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BuiltBuffer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.util.BufferAllocator
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC


class CustomRenderPassHelper(
	val labelSupplier: () -> String,
	val drawMode: VertexFormat.DrawMode,
	val vertexFormat: VertexFormat,
	val frameBuffer: Framebuffer,
	val hasDepth: Boolean,
) : AutoCloseable {
	private val scope = mutableListOf<AutoCloseable>()
	private val preparations = mutableListOf<(RenderPass) -> Unit>()
	val device = RenderSystem.getDevice()
	private var hasPipelineAction = false
	val commandEncoder = device.createCommandEncoder()
	fun setPipeline(pipeline: RenderPipeline) {
		ErrorUtil.softCheck("Already has a pipeline", !hasPipelineAction)
		hasPipelineAction = true
		queueAction {
			it.setPipeline(pipeline)
		}
	}

	fun bindSampler(name: String, texture: Identifier) {
		bindSampler(name, MC.textureManager.getTexture(texture))
	}

	fun bindSampler(name: String, texture: AbstractTexture) {
		queueAction { it.bindSampler(name, texture.glTextureView) }
	}

	fun setAllDefaultUniforms() {
		queueAction {
			RenderSystem.bindDefaultUniforms(it)
		}
		setUniform(
			"DynamicTransforms", RenderSystem.getDynamicUniforms()
				.write(
					RenderSystem.getModelViewMatrix(),
					Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
					RenderSystem.getModelOffset(),
					RenderSystem.getTextureMatrix(),
					RenderSystem.getShaderLineWidth()
				)
		)
	}

	fun setUniform(name: String, slice: GpuBufferSlice) = queueAction { it.setUniform(name, slice) }
	fun setUniform(name: String, slice: GpuBuffer) = queueAction { it.setUniform(name, slice) }

	fun setUniform(name: String, size: Int, labelSupplier: () -> String = { name }, init: (Std140Builder) -> Unit) {
		val buffer = createUniformBuffer(labelSupplier, allocateByteBuf(size, init))
		setUniform(name, buffer)
	}

	var vertices: BuiltBuffer? = null

	fun uploadVertices(size: Int, init: (BufferBuilder) -> Unit) {
		uploadVertices(
			BufferBuilder(queueClose(BufferAllocator(size)), drawMode, vertexFormat)
				.also(init)
				.end()
		)
	}

	fun uploadVertices(buffer: BuiltBuffer) {
		queueClose(buffer)
		ErrorUtil.softCheck("Vertices have already been uploaded", vertices == null)
		vertices = buffer
		val vertexBuffer = vertexFormat.uploadImmediateVertexBuffer(buffer.buffer)
		val indexBufferConstructor = RenderSystem.getSequentialBuffer(drawMode)
		val indexBuffer = indexBufferConstructor.getIndexBuffer(buffer.drawParameters.indexCount)
		queueAction {
			it.setIndexBuffer(indexBuffer, indexBufferConstructor.indexType)
			it.setVertexBuffer(0, vertexBuffer)
		}
	}

	fun createUniformBuffer(labelSupplier: () -> String, buffer: ByteBuffer): GpuBuffer {
		return queueClose(
			device.createBuffer(
				labelSupplier::invoke,
				GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_READ,
				buffer
			)
		)
	}

	fun allocateByteBuf(size: Int, init: (Std140Builder) -> Unit): ByteBuffer {
		return Std140Builder.intoBuffer(
			ByteBuffer
				.allocateDirect(MathHelper.roundUpToMultiple(size, 16))
				.order(ByteOrder.nativeOrder())
		).also(init).get()
	}

	fun queueAction(action: (RenderPass) -> Unit) {
		preparations.add(action)
	}

	fun <T : AutoCloseable> queueClose(t: T): T = t.also { scope.add(it) }
	override fun close() {
		scope.reversed().forEach { it.close() }
	}

	object DrawToken

	fun draw(): DrawToken {
		val vertexData = (ErrorUtil.notNullOr(vertices, "No vertex data uploaded") { return DrawToken })
		ErrorUtil.softCheck("Missing a pipeline", hasPipelineAction)
		val renderPass = queueClose(
			commandEncoder.createRenderPass(
				labelSupplier::invoke,
				RenderSystem.outputColorTextureOverride ?: frameBuffer.getColorAttachmentView(),
				OptionalInt.empty(),
				(RenderSystem.outputDepthTextureOverride
					?: frameBuffer.getDepthAttachmentView()).takeIf { frameBuffer.useDepthAttachment && hasDepth },
				OptionalDouble.empty()
			)
		)
		preparations.forEach { it(renderPass) }
		renderPass.drawIndexed(
			0,
			0,
			vertexData.drawParameters.indexCount,
			1
		)
		return DrawToken
	}
}
