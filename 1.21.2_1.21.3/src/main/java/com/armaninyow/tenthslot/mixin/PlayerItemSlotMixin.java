package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks setItemSlot writes when slot 10 is selected.
 *  - MAINHAND: always blocked (would corrupt items[9])
 *  - OFFHAND: only block EMPTY writes (F key swap clears the offhand).
 *    Non-empty writes (e.g. returning a bucket/bowl/bottle after drinking)
 *    must be allowed so the container item is not lost.
 *
 * Valid for 1.21 through 1.21.4. In 1.21.5 setItemSlot was replaced by
 * setItemInHand — use PlayerItemSlotMixin from the 1.21.5 branch instead.
 */
// 1.21.2_1.21.3
@Mixin(Player.class)
public class PlayerItemSlotMixin {

	@Inject(
		at = @At("HEAD"),
		method = "setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
		cancellable = true
	)
	private void onSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (((InventoryAccessor) player.getInventory()).tenthslot$getSelected() != TenthSlot.TENTH_SLOT_INDEX) return;
		if (slot == EquipmentSlot.MAINHAND) {
			ci.cancel();
		} else if (slot == EquipmentSlot.OFFHAND && stack.isEmpty()) {
			ci.cancel();
		}
	}
}