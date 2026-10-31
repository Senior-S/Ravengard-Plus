package com.seniors.ravengardplus.client.debug;

import com.mojang.logging.LogUtils;
import com.seniors.ravengardplus.client.minimap.MinimapReader;
import com.seniors.ravengardplus.client.portal.ExitPortalTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// Debug helper behind the inspect command: dumps everything the client knows about the entity under the
// crosshair, so server-side markers can be recognized later. Walls are ignored and entities without a
// hitbox (displays, marker armor stands) are matched by how close the look ray passes to them.
public final class EntityInspector {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final double RANGE = 64;
	private static final double SMALL_ENTITY_TOLERANCE = 0.5;
	private static final int CHAT_VALUE_LIMIT = 160;

	private EntityInspector() {
	}

	public static void inspect(Minecraft client, Consumer<Component> chat) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			return;
		}

		Entity target = findTarget(client, player);
		if (target == null) {
			chat.accept(Component.literal("No entity found where you are looking.").withStyle(ChatFormatting.RED));
			return;
		}

		List<String[]> rows = describe(target, player);
		StringBuilder dump = new StringBuilder();
		chat.accept(Component.literal("Entity " + EntityType.getKey(target.getType()) + " (copied to clipboard)")
				.withStyle(ChatFormatting.GOLD));
		for (String[] row : rows) {
			dump.append(row[0]).append(": ").append(row[1]).append('\n');
			String value = row[1].length() > CHAT_VALUE_LIMIT ? row[1].substring(0, CHAT_VALUE_LIMIT) + "..." : row[1];
			chat.accept(Component.literal(row[0] + ": ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(value).withStyle(ChatFormatting.WHITE)));
		}
		client.keyboardHandler.setClipboard(dump.toString());
		LOGGER.info("Inspected entity:\n{}", dump);
	}

	private static Entity findTarget(Minecraft client, LocalPlayer player) {
		Vec3 eye = player.getEyePosition(1.0F);
		Vec3 look = player.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(RANGE));
		Entity best = null;
		double bestDistance = Double.POSITIVE_INFINITY;
		for (Entity entity : client.level.getEntities(player, player.getBoundingBox().inflate(RANGE))) {
			AABB box = entity.getBoundingBox();
			double distance;
			if (box.getXsize() < 0.05 || box.getYsize() < 0.05) {
				Vec3 offset = box.getCenter().subtract(eye);
				distance = offset.dot(look);
				if (distance <= 0 || offset.subtract(look.scale(distance)).length() > SMALL_ENTITY_TOLERANCE) {
					continue;
				}
			} else {
				Optional<Vec3> hit = box.inflate(0.1).clip(eye, end);
				if (hit.isEmpty()) {
					continue;
				}
				distance = hit.get().distanceTo(eye);
			}
			if (distance < bestDistance) {
				bestDistance = distance;
				best = entity;
			}
		}
		return best;
	}

	private static List<String[]> describe(Entity entity, LocalPlayer player) {
		List<String[]> rows = new ArrayList<>();
		rows.add(row("Type", EntityType.getKey(entity.getType()).toString()));
		rows.add(row("Id", entity.getId() + ", UUID " + entity.getUUID()));
		rows.add(row("Name", text(entity.getName())));
		if (entity.getCustomName() != null) {
			rows.add(row("Custom name", text(entity.getCustomName()) + (entity.isCustomNameVisible() ? " (visible)" : "")));
		}
		rows.add(row("Position", String.format("%.2f %.2f %.2f, block %s, %.1f blocks away",
				entity.getX(), entity.getY(), entity.getZ(), entity.blockPosition().toShortString(), entity.distanceTo(player))));
		rows.add(row("Rotation", String.format("yaw %.1f, pitch %.1f", entity.getYRot(), entity.getXRot())));
		AABB box = entity.getBoundingBox();
		rows.add(row("Hitbox", String.format("%.2f x %.2f x %.2f", box.getXsize(), box.getYsize(), box.getZsize())));
		rows.add(row("Flags", "invisible " + entity.isInvisible() + ", glowing " + entity.isCurrentlyGlowing()
				+ ", no gravity " + entity.isNoGravity() + ", silent " + entity.isSilent()));
		PlayerTeam team = entity.getTeam();
		if (team != null) {
			rows.add(row("Team", team.getName()));
		}
		if (entity.getVehicle() != null) {
			rows.add(row("Riding", summary(entity.getVehicle())));
		}
		for (Entity passenger : entity.getPassengers()) {
			rows.add(row("Passenger", summary(passenger)));
		}
		if (entity instanceof LivingEntity living) {
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack stack = living.getItemBySlot(slot);
				if (!stack.isEmpty()) {
					rows.add(row("Equipment " + slot.getName(), stack(stack)));
				}
			}
		}
		MinimapReader.Minimap minimap = MinimapReader.current();
		rows.add(row("Minimap", minimap.player() == null ? "not found" : minimap.hubOrTutorial() ? "hub or tutorial" : "in game"));
		String portal = ExitPortalTracker.describe(entity);
		if (portal != null) {
			rows.add(row("Ravengard Plus", portal));
		}
		List<SynchedEntityData.DataValue<?>> values = entity.getEntityData().getNonDefaultValues();
		if (values != null) {
			for (SynchedEntityData.DataValue<?> value : values) {
				rows.add(row("Data " + value.id(), value(value.value())));
			}
		}
		return rows;
	}

	private static String summary(Entity entity) {
		return EntityType.getKey(entity.getType()) + " #" + entity.getId() + " " + text(entity.getName());
	}

	private static String value(Object value) {
		if (value instanceof Optional<?> optional) {
			return optional.map(EntityInspector::value).orElse("empty");
		}
		if (value instanceof ItemStack stack) {
			return stack(stack);
		}
		if (value instanceof Component component) {
			return text(component);
		}
		return escape(String.valueOf(value));
	}

	private static String stack(ItemStack stack) {
		if (stack.isEmpty()) {
			return "empty";
		}
		return stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + escape(stack.getComponentsPatch().toString());
	}

	private static String text(Component component) {
		return "\"" + escape(component.getString()) + "\"";
	}

	// Writes non-ASCII characters as Java escapes, so custom font glyphs can be told apart
	private static String escape(String text) {
		StringBuilder builder = new StringBuilder(text.length());
		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (character < 0x20 || character > 0x7E) {
				builder.append('\\').append('u').append(String.format("%04x", (int) character));
			} else {
				builder.append(character);
			}
		}
		return builder.toString();
	}

	private static String[] row(String label, String value) {
		return new String[]{label, value};
	}
}
