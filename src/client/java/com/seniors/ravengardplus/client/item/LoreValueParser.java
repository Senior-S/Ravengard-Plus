package com.seniors.ravengardplus.client.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LoreValueParser {
	private static final Pattern DURATION_PART = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*([sm])", Pattern.CASE_INSENSITIVE);
	private static final Pattern EFFECT_DURATION = Pattern.compile("\\bfor\\s+(\\d+(?:[.,]\\d+)?)\\s+seconds\\b", Pattern.CASE_INSENSITIVE);

	private LoreValueParser() {
	}

	public static double find(ItemStack stack, String label) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return -1;
		}

		for (Component line : lore.lines()) {
			String text = line.getString();
			int labelStart = text.lastIndexOf(label);
			int labelEnd = labelStart + label.length();
			if (labelStart < 0) {
				continue;
			}
			if (labelEnd < text.length() && text.charAt(labelEnd) == 's') {
				labelEnd++;
			}
			if (labelEnd < text.length() && !Character.isWhitespace(text.codePointAt(labelEnd))) {
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

	public static double findDuration(ItemStack stack, String label) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return -1;
		}

		for (Component line : lore.lines()) {
			String text = line.getString();
			int labelStart = text.lastIndexOf(label);
			if (labelStart < 0) {
				continue;
			}

			Matcher matcher = DURATION_PART.matcher(text.substring(labelStart + label.length()));
			double seconds = 0;
			boolean found = false;
			while (matcher.find()) {
				double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
				seconds += Character.toLowerCase(matcher.group(2).charAt(0)) == 'm' ? value * 60 : value;
				found = true;
			}
			if (found) {
				return seconds;
			}
		}

		return -1;
	}

	public static double findEffectDuration(ItemStack stack) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return -1;
		}

		for (Component line : lore.lines()) {
			Matcher matcher = EFFECT_DURATION.matcher(line.getString());
			if (matcher.find()) {
				return Double.parseDouble(matcher.group(1).replace(',', '.'));
			}
		}

		return -1;
	}

	public static Display findDisplay(ItemStack stack, String label) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return null;
		}

		for (Component line : lore.lines()) {
			String text = line.getString();
			int labelStart = text.lastIndexOf(label);
			if (labelStart < 0) {
				continue;
			}
			int labelEnd = labelStart + label.length();
			if (labelEnd < text.length() && text.charAt(labelEnd) == 's') {
				labelEnd++;
			}
			if (labelEnd < text.length() && !Character.isWhitespace(text.codePointAt(labelEnd))) {
				continue;
			}

			int valueEnd = labelStart;
			while (valueEnd > 0 && Character.isWhitespace(text.codePointBefore(valueEnd))) {
				valueEnd -= Character.charCount(text.codePointBefore(valueEnd));
			}

			int valueStart = valueEnd;
			boolean foundDigit = false;
			while (valueStart > 0) {
				char character = text.charAt(valueStart - 1);
				if ((character < '0' || character > '9') && character != ',' && character != '.') {
					break;
				}
				foundDigit |= character >= '0' && character <= '9';
				valueStart--;
			}
			if (!foundDigit) {
				continue;
			}

			int glyphEnd = valueStart;
			while (glyphEnd > 0 && Character.isWhitespace(text.codePointBefore(glyphEnd))) {
				glyphEnd -= Character.charCount(text.codePointBefore(glyphEnd));
			}
			int glyphStart = glyphEnd;
			while (glyphStart > 0) {
				int codePoint = text.codePointBefore(glyphStart);
				glyphStart -= Character.charCount(codePoint);
				if (codePoint != 0xFE0F && Character.getType(codePoint) != Character.NON_SPACING_MARK) {
					break;
				}
			}

			return new Display(text.substring(glyphStart, glyphEnd), text.substring(valueStart, valueEnd));
		}

		return null;
	}

	public record Display(String glyph, String value) {
	}
}
