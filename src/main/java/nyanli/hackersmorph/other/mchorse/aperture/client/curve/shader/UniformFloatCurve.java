package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public class UniformFloatCurve extends AbstractShaderCurve {

	public UniformFloatCurve(String id, String name) {
		super(id, name);
	}

	@Override
	public void applyCurve(double value) {
		ShaderManager.setUniform1f(this.id, (float) value);
	}

}
