package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.texturepack.CustomTextReplacements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.stream.Stream;

@Mixin(DrawContext.class)
public class ReplaceTextsInDrawContext {
	// I HATE THIS SO MUCH WHY CANT I JUST OPERATE ON ORDEREDTEXTS!!!
	// JUNE I WILL RIP ALL OF THIS OUT AND MAKE YOU REWRITE EVERYTHING
	// TODO: be in a mood to rewrite this

	@ModifyVariable(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V", at = @At("HEAD"), argsOnly = true)
	private Text replaceTextInDrawText(Text text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyVariable(method = "drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", at = @At("HEAD"), argsOnly = true)
	private Text replaceTextInDrawCenteredTextWithShadow(Text text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyVariable(method = "drawWrappedText", at = @At("HEAD"), argsOnly = true)
	private StringVisitable replaceTextInDrawWrappedText(StringVisitable stringVisitable) {
		return stringVisitable instanceof Text text ? CustomTextReplacements.replaceText(text) : stringVisitable;
	}

	@ModifyExpressionValue(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
	private Stream<Text> replaceTextInDrawTooltipListText(Stream<Text> original) {
		return original.map(CustomTextReplacements::replaceText);
	}

	@ModifyExpressionValue(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
	private Stream<Text> replaceTextInDrawTooltipListTextWithOptional(Stream<Text> original) {
		return original.map(CustomTextReplacements::replaceText);
	}

	@ModifyVariable(method = "drawTooltip(Lnet/minecraft/text/Text;II)V", at = @At("HEAD"), argsOnly = true)
	private Text replaceTextInDrawTooltipSingle(Text text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyExpressionValue(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/HoverEvent$ShowText;value()Lnet/minecraft/text/Text;"))
	private Text replaceShowTextInHover(Text text) {
		return CustomTextReplacements.replaceText(text);
	}

}
