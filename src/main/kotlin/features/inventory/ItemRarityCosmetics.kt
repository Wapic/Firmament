package moe.nea.firmament.features.inventory

import java.awt.Color
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HotbarItemRenderEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.skyblock.Rarity

object ItemRarityCosmetics {
	val identifier: String
		get() = "item-rarity-cosmetics"

	@Config
	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val showItemRarityBackground by toggle("background") { false }
		val showItemRarityInHotbar by toggle("background-hotbar") { false }
	}

	private val rarityToColor = Rarity.colourMap.mapValues {
		val c = Color(it.value.colorValue!!)
		c.rgb
	}

	fun drawItemStackRarity(drawContext: DrawContext, x: Int, y: Int, item: ItemStack) {
		val rarity = Rarity.fromItem(item) ?: return
		val rgb = rarityToColor[rarity] ?: 0xFF00FF80.toInt()
		drawContext.drawGuiTexture(
			RenderPipelines.GUI_TEXTURED,
			Identifier.of("firmament:item_rarity_background"),
			x, y,
			16, 16,
			rgb
		)
	}


	@Subscribe
	fun onRenderSlot(it: SlotRenderEvents.Before) {
		if (!TConfig.showItemRarityBackground) return
		val stack = it.slot.stack ?: return
		drawItemStackRarity(it.context, it.slot.x, it.slot.y, stack)
	}

	@Subscribe
	fun onRenderHotbarItem(it: HotbarItemRenderEvent) {
		if (!TConfig.showItemRarityInHotbar) return
		val stack = it.item
		drawItemStackRarity(it.context, it.x, it.y, stack)
	}
}
