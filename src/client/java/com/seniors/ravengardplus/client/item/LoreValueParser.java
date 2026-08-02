package com.seniors.ravengardplus.client.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public final class LoreValueParser {
	private LoreValueParser() {
	}

	public static long find(ItemStack stack, String label, boolean skipFirstGlyph) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return -1;
		}

		for (Component line : lore.lines()) {
			String text = line.getString();
			int labelStart = text.length() - label.length();
			if (labelStart < 0 || !text.regionMatches(labelStart, label, 0, label.length())) {
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
			if (skipFirstGlyph) {
				while (valueStart > 0) {
					int codePoint = text.codePointBefore(valueStart);
					if (Character.isWhitespace(codePoint)) {
						break;
					}
					valueStart -= Character.charCount(codePoint);
				}

				if (valueStart < valueEnd) {
					valueStart = text.offsetByCodePoints(valueStart, 1);
				}
			} else {
				while (valueStart > 0) {
					char character = text.charAt(valueStart - 1);
					if ((character < '0' || character > '9') && character != ',') {
						break;
					}
					valueStart--;
				}
			}

			long value = 0;
			boolean foundDigit = false;
			boolean valid = true;
			for (int index = valueStart; index < valueEnd; index++) {
				char character = text.charAt(index);
				if (character >= '0' && character <= '9') {
					value = Math.addExact(Math.multiplyExact(value, 10), character - '0');
					foundDigit = true;
				} else if (character != ',') {
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
