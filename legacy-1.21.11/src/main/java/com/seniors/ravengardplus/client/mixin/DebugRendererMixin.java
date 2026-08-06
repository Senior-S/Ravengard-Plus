package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Unique
	private boolean ravengardPlus$alwaysShowHitboxes;

	@Inject(method = "render", at = @At("HEAD"))
	private void ravengardPlus$refreshHitboxRenderer(
			Frustum frustum,
			double cameraX,
			double cameraY,
			double cameraZ,
			float tickProgress,
			CallbackInfo callbackInfo
	) {
		boolean alwaysShow = RavengardConfig.HANDLER.instance().alwaysShowHitboxes;
		if (alwaysShow != ravengardPlus$alwaysShowHitboxes) {
			ravengardPlus$alwaysShowHitboxes = alwaysShow;
			((DebugRenderer) (Object) this).initRenderers();
		}
	}
}
