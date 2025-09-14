

package moe.nea.firmament.util.accessors

import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.screen.ingame.HandledScreen
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen

fun HandledScreen<*>.getRectangle(): Rectangle {
    this as AccessorHandledScreen
    return Rectangle(
        getX_Firmament(),
        getY_Firmament(),
        getBackgroundWidth_Firmament(),
        getBackgroundHeight_Firmament()
    )
}
