package com.armaninyow.tenthslot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

// 26.1.x
@Environment(EnvType.CLIENT)
public class TenthSlotClient implements ClientModInitializer {

	public static KeyMapping KEY_TENTH_SLOT;

	@Override
	public void onInitializeClient() {
		TenthSlotConfig.get();

		KEY_TENTH_SLOT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.tenthslot.tenth_slot",
			GLFW.GLFW_KEY_0,
			KeyMapping.Category.INVENTORY
		));

		LOGGER.info("Tenth Slot client initialized.");
	}

	private static final org.slf4j.Logger LOGGER =
		org.slf4j.LoggerFactory.getLogger(TenthSlot.MOD_ID);
}