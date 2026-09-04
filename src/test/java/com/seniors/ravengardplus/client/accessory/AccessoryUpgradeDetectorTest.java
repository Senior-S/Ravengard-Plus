package com.seniors.ravengardplus.client.accessory;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccessoryUpgradeDetectorTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void preservesBuffsAndTreatsRemovingAPenaltyAsAnImprovement() {
		Inventory inventory = new Inventory(null, new EntityEquipment());
		inventory.setItem(9, necklace("\u2663+10 Health", "\u2663-2 Speed"));
		ItemStack upgrade = necklace("\u2663+10 Health");
		ItemStack worsePenalty = necklace("\u2663+20 Health", "\u2663-3 Speed");
		ItemStack missingHealth = necklace("\u2663+10 Speed");
		AccessoryUpgradeDetector.update(inventory, List.of(upgrade, worsePenalty, missingHealth), true);
		assertTrue(AccessoryUpgradeDetector.isUpgrade(upgrade));
		assertFalse(AccessoryUpgradeDetector.isUpgrade(worsePenalty));
		assertFalse(AccessoryUpgradeDetector.isUpgrade(missingHealth));
	}

	@Test
	void refreshesCachedBuffsAndClearsResultsWhenDisabled() {
		Inventory inventory = new Inventory(null, new EntityEquipment());
		inventory.setItem(9, necklace("\u2663+0.29 Health"));
		ItemStack candidate = necklace("\u2663+0.30 Health");
		AccessoryUpgradeDetector.update(inventory, List.of(candidate), true);
		assertTrue(AccessoryUpgradeDetector.isUpgrade(candidate));
		candidate.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("\u2663+0.28 Health"))));
		AccessoryUpgradeDetector.update(inventory, List.of(candidate), true);
		assertFalse(AccessoryUpgradeDetector.isUpgrade(candidate));
		candidate.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("\u2663+0.40 Health"))));
		AccessoryUpgradeDetector.update(inventory, List.of(candidate), true);
		assertTrue(AccessoryUpgradeDetector.isUpgrade(candidate));
		AccessoryUpgradeDetector.update(inventory, null, false);
		assertFalse(AccessoryUpgradeDetector.isUpgrade(candidate));
	}

	@Test
	void ignoresUnavailableModelsAndFindsTheStrongestUpgrade() {
		Inventory inventory = new Inventory(null, new EntityEquipment());
		inventory.setItem(9, necklace("\u2663+10 Health"));
		ItemStack lesser = necklace("\u2663+11 Health");
		ItemStack best = necklace("\u2663+15 Health", "\u2663+1 Speed");
		ItemStack unavailable = necklace("\u2663+100 Health");
		unavailable.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(
				"hypixel_ravengard", "item/accessories/neck_greyed"));
		AccessoryUpgradeDetector.update(inventory, List.of(lesser, unavailable, best), true);
		assertFalse(AccessoryUpgradeDetector.isUpgrade(lesser));
		assertFalse(AccessoryUpgradeDetector.isUpgrade(unavailable));
		assertTrue(AccessoryUpgradeDetector.isUpgrade(best));
	}

	private static ItemStack necklace(String... buffs) {
		ItemStack stack = new ItemStack(Holder.direct(Items.PAPER, DataComponentMap.EMPTY));
		stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("hypixel_ravengard", "item/accessories/neck"));
		stack.set(DataComponents.LORE, new ItemLore(Arrays.stream(buffs).<Component>map(Component::literal).toList()));
		return stack;
	}
}
