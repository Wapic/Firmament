package moe.nea.firmament.features.events.carnival

import me.shedaniel.math.Color
import net.minecraft.block.Blocks
import net.minecraft.block.RedstoneLampBlock
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.BlockChangeEvent
import moe.nea.firmament.events.EntityDespawnEvent
import moe.nea.firmament.events.EntityRenderTintEvent
import moe.nea.firmament.events.EntitySpawnEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.debug.DebugLogger
import moe.nea.firmament.features.diana.toBlockPos
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.NetworkUtils
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.render.TintedOverlayTexture

object ZombieShootoutHelper {

	val log = DebugLogger("ZombieShootout-Helper")

	var isActive = false

	var litLamp: Vec3d? = null
	val entityList: MutableList<ZombieEntity> = mutableListOf()

	const val GAME_START_MESSAGE: String = "[NPC] Carnival Cowboy: Good luck, pal!"
	const val GAME_END_MESSAGE: String = "Zombie Shootout"

	data class ZombieHelmet(val helmet: Item, val color: Color) {
		val tintOverlay by lazy {
			TintedOverlayTexture().setColor(color)
		}
	}

	val zombieColors = listOf(
		ZombieHelmet(Items.LEATHER_HELMET, Color.ofRGB(100, 50, 0)),
		ZombieHelmet(Items.IRON_HELMET, Color.ofRGB(255, 255, 255)),
		ZombieHelmet(Items.GOLDEN_HELMET, Color.ofRGB(255, 170, 0)),
		ZombieHelmet(Items.DIAMOND_HELMET, Color.ofRGB(85, 255, 255)),
	)

	@Subscribe
	fun onChat(event: ProcessChatEvent) {
		if(CarnivalFeatures.TConfig.enableZombieShootoutHelper) {
			when(event.unformattedString) {
				GAME_START_MESSAGE -> {
					log.log { "Game Started" }
					isActive = true
				}
				GAME_END_MESSAGE -> {
					log.log { "Game Ended" }
					isActive = false
				}
			}
		}
	}

	@Subscribe
	fun onBlockChange(event: BlockChangeEvent) {
		if(isActive && event.oldBlockState.block == Blocks.REDSTONE_LAMP && event.newBlockState.block == Blocks.REDSTONE_LAMP) {
			litLamp = if(event.newBlockState.get(RedstoneLampBlock.LIT)) event.blockPos.toCenterPos() else null
		}
	}

	@Subscribe
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if(isActive) entityList.add(event.entity as? ZombieEntity ?: return)
	}

	@Subscribe
	fun onEntityDespawn(event: EntityDespawnEvent) {
		if(isActive) entityList.remove(event.entity as? ZombieEntity ?: return)
	}

	@Subscribe
	fun onRenderWorld(event: WorldRenderLastEvent) {
		if(!isActive) return

		RenderInWorldContext.renderInWorld(event) {
			litLamp?.let {
				wireframeCube(it.toBlockPos())

				tracer(it) // TODO: Tracer method won't draw a line to blocks that are above the cameras view angle
			}

			entityList.forEach { entity ->
				val color = helmetToColor[entity.armorItems.first().item]?.color?.color ?: return@forEach
				val travelTime = (NetworkUtils.getPing() / 50) + (entity.distanceTo(MC.player) / 3)
				val entityPos = entity.pos.add(
					(entity.x - entity.prevX) + entity.movement.x * travelTime,
					1.0,
					(entity.z - entity.prevZ) + entity.movement.z * travelTime
				)

				tinyBlock(entityPos, 0.33f, color)
			}
		}
	}

	val helmetToColor = zombieColors.associateBy { it.helmet }

	@Subscribe
	fun onEntityRender(event: EntityRenderTintEvent) {
		if(isActive) {
			val entity = event.entity as? ZombieEntity ?: return
			val helmet = helmetToColor[entity.armorItems.first().item]
			event.renderState.overlayTexture_firmament = helmet?.tintOverlay ?: return
		}
	}

	@Subscribe
	fun reset(event: WorldReadyEvent) {
		isActive = false
		litLamp = null
		entityList.clear()
	}
}
