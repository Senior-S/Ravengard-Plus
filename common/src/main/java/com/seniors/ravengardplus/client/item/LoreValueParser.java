package com.seniors.ravengardplus.client.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LoreValueParser {
	public static final Pattern NUMBER = Pattern.compile("(?:\\d+(?:,\\d{3})*(?:\\.\\d*)?|\\.\\d+)");
	private static final Cache<ItemLore, Map<String, Display>> DISPLAYS = CacheBuilder.newBuilder()
			.maximumSize(512).weakKeys().build();
	private static final Pattern DURATION_PART = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*([sm])", Pattern.CASE_INSENSITIVE);
	private static final Pattern EFFECT_DURATION = Pattern.compile("\\bfor\\s+(\\d+(?:[.,]\\d+)?)\\s+seconds\\b", Pattern.CASE_INSENSITIVE);

	private LoreValueParser() {
	}

	public static double find(ItemStack stack, String label) {
		Display display = findDisplay(stack, label);
		return display == null ? -1 : display.amount().doubleValue();
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

		Map<String, Display> displays = DISPLAYS.getIfPresent(lore);
		if (displays == null) {
			displays = new HashMap<>();
			DISPLAYS.put(lore, displays);
		} else if (displays.containsKey(label)) {
			return displays.get(label);
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
			while (valueStart > 0) {
				char character = text.charAt(valueStart - 1);
				if ((character < '0' || character > '9') && character != ',' && character != '.') {
					break;
				}
				valueStart--;
			}
			String value = text.substring(valueStart, valueEnd);
			if (!NUMBER.matcher(value).matches()) {
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

			Display display = new Display(text.substring(glyphStart, glyphEnd), value,
					new BigDecimal(value.replace(",", "")));
			displays.put(label, display);
			return display;
		}

		displays.put(label, null);
		return null;
	}

	public record Display(String glyph, String value, BigDecimal amount) {
	}
}
