package com.seniors.ravengardplus.client;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.InputConstants;
import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.debug.EntityInspector;
import com.seniors.ravengardplus.client.door.DoorTracker;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import com.seniors.ravengardplus.client.portal.ExitPortalTracker;
import com.seniors.ravengardplus.client.team.TeammateGlowTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Inventory;

public class RavengardPlusClient implements ClientModInitializer {
	public static final String MOD_ID = "ravengard-plus";

	@Override
	public void onInitializeClient() {
		RavengardConfig.HANDLER.load();

		KeyMapping openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.ravengard-plus.open_config",
				InputConstants.KEY_O,
				KeyMapping.Category.register(id("general"))
		));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
				ClientCommands.literal("rgp").then(ClientCommands.literal("entity").executes(command -> {
					EntityInspector.inspect(command.getSource().getClient(), command.getSource()::sendFeedback);
					return 1;
				}))
		));
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> AbilityCooldownTracker.onGameMessage(message.getString()));
		ClientTickEvents.START_CLIENT_TICK.register(AbilityCooldownTracker::tick);
		ClientTickEvents.END_CLIENT_TICK.register(MinimapReader::tick);
		ClientTickEvents.END_CLIENT_TICK.register(TeammateGlowTracker::tick);
		ClientTickEvents.END_CLIENT_TICK.register(DoorTracker::tick);
		ClientTickEvents.END_CLIENT_TICK.register(ExitPortalTracker::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			RavengardConfig config = RavengardConfig.HANDLER.instance();
			Inventory inventory = client.player == null ? null : client.player.getInventory();
			Iterable<ItemStack> candidates = null;
			AbstractContainerScreen<?> containerScreen = null;
			if (client.player != null) {
				containerScreen = ScreenCompat.get(client) instanceof AbstractContainerScreen<?> screen ? screen : null;
				if (config.armorUpgradeIndicatorEnabled || config.accessoryUpgradeIndicatorEnabled) {
					candidates = containerScreen != null
							? Iterables.transform(containerScreen.getMenu().slots, Slot::getItem)
							: inventory.getNonEquipmentItems();
				}
			}
			ArmorUpgradeDetector.update(client.player, candidates, config.armorUpgradeIndicatorEnabled);
			AccessoryUpgradeDetector.update(inventory, candidates, config.accessoryUpgradeIndicatorEnabled);
			CrownReplacementDetector.update(
					inventory,
					containerScreen == null || containerScreen.getMenu() == client.player.inventoryMenu
							? null : containerScreen.getMenu(),
					config.crownReplacementEnabled,
					config.crownReplacementIncludeHealthItems
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
