package nyanli.hackersmorph.side;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.metamorph.api.MorphManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.network.Dispatcher;
import nyanli.hackersmorph.other.mchorse.aperture.client.camera.fixture.CameraMorphFixture;
import nyanli.hackersmorph.other.mchorse.metamorph.common.HackersMorphFactory;

public class CommonProxy {
	
	private HackersMorphFactory factory;
	
	public void preInit() {
		Dispatcher.init();
		factory = new HackersMorphFactory();
		if (Loader.isModLoaded(Aperture.MOD_ID)) {
			MorphManager.INSTANCE.factories.add(factory);

	        FixtureRegistry.register("cameramorph", CameraMorphFixture.class);
		}
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(HackersMorph.getConfig());
	}

}
