package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public class CenterDepthUniformCurve extends AbstractShaderCurve {

	public CenterDepthUniformCurve() {
		super("centerDepthSmooth", I18n.format("hackersmorph.gui.curve.shader.centerdepth"));
	}

	@Override
	public void applyCurve(double value) {
		double near = 0.05f;
		double far = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16.0;
		double depth = ((far + near) * value - 2 * near * far) / (value * (far - near)) * 0.5 + 0.5;
		ShaderManager.setUniform1f("centerDepthSmooth", (float) depth);
	}

}
