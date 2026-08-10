package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugScreenEntryList.class)
public class DebugScreenEntryListMixin {
	@Inject(method = "isCurrentlyEnabled", at = @At("RETURN"), cancellable = true)
	private void ravengardPlus$alwaysEnableHitboxes(Identifier entry, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (RavengardConfig.HANDLER.instance().alwaysShowHitboxes
				&& DebugScreenEntries.ENTITY_HITBOXES.equals(entry)) {
			callbackInfo.setReturnValue(true);
		}
	}
}
