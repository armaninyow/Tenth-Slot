package com.armaninyow.tenthslot;

// 26.1.x: Replaced cloth-config/AutoConfig with YACL 3.x.
// Config values are plain public fields; YACL reads/writes them via Binding.
// Persistence is handled manually in load()/save() using a simple JSON file
// in the default config directory, since YACL itself is purely a GUI library.
public class TenthSlotConfig {

	public boolean showOnlyWhenHotbarFull = false;
	public boolean vanillaOffhandActions = true;
	public boolean vanillaNonItemRightClick = true;
	public boolean vanillaLeftClick = true;

	// --- Singleton ---

	private static TenthSlotConfig INSTANCE = null;

	public static TenthSlotConfig get() {
		if (INSTANCE == null) {
			INSTANCE = new TenthSlotConfig();
			INSTANCE.load();
		}
		return INSTANCE;
	}

	// --- Persistence (simple JSON via Gson, already on the classpath via MC) ---

	private static java.io.File configFile() {
		// fabric loader exposes the config dir via FabricLoader.getInstance().getConfigDir()
		return net.fabricmc.loader.api.FabricLoader.getInstance()
			.getConfigDir()
			.resolve(TenthSlot.MOD_ID + ".json")
			.toFile();
	}

	public void load() {
		java.io.File file = configFile();
		if (!file.exists()) return;
		try (java.io.FileReader reader = new java.io.FileReader(file)) {
			TenthSlotConfig loaded = new com.google.gson.Gson().fromJson(reader, TenthSlotConfig.class);
			if (loaded != null) {
				this.showOnlyWhenHotbarFull = loaded.showOnlyWhenHotbarFull;
				this.vanillaOffhandActions  = loaded.vanillaOffhandActions;
				this.vanillaNonItemRightClick = loaded.vanillaNonItemRightClick;
				this.vanillaLeftClick       = loaded.vanillaLeftClick;
			}
		} catch (Exception e) {
			TenthSlot.LOGGER.error("Failed to load config", e);
		}
	}

	public void save() {
		java.io.File file = configFile();
		try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
			new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
		} catch (Exception e) {
			TenthSlot.LOGGER.error("Failed to save config", e);
		}
	}
}