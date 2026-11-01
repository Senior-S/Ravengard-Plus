package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.item.IgnoredItems;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Keybinds don't fire while a screen is open, so the ignore key is read from the container screen itself
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenIgnoreMixin {
	@Shadow
	protected Slot hoveredSlot;

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$ignoreHoveredItem(KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (IgnoredItems.handleKey(event, hoveredSlot)) {
			callbackInfo.setReturnValue(true);
		}
	}
}
