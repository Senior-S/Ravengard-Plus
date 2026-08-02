package com.seniors.ravengardplus.client.armor;

import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class ArmorUpgradeDetector {
	private ArmorUpgradeDetector() {
	}

	public static boolean isUpgrade(ItemStack candidate) {
		Equippable equippable = candidate.get(DataComponents.EQUIPPABLE);
		if (equippable == null || !equippable.slot().isArmor()) {
			return false;
		}

		Identifier itemModel = candidate.get(DataComponents.ITEM_MODEL);
		if (itemModel != null && itemModel.getPath().endsWith("_greyed")) {
			return false;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}

		long candidateDefense = LoreValueParser.find(candidate, "Defense", true);
		long equippedDefense = LoreValueParser.find(player.getItemBySlot(equippable.slot()), "Defense", true);
		return candidateDefense >= 0 && candidateDefense > equippedDefense;
	}
}
