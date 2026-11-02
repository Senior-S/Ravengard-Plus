package com.seniors.ravengardplus.client.ping;

import com.seniors.ravengardplus.client.HudCompat;
import com.seniors.ravengardplus.client.mixin.BossHealthOverlayAccessor;
import com.seniors.ravengardplus.client.mixin.ItemDisplayAccessor;
import com.seniors.ravengardplus.client.mixin.TextDisplayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

// Works out what the player is looking at for the contextual ping. Ravengard draws its objects
// (chests, the revival altar, portals, mob bodies) with item displays, so most of them are recognized
// by the model of the displayed item, as listed in the server's resource pack.
public final class PingContext {
	private static final double RANGE = 96;
	private static final double BEHIND_WALL_SLACK = 1.0;
	private static final double DISPLAY_TOLERANCE = 1.1;
	private static final double OBJECT_SEARCH_RADIUS = 2.5;
	private static final double CORPSE_SEARCH_RADIUS = 128;
	private static final String NAMESPACE = "hypixel_ravengard";
	private static final Pattern HEALTH = Pattern.compile("\\d+(\\.\\d+)?\\s*/\\s*\\d+.*$");
	private static final Pattern NOT_NAME = Pattern.compile("[^\\p{L}\\p{N} '\\-]");
	// Exit portals are usable through a 1.2 x 2.5 interaction
	private static final float EXIT_PORTAL_WIDTH = 1.2F;
	private static final float EXIT_PORTAL_HEIGHT = 2.5F;
	private static final float SIZE_TOLERANCE = 0.01F;

	private PingContext() {
	}

	private static boolean isExitPortal(Entity entity) {
		return Math.abs(entity.getBbWidth() - EXIT_PORTAL_WIDTH) < SIZE_TOLERANCE
				&& Math.abs(entity.getBbHeight() - EXIT_PORTAL_HEIGHT) < SIZE_TOLERANCE;
	}

	public enum Kind {
		ITEM, ENEMY, MOB, BOSS, FOUNTAIN, CONTAINER, CORPSE, BOSS_PORTAL, EXIT_PORTAL
	}

	public record Target(Component message, Vec3 position) {
	}

	private record Match(Kind kind, Component name, Vec3 position) {
	}

