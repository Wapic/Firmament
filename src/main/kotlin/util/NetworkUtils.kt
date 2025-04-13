package moe.nea.firmament.util

import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.util.Util
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.WorldReadyEvent

object NetworkUtils {
	var averagePing = 0.0
	var isPinging = false
	var lastPingTime = 0L

	var averageTPS = 20.0
	var prevTime = 0L

	@Subscribe
	fun onWorldLoad(event: WorldReadyEvent) {
		reset()
	}

	fun sendPing() {
		if(isPinging) return
		if(lastPingTime - Util.getMeasuringTimeNano() > 10e6) reset()
		MC.networkHandler?.let {
			isPinging = true
			it.sendPacket(QueryPingC2SPacket(Util.getMeasuringTimeNano()))
		}
	}

	private fun reset(){
		prevTime = 0L
		averageTPS = 20.0
		averagePing = 0.0
	}
}
