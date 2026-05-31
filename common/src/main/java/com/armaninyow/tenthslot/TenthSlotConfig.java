package com.armaninyow.tenthslot;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = TenthSlot.MOD_ID)
public class TenthSlotConfig implements ConfigData {

	// When true, the tenth slot is only shown when all 9 hotbar slots are occupied.
	// Default is false — the tenth slot is always visible.
	@ConfigEntry.Gui.Tooltip
	public boolean showOnlyWhenHotbarFull = false;

	// When true, offhand actions (placing blocks, eating, using items, etc.)
	// work normally while the tenth slot is selected.
	// When false, only non-item interactions work (opening doors, mounting, trading).
	// Default is true — matches vanilla offhand behavior as per spec.
	@ConfigEntry.Gui.Tooltip
	public boolean vanillaOffhandActions = true;

	// When true, non-item right-click interactions (opening doors, mounting animals,
	// trading with villagers, opening block GUIs, etc.) work normally while the
	// tenth slot is selected.
	// When false, these interactions are blocked.
	// Default is true.
	@ConfigEntry.Gui.Tooltip
	public boolean vanillaNonItemRightClick = true;

	// When true, left-click interactions (attacking entities, breaking blocks)
	// work normally while the tenth slot is selected.
	// When false, these interactions are blocked.
	// Default is true.
	@ConfigEntry.Gui.Tooltip
	public boolean vanillaLeftClick = true;

	public static TenthSlotConfig get() {
		return AutoConfig.getConfigHolder(TenthSlotConfig.class).getConfig();
	}

	public static void register() {
		AutoConfig.register(TenthSlotConfig.class, GsonConfigSerializer::new);
	}
}