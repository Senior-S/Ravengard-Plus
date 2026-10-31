package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.team.TeammateGlowTracker;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class TeammateGlowMixin {
	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$useMinimapMarkerColor(CallbackInfoReturnable<Integer> callbackInfo) {
		Integer color = TeammateGlowTracker.color((Entity) (Object) this);
		if (color != null) {
			callbackInfo.setReturnValue(color);
		}
	}
}
