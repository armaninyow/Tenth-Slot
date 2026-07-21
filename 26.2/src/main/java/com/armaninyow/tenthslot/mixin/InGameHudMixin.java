package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public class InGameHudMixin {

	private static final Identifier HOTBAR_SPRITE =
		Identifier.withDefaultNamespace("hud/hotbar");
	private static final Identifier HOTBAR_SELECTION_SPRITE =
		Identifier.withDefaultNamespace("hud/hotbar_selection");
	private static final Identifier HOTBAR_OFFHAND_LEFT_SPRITE =
		Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
	private static final Identifier HOTBAR_OFFHAND_RIGHT_SPRITE =
		Identifier.withDefaultNamespace("hud/hotbar_offhand_right");
	private static final Identifier BARRIER_TEXTURE =
		Identifier.withDefaultNamespace("textures/item/barrier.png");
	private static final int BARRIER_SIZE = 16;

	@Inject(
		at = @At("HEAD"),
		method = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
		cancellable = true
	)
	private void onExtractItemHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		int selected = ((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected();
		if (selected != TenthSlot.TENTH_SLOT_INDEX) return;

		ci.cancel();

		net.minecraft.world.entity.player.Player player =
			client.getCameraEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
		if (player == null) return;

		net.minecraft.world.item.ItemStack offhand = player.getOffhandItem();
		net.minecraft.world.entity.HumanoidArm humanoidArm = player.getMainArm().getOpposite();
		int i = guiGraphics.guiWidth() / 2;
		int screenH = guiGraphics.guiHeight();

		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, i - 91, screenH - 22, 182, 22);

		if (!offhand.isEmpty()) {
			if (humanoidArm == HumanoidArm.LEFT) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, screenH - 23, 29, 24);
			} else {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, screenH - 23, 29, 24);
			}
		}

		int l = 1;
		for (int m = 0; m < 9; m++) {
			int x = i - 90 + m * 20 + 2;
			int y = screenH - 16 - 3;
			renderSlotItem(guiGraphics, deltaTracker, player,
				x, y, ((InventoryAccessor) player.getInventory()).tenthslot$getItems().get(m), l++);
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
		method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void onExtractHotbarAndDecorationsTail(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		if (client.gameMode.getPlayerMode() == GameType.SPECTATOR) return;

		if (((InventoryAccessor) client.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) return;

		net.minecraft.world.entity.player.Player player =
			client.getCameraEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
		if (player == null) return;

		int i = guiGraphics.guiWidth() / 2;
		int screenH = guiGraphics.guiHeight();
		drawTenthSlot(guiGraphics, client, player, i, screenH);
	}

	private void drawTenthSlot(GuiGraphicsExtractor guiGraphics, Minecraft client,
	                            net.minecraft.world.entity.player.Player player,
	                            int i, int screenH) {
		if (com.armaninyow.tenthslot.TenthSlotConfig.get().showOnlyWhenHotbarFull) {
			boolean hotbarFull = true;
			for (int slot = 0; slot < 9; slot++) {
				if (((InventoryAccessor) player.getInventory()).tenthslot$getItems().get(slot).isEmpty()) {
					hotbarFull = false;
					break;
				}
			}
			if (!hotbarFull) {
				InventoryAccessor inv = (InventoryAccessor) client.player.getInventory();
				if (inv.tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
					inv.tenthslot$setSelected(0);
				}
				return;
			}
		}

		HumanoidArm mainArm = player.getMainArm();
		int slotY = screenH - 23;

		int slotX;
		Identifier sprite;

		if (mainArm == HumanoidArm.RIGHT) {
			slotX = i + 91;
			sprite = HOTBAR_OFFHAND_RIGHT_SPRITE;
		} else {
			slotX = i - 91 - 29;
			sprite = HOTBAR_OFFHAND_LEFT_SPRITE;
		}

		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, slotX, slotY, 29, 24);

		int barrierX = slotX + (mainArm == HumanoidArm.RIGHT ? 10 : 3);
		int barrierY = slotY + 4;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BARRIER_TEXTURE, barrierX, barrierY, 0.0F, 0.0F,
			BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE, BARRIER_SIZE, 0x80000000);

		InventoryAccessor inv = (InventoryAccessor) client.player.getInventory();
		if (inv.tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			int selX;
			int selY;
			if (mainArm == HumanoidArm.RIGHT) {
				selX = slotX + 29 - 24 - 1 + 2;
				selY = slotY - 1 + 1;
			} else {
				selX = slotX - 1;
				selY = slotY - 1 + 1;
			}
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, selX, selY, 24, 23);
		}
	}

	private void renderSlotItem(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker,
	                             net.minecraft.world.entity.player.Player player,
	                             int x, int y, net.minecraft.world.item.ItemStack stack, int seed) {
		if (stack.isEmpty()) return;
		float pop = stack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
		if (pop > 0.0F) {
			float scale = 1.0F + pop / 5.0F;
			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(x + 8, y + 12);
			guiGraphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F);
			guiGraphics.pose().translate(-(x + 8), -(y + 12));
		}
		guiGraphics.item(player, stack, x, y, seed);
		if (pop > 0.0F) guiGraphics.pose().popMatrix();
		guiGraphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
	}
}