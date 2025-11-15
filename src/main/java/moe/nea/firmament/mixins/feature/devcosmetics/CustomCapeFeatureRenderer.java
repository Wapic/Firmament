package moe.nea.firmament.mixins.feature.devcosmetics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.Unit;
import moe.nea.firmament.features.misc.CustomCapes;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeFeatureRenderer.class)
public abstract class CustomCapeFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	public CustomCapeFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
		super(context);
	}

	@WrapOperation(
		method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V")
	)
	private void onRender(OrderedRenderCommandQueue instance, Model model, Object o, MatrixStack matrixStack, RenderLayer renderLayer, int light, int overlay, int outlineColor, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand, Operation<Void> original,
						  @Local(argsOnly = true) PlayerEntityRenderState playerEntityRenderState, @Local SkinTextures skinTextures) {
		// TODO: 1.21.10 custom capes by pre rendering the texture id. this is more viable on this version i am fairly sure, without clogging up all of the cached image render layers
//		CustomCapes.render(
//			playerEntityRenderState,
//			vertexConsumer,
//			RenderLayer.getEntitySolid(skinTextures.cape().id()),
//			vertexConsumerProvider,
//			matrixStack,
//			updatedConsumer -> {
//				original.call(instance, matrixStack, updatedConsumer, light, overlay, outlineColor);
//				return Unit.INSTANCE;
//			});
	}
}
