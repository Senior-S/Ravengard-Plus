package com.seniors.ravengardplus.client.armor;

import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ArmorUpgradeDetector {
	private ArmorUpgradeDetector() {
	}

	public static boolean isUpgrade(ItemStack candidate) {
		EquippableComponent equippable = candidate.get(DataComponentTypes.EQUIPPABLE);
		if (equippable == null) {
			return false;
		}

		EquipmentSlot slot = equippable.slot();
		if (slot != EquipmentSlot.HEAD
				&& slot != EquipmentSlot.CHEST
				&& slot != EquipmentSlot.LEGS
				&& slot != EquipmentSlot.FEET) {
			return false;
		}

		Identifier itemModel = candidate.get(DataComponentTypes.ITEM_MODEL);
		if (itemModel != null && itemModel.getPath().endsWith("_greyed")) {
			return false;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			return false;
		}

		double candidateDefense = LoreValueParser.find(candidate, "Defense");
		double equippedDefense = LoreValueParser.find(player.getEquippedStack(slot), "Defense");
		return candidateDefense >= 0 && candidateDefense > equippedDefense;
	}
}
