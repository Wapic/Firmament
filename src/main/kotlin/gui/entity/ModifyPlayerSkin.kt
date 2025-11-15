package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.experimental.and
import kotlin.experimental.or
import net.minecraft.client.network.ClientPlayerLikeEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.PlayerLikeEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerModelPart
import net.minecraft.entity.player.PlayerSkinType
import net.minecraft.entity.player.SkinTextures
import net.minecraft.util.AssetInfo
import net.minecraft.util.Identifier

object ModifyPlayerSkin : EntityModifier {
	val playerModelPartIndex = PlayerModelPart.entries.associateBy { it.getName() }
	override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
		require(entity is GuiPlayer)
		var capeTexture = entity.skinTextures.cape
		var model = entity.skinTextures.model
		var bodyTexture = entity.skinTextures.body
		fun mkTexAsset(id: Identifier) = AssetInfo.TextureAssetInfo(id, id)
		info["cape"]?.let {
			capeTexture = mkTexAsset(Identifier.of(it.asString))
		}
		info["skin"]?.let {
			bodyTexture = mkTexAsset(Identifier.of(it.asString))
		}
		info["slim"]?.let {
			model = if (it.asBoolean) PlayerSkinType.SLIM else PlayerSkinType.WIDE
		}
		info["parts"]?.let {
			var trackedData = entity.dataTracker.get(PlayerLikeEntity.PLAYER_MODE_CUSTOMIZATION_ID)
			if (it is JsonPrimitive && it.isBoolean) {
				trackedData = (if (it.asBoolean) -1 else 0).toByte()
			} else {
				val obj = it.asJsonObject
				for ((k, v) in obj.entrySet()) {
					val part = playerModelPartIndex[k]!!
					trackedData = if (v.asBoolean) {
						trackedData and (part.bitFlag.inv().toByte())
					} else {
						trackedData or (part.bitFlag.toByte())
					}
				}
			}
			entity.dataTracker.set(PlayerEntity.PLAYER_MODE_CUSTOMIZATION_ID, trackedData)
		}
		entity.skinTextures = SkinTextures(
			bodyTexture, capeTexture, null, model, true
		)
		return entity
	}

}
