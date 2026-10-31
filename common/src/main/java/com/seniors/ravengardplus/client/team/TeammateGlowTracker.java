package com.seniors.ravengardplus.client.team;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Gives each glowing teammate the color of their minimap marker. The minimap is drawn from scoreboard
// glyphs whose text color encodes the marker's offset from the player, so markers are matched to the
// glowing players whose positions fit those offsets best.
public final class TeammateGlowTracker {
	private static final int[] TEAMMATE_COLORS = {0x358BFC, 0x5FCE51};
	private static final int UPDATE_INTERVAL = 5;
	private static final int MAX_CANDIDATES = 6;
	private static final Map<UUID, Integer> COLORS = new HashMap<>();
	private static List<UUID> assigned = List.of();
	private static int ticks;

	private TeammateGlowTracker() {
	}

	public static void tick(Minecraft client) {
		if (!RavengardConfig.HANDLER.instance().teammateGlowColorsEnabled || client.level == null || client.player == null) {
			reset();
			return;
		}
		if (++ticks % UPDATE_INTERVAL != 0) {
			return;
		}

		double[][] markers = MinimapReader.current().teammates();
		List<Player> candidates = new ArrayList<>();
		for (Player player : client.level.players()) {
			if (player != client.player && player.isCurrentlyGlowing()) {
				candidates.add(player);
			}
		}
		candidates.sort((a, b) -> Double.compare(a.distanceToSqr(client.player), b.distanceToSqr(client.player)));
		if (candidates.size() > MAX_CANDIDATES) {
			candidates = candidates.subList(0, MAX_CANDIDATES);
		}

		List<Integer> markerIndexes = new ArrayList<>();
		List<double[]> markerOffsets = new ArrayList<>();
		for (int index = 0; index < markers.length; index++) {
			if (markers[index] != null) {
				markerIndexes.add(index);
				markerOffsets.add(markers[index]);
			}
		}
		if (markerOffsets.isEmpty() || candidates.isEmpty()) {
			reset();
			return;
		}

		List<double[]> offsets = new ArrayList<>();
		for (Player candidate : candidates) {
			offsets.add(new double[]{candidate.getX() - client.player.getX(), candidate.getZ() - client.player.getZ()});
		}

		int[] best = null;
		double bestCost = Double.POSITIVE_INFINITY;
		double keptCost = Double.POSITIVE_INFINITY;
		int[] kept = keptAssignment(candidates, markerOffsets.size());
		for (int[] assignment : assignments(candidates.size(), markerOffsets.size())) {
			double cost = cost(markerOffsets, offsets, assignment);
			if (cost < bestCost) {
				bestCost = cost;
				best = assignment;
			}
		}
		if (kept != null) {
			keptCost = cost(markerOffsets, offsets, kept);
		}
		// Keep the current pairing unless another one fits clearly better, so colors don't flicker
		int[] chosen = kept != null && keptCost <= bestCost * 2 + 1 ? kept : best;
		if (chosen == null) {
			reset();
			return;
		}

		COLORS.clear();
		List<UUID> chosenIds = new ArrayList<>();
		for (int marker = 0; marker < chosen.length; marker++) {
			UUID id = chosen[marker] < 0 ? null : candidates.get(chosen[marker]).getUUID();
			if (id != null) {
				COLORS.put(id, TEAMMATE_COLORS[markerIndexes.get(marker)]);
			}
			chosenIds.add(id);
		}
		assigned = chosenIds;
	}

	public static Integer color(Entity entity) {
		return COLORS.isEmpty() ? null : COLORS.get(entity.getUUID());
	}

	private static void reset() {
		COLORS.clear();
		assigned = List.of();
	}

	private static int[] keptAssignment(List<Player> candidates, int markerCount) {
		if (assigned.size() != markerCount) {
			return null;
		}
		int[] assignment = new int[markerCount];
		int matched = 0;
		for (int marker = 0; marker < markerCount; marker++) {
			assignment[marker] = -1;
			UUID id = assigned.get(marker);
			if (id == null) {
				continue;
			}
			for (int candidate = 0; candidate < candidates.size(); candidate++) {
				if (candidates.get(candidate).getUUID().equals(id)) {
					assignment[marker] = candidate;
					matched++;
				}
			}
			if (assignment[marker] < 0) {
				return null;
			}
		}
		return matched == Math.min(markerCount, candidates.size()) ? assignment : null;
	}

	// Candidate index per marker, -1 for a marker left unmatched; pairs up as many markers as possible
	private static List<int[]> assignments(int candidateCount, int markerCount) {
		List<int[]> result = new ArrayList<>();
		collect(new int[markerCount], 0, 0, Math.min(markerCount, candidateCount), new boolean[candidateCount], result);
		return result;
	}

	private static void collect(int[] current, int depth, int matched, int required, boolean[] used, List<int[]> result) {
		if (depth == current.length) {
			if (matched == required) {
				result.add(current.clone());
			}
			return;
		}
		current[depth] = -1;
		collect(current, depth + 1, matched, required, used, result);
		for (int candidate = 0; candidate < used.length; candidate++) {
			if (!used[candidate]) {
				used[candidate] = true;
				current[depth] = candidate;
				collect(current, depth + 1, matched + 1, required, used, result);
				used[candidate] = false;
			}
		}
	}

	// Squared distance between the markers and the candidates' offsets, after fitting the unknown map scale
	static double cost(List<double[]> markers, List<double[]> offsets, int[] assignment) {
		double dot = 0;
		double length = 0;
		for (int marker = 0; marker < assignment.length; marker++) {
			if (assignment[marker] < 0) {
				continue;
			}
			double[] offset = offsets.get(assignment[marker]);
			dot += markers.get(marker)[0] * offset[0] + markers.get(marker)[1] * offset[1];
			length += offset[0] * offset[0] + offset[1] * offset[1];
		}
		double scale = length < 1.0E-6 ? 1 : Math.max(0, dot / length);
		double cost = 0;
		for (int marker = 0; marker < assignment.length; marker++) {
			if (assignment[marker] < 0) {
				continue;
			}
			double[] offset = offsets.get(assignment[marker]);
			double dx = markers.get(marker)[0] - offset[0] * scale;
			double dy = markers.get(marker)[1] - offset[1] * scale;
			cost += dx * dx + dy * dy;
		}
		return cost;
	}
}
