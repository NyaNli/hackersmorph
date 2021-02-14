package nyanli.hackersmorph.side;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.utils.Color;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import nyanli.hackersmorph.other.mchorse.aperture.client.camera.fixture.CameraMorphFixture;
import nyanli.hackersmorph.other.mchorse.aperture.client.gui.GuiCameraMorphFixturePanel;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.handler.DamageControlChecker;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(new DamageControlChecker());
		if (Loader.isModLoaded(Aperture.MOD_ID)) {
	        GuiCameraEditor.PANELS.put(CameraMorphFixture.class, GuiCameraMorphFixturePanel.class);
	        FixtureRegistry.registerClient(CameraMorphFixture.class, "hackersmorph.gui.fixture.cameramorph", new Color(0.38f, 0.75f, 1f));
		}
	}

}
