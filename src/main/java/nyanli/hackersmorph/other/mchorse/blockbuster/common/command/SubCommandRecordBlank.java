package nyanli.hackersmorph.other.mchorse.blockbuster.common.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mchorse.blockbuster.CommonProxy;
import mchorse.blockbuster.recording.RecordUtils;
import mchorse.blockbuster.recording.data.Frame;
import mchorse.blockbuster.recording.data.Record;
import mchorse.blockbuster.recording.scene.Replay;
import mchorse.blockbuster.recording.scene.Scene;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class SubCommandRecordBlank extends CommandBase {

	@Override
	public String getName() {
		return "blank";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "hackersmorph.command.record.blank";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			EntityPlayerMP player = getCommandSenderAsPlayer(sender);
			String filename = args[0];
			int frames = Integer.parseInt(args[1]);
			String mode = args.length > 2 ? args[2] : "";
			try {
				CommonProxy.manager.get(filename);
			} catch (Exception e) {
				CommonProxy.manager.records.put(filename, new Record(filename));
			}
			Record record = CommonProxy.manager.records.get(filename);
			for (int i = record.actions.size() - 1; i >= 0; i--) {
				if (record.actions.get(i) != null && !record.actions.get(i).isEmpty()) {
					frames = Math.max(frames, i + 1);
					break;
				}
			}
			while (record.actions.size() > frames) record.actions.remove(record.actions.size() - 1);
			while (record.actions.size() < frames) record.actions.add(null);
			Frame frame = new Frame();
			frame.fromPlayer(player);
			frame.motionX = frame.motionY = frame.motionZ;
			switch(mode) {
			case "3":break;
			case "2":
				frame.hasBodyYaw = true;
				frame.bodyYaw = frame.yaw;
				if (frame.isMounted)
					frame.mountYaw = frame.yaw;
				break;
			case "1":
				frame.hasBodyYaw = true;
				frame.bodyYaw = frame.yaw;
				frame.pitch = 0;
				if (frame.isMounted)
					frame.mountPitch = 0;
				break;
			default:
				frame.yaw = frame.yawHead = frame.bodyYaw = frame.mountYaw = frame.pitch = frame.mountPitch = 0;
				frame.hasBodyYaw = false;
				break;
			}
			record.frames.clear();
			int i = frames;
			while (i-- > 0)
				record.frames.add(frame);
			record.save(RecordUtils.replayFile(record.filename));
			RecordUtils.unloadRecord(record);
			notifyCommandListener(sender, this, "hackersmorph.command.record.blank.success", filename, frames);
		} catch (Exception ex) {
			throw new WrongUsageException(this.getUsage(sender));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length <= 1) {
			ArrayList<String> replays = new ArrayList<>();
			for (String sceneFile : CommonProxy.scenes.sceneFiles()) {
				try {
					Scene scene = CommonProxy.scenes.load(sceneFile); // Disk killer
					if (scene != null)
						for (Replay replay : scene.replays) {
							replays.add(replay.id);
						}
				} catch (IOException e) {}
			}
			return getListOfStringsMatchingLastWord(args, replays);
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}

}
