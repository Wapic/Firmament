package moe.nea.firmament.features.misc

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.systems.RenderSystem
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.OptionalDouble
import java.util.OptionalInt
import org.joml.Vector4f
import util.render.CustomRenderPipelines
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark

object CustomCapes : FirmamentFeature {
	override val identifier: String
		get() = "developer-capes"

	object TConfig : ManagedConfig(identifier, Category.DEV) {
		val showCapes by toggle("show-cape") { true }
	}

	override val config: ManagedConfig
		get() = TConfig

	interface CustomCapeRenderer {
		fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		)
	}

	data class TexturedCapeRenderer(
		val location: Identifier
	) : CustomCapeRenderer {
		override fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		) {
			model(vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(location)))
		}
	}

	data class ParallaxedHighlightCapeRenderer(
		val template: Identifier,
		val background: Identifier,
		val overlay: Identifier,
		val animationSpeed: Duration,
	) : CustomCapeRenderer {
		override fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		) {
			// TODO: figure out how exactly the rotation abstraction works
			val animationValue = (startTime.passedTime() / animationSpeed).mod(1F)
			val commandEncoder = RenderSystem.getDevice().createCommandEncoder()
			val animationBufSource = Std140Builder.intoBuffer(ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()))
				.putFloat(animationValue.toFloat())
				.align(16)
				.get()
			BufferAllocator(2048).use { allocator ->
				val bufferBuilder = BufferBuilder(allocator, renderLayer.drawMode, renderLayer.vertexFormat)
				model(bufferBuilder)
				bufferBuilder.end().use { buffer ->
					val vertexBuffer = renderLayer.vertexFormat.uploadImmediateVertexBuffer(buffer.buffer)
					val indexBufferConstructor = RenderSystem.getSequentialBuffer(renderLayer.drawMode)
					val indexBuffer = indexBufferConstructor.getIndexBuffer(buffer.drawParameters.indexCount)
					val templateTexture = MC.textureManager.getTexture(template)
					val backgroundTexture = MC.textureManager.getTexture(background)
					val foregroundTexture = MC.textureManager.getTexture(overlay)
					val dynamicTransforms = RenderSystem.getDynamicUniforms()
						.write(
							RenderSystem.getModelViewMatrix(),
							Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
							RenderSystem.getModelOffset(),
							RenderSystem.getTextureMatrix(),
							RenderSystem.getShaderLineWidth()
						)
					val framebuffer = MC.instance.framebuffer
					val colorAttachment =
						RenderSystem.outputColorTextureOverride ?: framebuffer.getColorAttachmentView()
					val depthAttachment =
						(RenderSystem.outputDepthTextureOverride
							?: framebuffer.getDepthAttachmentView()).takeIf { framebuffer.useDepthAttachment }

					RenderSystem.getDevice()
						.createBuffer({ "Firm Cape Animation" }, GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_READ, animationBufSource)
						.use { animationUniformBuf ->
							commandEncoder.createRenderPass(
								{ "FirmamentCustomCape" },
								colorAttachment,
								OptionalInt.empty(),
								depthAttachment,
								OptionalDouble.empty(),
							).use { renderPass ->
								// TODO: account for lighting
								renderPass.setPipeline(CustomRenderPipelines.PARALLAX_CAPE_SHADER)
								renderPass.bindSampler("Sampler0", templateTexture.glTextureView)
								renderPass.bindSampler("Sampler1", backgroundTexture.glTextureView)
								renderPass.bindSampler("Sampler3", foregroundTexture.glTextureView)
								RenderSystem.bindDefaultUniforms(renderPass)
								renderPass.setUniform("DynamicTransforms", dynamicTransforms)
								renderPass.setUniform("Animation", animationUniformBuf)
								renderPass.setIndexBuffer(indexBuffer, indexBufferConstructor.indexType)
								renderPass.setVertexBuffer(0, vertexBuffer)
								renderPass.drawIndexed(0, 0, buffer.drawParameters.indexCount, 1)
							}

						}
				}
			}
		}
	}

	interface CapeStorage {
		companion object {
			@JvmStatic
			fun cast(playerEntityRenderState: PlayerEntityRenderState) =
				playerEntityRenderState as CapeStorage

		}

		var cape_firmament: CustomCape?
	}

	data class CustomCape(
		val id: String,
		val label: String,
		val render: CustomCapeRenderer,
	)

	enum class AllCapes(val label: String, val render: CustomCapeRenderer) {
		FIRMAMENT_ANIMATED(
			"Animated Firmament", ParallaxedHighlightCapeRenderer(
				Firmament.identifier("textures/cape/parallax_template.png"),
				Firmament.identifier("textures/cape/parallax_background.png"),
				Firmament.identifier("textures/cape/firmament_star.png"),
				110.seconds
			)
		),

		FURFSKY_STATIC(
			"FurfSky",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/fsr_static.png"))
		),

		FIRMAMENT_STATIC(
			"Firmament",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/firm_static.png"))
		)
		;

		val cape = CustomCape(name, label, render)
	}

	val byId = AllCapes.entries.associateBy { it.cape.id }
	val byUuid =
		listOf(
			listOf(
				Devs.nea to AllCapes.FIRMAMENT_ANIMATED,
				Devs.kath to AllCapes.FIRMAMENT_STATIC,
				Devs.jani to AllCapes.FIRMAMENT_ANIMATED,
			),
			Devs.FurfSky.all.map { it to AllCapes.FURFSKY_STATIC },
		).flatten().flatMap { (dev, cape) -> dev.uuids.map { it to cape.cape } }.toMap()

	@JvmStatic
	fun render(
		playerEntityRenderState: PlayerEntityRenderState,
		vertexConsumer: VertexConsumer,
		renderLayer: RenderLayer,
		vertexConsumerProvider: VertexConsumerProvider,
		matrixStack: MatrixStack,
		model: (VertexConsumer) -> Unit
	) {
		val capeStorage = CapeStorage.cast(playerEntityRenderState)
		val firmCape = capeStorage.cape_firmament
		if (firmCape != null) {
			firmCape.render.replaceRender(renderLayer, vertexConsumerProvider, matrixStack, model)
		} else {
			model(vertexConsumer)
		}
	}

	@JvmStatic
	fun addCapeData(
		player: AbstractClientPlayerEntity,
		playerEntityRenderState: PlayerEntityRenderState
	) {
		val cape = if (TConfig.showCapes) byUuid[player.uuid] else null
		val capeStorage = CapeStorage.cast(playerEntityRenderState)
		if (cape == null) {
			capeStorage.cape_firmament = null
		} else {
			capeStorage.cape_firmament = cape
			playerEntityRenderState.skinTextures = SkinTextures(
				playerEntityRenderState.skinTextures.texture,
				playerEntityRenderState.skinTextures.textureUrl,
				Firmament.identifier("placeholder/fake_cape"),
				playerEntityRenderState.skinTextures.elytraTexture,
				playerEntityRenderState.skinTextures.model,
				playerEntityRenderState.skinTextures.secure,
			)
			playerEntityRenderState.capeVisible = true
		}
	}

	val startTime = TimeMark.now()
}
