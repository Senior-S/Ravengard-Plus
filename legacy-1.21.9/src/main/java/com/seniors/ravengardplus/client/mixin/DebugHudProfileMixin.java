package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugHudProfile.class)
public class DebugHudProfileMixin {
	@Inject(method = "isEntryVisible", at = @At("RETURN"), cancellable = true)
	private void ravengardPlus$alwaysEnableHitboxes(Identifier entry, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (RavengardConfig.HANDLER.instance().alwaysShowHitboxes
				&& DebugHudEntries.ENTITY_HITBOXES.equals(entry)) {
			callbackInfo.setReturnValue(true);
		}
	}
}
