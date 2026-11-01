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
import com.seniors.ravengardplus.client.item.IgnoredItems;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import com.seniors.ravengardplus.client.ping.PingWheel;
import com.seniors.ravengardplus.client.portal.ExitPortalTracker;
import com.seniors.ravengardplus.client.team.TeammateGlowTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = RavengardPlusClient.MOD_ID, dist = Dist.CLIENT)
public class RavengardPlusClient {
	public static final String MOD_ID = "ravengardplus";

	public RavengardPlusClient(IEventBus modEventBus, ModContainer container) {
		RavengardConfig.HANDLER.load();

		KeyMapping.Category category = new KeyMapping.Category(id("general"));
		KeyMapping openConfig = new KeyMapping(
				"key.ravengard-plus.open_config",
				InputConstants.KEY_O,
				category
		);
		KeyMapping ignoreItem = new KeyMapping(
				"key.ravengard-plus.ignore_item",
				InputConstants.KEY_H,
				category
		);
		IgnoredItems.setKey(ignoreItem);
		KeyMapping ping = new KeyMapping(
				"key.ravengard-plus.ping",
				InputConstants.Type.MOUSE,
				InputConstants.MOUSE_BUTTON_MIDDLE,
				category
		);
		PingWheel.setKey(ping);

		modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.registerCategory(category);
			event.register(openConfig);
			event.register(ignoreItem);
			event.register(ping);
		});
		container.registerExtensionPoint(
				IConfigScreenFactory.class,
				(mod, parent) -> RavengardConfig.createScreen(parent)
		);

		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
				Commands.literal("rgp").then(Commands.literal("entity").executes(command -> {
					EntityInspector.inspect(Minecraft.getInstance(), command.getSource()::sendSystemMessage);
					return 1;
				}))
		));
		NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) -> IgnoredItems.appendTooltip(event.getItemStack(), event.getToolTip()));
		NeoForge.EVENT_BUS.addListener((ClientChatReceivedEvent.System event) ->
				AbilityCooldownTracker.onGameMessage(event.getMessage().getString()));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) ->
				AbilityCooldownTracker.tick(Minecraft.getInstance()));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
			MinimapReader.tick(Minecraft.getInstance());
			TeammateGlowTracker.tick(Minecraft.getInstance());
			DoorTracker.tick(Minecraft.getInstance());
			ExitPortalTracker.tick(Minecraft.getInstance());
			PingWheel.tick(Minecraft.getInstance());
		});
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
			Minecraft client = Minecraft.getInstance();
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
			AbstractContainerMenu openContainer = containerScreen == null || containerScreen.getMenu() == client.player.inventoryMenu
					? null : containerScreen.getMenu();
			CrownReplacementDetector.updateMostValuable(inventory, openContainer, config.mostValuableHighlightEnabled);
			CrownReplacementDetector.update(
					inventory,
					openContainer,
					config.crownReplacementEnabled,
					config.crownReplacementIncludeHealthItems,
					IgnoredItems::isIgnored
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
