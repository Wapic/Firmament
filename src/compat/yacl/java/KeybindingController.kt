package moe.nea.firmament.compat.yacl

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.ControllerWidget
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.KeyBindingStateManager
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.keybindings.GenericInputButton
import moe.nea.firmament.keybindings.SavedKeyBinding

class KeybindingController(
	val option: Option<SavedKeyBinding>,
	val managedOption: ManagedOption<SavedKeyBinding>,
) : Controller<SavedKeyBinding> {
	val handler = managedOption.handler as KeyBindingHandler
	override fun option(): Option<SavedKeyBinding> {
		return option
	}

	override fun formatValue(): Text {
		return option.pendingValue().format()
	}

	override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>): AbstractWidget {
		lateinit var button: ControllerWidget<KeybindingController>
		val sm = KeyBindingStateManager(
			{ option.pendingValue() },
			{ option.requestSet(it) },
			{ screen.focused = null },
			{ screen.focused = button },
		)
		button = KeybindingWidget(sm, this, screen, widgetDimension)
		option.addListener { t, u ->
			sm.updateLabel()
		}
		sm.updateLabel()
		return button
	}
}

class KeybindingWidget(
	val sm: KeyBindingStateManager,
	controller: KeybindingController,
	screen: YACLScreen,
	dimension: Dimension<Int>
) : ControllerWidget<KeybindingController>(controller, screen, dimension) {
	override fun getHoveredControlWidth(): Int {
		return 130
	}

	override fun getValueText(): Text {
		return sm.label
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return sm.keyboardEvent(GenericInputButton.ofKeyAndScan(keyCode, scanCode), true)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return sm.keyboardEvent(GenericInputButton.ofKeyAndScan(keyCode, scanCode), false)
	}

	override fun unfocus() {
		sm.onLostFocus()
	}

	override fun setFocused(focused: Boolean) {
		super.setFocused(focused)
		if (!focused) sm.onLostFocus()
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (isHovered) {
			sm.onClick(button)
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}
}
