package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds a dedicated backing store for the tenth hotbar slot.
 *
 * The tenth slot is a virtual selection state (selected == 9). Vanilla's
 * getSelected() already returns ItemStack.EMPTY when isHotbarSlot(selected)
 * is false, and isHotbarSlot(9) is false, so no patching of getSelected()
 * or isHotbarSlot() is needed.
 *
 * We attach a @Unique NonNullList so the slot has a real, always-empty
 * backing store that is completely separate from vanilla's items/armor/offhand
 * compartments. Vanilla never touches it, so items[9] (the top-left main
 * inventory slot) is never affected.
 *
 * getSelectionSize() is NOT patched here — patching it globally broke
 * creative mode hotbar shortcut keys by widening the range used to validate
 * inventory swap targets. The server-side slot validation is handled
 * separately in SetCarriedItemMixin by patching handleSetCarriedItem directly.
 */
@Mixin(Inventory.class)
public class InventoryMixin {

	@Unique
	private final NonNullList<ItemStack> tenthslot$tenthSlot =
		NonNullList.withSize(1, ItemStack.EMPTY);
}