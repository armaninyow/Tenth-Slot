package com.armaninyow.tenthslot.mixin;

import com.armaninyow.tenthslot.TenthSlot;
import com.armaninyow.tenthslot.TenthSlotConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1.x changes from 1.21.4:
 *  - interact(Player, Entity, InteractionHand) removed.
 *  - interactAt(Player, Entity, EntityHitResult, InteractionHand) removed.
 *  - Both replaced by interact(Player, Entity, EntityHitResult, InteractionHand).
 *  - useItemOn now takes LocalPlayer instead of Player (no change needed here,
 *    we still inject with the same descriptor since LocalPlayer extends Player).
 */
@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class GameModeMixin {

	@Shadow @Final
	private Minecraft minecraft;

	private static boolean isTenthSlotSelected(Player player) {
		return ((InventoryAccessor) player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX;
	}

	private static boolean shouldBlockOffhandActions(Player player) {
		return !TenthSlotConfig.get().vanillaOffhandActions
			&& isTenthSlotSelected(player);
	}

	private static boolean shouldBlockNonItemRightClick(Player player) {
		return !TenthSlotConfig.get().vanillaNonItemRightClick
			&& isTenthSlotSelected(player);
	}

	private static boolean shouldBlockLeftClick(Player player) {
		return !TenthSlotConfig.get().vanillaLeftClick
			&& isTenthSlotSelected(player);
	}

	// --- vanillaOffhandActions / vanillaNonItemRightClick ---

	@Inject(
		at = @At("HEAD"),
		method = "useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
		cancellable = true
	)
	private void onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (shouldBlockOffhandActions(player)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@Inject(
		at = @At("HEAD"),
		method = "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
		cancellable = true
	)
	private void onUseItemOn(LocalPlayer player, InteractionHand hand,
	                          BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (hand == InteractionHand.OFF_HAND && shouldBlockOffhandActions(player)) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}
		if (hand == InteractionHand.MAIN_HAND && shouldBlockNonItemRightClick(player)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	/**
	 * In 26.1.x, interact(Player, Entity, InteractionHand) and
	 * interactAt(Player, Entity, EntityHitResult, InteractionHand) were merged
	 * into interact(Player, Entity, EntityHitResult, InteractionHand).
	 */
	@Inject(
		at = @At("HEAD"),
		method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
		cancellable = true
	)
	private void onInteract(Player player, Entity entity, EntityHitResult hitResult,
	                         InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (hand == InteractionHand.OFF_HAND && shouldBlockOffhandActions(player)) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}
		if (hand == InteractionHand.MAIN_HAND && shouldBlockNonItemRightClick(player)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	// --- vanillaLeftClick ---

	@Inject(
		at = @At("HEAD"),
		method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V",
		cancellable = true
	)
	private void onAttack(Player player, Entity entity, CallbackInfo ci) {
		if (shouldBlockLeftClick(player)) {
			ci.cancel();
		}
	}

	@Inject(
		at = @At("HEAD"),
		method = "startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
		cancellable = true
	)
	private void onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (minecraft.player != null && shouldBlockLeftClick(minecraft.player)) {
			cir.setReturnValue(false);
		}
	}

	// --- handlePickItem (always blocked when slot 10 is selected) ---

	@Inject(
		at = @At("HEAD"),
		method = "handlePickItemFromBlock(Lnet/minecraft/core/BlockPos;Z)V",
		cancellable = true
	)
	private void onHandlePickItemFromBlock(BlockPos blockPos, boolean bl, CallbackInfo ci) {
		if (minecraft.player != null
				&& ((InventoryAccessor) minecraft.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			ci.cancel();
		}
	}

	@Inject(
		at = @At("HEAD"),
		method = "handlePickItemFromEntity(Lnet/minecraft/world/entity/Entity;Z)V",
		cancellable = true
	)
	private void onHandlePickItemFromEntity(Entity entity, boolean bl, CallbackInfo ci) {
		if (minecraft.player != null
				&& ((InventoryAccessor) minecraft.player.getInventory()).tenthslot$getSelected() == TenthSlot.TENTH_SLOT_INDEX) {
			ci.cancel();
		}
	}
}