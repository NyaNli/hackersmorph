package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mchorse.blockbuster_pack.client.gui.GuiSequencerMorph;
import mchorse.blockbuster_pack.client.gui.GuiSequencerMorph.GuiSequenceEntryList;
import mchorse.blockbuster_pack.client.gui.GuiSequencerMorph.GuiSequencerMorphPanel;
import mchorse.blockbuster_pack.morphs.SequencerMorph;
import mchorse.blockbuster_pack.morphs.SequencerMorph.SequenceEntry;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager.ExtraProps;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager.OnionSkin;
import nyanli.hackersmorph.util.MorphAnimUtils;

public class GuiSequencerMorphExtraPanel extends GuiSequencerMorphPanel {
	
	private static boolean newVersion;
	private static Field fieldPreviewMorph;
	private static Field fieldTick;
	private static Field fieldPlaying;
	private static Method methodStopPlayback;
	
	static {
		try {
			fieldPreviewMorph = GuiSequencerMorphPanel.class.getDeclaredField("previewMorph");
			fieldTick = GuiSequencerMorphPanel.class.getDeclaredField("tick");
			fieldPlaying = GuiSequencerMorphPanel.class.getDeclaredField("playing");
			methodStopPlayback = GuiSequencerMorphPanel.class.getDeclaredMethod("stopPlayback");
			fieldPreviewMorph.setAccessible(true);
			fieldTick.setAccessible(true);
			fieldPlaying.setAccessible(true);
			methodStopPlayback.setAccessible(true);
			newVersion = true;
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			newVersion = false;
		}
	}

	private GuiListElement<SequenceEntry> list;
	
	private GuiToggleElement onionSkin;
	private GuiTrackpadElement skinPast;
	private GuiTrackpadElement skinLater;
	
	private GuiLabel repeatOffset;
	private GuiTrackpadElement repeatTimes;
	private GuiTrackpadElement offsetX;
	private GuiTrackpadElement offsetY;
	private GuiTrackpadElement offsetZ;
	
	private ExtraProps prop;
	
	public GuiSequencerMorphExtraPanel(Minecraft mc, GuiSequencerMorph editor) {
		super(mc, editor);
		
		this.list = this.getChildren(GuiSequenceEntryList.class).get(0);
		this.list.callback = mergeCallback(this.list.callback, this::updateOnionSkin);
		for (GuiButtonElement btn : this.getChildren(GuiButtonElement.class)) {
			if (btn.getParent() == this)
				btn.callback = mergeCallback(btn.callback, this::updateOnionSkin);
		}
		
		this.onionSkin = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.sequencer.onionskin"), this::updateOnionSkin);
		this.skinPast = new GuiTrackpadElement(mc, this::updateOnionSkin).integer().limit(0, 4);
		this.skinLater = new GuiTrackpadElement(mc, this::updateOnionSkin).integer().limit(0, 4);
		
		this.onionSkin.callback = mergeCallback(this.onionSkin.callback, this::stopPlayback);
		this.skinPast.callback = mergeCallback(this.skinPast.callback, this::stopPlayback);
		this.skinLater.callback = mergeCallback(this.skinLater.callback, this::stopPlayback);

		this.skinPast.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.past"));
		this.skinLater.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.later"));
		
		this.onionSkin.flex().relative(this).w(105).x(1.0f, -115).y(0f, 10);
		this.skinPast.flex().relative(this.onionSkin).w(52).y(20);
		this.skinLater.flex().relative(this.skinPast).w(52).x(53);

		this.add(this.onionSkin, this.skinPast, this.skinLater);
		
		this.repeatOffset = new GuiLabel(mc, IKey.lang("hackersmorph.gui.sequencer.repeatoffset"));
		this.repeatTimes = new GuiTrackpadElement(mc, v -> prop.repeatTimes = v.intValue()).integer().limit(0);
		this.offsetX = new GuiTrackpadElement(mc, v -> prop.offsetX = v).values(0.05, 0.005, 0.5);
		this.offsetY = new GuiTrackpadElement(mc, v -> prop.offsetY = v).values(0.05, 0.005, 0.5);
		this.offsetZ = new GuiTrackpadElement(mc, v -> prop.offsetZ = v).values(0.05, 0.005, 0.5);
		
		this.repeatTimes.callback = mergeCallback(this.repeatTimes.callback, this::stopPlayback);
		this.offsetX.callback = mergeCallback(this.offsetX.callback, this::stopPlayback);
		this.offsetY.callback = mergeCallback(this.offsetY.callback, this::stopPlayback);
		this.offsetZ.callback = mergeCallback(this.offsetZ.callback, this::stopPlayback);
		
		this.repeatTimes.callback = mergeCallback(this.repeatTimes.callback, this::updateOnionSkin);
		this.offsetX.callback = mergeCallback(this.offsetX.callback, this::updateOnionSkin);
		this.offsetY.callback = mergeCallback(this.offsetY.callback, this::updateOnionSkin);
		this.offsetZ.callback = mergeCallback(this.offsetZ.callback, this::updateOnionSkin);
		
		this.repeatTimes.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.times"));
		this.offsetX.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.x"));
		this.offsetY.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.y"));
		this.offsetZ.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.z"));

		this.repeatOffset.flex().relative(this.skinPast).w(105).y(30);
		this.repeatTimes.flex().relative(this.repeatOffset).w(105).y(20);
		this.offsetX.flex().relative(this.repeatTimes).w(105).y(20);
		this.offsetY.flex().relative(this.offsetX).w(105).y(20);
		this.offsetZ.flex().relative(this.offsetY).w(105).y(20);
		
		this.add(this.repeatOffset, this.repeatTimes, this.offsetX, this.offsetY, this.offsetZ);
		
		// 2.2
		if (newVersion) {
			this.editor.renderer.removeFromParent();
			this.editor.renderer = new GuiSequencerPreviewer(mc, this);
			this.editor.renderer.flex().relative(this.editor).wh(1.0f, 1.0f);
			this.editor.prepend(this.editor.renderer);
			
			this.preview.callback = mergeCallback(this.preview.callback, this::preview);
			this.plause.callback = mergeCallback(this.plause.callback, this::preview);
			this.stop.callback = mergeCallback(this.stop.callback, this::selectFirst);
			this.list.callback = mergeCallback(this.list.callback, this::stopPreview);
		}
	}

