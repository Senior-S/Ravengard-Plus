package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.GuiQuads;
import com.seniors.ravengardplus.client.ScreenCompat;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import com.seniors.ravengardplus.client.item.CrownReplacementDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
	@Unique
	private static final float ravengardPlus$CROWN_VALUE_SCALE = 0.6F;

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
		// A container item not worth replacing with keeps the gray tint, even if it's the most valuable
		if (config.mostValuableHighlightEnabled && CrownReplacementDetector.isMostValuableContainerItem(stack)
				&& !(config.crownReplacementEnabled && CrownReplacementDetector.isRejectedContainerItem(stack))) {
			// A light tint keeps the item readable, and the solid outline keeps it easy to spot
			int color = config.mostValuableHighlightColor.getRGB() & 0x00FFFFFF;
			GuiQuads.fill(graphics, x, y, x + 16, y + 16, color | 0x40000000);
			GuiQuads.outline(graphics, x, y, 16, 16, color | 0xFF000000);
		}
		if (config.crownReplacementEnabled) {
			if (CrownReplacementDetector.isCheapest(stack)) {
				GuiQuads.fill(graphics, x, y, x + 16, y + 16, 0x66FFFFFF);
			} else if (CrownReplacementDetector.isRejectedContainerItem(stack)) {
				GuiQuads.fill(graphics, x, y, x + 16, y + 16, 0x70C0C0C0);
			}
		}
		// Prices only show while an inventory or container is open, not on the hotbar
		if (!config.crownValueOverlayEnabled
				|| !(ScreenCompat.get(Minecraft.getInstance()) instanceof AbstractContainerScreen<?>)) {
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
}
