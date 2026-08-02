package com.seniors.ravengardplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class RavengardPlusClient implements ClientModInitializer {
	public static final String MOD_ID = "ravengard-plus";

	@Override
	public void onInitializeClient() {
		RavengardConfig.HANDLER.load();

		KeyMapping openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.ravengard-plus.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_O,
				KeyMapping.Category.register(id("general"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfig.consumeClick()) {
				client.gui.setScreen(RavengardConfig.createScreen(client.gui.screen()));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
