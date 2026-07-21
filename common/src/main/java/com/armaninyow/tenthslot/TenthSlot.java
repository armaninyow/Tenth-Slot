package com.armaninyow.tenthslot;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenthSlot implements ModInitializer {
	public static final String MOD_ID = "tenthslot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final int TENTH_SLOT_INDEX = 9;

	@Override
	public void onInitialize() {
		LOGGER.info("Tenth Slot mod initialized.");
	}
}