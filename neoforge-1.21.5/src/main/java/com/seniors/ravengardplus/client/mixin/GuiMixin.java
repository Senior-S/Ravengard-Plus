package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void ravengardPlus$drawAbilityCooldowns(
			GuiGraphics graphics,
			DeltaTracker deltaTracker,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		Minecraft client = Minecraft.getInstance();
		if (!config.abilityCooldownEnabled || client.options.hideGui) {
			return;
		}

		Font font = client.font;
		float scale = Math.clamp(config.abilityCooldownScale, 50, 150) / 100.0F;
		for (int ability = 0; ability < 2; ability++) {
			String text = AbilityCooldownTracker.remainingText(ability);
			if (text == null) {
				continue;
			}

			int center = graphics.guiWidth() / 2 + (ability == AbilityCooldownTracker.DROP_ABILITY ? -123 : 136);
			if (scale == 1.0F) {
				graphics.drawCenteredString(font, text, center, graphics.guiHeight() - 69, config.abilityCooldownColor.getRGB());
				continue;
			}

			graphics.pose().pushPose();
			graphics.pose().translate(center, graphics.guiHeight() - 69, 0.0F);
			graphics.pose().scale(scale, scale, 1.0F);
			graphics.drawCenteredString(font, text, 0, 0, config.abilityCooldownColor.getRGB());
			graphics.pose().popPose();
		}
	}
}
