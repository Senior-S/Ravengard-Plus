package com.seniors.ravengardplus.client.mixin;

import com.google.common.collect.Iterables;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
	@Redirect(
			method = "emitGizmos",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"
			)
	)
	private Iterable<Entity> ravengardPlus$filterUnnecessaryHitboxes(ClientLevel level) {
		Iterable<Entity> entities = level.entitiesForRendering();
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		boolean hideDecorations = config.hideArmorStandHitboxes;
		// Hitboxes the mod forces on never include players, so they can't be used as an edge in PvP
		boolean hidePlayers = config.alwaysShowHitboxes;
		if (!hideDecorations && !hidePlayers) {
			return entities;
		}

		return Iterables.filter(entities, entity -> !(hidePlayers && entity instanceof Player)
				&& !(hideDecorations && (entity instanceof ArmorStand
						|| entity instanceof Display.ItemDisplay
						|| entity instanceof Display.TextDisplay)));
	}

	@Redirect(
			method = "showHitboxes",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/gizmos/GizmoStyle;stroke(I)Lnet/minecraft/gizmos/GizmoStyle;",
					ordinal = 0
			)
	)
	private GizmoStyle ravengardPlus$colorHitbox(
			int originalColor,
			Entity entity,
			float partialTick,
			boolean serverEntity
	) {
		// Players only show in Minecraft's own hitbox view, so they keep its color
		return entity instanceof Player
				? GizmoStyle.stroke(originalColor)
				: GizmoStyle.stroke(RavengardConfig.HANDLER.instance().mobHitboxColor.getRGB());
	}
}
