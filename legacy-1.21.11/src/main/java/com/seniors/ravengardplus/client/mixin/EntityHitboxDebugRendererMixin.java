package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
	@Inject(method = "drawHitbox", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$hideArmorStandHitboxes(
			Entity entity,
			float tickProgress,
			boolean serverEntity,
			CallbackInfo callbackInfo
	) {
		if (RavengardConfig.HANDLER.instance().hideArmorStandHitboxes && entity instanceof ArmorStandEntity) {
			callbackInfo.cancel();
		}
	}

	@Redirect(
			method = "drawHitbox",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/DrawStyle;stroked(I)Lnet/minecraft/client/render/DrawStyle;",
					ordinal = 0
			)
	)
	private DrawStyle ravengardPlus$colorHitbox(
			int originalColor,
			Entity entity,
			float tickProgress,
			boolean serverEntity
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		return DrawStyle.stroked((entity instanceof PlayerEntity ? config.playerHitboxColor : config.mobHitboxColor).getRGB());
	}
}
