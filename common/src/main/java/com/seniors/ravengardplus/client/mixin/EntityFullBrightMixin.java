package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityFullBrightMixin {
	@Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$applyEntityFullBright(
			Entity entity,
			float partialTick,
			CallbackInfoReturnable<Integer> callbackInfo
	) {
		if (RavengardConfig.HANDLER.instance().entityFullBrightEnabled) {
			callbackInfo.setReturnValue(0xF000F0);
		}
	}
}
