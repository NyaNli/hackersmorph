package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import net.minecraft.client.resources.I18n;

public class WorldTimeUniformCurve extends UniformIntegerCurve {

	public WorldTimeUniformCurve() {
		super("worldTime", I18n.format("hackersmorph.gui.curve.shader.worldtime"));
	}

	@Override
	public void applyCurve(double value) {
		while (value < 0) value += 24.0;
		super.applyCurve((value % 24.0) * 1000.0);
	}

}
