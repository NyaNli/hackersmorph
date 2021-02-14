package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.vecmath.Color4f;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.ClientProxy;
import mchorse.blockbuster.aperture.CameraHandler;
import mchorse.blockbuster.client.gui.dashboard.panels.recording_editor.GuiRecordingEditorPanel;
import mchorse.blockbuster.client.gui.dashboard.panels.recording_editor.actions.GuiMorphActionPanel;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.recording.PacketFramesLoad;
import mchorse.blockbuster.network.common.recording.actions.PacketRequestAction;
import mchorse.blockbuster.recording.actions.Action;
import mchorse.blockbuster.recording.actions.MorphAction;
import mchorse.blockbuster.recording.data.Frame;
import mchorse.blockbuster.recording.data.Record;
import mchorse.blockbuster.recording.data.Record.FoundAction;
import mchorse.blockbuster.recording.scene.Replay;
import mchorse.blockbuster_pack.client.gui.GuiSequencerMorph;
import mchorse.blockbuster_pack.client.gui.GuiSequencerMorph.GuiSequencerMorphPanel;
import mchorse.blockbuster_pack.morphs.SequencerMorph;
import mchorse.blockbuster_pack.morphs.SequencerMorph.SequenceEntry;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.client.gui.creative.GuiCreativeMorphsMenu;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager.OnionSkin;

public class GuiMorphActionExtraPanel extends GuiMorphActionPanel {

	private static final FoundAction ACTION = new FoundAction();
	private static final EntityActor actor;
	
	static {
		actor = new EntityActor(Minecraft.getMinecraft().world);
		actor.manual = true;
	}
	
	private static void applyRecord(Record record, int tick, Replay replay, boolean doAction) {
		actor.morph.setDirect(null);
		if (tick >= record.frames.size())
			return;
		record.applyFrame(Math.max(0, tick - 1), actor, true); // Not tick, it's tick - 1
		if (doAction && replay != null) {
			record.applyPreviousMorph(actor, replay, tick, Record.MorphType.PAUSE);
		}
		Frame frame = record.frames.get(tick);
		actor.renderYawOffset = Blockbuster.actorPlaybackBodyYaw.get() && frame.hasBodyYaw ? frame.bodyYaw : frame.yaw;
	}
	
	private static void applyPrevMorph(Record record, int tick, Replay replay, MorphAction from) {
		if (tick >= record.frames.size())
			return;
		record.applyFrame(Math.max(0, tick - 1), actor, true); // Not tick, it's tick - 1

		if (tick >= record.actions.size())
			return;
		FoundAction found = seekPrevMorphAction(record, tick, from);
		if (found == null) {
			AbstractMorph morph = MorphUtils.copy(replay.morph);
			MorphUtils.pause(morph, null, tick);
			actor.morph.setDirect(morph);
			return;
		} else if (found.action.morph == null) {
			actor.morph.setDirect(null);
			return;
		}
		int offset1 = tick - found.tick;
		int offset2 = found.tick;
		AbstractMorph morph1 = MorphUtils.copy(found.action.morph);
		AbstractMorph morph2 = MorphUtils.copy(replay.morph);
		found = seekPrevMorphAction(record, found.tick, found.action);
		if (found != null) {
			offset2 -= found.tick;
			morph2 = MorphUtils.copy(found.action.morph);
		}
		MorphUtils.pause(morph2, null, offset2);
		MorphUtils.pause(morph1, morph2, offset1);
		actor.morph.setDirect(morph1);
	}
	
	private static FoundAction seekPrevMorphAction(Record record, int tick, MorphAction from) {
		List<Action> actions = record.getActions(tick);
		if (actions == null)
			return null;
		int first = -1;
		for (int i = 0; i < actions.size(); i++)
			if (actions.get(i) == from) {
				first = i;
				break;
			}
		if (first == -1)
			return null;
		for (int i = tick; i >= 0; i--) {
			if (record.getActions(i) != null) {
				for (int j = (first == -1 ? record.getActions(i).size() - 1 : first - 1); j >= 0; j--) {
					if (record.getAction(i, j) instanceof MorphAction) {
						ACTION.action = (MorphAction) record.getAction(i, j);
						ACTION.tick = i;
						return ACTION;
					}
				}
			}
			first = -1;
		}
		return null;
	}
	
	private GuiToggleElement showScene;
	private GuiToggleElement showOnionSkin;
	
	private boolean enableOnionSkin;
	private HashMap<Replay, OnionSkin> onionSkinMap = new HashMap<>();
	private HashMap<Replay, Boolean> actionLoaded = new HashMap<>();
	private int offset;
	private Record currentRecord;
	private int tick;
	private ArrayList<SequenceEntry> sequencerMorphs = new ArrayList<>();
	
