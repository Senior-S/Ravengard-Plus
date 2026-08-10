package com.seniors.ravengardplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ScreenCompat {
	private static final Field SCREEN_FIELD;
	private static final Method SCREEN_METHOD;
	private static final Method SET_SCREEN_METHOD;

	static {
		Field screenField = null;
		Method screenMethod = null;
		Method setScreenMethod;
		try {
			screenField = Minecraft.class.getField("screen");
			setScreenMethod = Minecraft.class.getMethod("setScreen", Screen.class);
		} catch (NoSuchFieldException | NoSuchMethodException exception) {
			try {
				screenMethod = Gui.class.getMethod("screen");
				setScreenMethod = Gui.class.getMethod("setScreen", Screen.class);
			} catch (NoSuchMethodException missingScreenApi) {
				throw new ExceptionInInitializerError(missingScreenApi);
			}
		}
		SCREEN_FIELD = screenField;
		SCREEN_METHOD = screenMethod;
		SET_SCREEN_METHOD = setScreenMethod;
	}

	private ScreenCompat() {
	}

	public static Screen get(Minecraft client) {
		try {
			return SCREEN_FIELD != null
					? (Screen) SCREEN_FIELD.get(client)
					: (Screen) SCREEN_METHOD.invoke(client.gui);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalStateException("Could not read the current Minecraft screen", exception);
		}
	}

	public static void set(Minecraft client, Screen screen) {
		try {
			SET_SCREEN_METHOD.invoke(SCREEN_FIELD != null ? client : client.gui, screen);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalStateException("Could not change the current Minecraft screen", exception);
		}
	}
}
