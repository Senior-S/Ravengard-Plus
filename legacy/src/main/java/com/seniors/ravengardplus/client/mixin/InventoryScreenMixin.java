package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
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

	@Inject(method = "drawForeground", at = @At("TAIL"))
	private void ravengardPlus$drawInventoryTotal(
			DrawContext context,
			int mouseX,
			int mouseY,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		MinecraftClient client = MinecraftClient.getInstance();
		if (!config.inventoryTotalEnabled || client.player == null) {
			return;
		}

		PlayerInventory inventory = client.player.getInventory();
		if (inventory.getChangeCount() != ravengardPlus$cachedChangeCount) {
			BigDecimal total = BigDecimal.ZERO;
			ravengardPlus$cachedGlyph = "";
			ravengardPlus$cachedHasRavengardItem = false;
			for (int index = 0; index < inventory.size(); index++) {
				ItemStack stack = inventory.getStack(index);
				Identifier itemModel = stack.get(DataComponentTypes.ITEM_MODEL);
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
			ravengardPlus$cachedChangeCount = inventory.getChangeCount();
		}
		if (!ravengardPlus$cachedHasRavengardItem) {
			return;
		}

		TextRenderer textRenderer = client.textRenderer;
		boolean showGlyph = config.crownValueOverlayShowGlyph && !ravengardPlus$cachedGlyph.isEmpty();
		int width = textRenderer.getWidth(ravengardPlus$cachedTotal)
				+ (showGlyph ? textRenderer.getWidth(ravengardPlus$cachedGlyph) : 0);
		int drawX = ravengardPlus$TOTAL_RIGHT_X - width;
		if (showGlyph) {
			context.drawText(textRenderer, ravengardPlus$cachedGlyph, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayGlyphColor.getRGB(), true);
			drawX += textRenderer.getWidth(ravengardPlus$cachedGlyph);
		}
		context.drawText(textRenderer, ravengardPlus$cachedTotal, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayColor.getRGB(), true);
	}
}
