

package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text

abstract class FragmentGuiScreen(
    val dismissOnOutOfBounds: Boolean = true
) : Screen(Text.literal("")) {
    var popup: MoulConfigFragment? = null

    fun createPopup(context: GuiContext, position: Point) {
        popup = MoulConfigFragment(context, position) { popup = null }
    }

	fun renderPopup(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		popup?.render(context, mouseX, mouseY, delta)
	}

    private inline fun ifPopup(ifYes: (MoulConfigFragment) -> Unit): Boolean {
        val p = popup ?: return false
        ifYes(p)
        return true
    }

	override fun keyPressed(input: KeyInput): Boolean {
        return ifPopup {
            it.keyPressed(input)
        }
    }

	override fun keyReleased(input: KeyInput): Boolean {
        return ifPopup {
            it.keyReleased(input)
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        ifPopup { it.mouseMoved(mouseX, mouseY) }
    }

	override fun mouseReleased(click: Click): Boolean {
        return ifPopup {
            it.mouseReleased(click)
        }
    }

	override fun mouseDragged(click: Click, offsetX: Double, offsetY: Double): Boolean {
        return ifPopup {
            it.mouseDragged(click, offsetX, offsetY)
        }
    }

	override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        return ifPopup {
            if (!Rectangle(
                    it.position,
                    Dimension(it.guiContext.root.width, it.guiContext.root.height)
                ).contains(Point(click.x, click.y))
                && dismissOnOutOfBounds
            ) {
                popup = null
            } else {
                it.mouseClicked(click, doubled)
            }
        }|| super.mouseClicked(click, doubled)
    }

	override fun charTyped(input: CharInput): Boolean {
        return ifPopup { it.charTyped(input) }
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        return ifPopup {
            it.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }
    }
}
