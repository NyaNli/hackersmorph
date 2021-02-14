package nyanli.hackersmorph.other.minecraft.common.manager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProvider;

public class RenderManager {
	
	private static boolean renderFlag = true;
	
	private static HashSet<EnumOption> enabled = new HashSet<>();
	private static HashMap<EnumOption, Double> hookedValue = new HashMap<>();
	
	private static Method setupCameraTransform = null;
	
//	private static ArrayStack<String> debugStack = new ArrayStack<>();
	
	private static boolean canGlColor = true;
	
	public static void enableRender() {
		renderFlag = true;
	}
	
	public static void disableRender() {
		renderFlag = false;
	}
	
	public static void enableHook(EnumOption option) {
		enabled.add(option);
	}
	
	public static void disableHook(EnumOption option) {
		enabled.remove(option);
		hookedValue.remove(option);
	}
	
	public static void disableAll() {
		enabled.clear();
		hookedValue.clear();
	}
	
	public static void setOption(EnumOption option, double value) {
		hookedValue.put(option, value);
	}

	public static boolean setupCameraTransform(float partialTicks) {
		if (setupCameraTransform == null) {
			try {
				setupCameraTransform = EntityRenderer.class.getDeclaredMethod("func_78479_a", float.class, int.class);
				setupCameraTransform.setAccessible(true);
			} catch (NoSuchMethodException | SecurityException e) {
				try {
					setupCameraTransform = EntityRenderer.class.getDeclaredMethod("setupCameraTransform", float.class, int.class);
					setupCameraTransform.setAccessible(true);
				} catch (NoSuchMethodException | SecurityException e1) {
					return false;
				}
			}
		}
		try {
			setupCameraTransform.invoke(Minecraft.getMinecraft().entityRenderer, partialTicks, 2);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
//	public static void pushRender() {
//		String caller = new Throwable().getStackTrace()[2].toString();
//		if (debugStack != null) {
//			HackersMorphASM.LOGGER.error("BufferBuilder.begin is called twice.");
//			HackersMorphASM.LOGGER.error(debugStack);
//			HackersMorphASM.LOGGER.error(caller);
//		}
//	}
//	
//	public static void popRender() {
//		String caller = new Throwable().getStackTrace()[2].toString();
//		if (debugStack == null) {
//			HackersMorphASM.LOGGER.error("Unexcepted BufferBuilder.finishDrawing");
//			HackersMorphASM.LOGGER.error(caller);
//		}
//	}

	public static void lockColor() {
		canGlColor = false;
	}
	
	public static void unlockColor() {
		canGlColor = true;
	}
	
	public static void doRender(WorldVertexBufferUploader uploader, BufferBuilder buffer) {
		if (renderFlag)
			uploader.draw(buffer);
		else
			buffer.reset();
	}
	
	public static Vec3d hookSkyColor(WorldProvider provider, Entity entity, float partialTicks) {
		Vec3d sky = provider.getSkyColor(entity, partialTicks);
		double skyR = getOption(EnumOption.SkyR, sky.x);
		double skyG = getOption(EnumOption.SkyG, sky.y);
		double skyB = getOption(EnumOption.SkyB, sky.z);
		return new Vec3d(skyR, skyG, skyB);
	}
	
	public static Vec3d hookCloudColor(WorldProvider provider, float partialTicks) {
		Vec3d cloud = provider.getCloudColor(partialTicks);
		double cloudR = getOption(EnumOption.CloudR, cloud.x);
		double cloudG = getOption(EnumOption.CloudG, cloud.y);
		double cloudB = getOption(EnumOption.CloudB, cloud.z);
		return new Vec3d(cloudR, cloudG, cloudB);
	}
	
	public static Vec3d hookFogColor(WorldProvider provider, float celestialAngle, float partialTicks) {
		Vec3d fog = provider.getFogColor(celestialAngle, partialTicks);
		double fogR = getOption(EnumOption.FogR, fog.x);
		double fogG = getOption(EnumOption.FogG, fog.y);
		double fogB = getOption(EnumOption.FogB, fog.z);
		return new Vec3d(fogR, fogG, fogB);
	}
	
	public static float hookFogDensity(float density) {
		return (float) getOption(EnumOption.FogDensity, density);
	}
	
	public static float hookFogStart(float start) {
		return (float) getOption(EnumOption.FogStart, start);
	}
	
	public static float hookFogEnd(float end) {
		return (float) getOption(EnumOption.FogEnd, end);
	}
	
	public static float hookCelestialAngle(WorldProvider provider, long worldTime, float partialTicks) {
		double angle = getOption(EnumOption.CelestialAngle, provider.calculateCelestialAngle(worldTime, partialTicks) * 360.0);
		while (angle < 0.0) angle += 360.0;
		while (angle > 360.0) angle -= 360.0;
		return (float) angle / 360.0f;
	}
	
	public static boolean canGlColor() {
		return canGlColor;
	}
	
	private static double getOption(EnumOption option, double defaultValue) {
		if (enabled.contains(option)) {
			Double value = hookedValue.get(option);
			if (value != null)
				return value;
		}
		return defaultValue;
	}

	public static enum EnumOption {
		SkyR, SkyG, SkyB, CloudR, CloudG, CloudB, FogR, FogG, FogB, FogStart, FogEnd, FogDensity, CelestialAngle
	}
	
}

