package com.armaninyow.tenthslot.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes private fields of {@link Inventory} that became private in 1.21.4.
 */
@Mixin(Inventory.class)
public interface InventoryAccessor {

	@Accessor("selected")
	int tenthslot$getSelected();

	@Accessor("selected")
	void tenthslot$setSelected(int selected);

	@Accessor("items")
	NonNullList<ItemStack> tenthslot$getItems();
}