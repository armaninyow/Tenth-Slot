package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import com.armaninyow.tenthslot.TenthSlotConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into {@link MouseHandler#onScroll} to extend hotbar scrolling to
 * include the tenth slot (index 9).
 *
 * Vanilla computes the next slot internally (clamped to 0-8) and calls
 * setSelectedHotbarSlot before we can intercept the total-slots argument
 * without a refMap. Instead we inject at TAIL of onScroll and correct the
 * result:
 *
 *  - If vanilla just wrapped 8→0 (scrolled forward past last slot) and the
 *    tenth slot is visible, we set selected=9 instead.
 *  - If vanilla just wrapped 0→8 (scrolled back past first slot) and the
 *    tenth slot is visible, we set selected=9 instead.
 *  - If selected==9 before the scroll (HEAD), we handle it ourselves and
 *    cancel vanilla entirely — since vanilla doesn't know about slot 9 it
 *    would do nothing useful.
 */
// 1.21.2_1.21.3
@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class ScrollMixin {

	// Captured at HEAD so the TAIL inject knows the direction and prior slot.
	@Unique private double tenthslot$lastScrollY = 0.0;
	@Unique private int tenthslot$slotBeforeScroll = -1;

	@Inject(at = @At("HEAD"), method = "onScroll(JDD)V", cancellable = true)
	private void onScrollHead(long window, double x, double y, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		Inventory inv = client.player.getInventory();
		tenthslot$lastScrollY = y;
		tenthslot$slotBeforeScroll = inv.selected;

		// If we're on slot 9, vanilla won't do anything meaningful.
		// Handle scrolling away from slot 9 ourselves.
		if (inv.selected == TenthSlot.TENTH_SLOT_INDEX) {
			if (!isTenthSlotVisible(inv)) {
				inv.selected = 0;
				return;
			}
			// Determine direction using discreteMouseScroll sign convention:
			// positive y = scroll up = go to lower slot index (toward slot 1)
			// negative y = scroll down = go to higher slot index (toward slot 9)
			double sensitivity = client.options.mouseWheelSensitivity().get();
			double delta = y * sensitivity;
			if (delta > 0.0) {
				// Scrolling toward slot 1 — go to slot 8
				inv.selected = 8;
			} else if (delta < 0.0) {
				// Scrolling toward slot 9 — wrap around to slot 0
				inv.selected = 0;
			}
			ci.cancel();
		}
	}

	@Inject(at = @At("TAIL"), method = "onScroll(JDD)V")
	private void onScrollTail(long window, double x, double y, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		Inventory inv = client.player.getInventory();
		if (!isTenthSlotVisible(inv)) return;

		int prev = tenthslot$slotBeforeScroll;
		int curr = inv.selected;
		double sensitivity = client.options.mouseWheelSensitivity().get();
		double delta = tenthslot$lastScrollY * sensitivity;

		// Detect vanilla wrap-around: scrolled forward from slot 8 → landed on 0
		if (delta < 0.0 && prev == 8 && curr == 0) {
			inv.selected = TenthSlot.TENTH_SLOT_INDEX;
		}
		// Detect vanilla wrap-around: scrolled back from slot 0 → landed on 8
		else if (delta > 0.0 && prev == 0 && curr == 8) {
			inv.selected = TenthSlot.TENTH_SLOT_INDEX;
		}
	}

	private boolean isTenthSlotVisible(Inventory inventory) {
		if (!TenthSlotConfig.get().showOnlyWhenHotbarFull) return true;
		for (int i = 0; i < 9; i++) {
			if (inventory.items.get(i).isEmpty()) return false;
		}
		return true;
	}
}