	@Override
	public void fillData(SequencerMorph morph) {
		super.fillData(morph);
		this.prop = SequencerMorphManager.getExProps(morph);
		this.repeatTimes.setValue(prop.repeatTimes);
		this.offsetX.setValue(prop.offsetX);
		this.offsetY.setValue(prop.offsetY);
		this.offsetZ.setValue(prop.offsetZ);
	}

	@Override
	public void startEditing() {
		super.startEditing();
		this.updateOnionSkin(null);
	}

	@Override
	public void finishEditing() {
		super.finishEditing();
		OnionSkinManager.setOnionSkins();
	}

	@Override
	public void fromNBT(NBTTagCompound tag) {
		super.fromNBT(tag);
		this.onionSkin.toggled(tag.getBoolean("onionskin"));
		this.skinPast.setValue(tag.getInteger("onionpast"));
		this.skinLater.setValue(tag.getInteger("onionlater"));
		this.updateOnionSkin(null);
	}

	@Override
	public NBTTagCompound toNBT() {
		NBTTagCompound tag = super.toNBT();
		tag.setBoolean("onionskin", this.onionSkin.isToggled());
		tag.setInteger("onionpast", (int)this.skinPast.value);
		tag.setInteger("onionlater", (int)this.skinLater.value);
		return tag;
	}

	private <T> Consumer<T> mergeCallback(Consumer<T> callback, Consumer<Object> addon) {
		return obj -> {
			callback.accept(obj);
			addon.accept(obj);
		};
	}
	
