package moe.nea.firmament.features.texturepack

import java.lang.StringBuilder
import java.util.regex.Matcher
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.json.KJsonOps
import moe.nea.firmament.util.transformEachRecursively

@Serializable
data class TreeishTextReplacer(
	val match: StringMatcher,
	val replacements: List<SubPartReplacement>
) {
	@Serializable
	data class SubPartReplacement(
		val match: StringMatcher,
		val replace: @Serializable(TextSerializer::class) Text,
	)

	object TextSerializer : KSerializer<Text> {
		override val descriptor: SerialDescriptor
			get() = JsonElement.serializer().descriptor

		override fun serialize(encoder: Encoder, value: Text) {
			encoder.encodeSerializableValue(
				JsonElement.serializer(),
				TextCodecs.CODEC.encodeStart(KJsonOps.INSTANCE, value).orThrow
			)
		}

		override fun deserialize(decoder: Decoder): Text {
			return TextCodecs.CODEC.decode(KJsonOps.INSTANCE, decoder.decodeSerializableValue(JsonElement.serializer()))
				.orThrow.first
		}
	}

	companion object {
		val pattern = "(?!<\\$([$]{2})*)[$]\\{(?<name>[^}])\\}".toPattern()
		fun injectMatchResults(text: Text, matches: Matcher): Text {
			return text.transformEachRecursively { it ->
				val content = it.directLiteralStringContent ?: return@transformEachRecursively it
				val matcher = pattern.matcher(content)
				val builder = StringBuilder()
				while (matcher.find()) {
					matcher.appendReplacement(builder, matches.group(matcher.group("name")).toString())
				}
				matcher.appendTail(builder)
				Text.literal(builder.toString()).setStyle(it.style)
			}
		}
	}

	fun match(text: Text): Boolean {
		return match.matches(text)
	}

	fun replaceText(text: Text): Text {
		return text.transformEachRecursively { part ->
			var part: Text = part
			for (replacement in replacements) {
				val rawPartText = part.string
				val matcher = replacement.match.asRegex.matcher(rawPartText)
				if (!matcher.find()) continue
				val p = Text.literal("")
				p.setStyle(part.style)
				var lastAppendPosition = 0
				do {
					p.append(rawPartText.substring(lastAppendPosition, matcher.start()))
					lastAppendPosition = matcher.end()
					p.append(injectMatchResults(replacement.replace, matcher))
				} while (matcher.find())
				p.append(rawPartText.substring(lastAppendPosition))
				part = p
			}
			part
		}
	}

}
