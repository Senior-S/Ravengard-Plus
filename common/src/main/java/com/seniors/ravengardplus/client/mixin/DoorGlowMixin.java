package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.door.DoorTracker;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class DoorGlowMixin {
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
			at = @At("TAIL")
	)
	private void ravengardPlus$outlineDoors(Entity entity, EntityRenderState state, float partialTick, CallbackInfo callbackInfo) {
		Integer color = DoorTracker.glowColor(entity);
		if (color != null) {
			state.outlineColor = ARGB.opaque(color);
		}
	}
}