	private void updateOnionSkin(Object obj) {
		ArrayList<OnionSkin> skins = new ArrayList<>();
		List<SequenceEntry> list = this.list.getList();
		int index = this.list.getIndex();
		int past = (int) this.skinPast.value;
		int later = (int) this.skinLater.value;
//		if (index == -1 && this.list.getList().size() != 0)
//			index = 0;
		if (!this.onionSkin.isToggled())
			index = -1;
		if (index != -1) {
			for (int i = 5 - past; i < 5; i++) {
				int off = index - 5 + i;
				if (off < 0)
					continue;
				skins.add(new OnionSkin().morph(list.get(off).morph).light(false).color(0.2f + 0.2f * i, 0, 0, 0.2f * i));
			}
			for (int i = 1; i <= later; i++) {
				int off = index + i;
				if (off >= this.list.getList().size())
					break;
				skins.add(new OnionSkin().morph(list.get(off).morph).light(false).color(0, 0.2f * (6 - i), 0, 0.2f * (5 - i)));
			}
			if (this.repeatTimes.value > 0) {
				skins.add(new OnionSkin()
						.morph(getNextRepeatMorph())
						.color(0.5f, 0.5f, 1.0f, 0.75f)
						.offset(this.offsetX.value * 0.0625, this.offsetY.value * 0.0625, this.offsetZ.value * 0.0625, 0, 0, 0));
			}
		}
		OnionSkinManager.setOnionSkins(skins.toArray(new OnionSkin[0]));
	}
	
	private AbstractMorph getNextRepeatMorph() {
		List<SequenceEntry> list = this.list.getList();
		if (list.size() == 0) return null;
		AbstractMorph morph = list.get(0).morph;
		AbstractMorph previous = list.get(list.size() - 1).morph;
		if (morph == previous) return morph;
		morph = MorphUtils.copy(morph);
		if (list.get(0).setDuration && morph instanceof IAnimationProvider)
			((IAnimationProvider) morph).getAnimation().duration = (int) list.get(0).getDuration(SequencerMorphManager.getRandomSeed(this.morph.getDuration()));
		MorphUtils.pause(morph, previous, 0);
		return morph;
	}
	
	private void preview(Object obj) {
		this.list.current.clear();
		this.entry = null;
		this.elements.setVisible(false);
		updateOnionSkin(obj);
	}
	
	private void stopPreview(Object obj) {
		if (newVersion) {
			try {
				if (fieldPlaying.getBoolean(this))
					stopPlayback(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {}
		}
	}
	
	private void stopPlayback(Object obj) {
		if (newVersion) {
			try {
				methodStopPlayback.invoke(this);
				selectFirst(obj);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
		}
	}
	
	private void selectFirst(Object obj) {
		if (this.list.getIndex() == -1 && this.list.getList().size() > 0) {
			this.list.setIndex(0);
			this.list.callback.accept(this.list.getCurrent());
		}
	}
	
	public static class GuiSequencerPreviewer extends GuiMorphRenderer {

		private static final SequencerMorph previewer = new SequencerMorph();
		
		private GuiSequencerMorphExtraPanel panel;
		private boolean preview = false;
		private boolean playing = false;
		private long lastTick;
		private int lastTimer = -1;
		
		public GuiSequencerPreviewer(Minecraft mc, GuiSequencerMorphExtraPanel panel) {
			super(mc);
			this.panel = panel;
		}

		@Override
		protected void drawUserModel(GuiContext context) {
			if (newVersion) {
				try {
					boolean playing = fieldPlaying.getBoolean(this.panel);
					Morph previewMorph = (Morph) fieldPreviewMorph.get(this.panel);
					if (previewMorph.get() == this.morph) {
						int timer = fieldTick.getInt(this.panel);
						
						if (this.morph != previewer) {
							this.morph = previewer;
							previewMorph.setDirect(previewer);
						}
						if (playing && this.playing) {
							timer += getOffsetTick(context);
							MorphAnimUtils.updateTick(previewer, timer - previewer.timer);
						} else {
							if (playing || this.lastTimer != timer) {
								if (!this.preview) {
									previewer.reset();
									previewer.copy(this.panel.morph);
								}
								previewer.pause(null, timer);
								if (playing) {
									MorphAnimUtils.unpauseAll(previewer);
									this.lastTimer = -1;
								} else
									this.lastTimer = timer;
							}
						}
						this.preview = true;
						this.playing = playing;
					} else {
						this.preview = false;
						this.playing = false;
						this.lastTimer = -1;
					}
					this.lastTick = context.tick;
				} catch (Exception e) {}
			}
			float yaw = this.yaw;
			this.yaw = 0;
			super.drawUserModel(context);
			this.yaw = yaw;
		}
		
		public int getOffsetTick(GuiContext context) {
			return (int) MathHelper.clamp(context.tick - this.lastTick, 0, 10);
		}
		
	}
	
}
