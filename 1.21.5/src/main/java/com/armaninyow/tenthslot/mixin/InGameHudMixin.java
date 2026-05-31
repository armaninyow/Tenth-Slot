package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
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
 *
 * 1.21.2+: blitSprite and blit now require Function<ResourceLocation, RenderType>
 * as first argument. The barrier icon is rendered via blitSprite with a
 * dedicated single-pixel white sprite scaled up, or via the item renderer.
 * We use renderItem to draw the barrier instead of blit to avoid the removed
 * raw blit overload.
 */
// 1.21.5
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

	// Size to render the barrier icon inside the slot (matches vanilla item size).
	private static final ResourceLocation BARRIER_TEXTURE =
		ResourceLocation.withDefaultNamespace("textures/item/barrier.png");

	// Size to render the barrier icon inside the slot (matches vanilla item size).
	private static final int BARRIER_SIZE = 16;

	// Cached barrier ItemStack to render as the tenth slot icon.
	private static net.minecraft.world.item.ItemStack barrierStack = null;

	private static net.minecraft.world.item.ItemStack getBarrierStack() {
		if (barrierStack == null) {
			barrierStack = new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.BARRIER);
		}
		return barrierStack;
	}

	@Inject(
		at = @At("HEAD"),
		method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
		cancellable = true
	)
	private void onRenderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		int selected = ((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected();
		if (selected != TenthSlot.TENTH_SLOT_INDEX) {
			return;
		}

		ci.cancel();

		net.minecraft.world.entity.player.Player player =
			client.getCameraEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
		if (player == null) return;

		net.minecraft.world.item.ItemStack offhand = player.getOffhandItem();
		net.minecraft.world.entity.HumanoidArm humanoidArm = player.getMainArm().getOpposite();
		int i = guiGraphics.guiWidth() / 2;
		int screenH = guiGraphics.guiHeight();

				guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);

		guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SPRITE, i - 91, screenH - 22, 182, 22);

		if (!offhand.isEmpty()) {
			if (humanoidArm == HumanoidArm.LEFT) {
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, screenH - 23, 29, 24);
			} else {
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, screenH - 23, 29, 24);
			}
		}

		guiGraphics.pose().popPose();
		
		int l = 1;
		for (int m = 0; m < 9; m++) {
			int x = i - 90 + m * 20 + 2;
			int y = screenH - 16 - 3;
			renderSlotItem(guiGraphics, deltaTracker, player, x, y,
				player.getInventory().getItem(m), l++);
		}

		if (!offhand.isEmpty()) {
			int y = screenH - 16 - 3;
			if (humanoidArm == HumanoidArm.LEFT) {
				renderSlotItem(guiGraphics, deltaTracker, player, i - 91 - 26, y, offhand, l++);
			} else {
				renderSlotItem(guiGraphics, deltaTracker, player, i + 91 + 10, y, offhand, l++);
			}
		}

		drawTenthSlot(guiGraphics, client, player, i, screenH);
	}

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

	private void drawTenthSlot(GuiGraphics guiGraphics, Minecraft client,
	                            net.minecraft.world.entity.player.Player player,
	                            int i, int screenH) {
		if (com.armaninyow.tenthslot.TenthSlotConfig.get().showOnlyWhenHotbarFull) {
			boolean hotbarFull = true;
			for (int slot = 0; slot < 9; slot++) {
				if (player.getInventory().getItem(slot).isEmpty()) {
					hotbarFull = false;
					break;
				}
			}
			if (!hotbarFull) {
				if (((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
					((InventoryAccessor) client.player.getInventory()).tenthslot$setSelected(0);
				}
				return;
			}
		}

		HumanoidArm mainArm = player.getMainArm();
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

				guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
		guiGraphics.blitSprite(RenderType::guiTextured, sprite, slotX, slotY, 29, 24);
		guiGraphics.pose().popPose();
		
		// Render the barrier texture darkened, matching the 1.21.1 tint.
		// In 1.21.2 blit requires a RenderType function as the first argument.
		int barrierX = slotX + (mainArm == HumanoidArm.RIGHT ? 10 : 3);
		int barrierY = slotY + 4;
		// Draw the barrier texture grayscale at 25% opacity, matching 1.21.1.
		// flush() isolates this draw so setShaderColor does not bleed into
		// the sprite draws above or below.
		guiGraphics.flush();
				com.mojang.blaze3d.systems.RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
		guiGraphics.blit(RenderType::guiTextured, BARRIER_TEXTURE, barrierX, barrierY, 0.0F, 0.0F,
			BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE);
		guiGraphics.flush();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		if (((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			int selX;
			int selY;
			if (mainArm == HumanoidArm.RIGHT) {
				selX = slotX + 29 - 24 - 1 + 2;
				selY = slotY - 1 + 1;
			} else {
				selX = slotX - 1;
				selY = slotY - 1 + 1;
			}
						guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SELECTION_SPRITE, selX, selY, 24, 23);
			guiGraphics.pose().popPose();
					}
	}

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