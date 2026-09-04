package com.seniors.ravengardplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenCompat {
	private ScreenCompat() {
	}

	public static Screen get(Minecraft client) {
		return client.screen;
	}

	public static void set(Minecraft client, Screen screen) {
		client.setScreen(screen);
	}
}
