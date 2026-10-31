package com.seniors.ravengardplus.client.portal;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.marker.ProximityPuff;
import com.seniors.ravengardplus.client.marker.WorldMarker;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Marks the dungeon's exit portals, which the server makes usable through a 1.2 x 2.5 interaction hitbox
public final class ExitPortalTracker {
	private static final float PORTAL_WIDTH = 1.2F;
	private static final float PORTAL_HEIGHT = 2.5F;
	private static final float SIZE_TOLERANCE = 0.01F;
	private static final String ICON = "\u25c8";
	private static final float ICON_SCALE = 1.75F;
	private static final int PORTAL_COLOR = 0xFF6FE3F0;
	private static final int DISTANCE_COLOR = 0xFFAAAAAA;
	private static final double FULL_SCALE_DISTANCE = 6;
	private static final double MIN_SCALE_DISTANCE = 24;
	private static final float MIN_SCALE = 0.8F;
	private static final ProximityPuff PUFF = new ProximityPuff();
	private static List<Vec3> portals = List.of();

	private ExitPortalTracker() {
	}

	public static void tick(Minecraft client) {
		if (!RavengardConfig.HANDLER.instance().exitPortalMarkersEnabled || client.level == null
				|| !MinimapReader.current().inGame()) {
			portals = List.of();
			return;
		}

		List<Vec3> found = new ArrayList<>();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity instanceof Interaction && isPortal(entity.getBbWidth(), entity.getBbHeight())) {
				found.add(markerPosition(entity));
			}
		}
		portals = found;
	}

	public static void drawMarkers(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (portals.isEmpty() || client.player == null) {
			return;
		}

		// Farthest first, so nearer markers are drawn on top
		Vec3 eye = client.player.getEyePosition();
		List<Vec3> sorted = new ArrayList<>(portals);
		sorted.sort(Comparator.comparingDouble((Vec3 portal) -> portal.distanceToSqr(eye)).reversed());
		PUFF.beginFrame();
		for (Vec3 portal : sorted) {
			float shown = PUFF.update(portal, client.player.position());
			float scale = WorldMarker.distanceScale(portal.distanceTo(eye), FULL_SCALE_DISTANCE, MIN_SCALE_DISTANCE, MIN_SCALE)
					* ProximityPuff.growth(shown);
			WorldMarker.draw(graphics, portal, scale, shown, ICON, ICON_SCALE, PORTAL_COLOR,
					new WorldMarker.Line(Component.translatable("ravengard-plus.exit_portal"), PORTAL_COLOR),
					new WorldMarker.Line(Component.literal(Math.round(client.player.position().distanceTo(portal)) + "m"), DISTANCE_COLOR));
		}
		PUFF.endFrame();
	}

	// How the marker sees this entity, for the inspect command; null when it isn't an exit portal
	public static String describe(Entity entity) {
		if (!(entity instanceof Interaction) || !isPortal(entity.getBbWidth(), entity.getBbHeight())) {
			return null;
		}
		Vec3 marker = markerPosition(entity);
		return "exit portal, tracked " + portals.contains(marker)
				+ ", marker shown " + Math.round(PUFF.visibility(marker) * 100) + "%";
	}

	private static Vec3 markerPosition(Entity entity) {
		return entity.position().add(0, PORTAL_HEIGHT / 2, 0);
	}

	static boolean isPortal(float width, float height) {
		return Math.abs(width - PORTAL_WIDTH) < SIZE_TOLERANCE && Math.abs(height - PORTAL_HEIGHT) < SIZE_TOLERANCE;
	}
}
