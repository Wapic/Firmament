package moe.nea.firmament.features.world

import com.mojang.brigadier.arguments.IntegerArgumentType
import me.shedaniel.math.Color
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.asFakeServer
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.tr

object Waypoints : FirmamentFeature {
	override val identifier: String
		get() = "waypoints"

	object TConfig : ManagedConfig(identifier, Category.MINING) { // TODO: add to misc
		val tempWaypointDuration by duration("temp-waypoint-duration", 0.seconds, 1.hours) { 30.seconds }
		val showIndex by toggle("show-index") { true }
		val skipToNearest by toggle("skip-to-nearest") { false }
		// TODO: look ahead size
	}

	override val config get() = TConfig
	var waypoints: FirmWaypoints? = null
	var orderedIndex = 0

	@Subscribe
	fun onRenderOrderedWaypoints(event: WorldRenderLastEvent) {
		val w = useNonEmptyWaypoints() ?: return
		RenderInWorldContext.renderInWorld(event) {
			if (!w.isOrdered) {
				w.waypoints.withIndex().forEach {
					block(it.value.blockPos, 0x800050A0.toInt())
					if (TConfig.showIndex) withFacingThePlayer(it.value.blockPos.toCenterPos()) {
						text(Text.literal(it.index.toString()))
					}
				}
			} else {
				orderedIndex %= w.waypoints.size
				val firstColor = Color.ofRGBA(0, 200, 40, 180)
				color(firstColor)
				tracer(w.waypoints[orderedIndex].blockPos.toCenterPos(), lineWidth = 3f)
				w.waypoints.withIndex().toList().wrappingWindow(orderedIndex, 3).zip(listOf(
					firstColor,
					Color.ofRGBA(180, 200, 40, 150),
					Color.ofRGBA(180, 80, 20, 140),
				)).reversed().forEach { (waypoint, col) ->
					val (index, pos) = waypoint
					block(pos.blockPos, col.color)
					if (TConfig.showIndex) withFacingThePlayer(pos.blockPos.toCenterPos()) {
						text(Text.literal(index.toString()))
					}
				}
			}
		}
	}

	@Subscribe
	fun onTick(event: TickEvent) {
		val w = useNonEmptyWaypoints() ?: return
		if (!w.isOrdered) return
		orderedIndex %= w.waypoints.size
		val p = MC.player?.pos ?: return
		if (TConfig.skipToNearest) {
			orderedIndex =
				(w.waypoints.withIndex().minBy { it.value.blockPos.getSquaredDistance(p) }.index + 1) % w.waypoints.size

		} else {
			if (w.waypoints[orderedIndex].blockPos.isWithinDistance(p, 3.0)) {
				orderedIndex = (orderedIndex + 1) % w.waypoints.size
			}
		}
	}


	fun useEditableWaypoints(): FirmWaypoints {
		var w = waypoints
		if (w == null) {
			w = FirmWaypoints("Unlabeled", "unlabeled", null, mutableListOf(), false)
			waypoints = w
		}
		return w
	}

	fun useNonEmptyWaypoints(): FirmWaypoints? {
		val w = waypoints
		if (w == null) return null
		if (w.waypoints.isEmpty()) return null
		return w
	}

