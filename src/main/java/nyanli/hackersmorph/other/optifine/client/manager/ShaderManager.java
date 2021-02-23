package nyanli.hackersmorph.other.optifine.client.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.uniform.ShaderUniform1f;
import net.optifine.shaders.uniform.ShaderUniform1i;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.other.optifine.client.shader.IOptionUniform;

@SideOnly(Side.CLIENT)
public class ShaderManager {
	
	private static boolean haveShader;
	
	private static float sunPathRotation;
//	private static float shadowIntervalSize;
	
	private static HashSet<String> enabled = new HashSet<>();
	
	private static HashMap<String, Integer> uniform1i = new HashMap<>();
//	private static HashMap<String, int[]> uniform2i = new HashMap<>();
	private static HashMap<String, Float> uniform1f = new HashMap<>();
//	private static HashMap<String, float[]> uniform3f = new HashMap<>();
//	private static HashMap<String, Double> shadow = new HashMap<>();
	
	private static HashMap<IOptionUniform, ShaderUniform1i> optionUniform1i = new HashMap<>();
	private static HashMap<IOptionUniform, ShaderUniform1f> optionUniform1f = new HashMap<>();
	
	private static HashSet<String> addonSources = new HashSet<>();
	
	private static boolean requestedReload = false;
	
	static {
		try {
			Class.forName("Config");
			Class.forName("net.optifine.shaders.Shaders");
			haveShader = true;
		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			haveShader = false;
		}
	}
	
	public static boolean usingShaders() {
		return haveShader ? Shaders.shaderPackLoaded : false;
	}
	
	public static boolean isShadowPass() {
		return haveShader ? Shaders.isShadowPass : false;
	}
	
	public static void reloadShaderpack() {
		if (usingShaders())
			Shaders.uninit();
	}
	
	public static void enableHook(String option) {
		if (option == "sunPathRotation")
			return;
		if (haveShader)
			enabled.add(option);
	}
	
	public static void disableHook(String option) {
		if (haveShader && option == "sunPathRotation") {
			Shaders.sunPathRotation = sunPathRotation;
			return;
		}
		enabled.remove(option);
		uniform1i.remove(option);
//		uniform2i.remove(option);
		uniform1f.remove(option);
//		uniform3f.remove(option);
//		shadow.remove(option);
//		if (!enabled.contains("shadowDistance") && !enabled.contains("shadowSize"))
//			Shaders.shadowIntervalSize = shadowIntervalSize;
	}
	
	public static void disableAll() {
		enabled.clear();
		uniform1i.clear();
//		uniform2i.clear();
		uniform1f.clear();
//		uniform3f.clear();
//		shadow.clear();
		if (haveShader)
			Shaders.sunPathRotation = sunPathRotation;
//		Shaders.shadowIntervalSize = shadowIntervalSize;
	}
	
	public static void setUniform1i(String name, int value) {
		if (!uniform1i.containsKey(name) || uniform1i.get(name) != value)
			uniform1i.put(name, value);
	}
	
//	public static void setUniform2i(String name, int v1, int v2) {
//		if (!uniform2i.containsKey(name))
//			uniform2i.put(name, new int[2]);
//		uniform2i.get(name)[0] = v1;
//		uniform2i.get(name)[1] = v2;
//	}
	
	public static void setUniform1f(String name, float value) {
		if (!uniform1f.containsKey(name) || uniform1f.get(name) != value)
			uniform1f.put(name, value);
	}
	
//	public static void setUniform3f(String name, float x, float y, float z) {
//		uniform3f.put(name, new float[]{x, y, z});
//		if (!uniform3f.containsKey(name))
//			uniform3f.put(name, new float[3]);
//		uniform3f.get(name)[0] = x;
//		uniform3f.get(name)[1] = y;
//		uniform3f.get(name)[2] = z;
//	}
	
	// Shaders.loadShaderPack() -> generate SHaderpack options
	// game begin render -> Shaders.init() -> inject option values to code (just modify the #define)
	// useProgram -> upload uniform values to glsl
	// Shaders.uninit() -> clear uniforms' location
	public static void addOptionUniform(IOptionUniform option) {
		if (haveShader) {
			IOptionUniform optionUniform = (IOptionUniform) option;
			if (optionUniform.isUniform()) {
				switch (optionUniform.getUniformType()) {
				case INT:
					if (!optionUniform1i.containsKey(option))
						optionUniform1i.put(option, new ShaderUniform1i(optionUniform.getUniformName()));
					break;
				case FLOAT:
					if (!optionUniform1f.containsKey(option))
						optionUniform1f.put(option, new ShaderUniform1f(optionUniform.getUniformName()));
					break;
				}
			}
			addonSources.add(String.format("uniform %s %s;\n", optionUniform.getUniformType(), optionUniform.getUniformName()));
		}
	}
	
