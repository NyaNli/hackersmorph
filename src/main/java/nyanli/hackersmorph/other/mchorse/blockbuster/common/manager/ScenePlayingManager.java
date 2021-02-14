package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.util.LinkedHashSet;
import java.util.WeakHashMap;

import mchorse.blockbuster.CommonProxy;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.PacketCaption;
import mchorse.blockbuster.recording.scene.SceneManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

// 在播放回放的玩家屏幕左上角显示提示
// 使用了BlockBuster录制回放时用到的GUI
public class ScenePlayingManager {
	
	private static WeakHashMap<EntityPlayer, LinkedHashSet<String>> playingSceneList = new WeakHashMap<>();

	public static boolean toggle(SceneManager mgr, String scene, World world, EntityPlayer player) {
		boolean playing = mgr.toggle(scene, world);
		if (!world.isRemote) {
			if (playing) {
				if (!playingSceneList.containsKey(player))
					playingSceneList.put(player, new LinkedHashSet<>());
				playingSceneList.get(player).add(scene);
			} else if (playingSceneList.containsKey(player)) {
				playingSceneList.get(player).remove(scene);
			}
			caption(player);
		}
		return playing;
	}
	
	public static void stopPlayback(String scene) {
		playingSceneList.forEach((player, set) -> {
			set.remove(scene);
			caption(player);
		});
	}
	
	private static void caption(EntityPlayer player) {
		LinkedHashSet<String> list = playingSceneList.get(player);
		if (list != null && !list.isEmpty())
			Dispatcher.sendTo(new PacketCaption(new TextComponentTranslation("hackersmorph.gui.caption.scene_playing", String.join(", ", list))), (EntityPlayerMP) player);
		else {
			if (player instanceof EntityPlayerMP && !CommonProxy.manager.recorders.containsKey(player))
				Dispatcher.sendTo(new PacketCaption(), (EntityPlayerMP) player);
		}
	}
	
}
