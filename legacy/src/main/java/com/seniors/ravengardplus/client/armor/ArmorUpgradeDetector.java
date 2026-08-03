package com.seniors.ravengardplus.client.armor;

import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.EnumMap;

public final class ArmorUpgradeDetector {
	private static final EnumMap<EquipmentSlot, ItemStack> BEST_UPGRADES = new EnumMap<>(EquipmentSlot.class);
	private static final EnumMap<EquipmentSlot, Double> BEST_DEFENSE = new EnumMap<>(EquipmentSlot.class);

	private ArmorUpgradeDetector() {
	}

	public static void update(ClientPlayerEntity player, Iterable<ItemStack> candidates) {
		BEST_UPGRADES.clear();
		BEST_DEFENSE.clear();
		if (player == null) {
			return;
		}

		for (ItemStack candidate : candidates) {
			EquippableComponent equippable = candidate.get(DataComponentTypes.EQUIPPABLE);
			if (equippable == null) {
				continue;
			}

			EquipmentSlot slot = equippable.slot();
			if (slot != EquipmentSlot.HEAD
					&& slot != EquipmentSlot.CHEST
					&& slot != EquipmentSlot.LEGS
					&& slot != EquipmentSlot.FEET) {
				continue;
			}

			Identifier itemModel = candidate.get(DataComponentTypes.ITEM_MODEL);
			if (itemModel != null && itemModel.getPath().endsWith("_greyed")) {
				continue;
			}

			double candidateDefense = LoreValueParser.find(candidate, "Defense");
			double equippedDefense = LoreValueParser.find(player.getEquippedStack(slot), "Defense");
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
