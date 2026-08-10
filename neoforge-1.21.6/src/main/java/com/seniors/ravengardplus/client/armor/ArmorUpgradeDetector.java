package com.seniors.ravengardplus.client.armor;

import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import java.util.EnumMap;

public final class ArmorUpgradeDetector {
	private static final EnumMap<EquipmentSlot, ItemStack> BEST_UPGRADES = new EnumMap<>(EquipmentSlot.class);
	private static final EnumMap<EquipmentSlot, Double> BEST_DEFENSE = new EnumMap<>(EquipmentSlot.class);

	private ArmorUpgradeDetector() {
	}

	public static void update(LocalPlayer player, Iterable<ItemStack> candidates) {
		BEST_UPGRADES.clear();
		BEST_DEFENSE.clear();
		if (player == null) {
			return;
		}

		for (ItemStack candidate : candidates) {
			Equippable equippable = candidate.get(DataComponents.EQUIPPABLE);
			if (equippable == null || !equippable.slot().isArmor()) {
				continue;
			}

			ResourceLocation itemModel = candidate.get(DataComponents.ITEM_MODEL);
			if (itemModel != null && itemModel.getPath().endsWith("_greyed")) {
				continue;
			}

			EquipmentSlot slot = equippable.slot();
			double candidateDefense = LoreValueParser.find(candidate, "Defense");
			double equippedDefense = LoreValueParser.find(player.getItemBySlot(slot), "Defense");
			double bestDefense = BEST_DEFENSE.getOrDefault(slot, equippedDefense);
			if (candidateDefense >= 0 && candidateDefense > bestDefense) {
				BEST_UPGRADES.put(slot, candidate);
				BEST_DEFENSE.put(slot, candidateDefense);
			}
		}
	}

	public static boolean isUpgrade(ItemStack candidate) {
		for (ItemStack bestUpgrade : BEST_UPGRADES.values()) {
			if (candidate == bestUpgrade) {
				return true;
			}
		}
		return false;
	}
}
