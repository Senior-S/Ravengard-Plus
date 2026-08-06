package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
	@Unique
	private static final int ravengardPlus$TOTAL_RIGHT_X = 168;
	@Unique
	private static final int ravengardPlus$TOTAL_Y = 75;
	@Unique
	private int ravengardPlus$cachedChangeCount = Integer.MIN_VALUE;
	@Unique
	private String ravengardPlus$cachedGlyph = "";
	@Unique
	private String ravengardPlus$cachedTotal = "0";
	@Unique
	private boolean ravengardPlus$cachedHasRavengardItem;

	@Inject(method = "renderLabels", at = @At("TAIL"))
	private void ravengardPlus$drawInventoryTotal(
			GuiGraphics graphics,
			int mouseX,
			int mouseY,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		Minecraft client = Minecraft.getInstance();
		if (!config.inventoryTotalEnabled || client.player == null) {
			return;
		}

		Inventory inventory = client.player.getInventory();
		if (inventory.getTimesChanged() != ravengardPlus$cachedChangeCount) {
			BigDecimal total = BigDecimal.ZERO;
			ravengardPlus$cachedGlyph = "";
			ravengardPlus$cachedHasRavengardItem = false;
			for (int index = 0; index < inventory.getContainerSize(); index++) {
				ItemStack stack = inventory.getItem(index);
				ResourceLocation itemModel = stack.get(DataComponents.ITEM_MODEL);
				if (itemModel != null && itemModel.getNamespace().equals("hypixel_ravengard")) {
					ravengardPlus$cachedHasRavengardItem = true;
				}
				double value = LoreValueParser.find(stack, "Crown");
				if (value < 0) {
					continue;
				}

				total = total.add(BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(stack.getCount())));
				if (ravengardPlus$cachedGlyph.isEmpty()) {
					LoreValueParser.Display display = LoreValueParser.findDisplay(stack, "Crown");
					if (display != null) {
						ravengardPlus$cachedGlyph = display.glyph();
					}
				}
			}
			ravengardPlus$cachedTotal = total.stripTrailingZeros().toPlainString();
			ravengardPlus$cachedChangeCount = inventory.getTimesChanged();
		}
		if (!ravengardPlus$cachedHasRavengardItem) {
			return;
		}

		Font font = client.font;
		boolean showGlyph = config.crownValueOverlayShowGlyph && !ravengardPlus$cachedGlyph.isEmpty();
		int width = font.width(ravengardPlus$cachedTotal)
				+ (showGlyph ? font.width(ravengardPlus$cachedGlyph) : 0);
		int drawX = ravengardPlus$TOTAL_RIGHT_X - width;
		if (showGlyph) {
			graphics.drawString(font, ravengardPlus$cachedGlyph, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayGlyphColor.getRGB(), true);
			drawX += font.width(ravengardPlus$cachedGlyph);
		}
		graphics.drawString(font, ravengardPlus$cachedTotal, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayColor.getRGB(), true);
	}
}
