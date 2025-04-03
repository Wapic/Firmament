package moe.nea.firmament.events

import net.minecraft.entity.Entity

data class EntitySpawnEvent(
	val entity: Entity?,
) : FirmamentEvent() {
	companion object: FirmamentEventBus<EntitySpawnEvent>()
}
