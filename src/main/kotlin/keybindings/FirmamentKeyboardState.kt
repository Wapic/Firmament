package moe.nea.firmament.keybindings

import java.util.BitSet
import org.lwjgl.glfw.GLFW

object FirmamentKeyboardState {

	private val pressedScancodes = BitSet()

	@Synchronized
	fun isScancodeDown(scancode: Int): Boolean {
		// TODO: maintain a record of keycodes that were pressed for this scanCode to check if they are still held
		return pressedScancodes.get(scancode)
	}

	@Synchronized
	fun maintainState(key: Int, scancode: Int, action: Int, modifiers: Int) {
		when (action) {
			GLFW.GLFW_PRESS -> pressedScancodes.set(scancode)
			GLFW.GLFW_RELEASE -> pressedScancodes.clear(scancode)
		}
	}
}
