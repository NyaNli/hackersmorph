package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.ClientProxy;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.network.common.PacketActorPause;
import mchorse.blockbuster.recording.RecordPlayer;
import mchorse.blockbuster.recording.actions.Action;
import mchorse.blockbuster.recording.actions.MorphAction;
import mchorse.blockbuster.recording.data.Frame;
import mchorse.blockbuster.recording.data.Record;
import mchorse.blockbuster.recording.data.Record.FoundAction;
import mchorse.blockbuster.recording.data.Record.MorphType;
import mchorse.blockbuster.recording.scene.Replay;
import mchorse.blockbuster.recording.scene.SceneLocation;
import mchorse.blockbuster.utils.EntityUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Timer;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.util.MorphAnimUtils;

public class RecordExtraManager {
	
	private static final Field fieldTimer;
	
	static {
		Field timer = null;
		try {
			timer = Minecraft.class.getDeclaredField("field_71428_T");
			timer.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				timer = Minecraft.class.getDeclaredField("timer");
				timer.setAccessible(true);
			} catch (NoSuchFieldException | SecurityException e1) {
				HackersMorph.throwCommonException(e1);
			}
		}
		fieldTimer = timer;
	}
	
	public static FoundAction seekMorphAction(Record record, int tick, boolean prev) {
		Action current = null;
		boolean retit = true;
		if (prev) {
			tick++;
			List<Action> actions = record.actions.get(tick);
			if (actions != null)
				for (int i = actions.size() - 1; i >= 0; i--) {
					Action action = actions.get(i);
					if (action instanceof MorphAction) {
						current = action;
						retit = false;
						break; // It should always break
					}
				}
		}
        while (tick >= 0) {
            List<Action> actions = record.actions.get(tick);
            if (actions != null)
            	for (int i = actions.size() - 1; i >= 0; i--) {
					Action action = actions.get(i);
					if (action instanceof MorphAction) {
						if (retit) {
							Record.ACTION.set(tick, (MorphAction) action);
							return Record.ACTION;
						} else if (action == current)
							retit = true;
					}
            	}
            tick--;
        }

        return null;
	}
	
	public static void playerResume(Record record, EntityLivingBase actor, Replay replay, int tick, MorphType type) {
		if (type == MorphType.FORCE && actor instanceof EntityActor) {
			EntityActor act = (EntityActor) actor;
			boolean manual = act.manual;
			if (tick >= 0) {
				act.manual = true;
				record.applyPreviousMorph(actor, replay, tick, MorphType.PAUSE);
			}
			act.forceMorph = true;
//			MorphAnimUtils.unpauseAll(act.morph.get());
			act.manual = manual;
			if (actor.isServerWorld())
				act.notifyPlayers();
			act.pauseOffset = -1;
			act.pausePreviousMorph = null;
			act.pausePreviousOffset = -1;
		} else
			record.applyPreviousMorph(actor, replay, tick, type);
	}
	
	public static void actorApplyModifyPacketPause(EntityActor actor, AbstractMorph morph, int offset, AbstractMorph previous, int previousOffset) {
		boolean force = actor.forceMorph;
		actor.applyPause(morph, offset, previous, previousOffset);
		if (force) {
			MorphAnimUtils.unpauseAll(actor.morph.get());
			MorphAnimUtils.updateTick(actor.morph.get(), -Math.min(10, getTimer().elapsedTicks));
			actor.pauseOffset = -1;
			actor.pausePreviousMorph = null;
			actor.pausePreviousOffset = -1;
			actor.forceMorph = true;
		}
	}
	
	public static void actorApplyModifyPacketModify(EntityActor actor, AbstractMorph morph, boolean invisible, boolean notify) {
		actor.modify(morph, invisible, notify);
		MorphAnimUtils.updateTick(actor.morph.get(), -Math.min(10, getTimer().elapsedTicks));
	}
	
	public static void sceneGoTo(RecordPlayer player, int tick, boolean actions) {
		player.goTo(tick, actions);
		if (actions && player.playing && player.actor instanceof EntityActor) {
			EntityActor actor = (EntityActor) player.actor;
			if (actor.isServerWorld())
				player.resume(tick);
			else
				MorphAnimUtils.unpauseAll(actor.morph.get());
		}
	}
	
