package com.seniors.ravengardplus.client;

import com.seniors.ravengardplus.client.accessory.AccessoryUpgradeDetector;
import com.seniors.ravengardplus.client.armor.ArmorUpgradeDetector;
import com.seniors.ravengardplus.client.config.RavengardConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RavengardPlusLegacyClient implements ClientModInitializer {
	public static final String MOD_ID = "ravengard-plus";

	@Override
	public void onInitializeClient() {
		RavengardConfig.HANDLER.load();

		KeyBinding openConfig = KeyBindingHelper.registerKeyBinding(createOpenConfigKey());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Iterable<ItemStack> candidates = null;
			if (client.player != null) {
				candidates = client.currentScreen instanceof HandledScreen<?> screen
						? screen.getScreenHandler().getStacks()
						: client.player.getInventory().getMainStacks();
			}
			ArmorUpgradeDetector.update(client.player, candidates);
			AccessoryUpgradeDetector.update(client.player, candidates);

			while (openConfig.wasPressed()) {
				client.setScreen(RavengardConfig.createScreen(client.currentScreen));
			}
		});
	}

	private static KeyBinding createOpenConfigKey() {
		try {
			for (Constructor<?> constructor : KeyBinding.class.getConstructors()) {
				Class<?>[] parameters = constructor.getParameterTypes();
				if (parameters.length != 4
						|| parameters[0] != String.class
						|| parameters[1] != InputUtil.Type.class
						|| parameters[2] != int.class) {
					continue;
				}

				Object category;
				if (parameters[3] == String.class) {
					category = "key.category.ravengard-plus.general";
				} else {
					category = null;
					for (Method method : parameters[3].getDeclaredMethods()) {
						if (Modifier.isStatic(method.getModifiers())
								&& method.getReturnType() == parameters[3]
								&& method.getParameterCount() == 1
								&& method.getParameterTypes()[0] == Identifier.class) {
							category = method.invoke(null, id("general"));
							break;
						}
					}

					if (category == null) {
						throw new IllegalStateException("Unable to register the Ravengard Plus key category");
					}
				}

				return (KeyBinding) constructor.newInstance(
						"key.ravengard-plus.open_config",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_O,
						category
				);
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create the Ravengard Plus key binding", exception);
		}

		throw new IllegalStateException("No compatible Minecraft key binding constructor was found");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
