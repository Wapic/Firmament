package moe.nea.firmament.events

import org.lwjgl.glfw.GLFW
import net.minecraft.client.gui.screen.ingame.HandledScreen
import moe.nea.firmament.keybindings.GenericInputAction
import moe.nea.firmament.keybindings.InputModifiers
import moe.nea.firmament.keybindings.SavedKeyBinding

sealed interface HandledScreenInputEvent {
	val screen: HandledScreen<*>
	val input: GenericInputAction
	val modifiers: InputModifiers
}

data class HandledScreenKeyPressedEvent(
	override val screen: HandledScreen<*>,
	override val input: GenericInputAction,
	override val modifiers: InputModifiers,
	// TODO: val isRepeat: Boolean,
) : FirmamentEvent.Cancellable(), HandledScreenInputEvent {
	fun matches(keyBinding: SavedKeyBinding, atLeast: Boolean = false): Boolean {
		return keyBinding.matches(input, modifiers, atLeast)
	}

	fun isLeftClick() = input == GenericInputAction.mouse(GLFW.GLFW_MOUSE_BUTTON_LEFT)
	companion object : FirmamentEventBus<HandledScreenKeyPressedEvent>()
}

data class HandledScreenKeyReleasedEvent(
	override val screen: HandledScreen<*>,
	override val input: GenericInputAction,
	override val modifiers: InputModifiers,
) : FirmamentEvent.Cancellable(), HandledScreenInputEvent {
	fun matches(keyBinding: SavedKeyBinding, atLeast: Boolean = false): Boolean {
		return keyBinding.matches(input, modifiers, atLeast)
	}

	companion object : FirmamentEventBus<HandledScreenKeyReleasedEvent>()
}
