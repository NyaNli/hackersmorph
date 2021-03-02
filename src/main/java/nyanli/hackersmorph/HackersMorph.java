package nyanli.hackersmorph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import nyanli.hackersmorph.common.command.CommandRelight;
import nyanli.hackersmorph.config.Config;
import nyanli.hackersmorph.side.CommonProxy;

//@Mod(modid = HackersMorph.MODID, name = "Hacker's Morph", version = "0.0.0.1", dependencies = "required-after:mclib@[2.0.3,);required-after:blockbuster@[2.0,);after:aperture@[1.4.2,);after:emoticons", guiFactory="nyanli.hackersmorph.config.ConfigFactory")
@Mod(modid = HackersMorph.MODID, name = "Hacker's Morph", version = "0.0.0.1", dependencies = "required-after:blockbuster@[2.1,2.2.2];after:aperture@[1.5,1.6];after:emoticons", guiFactory="nyanli.hackersmorph.config.ConfigFactory")
public class HackersMorph {
	
	public static final String MODID = "hackersmorph";
	
    @SidedProxy(clientSide = "nyanli.hackersmorph.side.ClientProxy", serverSide = "nyanli.hackersmorph.side.CommonProxy")
    public static CommonProxy thisSide;
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    private static Config config;

    public static Config getConfig() {
    	return config;
    }
    
    public static void throwCommonException(Throwable e) {
		Throwable ex = e;
		while(ex.getCause() != null && ex.getCause() != ex) ex = ex.getCause();
		HackersMorphASM.LOGGER.debug(ex);
		throw new RuntimeException(I18n.translateToLocal("hackersmorph.core.error"), e);
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
    	config = new Config(event.getSuggestedConfigurationFile());
        thisSide.preInit();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
    	thisSide.init();
    }
    
    @EventHandler
    public void onServer(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandRelight());
    }
    
}