	@Subscribe
	fun onCommand(event: CommandEvent.SubCommand) {
		event.subcommand("waypoint") {
			thenArgument("pos", BlockPosArgumentType.blockPos()) { pos ->
				thenExecute {
					source
					val position = pos.get(this).toAbsoluteBlockPos(source.asFakeServer())
					val w = useEditableWaypoints()
					w.waypoints.add(FirmWaypoints.Waypoint.from(position))
					source.sendFeedback(Text.stringifiedTranslatable("firmament.command.waypoint.added",
					                                                 position.x,
					                                                 position.y,
					                                                 position.z))
				}
			}
		}
		event.subcommand("waypoints") {
			thenLiteral("clear") {
				thenExecute {
					waypoints?.waypoints?.clear()
					source.sendFeedback(Text.translatable("firmament.command.waypoint.clear"))
				}
			}
			thenLiteral("toggleordered") {
				thenExecute {
					val w = useEditableWaypoints()
					w.isOrdered = !w.isOrdered
					if (w.isOrdered) {
						val p = MC.player?.pos ?: Vec3d.ZERO
						orderedIndex = // TODO: this should be extracted to a utility method
							w.waypoints.withIndex().minByOrNull { it.value.blockPos.getSquaredDistance(p) }?.index ?: 0
					}
					source.sendFeedback(Text.translatable("firmament.command.waypoint.ordered.toggle.${w.isOrdered}"))
				}
			}
			thenLiteral("skip") {
				thenExecute {
					val w = useNonEmptyWaypoints()
					if (w != null && w.isOrdered) {
						orderedIndex = (orderedIndex + 1) % w.size
						source.sendFeedback(Text.translatable("firmament.command.waypoint.skip"))
					} else {
						source.sendError(Text.translatable("firmament.command.waypoint.skip.error"))
					}
				}
			}
			thenLiteral("remove") {
				thenArgument("index", IntegerArgumentType.integer(0)) { indexArg ->
					thenExecute {
						val index = get(indexArg)
						val w = useNonEmptyWaypoints()
						if (w != null && index in w.waypoints.indices) {
							w.waypoints.removeAt(index)
							source.sendFeedback(Text.stringifiedTranslatable("firmament.command.waypoint.remove",
							                                                 index))
						} else {
							source.sendError(Text.stringifiedTranslatable("firmament.command.waypoint.remove.error"))
						}
					}
				}
			}
			thenLiteral("export") {
				thenExecute {
					TODO()
//					val data = Firmament.tightJson.encodeToString<List<ColeWeightWaypoint>>(waypoints.map {
//						ColeWeightWaypoint(it.x,
//						                   it.y,
//						                   it.z)
//					})
//					ClipboardUtils.setTextContent(data)
//					source.sendFeedback(tr("firmament.command.waypoint.export",
//					                       "Copied ${waypoints.size} waypoints to clipboard"))
				}
			}
			thenLiteral("exportrelative") {
				thenExecute {
					TODO()
//					val playerPos = MC.player!!.blockPos
//					val x = playerPos.x
//					val y = playerPos.y
//					val z = playerPos.z
//					val data = Firmament.tightJson.encodeToString<List<ColeWeightWaypoint>>(waypoints.map {
//						ColeWeightWaypoint(it.x - x,
//						                   it.y - y,
//						                   it.z - z)
//					})
//					ClipboardUtils.setTextContent(data)
//					source.sendFeedback(tr("firmament.command.waypoint.export.relative",
//					                       "Copied ${waypoints.size} relative waypoints to clipboard. Make sure to stand in the same position when importing."))
//
				}
			}
			thenLiteral("import") {
				thenExecute {
					source.sendFeedback(
						importRelative(BlockPos.ORIGIN)// TODO: rework imports
							?: Text.stringifiedTranslatable("firmament.command.waypoint.import",
							                                useNonEmptyWaypoints()?.waypoints?.size),
					)
				}
			}
			thenLiteral("importrelative") {
				thenExecute {
					source.sendFeedback(
						importRelative(MC.player!!.blockPos) ?: tr("firmament.command.waypoint.import.relative",
						                                           "Imported ${useNonEmptyWaypoints()?.waypoints?.size} relative waypoints from clipboard. Make sure you stand in the same position as when you exported these waypoints for them to line up correctly."),
					)
				}
			}
		}
	}

	fun importRelative(pos: BlockPos): Text? {
		val contents = ClipboardUtils.getTextContents()
		val cw = ColeWeightCompat.tryParse(contents).map { ColeWeightCompat.intoFirm(it) }
		waypoints = cw.getOrNull() // TODO: directly parse firm waypoints
		return null // TODO: show error if this does not work
		// TODO: make relative imports work again
	}

}

fun <E> List<E>.wrappingWindow(startIndex: Int, windowSize: Int): List<E> {
	val result = ArrayList<E>(windowSize)
	if (startIndex + windowSize < size) {
		result.addAll(subList(startIndex, startIndex + windowSize))
	} else {
		result.addAll(subList(startIndex, size))
		result.addAll(subList(0, minOf(windowSize - (size - startIndex), startIndex)))
	}
	return result
}
