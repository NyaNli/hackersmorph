package nyanli.hackersmorph;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name(HackersMorphASM.COREMODID)
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class HackersMorphASM implements IFMLLoadingPlugin {
	
	public static final String COREMODID = "HackersMorphTheHacker";
	public static final Logger LOGGER = LogManager.getLogger(COREMODID);
	public static File mcDir;

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"nyanli.hackersmorph.other.minecraft.ASM",
				"nyanli.hackersmorph.other.mchorse.ASM",
				"nyanli.hackersmorph.other.mchorse.mclib.ASM",
				"nyanli.hackersmorph.other.mchorse.aperture.ASM",
				"nyanli.hackersmorph.other.mchorse.blockbuster.ASM",
				"nyanli.hackersmorph.other.mchorse.metamorph.ASM",
				"nyanli.hackersmorph.other.mchorse.chameleon.ASM",
				"nyanli.hackersmorph.other.mchorse.emoticons.ASM",
				"nyanli.hackersmorph.other.optifine.ASM",
			};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		mcDir = (File) data.get("mcLocation");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
