package moe.nea.firmament.features.inventory.storageoverlay

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.async
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtSizeTracker
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.inventory.storageoverlay.VirtualInventory.Serializer.writeToByteArray
import moe.nea.firmament.util.Base64Util
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.TolerantRegistriesOps

@Serializable(with = VirtualInventory.Serializer::class)
data class VirtualInventory(
	val stacks: List<ItemStack>
) {
	val rows = stacks.size / 9

	val serializationCache = CompletableFuture.supplyAsync {
		writeToByteArray(this)
	}

	init {
		assert(stacks.size % 9 == 0)
		assert(stacks.size / 9 in 1..5)
	}


	object Serializer : KSerializer<VirtualInventory> {
		fun writeToByteArray(value: VirtualInventory): ByteArray {
			val list = NbtList()
			val ops = getOps()
			value.stacks.forEach {
				if (it.isEmpty) list.add(NbtCompound())
				else list.add(ErrorUtil.catch("Could not serialize item") {
					ItemStack.CODEC.encode(
						it,
						ops,
						NbtCompound()
					).orThrow
				}
					.or { NbtCompound() })
			}
			val baos = ByteArrayOutputStream()
			NbtIo.writeCompressed(NbtCompound().also { it.put(INVENTORY, list) }, baos)
			return baos.toByteArray()
		}

		const val INVENTORY = "INVENTORY"
		override val descriptor: SerialDescriptor
			get() = PrimitiveSerialDescriptor("VirtualInventory", PrimitiveKind.STRING)

		override fun deserialize(decoder: Decoder): VirtualInventory {
			val s = decoder.decodeString()
			val n = NbtIo.readCompressed(ByteArrayInputStream(Base64Util.decodeBytes(s)), NbtSizeTracker.of(100_000_000))
			val items = n.getList(INVENTORY).getOrNull()
			val ops = getOps()
			return VirtualInventory(items?.map {
				it as NbtCompound
				if (it.isEmpty) ItemStack.EMPTY
				else ErrorUtil.catch("Could not deserialize item") {
					ItemStack.CODEC.parse(ops, it).orThrow
				}.or { ItemStack.EMPTY }
			} ?: listOf())
		}

		fun getOps() = MC.currentOrDefaultRegistryNbtOps

		override fun serialize(encoder: Encoder, value: VirtualInventory) {
			encoder.encodeString(Base64Util.encodeToString(value.serializationCache.get()))
		}
	}
}
