

package moe.nea.firmament.events

import net.minecraft.client.render.Camera
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.state.CameraRenderState
import net.minecraft.client.util.math.MatrixStack

/**
 * This event is called after all world rendering is done, but before any GUI rendering (including hand) has been done.
 */
data class WorldRenderLastEvent(
    val matrices: MatrixStack,
    val tickCounter: Int,
    val camera: CameraRenderState,
    val vertexConsumers: VertexConsumerProvider.Immediate,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<WorldRenderLastEvent>()
}
