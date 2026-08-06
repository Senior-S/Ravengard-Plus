package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.render.GuiMatrixScaler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void ravengardPlus$drawAbilityCooldowns(
			DrawContext context,
			RenderTickCounter tickCounter,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		MinecraftClient client = MinecraftClient.getInstance();
		if (!config.abilityCooldownEnabled || client.options.hudHidden) {
			return;
		}

		TextRenderer textRenderer = client.textRenderer;
		float scale = Math.max(50, Math.min(150, config.abilityCooldownScale)) / 100.0F;
		for (int ability = 0; ability < 2; ability++) {
			String text = AbilityCooldownTracker.remainingText(ability);
			if (text == null) {
				continue;
			}

			int center = context.getScaledWindowWidth() / 2 + (ability == AbilityCooldownTracker.DROP_ABILITY ? -123 : 136);
			if (scale == 1.0F) {
				context.drawCenteredTextWithShadow(textRenderer, text, center, context.getScaledWindowHeight() - 69, config.abilityCooldownColor.getRGB());
				continue;
			}

			int x = Math.round(center / scale);
			int y = Math.round((context.getScaledWindowHeight() - 69) / scale);
			Object matrices = GuiMatrixScaler.push(context, scale);
			context.drawCenteredTextWithShadow(textRenderer, text, x, y, config.abilityCooldownColor.getRGB());
			GuiMatrixScaler.pop(matrices);
		}
	}
}
