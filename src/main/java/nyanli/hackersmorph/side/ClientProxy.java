package nyanli.hackersmorph.side;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.blockbuster.utils.mclib.BBIcons;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.events.RegisterDashboardPanels;
import mchorse.mclib.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nyanli.hackersmorph.other.mchorse.aperture.client.camera.fixture.CameraMorphFixture;
import nyanli.hackersmorph.other.mchorse.aperture.client.gui.GuiCameraMorphFixturePanel;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.handler.DamageControlChecker;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.dashboard.GuiMidKeyframeGenerator;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(new DamageControlChecker());
		if (Loader.isModLoaded(Aperture.MOD_ID)) {
	        GuiCameraEditor.PANELS.put(CameraMorphFixture.class, GuiCameraMorphFixturePanel.class);
	        FixtureRegistry.registerClient(CameraMorphFixture.class, "hackersmorph.gui.fixture.cameramorph", new Color(0.38f, 0.75f, 1f));
	        McLib.EVENT_BUS.register(this);
		}
	}
	
    @SubscribeEvent
    public void onRegister(RegisterDashboardPanels event)
    {
    	Minecraft mc = Minecraft.getMinecraft();
    	event.dashboard.panels.registerPanel(new GuiMidKeyframeGenerator(mc, event.dashboard), IKey.lang("hackersmorph.gui.dashboard.midkeyframegenerator"), Icons.CUT);
    }

}
