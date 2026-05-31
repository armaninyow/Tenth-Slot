package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import com.armaninyow.tenthslot.TenthSlotClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class HotbarMixin {

	@Inject(at = @At("TAIL"), method = "tick")
	private void onTick(CallbackInfo ci) {
		Minecraft client = (Minecraft) (Object) this;
		LocalPlayer player = client.player;
		if (player == null) return;

		if (TenthSlotClient.KEY_TENTH_SLOT.consumeClick()) {
			((InventoryAccessor) player.getInventory()).tenthslot$setSelected(TenthSlot.TENTH_SLOT_INDEX);
		}
	}
}