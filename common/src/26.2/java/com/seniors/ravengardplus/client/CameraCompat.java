package com.seniors.ravengardplus.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public final class CameraCompat {
	private CameraCompat() {
	}

	public static Camera get(Minecraft client) {
		return client.gameRenderer.mainCamera();
	}
}
