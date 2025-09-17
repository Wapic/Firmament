package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSignEditScreen.class)
public class MoveSignElements {
	@WrapWithCondition(
		method = "renderSign",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractSignEditScreen;renderSignBackground(Lnet/minecraft/client/gui/DrawContext;)V"))
	private boolean onDrawBackgroundSign(AbstractSignEditScreen instance, DrawContext drawContext) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderDirect(drawContext);
		return false;
	}

	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawSelection(IIII)V")}
	)
	private void onRenderSignTextSelection(
		DrawContext instance, int x1, int y1, int x2, int y2, Operation<Void> original,
		@Local(index = 9) int messageIndex) {
		instance.getMatrices().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.getMatrices().translate(override.getX(), override.getY());
		}
		original.call(instance, x1, y1, x2, y2);
		instance.getMatrices().popMatrix();
	}
	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V")}
	)
	private void onRenderSignTextFill(
		DrawContext instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original, @Local(index = 9) int messageIndex) {
		instance.getMatrices().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.getMatrices().translate(override.getX(), override.getY());
		}
		original.call(instance, x1, y1, x2, y2, color);
		instance.getMatrices().popMatrix();
	}

	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V")},
		expect = 2)
	private void onRenderSignTextRendering(DrawContext instance, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow, Operation<Void> original, @Local(index = 9) int messageIndex) {
		instance.getMatrices().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.getMatrices().translate(override.getX(), override.getY());
		}
		original.call(instance, textRenderer, text, x, y, color, shadow);
		instance.getMatrices().popMatrix();
	}

}
