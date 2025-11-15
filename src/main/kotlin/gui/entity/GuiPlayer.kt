package moe.nea.firmament.gui.entity

import net.minecraft.client.network.ClientMannequinEntity
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.SkinTextures
import net.minecraft.world.World
import moe.nea.firmament.util.MC

fun makeGuiPlayer(world: World): GuiPlayer {
	val player = GuiPlayer(MC.instance.world!!)
	return player
}

class GuiPlayer(world: ClientWorld?) : ClientMannequinEntity(world, MC.instance.playerSkinCache) {
	override fun isSpectator(): Boolean {
		return false
	}

	override fun shouldRenderName(): Boolean {
		return false
	}

	var skinTextures: SkinTextures = DefaultSkinHelper.getSkinTextures(this.getUuid()) // TODO: 1.21.10
	override fun getSkin(): SkinTextures {
		return skinTextures
	}
}
