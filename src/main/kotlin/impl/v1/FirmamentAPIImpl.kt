package moe.nea.firmament.impl.v1

import com.mojang.blaze3d.platform.InputConstants
import java.util.Collections
import java.util.Optional
import net.fabricmc.loader.api.FabricLoader
import kotlin.jvm.optionals.getOrNull
import moe.nea.firmament.Firmament
import moe.nea.firmament.api.v1.FirmamentAPI
import moe.nea.firmament.api.v1.FirmamentExtension
import moe.nea.firmament.api.v1.FirmamentItemWidget
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.intoOptional

object FirmamentAPIImpl : FirmamentAPI() {
	@JvmField
	val INSTANCE: FirmamentAPI = FirmamentAPIImpl

	private val _extensions = mutableListOf<FirmamentExtension>()
	override fun getExtensions(): List<FirmamentExtension> {
		return Collections.unmodifiableList(_extensions)
	}

	override fun getHoveredItemWidget(): Optional<FirmamentItemWidget> {
		val mouse = MC.instance.mouseHandler
		val window = MC.window
		val xpos = mouse.getScaledXPos(window)
		val ypos = mouse.getScaledYPos(window)
		val widget = MC.screen
			?.getChildAt(xpos, ypos)
			?.getOrNull()
		if (widget is FirmamentItemWidget) return widget.intoOptional()
		return Optional.empty()
	}

	fun loadExtensions() {
		for (container in FabricLoader.getInstance()
			.getEntrypointContainers("firmament:v1", FirmamentExtension::class.java)) {
			Firmament.logger.info("Loading extension ${container.entrypoint} from ${container.provider.metadata.name}")
			loadExtension(container.entrypoint)
		}
		extensions.forEach { it.onLoad() }
	}

	fun loadExtension(entrypoint: FirmamentExtension) {
		_extensions.add(entrypoint)
	}
}
