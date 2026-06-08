package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import com.armaninyow.tenthslot.TenthSlotConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Patches {@link Inventory#swapPaint} which is the method called by the
 * client when the player scrolls the hotbar. Vanilla clamps the result to
 * 0–8. We extend it to 0–9 and keep the wrap-around behaviour correct.
 *
 * When the tenth slot is hidden by config (showOnlyWhenHotbarFull is enabled
 * and the hotbar is not full), we skip slot 9 entirely and behave as if only
 * 9 slots exist — matching vanilla wrap-around exactly.
 */
// 1.21_1.21.1
@Environment(EnvType.CLIENT)
@Mixin(Inventory.class)
public class ScrollMixin {

	@Inject(
		at = @At("HEAD"),
		method = "swapPaint(D)V",
		cancellable = true
	)
	private void onSwapPaint(double direction, CallbackInfo ci) {
		Inventory inventory = (Inventory) (Object) this;

		boolean tenthVisible = isTenthSlotVisible(inventory);
		int totalSlots = tenthVisible ? TenthSlot.TENTH_SLOT_INDEX + 1 : 9;
		int current = inventory.selected;

		// If we're on the tenth slot but it just became hidden, snap to slot 0.
		if (!tenthVisible && current == TenthSlot.TENTH_SLOT_INDEX) {
			current = 0;
		}

		if (direction > 0.0) {
			current = (current - 1 + totalSlots) % totalSlots;
		} else {
			current = (current + 1) % totalSlots;
		}

		inventory.selected = current;
		ci.cancel();
	}

	private boolean isTenthSlotVisible(Inventory inventory) {
		if (!TenthSlotConfig.get().showOnlyWhenHotbarFull) return true;
		for (int i = 0; i < 9; i++) {
			if (inventory.items.get(i).isEmpty()) return false;
		}
		return true;
	}
}