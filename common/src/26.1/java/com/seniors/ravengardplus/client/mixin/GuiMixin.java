package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.door.DoorTracker;
import com.seniors.ravengardplus.client.marker.MapCenterMarker;
import com.seniors.ravengardplus.client.ping.PingMarkers;
import com.seniors.ravengardplus.client.ping.PingWheel;
import com.seniors.ravengardplus.client.portal.ExitPortalTracker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void ravengardPlus$drawMapCenterMarker(
			GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker,
			CallbackInfo callbackInfo
	) {
		if (!Minecraft.getInstance().options.hideGui) {
			DoorTracker.drawMarkers(graphics);
			ExitPortalTracker.drawMarkers(graphics);
			MapCenterMarker.draw(graphics);
			PingMarkers.draw(graphics);
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void ravengardPlus$drawAbilityCooldowns(
			GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker,
			CallbackInfo callbackInfo
	) {
		if (!Minecraft.getInstance().options.hideGui) {
			PingWheel.draw(graphics);
		}
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.abilityCooldownEnabled || Minecraft.getInstance().options.hideGui) {
			return;
		}

		Font font = Minecraft.getInstance().font;
		float scale = Math.clamp(config.abilityCooldownScale, 50, 150) / 100.0F;
		for (int ability = 0; ability < 2; ability++) {
			String text = AbilityCooldownTracker.remainingText(ability);
			if (text == null) {
				continue;
			}

			int center = graphics.guiWidth() / 2 + (ability == AbilityCooldownTracker.DROP_ABILITY ? -123 : 136);
			if (scale == 1.0F) {
				graphics.centeredText(font, text, center, graphics.guiHeight() - 69, config.abilityCooldownColor.getRGB());
				continue;
			}

			graphics.pose().pushMatrix();
			graphics.pose().translate(center, graphics.guiHeight() - 69);
			graphics.pose().scale(scale, scale);
			graphics.centeredText(font, text, 0, 0, config.abilityCooldownColor.getRGB());
			graphics.pose().popMatrix();
		}
	}
}