	public GuiMorphActionExtraPanel(Minecraft mc, GuiRecordingEditorPanel panel) {
		super(mc, panel);
		this.pickMorph.pick.callback = this.mergeCallback(this.pickMorph.pick.callback, this::beginOnionSkin);
		this.pickMorph.edit.callback = this.mergeCallback(this.pickMorph.edit.callback, this::beginOnionSkin);
		
		this.showScene = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.action.morph.showscene"), null);
		this.showScene.toggled(true);
		this.showOnionSkin = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.action.morph.showonionskin"), null);
		this.showOnionSkin.toggled(true);
		
		this.showOnionSkin.flex().relative(this).x(0.0f, 10).w(100).y(1.0f, -30).anchorY(0);
		this.showScene.flex().relative(this.showOnionSkin).w(100).y(-25).anchorY(0);
		
		this.add(this.showOnionSkin, this.showScene);
	}

	@Override
	public void fill(MorphAction action) {
		endOnionSkin();
		super.fill(action);
	}

	@Override
	public void setMorph(AbstractMorph morph) {
		super.setMorph(morph);
		updateOnionSkin();
	}

	@Override
	public void disappear() {
		endOnionSkin();
		super.disappear();
	}
	
	@Override
	public void draw(GuiContext context) {
		this.showScene.setVisible(ClientProxy.panels.directorPanel.getReplays() != null && CameraHandler.canSync() && CameraHandler.isCameraEditorOpen());
		this.showOnionSkin.setVisible(ClientProxy.panels.directorPanel.getReplays() != null && CameraHandler.canSync() && CameraHandler.isCameraEditorOpen());
		
		super.draw(context);
		
		if (this.enableOnionSkin) {
			boolean update = false;
			
			// Check Actions Load
			for (Replay replay : this.actionLoaded.keySet()) {
				Record record = ClientProxy.manager.records.get(replay.id);
				record.resetUnload();
				if (!Boolean.TRUE.equals(actionLoaded.get(replay)) && record.actions != null && record.actions.size() > 0) {
					update |= true;
					actionLoaded.put(replay, true);
				}
			}
			
			// Check Current Morph Duration
			int offset = getMorphOffset();
			if (this.offset != offset) {
				this.offset = offset;
				update |= true;
			}
			
			if (this.action.morph instanceof SequencerMorph)
				update |= updateSequencerMorph((SequencerMorph) this.action.morph);
			
			if (update)
				updateOnionSkin();
		}
	}
	
	@Override
	public void remove(GuiElement element) {
		if (element instanceof GuiCreativeMorphsMenu)
			endOnionSkin();
		super.remove(element);
	}

	private void beginOnionSkin(Object obj) {
		List<Replay> replays = ClientProxy.panels.directorPanel.getReplays();
		this.enableOnionSkin = replays != null && CameraHandler.canSync() && CameraHandler.isCameraEditorOpen();
		if (this.enableOnionSkin) {
			Record current = ClientProxy.manager.records.get(this.panel.record.filename);
			if (current == null || current.frames == null || current.frames.isEmpty())
				this.enableOnionSkin = false;
		}
		if (this.enableOnionSkin) {
			for (Replay replay : replays) {
				if (!replay.enabled) continue;
				Record record = ClientProxy.manager.records.get(replay.id);
				if (record == null || record.frames == null || record.frames.isEmpty()) continue;
				if (record.actions == null || record.actions.size() == 0) {
					Dispatcher.sendToServer(new PacketRequestAction(replay.id, false));
					actionLoaded.put(replay, false);
				} else
					actionLoaded.put(replay, true);
				this.onionSkinMap.put(replay, new OnionSkin());
			}
			
			this.currentRecord = this.panel.record;
			this.tick = this.panel.selector.tick;
			this.offset = getMorphOffset();
			OnionSkinManager.setOnionSkins(this.onionSkinMap.values().toArray(new OnionSkin[0]));
			OnionSkinManager.push();
			updateOnionSkin();
		}
	}
	
