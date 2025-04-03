package moe.nea.firmament.events

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

data class BlockChangeEvent(
	val blockPos: BlockPos,
	val newBlockState: BlockState,
	val oldBlockState: BlockState
) : FirmamentEvent(){
	companion object : FirmamentEventBus<BlockChangeEvent>()
}
