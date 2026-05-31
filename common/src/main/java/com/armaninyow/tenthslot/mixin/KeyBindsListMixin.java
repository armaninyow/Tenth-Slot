package com.armaninyow.tenthslot.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Injects into KeyBindsScreen.addContents() at TAIL — which fires after
 * KeyBindsList is fully built and sorted — to move the Tenth Slot entry
 * after Hotbar Slot 9.
 */
@Environment(EnvType.CLIENT)
@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsListMixin {

	@Inject(at = @At("TAIL"), method = "addContents()V")
	private void onAddContents(CallbackInfo ci) {
		KeyBindsScreen screen = (KeyBindsScreen) (Object) this;

		KeyBindsList list = null;
		try {
			for (Field f : screen.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				Object val = f.get(screen);
				if (val instanceof KeyBindsList kbl) {
					list = kbl;
					break;
				}
			}
			if (list == null) {
				for (Field f : screen.getClass().getSuperclass().getDeclaredFields()) {
					f.setAccessible(true);
					Object val = f.get(screen);
					if (val instanceof KeyBindsList kbl) {
						list = kbl;
						break;
					}
				}
			}
		} catch (Exception e) {
			return;
		}

		if (list == null) return;
		repositionTenthSlot(list);
	}

	private static void repositionTenthSlot(KeyBindsList list) {
		List<KeyBindsList.Entry> entries = null;
		try {
			Class<?> cls = list.getClass().getSuperclass();
			while (cls != null) {
				for (Field f : cls.getDeclaredFields()) {
					if (List.class.isAssignableFrom(f.getType())) {
						f.setAccessible(true);
						Object val = f.get(list);
						if (val instanceof List<?> l && !l.isEmpty()
								&& l.get(0) instanceof KeyBindsList.Entry) {
							@SuppressWarnings("unchecked")
							List<KeyBindsList.Entry> typed = (List<KeyBindsList.Entry>) l;
							entries = typed;
							break;
						}
					}
				}
				if (entries != null) break;
				cls = cls.getSuperclass();
			}
		} catch (Exception e) {
			return;
		}

		if (entries == null) return;

		int slot9Index = -1;
		int tenthSlotIndex = -1;

		for (int i = 0; i < entries.size(); i++) {
			KeyBindsList.Entry entry = entries.get(i);
			if (entry instanceof KeyBindsList.KeyEntry keyEntry) {
				String name = ((KeyEntryAccessor) keyEntry).tenthslot$getKey().getName();
				if ("key.hotbar.9".equals(name)) slot9Index = i;
				else if ("key.tenthslot.tenth_slot".equals(name)) tenthSlotIndex = i;
			}
		}

		if (slot9Index == -1 || tenthSlotIndex == -1) return;
		if (tenthSlotIndex == slot9Index + 1) return;

		KeyBindsList.Entry tenthEntry = entries.remove(tenthSlotIndex);
		if (tenthSlotIndex < slot9Index) slot9Index--;
		entries.add(slot9Index + 1, tenthEntry);
	}
}