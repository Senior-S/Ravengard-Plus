package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
	@Unique
	private static final float ravengardPlus$CROWN_VALUE_SCALE = 0.6F;

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

		double value = LoreValueParser.find(stack, "Crown");
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

	@Inject(
			method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
			at = @At("TAIL")
	)
	private void ravengardPlus$drawCrownValue(
			LivingEntity entity,
			Level level,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
		if (config.crownReplacementEnabled) {
			if (CrownReplacementDetector.isCheapest(stack)) {
				graphics.fill(x, y, x + 16, y + 16, 0x66FFFFFF);
			} else if (CrownReplacementDetector.isRejectedContainerItem(stack)) {
				graphics.fill(x, y, x + 16, y + 16, 0x70C0C0C0);
			}
		}
		if (!config.crownValueOverlayEnabled) {
			return;
		}

		LoreValueParser.Display display = LoreValueParser.findDisplay(stack, "Crown");
		if (display == null) {
			return;
		}

		Font font = Minecraft.getInstance().font;
		int valueX = Math.round(x / ravengardPlus$CROWN_VALUE_SCALE);
		int valueY = Math.round((y - 1) / ravengardPlus$CROWN_VALUE_SCALE);
		graphics.pose().pushMatrix();
		graphics.pose().scale(ravengardPlus$CROWN_VALUE_SCALE, ravengardPlus$CROWN_VALUE_SCALE);
		if (config.crownValueOverlayShowGlyph && !display.glyph().isEmpty()) {
			graphics.text(font, display.glyph(), valueX, valueY, config.crownValueOverlayGlyphColor.getRGB(), true);
			valueX += font.width(display.glyph());
		}
		graphics.text(font, display.value(), valueX, valueY, config.crownValueOverlayColor.getRGB(), true);
		graphics.pose().popMatrix();
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
