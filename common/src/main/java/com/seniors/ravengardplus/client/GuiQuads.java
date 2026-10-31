package com.seniors.ravengardplus.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

// Solid quads drawn from the GUI atlas instead of the untextured GUI pipeline. Since 26.3 every GUI
// draw shares one vertex buffer, so smaller position-color vertices shift the base vertex of the
// textured draws that follow. Server resource-pack shaders that derive quad corners from the vertex
// index (Ravengard's health bar and hotbar) then render garbage. Textured quads keep the alignment.
public final class GuiQuads {
	private static final Identifier WHITE = Identifier.fromNamespaceAndPath("ravengard-plus", "white");

	private GuiQuads() {
	}

	public static void fill(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WHITE, x1, y1, x2 - x1, y2 - y1, color);
	}

	public static void outline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		fill(graphics, x, y, x + width, y + 1, color);
		fill(graphics, x, y + height - 1, x + width, y + height, color);
		fill(graphics, x, y + 1, x + 1, y + height - 1, color);
		fill(graphics, x + width - 1, y + 1, x + width, y + height - 1, color);
	}
}
