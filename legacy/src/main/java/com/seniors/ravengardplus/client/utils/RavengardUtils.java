package com.seniors.ravengardplus.client.utils;

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
		return HUB_SIDEBAR.equals(ScoreboardUtils.getSidebar());
	}

	public static boolean isInRavengardDungeon() {
		return DUNGEON_SIDEBAR.equals(ScoreboardUtils.getSidebar());
	}
}
