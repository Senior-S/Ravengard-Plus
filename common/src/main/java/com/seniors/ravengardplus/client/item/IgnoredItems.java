package com.seniors.ravengardplus.client.item;

import com.seniors.ravengardplus.client.ScreenCompat;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Items the player chose to keep out of the replacement highlight. Ravengard items are mostly the same
// vanilla item with a custom name, so an item is remembered by its type plus that name.
public final class IgnoredItems {
	private static KeyMapping key;

	private IgnoredItems() {
	}

	public static void setKey(KeyMapping mapping) {
		key = mapping;
	}

	// Toggles the hovered item when the ignore key is pressed in a container screen. The item's tooltip
	// shows the new state right away, so there's no separate message.
	public static boolean handleKey(KeyEvent event, Slot hovered) {
		if (!available() || !key.matches(event) || hovered == null || !hovered.hasItem()) {
			return false;
		}
		ItemStack stack = hovered.getItem();
		if (!hasPrice(stack)) {
			return false;
		}

		List<String> ignored = RavengardConfig.HANDLER.instance().crownReplacementIgnoredItems;
		String id = id(stack);
		if (!ignored.remove(id)) {
			ignored.add(id);
		}
		RavengardConfig.HANDLER.save();
		return true;
	}

	// Tells the player about the ignore key, and whether the item is ignored, on priced items in containers
	public static void appendTooltip(ItemStack stack, List<Component> lines) {
		if (!available() || !hasPrice(stack)
				|| !(ScreenCompat.get(Minecraft.getInstance()) instanceof AbstractContainerScreen<?>)) {
			return;
		}
		Component keyName = key.getTranslatedKeyMessage();
		lines.add(isIgnored(stack)
				? Component.translatable("ravengard-plus.ignored_item.ignored", keyName).withStyle(ChatFormatting.GOLD)
				: Component.translatable("ravengard-plus.ignored_item.hint", keyName).withStyle(ChatFormatting.DARK_GRAY));
	}

	public static boolean isIgnored(ItemStack stack) {
		List<String> ignored = RavengardConfig.HANDLER.instance().crownReplacementIgnoredItems;
		return !ignored.isEmpty() && !stack.isEmpty() && ignored.contains(id(stack));
	}

	private static boolean available() {
		return key != null && !key.isUnbound() && RavengardConfig.HANDLER.instance().crownReplacementEnabled;
	}

	private static boolean hasPrice(ItemStack stack) {
		return LoreValueParser.find(stack, "Crown") >= 0;
	}

	static String id(ItemStack stack) {
		String type = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		Component name = stack.get(DataComponents.CUSTOM_NAME);
		return name == null ? type : type + "|" + name.getString();
	}
}
