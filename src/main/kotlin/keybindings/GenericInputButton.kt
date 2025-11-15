package moe.nea.firmament.keybindings

import org.lwjgl.glfw.GLFW
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.put
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.KeyInput
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.MacWindowUtil
import net.minecraft.text.Text
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.InitLevel

@Serializable(with = GenericInputButton.Serializer::class)
sealed interface GenericInputButton {

	object Serializer : KSerializer<GenericInputButton> {
		override val descriptor: SerialDescriptor
			get() = SerialDescriptor("Firmament:GenericInputButton", JsonElement.serializer().descriptor)

		override fun serialize(
			encoder: Encoder,
			value: GenericInputButton
		) {
			JsonElement.serializer().serialize(
				encoder,
				when (value) {
					is KeyCodeButton -> buildJsonObject { put("keyCode", value.keyCode) }
					is MouseButton -> buildJsonObject { put("mouse", value.mouseButton) }
					is ScanCodeButton -> buildJsonObject { put("scanCode", value.scanCode) }
					Unbound -> JsonNull
				})
		}

		override fun deserialize(decoder: Decoder): GenericInputButton {
			val element = JsonElement.serializer().deserialize(decoder)
			if (element is JsonNull)
				return Unbound
			require(element is JsonObject)
			(element["keyCode"] as? JsonPrimitive)?.let {
				return KeyCodeButton(it.int)
			}
			(element["mouse"] as? JsonPrimitive)?.let {
				return MouseButton(it.int)
			}
			(element["scanCode"] as? JsonPrimitive)?.let {
				return ScanCodeButton(it.int)
			}
			error("Could not parse GenericInputButton: $element")
		}
	}

	companion object {

		fun escape() = ofKeyCode(GLFW.GLFW_KEY_ESCAPE)
		fun ofKeyCode(keyCode: Int): GenericInputButton = KeyCodeButton(keyCode)
		fun ofScanCode(scanCode: Int): GenericInputButton = ScanCodeButton(scanCode)
		fun ofScanCodeFromKeyCode(keyCode: Int): GenericInputButton = ScanCodeButton(GLFW.glfwGetKeyScancode(keyCode))
		fun unbound(): GenericInputButton = Unbound
		fun mouse(mouseButton: Int): GenericInputButton = MouseButton(mouseButton)
		fun ofKeyAndScan(keyCode: Int, scanCode: Int): GenericInputButton {
			if (keyCode == GLFW.GLFW_KEY_UNKNOWN)
				return ofScanCode(scanCode)
			return ofKeyCode(keyCode) // TODO: should i always upgrade to a scanCode?
		}
	}

	data object Unbound : GenericInputButton {
		override fun toInputKey(): InputUtil.Key {
			return InputUtil.UNKNOWN_KEY
		}

		override fun isBound(): Boolean {
			return false
		}

		override fun isPressed(): Boolean {
			return false
		}
	}

	data class MouseButton(
		val mouseButton: Int
	) : GenericInputButton {
		override fun toInputKey(): InputUtil.Key {
			return InputUtil.Type.MOUSE.createFromCode(mouseButton)
		}

		override fun isPressed(): Boolean {
			return GLFW.glfwGetMouseButton(MC.window.handle, mouseButton) == GLFW.GLFW_PRESS
		}
	}

	data class KeyCodeButton(
		val keyCode: Int
	) : GenericInputButton {
		override fun toInputKey(): InputUtil.Key {
			return InputUtil.Type.KEYSYM.createFromCode(keyCode)
		}

		override fun isPressed(): Boolean {
			return InputUtil.isKeyPressed(MC.window, keyCode)
		}

		override fun isCtrl(): Boolean {
			return keyCode in InputModifiers.controlKeys
		}

		override fun isAlt(): Boolean {
			return keyCode in InputModifiers.altKeys
		}

		override fun isShift(): Boolean {
			return keyCode in InputModifiers.shiftKeys
		}

		override fun isSuper(): Boolean {
			return keyCode in InputModifiers.superKeys
		}
	}

	data class ScanCodeButton(
		val scanCode: Int
	) : GenericInputButton {
		override fun toInputKey(): InputUtil.Key {
			return InputUtil.Type.SCANCODE.createFromCode(scanCode)
		}

		override fun isPressed(): Boolean {
			return FirmamentKeyboardState.isScancodeDown(scanCode)
		}
	}

	fun isBound() = true

	fun isModifier() = isCtrl() || isAlt() || isSuper() || isShift()
	fun isCtrl() = false
	fun isAlt() = false
	fun isSuper() = false
	fun isShift() = false

	fun toInputKey(): InputUtil.Key
	fun format(): Text =
		if (InitLevel.isAtLeast(InitLevel.RENDER_INIT)) {
			toInputKey().localizedText
		} else {
			Text.of(toString())
		}

	fun matches(inputAction: GenericInputAction) = inputAction.matches(this)
	fun isPressed(): Boolean
}

