package moe.nea.firmament.util.data

import java.nio.file.Path
import kotlinx.serialization.KSerializer
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.storage.ConfigStorageClass

abstract class DataHolder<T>(
	serializer: KSerializer<T>,
	name: String,
	default: () -> T
) : GenericConfig<T>(name, serializer, default) {
	override val storageClass: ConfigStorageClass
		get() = ConfigStorageClass.STORAGE
}
