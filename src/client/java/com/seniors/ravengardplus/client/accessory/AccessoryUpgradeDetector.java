package com.seniors.ravengardplus.client.accessory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class AccessoryUpgradeDetector {
	private static final String MODEL_NAMESPACE = "hypixel_ravengard";
	private static final String MODEL_PREFIX = "item/accessories/";
	private static final ItemStack[] BEST_UPGRADES = new ItemStack[4];
	private static final double[] BEST_IMPROVEMENTS = new double[4];

	private AccessoryUpgradeDetector() {
	}

	public static void update(LocalPlayer player, Iterable<ItemStack> candidates) {
		Arrays.fill(BEST_UPGRADES, null);
		Arrays.fill(BEST_IMPROVEMENTS, Double.NEGATIVE_INFINITY);
		if (player == null) {
			return;
		}

		Inventory inventory = player.getInventory();
		Map<String, Double> neckBuffs = equippedBuffs(inventory, 9);
		Map<String, Double> earringsBuffs = equippedBuffs(inventory, 10);
		Map<String, Double> beltBuffs = equippedBuffs(inventory, 11);
		Map<String, Double> ringBuffs = equippedBuffs(inventory, 12);

		for (ItemStack candidate : candidates) {
			int slot = accessorySlot(candidate);
			if (slot < 0) {
				continue;
			}

			Map<String, Double> candidateBuffs = buffs(candidate);
			if (candidateBuffs.isEmpty()) {
				continue;
			}

			int type = slot - 9;
			Map<String, Double> currentBuffs = switch (type) {
				case 0 -> neckBuffs;
				case 1 -> earringsBuffs;
				case 2 -> beltBuffs;
				default -> ringBuffs;
			};
			double improvement = 0;
			boolean valid = true;

			for (Map.Entry<String, Double> buff : currentBuffs.entrySet()) {
				double candidateValue = candidateBuffs.getOrDefault(buff.getKey(), 0.0);
				if (candidateValue < buff.getValue()) {
					valid = false;
					break;
				}
				if (!candidateBuffs.containsKey(buff.getKey()) && candidateValue > buff.getValue()) {
					improvement += candidateValue - buff.getValue();
				}
			}
			if (!valid) {
				continue;
			}

			for (Map.Entry<String, Double> buff : candidateBuffs.entrySet()) {
				double currentValue = currentBuffs.getOrDefault(buff.getKey(), 0.0);
				if (buff.getValue() < currentValue) {
					valid = false;
					break;
				}
				if (buff.getValue() > currentValue) {
					improvement += buff.getValue() - currentValue;
				}
			}

			if (valid && improvement > 0 && improvement > BEST_IMPROVEMENTS[type]) {
				BEST_UPGRADES[type] = candidate;
				BEST_IMPROVEMENTS[type] = improvement;
			}
		}
	}

	public static boolean isUpgrade(ItemStack candidate) {
		for (ItemStack bestUpgrade : BEST_UPGRADES) {
			if (candidate == bestUpgrade) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, Double> equippedBuffs(Inventory inventory, int slot) {
		ItemStack equipped = inventory.getItem(slot);
		return accessorySlot(equipped) == slot ? buffs(equipped) : Map.of();
	}

	private static int accessorySlot(ItemStack stack) {
		Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);
		if (itemModel == null || !MODEL_NAMESPACE.equals(itemModel.getNamespace())) {
			return -1;
		}

		String path = itemModel.getPath();
		if (path.endsWith("_greyed")) {
			return -1;
		}
		if (path.startsWith(MODEL_PREFIX + "neck")) {
			return 9;
		}
		if (path.startsWith(MODEL_PREFIX + "earrings")) {
			return 10;
		}
		if (path.startsWith(MODEL_PREFIX + "belts")) {
			return 11;
		}
		if (path.startsWith(MODEL_PREFIX + "rings")) {
			return 12;
		}
		return -1;
	}

	private static Map<String, Double> buffs(ItemStack stack) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return Map.of();
		}

		Map<String, Double> buffs = new HashMap<>(2);
		for (Component line : lore.lines()) {
			String text = line.getString();
			int tokenStart = 0;
			while (tokenStart < text.length() && Character.isWhitespace(text.codePointAt(tokenStart))) {
				tokenStart = text.offsetByCodePoints(tokenStart, 1);
			}
			if (tokenStart >= text.length()) {
				continue;
			}

			int numberStart = text.offsetByCodePoints(tokenStart, 1);
			while (numberStart < text.length()) {
				int codePoint = text.codePointAt(numberStart);
				if (codePoint != 0xFE0F && Character.getType(codePoint) != Character.NON_SPACING_MARK) {
					break;
				}
				numberStart = text.offsetByCodePoints(numberStart, 1);
			}

			int index = numberStart;
			boolean negative = false;
			if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) {
				negative = text.charAt(index) == '-';
				index++;
			}

			double value = 0;
			double decimalPlace = 0;
			boolean foundDigit = false;
			while (index < text.length()) {
				char character = text.charAt(index);
				if (character >= '0' && character <= '9') {
					if (decimalPlace == 0) {
						value = value * 10 + character - '0';
					} else {
						value += (character - '0') * decimalPlace;
						decimalPlace /= 10;
					}
					foundDigit = true;
				} else if (character == ',' && decimalPlace == 0) {
				} else if (character == '.' && decimalPlace == 0) {
					decimalPlace = 0.1;
				} else {
					break;
				}
				index++;
			}

			if (!foundDigit) {
				continue;
			}
			if (index < text.length() && text.charAt(index) == '%') {
				index++;
			}
			if (index >= text.length() || !Character.isWhitespace(text.codePointAt(index))) {
				continue;
			}
			while (index < text.length() && Character.isWhitespace(text.codePointAt(index))) {
				index = text.offsetByCodePoints(index, 1);
			}
			if (index >= text.length()) {
				continue;
			}

			String name = text.substring(index).stripTrailing();
			if (!name.equals("Crown") && !name.equals("Crowns")) {
				buffs.put(name, negative ? -value : value);
			}
		}

		return buffs;
	}
}
