package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import com.seniors.ravengardplus.client.render.GuiMatrixScaler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(DrawContext.class)
public class DrawContextMixin {
	@Unique
	private static final float ravengardPlus$CROWN_VALUE_SCALE = 0.6F;

	@Group(name = "ravengardPlusCrownBorder", min = 1, max = 1)
	@Inject(
			method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;III)V",
			at = @At("HEAD"),
			require = 0
	)
	private void ravengardPlus$drawCrownBorder1215(
			LivingEntity entity,
			World world,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		ravengardPlus$drawCrownBorder(stack, x, y);
	}

	@Group(name = "ravengardPlusCrownBorder", min = 1, max = 1)
	@Inject(
			method = "method_51425(Lnet/minecraft/class_1309;Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;III)V",
			at = @At("HEAD"),
			require = 0,
			remap = false
	)
	private void ravengardPlus$drawCrownBorder1216(
			LivingEntity entity,
			World world,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		ravengardPlus$drawCrownBorder(stack, x, y);
	}

	@Group(name = "ravengardPlusUpgradeIndicator", min = 1, max = 1)
	@Inject(
			method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;III)V",
			at = @At("TAIL"),
			require = 0
	)
	private void ravengardPlus$drawUpgradeIndicator1215(
			LivingEntity entity,
			World world,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		ravengardPlus$drawUpgradeIndicator(stack, x, y);
		ravengardPlus$drawCrownValue(stack, x, y);
	}

	@Group(name = "ravengardPlusUpgradeIndicator", min = 1, max = 1)
	@Inject(
			method = "method_51425(Lnet/minecraft/class_1309;Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;III)V",
			at = @At("TAIL"),
			require = 0,
			remap = false
	)
	private void ravengardPlus$drawUpgradeIndicator1216(
			LivingEntity entity,
			World world,
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
	private void ravengardPlus$drawCrownBorder(ItemStack stack, int x, int y) {
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

	@Unique
	private void ravengardPlus$drawBorder(RavengardConfig.BorderStyle style, int x, int y, int color) {
		DrawContext context = (DrawContext) (Object) this;
		switch (style) {
			case PIXEL_CORNERS -> {
				context.fill(x - 1, y - 1, x + 3, y, color);
				context.fill(x - 1, y - 1, x, y + 3, color);
				context.fill(x + 13, y - 1, x + 17, y, color);
				context.fill(x + 16, y - 1, x + 17, y + 3, color);
				context.fill(x - 1, y + 16, x + 3, y + 17, color);
				context.fill(x - 1, y + 13, x, y + 17, color);
				context.fill(x + 13, y + 16, x + 17, y + 17, color);
				context.fill(x + 16, y + 13, x + 17, y + 17, color);
			}
			case BOTTOM_STRIPE -> context.fill(x + 2, y + 16, x + 14, y + 18, color);
			case SIDE_MARKERS -> {
				context.fill(x - 1, y + 5, x, y + 11, color);
				context.fill(x + 16, y + 5, x + 17, y + 11, color);
			}
			case SOFT_TINT -> context.fill(x - 1, y - 1, x + 17, y + 17, (color & 0x00FFFFFF) | 0x38000000);
			case FULL_OUTLINE -> {
				context.fill(x - 1, y - 1, x + 17, y, color);
				context.fill(x - 1, y + 16, x + 17, y + 17, color);
				context.fill(x - 1, y, x, y + 16, color);
				context.fill(x + 16, y, x + 17, y + 16, color);
			}
		}
	}

	@Unique
	private void ravengardPlus$drawUpgradeIndicator(ItemStack stack, int x, int y) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		DrawContext context = (DrawContext) (Object) this;
		if (config.crownReplacementEnabled) {
			if (CrownReplacementDetector.isCheapest(stack)) {
				context.fill(x, y, x + 16, y + 16, 0x66FFFFFF);
			} else if (CrownReplacementDetector.isRejectedContainerItem(stack)) {
				context.fill(x, y, x + 16, y + 16, 0x70C0C0C0);
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

		context.fill(x + 11, y - 1, x + 17, y + 6, 0xB0000000);
		context.fill(x + 13, y, x + 15, y + 1, color);
		context.fill(x + 12, y + 1, x + 16, y + 2, color);
		context.fill(x + 13, y + 2, x + 15, y + 6, color);
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

		DrawContext context = (DrawContext) (Object) this;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int valueX = Math.round(x / ravengardPlus$CROWN_VALUE_SCALE);
		int valueY = Math.round((y - 1) / ravengardPlus$CROWN_VALUE_SCALE);
		Object matrices = GuiMatrixScaler.push(context, ravengardPlus$CROWN_VALUE_SCALE);
		if (config.crownValueOverlayShowGlyph && !display.glyph().isEmpty()) {
			context.drawText(textRenderer, display.glyph(), valueX, valueY, config.crownValueOverlayGlyphColor.getRGB(), true);
			valueX += textRenderer.getWidth(display.glyph());
		}
		context.drawText(textRenderer, display.value(), valueX, valueY, config.crownValueOverlayColor.getRGB(), true);
		GuiMatrixScaler.pop(matrices);
	}
}
