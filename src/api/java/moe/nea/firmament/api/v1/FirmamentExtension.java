package moe.nea.firmament.api.v1;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collection;
import java.util.List;

public interface FirmamentExtension {

	/**
	 * This method gets called during client initialization, if firmament is installed. Can be used as an alternative to
	 * checking {@code FabricLoader.getInstance().isModLoaded("firmament")}.
	 */
	default void onLoad() {}

	/**
	 * @param screen the current active screen
	 * @return whether inventory buttons should be hidden on the current screen.
	 */
	default boolean shouldHideInventoryButtons(Screen screen) {
		return false;
	}

	/**
	 * @param screen the current active screen
	 * @return a list of zones which contain content rendered by other mods, which should therefore hide the items in those areas
	 */
	default Collection<? extends ScreenRectangle> getExclusionZones(Screen screen) {
		return List.of();
	}
}
