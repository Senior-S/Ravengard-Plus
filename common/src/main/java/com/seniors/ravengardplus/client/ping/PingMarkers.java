package com.seniors.ravengardplus.client.ping;

import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.marker.WorldMarker;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Pings travel as normal team chat with the spot appended as "[x, y, z]". Players without the mod read
// the coordinates; with the mod the coordinates are cut from the chat line and shown as a marker.
public final class PingMarkers {
	private static final Pattern METADATA = Pattern.compile(" ?\\[(-?\\d{1,6}), (-?\\d{1,6}), (-?\\d{1,6})]$");
	private static final Pattern SEPARATORS = Pattern.compile("^[\\s:>»\\-|]+");
	private static final long FADE_NANOS = 1_000_000_000L;
	private static final long SEND_COOLDOWN_NANOS = 1_000_000_000L;
	private static final double MAX_DISTANCE = 512;
	private static final double SAME_SPOT = 1.5;
	private static final String ICON = "▼";
	private static final float ICON_SCALE = 1.5F;
	private static final int PING_COLOR = 0xFFFFCE47;
	private static final int OWN_COLOR = 0xFFB6C1CC;
	private static final int DETAIL_COLOR = 0xFFAAAAAA;
	private static final List<Marker> MARKERS = new ArrayList<>();
	private static long lastSent;

	private PingMarkers() {
	}

	private record Marker(Vec3 position, String label, String sender, boolean own, long created) {
	}

	// Sends the ping to team chat and places the marker right away, without waiting for the echo
	public static void send(Minecraft client, Component message, Vec3 position) {
		long now = System.nanoTime();
		if (client.player == null || now - lastSent < SEND_COOLDOWN_NANOS) {
			return;
		}
		lastSent = now;

		int x = (int) Math.floor(position.x);
		int y = (int) Math.floor(position.y);
		int z = (int) Math.floor(position.z);
		String text = message.getString();
		client.player.connection.sendChat(text + " [" + x + ", " + y + ", " + z + "]");
		add(center(x, y, z), text, client.player.getName().getString(), true);
		playSound();
	}

	// Reads a teammate's ping out of a chat line and returns the line without the coordinates
	public static Component onChatMessage(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (!RavengardConfig.HANDLER.instance().pingsEnabled || client.player == null || !MinimapReader.current().inGame()) {
			return message;
		}
		String text = message.getString();
		Matcher metadata = METADATA.matcher(text);
		if (!metadata.find()) {
			return message;
		}
		String sender = findSender(client.getConnection(), text, metadata.start());
		if (sender == null) {
			return message;
		}

		Vec3 position = center(Integer.parseInt(metadata.group(1)), Integer.parseInt(metadata.group(2)), Integer.parseInt(metadata.group(3)));
		if (position.distanceTo(client.player.position()) > MAX_DISTANCE) {
			return message;
		}
		int nameEnd = wordIndex(text, sender, metadata.start()) + sender.length();
		String label = SEPARATORS.matcher(text.substring(nameEnd, metadata.start())).replaceAll("").trim();
		// Our own ping comes back as an echo; it only refreshes the marker placed when sending
		boolean own = sender.equals(client.player.getName().getString());
		add(position, label, sender, own);
		if (!own) {
			playSound();
		}
		return truncate(message, metadata.start());
	}

	// The online player named earliest in the line, which is the sender in any "[rank] name: text" format.
	// Lines that name nobody online (server announcements) aren't treated as pings.
	private static String findSender(ClientPacketListener connection, String text, int end) {
		if (connection == null) {
			return null;
		}
		String sender = null;
		int senderIndex = end;
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			String name = info.getProfile().name();
			int index = wordIndex(text, name, end);
			if (index >= 0 && index < senderIndex) {
				sender = name;
				senderIndex = index;
			}
		}
		return sender;
	}

	private static int wordIndex(String text, String name, int end) {
		int index = text.indexOf(name);
		while (index >= 0 && index + name.length() <= end) {
			boolean startsWord = index == 0 || !isNameChar(text.charAt(index - 1));
			boolean endsWord = index + name.length() == text.length() || !isNameChar(text.charAt(index + name.length()));
			if (startsWord && endsWord) {
				return index;
			}
			index = text.indexOf(name, index + 1);
		}
		return -1;
	}

	private static boolean isNameChar(char character) {
		return Character.isLetterOrDigit(character) || character == '_';
	}

	// Keeps the first length characters of the line, with their original styles
	static Component truncate(Component message, int length) {
		MutableComponent result = Component.empty();
		int[] remaining = {length};
		message.visit((Style style, String part) -> {
			if (remaining[0] <= 0) {
				return Optional.of(Boolean.TRUE);
			}
			String kept = part.length() <= remaining[0] ? part : part.substring(0, remaining[0]);
			remaining[0] -= kept.length();
			if (!kept.isEmpty()) {
				result.append(Component.literal(kept).withStyle(style));
			}
			return Optional.empty();
		}, Style.EMPTY);
		return result;
	}

	private static Vec3 center(int x, int y, int z) {
		return new Vec3(x + 0.5, y + 0.5, z + 0.5);
	}

	private static void add(Vec3 position, String label, String sender, boolean own) {
		MARKERS.removeIf(marker -> marker.position().distanceTo(position) < SAME_SPOT);
		MARKERS.add(new Marker(position, label, sender, own, System.nanoTime()));
	}

	// Opacity settings are percentages, never below 25% so markers can't vanish entirely
	public static float opacity(int percent) {
		return Math.clamp(percent, 25, 100) / 100.0F;
	}

	private static void playSound() {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (config.pingSoundEnabled) {
			AbilityCooldownTracker.playSound(config.pingSound);
		}
	}

	public static void draw(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		long now = System.nanoTime();
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		long lifetime = Math.clamp(config.pingDuration, 1, 10) * 1_000_000_000L;
		long fade = Math.min(FADE_NANOS, lifetime / 2);
		float opacity = opacity(config.pingMarkerOpacity);
		MARKERS.removeIf(marker -> now - marker.created() > lifetime);
		if (MARKERS.isEmpty() || client.player == null || !MinimapReader.current().inGame()
				|| !RavengardConfig.HANDLER.instance().pingsEnabled) {
			if (client.level == null) {
				MARKERS.clear();
			}
			return;
		}

		Vec3 eye = client.player.getEyePosition();
		List<Marker> sorted = new ArrayList<>(MARKERS);
		sorted.sort(Comparator.comparingDouble((Marker marker) -> marker.position().distanceToSqr(eye)).reversed());
		for (Marker marker : sorted) {
			long left = lifetime - (now - marker.created());
			float alpha = (left < fade ? left / (float) fade : 1) * opacity;
			// Pops in slightly larger, so a new ping catches the eye
			float age = (now - marker.created()) / 1.0E9F;
			float pop = 1 + Math.max(0, 0.25F - age) * 2;
			double distance = marker.position().distanceTo(client.player.position());
			float scale = WorldMarker.distanceScale(marker.position().distanceTo(eye), 6, 32, 0.75F) * pop;
			int color = marker.own() ? OWN_COLOR : PING_COLOR;
			WorldMarker.draw(graphics, marker.position(), scale, alpha, ICON, ICON_SCALE, color,
					new WorldMarker.Line(Component.literal(marker.label()), color),
					new WorldMarker.Line(Component.literal(marker.sender() + " · " + Math.round(distance) + "m"), DETAIL_COLOR));
		}
	}
}
