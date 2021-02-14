package nyanli.hackersmorph.other.mchorse.blockbuster.common.network;

import java.util.Map;
import java.util.WeakHashMap;

import io.netty.buffer.ByteBuf;
import mchorse.blockbuster.CommonProxy;
import mchorse.blockbuster.recording.RecordRecorder;
import mchorse.blockbuster.recording.capturing.DamageControl;
import mchorse.blockbuster.recording.capturing.DamageControl.BlockEntry;
import mchorse.blockbuster.recording.scene.Scene;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// 破坏控制显示
public class PacketDamageControlCheck implements IMessage {
	
	public BlockPos pointPos;
	
	public PacketDamageControlCheck() {
		pointPos = null;
	}
	
	public PacketDamageControlCheck(BlockPos pointPos) {
		this.pointPos = pointPos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		boolean havePointPos = buf.readBoolean();
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		if (havePointPos)
			pointPos = new BlockPos(x, y, z);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(pointPos != null);
		buf.writeInt(pointPos != null ? pointPos.getX() : 0);
		buf.writeInt(pointPos != null ? pointPos.getY() : 0);
		buf.writeInt(pointPos != null ? pointPos.getZ() : 0);
	}
	
	public static class ServerHandler extends ServerMessageHandler<PacketDamageControlCheck> {
		
		private static WeakHashMap<EntityPlayerMP, Boolean> haveMsg = new WeakHashMap<>();

		@Override
		public void run(EntityPlayerMP player, PacketDamageControlCheck packet) {
			if (packet.pointPos != null && !player.world.isAirBlock(packet.pointPos)) {
				Object target = null;
				for (Map.Entry<Object, DamageControl> entry : CommonProxy.damage.damage.entrySet()) {
					DamageControl control = entry.getValue();
					for (BlockEntry block : control.blocks) {
						if (block.pos.equals(packet.pointPos)) {
							target = entry.getKey();
							break;
						}
					}
					if (target != null)
						break;
					double x = Math.abs(control.target.posX - (double)packet.pointPos.getX());
					double y = Math.abs(control.target.posY - (double)packet.pointPos.getY());
					double z = Math.abs(control.target.posZ - (double)packet.pointPos.getZ());
					if (x <= control.maxDistance && y <= control.maxDistance && z <= control.maxDistance) {
						target = entry.getKey();
						break;
					}
				}
				if (target != null) {
					if (target instanceof Scene) {
						player.sendStatusMessage(new TextComponentTranslation("hackersmorph.msg.actionbar.damagecontrol.scene", ((Scene) target).getId()), true);
					} else if (target instanceof RecordRecorder) {
						EntityPlayer actor = (EntityPlayer) CommonProxy.damage.damage.get(target).target;
						if (!actor.getUniqueID().equals(player.getUniqueID())) {
							player.sendStatusMessage(new TextComponentTranslation("hackersmorph.msg.actionbar.damagecontrol.recorder", ((Scene) target).getId()), true);
						}
					}
					haveMsg.put(player, true);
					return;
				}
			}
			if (Boolean.TRUE.equals(haveMsg.get(player))) {
				haveMsg.put(player, false);
				player.sendStatusMessage(new TextComponentString(""), true);
			}
		}
		
	}

}
