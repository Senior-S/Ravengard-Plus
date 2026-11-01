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
import java.util.function.Predicate;

public final class CrownReplacementDetector {
	private static final Set<ItemStack> REJECTED_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Set<ItemStack> MOST_VALUABLE_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static ItemStack cheapestItem;

	private CrownReplacementDetector() {
	}

	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled) {
		update(inventory, menu, enabled, false);
	}

	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled, boolean includeHealthItems) {
		update(inventory, menu, enabled, includeHealthItems, stack -> false);
	}

	// Health items are left out unless includeHealthItems is set, and items the player ignored are always
	// left out, so neither is offered for replacement
	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled, boolean includeHealthItems,
			Predicate<ItemStack> ignored) {
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
			if (!includeHealthItems && isHealthItem(stack) || ignored.test(stack)) {
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

	// Marks the highest-priced items of an open container (every one, on a tie), whether or not the
	// inventory is full, so the player knows what to take. The player's own slots are never marked.
	public static void updateMostValuable(Inventory inventory, AbstractContainerMenu menu, boolean enabled) {
		MOST_VALUABLE_CONTAINER_ITEMS.clear();
		if (!enabled || inventory == null || menu == null) {
			return;
		}

		double highest = -1;
		for (Slot slot : menu.slots) {
			if (slot.container == inventory) {
				continue;
			}
			ItemStack stack = slot.getItem();
			double value = LoreValueParser.find(stack, "Crown");
			if (value < 0 || value < highest) {
				continue;
			}
			if (value > highest) {
				MOST_VALUABLE_CONTAINER_ITEMS.clear();
				highest = value;
			}
			MOST_VALUABLE_CONTAINER_ITEMS.add(stack);
		}
	}

	public static boolean isMostValuableContainerItem(ItemStack stack) {
		return MOST_VALUABLE_CONTAINER_ITEMS.contains(stack);
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
