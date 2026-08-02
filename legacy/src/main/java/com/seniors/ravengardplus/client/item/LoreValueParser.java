package com.seniors.ravengardplus.client.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class LoreValueParser {
	private LoreValueParser() {
	}

	public static double find(ItemStack stack, String label) {
		LoreComponent lore = stack.get(DataComponentTypes.LORE);
		if (lore == null) {
			return -1;
		}

		for (Text line : lore.lines()) {
			String text = line.getString();
			int labelStart = text.lastIndexOf(label);
			int labelEnd = labelStart + label.length();
			if (labelStart < 0
					|| labelEnd < text.length() && !Character.isWhitespace(text.codePointAt(labelEnd))) {
				continue;
			}

			int valueEnd = labelStart;
			while (valueEnd > 0) {
				int codePoint = text.codePointBefore(valueEnd);
				if (!Character.isWhitespace(codePoint)) {
					break;
				}
				valueEnd -= Character.charCount(codePoint);
			}

			int valueStart = valueEnd;
			while (valueStart > 0) {
				char character = text.charAt(valueStart - 1);
				if ((character < '0' || character > '9') && character != ',' && character != '.') {
					break;
				}
				valueStart--;
			}

			double value = 0;
			double decimalPlace = 0;
			boolean foundDigit = false;
			boolean valid = true;
			for (int index = valueStart; index < valueEnd; index++) {
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
					valid = false;
					break;
				}
			}

			if (valid && foundDigit) {
				return value;
			}
		}

		return -1;
	}
}
