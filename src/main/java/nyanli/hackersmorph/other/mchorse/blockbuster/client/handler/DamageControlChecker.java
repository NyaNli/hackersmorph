package nyanli.hackersmorph.other.mchorse.blockbuster.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import nyanli.hackersmorph.network.Dispatcher;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.network.PacketDamageControlCheck;

// 破坏控制显示
public class DamageControlChecker {
	
	private static boolean enabled = true;
	
	private BlockPos last = null;
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (enabled && event.phase == Phase.END) {
			if (Minecraft.getMinecraft().objectMouseOver == null) {
				last = null;
				return;
			}
			BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			if (last != pos && pos != null && !pos.equals(last)) {
				Dispatcher.server(new PacketDamageControlCheck(pos));
			}
			last = pos;
		}
	}
	
	public static void setEnable(boolean bool) {
		enabled = bool;
	}

}
