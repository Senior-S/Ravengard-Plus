package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.config.RavengardConfig;
import com.seniors.ravengardplus.client.item.LoreValueParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
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
	private ItemStack[] ravengardPlus$cachedItems = new ItemStack[0];
	@Unique
	private String ravengardPlus$cachedGlyph = "";
	@Unique
	private String ravengardPlus$cachedTotal = "0";
	@Unique
	private boolean ravengardPlus$cachedHasRavengardItem;

	@Inject(method = "extractLabels", at = @At("TAIL"))
	private void ravengardPlus$drawInventoryTotal(
			GuiGraphicsExtractor graphics,
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
		int size = inventory.getContainerSize();
		boolean changed = ravengardPlus$cachedItems.length != size;
		for (int index = 0; !changed && index < size; index++) {
			changed = !ItemStack.matches(inventory.getItem(index), ravengardPlus$cachedItems[index]);
		}
		if (changed) {
			ravengardPlus$cachedItems = new ItemStack[size];
			BigDecimal total = BigDecimal.ZERO;
			ravengardPlus$cachedGlyph = "";
			ravengardPlus$cachedHasRavengardItem = false;
			for (int index = 0; index < size; index++) {
				ItemStack stack = inventory.getItem(index);
				ravengardPlus$cachedItems[index] = stack.copy();
				Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);
				if (itemModel != null && itemModel.getNamespace().equals("hypixel_ravengard")) {
					ravengardPlus$cachedHasRavengardItem = true;
				}
				LoreValueParser.Display display = LoreValueParser.findDisplay(stack, "Crown");
				if (display == null) {
					continue;
				}

				total = total.add(display.amount().multiply(BigDecimal.valueOf(stack.getCount())));
				if (ravengardPlus$cachedGlyph.isEmpty()) {
					ravengardPlus$cachedGlyph = display.glyph();
				}
			}
			ravengardPlus$cachedTotal = total.stripTrailingZeros().toPlainString();
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
			graphics.text(font, ravengardPlus$cachedGlyph, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayGlyphColor.getRGB(), true);
			drawX += font.width(ravengardPlus$cachedGlyph);
		}
		graphics.text(font, ravengardPlus$cachedTotal, drawX, ravengardPlus$TOTAL_Y, config.crownValueOverlayColor.getRGB(), true);
	}
}
