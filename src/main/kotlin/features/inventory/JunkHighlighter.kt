package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.skyblock.SBItemUtil.getSearchName
import moe.nea.firmament.util.useMatch

object JunkHighlighter : FirmamentFeature {
	override val identifier: String
		get() = "junk-highlighter"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val junkRegex by string("regex") { "" }
		val highlightBind by keyBinding("highlight") { GLFW.GLFW_KEY_LEFT_CONTROL }
	}

	@Subscribe
	fun onDrawSlot(event: SlotRenderEvents.After) {
		if(!TConfig.highlightBind.isPressed() || TConfig.junkRegex.isEmpty()) return
		val junkRegex = TConfig.junkRegex.toPattern()
		val slot = event.slot
		junkRegex.useMatch(slot.stack.getSearchName()) {
			event.context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0xffff0000.toInt())
		}
	}
}
