package moe.nea.firmament.features.items.recipes

import java.util.Optional
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.api.v1.FirmamentAPI
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.ReloadRegistrationEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.accessors.castAccessor
import moe.nea.firmament.util.skyblockId

object ItemList {
	// TODO: add a global toggle for this and RecipeRegistry

	fun collectExclusions(screen: Screen): Set<ScreenRectangle> {
		val exclusions = mutableSetOf<ScreenRectangle>()
		if (screen is AbstractContainerScreen<*>) {
			val screenHandler = screen.castAccessor()
			exclusions.add(
				ScreenRectangle(
					screenHandler.x_Firmament,
					screenHandler.y_Firmament,
					screenHandler.backgroundWidth_Firmament,
					screenHandler.backgroundHeight_Firmament
				)
			)
		}
		FirmamentAPI.getInstance().extensions
			.forEach { extension ->
				for (rectangle in extension.getExclusionZones(screen)) {
					if (exclusions.any { it.encompasses(rectangle) })
						continue
					exclusions.add(rectangle)
				}
			}

		return exclusions
	}

	var reachableItems = listOf<SBItemStack>()
	var pageOffset = 0
	fun recalculateVisibleItems() {
		reachableItems = RepoManager.neuRepo.items
			.items.values.map { SBItemStack(it.skyblockId) }
	}

	@Subscribe
	fun onReload(event: ReloadRegistrationEvent) {
		event.repo.registerReloadListener { recalculateVisibleItems() }
	}

	fun coordinates(outer: ScreenRectangle, exclusions: Collection<ScreenRectangle>): Sequence<ScreenRectangle> {
		val entryWidth = 18
		val columns = outer.width / entryWidth
		val rows = outer.height / entryWidth
		val lowX = outer.right() - columns * entryWidth
		val lowY = outer.top()
		return generateSequence(0) { it + 1 }
			.map {
				val xIndex = it % columns
				val yIndex = it / columns
				ScreenRectangle(
					lowX + xIndex * entryWidth, lowY + yIndex * entryWidth,
					entryWidth, entryWidth
				)
			}
			.take(rows * columns)
			.filter { candidate -> exclusions.none { it.intersects(candidate) } }
	}

	var lastRenderPositions: List<Pair<ScreenRectangle, SBItemStack>> = listOf()
	var lastHoveredItemStack: Pair<ScreenRectangle, SBItemStack>? = null

	fun findStackUnder(mouseX: Int, mouseY: Int): Pair<ScreenRectangle, SBItemStack>? {
		val lhis = lastHoveredItemStack
		if (lhis != null && lhis.first.containsPoint(mouseX, mouseY))
			return lhis
		return lastRenderPositions.firstOrNull { it.first.containsPoint(mouseX, mouseY) }
	}

	@Subscribe
	fun onRender(event: HandledScreenForegroundEvent) {
		lastHoveredItemStack = null
		lastRenderPositions = listOf()
		val exclusions = collectExclusions(event.screen)
		val potentiallyVisible = reachableItems.subList(pageOffset, reachableItems.size)
		val screenWidth = event.screen.width
		val rightThird = ScreenRectangle(
			screenWidth - screenWidth / 3, 0,
			screenWidth / 3, event.screen.height
		)
		val coords = coordinates(rightThird, exclusions)

		lastRenderPositions = coords.zip(potentiallyVisible.asSequence()).toList()
		lastRenderPositions.forEach { (pos, stack) ->
			val realStack = stack.asLazyImmutableItemStack()
			val toRender = realStack ?: ItemStack(Items.PAINTING)
			event.context.renderItem(toRender, pos.left() + 1, pos.top() + 1)
			if (pos.containsPoint(event.mouseX, event.mouseY)) {
				lastHoveredItemStack = pos to stack
				event.context.setTooltipForNextFrame(
					MC.font,
					if (realStack != null)
						ItemSlotWidget.getTooltip(realStack)
					else
						stack.estimateLore(),
					Optional.empty(),
					event.mouseX, event.mouseY
				)
			}
		}
	}
}
