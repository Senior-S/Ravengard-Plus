package com.seniors.ravengardplus.client.ping;

import com.seniors.ravengardplus.client.ScreenCompat;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

// Tap the ping key for a contextual ping on whatever is under the crosshair. Hold it to open the
// wheel, move the mouse toward an option and let go to send it; letting go in the middle cancels.
// The wheel is part of the HUD rather than a screen, so the player keeps moving while choosing.
public final class PingWheel {
	private static final int HOLD_TICKS = 4;
	private static final double CURSOR_SPEED = 0.25;
	private static final double CURSOR_LIMIT = 40;
	private static final double DEAD_ZONE = 10;
	private static final int WHEEL_SIZE = 124;
	private static final int SLOT_SIZE = 24;
	private static final int SLOT_RADIUS = 44;
	private static final int HUB_SIZE = 28;
	private static final int POINTER_RADIUS = 19;
	private static final int LABEL_HEIGHT = 14;
	private static final float OPEN_SECONDS = 0.08F;
	private static final Identifier WHEEL = sprite("wheel");
	private static final Identifier SLOT = sprite("slot");
	private static final Identifier SLOT_SELECTED = sprite("slot_selected");
	private static final Identifier HUB = sprite("hub");
	private static final Identifier LABEL = sprite("label");
	private static final Identifier POINTER = sprite("pointer");
	private static KeyMapping key;
	private static boolean open;
	private static boolean wasDown;
	private static int heldTicks;
	private static long openedAt;
	private static double cursorX;
	private static double cursorY;
	private static PingContext.Target context;

	private PingWheel() {
	}

	// Clockwise from the top. Options about the player themself mark where they stand, the others
	// mark the spot under the crosshair.
	public enum Option {
		CONTEXT("context", 11, 11, false),
		GO("go", 11, 11, false),
		ENEMY("enemy", 11, 10, false),
		TRAP("trap", 11, 10, false),
		RETREAT("retreat", 11, 11, false),
		WAIT("wait", 11, 11, true),
		HELP("help", 11, 11, true),
		HEAL("heal", 11, 10, true);

		private final String id;
		private final int iconWidth;
		private final int iconHeight;
		private final boolean atPlayer;
		private final Identifier icon;

		Option(String id, int iconWidth, int iconHeight, boolean atPlayer) {
			this.id = id;
			this.iconWidth = iconWidth;
			this.iconHeight = iconHeight;
			this.atPlayer = atPlayer;
			this.icon = sprite("icon_" + id);
		}

		Component message() {
			return Component.translatable("ravengard-plus.ping.message." + id);
		}
	}

	public static void setKey(KeyMapping mapping) {
		key = mapping;
	}

	public static boolean isOpen() {
		return open;
	}

	public static void moveCursor(double dx, double dy) {
		cursorX += dx * CURSOR_SPEED;
		cursorY += dy * CURSOR_SPEED;
		double length = Math.hypot(cursorX, cursorY);
		if (length > CURSOR_LIMIT) {
			cursorX *= CURSOR_LIMIT / length;
			cursorY *= CURSOR_LIMIT / length;
		}
	}

	public static void tick(Minecraft client) {
		if (key == null) {
			return;
		}
		boolean down = key.isDown();
		boolean clicked = false;
		while (key.consumeClick()) {
			clicked = true;
		}
		boolean usable = RavengardConfig.HANDLER.instance().pingsEnabled && client.player != null
				&& ScreenCompat.get(client) == null && MinimapReader.current().inGame();
		if (!usable) {
			open = false;
			wasDown = down;
			heldTicks = 0;
			return;
		}

		if (down) {
			if (!wasDown) {
				heldTicks = 0;
			}
			if (++heldTicks >= HOLD_TICKS && !open) {
				open = true;
				openedAt = System.nanoTime();
				cursorX = 0;
				cursorY = 0;
			}
			if (open) {
				context = PingContext.resolve(client);
			}
		} else if (wasDown || clicked) {
			if (open) {
				open = false;
				Option option = selected();
				if (option != null) {
					send(client, option);
				}
			} else {
				send(client, Option.CONTEXT);
			}
			heldTicks = 0;
		}
		wasDown = down;
	}

	private static void send(Minecraft client, Option option) {
		PingContext.Target target = PingContext.resolve(client);
		if (option == Option.CONTEXT) {
			if (target != null) {
				PingMarkers.send(client, target.message(), target.position());
			}
			return;
		}
		Vec3 position = option.atPlayer || target == null
				? client.player.position().add(0, client.player.getBbHeight() + 0.5, 0)
				: target.position();
		PingMarkers.send(client, option.message(), position);
	}

	private static Option selected() {
		if (Math.hypot(cursorX, cursorY) < DEAD_ZONE) {
			return null;
		}
		double angle = Math.atan2(cursorY, cursorX) + Math.PI / 2;
		int count = Option.values().length;
		int index = (int) Math.round(angle / (Math.PI * 2 / count));
		return Option.values()[Math.floorMod(index, count)];
	}

	public static void draw(GuiGraphicsExtractor graphics) {
		if (!open) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		int centerX = graphics.guiWidth() / 2;
		int centerY = graphics.guiHeight() / 2;
		float opening = Math.min(1, (System.nanoTime() - openedAt) / 1.0E9F / OPEN_SECONDS);
		float scale = 0.85F + 0.15F * opening;

		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().scale(scale, scale);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WHEEL, -WHEEL_SIZE / 2, -WHEEL_SIZE / 2, WHEEL_SIZE, WHEEL_SIZE);

		Option selected = selected();
		Option[] options = Option.values();
		for (int index = 0; index < options.length; index++) {
			Option option = options[index];
			double angle = -Math.PI / 2 + index * Math.PI * 2 / options.length;
			int x = (int) Math.round(Math.cos(angle) * SLOT_RADIUS);
			int y = (int) Math.round(Math.sin(angle) * SLOT_RADIUS);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, option == selected ? SLOT_SELECTED : SLOT,
					x - SLOT_SIZE / 2, y - SLOT_SIZE / 2, SLOT_SIZE, SLOT_SIZE);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, option.icon,
					x - option.iconWidth / 2, y - option.iconHeight / 2, option.iconWidth, option.iconHeight);
		}

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HUB, -HUB_SIZE / 2, -HUB_SIZE / 2, HUB_SIZE, HUB_SIZE);
		if (selected != null) {
			graphics.pose().pushMatrix();
			graphics.pose().rotate((float) (Math.atan2(cursorY, cursorX) + Math.PI / 2));
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, POINTER, -3, -POINTER_RADIUS - 3, 7, 5);
			graphics.pose().popMatrix();
		}

		Component label = label(selected);
		int labelWidth = font.width(label) + 16;
		int labelY = WHEEL_SIZE / 2 + 4;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LABEL, -labelWidth / 2, labelY, labelWidth, LABEL_HEIGHT);
		graphics.centeredText(font, label, 0, labelY + 3, selected == null ? 0xFFAAAAAA : 0xFFFFFFFF);
		graphics.pose().popMatrix();
	}

	// Shows the message the option sends, so the player knows what teammates will read
	private static Component label(Option selected) {
		if (selected == null) {
			return Component.translatable("ravengard-plus.ping.cancel");
		}
		if (selected == Option.CONTEXT) {
			return context == null ? Component.translatable("ravengard-plus.ping.nothing") : context.message();
		}
		return selected.message();
	}

	private static Identifier sprite(String name) {
		return Identifier.fromNamespaceAndPath("ravengard-plus", "ping/" + name);
	}
}
