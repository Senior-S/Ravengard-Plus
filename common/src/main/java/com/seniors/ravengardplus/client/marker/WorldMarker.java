package com.seniors.ravengardplus.client.marker;

import com.seniors.ravengardplus.client.CameraCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

// Screen-space waypoint drawn over a world position, styled like the server's objective markers:
// an icon above the position, then a stack of centered, outlined text lines
public final class WorldMarker {
	private static final int LINE_HEIGHT = 9;
	private static final int OUTLINE_COLOR = 0xFF000000;
	private static final float MIN_ALPHA = 0.04F;

	private WorldMarker() {
	}

	public record Line(Component text, int color) {
	}

	public static void draw(GuiGraphicsExtractor graphics, Vec3 position, String icon, int iconColor, Line... lines) {
		draw(graphics, position, 1, 1, icon, 1, iconColor, lines);
	}

	public static void draw(GuiGraphicsExtractor graphics, Vec3 position, float scale, float alpha, String icon, int iconColor, Line... lines) {
		draw(graphics, position, scale, alpha, icon, 1, iconColor, lines);
	}

	// iconScale enlarges the icon on top of scale; the icon grows upward so it never covers the text
	public static void draw(GuiGraphicsExtractor graphics, Vec3 position, float scale, float alpha, String icon, float iconScale, int iconColor, Line... lines) {
		// Text this faint is drawn fully opaque by the font renderer, so skip it
		if (alpha < MIN_ALPHA) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		Camera camera = CameraCompat.get(client);
		Vec3 offset = position.subtract(camera.position());
		Vector3fc forward = camera.forwardVector();
		if (offset.x * forward.x() + offset.y * forward.y() + offset.z * forward.z() <= 0) {
			return;
		}

		Vec3 screen = client.gameRenderer.projectPointToScreen(position);
		int x = (int) Math.round((screen.x + 1) / 2 * graphics.guiWidth());
		int y = (int) Math.round((1 - screen.y) / 2 * graphics.guiHeight());
		if (x < -50 || x > graphics.guiWidth() + 50 || y < -30 || y > graphics.guiHeight() + 30) {
			return;
		}

		Font font = client.font;
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.pose().pushMatrix();
		graphics.pose().translate(0, -1);
		graphics.pose().scale(iconScale, iconScale);
		outlinedText(graphics, font, Component.literal(icon), 0, -LINE_HEIGHT, fade(iconColor, alpha), fade(OUTLINE_COLOR, alpha));
		graphics.pose().popMatrix();
		for (int line = 0; line < lines.length; line++) {
			outlinedText(graphics, font, lines[line].text(), 0, line * LINE_HEIGHT, fade(lines[line].color(), alpha), fade(OUTLINE_COLOR, alpha));
		}
		graphics.pose().popMatrix();
	}

	// Full size up to fullDistance, shrinking linearly to minScale at minDistance and beyond
	public static float distanceScale(double distance, double fullDistance, double minDistance, float minScale) {
		double progress = Math.clamp((distance - fullDistance) / (minDistance - fullDistance), 0, 1);
		return (float) (1 - progress * (1 - minScale));
	}

	// Black outline keeps the text readable over bright backgrounds such as hitbox lines
	private static void outlinedText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color, int outline) {
		int x = centerX - font.width(text) / 2;
		graphics.text(font, text, x - 1, y, outline, false);
		graphics.text(font, text, x + 1, y, outline, false);
		graphics.text(font, text, x, y - 1, outline, false);
		graphics.text(font, text, x, y + 1, outline, false);
		graphics.text(font, text, x, y, color, false);
	}

	private static int fade(int color, float alpha) {
		int faded = Math.round((color >>> 24) * alpha);
		return faded << 24 | (color & 0xFFFFFF);
	}
}
