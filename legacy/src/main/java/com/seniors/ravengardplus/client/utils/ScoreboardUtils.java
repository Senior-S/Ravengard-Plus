package com.seniors.ravengardplus.client.utils;

import java.util.Objects;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;

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

	private static void onClientTick(MinecraftClient client) {
		if (client.world == null) {
			update(null, null);
			return;
		}

		Scoreboard scoreboard = client.world.getScoreboard();
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

		ScoreboardObjective sidebarObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (sidebarObjective == null) {
			return null;
		}

        //note : concat scoreboard name & display name to ensure a unique id
		return sidebarObjective.getName() + "/" + sidebarObjective.getDisplayName().getString();
	}
}
