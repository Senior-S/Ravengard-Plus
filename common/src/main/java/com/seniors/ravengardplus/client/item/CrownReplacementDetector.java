package com.seniors.ravengardplus.client.item;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class CrownReplacementDetector {
	private static final Set<ItemStack> REJECTED_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static ItemStack cheapestItem;

	private CrownReplacementDetector() {
	}

	public static void update(Inventory inventory, AbstractContainerMenu menu, boolean enabled) {
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

	public static boolean isCheapest(ItemStack stack) {
		return stack == cheapestItem;
	}

	public static boolean isRejectedContainerItem(ItemStack stack) {
		return REJECTED_CONTAINER_ITEMS.contains(stack);
	}
}