//	public static void clientSyncTick(EntityPlayerSP player, PacketSyncTick message) {
//		if (HackersMorph.getConfig().canForceSync()) {
//			final EntityLivingBase actor = (EntityLivingBase) player.world.getEntityByID(message.id);
//			final RecordPlayer playback = EntityUtils.getRecordPlayer(actor);
//			if (playback != null && playback.record != null && Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor) {
//				GuiCameraEditor editor = (GuiCameraEditor) Minecraft.getMinecraft().currentScreen;
//				if (editor.getRunner().isRunning() && editor.minema.isRecording()) {
//					message.tick = Math.max(0, message.tick + HackersMorph.getConfig().getSyncOffset());
//					if (editor.getRunner().ticks != message.tick)
//						editor.getRunner().ticks = message.tick - 1; // then it will call tick++
//					if (playback.tick != message.tick && actor instanceof EntityActor) {
//						EntityActor act = (EntityActor) actor;
//						playback.goTo(message.tick, true);
//						MorphAnimUtils.unpauseAll(act.morph.get());
//						MorphAnimUtils.updateTick(act.morph.get(), -1);
//					}
//				}
//			}
//		}
//	}
	
	public static void cameraOnRewind(SceneLocation location) {
		if (location == null)
			return;
		ArrayList<String> replays = new ArrayList<>();
		for (Replay replay : ClientProxy.panels.directorPanel.getReplays())
			replays.add(replay.id);
		for (EntityActor actor : Minecraft.getMinecraft().world.getEntities(EntityActor.class, actor -> actor.playback != null && actor.playback.record != null && replays.contains(actor.playback.record.filename)))
			actor.setDead();
	}
	
	public static void playerGoTo(Record record, int tick, EntityLivingBase actor, boolean force, RecordPlayer player) {
		if ((player.playing || Blockbuster.recordPausePreview.get()) && tick > 0)
			applyPrevFrame(actor, record, tick, force);
		else
			record.applyFrame(tick, actor, force);
	}
	
	public static void playerGoTo(Record record, int tick, EntityLivingBase actor, boolean force, boolean realPlayer, RecordPlayer player) {
		if ((player.playing || Blockbuster.recordPausePreview.get()) && tick > 0)
			applyPrevFrame(actor, record, tick, force, realPlayer);
		else
			record.applyFrame(tick, actor, force, realPlayer);
	}
	
	public static boolean actorReadSpawnData(Map map, String record) {
		return map.containsKey(record) && ((Record)map.get(record)).frames != null && !((Record)map.get(record)).frames.isEmpty();
	}
	
	public static void clientActorPause(EntityPlayerSP player, PacketActorPause message) {
		if (message.pause && !Blockbuster.recordPausePreview.get())
			return;
		EntityLivingBase actor = (EntityLivingBase) player.world.getEntityByID(message.id);
		RecordPlayer playback = EntityUtils.getRecordPlayer(actor);
		if (playback != null && playback.record != null) {
			int tick = message.tick - playback.record.preDelay;
			if (tick < 0)
				tick = 0;
			else if (tick >= playback.record.frames.size())
				tick = playback.record.frames.size() - 1;
			try {
				playback.getClass().getField("realPlayer");
				applyPrevFrame(actor, playback.record, tick, true, playback.realPlayer);
			} catch (NoSuchFieldException | SecurityException e) {
				applyPrevFrame(actor, playback.record, tick, true);
			}
		}
	}
	
	private static void applyPrevFrame(EntityLivingBase actor, Record record, int tick, boolean force) {
		if (tick < 0 || tick >= record.frames.size())
			return;
		int prev = Math.max(tick - 1, 0);
		record.applyFrame(prev, actor, force);
		Frame frame = record.frames.get(prev);
		if (actor.world.isRemote && frame.hasBodyYaw)
			actor.prevRenderYawOffset = actor.renderYawOffset = frame.bodyYaw;
		actor.lastTickPosX = actor.prevPosX = actor.posX;
		actor.lastTickPosY = actor.prevPosY = actor.posY;
		actor.lastTickPosZ = actor.prevPosZ = actor.posZ;
		actor.prevRotationPitch = actor.rotationPitch;
		actor.prevRotationYaw = actor.rotationYaw;
		actor.prevRotationYawHead = actor.rotationYawHead;
//		if (pause)
//			return;
//		record.applyFrame(tick, actor, force, realPlayer);
//		frame = record.frames.get(tick);
//		if (actor.world.isRemote && frame.hasBodyYaw)
//			actor.renderYawOffset = frame.bodyYaw;
	}
	
	private static void applyPrevFrame(EntityLivingBase actor, Record record, int tick, boolean force, boolean realPlayer) {
		if (tick < 0 || tick >= record.frames.size())
			return;
		int prev = Math.max(tick - 1, 0);
		record.applyFrame(prev, actor, force, realPlayer);
		Frame frame = record.frames.get(prev);
		if (actor.world.isRemote && frame.hasBodyYaw)
			actor.prevRenderYawOffset = actor.renderYawOffset = frame.bodyYaw;
		actor.lastTickPosX = actor.prevPosX = actor.posX;
		actor.lastTickPosY = actor.prevPosY = actor.posY;
		actor.lastTickPosZ = actor.prevPosZ = actor.posZ;
		actor.prevRotationPitch = actor.rotationPitch;
		actor.prevRotationYaw = actor.rotationYaw;
		actor.prevRotationYawHead = actor.rotationYawHead;
//		if (pause)
//			return;
//		record.applyFrame(tick, actor, force, realPlayer);
//		frame = record.frames.get(tick);
//		if (actor.world.isRemote && frame.hasBodyYaw)
//			actor.renderYawOffset = frame.bodyYaw;
	}
	
	private static Timer getTimer() {
		try {
			return (Timer) fieldTimer.get(Minecraft.getMinecraft());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			HackersMorph.throwCommonException(e);
			return null;
		}
	}

}
