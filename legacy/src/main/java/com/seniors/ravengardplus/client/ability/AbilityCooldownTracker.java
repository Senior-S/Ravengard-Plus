package com.seniors.ravengardplus.client.ability;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class AbilityCooldownTracker {
	public static final int DROP_ABILITY = 0;
	public static final int SWAP_ABILITY = 1;
	private static final int[] MENU_SLOTS = {1, 4};
	private static final double[] DURATIONS = {-1, -1};
	private static final double[] EFFECT_DURATIONS = new double[2];
	private static final long[] START_TIMES = new long[2];
	private static final long[] END_TIMES = new long[2];
	private static final long CONFIRMATION_WINDOW = 5_000_000_000L;
	private static boolean dropWasDown;
	private static boolean swapWasDown;
	private static int pendingAbility = -1;
	private static double pendingDuration;
	private static double pendingEffectDuration;
	private static long pendingUntil;

	private AbilityCooldownTracker() {
	}

	public static void tick(MinecraftClient client) {
		boolean dropDown = client.options.dropKey.isPressed();
		boolean swapDown = client.options.swapHandsKey.isPressed();
		if (!RavengardConfig.HANDLER.instance().abilityCooldownEnabled || client.player == null) {
			START_TIMES[DROP_ABILITY] = 0;
			START_TIMES[SWAP_ABILITY] = 0;
			END_TIMES[DROP_ABILITY] = 0;
			END_TIMES[SWAP_ABILITY] = 0;
			pendingAbility = -1;
			dropWasDown = dropDown;
			swapWasDown = swapDown;
			return;
		}

		for (int index = 0; index < MENU_SLOTS.length; index++) {
			ItemStack ability = client.player.playerScreenHandler.getSlot(MENU_SLOTS[index]).getStack();
			if (ability.isEmpty()) {
				DURATIONS[index] = -1;
				EFFECT_DURATIONS[index] = 0;
				START_TIMES[index] = 0;
				END_TIMES[index] = 0;
				continue;
			}

			double duration = LoreValueParser.findDuration(ability, "Cooldown");
			DURATIONS[index] = duration > 0 ? duration : -1;
			double effectDuration = LoreValueParser.findEffectDuration(ability);
			EFFECT_DURATIONS[index] = effectDuration > 0 ? effectDuration : 0;
		}

		long now = System.nanoTime();
		for (int ability = 0; ability < END_TIMES.length; ability++) {
			if (END_TIMES[ability] > 0 && now >= END_TIMES[ability]) {
				START_TIMES[ability] = 0;
				END_TIMES[ability] = 0;
				RavengardConfig config = RavengardConfig.HANDLER.instance();
				if (config.abilityCooldownSoundEnabled) {
					playSound(config.abilityCooldownSound);
				}
			}
		}
		if (now > pendingUntil) {
			pendingAbility = -1;
		}
		if (client.currentScreen == null) {
			if (dropDown && !dropWasDown
					&& !client.player.getMainHandStack().isEmpty()
					&& DURATIONS[DROP_ABILITY] > 0
					&& now >= END_TIMES[DROP_ABILITY]) {
				pendingAbility = DROP_ABILITY;
				pendingDuration = DURATIONS[DROP_ABILITY];
				pendingEffectDuration = EFFECT_DURATIONS[DROP_ABILITY];
				pendingUntil = now + CONFIRMATION_WINDOW;
			}
			if (swapDown && !swapWasDown
					&& DURATIONS[SWAP_ABILITY] > 0
					&& now >= END_TIMES[SWAP_ABILITY]) {
				pendingAbility = SWAP_ABILITY;
				pendingDuration = DURATIONS[SWAP_ABILITY];
				pendingEffectDuration = EFFECT_DURATIONS[SWAP_ABILITY];
				pendingUntil = now + CONFIRMATION_WINDOW;
			}
		}

		dropWasDown = dropDown;
		swapWasDown = swapDown;
	}

	public static void onGameMessage(String message) {
		long now = System.nanoTime();
		if (pendingAbility < 0
				|| now > pendingUntil
				|| !message.startsWith("You used your ")
				|| !message.endsWith(" ability!")) {
			return;
		}

		START_TIMES[pendingAbility] = now + (long) (pendingEffectDuration * 1_000_000_000L);
		END_TIMES[pendingAbility] = START_TIMES[pendingAbility] + (long) (pendingDuration * 1_000_000_000L);
		pendingAbility = -1;
	}

	public static void playSound(String soundId) {
		Identifier identifier = Identifier.tryParse(soundId);
		if (identifier == null) {
			return;
		}

		MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(
				identifier,
				SoundCategory.MASTER,
				1.0F,
				1.0F,
				Random.create(),
				false,
				0,
				SoundInstance.AttenuationType.NONE,
				0,
				0,
				0,
				true
		));
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
