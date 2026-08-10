package com.seniors.ravengardplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = RavengardPlusClient.MOD_ID, dist = Dist.CLIENT)
public class RavengardPlusClient {
	public static final String MOD_ID = "ravengardplus";

	public RavengardPlusClient(IEventBus modEventBus, ModContainer container) {
		RavengardConfig.HANDLER.load();

		KeyMapping.Category category = new KeyMapping.Category(id("general"));
		KeyMapping openConfig = new KeyMapping(
				"key.ravengard-plus.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_O,
				category
		);

		modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.registerCategory(category);
			event.register(openConfig);
		});
		container.registerExtensionPoint(
				IConfigScreenFactory.class,
				(mod, parent) -> RavengardConfig.createScreen(parent)
		);

		NeoForge.EVENT_BUS.addListener((ClientChatReceivedEvent.System event) ->
				AbilityCooldownTracker.onGameMessage(event.getMessage().getString()));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) ->
				AbilityCooldownTracker.tick(Minecraft.getInstance()));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
			Minecraft client = Minecraft.getInstance();
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

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
