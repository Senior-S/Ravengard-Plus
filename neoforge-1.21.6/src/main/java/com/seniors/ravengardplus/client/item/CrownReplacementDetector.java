package com.seniors.ravengardplus.client.item;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class CrownReplacementDetector {
	private static final Set<ItemStack> REJECTED_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static AbstractContainerMenu cachedMenu;
	private static int cachedMenuState = Integer.MIN_VALUE;
	private static int cachedInventoryChanges = Integer.MIN_VALUE;
	private static ItemStack cheapestItem;

	private CrownReplacementDetector() {
	}

	public static void update(LocalPlayer player, AbstractContainerMenu menu, boolean enabled) {
		if (!enabled || player == null || menu == null || menu == player.inventoryMenu) {
			clear();
			return;
		}

		Inventory inventory = player.getInventory();
		int menuState = menu.getStateId();
		int inventoryChanges = inventory.getTimesChanged();
		if (menu == cachedMenu && menuState == cachedMenuState && inventoryChanges == cachedInventoryChanges) {
			return;
		}
		cachedMenu = menu;
		cachedMenuState = menuState;
		cachedInventoryChanges = inventoryChanges;
		cheapestItem = null;
		REJECTED_CONTAINER_ITEMS.clear();

		double cheapestValue = Double.POSITIVE_INFINITY;
		for (int index = 0; index < 36; index++) {
			if (index == 8 || index >= 9 && index <= 12) {
				continue;
			}

			ItemStack stack = inventory.getItem(index);
			if (stack.isEmpty()) {
				return;
			}
			double value = LoreValueParser.find(stack, "Crown");
			if (value >= 0 && value < cheapestValue) {
				cheapestItem = stack;
				cheapestValue = value;
			}
		}
		if (cheapestItem == null) {
			return;
		}

		boolean hasBetterItem = false;
		for (Slot slot : menu.slots) {
			if (slot.container == inventory) {
				continue;
			}

			ItemStack stack = slot.getItem();
			double value = LoreValueParser.find(stack, "Crown");
			if (value > cheapestValue) {
				hasBetterItem = true;
			} else if (value >= 0) {
				REJECTED_CONTAINER_ITEMS.add(stack);
			}
		}
		if (!hasBetterItem) {
			cheapestItem = null;
		}
	}

	public static boolean isCheapest(ItemStack stack) {
		return stack == cheapestItem;
	}

	public static boolean isRejectedContainerItem(ItemStack stack) {
		return REJECTED_CONTAINER_ITEMS.contains(stack);
	}

	private static void clear() {
		cachedMenu = null;
		cachedMenuState = Integer.MIN_VALUE;
		cachedInventoryChanges = Integer.MIN_VALUE;
		cheapestItem = null;
		REJECTED_CONTAINER_ITEMS.clear();
	}
}
