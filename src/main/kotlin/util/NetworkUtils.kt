package moe.nea.firmament.util

import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket

object NetworkUtils {
	var ping: Long? = null
	var isPinging: Boolean = false
	private var lastPingCheck = 0L

	fun getPing() : Long {
		if(System.currentTimeMillis() - lastPingCheck > 5000)
		{
			lastPingCheck = System.currentTimeMillis();
			isPinging = true
			MC.networkHandler?.sendPacket(QueryPingC2SPacket(System.currentTimeMillis()))
		}
		return ping ?: 1
	}
}
