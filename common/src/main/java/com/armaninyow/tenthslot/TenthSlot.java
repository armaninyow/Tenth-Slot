package com.armaninyow.tenthslot;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenthSlot implements ModInitializer {
	public static final String MOD_ID = "tenthslot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// The index of the tenth slot (0-based, so index 9).
	public static final int TENTH_SLOT_INDEX = 9;

	@Override
	public void onInitialize() {
		LOGGER.info("Tenth Slot mod initialized.");
	}
}