sealed interface GenericInputAction {
	fun matches(inputButton: GenericInputButton): Boolean

	data class MouseInput(
		val mouseButton: Int
	) : GenericInputAction {
		override fun matches(inputButton: GenericInputButton): Boolean {
			return inputButton is GenericInputButton.MouseButton && inputButton.mouseButton == mouseButton
		}
	}

	data class KeyboardInput(
		val keyCode: Int,
		val scanCode: Int,
	) : GenericInputAction {
		override fun matches(inputButton: GenericInputButton): Boolean {
			return when (inputButton) {
				is GenericInputButton.KeyCodeButton -> inputButton.keyCode == keyCode
				is GenericInputButton.ScanCodeButton -> inputButton.scanCode == scanCode
				else -> false
			}
		}
	}

	companion object {
		@JvmStatic
		fun mouse(mouseButton: Int): GenericInputAction = MouseInput(mouseButton)

		@JvmStatic
		fun of(input: KeyInput): GenericInputAction = key(input.keycode, input.scancode)

		@JvmStatic
		fun key(keyCode: Int, scanCode: Int): GenericInputAction = KeyboardInput(keyCode, scanCode)
	}
}

@Serializable
data class InputModifiers(
	val modifiers: Int
) {
	companion object {
		@JvmStatic
		fun current(): InputModifiers {
			val h = MC.window
			val ctrl = if (MacWindowUtil.IS_MAC) {
				InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_SUPER)
					|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_SUPER)
			} else InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_CONTROL)
				|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_CONTROL)
			val shift = InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(
				h,
				GLFW.GLFW_KEY_RIGHT_SHIFT
			)
			val alt = InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_ALT)
				|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_ALT)
			val `super` = InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_SUPER)
				|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_SUPER)
			return of(
				ctrl = ctrl,
				shift = shift,
				alt = alt,
				`super` = `super`,
			)
		}


		val superKeys = listOf(GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)
		val controlKeys = if (MacWindowUtil.IS_MAC) {
			listOf(GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)
		} else {
			listOf(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)
		}
		val shiftKeys = listOf(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT)
		val altKeys = listOf(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT)

		fun of(
			vararg useNamedArgs: Boolean,
			ctrl: Boolean = false,
			shift: Boolean = false,
			alt: Boolean = false,
			`super`: Boolean = false
		): InputModifiers {
			require(useNamedArgs.isEmpty())
			return InputModifiers(
				(if (ctrl) GLFW.GLFW_MOD_CONTROL else 0)
					or (if (shift) GLFW.GLFW_MOD_SHIFT else 0)
					or (if (alt) GLFW.GLFW_MOD_ALT else 0)
					or (if (`super`) GLFW.GLFW_MOD_SUPER else 0)
			)
		}

		fun ofKeyCodes(vararg keys: Int): InputModifiers {
			var mods = 0
			for (key in keys) {
				if (key in superKeys)
					mods = mods or GLFW.GLFW_MOD_SUPER
				if (key in controlKeys)
					mods = mods or GLFW.GLFW_MOD_CONTROL
				if (key in altKeys)
					mods = mods or GLFW.GLFW_MOD_ALT
				if (key in shiftKeys)
					mods = mods or GLFW.GLFW_MOD_SHIFT
			}
			return of(mods)
		}

		@JvmStatic
		fun of(modifiers: Int) = InputModifiers(modifiers)

		@JvmStatic
		fun of(input: KeyInput) = InputModifiers(input.modifiers)

		fun none(): InputModifiers {
			return InputModifiers(0)
		}

		fun ofKey(button: GenericInputButton): InputModifiers {
			return when (button) {
				is GenericInputButton.KeyCodeButton -> ofKeyCodes(button.keyCode)
				else -> none()
			}
		}
	}

	fun isAtLeast(other: InputModifiers): Boolean {
		return this.modifiers and other.modifiers == this.modifiers
	}

	fun without(other: InputModifiers): InputModifiers {
		return InputModifiers(this.modifiers and other.modifiers.inv())
	}

	fun isEmpty() = modifiers == 0

	fun getFlag(flag: Int) = modifiers and flag != 0
	val ctrl get() = getFlag(GLFW.GLFW_MOD_CONTROL) // TODO: consult someone on control vs command again
	val shift get() = getFlag(GLFW.GLFW_MOD_SHIFT)
	val alt get() = getFlag(GLFW.GLFW_MOD_ALT)
	val `super` get() = getFlag(GLFW.GLFW_MOD_SUPER)

	override fun toString(): String {
		return listOfNotNull(
			if (ctrl) "CTRL" else null,
			if (shift) "SHIFT" else null,
			if (alt) "ALT" else null,
			if (`super`) "SUPER" else null,
		).joinToString(" + ")
	}

	fun matches(other: InputModifiers, atLeast: Boolean): Boolean {
		if (atLeast)
			return isAtLeast(other)
		return this == other
	}

	fun format(): Text { // TODO: translation for mods
		return Text.of(toString())
	}

}
