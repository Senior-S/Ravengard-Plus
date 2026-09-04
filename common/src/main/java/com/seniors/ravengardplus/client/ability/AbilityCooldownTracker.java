package com.seniors.ravengardplus.client.ability;

import com.seniors.ravengardplus.client.ScreenCompat;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class AbilityCooldownTracker {
	public static final int DROP_ABILITY = 0;
	public static final int SWAP_ABILITY = 1;
	private static final int[] MENU_SLOTS = {1, 4};
	private static final long[] START_TIMES = new long[2];
	private static final long[] END_TIMES = new long[2];
	private static final PendingUse[] PENDING_USES = new PendingUse[2];
	private static final long CONFIRMATION_WINDOW = 5_000_000_000L;
	private static boolean dropWasDown;
	private static boolean swapWasDown;

	private AbilityCooldownTracker() {
	}

	public static void tick(Minecraft client) {
		boolean dropDown = client.options.keyDrop.isDown();
		boolean swapDown = client.options.keySwapOffhand.isDown();
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.abilityCooldownEnabled || client.player == null) {
			for (int ability = 0; ability < MENU_SLOTS.length; ability++) {
				START_TIMES[ability] = 0;
				END_TIMES[ability] = 0;
				PENDING_USES[ability] = null;
			}
			dropWasDown = dropDown;
			swapWasDown = swapDown;
			return;
		}

		long now = System.nanoTime();
		boolean screenClosed = ScreenCompat.get(client) == null;
		for (int ability = 0; ability < MENU_SLOTS.length; ability++) {
			ItemStack stack = client.player.inventoryMenu.getSlot(MENU_SLOTS[ability]).getItem();
			if (stack.isEmpty()) {
				START_TIMES[ability] = 0;
				END_TIMES[ability] = 0;
				PENDING_USES[ability] = null;
				continue;
			}
			if (END_TIMES[ability] > 0 && now >= END_TIMES[ability]) {
				START_TIMES[ability] = 0;
				END_TIMES[ability] = 0;
				if (config.abilityCooldownSoundEnabled) {
					playSound(config.abilityCooldownSound);
				}
			}
			PendingUse pending = PENDING_USES[ability];
			if (pending != null && now > pending.until()) {
				PENDING_USES[ability] = null;
			}
			boolean pressed = ability == DROP_ABILITY
					? dropDown && !dropWasDown && !client.player.getMainHandItem().isEmpty()
					: swapDown && !swapWasDown;
			if (screenClosed && pressed && now >= END_TIMES[ability]) {
				onAbilityPressed(ability, stack, now);
			}
		}

		dropWasDown = dropDown;
		swapWasDown = swapDown;
	}

	static void onAbilityPressed(int ability, ItemStack stack, long now) {
		double duration = LoreValueParser.findDuration(stack, "Cooldown");
		if (duration <= 0) {
			return;
		}
		String name = stack.getHoverName().getString().strip();
		if (name.startsWith("Ability: ")) {
			name = name.substring("Ability: ".length()).strip();
		}
		double effectDuration = Math.max(0, LoreValueParser.findEffectDuration(stack));
		PENDING_USES[ability] = new PendingUse(name, duration, effectDuration, now + CONFIRMATION_WINDOW);
	}

	public static void onGameMessage(String message) {
		String prefix = "You used your ";
		String suffix = " ability!";
		if (!message.startsWith(prefix) || !message.endsWith(suffix)) {
			return;
		}
		String name = message.substring(prefix.length(), message.length() - suffix.length());
		long now = System.nanoTime();
		for (int ability = 0; ability < PENDING_USES.length; ability++) {
			PendingUse pending = PENDING_USES[ability];
			if (pending != null && now <= pending.until() && pending.name().equals(name)) {
				START_TIMES[ability] = now + (long) (pending.effectDuration() * 1_000_000_000L);
				END_TIMES[ability] = START_TIMES[ability] + (long) (pending.duration() * 1_000_000_000L);
				PENDING_USES[ability] = null;
				return;
			}
		}
	}

	private record PendingUse(String name, double duration, double effectDuration, long until) {
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
