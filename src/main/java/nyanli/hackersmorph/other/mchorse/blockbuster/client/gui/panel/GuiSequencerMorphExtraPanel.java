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
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.input.color.GuiColorPicker;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.keys.LangKey;
import mchorse.mclib.utils.Color;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Property;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.config.Config;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SequencerMorphManager.ExtraProps;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager;
import nyanli.hackersmorph.other.mchorse.metamorph.client.manager.OnionSkinManager.OnionSkin;
import nyanli.hackersmorph.util.MorphAnimUtils;

public class GuiSequencerMorphExtraPanel extends GuiSequencerMorphPanel {
	
	private static final String CATEGORY = "SequencerMorph";
	
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
	
	private Property cfgOnionSkin;
	private Property cfgSkinPast;
	private Property cfgSkinLater;
	private Property cfgPastColor;
	private Property cfgLaterColor;
	private Property cfgOffsetColor;

	private GuiListElement<SequenceEntry> list;
	
	private GuiToggleElement onionSkin;
	private GuiTrackpadElement skinPast;
	private GuiColorElement pastColor;
	private GuiTrackpadElement skinLater;
	private GuiColorElement laterColor;
	
	private GuiLabel repeatOffset;
	private GuiTrackpadElement repeatTimes;
	private GuiTrackpadElement offsetX;
	private GuiTrackpadElement offsetY;
	private GuiTrackpadElement offsetZ;
	private GuiColorElement offsetColor;
	
	private ExtraProps prop;
	
