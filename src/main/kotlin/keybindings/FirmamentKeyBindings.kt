package moe.nea.firmament.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.util.TestUtil

object FirmamentKeyBindings {
	val cat = KeyMapping.Category(Firmament.identifier("category"))
	fun registerKeyBinding(name: String, config: ManagedOption<SavedKeyBinding>) {
		val vanillaKeyBinding = KeyMapping(
			name,
			InputConstants.Type.KEYSYM,
			-1,
			cat
		)
		if (!TestUtil.isInTest) {
			KeyBindingHelper.registerKeyBinding(vanillaKeyBinding)
		}
		keyBindings[vanillaKeyBinding] = config
	}

	val keyBindings = mutableMapOf<KeyMapping, ManagedOption<SavedKeyBinding>>()

}
