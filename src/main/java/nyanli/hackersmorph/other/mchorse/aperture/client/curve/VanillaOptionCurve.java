package nyanli.hackersmorph.other.mchorse.aperture.client.curve;

import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.minecraft.common.manager.RenderManager;

public class VanillaOptionCurve implements ICurve {
	
	private final RenderManager.EnumOption option;
	
	public VanillaOptionCurve(RenderManager.EnumOption option) {
		this.option = option;
	}

	@Override
	public String getId() {
		return "vanilla:" + option.toString();
	}

	@Override
	public String getName() {
		return I18n.format("hackersmorph.gui.curve.vanilla." + option.toString().toLowerCase());
	}

	@Override
	public void applyCurve(double value) {
		RenderManager.setOption(option, value);
	}

	@Override
	public void enable() {
		RenderManager.enableHook(option);
	}

	@Override
	public void disable() {
		RenderManager.disableHook(option);
	}

}
