package nyanli.hackersmorph.network;

import mchorse.mclib.network.AbstractDispatcher;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.network.PacketDamageControlCheck;

public class Dispatcher extends AbstractDispatcher {

	private static final Dispatcher instance = new Dispatcher();
	
	private Dispatcher() {
		super(HackersMorph.MODID);
	}

	@Override
	public void register() {
		register(PacketDamageControlCheck.class, PacketDamageControlCheck.ServerHandler.class, Side.SERVER);
	}

    public static void client(IMessage message, EntityPlayerMP player) {
    	instance.sendTo(message, player);
    }

    public static void server(IMessage message) {
    	instance.sendToServer(message);
    }

    public static void init() {
    	instance.register();
    }

}
