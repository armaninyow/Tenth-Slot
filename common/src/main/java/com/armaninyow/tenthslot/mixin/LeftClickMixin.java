package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import com.armaninyow.tenthslot.TenthSlotConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks the hand swing animation and crosshair attack indicator when
 * vanillaLeftClick is disabled and the tenth slot is selected.
 *
 * startAttack() and continueAttack() in Minecraft call player.swing()
 * after invoking gameMode.attack() / gameMode.startDestroyBlock() /
 * gameMode.continueDestroyBlock(). Cancelling them here at HEAD stops
 * both the gameMode calls and the swing in one shot, complementing the
 * injections in GameModeMixin which would otherwise still leave the
 * animation firing.
 *
 * Valid for 1.21_1.21.1.
 */
// 1.21_1.21.1
@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class LeftClickMixin {

	private boolean shouldBlockLeftClick() {
		Minecraft client = (Minecraft) (Object) this;
		if (client.player == null) return false;
		return !TenthSlotConfig.get().vanillaLeftClick
			&& ((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX;
	}

	@Inject(
		at = @At("HEAD"),
		method = "startAttack()Z",
		cancellable = true
	)
	private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (shouldBlockLeftClick()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(
		at = @At("HEAD"),
		method = "continueAttack(Z)V",
		cancellable = true
	)
	private void onContinueAttack(boolean breaking, CallbackInfo ci) {
		if (shouldBlockLeftClick()) {
			ci.cancel();
		}
	}
}