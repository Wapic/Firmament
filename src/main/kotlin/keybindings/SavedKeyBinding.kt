package moe.nea.firmament.keybindings

import kotlinx.serialization.Serializable
import net.minecraft.text.Text

@Serializable
data class SavedKeyBinding(
	val button: GenericInputButton,
	val modifiers: InputModifiers,
) {
	companion object {
		fun isShiftDown() = InputModifiers.getCurrentModifiers().shift

		fun unbound(): SavedKeyBinding = withoutMods(GenericInputButton.unbound())
		fun withoutMods(input: GenericInputButton) = SavedKeyBinding(input, InputModifiers.none())
		fun keyWithoutMods(keyCode: Int): SavedKeyBinding = withoutMods(GenericInputButton.ofKeyCode(keyCode))
		fun keyWithMods(keyCode: Int, mods: InputModifiers) =
			SavedKeyBinding(GenericInputButton.ofKeyCode(keyCode), mods)
	}

	fun isPressed(atLeast: Boolean = false): Boolean {
		if (!button.isPressed())
			return false
		val mods = InputModifiers.getCurrentModifiers()
		return mods.matches(this.modifiers, atLeast)
	}

	override fun toString(): String {
		return format().string
	}

	fun format(): Text {
		val stroke = Text.empty()
		if (!modifiers.isEmpty()) {
			stroke.append(modifiers.format())
			stroke.append(" + ")
		}
		stroke.append(button.format())
		return stroke
	}

	val isBound: Boolean get() = button.isBound()
	fun matches(action: GenericInputAction, inputModifiers: InputModifiers, atLeast: Boolean = false): Boolean {
		return action.matches(button) && this.modifiers.matches(inputModifiers, atLeast)
	}

}

