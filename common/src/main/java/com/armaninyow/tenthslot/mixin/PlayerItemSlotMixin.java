package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PlayerItemSlotMixin {

	@Inject(
		at = @At("HEAD"),
		method = "setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
		cancellable = true
	)
	private void onSetItemInHand(InteractionHand hand, ItemStack stack, CallbackInfo ci) {
		if (!((Object) this instanceof Player player)) return;
		if (((InventoryAccessor) player.getInventory()).tenthslot$getSelected() != TenthSlot.TENTH_SLOT_INDEX) return;
		if (hand == InteractionHand.MAIN_HAND) {
			ci.cancel();
		} else if (hand == InteractionHand.OFF_HAND && stack.isEmpty()) {
			ci.cancel();
		}
	}
}