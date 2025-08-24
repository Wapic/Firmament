package moe.nea.firmament.events

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.network.ClientPlayNetworkHandler

data class JoinServerEvent(
	val networkHandler: ClientPlayNetworkHandler,
	val packetSender: PacketSender,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<JoinServerEvent>()
}
