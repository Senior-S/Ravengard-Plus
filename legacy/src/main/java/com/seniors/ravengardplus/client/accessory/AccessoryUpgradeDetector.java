package com.seniors.ravengardplus.client.accessory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class AccessoryUpgradeDetector {
	private static final String MODEL_NAMESPACE = "hypixel_ravengard";
	private static final String MODEL_PREFIX = "item/accessories/";

	private AccessoryUpgradeDetector() {
	}

	public static boolean isUpgrade(ItemStack candidate) {
		int slot = accessorySlot(candidate);
		if (slot < 0) {
			return false;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			return false;
		}

		ItemStack equipped = player.getInventory().getStack(slot);
		Map<String, Double> candidateBuffs = buffs(candidate);
		Map<String, Double> equippedBuffs = accessorySlot(equipped) == slot ? buffs(equipped) : Map.of();
		if (candidateBuffs.isEmpty()) {
			return false;
		}

		boolean improved = false;
		for (Map.Entry<String, Double> buff : candidateBuffs.entrySet()) {
			double equippedValue = equippedBuffs.getOrDefault(buff.getKey(), 0.0);
			if (buff.getValue() < equippedValue) {
				return false;
			}
			improved |= buff.getValue() > equippedValue;
		}

		for (Map.Entry<String, Double> buff : equippedBuffs.entrySet()) {
			double candidateValue = candidateBuffs.getOrDefault(buff.getKey(), 0.0);
			if (candidateValue < buff.getValue()) {
				return false;
			}
			improved |= candidateValue > buff.getValue();
		}

		return improved;
	}

	private static int accessorySlot(ItemStack stack) {
		Identifier itemModel = stack.get(DataComponentTypes.ITEM_MODEL);
		if (itemModel == null || !MODEL_NAMESPACE.equals(itemModel.getNamespace())) {
			return -1;
		}

		String path = itemModel.getPath();
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
		LoreComponent lore = stack.get(DataComponentTypes.LORE);
		if (lore == null) {
			return Map.of();
		}

		Map<String, Double> buffs = new HashMap<>(2);
		for (Text line : lore.lines()) {
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
			if (!name.equals("Crowns")) {
				buffs.put(name, negative ? -value : value);
			}
		}

		return buffs;
	}
}
