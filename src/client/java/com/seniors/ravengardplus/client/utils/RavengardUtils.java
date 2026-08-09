package com.seniors.ravengardplus.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class RavengardUtils {
	//change both string to match scoreboard changes with new ravengard release if broken
	public static final String HUB_SIDEBAR = "hub_sb/PROTOTYPE";
	public static final String DUNGEON_SIDEBAR = "dungeon_sb/PROTOTYPE";

	private RavengardUtils() {
	}

	public static boolean isInRavengard() {
		return isInRavengardHub() || isInRavengardDungeon();
	}

	public static boolean isInRavengardHub() {
		return HUB_SIDEBAR.equals(getSidebar());
	}

	public static boolean isInRavengardDungeon() {
		return DUNGEON_SIDEBAR.equals(getSidebar());
	}

	private static String getSidebar() {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return null;
		}

		Scoreboard scoreboard = client.level.getScoreboard();
		Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebarObjective == null) {
			return null;
		}

		return sidebarObjective.getName() + "/" + sidebarObjective.getDisplayName().getString();
	}
}