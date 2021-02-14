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
//	private Property forceSync;
//	private Property forceSyncOffset;
	
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
	
//	public boolean canForceSync() {
//		return this.forceSync.getBoolean();
//	}
//	
//	public int getSyncOffset() {
//		return this.forceSyncOffset.getInt();
//	}
	
	private void init() {
		ConfigCategory category = forgeConfig.getCategory("experimental");
		category.setLanguageKey("hackersmorph.config.experimental");
		convertDefine = forgeConfig.get("experimental", "convertdef", false);
		convertDefine.setLanguageKey("hackersmorph.config.experimental.convertdef");
		convertDefine.setComment(I18n.translateToLocal("hackersmorph.config.experimental.convertdef.comment"));
//		forceSync = forgeConfig.get("experimental", "forcesync", false);
//		forceSync.setLanguageKey("hackersmorph.config.experimental.forcesync");
//		forceSync.setComment(I18n.translateToLocal("hackersmorph.config.experimental.forcesync.comment"));
//		forceSyncOffset = forgeConfig.get("experimental", "forcesyncoffset", -1);
//		forceSyncOffset.setLanguageKey("hackersmorph.config.experimental.forcesyncoffset");
//		forceSyncOffset.setComment(I18n.translateToLocal("hackersmorph.config.experimental.forcesyncoffset.comment"));
	}
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event) {
    	if (HackersMorph.MODID.equals(event.getModID())) {
    		forgeConfig.save();
    		ShaderManager.reloadShaderpack();
    	}
    }
	
}
