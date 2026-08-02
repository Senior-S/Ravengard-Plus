package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
	@Inject(
			method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
			at = @At("HEAD")
	)
	private void ravengardPlus$drawCrownBorder(
			LivingEntity entity,
			Level level,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.crownBordersEnabled) {
			return;
		}

		double value = LoreValueParser.find(stack, "Crowns");
		if (value < 0) {
			return;
		}

		Color color = null;
		int selectedMaximum = Integer.MAX_VALUE;
		for (RavengardConfig.CrownTier tier : config.crownTiers) {
			if (tier.maximumValue < 0) {
				if (color == null) {
					color = tier.color;
				}
			} else if (value <= tier.maximumValue && tier.maximumValue < selectedMaximum) {
				color = tier.color;
				selectedMaximum = tier.maximumValue;
			}
		}

		if (color != null) {
			ravengardPlus$drawBorder(
					(GuiGraphicsExtractor) (Object) this,
					config.crownBorderStyle,
					x,
					y,
					color.getRGB()
			);
		}
	}

	@Unique
	private static void ravengardPlus$drawBorder(
			GuiGraphicsExtractor graphics,
			RavengardConfig.BorderStyle style,
			int x,
			int y,
			int color
	) {
		switch (style) {
			case PIXEL_CORNERS -> {
				graphics.fill(x - 1, y - 1, x + 3, y, color);
				graphics.fill(x - 1, y - 1, x, y + 3, color);
				graphics.fill(x + 13, y - 1, x + 17, y, color);
				graphics.fill(x + 16, y - 1, x + 17, y + 3, color);
				graphics.fill(x - 1, y + 16, x + 3, y + 17, color);
				graphics.fill(x - 1, y + 13, x, y + 17, color);
				graphics.fill(x + 13, y + 16, x + 17, y + 17, color);
				graphics.fill(x + 16, y + 13, x + 17, y + 17, color);
			}
			case BOTTOM_STRIPE -> graphics.fill(x + 2, y + 16, x + 14, y + 18, color);
			case SIDE_MARKERS -> {
				graphics.fill(x - 1, y + 5, x, y + 11, color);
				graphics.fill(x + 16, y + 5, x + 17, y + 11, color);
			}
			case SOFT_TINT -> graphics.fill(x - 1, y - 1, x + 17, y + 17, (color & 0x00FFFFFF) | 0x38000000);
			case FULL_OUTLINE -> graphics.outline(x - 1, y - 1, 18, 18, color);
		}
	}
}
