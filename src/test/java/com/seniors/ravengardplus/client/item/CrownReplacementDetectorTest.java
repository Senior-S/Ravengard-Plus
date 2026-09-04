package com.seniors.ravengardplus.client.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrownReplacementDetectorTest {
	private Inventory inventory;
	private ChestMenu chest;

	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@BeforeEach
	void createInventory() {
		inventory = new Inventory(null, new EntityEquipment());
		chest = ChestMenu.oneRow(1, inventory);
	}

	@Test
	void doesNotHighlightAPartiallyFilledInventory() {
		ItemStack carried = crowns(5);
		inventory.setItem(0, carried);
		chest.getSlot(0).set(crowns(10));
		CrownReplacementDetector.update(inventory, chest, true);
		assertFalse(CrownReplacementDetector.isCheapest(carried));
	}

	@Test
	void respectsReservedSlotsAndRefreshesWithoutChangeCounters() {
		for (int slot = 0; slot < 36; slot++) {
			if (slot != 8 && (slot < 9 || slot > 12)) {
				inventory.setItem(slot, crowns(20));
			}
		}
		ItemStack cheap = crowns(5);
		ItemStack offer = crowns(10);
		ItemStack rejected = crowns(5);
		inventory.setItem(0, cheap);
		chest.getSlot(0).set(offer);
		chest.getSlot(1).set(rejected);
		CrownReplacementDetector.update(inventory, chest, true);
		assertTrue(CrownReplacementDetector.isCheapest(cheap));
		assertFalse(CrownReplacementDetector.isRejectedContainerItem(offer));
		assertTrue(CrownReplacementDetector.isRejectedContainerItem(rejected));

		int changes = inventory.getTimesChanged();
		int state = chest.getStateId();
		inventory.setItem(0, crowns(30));
		assertEquals(changes, inventory.getTimesChanged());
		assertEquals(state, chest.getStateId());
		CrownReplacementDetector.update(inventory, chest, true);
		assertFalse(CrownReplacementDetector.isCheapest(cheap));
		assertTrue(CrownReplacementDetector.isRejectedContainerItem(offer));

		CrownReplacementDetector.update(inventory, chest, false);
		assertFalse(CrownReplacementDetector.isRejectedContainerItem(offer));
	}

	@Test
	void clearsTheHighlightWhenAnInventorySlotBecomesEmpty() {
		for (int slot = 0; slot < 36; slot++) {
			inventory.setItem(slot, crowns(5));
		}
		ItemStack cheap = inventory.getItem(0);
		chest.getSlot(0).set(crowns(10));
		CrownReplacementDetector.update(inventory, chest, true);
		assertTrue(CrownReplacementDetector.isCheapest(cheap));
		inventory.setItem(35, ItemStack.EMPTY);
		CrownReplacementDetector.update(inventory, chest, true);
		assertFalse(CrownReplacementDetector.isCheapest(cheap));
	}

	private static ItemStack crowns(int value) {
		ItemStack stack = new ItemStack(Holder.direct(Items.PAPER, DataComponentMap.EMPTY));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(value + " Crowns"))));
		return stack;
	}
}
