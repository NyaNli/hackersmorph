package nyanli.hackersmorph.other.mchorse.aperture.client.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.blockbuster.aperture.CameraHandler;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.scene.sync.PacketScenePlay;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.ICurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.VanillaOptionCurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.CenterDepthUniformCurve;
//import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.ShadowDistanceCurve;
//import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.ShadowSizeCurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.SunPathRotationCurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.UniformFloatCurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.UniformIntegerCurve;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader.WorldTimeUniformCurve;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.handler.DamageControlChecker;
import nyanli.hackersmorph.other.mchorse.metamorph.common.morph.CameraMorph;
import nyanli.hackersmorph.other.minecraft.common.manager.RenderManager;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public class CameraEditorManager {
	
	private static final ArrayList<VanillaOptionCurve> vaillaOptions = new ArrayList<>();
	
	private static final ArrayList<ICurve> curves = new ArrayList<>();
	
	static {
		for (RenderManager.EnumOption option : RenderManager.EnumOption.values())
			vaillaOptions.add(new VanillaOptionCurve(option));
	}
	
	public static void onGuiOpen(GuiCameraEditor editor) {
		updateCurves();
		editor.profiles.curves.update();
		DamageControlChecker.setEnable(false);
		CameraMorph.setEnabledDraw(false);
		Minecraft mc = Minecraft.getMinecraft();
		mc.setRenderViewEntity(mc.player);
		mc.gameSettings.thirdPersonView = 0;
	}

	public static void onGuiClose() {
		curves.clear();
		RenderManager.disableAll();
		ShaderManager.disableAll();
		DamageControlChecker.setEnable(true);
		CameraMorph.setCamera(null);
		CameraMorph.setEnabledDraw(true);
		if (CameraHandler.get() != null && CameraHandler.get() != null)
			Dispatcher.sendToServer(new PacketScenePlay(CameraHandler.get(), PacketScenePlay.STOP, 0));
	}
	
	public static void onProfileApplyCurves(CameraProfile profile, long progress, float partialTick) {
		Map<String, KeyframeChannel> channels = profile.getCurves();
		for (ICurve curve : curves) {
			KeyframeChannel channel = channels.get(curve.getId());
			if (channel != null && !channel.isEmpty()) {
				curve.enable();
				curve.applyCurve(channel.interpolate(progress + partialTick));
			} else
				curve.disable();
		}
	}
	
	public static Map<String, String> getAddonCurves() {
		LinkedHashMap<String, String> idMap = new LinkedHashMap<>();
		curves.forEach(curve -> idMap.put(curve.getId(), curve.getName()));
		return idMap;
	}
	
	private static void updateCurves() {
		curves.clear();
		curves.addAll(vaillaOptions);
		if (ShaderManager.usingShaders()) {
			curves.add(new UniformFloatCurve("rainStrength", I18n.format("hackersmorph.gui.curve.shader.rainstrength")));
			curves.add(new UniformFloatCurve("wetness", I18n.format("hackersmorph.gui.curve.shader.wetness")));
			curves.add(new CenterDepthUniformCurve());
			curves.add(new SunPathRotationCurve());
			curves.add(new UniformIntegerCurve("isEyeInWater", I18n.format("hackersmorph.gui.curve.shader.iseyeinwater")));
			curves.add(new WorldTimeUniformCurve());
//			curves.add(new ShadowDistanceCurve());
//			curves.add(new ShadowSizeCurve());
			ArrayList<ICurve> unsort = new ArrayList<>();
			ShaderManager.getAllFloatOptionUniform((uniform) -> unsort.add(new UniformFloatCurve(uniform.getUniformName(), String.format("%s/%s", uniform.getNameText(), uniform.getName()))));
			unsort.sort((a, b) -> a.getName().compareTo(b.getName()));
			curves.addAll(unsort);
			unsort.clear();
			ShaderManager.getAllIntegerOptionUniform((uniform) -> curves.add(new UniformIntegerCurve(uniform.getUniformName(), String.format("%s/%s", uniform.getNameText(), uniform.getName()))));
			unsort.sort((a, b) -> a.getName().compareTo(b.getName()));
			curves.addAll(unsort);
			unsort.clear();
		}
	}
	
}
