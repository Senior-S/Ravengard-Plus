package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Unique
	private boolean ravengardPlus$alwaysShowHitboxes;

	@Inject(method = "emitGizmos", at = @At("HEAD"))
	private void ravengardPlus$refreshHitboxRenderer(
			Frustum frustum,
			double cameraX,
			double cameraY,
			double cameraZ,
			float partialTick,
			CallbackInfo callbackInfo
	) {
		boolean alwaysShow = RavengardConfig.HANDLER.instance().alwaysShowHitboxes;
		if (alwaysShow != ravengardPlus$alwaysShowHitboxes) {
			ravengardPlus$alwaysShowHitboxes = alwaysShow;
			((DebugRenderer) (Object) this).refreshRendererList();
		}
	}
}
