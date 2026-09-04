package com.seniors.ravengardplus.client.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoreValueParserTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void preservesDecimalValueAndCrownGlyph() {
		ItemStack stack = item("\u265B\uFE0F 0.29 Crowns");
		LoreValueParser.Display display = LoreValueParser.findDisplay(stack, "Crown");
		assertNotNull(display);
		assertEquals("\u265B\uFE0F", display.glyph());
		assertEquals("0.29", display.value());
		assertEquals(new BigDecimal("0.29"), display.amount());
		assertEquals(0.29, LoreValueParser.find(stack, "Crown"));
		assertEquals(new BigDecimal("0.87"), display.amount().multiply(BigDecimal.valueOf(3)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"1..2", "1,23", ".", ",", "1.2.3", "1,000.2,3"})
	void rejectsMalformedValuesInBothNumericAndDisplayParsing(String value) {
		ItemStack stack = item(value + " Crowns");
		assertEquals(-1, LoreValueParser.find(stack, "Crown"));
		assertNull(LoreValueParser.findDisplay(stack, "Crown"));
	}

	@Test
	void acceptsThousandsAndKeepsLabelsSeparate() {
		ItemStack stack = item("1,234.5 Crowns", "20 Defense");
		assertEquals(1234.5, LoreValueParser.find(stack, "Crown"));
		assertEquals(20, LoreValueParser.find(stack, "Defense"));
		assertEquals("1,234.5", LoreValueParser.findDisplay(stack, "Crown").value());
		assertEquals(-1, LoreValueParser.find(item("5 Crownstone"), "Crown"));
	}

	@Test
	void observesReplacementLoreAfterCachingAPresentOrMissingValue() {
		ItemStack stack = item("5 Crowns");
		assertEquals(5, LoreValueParser.find(stack, "Crown"));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("No price"))));
		assertNull(LoreValueParser.findDisplay(stack, "Crown"));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("8 Crowns"))));
		assertEquals(8, LoreValueParser.find(stack, "Crown"));
	}

	@Test
	void parsesCooldownAndEffectDurations() {
		ItemStack stack = item("Cooldown: 1m 2.5s", "Heal for 3,5 seconds");
		assertEquals(62.5, LoreValueParser.findDuration(stack, "Cooldown"));
		assertEquals(3.5, LoreValueParser.findEffectDuration(stack));
	}

	private static ItemStack item(String... lines) {
		ItemStack stack = new ItemStack(Holder.direct(Items.PAPER, DataComponentMap.EMPTY));
		stack.set(DataComponents.LORE, new ItemLore(java.util.Arrays.stream(lines)
				.<Component>map(Component::literal).toList()));
		return stack;
	}
}
