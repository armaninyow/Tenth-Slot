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

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class ScrollMixin {

	@Unique private double tenthslot$lastScrollY = 0.0;
	@Unique private int tenthslot$slotBeforeScroll = -1;

	@Inject(at = @At("HEAD"), method = "onScroll(JDD)V", cancellable = true)
	private void onScrollHead(long window, double x, double y, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		Inventory inv = client.player.getInventory();
		tenthslot$lastScrollY = y;
		tenthslot$slotBeforeScroll = ((InventoryAccessor) inv).tenthslot$getSelected();

		if (((InventoryAccessor) inv).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			if (!isTenthSlotVisible(inv)) {
				((InventoryAccessor) inv).tenthslot$setSelected(0);
				return;
			}
			double sensitivity = client.options.mouseWheelSensitivity().get();
			double delta = y * sensitivity;
			if (delta > 0.0) {
				((InventoryAccessor) inv).tenthslot$setSelected(8);
			} else if (delta < 0.0) {
				((InventoryAccessor) inv).tenthslot$setSelected(0);
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
		int curr = ((InventoryAccessor) inv).tenthslot$getSelected();
		double sensitivity = client.options.mouseWheelSensitivity().get();
		double delta = tenthslot$lastScrollY * sensitivity;

		if (delta < 0.0 && prev == 8 && curr == 0) {
			((InventoryAccessor) inv).tenthslot$setSelected(TenthSlot.TENTH_SLOT_INDEX);
		}
		else if (delta > 0.0 && prev == 0 && curr == 8) {
			((InventoryAccessor) inv).tenthslot$setSelected(TenthSlot.TENTH_SLOT_INDEX);
		}
	}

	private boolean isTenthSlotVisible(Inventory inventory) {
		if (!TenthSlotConfig.get().showOnlyWhenHotbarFull) return true;
		for (int i = 0; i < 9; i++) {
			if (((InventoryAccessor) inventory).tenthslot$getItems().get(i).isEmpty()) return false;
		}
		return true;
	}
}