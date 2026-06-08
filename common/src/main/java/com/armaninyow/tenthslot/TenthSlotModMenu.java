package com.armaninyow.tenthslot;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TenthSlotModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			TenthSlotConfig cfg = TenthSlotConfig.get();

			return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.tenthslot.title"))
				.category(ConfigCategory.createBuilder()
					.name(Component.translatable("config.tenthslot.category.general"))
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.tenthslot.showOnlyWhenHotbarFull"))
						.description(OptionDescription.of(Component.translatable("config.tenthslot.showOnlyWhenHotbarFull.desc")))
						.binding(false, () -> cfg.showOnlyWhenHotbarFull, v -> cfg.showOnlyWhenHotbarFull = v)
						.controller(TickBoxControllerBuilder::create)
						.build())
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.tenthslot.vanillaOffhandActions"))
						.description(OptionDescription.of(Component.translatable("config.tenthslot.vanillaOffhandActions.desc")))
						.binding(true, () -> cfg.vanillaOffhandActions, v -> cfg.vanillaOffhandActions = v)
						.controller(TickBoxControllerBuilder::create)
						.build())
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.tenthslot.vanillaNonItemRightClick"))
						.description(OptionDescription.of(Component.translatable("config.tenthslot.vanillaNonItemRightClick.desc")))
						.binding(true, () -> cfg.vanillaNonItemRightClick, v -> cfg.vanillaNonItemRightClick = v)
						.controller(TickBoxControllerBuilder::create)
						.build())
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.tenthslot.vanillaLeftClick"))
						.description(OptionDescription.of(Component.translatable("config.tenthslot.vanillaLeftClick.desc")))
						.binding(true, () -> cfg.vanillaLeftClick, v -> cfg.vanillaLeftClick = v)
						.controller(TickBoxControllerBuilder::create)
						.build())
					.build())
				.save(cfg::save)
				.build()
				.generateScreen(parent);
		};
	}
}