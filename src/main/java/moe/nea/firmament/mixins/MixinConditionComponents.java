package moe.nea.firmament.mixins;

// People are complaining but this really is not my place to fix things

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.util.ConditionNBTMixin;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.defaults.cit.conditions.ConditionComponents;
import shcm.shsupercm.fabric.citresewn.defaults.cit.conditions.ConditionNBT;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyGroup;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyKey;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyValue;

@Mixin(ConditionComponents.class)
@Pseudo
public class MixinConditionComponents {
    @Shadow
    private ComponentType<?> componentType;

    @Shadow(remap = false)
    private ConditionNBT fallbackNBTCheck;
    @Unique
    private String[] pathCheck;
    @Unique
    private int loreInt = -1;

    @Inject(method = "load",
        at = @At(value = "INVOKE", remap = false, target = "Lshcm/shsupercm/fabric/citresewn/defaults/cit/conditions/ConditionNBT;loadNbtCondition(Lshcm/shsupercm/fabric/citresewn/pack/format/PropertyValue;Lshcm/shsupercm/fabric/citresewn/pack/format/PropertyGroup;[Ljava/lang/String;Ljava/lang/String;)V"),
        remap = false)
    private void onLoadSavePath(PropertyKey key, PropertyValue value, PropertyGroup properties, CallbackInfo ci,
                                @Local String[] path) {
        this.pathCheck = path;
        this.loreInt = -1;
    }

    private boolean matchStringDirect(String directString, CITContext context) {
        return ConditionNBTMixin.invokeDirectConditionNBTStringMatch(fallbackNBTCheck, directString);
    }

    @Inject(method = "test", at = @At("HEAD"), cancellable = true, remap = false)
    void fastPathDisplayName(CITContext context, CallbackInfoReturnable<Boolean> cir) {
        if (this.componentType == DataComponentTypes.CUSTOM_NAME && pathCheck.length == 0) {
            var displayName = context.stack.getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (displayName != null) {
                cir.setReturnValue(matchStringDirect((displayName.getString()), context));
            }
        }
        if (this.componentType == DataComponentTypes.LORE && pathCheck.length == 1) {
            var lore = context.stack.getComponents().get(DataComponentTypes.LORE);
            if (lore != null) {
                var loreLines = lore.lines();
                if (pathCheck[0].equals("*")) {
                    for (var loreLine : loreLines) {
                        if (matchStringDirect((loreLine.getString()), context)) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                    cir.setReturnValue(false);
                } else {
                    if (loreInt < 0)
                        loreInt = Integer.parseInt(pathCheck[0]);
                    cir.setReturnValue(0 <= loreInt && loreInt < loreLines.size() &&
                                           matchStringDirect((loreLines.get(loreInt).getString()), context));
                }
            }
        }
    }


}
