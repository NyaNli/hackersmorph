package nyanli.hackersmorph.other.mchorse.aperture.client.curve.shader;

import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.mchorse.aperture.client.curve.ICurve;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public abstract class AbstractShaderCurve implements ICurve {
	
	protected final String id;
	protected final String name;
	
	public AbstractShaderCurve(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return "shader:" + id;
	}

	@Override
	public String getName() {
		return I18n.format("hackersmorph.gui.curve.shader", name);
	}

	@Override
	public abstract void applyCurve(double value);

	@Override
	public void enable() {
		ShaderManager.enableHook(id);
	}

	@Override
	public void disable() {
		ShaderManager.disableHook(id);
	}

}
