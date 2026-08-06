package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRenderHitBoxes", at = @At("RETURN"), cancellable = true)
	private void ravengardPlus$alwaysShowHitboxes(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (RavengardConfig.HANDLER.instance().alwaysShowHitboxes) {
			callbackInfo.setReturnValue(true);
		}
	}
}
