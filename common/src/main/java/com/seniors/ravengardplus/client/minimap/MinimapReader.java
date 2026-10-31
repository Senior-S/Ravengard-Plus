package com.seniors.ravengardplus.client.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// Reads Ravengard's minimap, which the server draws from scoreboard glyphs. Marker glyphs carry their
// offset from the player in their text color; map segment glyphs tell which map is loaded.
public final class MinimapReader {
	public static final int TEAMMATE_COUNT = 2;
	private static final char PLAYER_MARKER = '';
	private static final char[] TEAMMATE_MARKERS = {'', ''};
	private static final char FIRST_HUB_SEGMENT = '';
	private static final char LAST_TUTORIAL_SEGMENT = '';
	private static final int UPDATE_INTERVAL = 5;
	private static Minimap current = Minimap.EMPTY;
	private static int ticks;

	private MinimapReader() {
	}

	// Marker offsets in minimap pixels, teammates relative to the player marker and null where missing
	public record Minimap(double[] player, double[][] teammates, boolean hubOrTutorial) {
		static final Minimap EMPTY = new Minimap(null, new double[TEAMMATE_COUNT][], false);

		public boolean inGame() {
			return player != null && !hubOrTutorial;
		}
	}

	public static Minimap current() {
		return current;
	}

	public static void tick(Minecraft client) {
		if (client.level == null) {
			current = Minimap.EMPTY;
			return;
		}
		if (++ticks % UPDATE_INTERVAL == 0) {
			current = read(client.level.getScoreboard());
		}
	}

	static Minimap read(Scoreboard scoreboard) {
		Scan scan = new Scan();
		Set<Objective> objectives = new HashSet<>();
		for (DisplaySlot slot : DisplaySlot.values()) {
			Objective objective = scoreboard.getDisplayObjective(slot);
			if (objective == null || !objectives.add(objective)) {
				continue;
			}
			scan.component(objective.getDisplayName());
			for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
				if (entry.isHidden()) {
					continue;
				}
				PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
				scan.component(PlayerTeam.formatNameForTeam(team, entry.ownerName()));
				if (entry.display() != null) {
					scan.component(entry.display());
				}
				scan.component(entry.formatValue(objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT)));
			}
		}
		for (PlayerTeam team : scoreboard.getPlayerTeams()) {
			scan.component(team.getPlayerPrefix());
			scan.component(team.getPlayerSuffix());
		}
		return scan.result();
	}

	static final class Scan {
		private double[] player;
		private final double[][] teammates = new double[TEAMMATE_COUNT][];
		private boolean hubOrTutorial;

		void component(Component component) {
			component.visit((Style style, String text) -> {
				TextColor color = style.getColor();
				for (int index = 0; index < text.length(); index++) {
					char character = text.charAt(index);
					if (character >= FIRST_HUB_SEGMENT && character <= LAST_TUTORIAL_SEGMENT) {
						hubOrTutorial = true;
					}
					if (color == null) {
						continue;
					}
					if (character == PLAYER_MARKER) {
						player = decode(color.getValue());
					}
					for (int marker = 0; marker < TEAMMATE_MARKERS.length; marker++) {
						if (character == TEAMMATE_MARKERS[marker]) {
							teammates[marker] = decode(color.getValue());
						}
					}
				}
				return Optional.empty();
			}, Style.EMPTY);
		}

		Minimap result() {
			if (player != null) {
				for (double[] teammate : teammates) {
					if (teammate != null) {
						teammate[0] -= player[0];
						teammate[1] -= player[1];
					}
				}
			}
			return new Minimap(player, teammates, hubOrTutorial);
		}
	}

	// Color = 0bXXXXXXXX XYYYYYYY YYRRRRRR: 9 X bits, 9 Y bits and 6 rotation bits
	static double[] decode(int color) {
		int x = (color >> 15) & 0x1FF;
		int y = (color >> 6) & 0x1FF;
		return new double[]{x / 2.0 - 128, y / 2.0 - 128};
	}
}
