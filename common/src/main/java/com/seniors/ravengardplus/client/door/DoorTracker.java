package com.seniors.ravengardplus.client.door;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.marker.ProximityPuff;
import com.seniors.ravengardplus.client.marker.WorldMarker;
import com.seniors.ravengardplus.client.mixin.ItemDisplayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Finds Ravengard's dungeon doors. A door is drawn by several item displays using a gameplay/doors
// model (top and bottom of each leaf), grouped here by distance. Whether a door is open is left out on
// purpose, so the mod doesn't show anything the game itself hides.
public final class DoorTracker {
	private static final String MODEL_NAMESPACE = "hypixel_ravengard";
	private static final String MODEL_PREFIX = "item/gameplay/doors/";
	private static final double PART_SPACING = 3.0;
	private static final String ICON = "\u25c6";
	private static final int LABEL_COLOR = 0xFFFFFFFF;
	private static final double FULL_SCALE_DISTANCE = 6;
	private static final double MIN_SCALE_DISTANCE = 24;
	private static final float MIN_SCALE = 0.6F;
	private static final ProximityPuff PUFF = new ProximityPuff();
	private static List<Door> doors = List.of();
	private static Map<Integer, Integer> glowColors = Map.of();

	private DoorTracker() {
	}

	public record Door(Vec3 position, List<Integer> displayIds) {
	}

	public static void tick(Minecraft client) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.doorIndicatorsEnabled || client.level == null || client.player == null) {
			doors = List.of();
			glowColors = Map.of();
			return;
		}

		double range = config.doorIndicatorRange;
		AABB area = client.player.getBoundingBox().inflate(range);
		List<Display.ItemDisplay> parts = new ArrayList<>();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (!area.contains(entity.position())) {
				continue;
			}
			if (entity instanceof Display.ItemDisplay display && isDoor(((ItemDisplayAccessor) display).ravengardPlus$getItemStack())) {
				parts.add(display);
			}
		}

		List<Door> found = new ArrayList<>();
		Map<Integer, Integer> colors = new HashMap<>();
		int color = config.doorColor.getRGB();
		for (List<Display.ItemDisplay> door : group(parts)) {
			double x = door.stream().mapToDouble(Entity::getX).average().orElseThrow();
			double z = door.stream().mapToDouble(Entity::getZ).average().orElseThrow();
			double y = door.stream().mapToDouble(Entity::getY).max().orElseThrow();
			List<Integer> ids = door.stream().map(Entity::getId).toList();
			found.add(new Door(new Vec3(x, y, z), ids));
			for (int id : ids) {
				colors.put(id, color);
			}
		}
		doors = found;
		glowColors = config.doorIndicatorStyle == RavengardConfig.DoorIndicatorStyle.GLOW ? colors : Map.of();
	}

	public static Integer glowColor(Entity entity) {
		return glowColors.isEmpty() ? null : glowColors.get(entity.getId());
	}

	public static void drawMarkers(GuiGraphicsExtractor graphics) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		Minecraft client = Minecraft.getInstance();
		if (config.doorIndicatorStyle != RavengardConfig.DoorIndicatorStyle.MARKER || doors.isEmpty() || client.player == null) {
			return;
		}

		int color = config.doorColor.getRGB() | 0xFF000000;
		PUFF.beginFrame();
		// Farthest first, so nearer markers are drawn on top
		Vec3 eye = client.player.getEyePosition();
		List<Door> sorted = new ArrayList<>(doors);
		sorted.sort(Comparator.comparingDouble((Door door) -> door.position().distanceToSqr(eye)).reversed());
		for (Door door : sorted) {
			float shown = PUFF.update(door.position(), client.player.position());
			float scale = scale(door.position().distanceTo(eye)) * ProximityPuff.growth(shown);
			WorldMarker.draw(graphics, door.position(), scale, shown, ICON, color,
					new WorldMarker.Line(Component.translatable("ravengard-plus.door"), LABEL_COLOR));
		}
		PUFF.endFrame();
	}

	static float scale(double distance) {
		return WorldMarker.distanceScale(distance, FULL_SCALE_DISTANCE, MIN_SCALE_DISTANCE, MIN_SCALE);
	}

	// Groups parts that are within PART_SPACING of each other, directly or through other parts
	private static List<List<Display.ItemDisplay>> group(List<Display.ItemDisplay> parts) {
		List<List<Display.ItemDisplay>> groups = new ArrayList<>();
		boolean[] grouped = new boolean[parts.size()];
		for (int start = 0; start < parts.size(); start++) {
			if (grouped[start]) {
				continue;
			}
			List<Display.ItemDisplay> group = new ArrayList<>();
			grouped[start] = true;
			group.add(parts.get(start));
			for (int member = 0; member < group.size(); member++) {
				Display.ItemDisplay current = group.get(member);
				for (int other = 0; other < parts.size(); other++) {
					if (!grouped[other] && current.distanceTo(parts.get(other)) <= PART_SPACING) {
						grouped[other] = true;
						group.add(parts.get(other));
					}
				}
			}
			groups.add(group);
		}
		return groups;
	}

	static boolean isDoor(ItemStack stack) {
		Identifier model = stack.get(DataComponents.ITEM_MODEL);
		return model != null && model.getNamespace().equals(MODEL_NAMESPACE) && model.getPath().startsWith(MODEL_PREFIX);
	}
}
