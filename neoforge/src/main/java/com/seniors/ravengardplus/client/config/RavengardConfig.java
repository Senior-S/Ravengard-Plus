package com.seniors.ravengardplus.client.config;

import com.seniors.ravengardplus.client.ScreenCompat;
import com.seniors.ravengardplus.client.RavengardPlusClient;
import com.seniors.ravengardplus.client.ability.AbilityCooldownTracker;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RavengardConfig {
	private static final Color DEFAULT_MEDIUM_COLOR = new Color(0x55FF55);
	private static final Color DEFAULT_HIGH_COLOR = new Color(0xFF5555);
	private static final Color DEFAULT_CROWN_GLYPH_COLOR = new Color(0xFFD34E);
	private static final Color DEFAULT_ARMOR_UPGRADE_COLOR = new Color(0x55FF7F);
	private static final Color DEFAULT_ACCESSORY_UPGRADE_COLOR = new Color(0x55D6FF);
	private static final Color DEFAULT_PLAYER_HITBOX_COLOR = new Color(0xFF0000);
	private static final String DEFAULT_ABILITY_SOUND = "minecraft:block.note_block.pling";

	public static final ConfigClassHandler<RavengardConfig> HANDLER = ConfigClassHandler
			.createBuilder(RavengardConfig.class)
			.id(RavengardPlusClient.id("config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(YACLPlatform.getConfigDir().resolve("ravengard-plus.json5"))
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
	public boolean crownReplacementEnabled = true;

	@SerialEntry
	public boolean armorUpgradeIndicatorEnabled = true;

	@SerialEntry
	public Color armorUpgradeColor = DEFAULT_ARMOR_UPGRADE_COLOR;

	@SerialEntry
	public boolean accessoryUpgradeIndicatorEnabled = true;

	@SerialEntry
	public Color accessoryUpgradeColor = DEFAULT_ACCESSORY_UPGRADE_COLOR;

	@SerialEntry
	public boolean abilityCooldownEnabled = true;

	@SerialEntry
	public Color abilityCooldownColor = Color.WHITE;

	@SerialEntry
	public int abilityCooldownScale = 100;

	@SerialEntry
	public boolean abilityCooldownSoundEnabled = true;

	@SerialEntry
	public String abilityCooldownSound = DEFAULT_ABILITY_SOUND;

	@SerialEntry
	public boolean alwaysShowHitboxes = false;

	@SerialEntry
	public boolean hideArmorStandHitboxes = true;

	@SerialEntry
	public Color playerHitboxColor = DEFAULT_PLAYER_HITBOX_COLOR;

	@SerialEntry
	public Color mobHitboxColor = Color.WHITE;

	@SerialEntry
	public List<CrownTier> crownTiers = new ArrayList<>(List.of(
			new CrownTier(4, Color.WHITE),
			new CrownTier(9, DEFAULT_MEDIUM_COLOR),
			new CrownTier(-1, DEFAULT_HIGH_COLOR)
	));

	public static Screen createScreen(Screen parent) {
		RavengardConfig config = HANDLER.instance();
		Option<String> abilitySoundOption = Option.<String>createBuilder()
				.name(Component.translatable("ravengard-plus.config.ability_cooldown_sound"))
				.description(OptionDescription.of(Component.translatable("ravengard-plus.config.ability_cooldown_sound.description")))
				.binding(DEFAULT_ABILITY_SOUND, () -> config.abilityCooldownSound, value -> config.abilityCooldownSound = value)
				.controller(option -> DropdownStringControllerBuilder.create(option)
						.values(BuiltInRegistries.SOUND_EVENT.keySet().stream().map(Object::toString).sorted().toList())
						.allowAnyValue(false))
				.build();

		ConfigCategory.Builder category = ConfigCategory.createBuilder()
						.name(Component.translatable("ravengard-plus.config.crown_borders"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.enabled"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.enabled.description")))
								.binding(true, () -> config.crownBordersEnabled, value -> config.crownBordersEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<BorderStyle>createBuilder()
								.name(Component.translatable("ravengard-plus.config.border_style"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.border_style.description")))
								.binding(BorderStyle.SOFT_TINT, () -> config.crownBorderStyle, value -> config.crownBorderStyle = value)
								.controller(option -> EnumControllerBuilder.create(option).enumClass(BorderStyle.class))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.crown_value_overlay"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.crown_value_overlay.description")))
								.binding(false, () -> config.crownValueOverlayEnabled, value -> config.crownValueOverlayEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.crown_value_overlay_glyph"))
								.binding(true, () -> config.crownValueOverlayShowGlyph, value -> config.crownValueOverlayShowGlyph = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.crown_value_overlay_color"))
								.binding(Color.WHITE, () -> config.crownValueOverlayColor, value -> config.crownValueOverlayColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.crown_value_overlay_glyph_color"))
								.binding(DEFAULT_CROWN_GLYPH_COLOR, () -> config.crownValueOverlayGlyphColor, value -> config.crownValueOverlayGlyphColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.inventory_total"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.inventory_total.description")))
								.binding(true, () -> config.inventoryTotalEnabled, value -> config.inventoryTotalEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.crown_replacement"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.crown_replacement.description")))
								.binding(true, () -> config.crownReplacementEnabled, value -> config.crownReplacementEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("ravengard-plus.config.add_tier"))
								.text(Component.translatable("ravengard-plus.config.add"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.add_tier.description")))
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
			Component tierName = tier.maximumValue < 0
					? Component.translatable("ravengard-plus.config.tier_unlimited", index + 1)
					: Component.translatable("ravengard-plus.config.tier", index + 1, tier.maximumValue);

			category.group(OptionGroup.createBuilder()
					.name(tierName)
					.collapsed(true)
					.option(Option.<Integer>createBuilder()
							.name(Component.translatable("ravengard-plus.config.maximum_value"))
							.description(OptionDescription.of(Component.translatable("ravengard-plus.config.maximum_value.description")))
							.binding(tier.maximumValue, () -> tier.maximumValue, value -> tier.maximumValue = value)
							.controller(option -> IntegerFieldControllerBuilder.create(option).min(-1))
							.build())
					.option(Option.<Color>createBuilder()
							.name(Component.translatable("ravengard-plus.config.tier_color"))
							.binding(tier.color, () -> tier.color, value -> tier.color = value)
							.controller(ColorControllerBuilder::create)
							.build())
					.option(ButtonOption.createBuilder()
							.name(Component.translatable("ravengard-plus.config.remove_tier"))
							.text(Component.translatable("ravengard-plus.config.remove"))
							.action((screen, button) -> {
								config.crownTiers.remove(tier);
								saveAndRefresh(parent, screen);
							})
							.build())
					.build());
		}

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("ravengard-plus.config.title"))
				.category(category.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("ravengard-plus.config.armor_upgrades"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.armor_upgrade_indicator"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.armor_upgrade_indicator.description")))
								.binding(true, () -> config.armorUpgradeIndicatorEnabled, value -> config.armorUpgradeIndicatorEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.armor_upgrade_color"))
								.binding(DEFAULT_ARMOR_UPGRADE_COLOR, () -> config.armorUpgradeColor, value -> config.armorUpgradeColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("ravengard-plus.config.accessory_upgrades"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.accessory_upgrade_indicator"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.accessory_upgrade_indicator.description")))
								.binding(true, () -> config.accessoryUpgradeIndicatorEnabled, value -> config.accessoryUpgradeIndicatorEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.accessory_upgrade_color"))
								.binding(DEFAULT_ACCESSORY_UPGRADE_COLOR, () -> config.accessoryUpgradeColor, value -> config.accessoryUpgradeColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("ravengard-plus.config.ability_cooldowns"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.ability_cooldown_enabled"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.ability_cooldown_enabled.description")))
								.binding(true, () -> config.abilityCooldownEnabled, value -> config.abilityCooldownEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.ability_cooldown_color"))
								.binding(Color.WHITE, () -> config.abilityCooldownColor, value -> config.abilityCooldownColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("ravengard-plus.config.ability_cooldown_scale"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.ability_cooldown_scale.description")))
								.binding(100, () -> config.abilityCooldownScale, value -> config.abilityCooldownScale = value)
								.controller(option -> IntegerSliderControllerBuilder.create(option).range(50, 150).step(5))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.ability_cooldown_sound_enabled"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.ability_cooldown_sound_enabled.description")))
								.binding(true, () -> config.abilityCooldownSoundEnabled, value -> config.abilityCooldownSoundEnabled = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(abilitySoundOption)
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("ravengard-plus.config.preview_ability_cooldown_sound"))
								.text(Component.translatable("ravengard-plus.config.preview"))
								.action((screen, button) -> AbilityCooldownTracker.playSound(abilitySoundOption.pendingValue()))
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("ravengard-plus.config.hitboxes"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.always_show_hitboxes"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.always_show_hitboxes.description")))
								.binding(false, () -> config.alwaysShowHitboxes, value -> config.alwaysShowHitboxes = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("ravengard-plus.config.hide_armor_stand_hitboxes"))
								.description(OptionDescription.of(Component.translatable("ravengard-plus.config.hide_armor_stand_hitboxes.description")))
								.binding(true, () -> config.hideArmorStandHitboxes, value -> config.hideArmorStandHitboxes = value)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.player_hitbox_color"))
								.binding(DEFAULT_PLAYER_HITBOX_COLOR, () -> config.playerHitboxColor, value -> config.playerHitboxColor = value)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("ravengard-plus.config.mob_hitbox_color"))
								.binding(Color.WHITE, () -> config.mobHitboxColor, value -> config.mobHitboxColor = value)
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
		ScreenCompat.set(Minecraft.getInstance(), createScreen(parent));
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
		public Component getDisplayName() {
			return Component.translatable("ravengard-plus.config.border_style." + translationKey);
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
