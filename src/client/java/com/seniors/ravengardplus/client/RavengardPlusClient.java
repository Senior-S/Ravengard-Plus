package com.seniors.ravengardplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

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

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> AbilityCooldownTracker.onGameMessage(message.getString()));
		ClientTickEvents.START_CLIENT_TICK.register(AbilityCooldownTracker::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Iterable<ItemStack> candidates = null;
			AbstractContainerScreen<?> containerScreen = null;
			if (client.player != null) {
				containerScreen = ScreenCompat.get(client) instanceof AbstractContainerScreen<?> screen ? screen : null;
				candidates = containerScreen != null
						? containerScreen.getMenu().getItems()
						: client.player.getInventory().getNonEquipmentItems();
			}
			ArmorUpgradeDetector.update(client.player, candidates);
			AccessoryUpgradeDetector.update(client.player, candidates);
			CrownReplacementDetector.update(
					client.player,
					containerScreen == null ? null : containerScreen.getMenu(),
					RavengardConfig.HANDLER.instance().crownReplacementEnabled
			);

			while (openConfig.consumeClick()) {
				ScreenCompat.set(client, RavengardConfig.createScreen(ScreenCompat.get(client)));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