	public static Target resolve(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			return null;
		}

		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(RANGE));
		BlockHitResult block = client.level.clip(new ClipContext(eye, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
		double wall = block.getType() == HitResult.Type.MISS ? RANGE : block.getLocation().distanceTo(eye);

		Match best = null;
		double bestDistance = Double.POSITIVE_INFINITY;
		for (Entity entity : client.level.getEntities(player, new AABB(eye, end).inflate(2))) {
			double distance = distanceAlongRay(entity, eye, look, end);
			if (distance < 0 || distance > wall + BEHIND_WALL_SLACK || distance >= bestDistance) {
				continue;
			}
			Match match = classify(client, entity);
			if (match != null) {
				best = match;
				bestDistance = distance;
			}
		}

		if (best != null) {
			return new Target(message(client, best), best.position());
		}
		if (block.getType() == HitResult.Type.MISS) {
			return null;
		}
		return new Target(Component.translatable("ravengard-plus.ping.context.location"), block.getLocation());
	}

	// What the contextual ping would say about this entity, for the inspect command
	public static String describe(Minecraft client, Entity entity) {
		Match match = classify(client, entity);
		return match == null ? null : message(client, match).getString();
	}

	// How far along the look ray the entity is hit, or -1 when the ray misses it. Displays have no
	// hitbox, so they count when the ray passes close to them.
	private static double distanceAlongRay(Entity entity, Vec3 eye, Vec3 look, Vec3 end) {
		AABB box = entity.getBoundingBox();
		if (box.getXsize() >= 0.05 && box.getYsize() >= 0.05) {
			Optional<Vec3> hit = box.inflate(0.25).clip(eye, end);
			return hit.map(point -> point.distanceTo(eye)).orElse(-1.0);
		}
		Vec3 offset = entity.position().subtract(eye);
		double along = offset.dot(look);
		if (along <= 0) {
			return -1;
		}
		double aside = offset.subtract(look.scale(along)).length();
		return aside <= DISPLAY_TOLERANCE + along * 0.02 ? along : -1;
	}

	private static Match classify(Minecraft client, Entity entity) {
		if (entity == client.player || entity instanceof ArmorStand) {
			return null;
		}
		if (entity instanceof Player player) {
			// Teammates glow, everyone else in a game is on another team
			if (player.isSpectator() || player.isInvisible() || player.isCurrentlyGlowing()) {
				return null;
			}
			return new Match(Kind.ENEMY, player.getName(), above(entity));
		}
		if (entity instanceof Mannequin) {
			return new Match(Kind.CORPSE, null, above(entity));
		}
		if (entity instanceof ItemEntity item) {
			return new Match(Kind.ITEM, item.getItem().getHoverName(), entity.position().add(0, 0.6, 0));
		}
		if (entity instanceof Interaction) {
			if (isExitPortal(entity)) {
				return new Match(Kind.EXIT_PORTAL, null, entity.position().add(0, 1.5, 0));
			}
			// Mobs are an interaction hitbox riding an area effect cloud, with the body models next to it
			String model = bodyModel(client, entity);
			if (model != null) {
				return mob(client, entity, Component.literal(prettify(model.split("/")[1])), model);
			}
			return nearbyObject(client, entity.getBoundingBox().getCenter());
		}
		if (entity instanceof LivingEntity living) {
			return mob(client, living, mobName(client, living), null);
		}
		if (entity instanceof Display.ItemDisplay display) {
			return displayMatch(client, display);
		}
		return null;
	}

	private static Match displayMatch(Minecraft client, Display.ItemDisplay display) {
		ItemStack stack = ((ItemDisplayAccessor) display).ravengardPlus$getItemStack();
		String path = modelPath(stack);
		if (path == null) {
			return null;
		}
		Vec3 position = display.position().add(0, 1, 0);
		if (path.startsWith("item/gameplay/altar/resurrection")) {
			return new Match(Kind.FOUNTAIN, null, position);
		}
		if (path.startsWith("item/gameplay/chests/")) {
			String type = path.substring("item/gameplay/chests/".length()).split("/")[0];
			return new Match(Kind.CONTAINER, Component.translatable("ravengard-plus.ping.container." + switch (type) {
				case "gold", "silver", "golem", "boss", "stash" -> type;
				default -> "chest";
			}), position);
		}
		if (path.startsWith("item/gameplay/lootbag")) {
			return new Match(Kind.CONTAINER, Component.translatable("ravengard-plus.ping.container.loot_bag"), position);
		}
		if (path.startsWith("item/gameplay/tombs/tomb_ghost")) {
			return new Match(Kind.CORPSE, null, position);
		}
		if (path.startsWith("item/gameplay/portals/miniboss/")) {
			return new Match(Kind.BOSS_PORTAL, null, position);
		}
		if (path.startsWith("item/gameplay/portals/exit_portal")) {
			return new Match(Kind.EXIT_PORTAL, null, position);
		}
		if (path.startsWith("entity/")) {
			// A piece of a mob's body; its name comes from the model folder
			Entity body = display.getVehicle();
			if (body instanceof LivingEntity living) {
				return mob(client, living, mobName(client, living), path);
			}
			return mob(client, display, Component.literal(prettify(path.split("/")[1])), path);
		}
		boolean loot = path.startsWith("item/weapons/") || path.startsWith("item/armor/") || path.startsWith("item/accessories/")
				|| path.startsWith("item/gameplay/consumables/") || path.startsWith("item/gameplay/trinkets/");
		// Items held by mobs and players ride them; items lying around don't
		if (loot && display.getVehicle() == null) {
			return new Match(Kind.ITEM, stack.getHoverName(), display.position().add(0, 0.6, 0));
		}
		return null;
	}

	// The chest, altar or portal model closest to an interaction hitbox, which is what the server
	// actually puts under the crosshair for clickable objects
	private static Match nearbyObject(Minecraft client, Vec3 center) {
		Match best = null;
		double bestDistance = OBJECT_SEARCH_RADIUS * OBJECT_SEARCH_RADIUS;
		for (Entity entity : client.level.getEntities((Entity) null, new AABB(center, center).inflate(OBJECT_SEARCH_RADIUS))) {
			if (!(entity instanceof Display.ItemDisplay display) || display.getVehicle() != null) {
				continue;
			}
			double distance = entity.position().distanceToSqr(center);
			if (distance > bestDistance) {
				continue;
			}
			Match match = displayMatch(client, display);
			if (match != null && match.kind() != Kind.MOB && match.kind() != Kind.BOSS && match.kind() != Kind.ITEM) {
				best = match;
				bestDistance = distance;
			}
		}
		return best;
	}

	private static Match mob(Minecraft client, Entity entity, Component name, String modelPath) {
		String plain = clean(name.getString());
		String bossBar = matchingBossBar(client, plain);
		Vec3 position = entity instanceof Display ? entity.position().add(0, 1.5, 0) : above(entity);
		if (bossBar != null) {
			return new Match(Kind.BOSS, Component.literal(bossBar), position);
		}
		if (modelPath != null && modelPath.contains("boss") || entity instanceof LivingEntity living && isBossModel(client, living)) {
			return new Match(Kind.BOSS, Component.literal(plain), position);
		}
		return new Match(Kind.MOB, Component.literal(plain), position);
	}

	// Ravengard mobs are often a plain base entity wearing display models, so the name is looked for in
	// the custom name, then a text display riding the mob, then the model of a display near its body
	private static Component mobName(Minecraft client, LivingEntity living) {
		if (living.getCustomName() != null && !clean(living.getCustomName().getString()).isEmpty()) {
			return living.getCustomName();
		}
		for (Entity passenger : living.getPassengers()) {
			if (passenger instanceof Display.TextDisplay text) {
				String line = clean(((TextDisplayAccessor) text).ravengardPlus$getText().getString());
				if (!line.isEmpty()) {
					return Component.literal(line);
				}
			}
		}
		String model = bodyModel(client, living);
		if (model != null) {
			return Component.literal(prettify(model.split("/")[1]));
		}
		return living.getType().getDescription();
	}

	// The model of a mob's body: displays riding the same carrier entity first, then any display
	// overlapping the hitbox
	private static String bodyModel(Minecraft client, Entity body) {
		List<Entity> nearby = new ArrayList<>(body.getPassengers());
		if (body.getVehicle() != null) {
			nearby.addAll(body.getVehicle().getPassengers());
		}
		nearby.addAll(client.level.getEntities(body, body.getBoundingBox().inflate(1)));
		for (Entity entity : nearby) {
			if (entity instanceof Display.ItemDisplay display) {
				String path = modelPath(((ItemDisplayAccessor) display).ravengardPlus$getItemStack());
				if (path != null && path.startsWith("entity/") && path.split("/").length > 1) {
					return path;
				}
			}
		}
		return null;
	}

	private static boolean isBossModel(Minecraft client, LivingEntity living) {
		String model = bodyModel(client, living);
		return model != null && model.split("/")[1].contains("boss");
	}

	// The name of a boss bar that shares the mob's name, since the server also uses bars for other text
	private static String matchingBossBar(Minecraft client, String mobName) {
		if (mobName.isEmpty()) {
			return null;
		}
		String mob = mobName.toLowerCase(Locale.ROOT);
		for (LerpingBossEvent event : ((BossHealthOverlayAccessor) HudCompat.bossOverlay(client)).ravengardPlus$getEvents().values()) {
			String bar = clean(event.getName().getString());
			String lower = bar.toLowerCase(Locale.ROOT);
			if (!bar.isEmpty() && (lower.contains(mob) || mob.contains(lower))) {
				return bar;
			}
		}
		return null;
	}

	private static boolean someoneIsDead(Minecraft client) {
		AABB area = client.player.getBoundingBox().inflate(CORPSE_SEARCH_RADIUS);
		for (Entity entity : client.level.getEntities(client.player, area)) {
			if (entity instanceof Mannequin) {
				return true;
			}
			if (entity instanceof Display.ItemDisplay display) {
				String path = modelPath(((ItemDisplayAccessor) display).ravengardPlus$getItemStack());
				if (path != null && path.startsWith("item/gameplay/tombs/tomb_ghost")) {
					return true;
				}
			}
		}
		return false;
	}

	private static Component message(Minecraft client, Match match) {
		String key = "ravengard-plus.ping.context.";
		return switch (match.kind()) {
			case ITEM -> Component.translatable(key + "item", clean(match.name().getString()));
			case ENEMY -> Component.translatable(key + "enemy");
			case MOB -> Component.translatable(key + "mob", match.name());
			case BOSS -> Component.translatable(key + "boss", match.name());
			case FOUNTAIN -> Component.translatable(key + (someoneIsDead(client) ? "fountain_revive" : "fountain"));
			case CONTAINER -> Component.translatable(key + "container", match.name());
			case CORPSE -> Component.translatable(key + "corpse");
			case BOSS_PORTAL -> Component.translatable(key + "boss_portal");
			case EXIT_PORTAL -> Component.translatable(key + "exit_portal");
		};
	}

	private static Vec3 above(Entity entity) {
		return entity.position().add(0, entity.getBbHeight() + 0.3, 0);
	}

	private static String modelPath(ItemStack stack) {
		Identifier model = stack.get(DataComponents.ITEM_MODEL);
		return model != null && model.getNamespace().equals(NAMESPACE) ? model.getPath() : null;
	}

	// Drops the server's private-use glyphs, color codes and health counters from a display name
	static String clean(String text) {
		String line = text.split("\n")[0].replaceAll("§.", "");
		StringBuilder kept = new StringBuilder(line.length());
		line.codePoints().filter(point -> Character.getType(point) != Character.PRIVATE_USE && Character.getType(point) != Character.SURROGATE)
				.forEach(kept::appendCodePoint);
		String name = HEALTH.matcher(kept.toString()).replaceAll("");
		name = NOT_NAME.matcher(name).replaceAll("");
		return name.trim().replaceAll("\\s+", " ");
	}

	static String prettify(String id) {
		StringBuilder name = new StringBuilder();
		for (String word : id.split("_")) {
			if (word.isEmpty() || word.chars().allMatch(Character::isDigit)) {
				continue;
			}
			if (!name.isEmpty()) {
				name.append(' ');
			}
			name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return name.toString();
	}
}
