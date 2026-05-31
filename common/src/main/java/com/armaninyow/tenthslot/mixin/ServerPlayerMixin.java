package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ServerPlayerMixin {

	@Inject(
		at = @At("HEAD"),
		method = "getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
		cancellable = true
	)
	private void onGetMainHandItem(CallbackInfoReturnable<ItemStack> cir) {
		if (!((Object) this instanceof Player player)) return;
		if (((InventoryAccessor) player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}