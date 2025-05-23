package moe.nea.firmament.compat.jade

import net.fabricmc.loader.api.FabricLoader
import moe.nea.firmament.util.compatloader.CompatMeta
import moe.nea.firmament.util.compatloader.ICompatMeta

@CompatMeta
object Compat : ICompatMeta {
	override fun shouldLoad(): Boolean {
		return FabricLoader.getInstance().isModLoaded("jade")
	}
}
