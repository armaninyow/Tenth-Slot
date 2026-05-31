package com.armaninyow.tenthslot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

// 1.21.11
@Environment(EnvType.CLIENT)
public class TenthSlotClient implements ClientModInitializer {

	public static KeyMapping KEY_TENTH_SLOT;

	@Override
	public void onInitializeClient() {
		TenthSlotConfig.register();

		KEY_TENTH_SLOT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.tenthslot.tenth_slot",
			GLFW.GLFW_KEY_0,
			KeyMapping.Category.INVENTORY
		));

		LOGGER.info("Tenth Slot client initialized.");
	}

	private static final org.slf4j.Logger LOGGER =
		org.slf4j.LoggerFactory.getLogger(TenthSlot.MOD_ID);
}