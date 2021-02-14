package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.optifine.router.shaders.Shaders;

public class SunPathRotationCurve extends AbstractShaderCurve {

	public SunPathRotationCurve() {
		super("sunPathRotation", I18n.format("hackersmorph.gui.curve.shader.sunpathrotation"));
	}

	@Override
	public void applyCurve(double value) {
		Shaders.sunPathRotation = (float) value;
	}

}
