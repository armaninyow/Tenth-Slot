package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the vanilla hotbar render to include the tenth slot.
 *
 * Vanilla draws its selection box unconditionally for inventory.selected
 * via: i - 91 - 1 + selected * 20. When selected == 9 that lands at
 * i + 69, still inside the hotbar sprite — producing a phantom box.
 * We cancel the entire renderItemHotbar and re-implement it, skipping
 * vanilla's selection draw when slot 10 is active and drawing our own
 * instead over the offhand-nub sprite.
 *
 * Tenth slot sprite is placed flush against the hotbar end (no extra gap).
 * The 29 px offhand sprite has ~7 px of built-in padding on its inner edge,
 * which provides the natural visual separation.
 *
 * Selection box position: the actual slot inside the offhand sprite sits at
 * its LEFT side (for RIGHT-hand placement). Vanilla's selection box is 24 px
 * wide; we align it to x = slotX - 1 to match vanilla's -1 offset.
 */
// 1.21_1.21.1
@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class InGameHudMixin {

		private static final ResourceLocation HOTBAR_SPRITE =
		ResourceLocation.withDefaultNamespace("hud/hotbar");
	private static final ResourceLocation HOTBAR_SELECTION_SPRITE =
		ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
	private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE =
		ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
	private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE =
		ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
	private static final ResourceLocation BARRIER_TEXTURE =
		ResourceLocation.withDefaultNamespace("textures/item/barrier.png");

	// Size to render the barrier icon inside the slot (matches vanilla item size).
	private static final int BARRIER_SIZE = 16;

	@Inject(
		at = @At("HEAD"),
		method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
		cancellable = true
	)
	private void onRenderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		// Let vanilla handle everything when slot 10 is NOT selected.
		// We only need to take over when slot 10 IS selected, to suppress
		// vanilla's out-of-bounds selection box draw.
		int selected = client.player.getInventory().selected;
		if (selected != TenthSlot.TENTH_SLOT_INDEX) {
			// Not our slot — let vanilla render normally, then just append
			// the tenth-slot sprite at the tail.
			return;
		}

		// Slot 10 is active: cancel vanilla (which would draw a broken
		// selection box at slot index 9's position + 20 px = off-screen)
		// and repaint everything ourselves.
		ci.cancel();

		net.minecraft.world.entity.player.Player player =
			client.getCameraEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
		if (player == null) return;

		net.minecraft.world.item.ItemStack offhand = player.getOffhandItem();
		net.minecraft.world.entity.HumanoidArm humanoidArm = player.getMainArm().getOpposite();
		int i = guiGraphics.guiWidth() / 2;
		int screenH = guiGraphics.guiHeight();

		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);

		// Draw the main 9-slot hotbar sprite.
		guiGraphics.blitSprite(HOTBAR_SPRITE, i - 91, screenH - 22, 182, 22);

		// Do NOT draw vanilla's selection box (slot 10 is active, it would
		// render at i - 91 - 1 + 9*20 = i + 89, clipping into the nub area).

		// Draw the offhand nub if the player has an offhand item.
		if (!offhand.isEmpty()) {
			if (humanoidArm == HumanoidArm.LEFT) {
				guiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, screenH - 23, 29, 24);
			} else {
				guiGraphics.blitSprite(HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, screenH - 23, 29, 24);
			}
		}

		guiGraphics.pose().popPose();
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();

		// Render items in the 9 visible hotbar slots.
		int l = 1;
		for (int m = 0; m < 9; m++) {
			int x = i - 90 + m * 20 + 2;
			int y = screenH - 16 - 3;
			renderSlotItem(guiGraphics, deltaTracker, player, x, y,
				player.getInventory().items.get(m), l++);
		}

		// Render offhand item if present.
		if (!offhand.isEmpty()) {
			int y = screenH - 16 - 3;
			if (humanoidArm == HumanoidArm.LEFT) {
				renderSlotItem(guiGraphics, deltaTracker, player, i - 91 - 26, y, offhand, l++);
			} else {
				renderSlotItem(guiGraphics, deltaTracker, player, i + 91 + 10, y, offhand, l++);
			}
		}

		// Now draw the tenth slot sprite + selection box.
		drawTenthSlot(guiGraphics, client, player, i, screenH);
	}

	/**
	 * Always draw the tenth-slot sprite at the TAIL (when vanilla rendered
	 * normally, i.e. slot 10 was NOT active). When slot 10 IS active we
	 * already drew it inside the HEAD cancellation above; but since we
	 * cancelled, this TAIL never fires — so no double draw.
	 */
	@Inject(
		at = @At("TAIL"),
		method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void onRenderItemHotbarTail(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;
		net.minecraft.world.entity.player.Player player =
			client.getCameraEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
		if (player == null) return;

		int i = guiGraphics.guiWidth() / 2;
		int screenH = guiGraphics.guiHeight();
		drawTenthSlot(guiGraphics, client, player, i, screenH);
	}

	/**
	 * Draw the tenth-slot offhand sprite (and selection box if active).
	 *
	 * Placement: flush against the hotbar end, no extra gap.
	 * The offhand sprite is 29 px wide; its inner ~7 px is built-in padding.
	 *
	 * For RIGHT main hand: tenth slot goes on the RIGHT side.
	 *   Vanilla hotbar right edge = i + 91.
	 *   Sprite starts at i + 91 (same x vanilla uses for the offhand nub).
	 *   If there IS an offhand nub (29 px wide), tenth slot starts at i + 91 + 29.
	 *
	 * For LEFT main hand: mirror accordingly.
	 *
	 * Selection box: align to the LEFT edge of the sprite (x = slotX - 1),
	 * matching vanilla's -1 offset convention.
	 */
	private void drawTenthSlot(GuiGraphics guiGraphics, Minecraft client,
	                            net.minecraft.world.entity.player.Player player,
	                            int i, int screenH) {
		// If the config option is enabled, only show the tenth slot when all
		// 9 hotbar slots are occupied.
		if (com.armaninyow.tenthslot.TenthSlotConfig.get().showOnlyWhenHotbarFull) {
			boolean hotbarFull = true;
			for (int slot = 0; slot < 9; slot++) {
				if (player.getInventory().items.get(slot).isEmpty()) {
					hotbarFull = false;
					break;
				}
			}
			if (!hotbarFull) {
				// If the player is on the tenth slot but it's hidden, switch to slot 0.
				if (client.player.getInventory().selected == TenthSlot.TENTH_SLOT_INDEX) {
					client.player.getInventory().selected = 0;
				}
				return;
			}
		}

		HumanoidArm mainArm = player.getMainArm();
		// slotY matches vanilla's offhand nub y.
		int slotY = screenH - 23;

		int slotX;
		ResourceLocation sprite;

		if (mainArm == HumanoidArm.RIGHT) {
			slotX = i + 91;
			sprite = HOTBAR_OFFHAND_RIGHT_SPRITE;
		} else {
			slotX = i - 91 - 29;
			sprite = HOTBAR_OFFHAND_LEFT_SPRITE;
		}

		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
		guiGraphics.blitSprite(sprite, slotX, slotY, 29, 24);
		guiGraphics.pose().popPose();

		// Draw the barrier icon on top, outside the sprite pose block so it
		// is not affected by the -90 z offset and renders above the sprite.
		int barrierX = slotX + (mainArm == HumanoidArm.RIGHT ? 10 : 3);
		int barrierY = slotY + 4;
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
		guiGraphics.blit(BARRIER_TEXTURE, barrierX, barrierY, 0.0F, 0.0F,
			BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE);
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		com.mojang.blaze3d.systems.RenderSystem.disableBlend();

		// Draw selection box when slot 10 is active.
		if (client.player.getInventory().selected == TenthSlot.TENTH_SLOT_INDEX) {
			int selX;
			int selY;
			if (mainArm == HumanoidArm.RIGHT) {
				// hotbar_offhand_right: slot is on the RIGHT side of the 29px sprite.
				selX = slotX + 29 - 24 - 1 + 2;
				selY = slotY - 1 + 1;
			} else {
				// hotbar_offhand_left: slot is on the LEFT side of the 29px sprite.
				selX = slotX - 1;
				selY = slotY - 1 + 1;
			}
			guiGraphics.blitSprite(HOTBAR_SELECTION_SPRITE, selX, selY, 24, 23);
		}
	}

	/** Minimal item render helper — replicates what vanilla does per slot. */
	private void renderSlotItem(GuiGraphics guiGraphics, DeltaTracker deltaTracker,
	                             net.minecraft.world.entity.player.Player player,
	                             int x, int y, net.minecraft.world.item.ItemStack stack, int seed) {
		if (stack.isEmpty()) return;
		float pop = stack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
		if (pop > 0.0F) {
			float scale = 1.0F + pop / 5.0F;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(x + 8, y + 12, 0.0F);
			guiGraphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
			guiGraphics.pose().translate(-(x + 8), -(y + 12), 0.0F);
		}
		guiGraphics.renderItem(player, stack, x, y, seed);
		if (pop > 0.0F) guiGraphics.pose().popPose();
		guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
	}
}