	private void updateOnionSkin() {
		if (this.enableOnionSkin) {
			int actualTick = this.tick + this.offset;
			applyRecord(this.currentRecord, actualTick, null, false);
			Vec3d base = actor.getPositionVector();
			float yaw = actor.rotationYaw;
			
			Color4f color = new Color4f();
			boolean lighting = false;
			for (Replay replay : this.onionSkinMap.keySet()) {
				if (replay.id.equals(this.currentRecord.filename)) {
					// Onion Skin
					lighting = false;
					color.set(1f, 1f, 0f, 0.5f);
					if (!this.showOnionSkin.isToggled() || !this.showOnionSkin.isVisible() || actualTick == 0)
						actor.morph.setDirect(null);
					else if (this.offset == 0)
						applyPrevMorph(ClientProxy.manager.records.get(replay.id), this.tick, replay, this.action);
					else
						applyRecord(ClientProxy.manager.records.get(replay.id), this.tick, replay, true);
				} else {
					// Scene
					lighting = true;
					color.set(0.5f, 0.5f, 0.5f, 1f);
					if (!this.showScene.isToggled() || !this.showScene.isVisible())
						actor.morph.setDirect(null);
					else
						applyRecord(ClientProxy.manager.records.get(replay.id), actualTick, replay, true);
				}
				Vec3d pos = actor.getPositionVector().subtract(base);
				pos = pos.rotateYaw((float) Math.toRadians(yaw));
				this.onionSkinMap.get(replay)
					.color(color.x, color.y, color.z, color.w)
					.morph(actor.morph.get())
					.offset(pos.x, pos.y, pos.z, actor.rotationPitch, actor.rotationYaw - yaw, actor.renderYawOffset - yaw)
					.light(lighting);
			}
		}
	}
	
	private void endOnionSkin() {
		if (this.enableOnionSkin) {
			this.enableOnionSkin = false;
			OnionSkinManager.pop();
			OnionSkinManager.setOnionSkins();
			this.tick = -1;
			this.currentRecord = null;
			this.offset = 0;
			this.actionLoaded.clear();
			this.onionSkinMap.clear();
			this.sequencerMorphs.clear();
			if (CameraHandler.isCameraEditorOpen())
				((GuiCameraEditor)Minecraft.getMinecraft().currentScreen).haveScrubbed();
		}
	}

	private <T> Consumer<T> mergeCallback(Consumer<T> callback, Consumer<Object> addon) {
		return obj -> {
			callback.accept(obj);
			addon.accept(obj);
		};
	}
	
	private int getMorphOffset() {
		if (this.action.morph != null) {
			if (this.action.morph instanceof IAnimationProvider) {
				IAnimationProvider morph = (IAnimationProvider) this.action.morph;
				return morph.getAnimation().animates ? morph.getAnimation().duration : 0;
			} else if (this.action.morph instanceof SequencerMorph) {
				List<GuiCreativeMorphsMenu> children = this.getChildren(GuiCreativeMorphsMenu.class);
				if (children.size() > 0) {
					GuiCreativeMorphsMenu morphs = children.get(0);
					if (morphs.editor.delegate != null 
							&& morphs.editor.delegate instanceof GuiSequencerMorph
							&& morphs.editor.delegate.view.delegate != null
							&& morphs.editor.delegate.view.delegate instanceof GuiSequencerMorphPanel) {
						GuiSequencerMorphPanel gui = (GuiSequencerMorphPanel) morphs.editor.delegate.view.delegate;
						if (gui.morph == this.action.morph) {
							if (gui.morph.isRandom) {
								int current = this.panel.selector.tick;
								int nextTick = -1;
								for (int i = current + 1; i < this.panel.record.actions.size(); i++) {
									List<Action> actions = this.panel.record.actions.get(i);
									if (actions == null)
										continue;
									for (Action action : actions) {
										if (action instanceof MorphAction) {
											nextTick = i;
											break;
										}
									}
									if (nextTick > current) {
										return nextTick - current;
									}
								}
							}
							
							int index = -1;
							for (int i = 0; i < gui.morph.morphs.size(); i++)
								if (gui.entry == gui.morph.morphs.get(i)) {
									index = i;
									break;
								}
							int offset = (int) SequencerMorphManager.calcDuration(gui.morph, index);
							// 2.2
							try {
								Field fieldPreviewMorph = GuiSequencerMorphPanel.class.getDeclaredField("previewMorph");
								Field fieldTick = GuiSequencerMorphPanel.class.getDeclaredField("tick");
								fieldPreviewMorph.setAccessible(true);
								fieldTick.setAccessible(true);
								Morph previewMorph = (Morph) fieldPreviewMorph.get(gui);
								if (previewMorph.get() == ((GuiMorphRenderer)gui.editor.renderer).morph) {
									offset = fieldTick.getInt(gui);
									if (previewMorph.get() instanceof SequencerMorph)
										offset = ((SequencerMorph) previewMorph.get()).timer;
								}
							} catch (Exception e) {}
							return offset;
						}
					}
				}
				return this.offset;
			}
		}
		return 0;
	}
	
	private boolean updateSequencerMorph(SequencerMorph morph) {
		boolean update = this.sequencerMorphs.size() != morph.morphs.size();
		if (!update) {
			for (int i = 0; i < this.sequencerMorphs.size(); i++) {
				update |= this.sequencerMorphs.get(i).morph != morph.morphs.get(i).morph;
				if (update)
					break;
			}
		}
		if (update) {
			this.sequencerMorphs.clear();
			for (SequenceEntry amorph : morph.morphs)
				this.sequencerMorphs.add(amorph);
		}
		return update;
	}

}
