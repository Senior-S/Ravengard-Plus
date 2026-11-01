package com.seniors.ravengardplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;

public final class HudCompat {
	private HudCompat() {
	}

	public static BossHealthOverlay bossOverlay(Minecraft client) {
		return client.gui.hud.getBossOverlay();
	}
}
