package moe.nea.firmament.features.items.recipes

import java.util.Optional
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.keybindings.GenericInputAction
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.repo.recipes.RecipeLayouter
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.FirmFormatters.shortFormat
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.darkGrey
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt

class ItemSlotWidget(
	val point: Point,
	var content: List<SBItemStack>,
	val slotKind: RecipeLayouter.SlotKind
) : RecipeWidget(),
	RecipeLayouter.CyclingItemSlot {
	val backgroundTopLeft =
		if (slotKind.isBig) Point(point.x - 4, point.y - 4)
		else Point(point.x - 1, point.y - 1)
	val backgroundSize =
		if (slotKind.isBig) Dimension(16 + 8, 16 + 8)
		else Dimension(18, 18)
	val itemRect = Rectangle(point, Dimension(16, 16))
	override val rect: Rectangle
		get() = Rectangle(backgroundTopLeft, backgroundSize)

	@OptIn(ExpensiveItemCacheApi::class)
	override fun render(
		guiGraphics: GuiGraphics,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
		val stack = current().asImmutableItemStack()
		// TODO: draw slot background
		if (stack.isEmpty) return
		guiGraphics.renderItem(stack, point.x, point.y)
		guiGraphics.renderItemDecorations(
			MC.font, stack, point.x, point.y,
			if (stack.count >= SHORT_NUM_CUTOFF) shortFormat(stack.count.toDouble())
			else null
		)
		if (itemRect.contains(mouseX, mouseY))
			guiGraphics.setTooltipForNextFrame(
				MC.font, getTooltip(stack), Optional.empty(),
				mouseX, mouseY
			)
	}

	companion object {
		val SHORT_NUM_CUTOFF = 1000
		var canUseTooltipEvent = true
	}

	fun getTooltip(itemStack: ItemStack): List<Component> {
		val lore = mutableListOf(itemStack.displayNameAccordingToNbt)
		lore.addAll(itemStack.loreAccordingToNbt)
		if (canUseTooltipEvent) {
			try {
				ItemTooltipCallback.EVENT.invoker().getTooltip(
					itemStack, Item.TooltipContext.EMPTY,
					TooltipFlag.NORMAL, lore
				)
			} catch (ex: Exception) {
				canUseTooltipEvent = false
				ErrorUtil.softError("Failed to use vanilla tooltips", ex)
			}
		} else {
			ItemTooltipEvent.publish(
				ItemTooltipEvent(
					itemStack,
					Item.TooltipContext.EMPTY,
					TooltipFlag.NORMAL,
					lore
				)
			)
		}
		if (itemStack.count >= SHORT_NUM_CUTOFF && lore.isNotEmpty())
			lore.add(1, Component.literal("${itemStack.count}x").darkGrey())
		return lore
	}

	override fun tick() {
		if (SavedKeyBinding.isShiftDown()) return
		if (content.size <= 1) return
		if (MC.currentTick % 5 != 0) return
		index = (index + 1) % content.size
	}

	var index = 0
	var onUpdate: () -> Unit = {}

	override fun onUpdate(action: () -> Unit) {
		this.onUpdate = action
	}

	override fun current(): SBItemStack {
		return content.getOrElse(index) { SBItemStack.EMPTY }
	}

	override fun update(newValue: SBItemStack) {
		content = listOf(newValue)
		// SAFE: content was just assigned to a non-empty list
		index = index.coerceIn(content.indices)
	}
}
