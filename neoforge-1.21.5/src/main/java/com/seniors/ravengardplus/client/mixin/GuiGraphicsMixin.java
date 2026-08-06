package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@Unique
	private static final float ravengardPlus$CROWN_VALUE_SCALE = 0.6F;

	@Inject(
			method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
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
			ravengardPlus$drawBorder(config.crownBorderStyle, x, y, color.getRGB());
		}
	}

	@Inject(
			method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
			at = @At("TAIL")
	)
	private void ravengardPlus$drawIndicators(
			LivingEntity entity,
			Level level,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		ravengardPlus$drawUpgradeIndicator(stack, x, y);
		ravengardPlus$drawCrownValue(stack, x, y);
	}

	@Unique
	private void ravengardPlus$drawBorder(RavengardConfig.BorderStyle style, int x, int y, int color) {
		GuiGraphics graphics = (GuiGraphics) (Object) this;
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
			case FULL_OUTLINE -> {
				graphics.fill(x - 1, y - 1, x + 17, y, color);
				graphics.fill(x - 1, y + 16, x + 17, y + 17, color);
				graphics.fill(x - 1, y, x, y + 16, color);
				graphics.fill(x + 16, y, x + 17, y + 16, color);
			}
		}
	}

	@Unique
	private void ravengardPlus$drawUpgradeIndicator(ItemStack stack, int x, int y) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		GuiGraphics graphics = (GuiGraphics) (Object) this;
		if (config.crownReplacementEnabled) {
			if (CrownReplacementDetector.isCheapest(stack)) {
				graphics.fill(x, y, x + 16, y + 16, 0x66FFFFFF);
			} else if (CrownReplacementDetector.isRejectedContainerItem(stack)) {
				graphics.fill(x, y, x + 16, y + 16, 0x70C0C0C0);
			}
		}

		int color;
		if (config.armorUpgradeIndicatorEnabled && ArmorUpgradeDetector.isUpgrade(stack)) {
			color = config.armorUpgradeColor.getRGB();
		} else if (config.accessoryUpgradeIndicatorEnabled && AccessoryUpgradeDetector.isUpgrade(stack)) {
			color = config.accessoryUpgradeColor.getRGB();
		} else {
			return;
		}

		graphics.fill(x + 11, y - 1, x + 17, y + 6, 0xB0000000);
		graphics.fill(x + 13, y, x + 15, y + 1, color);
		graphics.fill(x + 12, y + 1, x + 16, y + 2, color);
		graphics.fill(x + 13, y + 2, x + 15, y + 6, color);
	}

	@Unique
	private void ravengardPlus$drawCrownValue(ItemStack stack, int x, int y) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		if (!config.crownValueOverlayEnabled) {
			return;
		}

		LoreValueParser.Display display = LoreValueParser.findDisplay(stack, "Crown");
		if (display == null) {
			return;
		}

		GuiGraphics graphics = (GuiGraphics) (Object) this;
		Font font = Minecraft.getInstance().font;
		int valueX = Math.round(x / ravengardPlus$CROWN_VALUE_SCALE);
		int valueY = Math.round((y - 1) / ravengardPlus$CROWN_VALUE_SCALE);
		graphics.pose().pushPose();
		graphics.pose().scale(ravengardPlus$CROWN_VALUE_SCALE, ravengardPlus$CROWN_VALUE_SCALE, 1.0F);
		if (config.crownValueOverlayShowGlyph && !display.glyph().isEmpty()) {
			graphics.drawString(font, display.glyph(), valueX, valueY, config.crownValueOverlayGlyphColor.getRGB(), true);
			valueX += font.width(display.glyph());
		}
		graphics.drawString(font, display.value(), valueX, valueY, config.crownValueOverlayColor.getRGB(), true);
		graphics.pose().popPose();
	}
}
