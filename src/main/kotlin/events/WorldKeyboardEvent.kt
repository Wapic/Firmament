package moe.nea.firmament.events

import moe.nea.firmament.keybindings.GenericInputAction
import moe.nea.firmament.keybindings.InputModifiers
import moe.nea.firmament.keybindings.SavedKeyBinding

data class WorldKeyboardEvent(val keyCode: Int, val scanCode: Int, val modifiers: Int) : FirmamentEvent.Cancellable() {
	fun matches(keyBinding: SavedKeyBinding, atLeast: Boolean = false): Boolean {
		return keyBinding.matches(intoAction(), InputModifiers(modifiers), atLeast)
	}

	fun intoAction() = GenericInputAction.key(keyCode, scanCode)


	companion object : FirmamentEventBus<WorldKeyboardEvent>()
}
