package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public class UniformIntegerCurve extends AbstractShaderCurve {

	public UniformIntegerCurve(String id, String name) {
		super(id, name + I18n.format("hackersmorph.gui.curve.integer"));
	}

	@Override
	public void applyCurve(double value) {
		ShaderManager.setUniform1i(this.id, (int) Math.floor(value));
	}

}
