package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts ClientboundSetHeldSlotPacket on the client.
 *
 * When the game pauses and resumes (or on certain server state resets), the
 * server sends this packet to sync the hotbar slot. Since the server reverts
 * to slot 0 during pause/resume (it never accepted slot 9 as persistent state
 * outside our patch), the client receives slot 0 and overwrites selected=9,
 * causing a desync where the server thinks slot 0 is active but the client
 * visually shows slot 9.
 *
 * Fix: if the client currently has slot 9 selected when this packet arrives,
 * ignore the packet's slot value and immediately re-send slot 9 to the server
 * to keep both sides in sync.
 *
 * 1.21.4: ClientboundSetHeldSlotPacket is a Record, so accessor is slot()
 * not getSlot().
 */
// 1.21.9_1.21.10
@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientboundSetCarriedItemMixin {

	@Inject(
		at = @At("HEAD"),
		method = "handleSetHeldSlot(Lnet/minecraft/network/protocol/game/ClientboundSetHeldSlotPacket;)V",
		cancellable = true
	)
	private void onHandleSetHeldSlot(ClientboundSetHeldSlotPacket packet, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		if (((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX
				&& packet.slot() != TenthSlot.TENTH_SLOT_INDEX) {
			client.player.connection.send(
				new ServerboundSetCarriedItemPacket(TenthSlot.TENTH_SLOT_INDEX));
			ci.cancel();
		}
	}
}