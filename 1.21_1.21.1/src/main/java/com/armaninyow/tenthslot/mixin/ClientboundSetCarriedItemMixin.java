package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts ClientboundSetCarriedItemPacket on the client.
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
 */
// 1.21_1.21.1
@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientboundSetCarriedItemMixin {

	@Inject(
		at = @At("HEAD"),
		method = "handleSetCarriedItem(Lnet/minecraft/network/protocol/game/ClientboundSetCarriedItemPacket;)V",
		cancellable = true
	)
	private void onHandleSetCarriedItem(ClientboundSetCarriedItemPacket packet, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		// If the client has slot 9 selected and the server is trying to reset
		// it to something else, cancel and re-sync slot 9 back to the server.
		if (client.player.getInventory().selected == TenthSlot.TENTH_SLOT_INDEX
				&& packet.getSlot() != TenthSlot.TENTH_SLOT_INDEX) {
			client.player.connection.send(
				new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(
					TenthSlot.TENTH_SLOT_INDEX));
			ci.cancel();
		}
	}
}