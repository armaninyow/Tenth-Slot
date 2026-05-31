package com.armaninyow.tenthslot.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code key} field of {@link KeyBindsList.KeyEntry}.
 */
@Mixin(KeyBindsList.KeyEntry.class)
public interface KeyEntryAccessor {

	@Accessor("key")
	KeyMapping tenthslot$getKey();
}