package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

	@Unique
	private boolean tenthslot$wasOnTenthSlot = false;

	@Inject(
		at = @At("HEAD"),
		method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"
	)
	private void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		InventoryAccessor inv = (InventoryAccessor) player.getInventory();
		if (inv.tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			tenthslot$wasOnTenthSlot = true;
			inv.tenthslot$setSelected(0);
		}
	}

	@Inject(
		at = @At("TAIL"),
		method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"
	)
	private void onAddAdditionalSaveDataTail(ValueOutput output, CallbackInfo ci) {
		if (tenthslot$wasOnTenthSlot) {
			Player player = (Player) (Object) this;
			((InventoryAccessor) player.getInventory()).tenthslot$setSelected(TenthSlot.TENTH_SLOT_INDEX);
			tenthslot$wasOnTenthSlot = false;
		}
	}
}