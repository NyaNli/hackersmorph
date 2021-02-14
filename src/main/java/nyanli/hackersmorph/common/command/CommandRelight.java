package nyanli.hackersmorph.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class CommandRelight extends CommandBase {

	@Override
	public String getName() {
		return "relight";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "hackersmorph.command.relight";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		BlockPos pos = player.getPosition();
		relight(player.world, new BlockPos(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4), -1);
		notifyCommandListener(sender, this, "hackersmorph.command.relight.done");
	}
	
	private void relight(World world, BlockPos base, int dir) {
		if (dir >= EnumFacing.values().length)
			return;
		
		boolean skip = false;
		BlockPos chunkPos = dir >= 0 ? base.offset(EnumFacing.byIndex(dir)) : base;
		if (chunkPos.getY() > 15 || chunkPos.getY() < 0) {
			skip = true;
		}
		
		if (!skip) {
			Chunk chunk = world.getChunk(chunkPos.getX(), chunkPos.getZ());
			ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();

			// Clear Block Lighting
			ExtendedBlockStorage storage = storages[chunkPos.getY()];
			if (storage != Chunk.NULL_BLOCK_STORAGE) {
				byte[] light = storage.getBlockLight().getData();
				for (int i = 0; i < 2048; i++)
					light[i] = 0;
			}
			
			relight(world, base, dir + 1);

			// Recalc Block Lighting
			if (storage != Chunk.NULL_BLOCK_STORAGE) {
				BlockPos worldPos = new BlockPos(chunkPos.getX() << 4, chunkPos.getY() << 4, chunkPos.getZ() << 4);
				for (int x = 0; x < 16; x++)
					for (int y = 0; y < 16; y++)
						for (int z = 0; z < 16; z++)
							if (storage.get(x, y, z).getLightValue() > 0)
								world.checkLightFor(EnumSkyBlock.BLOCK, worldPos.add(x, y, z));
			}
			
			// Recalc Sky Lighting
			if (world.provider.hasSkyLight() && (dir > 1 || dir < 0)) {
				for (int i = 0; i < chunk.getBlockStorageArray().length; i++) {
					ExtendedBlockStorage sky = storages[i];
					if (sky == Chunk.NULL_BLOCK_STORAGE)
						continue;
					byte[] light = sky.getSkyLight().getData();
					for (int j = 0; j < 2048; j++)
						light[j] = 0;
				}
				chunk.generateSkylightMap();
				chunk.checkLight();
			}
			
			chunk.markDirty();
			
			// Send to players
			if (dir > 1 || dir < 0) {
				SPacketUnloadChunk unload = new SPacketUnloadChunk(chunk.x, chunk.z);
				SPacketChunkData load = new SPacketChunkData(chunk, 0xFFFF);
				for (EntityPlayer player : world.playerEntities) {
					((EntityPlayerMP) player).connection.sendPacket(unload);
					((EntityPlayerMP) player).connection.sendPacket(load);
				}
			}
		} else
			relight(world, base, dir + 1);
	}

}
