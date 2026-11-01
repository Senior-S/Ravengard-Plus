package com.seniors.ravengardplus.client.marker;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

// Waypoint for the map center, styled like the server's own objective markers
public final class MapCenterMarker {
	private static final double HEIGHT_ABOVE_PLAYER = 3;
	private static final String ICON = "\u2726";
	// Light purple, apart from the gold doors and light blue exit portals
	private static final int ICON_COLOR = 0xFFFF55FF;
	private static final int LABEL_COLOR = 0xFFFFFFFF;
	private static final int DISTANCE_COLOR = 0xFFAAAAAA;

	private MapCenterMarker() {
	}

	public static void draw(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (!RavengardConfig.HANDLER.instance().mapCenterMarkerEnabled || client.player == null
				|| !MinimapReader.current().inGame()) {
			return;
		}

		Vec3 center = new Vec3(0, client.player.getY() + HEIGHT_ABOVE_PLAYER, 0);
		WorldMarker.draw(graphics, center, ICON, ICON_COLOR,
				new WorldMarker.Line(Component.translatable("ravengard-plus.map_center"), LABEL_COLOR),
				new WorldMarker.Line(Component.literal(Math.round(client.player.position().distanceTo(center)) + "m"), DISTANCE_COLOR));
	}
}
