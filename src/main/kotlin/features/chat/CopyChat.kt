package moe.nea.firmament.features.chat

import net.minecraft.text.OrderedText
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.reconstitute


object CopyChat : FirmamentFeature {
	override val identifier: String
		get() = "copy-chat"

	@Config
	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val copyChat by toggle("copy-chat") { false }
	}

	@Subscribe
	fun onInit(event: ClientStartedEvent) {
	}

	override val config: ManagedConfig?
		get() = TConfig

	fun orderedTextToString(orderedText: OrderedText): String {
		return orderedText.reconstitute().string
	}


}
