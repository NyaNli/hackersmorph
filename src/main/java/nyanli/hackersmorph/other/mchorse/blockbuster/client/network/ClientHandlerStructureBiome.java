package nyanli.hackersmorph.other.mchorse.blockbuster.client.network;

import mchorse.blockbuster.network.common.structure.PacketStructure;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager;

// 修改客户端接收结构伪装更新逻辑
public class ClientHandlerStructureBiome extends ClientMessageHandler<PacketStructure> {

	@Override
	public void run(EntityPlayerSP player, PacketStructure packet) {
		StructureMorphExtraManager.updateRenderer(packet.name, packet.tag);
	}

}
