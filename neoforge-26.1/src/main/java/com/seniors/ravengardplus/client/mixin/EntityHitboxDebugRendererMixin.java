package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
	@Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$hideArmorStandHitboxes(
			Entity entity,
			float partialTick,
			boolean serverEntity,
			CallbackInfo callbackInfo
	) {
		if (RavengardConfig.HANDLER.instance().hideArmorStandHitboxes && entity instanceof ArmorStand) {
			callbackInfo.cancel();
		}
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
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		return GizmoStyle.stroke((entity instanceof Player ? config.playerHitboxColor : config.mobHitboxColor).getRGB());
	}
}
