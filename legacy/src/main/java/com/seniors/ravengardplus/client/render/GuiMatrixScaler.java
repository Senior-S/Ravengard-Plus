package com.seniors.ravengardplus.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.lang.reflect.Method;

public final class GuiMatrixScaler {
	private static Method getMatrices;
	private static Method pushMatrices;
	private static Method scaleMatrices;
	private static Method popMatrices;

	private GuiMatrixScaler() {
	}

	public static Object push(DrawContext context, float scale) {
		Object matrices = matrices(context);
		if (matrices instanceof Matrix3x2fStack pose) {
			pose.pushMatrix();
			pose.scale(scale, scale);
			return pose;
		}

		try {
			pushMatrices.invoke(matrices);
			scaleMatrices.invoke(matrices, scale, scale, 1.0F);
			return matrices;
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not scale the GUI matrix stack", exception);
		}
	}

	public static void pop(Object matrices) {
		if (matrices instanceof Matrix3x2fStack pose) {
			pose.popMatrix();
			return;
		}

		try {
			popMatrices.invoke(matrices);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not restore the GUI matrix stack", exception);
		}
	}

	private static Object matrices(DrawContext context) {
		try {
			if (getMatrices == null) {
				var resolver = FabricLoader.getInstance().getMappingResolver();
				String legacyGetter = resolver.mapMethodName(
						"intermediary", "net.minecraft.class_332", "method_51448", "()Lnet/minecraft/class_4587;"
				);
				String currentGetter = resolver.mapMethodName(
						"intermediary", "net.minecraft.class_332", "method_51448", "()Lorg/joml/Matrix3x2fStack;"
				);
				for (Method method : DrawContext.class.getMethods()) {
					if (method.getParameterCount() == 0
							&& (method.getName().equals(legacyGetter) || method.getName().equals(currentGetter))) {
						getMatrices = method;
						break;
					}
				}
				if (getMatrices == null) {
					throw new NoSuchMethodException("DrawContext matrix getter");
				}

				Class<?> matrixType = getMatrices.getReturnType();
				if (!Matrix3x2fStack.class.isAssignableFrom(matrixType)) {
					pushMatrices = matrixType.getMethod(resolver.mapMethodName(
							"intermediary", "net.minecraft.class_4587", "method_22903", "()V"
					));
					scaleMatrices = matrixType.getMethod(resolver.mapMethodName(
							"intermediary", "net.minecraft.class_4587", "method_22905", "(FFF)V"
					), float.class, float.class, float.class);
					popMatrices = matrixType.getMethod(resolver.mapMethodName(
							"intermediary", "net.minecraft.class_4587", "method_22909", "()V"
					));
				}
			}
			return getMatrices.invoke(context);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not access the GUI matrix stack", exception);
		}
	}
}
