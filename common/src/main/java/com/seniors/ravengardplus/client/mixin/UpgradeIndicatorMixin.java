package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.GuiQuads;
import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class UpgradeIndicatorMixin {
	@Inject(
			method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
			at = @At("TAIL")
	)
	private void ravengardPlus$drawUpgradeIndicator(
			LivingEntity entity,
			Level level,
			ItemStack stack,
			int x,
			int y,
			int seed,
			CallbackInfo callbackInfo
	) {
		RavengardConfig config = RavengardConfig.HANDLER.instance();
		int color;
		if (config.armorUpgradeIndicatorEnabled && ArmorUpgradeDetector.isUpgrade(stack)) {
			color = config.armorUpgradeColor.getRGB();
		} else if (config.accessoryUpgradeIndicatorEnabled && AccessoryUpgradeDetector.isUpgrade(stack)) {
			color = config.accessoryUpgradeColor.getRGB();
		} else {
			return;
		}

		GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
		GuiQuads.fill(graphics, x + 11, y - 1, x + 17, y + 6, 0xB0000000);
		GuiQuads.fill(graphics, x + 13, y, x + 15, y + 1, color);
		GuiQuads.fill(graphics, x + 12, y + 1, x + 16, y + 2, color);
		GuiQuads.fill(graphics, x + 13, y + 2, x + 15, y + 6, color);
	}
}
