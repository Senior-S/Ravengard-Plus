package com.seniors.ravengardplus.client.utils;

import java.util.Objects;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public final class ScoreboardUtils {
	private static Scoreboard lastScoreboard;
	private static String sidebar;
	private static String previousSidebar;
	private static boolean sidebarChanged;

	private ScoreboardUtils() {
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(ScoreboardUtils::onClientTick);
	}

	public static Scoreboard getLastScoreboard() {
		return lastScoreboard;
	}

	public static String getSidebar() {
		return sidebar;
	}

	public static String getPreviousSidebar() {
		return previousSidebar;
	}

	public static boolean hasSidebarChanged() {
		return sidebarChanged;
	}

	private static void onClientTick(Minecraft client) {
		if (client.level == null) {
			update(null, null);
			return;
		}

		Scoreboard scoreboard = client.level.getScoreboard();
		update(scoreboard, getSidebarLabel(scoreboard));
	}

	private static void update(Scoreboard scoreboard, String nextSidebar) {
		lastScoreboard = scoreboard;
		previousSidebar = sidebar;
		sidebar = nextSidebar;
		sidebarChanged = !Objects.equals(previousSidebar, sidebar);
	}

	private static String getSidebarLabel(Scoreboard scoreboard) {
		if (scoreboard == null) {
			return null;
		}

		Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebarObjective == null) {
			return null;
		}

        //note : concat scoreboard name & display name to ensure a unique id
		return sidebarObjective.getName() + "/" + sidebarObjective.getDisplayName().getString();
	}
}