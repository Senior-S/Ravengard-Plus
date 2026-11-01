package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.ping.PingWheel;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	private double accumulatedDX;

	@Shadow
	private double accumulatedDY;

	// While the ping wheel is open, mouse movement picks an option instead of turning the camera
	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
	private void ravengardPlus$steerPingWheel(double movementTime, CallbackInfo callbackInfo) {
		if (PingWheel.isOpen()) {
			PingWheel.moveCursor(accumulatedDX, accumulatedDY);
			accumulatedDX = 0;
			accumulatedDY = 0;
			callbackInfo.cancel();
		}
	}
}
