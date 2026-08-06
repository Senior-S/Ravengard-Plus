package com.seniors.ravengardplus.client.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class CrownReplacementDetector {
	private static final Set<ItemStack> REJECTED_CONTAINER_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static ScreenHandler cachedHandler;
	private static int cachedRevision = Integer.MIN_VALUE;
	private static int cachedInventoryChanges = Integer.MIN_VALUE;
	private static ItemStack cheapestItem;

	private CrownReplacementDetector() {
	}

	public static void update(ClientPlayerEntity player, ScreenHandler handler, boolean enabled) {
		if (!enabled || player == null || handler == null || handler == player.playerScreenHandler) {
			clear();
			return;
		}

		PlayerInventory inventory = player.getInventory();
		int revision = handler.getRevision();
		int inventoryChanges = inventory.getChangeCount();
		if (handler == cachedHandler && revision == cachedRevision && inventoryChanges == cachedInventoryChanges) {
			return;
		}
		cachedHandler = handler;
		cachedRevision = revision;
		cachedInventoryChanges = inventoryChanges;
		cheapestItem = null;
		REJECTED_CONTAINER_ITEMS.clear();

		double cheapestValue = Double.POSITIVE_INFINITY;
		for (int index = 0; index < 36; index++) {
			if (index == 8 || index >= 9 && index <= 12) {
				continue;
			}

			ItemStack stack = inventory.getStack(index);
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
		for (Slot slot : handler.slots) {
			if (slot.inventory == inventory) {
				continue;
			}

			ItemStack stack = slot.getStack();
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
		cachedHandler = null;
		cachedRevision = Integer.MIN_VALUE;
		cachedInventoryChanges = Integer.MIN_VALUE;
		cheapestItem = null;
		REJECTED_CONTAINER_ITEMS.clear();
	}
}
