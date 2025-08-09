package moe.nea.firmament.features.texturepack

import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.text.Text
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.util.ErrorUtil.intoCatch

object CustomTextReplacements : SinglePreparationResourceReloader<List<TreeishTextReplacer>>() {

	override fun prepare(
		manager: ResourceManager,
		profiler: Profiler
	): List<TreeishTextReplacer> {
		return manager.findResources("overrides/texts") { it.namespace == "firmskyblock" && it.path.endsWith(".json") }
			.mapNotNull {
				Firmament.tryDecodeJsonFromStream<TreeishTextReplacer>(it.value.inputStream)
					.intoCatch("Failed to load text override from ${it.key}").orNull()
			}
	}

	var textReplacers: List<TreeishTextReplacer> = listOf()

	override fun apply(
		prepared: List<TreeishTextReplacer>,
		manager: ResourceManager,
		profiler: Profiler
	) {
		this.textReplacers = prepared
	}

	@JvmStatic
	fun replaceTexts(texts: List<Text>): List<Text> {
		return texts.map { replaceText(it) }
	}

	@JvmStatic
	fun replaceText(text: Text): Text {
		// TODO: add a config option for this
		val rawText = text.string
		var text = text
		for (replacer in textReplacers) {
			if (!replacer.match.matches(rawText)) continue
			text = replacer.replaceText(text)
		}
		return text
	}

	@Subscribe
	fun onReloadStart(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloader(this)
	}
}
