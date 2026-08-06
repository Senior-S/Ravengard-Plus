package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.Color;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@Inject(method = "createHitbox", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$hideArmorStandHitboxes(
			Entity entity,
			float tickDelta,
			boolean serverEntity,
			CallbackInfoReturnable<EntityHitboxAndView> callbackInfo
	) {
		if (RavengardConfig.HANDLER.instance().hideArmorStandHitboxes && entity instanceof ArmorStandEntity) {
			callbackInfo.setReturnValue(null);
		}
	}

	@ModifyArgs(
			method = "createHitbox",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/entity/state/EntityHitbox;<init>(DDDDDDFFF)V"
			)
	)
	private void ravengardPlus$colorHitbox(
			Args arguments,
			Entity entity,
			float tickDelta,
			boolean serverEntity
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		Color color = entity instanceof PlayerEntity ? config.playerHitboxColor : config.mobHitboxColor;
		arguments.set(6, color.getRed() / 255.0F);
		arguments.set(7, color.getGreen() / 255.0F);
		arguments.set(8, color.getBlue() / 255.0F);
	}
}
