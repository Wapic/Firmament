package moe.nea.firmament.features.items

import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.block.Blocks
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.tr

object EtherwarpOverlay : FirmamentFeature {
	override val identifier: String
		get() = "etherwarp-overlay"

	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var etherwarpOverlay by toggle("etherwarp-overlay") { false }
		var onlyShowWhileSneaking by toggle("only-show-while-sneaking") { true }
		var cube by toggle("cube") { true }
		val cubeColour by colour("cube-colour") { ChromaColour.fromStaticRGB(172, 0, 255, 60) }
		val failureCubeColour by colour("cube-colour-fail") { ChromaColour.fromStaticRGB(255, 0, 172, 60) }
		val tooCloseCubeColour by colour("cube-colour-tooclose") { ChromaColour.fromStaticRGB(0, 255, 0, 60) }
		val tooFarCubeColour by colour("cube-colour-toofar") { ChromaColour.fromStaticRGB(255, 255, 0, 60) }
		var wireframe by toggle("wireframe") { false }
		var failureText by toggle("failure-text") { false }
	}

	override val config: ManagedConfig
		get() = TConfig

	enum class EtherwarpResult(val label: Text?, val color: () -> ChromaColour) {
		SUCCESS(null, TConfig::cubeColour),
		INTERACTION_BLOCKED(
			tr("firmament.etherwarp.fail.tooclosetointeractable", "Too close to interactable"),
			TConfig::tooCloseCubeColour
		),
		TOO_DISTANT(tr("firmament.etherwarp.fail.toofar", "Too far away"), TConfig::tooFarCubeColour),
		OCCUPIED(tr("firmament.etherwarp.fail.occupied", "Occupied"), TConfig::failureCubeColour),
	}

	val interactionBlocked = setOf(
		Blocks.HOPPER,
		Blocks.CHEST,
		Blocks.ENDER_CHEST,
		Blocks.FURNACE,
		Blocks.CRAFTING_TABLE,
		Blocks.CAULDRON,
		Blocks.WATER_CAULDRON,
		Blocks.ENCHANTING_TABLE,
		Blocks.DISPENSER,
		Blocks.DROPPER,
		Blocks.BREWING_STAND,
		Blocks.TRAPPED_CHEST,
		Blocks.OAK_DOOR,
		Blocks.IRON_DOOR,
		Blocks.OAK_TRAPDOOR,
		Blocks.SPRUCE_TRAPDOOR,
		Blocks.BIRCH_TRAPDOOR,
		Blocks.JUNGLE_TRAPDOOR,
		Blocks.ACACIA_TRAPDOOR,
		Blocks.CHERRY_TRAPDOOR,
		Blocks.DARK_OAK_TRAPDOOR,
		Blocks.PALE_OAK_TRAPDOOR,
		Blocks.MANGROVE_TRAPDOOR,
		Blocks.BAMBOO_TRAPDOOR,
		Blocks.IRON_TRAPDOOR,
		Blocks.SPRUCE_DOOR,
		Blocks.BIRCH_DOOR,
		Blocks.JUNGLE_DOOR,
		Blocks.ACACIA_DOOR,
		Blocks.CHERRY_DOOR,
		Blocks.DARK_OAK_DOOR,
		Blocks.PALE_OAK_DOOR,
		Blocks.MANGROVE_DOOR,
		Blocks.BAMBOO_DOOR,
		Blocks.CRIMSON_TRAPDOOR,
		Blocks.WARPED_TRAPDOOR,
		Blocks.CRIMSON_DOOR,
		Blocks.WARPED_DOOR,
		Blocks.COPPER_DOOR,
		Blocks.EXPOSED_COPPER_DOOR,
		Blocks.OXIDIZED_COPPER_DOOR,
		Blocks.WEATHERED_COPPER_DOOR,
		Blocks.WAXED_COPPER_DOOR,
		Blocks.WAXED_EXPOSED_COPPER_DOOR,
		Blocks.WAXED_OXIDIZED_COPPER_DOOR,
		Blocks.WAXED_WEATHERED_COPPER_DOOR,
		Blocks.COPPER_TRAPDOOR,
		Blocks.EXPOSED_COPPER_TRAPDOOR,
		Blocks.OXIDIZED_COPPER_TRAPDOOR,
		Blocks.WEATHERED_COPPER_TRAPDOOR,
		Blocks.WAXED_COPPER_TRAPDOOR,
		Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
		Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR,
		Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
	)

	val etherwarpHallpasses = setOf(
		Blocks.CREEPER_HEAD,
		Blocks.CREEPER_WALL_HEAD,
		Blocks.DRAGON_HEAD,
		Blocks.DRAGON_WALL_HEAD,
		Blocks.SKELETON_SKULL,
		Blocks.SKELETON_WALL_SKULL,
		Blocks.WITHER_SKELETON_SKULL,
		Blocks.WITHER_SKELETON_WALL_SKULL,
		Blocks.PIGLIN_HEAD,
		Blocks.PIGLIN_WALL_HEAD,
		Blocks.ZOMBIE_HEAD,
		Blocks.ZOMBIE_WALL_HEAD,
		Blocks.PLAYER_HEAD,
		Blocks.PLAYER_WALL_HEAD,
		Blocks.REPEATER,
		Blocks.COMPARATOR,
		Blocks.BIG_DRIPLEAF_STEM,
		Blocks.WHITE_CARPET,
		Blocks.ORANGE_CARPET,
		Blocks.MAGENTA_CARPET,
		Blocks.LIGHT_BLUE_CARPET,
		Blocks.YELLOW_CARPET,
		Blocks.LIME_CARPET,
		Blocks.PINK_CARPET,
		Blocks.GRAY_CARPET,
		Blocks.LIGHT_GRAY_CARPET,
		Blocks.CYAN_CARPET,
		Blocks.PURPLE_CARPET,
		Blocks.BLUE_CARPET,
		Blocks.BROWN_CARPET,
		Blocks.GREEN_CARPET,
		Blocks.RED_CARPET,
		Blocks.BLACK_CARPET,
		Blocks.MOSS_CARPET,
		Blocks.PALE_MOSS_CARPET,
	)

	fun isEtherwarpTransparent(world: BlockView, blockPos: BlockPos): Boolean {
		val blockState = world.getBlockState(blockPos)
		if (blockState.block.defaultState.getCollisionShape(world, blockPos).isEmpty)
			return true
		if (blockState.block in etherwarpHallpasses)
			return true
		return false
	}

	sealed interface EtherwarpBlockHit {
		data class BlockHit(val blockPos: BlockPos) : EtherwarpBlockHit
		data object Miss : EtherwarpBlockHit
	}

	fun raycastWithEtherwarpTransparency(world: BlockView, start: Vec3d, end: Vec3d): EtherwarpBlockHit {
		return BlockView.raycast<EtherwarpBlockHit, Unit>(
			start, end, Unit,
			{ _, blockPos ->
				if (isEtherwarpTransparent(world, blockPos)) {
					return@raycast null
				}
//				val defaultedState = world.getBlockState(blockPos).block.defaultState
//				val hitShape = defaultedState.getCollisionShape(
//					world,
//					blockPos,
//					ShapeContext.absent()
//				)
//				if (world.raycastBlock(start, end, blockPos, hitShape, defaultedState) == null) {
//					return@raycast null
//				}
				return@raycast EtherwarpBlockHit.BlockHit(blockPos)
			},
			{ EtherwarpBlockHit.Miss })
	}

	enum class EtherwarpItemKind{
		MERGED,
		RAW
	}

	@Subscribe
	fun renderEtherwarpOverlay(event: WorldRenderLastEvent) {
		if (!TConfig.etherwarpOverlay) return
		val player = MC.player ?: return
		if (TConfig.onlyShowWhileSneaking && !player.isSneaking) return
		val world = player.world
		val heldItem = MC.stackInHand
		val etherwarpTyp = run {
			if (heldItem.extraAttributes.contains("ethermerge"))
				EtherwarpItemKind.MERGED
			else if (heldItem.skyBlockId == SkyBlockItems.ETHERWARP_CONDUIT)
				EtherwarpItemKind.RAW
			else
				return
		}
		val playerEyeHeight =
			if (player.isSneaking || etherwarpTyp == EtherwarpItemKind.MERGED) 1.54 else 1.62 // Sneaking: 1.27 (1.21) 1.54 (1.8.9) / Upright: 1.62 (1.8.9,1.21)
		val playerEyePos = player.pos.add(0.0, playerEyeHeight, 0.0)
		val start = playerEyePos
		val end = player.getRotationVec(0F).multiply(120.0).add(playerEyePos)
		val hitResult = raycastWithEtherwarpTransparency(
			world,
			start,
			end,
		)
		if (hitResult !is EtherwarpBlockHit.BlockHit) return
		val blockPos = hitResult.blockPos
		val success = run {
			if (!isEtherwarpTransparent(world, blockPos.up()))
				EtherwarpResult.OCCUPIED
			else if (!isEtherwarpTransparent(world, blockPos.up(2)))
				EtherwarpResult.OCCUPIED
			else if (player.squaredDistanceTo(blockPos.toCenterPos()) > 61 * 61)
				EtherwarpResult.TOO_DISTANT
			else if ((MC.instance.crosshairTarget as? BlockHitResult)
					?.takeIf { it.type == HitResult.Type.BLOCK }
					?.let { world.getBlockState(it.blockPos).block in interactionBlocked }
					?: false
			)
				EtherwarpResult.INTERACTION_BLOCKED
			else
				EtherwarpResult.SUCCESS
		}
		RenderInWorldContext.renderInWorld(event) {
			if (TConfig.cube)
				block(
					blockPos,
					success.color().getEffectiveColourRGB()
				)
			if (TConfig.wireframe) wireframeCube(blockPos, 10f)
			if (TConfig.failureText && success.label != null) {
				withFacingThePlayer(blockPos.toCenterPos()) {
					text(success.label)
				}
			}
		}
	}
}
