package moe.nea.firmament.features.texturepack

import java.util.regex.Matcher
import util.json.CodecSerializer
import kotlinx.serialization.Serializable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.transformEachRecursively

@Serializable
data class TreeishTextReplacer(
	val match: StringMatcher,
	val replacements: List<SubPartReplacement>
) {
	@Serializable
	data class SubPartReplacement(
		val match: StringMatcher,
		val style: @Serializable(StyleSerializer::class) Style?,
		val replace: @Serializable(TextSerializer::class) Text,
	)

	object TextSerializer : CodecSerializer<Text>(TextCodecs.CODEC)
	object StyleSerializer : CodecSerializer<Style>(Style.Codecs.CODEC)
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
				replacement.style?.let { expectedStyle ->
					val parentStyle = part.style
					val parented = expectedStyle.withParent(parentStyle)
					if (parented.isStrikethrough != parentStyle.isStrikethrough
						|| parented.isObfuscated != parentStyle.isObfuscated
						|| parented.isBold != parentStyle.isBold
						|| parented.isUnderlined != parentStyle.isUnderlined
						|| parented.isItalic != parentStyle.isItalic
						|| parented.color?.rgb != parentStyle.color?.rgb)
						continue
				}
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
