package com.seniors.ravengardplus.client.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class CrownReplacementDetector {
	private static final Set<ItemStack> REJECTED_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static ItemStack cheapestItem;

	private CrownReplacementDetector() {
	}

	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled) {
		update(inventory, menu, enabled, false);
	}

	// Health items are left out unless includeHealthItems is set, so they are never offered for replacement
	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled, boolean includeHealthItems) {
		cheapestItem = null;
		REJECTED_CONTAINER_ITEMS.clear();
		if (!enabled || inventory == null || menu == null) {
			return;
		}

		ItemStack candidate = null;
		double cheapestValue = Double.POSITIVE_INFINITY;
		for (int index = 0; index < 36; index++) {
			if (index == 8 || index >= 9 && index <= 12) {
				continue;
			}

			ItemStack stack = inventory.getItem(index);
			if (stack.isEmpty()) {
				return;
			}
			if (!includeHealthItems && isHealthItem(stack)) {
				continue;
			}
			double value = LoreValueParser.find(stack, "Crown");
			if (value >= 0 && value < cheapestValue) {
				candidate = stack;
				cheapestValue = value;
			}
		}
		if (candidate == null) {
			return;
		}

		boolean hasBetterItem = false;
		for (Slot slot : menu.slots) {
			if (slot.container == inventory) {
				continue;
			}

			ItemStack stack = slot.getItem();
			if (!includeHealthItems && isHealthItem(stack)) {
				continue;
			}
			double value = LoreValueParser.find(stack, "Crown");
			if (value > cheapestValue) {
				hasBetterItem = true;
			} else if (value >= 0) {
				REJECTED_CONTAINER_ITEMS.add(stack);
			}
		}
		if (hasBetterItem) {
			cheapestItem = candidate;
		}
	}

	// Healing consumables describe themselves as "Heals +50 HP over 5 seconds."
	static boolean isHealthItem(ItemStack stack) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return false;
		}
		for (Component line : lore.lines()) {
			String text = line.getString();
			if (text.contains("Heals") && text.contains("HP")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCheapest(ItemStack stack) {
		return stack == cheapestItem;
	}

	public static boolean isRejectedContainerItem(ItemStack stack) {
		return REJECTED_CONTAINER_ITEMS.contains(stack);
	}
}
