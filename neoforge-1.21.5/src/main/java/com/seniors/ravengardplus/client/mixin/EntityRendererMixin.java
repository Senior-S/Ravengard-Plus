package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.Color;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@Inject(method = "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$hideArmorStandHitboxes(
			Entity entity,
			float partialTick,
			boolean serverEntity,
			CallbackInfoReturnable<HitboxesRenderState> callbackInfo
	) {
		if (RavengardConfig.HANDLER.instance().hideArmorStandHitboxes && entity instanceof ArmorStand) {
			callbackInfo.setReturnValue(null);
		}
	}

	@ModifyArgs(
			method = "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/state/HitboxRenderState;<init>(DDDDDDFFF)V",
					ordinal = 0
			)
	)
	private void ravengardPlus$colorHitbox(
			Args arguments,
			Entity entity,
			float partialTick,
			boolean serverEntity
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		Color color = entity instanceof Player ? config.playerHitboxColor : config.mobHitboxColor;
		arguments.set(6, color.getRed() / 255.0F);
		arguments.set(7, color.getGreen() / 255.0F);
		arguments.set(8, color.getBlue() / 255.0F);
	}
}
