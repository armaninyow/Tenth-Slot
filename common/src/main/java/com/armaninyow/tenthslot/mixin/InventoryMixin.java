package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Inventory.class)
public class InventoryMixin {

	@Unique
	private final NonNullList<ItemStack> tenthslot$tenthSlot =
		NonNullList.withSize(1, ItemStack.EMPTY);
}