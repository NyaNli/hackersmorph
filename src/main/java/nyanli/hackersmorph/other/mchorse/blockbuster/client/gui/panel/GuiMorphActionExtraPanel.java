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
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.color.GuiColorPicker;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Color;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.client.gui.creative.GuiCreativeMorphsMenu;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.config.Property;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.config.Config;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager.OnionSkin;

public class GuiMorphActionExtraPanel extends GuiMorphActionPanel {
	
	public static final String CATEGORY = "MorphAction";

	private static final FoundAction ACTION = new FoundAction();
	private static EntityActor actor;
	
	private static void refreshActor() {
		if (actor == null || actor.world != Minecraft.getMinecraft().world) {
			actor = new EntityActor(Minecraft.getMinecraft().world);
			actor.manual = true;
		}
	}
	
	private static void applyRecord(Record record, int tick, Replay replay, boolean doAction) {
		refreshActor();
		actor.morph.setDirect(null);
		if (!applyFrame(record, tick))
			return;
		if (doAction && replay != null) {
			record.applyPreviousMorph(actor, replay, tick, Record.MorphType.PAUSE);
		}
		Frame frame = record.frames.get(Math.max(0, tick - 1));
		actor.renderYawOffset = Blockbuster.actorPlaybackBodyYaw.get() && frame.hasBodyYaw ? frame.bodyYaw : frame.yaw;
	}
	
	private static void applyPrevMorph(Record record, int tick, Replay replay, MorphAction from) {
		refreshActor();
		if (!applyFrame(record, tick))
			return;
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
		MorphAction morphAction = found.action;
		AbstractMorph prevMorph = replay.morph;
		found = seekPrevMorphAction(record, found.tick, found.action);
		if (found != null) {
			offset2 -= found.tick;
			prevMorph = found.action.morph;
		}
		morphAction.applyWithOffset(actor, offset1, prevMorph, offset2);
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
	
	private static boolean applyFrame(Record record, int tick) {
		if (tick >= record.frames.size())
			return false;
		int tick0 = Math.max(0, tick - 1); // Not tick, it's tick - 1
		record.applyFrame(tick0, actor, true);

		Frame frame = record.frames.get(tick0);
		actor.renderYawOffset = Blockbuster.actorPlaybackBodyYaw.get() && tick > 0 && frame.hasBodyYaw ? frame.bodyYaw : frame.yaw;
		return true;
	}
	
	private Property cfgShowScene;
	private Property cfgOtherColor;
	private Property cfgShowOnionSkin;
	private Property cfgSkinColor;
	
	private GuiToggleElement showScene;
	private GuiColorElement otherColor;
	private GuiToggleElement showOnionSkin;
	private GuiColorElement skinColor;
	
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
		
		Config cfg = HackersMorph.getConfig();
		
		this.cfgShowScene = cfg.getConfig(CATEGORY, "showscene", true);
		this.cfgOtherColor = cfg.getConfig(CATEGORY, "scenecolor", "#FF7F7F7F");
		this.cfgShowOnionSkin = cfg.getConfig(CATEGORY, "showonionskin", true);
		this.cfgSkinColor = cfg.getConfig(CATEGORY, "onionskincolor", "#7FFFFF00");
		
		this.showScene = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.action.morph.showscene"), b -> this.cfgShowScene.set(b.isToggled()));
		this.showScene.toggled(this.cfgShowScene.getBoolean());
		this.otherColor = new GuiColorElement(mc, c -> this.cfgOtherColor.set(String.format("#%08X", c))).onTop();
		this.otherColor.picker.editAlpha().setColor(Long.decode(cfgOtherColor.getString()).intValue());
		this.otherColor.tooltip(IKey.lang("hackersmorph.gui.action.morph.showscene.color"));
		this.showOnionSkin = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.action.morph.showonionskin"), b -> this.cfgShowOnionSkin.set(b.isToggled()));
		this.showOnionSkin.toggled(this.cfgShowOnionSkin.getBoolean());
		this.skinColor = new GuiColorElement(mc, c -> this.cfgSkinColor.set(String.format("#%08X", c))).onTop();
		this.skinColor.picker.editAlpha().setColor(Long.decode(cfgSkinColor.getString()).intValue());
		this.skinColor.tooltip(IKey.lang("hackersmorph.gui.action.morph.showonionskin.color"));
		
		this.skinColor.flex().relative(this).x(0.0f, 10).w(100).y(1.0f, -30).anchorY(0);
		this.showOnionSkin.flex().relative(this.skinColor).w(100).y(-20).anchorY(0);
		this.otherColor.flex().relative(this.showOnionSkin).w(100).y(-25).anchorY(0);
		this.showScene.flex().relative(this.otherColor).w(100).y(-20).anchorY(0);
		
		this.add(this.skinColor, this.showOnionSkin, this.otherColor, this.showScene);
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
		HackersMorph.getConfig().save();
		endOnionSkin();
		super.disappear();
	}
	
	@Override
	public void draw(GuiContext context) {
		boolean v = ClientProxy.panels.directorPanel.getReplays() != null && CameraHandler.canSync() && CameraHandler.isCameraEditorOpen();
		this.showScene.setVisible(v);
		this.otherColor.setVisible(v);
		this.showOnionSkin.setVisible(v);
		this.skinColor.setVisible(v);
		
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
			else
				this.currentRecord = current;
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
			
			Color color = null;
			boolean lighting = false;
			for (Replay replay : this.onionSkinMap.keySet()) {
				if (replay.id.equals(this.currentRecord.filename)) {
					// Onion Skin
					lighting = false;
					color = this.skinColor.picker.color;
					if (!this.showOnionSkin.isToggled() || !this.showOnionSkin.isVisible())
						actor.morph.setDirect(null);
					else if (this.offset == 0)
						applyPrevMorph(ClientProxy.manager.records.get(replay.id), this.tick, replay, this.action);
					else
						applyRecord(ClientProxy.manager.records.get(replay.id), this.tick, replay, true);
					actor.renderYawOffset = actor.rotationYaw = yaw;
					actor.rotationPitch = 0;
				} else {
					// Scene
					lighting = true;
					color = this.otherColor.picker.color;
					if (!this.showScene.isToggled() || !this.showScene.isVisible())
						actor.morph.setDirect(null);
					else
						applyRecord(ClientProxy.manager.records.get(replay.id), actualTick, replay, true);
				}
				Vec3d pos = actor.getPositionVector().subtract(base);
				pos = pos.rotateYaw((float) Math.toRadians(yaw));
				this.onionSkinMap.get(replay)
					.color(color.r, color.g, color.b, color.a)
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
