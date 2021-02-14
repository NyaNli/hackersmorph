package nyanli.hackersmorph.config;

import java.util.Set;

import mchorse.aperture.Aperture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.common.Loader;
import nyanli.hackersmorph.HackersMorph;

public class ConfigFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft mc) {
	}

	@Override
	public boolean hasConfigGui() {
		return Loader.isModLoaded(Aperture.MOD_ID);
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parent) {
		
		return new GuiConfig(parent, 
				HackersMorph.getConfig().getCategories(), 
				HackersMorph.MODID, false, false, "Hacker's Morph");
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

}