	public static void getAllIntegerOptionUniform(Consumer<IOptionUniform> callback) {
		optionUniform1i.forEach((option, uniform) -> callback.accept(option));
	}
	
	public static void getAllFloatOptionUniform(Consumer<IOptionUniform> callback) {
		optionUniform1f.forEach((option, uniform) -> callback.accept(option));
	}
	
	public static void requestReload() {
		requestedReload = true;
	}
	
//	public static void setShadowDistance(double value) {
//		shadow.put("shadowDistance", value);
//	}
//	
//	public static void setShadowSize(double value) {
//		shadow.put("shadowSize", value);
//	}
	
// Hooker =============================================
	
	public static void hookProgramUniform1i(ShaderUniform1i su, int value) {
		if (enabled.contains(su.getName()) && uniform1i.get(su.getName()) != null)
			value = uniform1i.get(su.getName());
		setUniform1i(su.getName(), value);
		su.setValue(value);
	}

//	public static void hookProgramUniform2i(ShaderUniform2i su, int v1, int v2) {
//		if (enabled.contains(su.getName()) && uniform2i.get(su.getName()) != null) {
//			v1 = uniform2i.get(su.getName())[0];
//			v2 = uniform2i.get(su.getName())[1];
//		}
//		setUniform2i(su.getName(), v1, v2);
//		su.setValue(v1, v2);
//	}

	public static void hookProgramUniform1f(ShaderUniform1f su, float value) {
		if (enabled.contains(su.getName()) && uniform1f.get(su.getName()) != null)
			value = uniform1f.get(su.getName());
		setUniform1f(su.getName(), value);
		su.setValue(value);
	}

//	public static void hookProgramUniform3f(ShaderUniform3f su, float x, float y, float z) {
//		if (enabled.contains(su.getName()) && uniform3f.get(su.getName()) != null) {
//			x = uniform3f.get(su.getName())[0];
//			y = uniform3f.get(su.getName())[1];
//			z = uniform3f.get(su.getName())[2];
//		}
//		setUniform3f(su.getName(), x, y, z);
//		su.setValue(x, y, z);
//	}
	
	@SuppressWarnings("unchecked")
	public static void updateOptionUniforms() {
		Iterator<?> iterator = optionUniform1i.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<IOptionUniform, ShaderUniform1i> entry = (Entry<IOptionUniform, ShaderUniform1i>) iterator.next();
			IOptionUniform option = entry.getKey();
			ShaderUniform1i uniform = entry.getValue();
			uniform.setProgram(Shaders.activeProgramID);
			hookProgramUniform1i(uniform, Integer.parseInt(option.getValue()));
		}
		iterator = optionUniform1f.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<IOptionUniform, ShaderUniform1f> entry = (Entry<IOptionUniform, ShaderUniform1f>) iterator.next();
			IOptionUniform option = entry.getKey();
			ShaderUniform1f uniform = entry.getValue();
			uniform.setProgram(Shaders.activeProgramID);
			hookProgramUniform1f(uniform, Float.parseFloat(option.getValue()));
		}
	}
	
	public static void resetOptionUniforms() {
//		optionUniform1i.forEach((option, uniform) -> {
//			uniform.reset();
//		});
//		optionUniform1f.forEach((option, uniform) -> {
//			uniform.reset();
//		});
		optionUniform1i.clear();
		optionUniform1f.clear();
	}
	
//	public static void clearOptionUniforms() {
//		resetOptionUniforms();
//		optionUniform1i.clear();
//		optionUniform1f.clear();
//	}
	
	public static void postProcessShader(StringBuilder builder) {
		int version = builder.indexOf("#version");
		int pos = builder.indexOf("#line", version);
		StringBuilder uniforms = new StringBuilder();
		addonSources.forEach(str -> uniforms.append(str));
		builder.insert(pos, uniforms);
		addonSources.clear();
	}
	
	public static void afterInit() {
		if (requestedReload) {
			HackersMorph.LOGGER.info("Requested reload shaders.");
			requestedReload = false;
			Shaders.uninit();
			Shaders.init();
		}
		sunPathRotation = Shaders.sunPathRotation;
//		shadowIntervalSize = Shaders.shadowIntervalSize;
	}
	
//	public static void setShadowCameraOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
//		double size = right;
//		double distance = zNear;
//		if (enabled.contains("shadowSize") && shadow.get("shadowSize") != null)
//			size = shadow.get("shadowSize");
//		setShadowSize(size);
//		if (enabled.contains("shadowDistance") && shadow.get("shadowDistance") != null)
//			distance = shadow.get("shadowDistance");
//		setShadowDistance(distance);
//		GL11.glOrtho(-size, size, -size, size, Math.min(distance, zFar), zFar);
//	}
//	
//	public static void shadowDisableCull() {
//		if (!enabled.contains("shadowDistance") && !enabled.contains("shadowSize"))
//			GlStateManager.disableCull();
//		else
//			GlStateManager.enableCull();
//	}
	
}
