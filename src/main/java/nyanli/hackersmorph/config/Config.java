package nyanli.hackersmorph.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

public class Config {
	
	private Configuration forgeConfig;
	private Property convertDefine;
	private Property cameraMode;
	
	public Config(File configFile) {
		forgeConfig = new Configuration(configFile);
		init();
	}
	
	public List<IConfigElement> getCategories() {
		return Arrays.asList(new ConfigElement(forgeConfig.getCategory("experimental")));
	}
	
	public boolean canConvertDefine() {
		return this.convertDefine.getBoolean();
	}
	
	public Property getConfig(String category, String key, String defaultValue) {
		return forgeConfig.get(category, key, defaultValue);
	}
	
	public Property getConfig(String category, String key, boolean defaultValue) {
		return forgeConfig.get(category, key, defaultValue);
	}
	
	public Property getConfig(String category, String key, int defaultValue) {
		return forgeConfig.get(category, key, defaultValue);
	}
	
	public void save() {
		forgeConfig.save();
	}
	
	private void init() {
		ConfigCategory category = forgeConfig.getCategory("experimental");
		category.setLanguageKey("hackersmorph.config.experimental");
		convertDefine = forgeConfig.get("experimental", "convertdef", false);
		convertDefine.setLanguageKey("hackersmorph.config.experimental.convertdef");
		convertDefine.setComment(I18n.translateToLocal("hackersmorph.config.experimental.convertdef.comment"));
	}
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event) {
    	if (HackersMorph.MODID.equals(event.getModID())) {
    		forgeConfig.save();
    		ShaderManager.reloadShaderpack();
    	}
    }
	
}
