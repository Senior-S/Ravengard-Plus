package com.seniors.ravengardplus.client.ability;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbilityCooldownTrackerTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void matchesIndependentActivationsAndIgnoresExpiredOrUnrelatedMessages() {
		ItemStack shadows = ability("Shadows", 10);
		ItemStack heal = ability("Heal Wounds", 20);
		AbilityCooldownTracker.onAbilityPressed(0, shadows, System.nanoTime() - 6_000_000_000L);
		AbilityCooldownTracker.onGameMessage("You used your Shadows ability!");
		assertNull(AbilityCooldownTracker.remainingText(0));

		long now = System.nanoTime();
		AbilityCooldownTracker.onAbilityPressed(0, shadows, now);
		AbilityCooldownTracker.onAbilityPressed(1, heal, now);
		AbilityCooldownTracker.onGameMessage("You used your Other ability!");
		assertNull(AbilityCooldownTracker.remainingText(0));
		assertNull(AbilityCooldownTracker.remainingText(1));

		AbilityCooldownTracker.onGameMessage("You used your Heal Wounds ability!");
		assertNull(AbilityCooldownTracker.remainingText(0));
		assertEquals("20s", AbilityCooldownTracker.remainingText(1));
		AbilityCooldownTracker.onGameMessage("You used your Shadows ability!");
		assertEquals("10s", AbilityCooldownTracker.remainingText(0));
		assertEquals("20s", AbilityCooldownTracker.remainingText(1));

		shadows.set(DataComponents.LORE, new ItemLore(List.of(
				Component.literal("Cooldown: 10s"), Component.literal("Invisible for 5 seconds"))));
		AbilityCooldownTracker.onAbilityPressed(0, shadows, System.nanoTime());
		AbilityCooldownTracker.onGameMessage("You used your Shadows ability!");
		assertNull(AbilityCooldownTracker.remainingText(0));
	}

	private static ItemStack ability(String name, int seconds) {
		ItemStack stack = new ItemStack(Holder.direct(Items.PAPER, DataComponentMap.EMPTY));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("Ability: " + name));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Cooldown: " + seconds + "s"))));
		return stack;
	}
}
