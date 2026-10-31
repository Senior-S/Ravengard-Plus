package com.seniors.ravengardplus.client.ability;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class AbilityCooldownTracker {
	public static final int DROP_ABILITY = 0;
	public static final int SWAP_ABILITY = 1;
	private static final int[] MENU_SLOTS = {1, 4};
	private static final String USED_PREFIX = "You used your ";
	private static final String USED_SUFFIX = " ability!";
	private static final String READY_PREFIX = "Your ";
	private static final String READY_SUFFIX = " ability is ready again!";
	private static final AbilityItem[] ITEMS = new AbilityItem[2];
	private static final long[] USED_TIMES = new long[2];
	private static final long[] START_TIMES = new long[2];
	private static final long[] END_TIMES = new long[2];
	// Cooldowns measured between the used and ready messages, for abilities whose lore has no cooldown
	private static final Map<String, Double> LEARNED_COOLDOWNS = new HashMap<>();

	private AbilityCooldownTracker() {
	}

	public static void tick(Minecraft client) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.abilityCooldownEnabled || client.player == null) {
			for (int ability = 0; ability < ITEMS.length; ability++) {
				clear(ability);
				ITEMS[ability] = null;
			}
			return;
		}

		long now = System.nanoTime();
		for (int ability = 0; ability < ITEMS.length; ability++) {
			ITEMS[ability] = AbilityItem.of(client.player.inventoryMenu.getSlot(MENU_SLOTS[ability]).getItem());
			if (ITEMS[ability] == null) {
				clear(ability);
				continue;
			}
			if (END_TIMES[ability] > 0 && now >= END_TIMES[ability]) {
				clear(ability);
				if (config.abilityCooldownSoundEnabled) {
					playSound(config.abilityCooldownSound);
				}
			}
		}
	}

	static void setItem(int ability, ItemStack stack) {
		ITEMS[ability] = AbilityItem.of(stack);
	}

	public static void onGameMessage(String message) {
		if (message.startsWith(USED_PREFIX) && message.endsWith(USED_SUFFIX)) {
			onAbilityUsed(message.substring(USED_PREFIX.length(), message.length() - USED_SUFFIX.length()), System.nanoTime());
		} else if (message.startsWith(READY_PREFIX) && message.endsWith(READY_SUFFIX)) {
			String name = message.substring(READY_PREFIX.length(), message.length() - READY_SUFFIX.length());
			RavengardConfig config = RavengardConfig.HANDLER.instance();
			if (onAbilityReady(name, System.nanoTime()) && config.abilityCooldownSoundEnabled) {
				playSound(config.abilityCooldownSound);
			}
		}
	}

	static void onAbilityUsed(String name, long now) {
		int ability = find(name);
		if (ability < 0) {
			return;
		}

		AbilityItem item = ITEMS[ability];
		double cooldown = item.cooldown();
		double effectDuration = item.effectDuration();
		if (cooldown <= 0) {
			cooldown = LEARNED_COOLDOWNS.getOrDefault(item.name(), 0.0);
			effectDuration = 0;
		}
		USED_TIMES[ability] = now;
		if (cooldown <= 0) {
			START_TIMES[ability] = 0;
			END_TIMES[ability] = 0;
			return;
		}
		START_TIMES[ability] = now + (long) (effectDuration * 1_000_000_000L);
		END_TIMES[ability] = START_TIMES[ability] + (long) (cooldown * 1_000_000_000L);
	}

	// Returns whether a running timer was cut short, so the caller can play the ready sound
	static boolean onAbilityReady(String name, long now) {
		int ability = find(name);
		if (ability < 0) {
			return false;
		}

		if (USED_TIMES[ability] > 0) {
			LEARNED_COOLDOWNS.put(ITEMS[ability].name(), (now - USED_TIMES[ability]) / 1_000_000_000.0);
		}
		boolean running = END_TIMES[ability] > now;
		clear(ability);
		return running;
	}

	private static int find(String name) {
		for (int ability = 0; ability < ITEMS.length; ability++) {
			if (ITEMS[ability] != null && ITEMS[ability].name().equals(name)) {
				return ability;
			}
		}
		for (int ability = 0; ability < ITEMS.length; ability++) {
			if (ITEMS[ability] != null && ITEMS[ability].name().contains(name)) {
				return ability;
			}
		}
		return -1;
	}

	private static void clear(int ability) {
		USED_TIMES[ability] = 0;
		START_TIMES[ability] = 0;
		END_TIMES[ability] = 0;
	}

	private record AbilityItem(String name, double cooldown, double effectDuration) {
		static AbilityItem of(ItemStack stack) {
			if (stack.isEmpty()) {
				return null;
			}
			String name = stack.getHoverName().getString().strip();
			if (name.startsWith("Ability: ")) {
				name = name.substring("Ability: ".length()).strip();
			}
			return new AbilityItem(
					name,
					LoreValueParser.findDuration(stack, "Cooldown"),
					Math.max(0, LoreValueParser.findEffectDuration(stack))
			);
		}
	}

	public static void playSound(String soundId) {
		Identifier identifier = Identifier.tryParse(soundId);
		if (identifier == null) {
			return;
		}

		BuiltInRegistries.SOUND_EVENT.getOptional(identifier).ifPresent(sound ->
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F))
		);
	}

	public static String remainingText(int ability) {
		long now = System.nanoTime();
		if (now < START_TIMES[ability]) {
			return null;
		}

		long remainingNanos = END_TIMES[ability] - now;
		if (remainingNanos <= 0) {
			return null;
		}

		long seconds = (remainingNanos + 999_999_999L) / 1_000_000_000L;
		if (seconds >= 60) {
			return seconds / 60 + "m " + seconds % 60 + "s";
		}
		return seconds + "s";
	}
}
