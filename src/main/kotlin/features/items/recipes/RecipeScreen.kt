package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.screens.Screen
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.tr

class RecipeScreen(
	val recipes: RenderableRecipe<*>,
) : Screen(tr("firmament.recipe.screen", "SkyBlock Recipe")) {

	lateinit var layoutedRecipe: StandaloneRecipeRenderer
	override fun init() {// TODO: wrap all of this in a scroll layout
		super.init()
		val bounds = Rectangle(
			width / 2 - recipes.renderer.displayWidth / 2,
			height / 2 - recipes.renderer.displayHeight / 2,
			recipes.renderer.displayWidth,
			recipes.renderer.displayHeight
		)
		layoutedRecipe = recipes.render(bounds)
		layoutedRecipe.widgets.forEach(this::addRenderableWidget)
	}

	override fun tick() {
		super.tick()
		layoutedRecipe.tick()
	}
}
