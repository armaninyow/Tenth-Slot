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
 * 1.21.2+ version — packet renamed from ClientboundSetCarriedItemPacket
 * to ClientboundSetHeldSlotPacket, handler renamed to handleSetHeldSlot.
 */
// 1.21.2_1.21.3
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

		if (client.player.getInventory().selected == TenthSlot.TENTH_SLOT_INDEX
				&& packet.getSlot() != TenthSlot.TENTH_SLOT_INDEX) {
			client.player.connection.send(
				new ServerboundSetCarriedItemPacket(TenthSlot.TENTH_SLOT_INDEX));
			ci.cancel();
		}
	}
}