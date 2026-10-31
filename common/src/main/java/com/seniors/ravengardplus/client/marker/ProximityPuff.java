package com.seniors.ravengardplus.client.marker;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

// Hides markers the player has walked up to: the marker grows while fading out, and plays the same
// animation backwards once the player steps away. Between the two distances a marker keeps its state,
// so it doesn't flicker at the edge.
public final class ProximityPuff {
	private static final double HIDE_DISTANCE = 2.0;
	private static final double SHOW_DISTANCE = 3.0;
	private static final float PUFF_SECONDS = 0.2F;
	private static final float PUFF_GROWTH = 0.6F;
	// Per marker, how far it is faded in: 1 shown, 0 hidden after the puff
	private Map<Long, Float> visibility = new HashMap<>();
	private Map<Long, Float> nextVisibility = new HashMap<>();
	private long lastFrame;
	private float step;

	public void beginFrame() {
		long now = System.nanoTime();
		step = lastFrame == 0 ? 0 : Math.min((now - lastFrame) / 1.0E9F, 0.1F) / PUFF_SECONDS;
		lastFrame = now;
		nextVisibility.clear();
	}

	// Markers not updated this frame are forgotten
	public void endFrame() {
		Map<Long, Float> previous = visibility;
		visibility = nextVisibility;
		nextVisibility = previous;
	}

	public float update(Vec3 marker, Vec3 player) {
		long key = key(marker);
		float shown = visibility.getOrDefault(key, 1.0F);
		double horizontal = Math.hypot(marker.x - player.x, marker.z - player.z);
		if (horizontal < HIDE_DISTANCE) {
			shown = Math.max(0, shown - step);
		} else if (horizontal > SHOW_DISTANCE) {
			shown = Math.min(1, shown + step);
		}
		nextVisibility.put(key, shown);
		return shown;
	}

	public float visibility(Vec3 marker) {
		return visibility.getOrDefault(key(marker), 1.0F);
	}

	private static long key(Vec3 marker) {
		return ((long) Math.round(marker.x * 2) << 32) ^ (Math.round(marker.z * 2) & 0xFFFFFFFFL);
	}

	public static float growth(float shown) {
		return 1 + (1 - shown) * PUFF_GROWTH;
	}
}