	public GuiSequencerMorphExtraPanel(Minecraft mc, GuiSequencerMorph editor) {
		super(mc, editor);

		Config cfg = HackersMorph.getConfig();
		
		this.cfgOnionSkin = cfg.getConfig(CATEGORY, "onionskin", false);
		this.cfgSkinPast = cfg.getConfig(CATEGORY, "pastcount", 1);
		this.cfgSkinLater = cfg.getConfig(CATEGORY, "latercount", 1);
		this.cfgPastColor = cfg.getConfig(CATEGORY, "pastcolor", "#CCFF0000");
		this.cfgLaterColor = cfg.getConfig(CATEGORY, "latercolor", "#CC00FF00");
		this.cfgOffsetColor = cfg.getConfig(CATEGORY, "offsetcolor", "#C07F7FFF");
		
		this.list = this.getChildren(GuiSequenceEntryList.class).get(0);
		this.list.callback = mergeCallback(this.list.callback, this::updateOnionSkin);
		for (GuiButtonElement btn : this.getChildren(GuiButtonElement.class)) {
			if (btn.getParent() == this) {
				if (btn.label instanceof LangKey && "blockbuster.gui.add".equals(((LangKey) btn.label).key)) {
					btn.callback = addBtn(btn.callback);
				}
				btn.callback = mergeCallback(btn.callback, this::updateOnionSkin);
			}
		}
		
		this.onionSkin = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.sequencer.onionskin"), b -> this.cfgOnionSkin.set(b.isToggled()));
		this.onionSkin.toggled(this.cfgOnionSkin.getBoolean());
		this.skinPast = new GuiTrackpadElement(mc, i -> this.cfgSkinPast.set(i.intValue())).integer().limit(0, 10);
		this.skinPast.setValue(this.cfgSkinPast.getInt());
		this.skinLater = new GuiTrackpadElement(mc, i -> this.cfgSkinLater.set(i.intValue())).integer().limit(0, 10);
		this.skinLater.setValue(this.cfgSkinLater.getInt());
		this.pastColor = new GuiColorElement(mc, c -> this.cfgPastColor.set(String.format("#%08X", c)));
		this.pastColor.picker.editAlpha().setColor(Long.decode(cfgPastColor.getString()).intValue());
		this.laterColor = new GuiColorElement(mc, c -> this.cfgLaterColor.set(String.format("#%08X", c)));
		this.laterColor.picker.editAlpha().setColor(Long.decode(cfgLaterColor.getString()).intValue());

		this.onionSkin.callback = mergeCallback(this.onionSkin.callback, this::updateOnionSkin);
		this.skinPast.callback = mergeCallback(this.skinPast.callback, this::updateOnionSkin);
		this.skinLater.callback = mergeCallback(this.skinLater.callback, this::updateOnionSkin);
		this.pastColor.picker.callback = mergeCallback(this.pastColor.picker.callback, this::updateOnionSkin);
		this.laterColor.picker.callback = mergeCallback(this.laterColor.picker.callback, this::updateOnionSkin);
		
		this.onionSkin.callback = mergeCallback(this.onionSkin.callback, this::stopPlayback);
		this.skinPast.callback = mergeCallback(this.skinPast.callback, this::stopPlayback);
		this.skinLater.callback = mergeCallback(this.skinLater.callback, this::stopPlayback);
		this.pastColor.picker.callback = mergeCallback(this.pastColor.picker.callback, this::stopPlayback);
		this.laterColor.picker.callback = mergeCallback(this.laterColor.picker.callback, this::stopPlayback);

		this.skinPast.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.past"));
		this.skinLater.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.later"));
		this.pastColor.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.past.color"));
		this.laterColor.tooltip(IKey.lang("hackersmorph.gui.sequencer.onionskin.later.color"));
		
		this.onionSkin.flex().relative(this).w(105).x(1.0f, -115).y(0f, 10);
		GuiElement row1 = new GuiElement(mc);
		row1.flex().relative(this.onionSkin).wh(105, 20).y(20).row(0);
		row1.add(this.skinPast, this.skinLater);
		GuiElement row2 = new GuiElement(mc);
		row2.flex().relative(row1).wh(105, 20).y(20).row(0);
		row2.add(this.pastColor, this.laterColor);

		this.add(this.onionSkin, row1, row2);
		
		this.repeatOffset = new GuiLabel(mc, IKey.lang("hackersmorph.gui.sequencer.repeatoffset"));
		this.repeatTimes = new GuiTrackpadElement(mc, v -> prop.repeatTimes = v.intValue()).integer().limit(0);
		this.offsetX = new GuiTrackpadElement(mc, v -> prop.offsetX = v).values(0.05, 0.005, 0.5);
		this.offsetY = new GuiTrackpadElement(mc, v -> prop.offsetY = v).values(0.05, 0.005, 0.5);
		this.offsetZ = new GuiTrackpadElement(mc, v -> prop.offsetZ = v).values(0.05, 0.005, 0.5);
		this.offsetColor = new GuiColorElement(mc, c -> this.cfgOffsetColor.set(String.format("#%08X", c)));
		this.offsetColor.picker.editAlpha().setColor(Long.decode(cfgOffsetColor.getString()).intValue());
		
		this.repeatTimes.callback = mergeCallback(this.repeatTimes.callback, this::updateOnionSkin);
		this.offsetX.callback = mergeCallback(this.offsetX.callback, this::updateOnionSkin);
		this.offsetY.callback = mergeCallback(this.offsetY.callback, this::updateOnionSkin);
		this.offsetZ.callback = mergeCallback(this.offsetZ.callback, this::updateOnionSkin);
		this.offsetColor.picker.callback = mergeCallback(this.offsetColor.picker.callback, this::updateOnionSkin);
		
		this.repeatTimes.callback = mergeCallback(this.repeatTimes.callback, this::stopPlayback);
		this.offsetX.callback = mergeCallback(this.offsetX.callback, this::stopPlayback);
		this.offsetY.callback = mergeCallback(this.offsetY.callback, this::stopPlayback);
		this.offsetZ.callback = mergeCallback(this.offsetZ.callback, this::stopPlayback);
		this.offsetColor.picker.callback = mergeCallback(this.offsetColor.picker.callback, this::stopPlayback);
		
		this.repeatTimes.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.times"));
		this.offsetX.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.x"));
		this.offsetY.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.y"));
		this.offsetZ.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.z"));
		this.offsetColor.tooltip(IKey.lang("hackersmorph.gui.sequencer.repeatoffset.color"));

		this.repeatOffset.flex().relative(this.pastColor).w(105).y(30);
		this.repeatTimes.flex().relative(this.repeatOffset).w(105).y(20);
		this.offsetX.flex().relative(this.repeatTimes).w(105).y(20);
		this.offsetY.flex().relative(this.offsetX).w(105).y(20);
		this.offsetZ.flex().relative(this.offsetY).w(105).y(20);
		this.offsetColor.flex().relative(this.offsetZ).w(105).y(20);
		
		this.add(this.repeatOffset, this.repeatTimes, this.offsetX, this.offsetY, this.offsetZ, this.offsetColor);
		
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
		HackersMorph.getConfig().save();
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
		Color pastColor = this.pastColor.picker.color.copy();
		if (past > 0) {
			pastColor.a /= past;
			pastColor.r /= past;
			pastColor.g /= past;
			pastColor.b /= past;
		}
		Color laterColor = this.laterColor.picker.color.copy();
		if (later > 0) {
			laterColor.a /= later;
			laterColor.r /= later;
			laterColor.g /= later;
			laterColor.b /= later;
		}
		Color offsetColor = this.offsetColor.picker.color.copy();
//		if (index == -1 && this.list.getList().size() != 0)
//			index = 0;
		if (!this.onionSkin.isToggled())
			index = -1;
		if (index != -1) {
			for (int i = 10; i > 0; i--) {
				if (i <= past && index - i >= 0) {
					int c = past - i + 1;
					skins.add(new OnionSkin().morph(list.get(index - i).morph).light(false).color(pastColor.r * c, pastColor.g * c, pastColor.b * c, pastColor.a * c));					
				}
				if (i <= later && index + i < list.size()) {
					int c = later - i + 1;
					skins.add(new OnionSkin().morph(list.get(index + i).morph).light(false).color(laterColor.r * c, laterColor.g * c, laterColor.b * c, laterColor.a * c));
				}
			}
			if (this.repeatTimes.value > 0) {
				skins.add(new OnionSkin()
						.morph(getNextRepeatMorph())
						.color(offsetColor.r, offsetColor.g, offsetColor.b, offsetColor.a)
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
	
	private Consumer<GuiButtonElement> addBtn(Consumer<GuiButtonElement> callback) {
		return callback == null ? null : btn -> {
			int index = this.list.getIndex();
			callback.accept(btn);
			if (index >= 0 && GuiScreen.isCtrlKeyDown()) {
				index++;
				SequenceEntry entry = this.list.getList().remove(this.list.getIndex());
				this.list.getList().add(index, entry);
				this.list.setIndex(index);
				this.list.update();
			}
		};
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
			super.drawUserModel(context);
		}
		
		public int getOffsetTick(GuiContext context) {
			return (int) MathHelper.clamp(context.tick - this.lastTick, 0, 10);
		}
		
	}
	
}
