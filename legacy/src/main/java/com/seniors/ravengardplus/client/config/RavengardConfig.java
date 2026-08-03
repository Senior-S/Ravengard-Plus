package com.seniors.ravengardplus.client.config;

import com.seniors.ravengardplus.client.RavengardPlusLegacyClient;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RavengardConfig {
	private static final Color DEFAULT_MEDIUM_COLOR = new Color(0x55FF55);
	private static final Color DEFAULT_HIGH_COLOR = new Color(0xFF5555);
	private static final Color DEFAULT_CROWN_GLYPH_COLOR = new Color(0xFFD34E);
	private static final Color DEFAULT_ARMOR_UPGRADE_COLOR = new Color(0x55FF7F);
	private static final Color DEFAULT_ACCESSORY_UPGRADE_COLOR = new Color(0x55D6FF);

	public static final ConfigClassHandler<RavengardConfig> HANDLER = ConfigClassHandler
			.createBuilder(RavengardConfig.class)
			.id(RavengardPlusLegacyClient.id("config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("ravengard-plus.json5"))
					.setJson5(true)
					.build())
			.build();

	@SerialEntry
	public boolean crownBordersEnabled = true;

	@SerialEntry
	public BorderStyle crownBorderStyle = BorderStyle.SOFT_TINT;

	@SerialEntry
	public boolean crownValueOverlayEnabled = false;

	@SerialEntry
	public boolean crownValueOverlayShowGlyph = true;

	@SerialEntry
	public Color crownValueOverlayColor = Color.WHITE;

	@SerialEntry
	public Color crownValueOverlayGlyphColor = DEFAULT_CROWN_GLYPH_COLOR;

	@SerialEntry
	public boolean inventoryTotalEnabled = true;

	@SerialEntry
	public boolean armorUpgradeIndicatorEnabled = true;

	@SerialEntry
	public Color armorUpgradeColor = DEFAULT_ARMOR_UPGRADE_COLOR;

	@SerialEntry
	public boolean accessoryUpgradeIndicatorEnabled = true;

	@SerialEntry
	public Color accessoryUpgradeColor = DEFAULT_ACCESSORY_UPGRADE_COLOR;

	@SerialEntry
	public List<CrownTier> crownTiers = new ArrayList<>(List.of(
			new CrownTier(4, Color.WHITE),
			new CrownTier(9, DEFAULT_MEDIUM_COLOR),
			new CrownTier(-1, DEFAULT_HIGH_COLOR)
	));

	public static Screen createScreen(Screen parent) {
		RavengardConfig config = HANDLER.instance();

		ConfigCategory.Builder category = ConfigCategory.createBuilder()
						.name(Text.translatable("ravengard-plus.config.crown_borders"))
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.enabled"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.enabled.description")))
								.binding(true, () -> config.crownBordersEnabled, value -> config.crownBordersEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<BorderStyle>createBuilder()
								.name(Text.translatable("ravengard-plus.config.border_style"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.border_style.description")))
								.binding(BorderStyle.SOFT_TINT, () -> config.crownBorderStyle, value -> config.crownBorderStyle = value)
								.controller(option -> EnumControllerBuilder.create(option).enumClass(BorderStyle.class))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.crown_value_overlay"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.crown_value_overlay.description")))
								.binding(false, () -> config.crownValueOverlayEnabled, value -> config.crownValueOverlayEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.crown_value_overlay_glyph"))
								.binding(true, () -> config.crownValueOverlayShowGlyph, value -> config.crownValueOverlayShowGlyph = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("ravengard-plus.config.crown_value_overlay_color"))
								.binding(Color.WHITE, () -> config.crownValueOverlayColor, value -> config.crownValueOverlayColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("ravengard-plus.config.crown_value_overlay_glyph_color"))
								.binding(DEFAULT_CROWN_GLYPH_COLOR, () -> config.crownValueOverlayGlyphColor, value -> config.crownValueOverlayGlyphColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.inventory_total"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.inventory_total.description")))
								.binding(true, () -> config.inventoryTotalEnabled, value -> config.inventoryTotalEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("ravengard-plus.config.add_tier"))
								.text(Text.translatable("ravengard-plus.config.add"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.add_tier.description")))
								.action((screen, button) -> {
									int nextMaximum = config.crownTiers.stream()
											.mapToInt(tier -> tier.maximumValue)
											.filter(maximum -> maximum >= 0)
											.max()
											.orElse(-1) + 1;
									config.crownTiers.add(new CrownTier(nextMaximum, Color.WHITE));
									saveAndRefresh(parent, screen);
								})
								.build());

		for (int index = 0; index < config.crownTiers.size(); index++) {
			CrownTier tier = config.crownTiers.get(index);
			Text tierName = tier.maximumValue < 0
					? Text.translatable("ravengard-plus.config.tier_unlimited", index + 1)
					: Text.translatable("ravengard-plus.config.tier", index + 1, tier.maximumValue);

			category.group(OptionGroup.createBuilder()
					.name(tierName)
					.collapsed(true)
					.option(Option.<Integer>createBuilder()
							.name(Text.translatable("ravengard-plus.config.maximum_value"))
							.description(OptionDescription.of(Text.translatable("ravengard-plus.config.maximum_value.description")))
							.binding(tier.maximumValue, () -> tier.maximumValue, value -> tier.maximumValue = value)
							.controller(option -> IntegerFieldControllerBuilder.create(option).min(-1))
							.build())
					.option(Option.<Color>createBuilder()
							.name(Text.translatable("ravengard-plus.config.tier_color"))
							.binding(tier.color, () -> tier.color, value -> tier.color = value)
							.controller(ColorControllerBuilder::create)
							.build())
					.option(ButtonOption.createBuilder()
							.name(Text.translatable("ravengard-plus.config.remove_tier"))
							.text(Text.translatable("ravengard-plus.config.remove"))
							.action((screen, button) -> {
								config.crownTiers.remove(tier);
								saveAndRefresh(parent, screen);
							})
							.build())
					.build());
		}

		return YetAnotherConfigLib.createBuilder()
				.title(Text.translatable("ravengard-plus.config.title"))
				.category(category.build())
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("ravengard-plus.config.armor_upgrades"))
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.armor_upgrade_indicator"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.armor_upgrade_indicator.description")))
								.binding(true, () -> config.armorUpgradeIndicatorEnabled, value -> config.armorUpgradeIndicatorEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("ravengard-plus.config.armor_upgrade_color"))
								.binding(DEFAULT_ARMOR_UPGRADE_COLOR, () -> config.armorUpgradeColor, value -> config.armorUpgradeColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("ravengard-plus.config.accessory_upgrades"))
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("ravengard-plus.config.accessory_upgrade_indicator"))
								.description(OptionDescription.of(Text.translatable("ravengard-plus.config.accessory_upgrade_indicator.description")))
								.binding(true, () -> config.accessoryUpgradeIndicatorEnabled, value -> config.accessoryUpgradeIndicatorEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("ravengard-plus.config.accessory_upgrade_color"))
								.binding(DEFAULT_ACCESSORY_UPGRADE_COLOR, () -> config.accessoryUpgradeColor, value -> config.accessoryUpgradeColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.save(HANDLER::save)
				.build()
				.generateScreen(parent);
	}

	private static void saveAndRefresh(Screen parent, YACLScreen screen) {
		screen.finishOrSave();
		HANDLER.save();
		MinecraftClient.getInstance().setScreen(createScreen(parent));
	}

	public enum BorderStyle implements NameableEnum {
		PIXEL_CORNERS("pixel_corners"),
		BOTTOM_STRIPE("bottom_stripe"),
		SIDE_MARKERS("side_markers"),
		SOFT_TINT("soft_tint"),
		FULL_OUTLINE("full_outline");

		private final String translationKey;

		BorderStyle(String translationKey) {
			this.translationKey = translationKey;
		}

		@Override
		public Text getDisplayName() {
			return Text.translatable("ravengard-plus.config.border_style." + translationKey);
		}
	}

	public static class CrownTier {
		public int maximumValue;
		public Color color;

		public CrownTier(int maximumValue, Color color) {
			this.maximumValue = maximumValue;
			this.color = color;
		}
	}
}
