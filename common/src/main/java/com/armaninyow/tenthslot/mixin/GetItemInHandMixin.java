package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class GetItemInHandMixin {

	@Inject(
		at = @At("HEAD"),
		method = "getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
		cancellable = true
	)
	private void onGetItemInHand(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
		if (hand != InteractionHand.MAIN_HAND) return;
		if (!((Object) this instanceof Player player)) return;
		if (((InventoryAccessor) player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}