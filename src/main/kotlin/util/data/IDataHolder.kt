package moe.nea.firmament.util.data

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.storage.ConfigStorageClass
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader
import moe.nea.firmament.util.SBData

sealed class IDataHolder<T> {
	fun markDirty() {
		FirmamentConfigLoader.markDirty(this)
	}

	init {
		require(this.javaClass.getAnnotation(Config::class.java) != null)
	}

	abstract fun keys(): Collection<T>
	abstract fun saveTo(key: T): JsonObject
	abstract fun loadFrom(key: T, jsonObject: JsonObject)
	abstract fun clear()
	abstract val storageClass: ConfigStorageClass
}

open class ProfileKeyedConfig<T>(
	val prefix: String,
	val serializer: KSerializer<T>,
	val default: () -> T,
) : IDataHolder<UUID>() {

	override val storageClass: ConfigStorageClass
		get() = ConfigStorageClass.PROFILE
	private var _data: MutableMap<UUID, T>? = null

	val data
		get() = _data!!.let { map ->
			map[SBData.profileIdOrNil]
				?: default().also { map[SBData.profileIdOrNil] = it }
		} ?: error("Config $this not loaded — forgot to register?")

	override fun keys(): Collection<UUID> {
		return _data!!.keys
	}

	override fun saveTo(key: UUID): JsonObject {
		val d = _data!!
		return buildJsonObject {
			put(prefix, Firmament.json.encodeToJsonElement(serializer, d[key] ?: return@buildJsonObject))
		}
	}

	override fun loadFrom(key: UUID, jsonObject: JsonObject) {
		(_data ?: mutableMapOf<UUID, T>().also { _data = it })[key] =
			jsonObject[prefix]
				?.let {
					Firmament.json.decodeFromJsonElement(serializer, it)
				} ?: default()
	}

	override fun clear() {
		_data = null
	}
}

abstract class GenericConfig<T>(
	val prefix: String,
	val serializer: KSerializer<T>,
	val default: () -> T,
) : IDataHolder<Unit>() {

	private var _data: T? = null

	val data get() = _data ?: error("Config $this not loaded — forgot to register?")

	override fun keys(): Collection<Unit> {
		return listOf(Unit)
	}

	open fun onLoad() {
	}

	override fun saveTo(key: Unit): JsonObject {
		return buildJsonObject {
			put(prefix, Firmament.json.encodeToJsonElement(serializer, data))
		}
	}

	override fun loadFrom(key: Unit, jsonObject: JsonObject) {
		_data = jsonObject[prefix]?.let { Firmament.json.decodeFromJsonElement(serializer, it) } ?: default()
		onLoad()
	}

	override fun clear() {
		_data = null
	}
}
