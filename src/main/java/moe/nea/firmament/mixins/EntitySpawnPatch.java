
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.EntitySpawnEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class EntitySpawnPatch {
    @Inject(method = "addEntity", at = @At(value = "TAIL"))
    private void addEntity(Entity entity, CallbackInfo ci) {
        EntitySpawnEvent.Companion.publish(new EntitySpawnEvent(entity));
    }
}
