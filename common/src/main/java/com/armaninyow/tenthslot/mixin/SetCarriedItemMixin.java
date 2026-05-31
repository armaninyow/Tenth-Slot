package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class SetCarriedItemMixin {

	@Inject(
		at = @At("HEAD"),
		method = "handleSetCarriedItem(Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;)V",
		cancellable = true
	)
	private void onHandleSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
		if (packet.getSlot() != TenthSlot.TENTH_SLOT_INDEX) return;

		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		if (listener.player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
			listener.player.stopUsingItem();
		}
		((InventoryAccessor) listener.player.getInventory()).tenthslot$setSelected(TenthSlot.TENTH_SLOT_INDEX);
		listener.player.resetLastActionTime();
		ci.cancel();
	}
}