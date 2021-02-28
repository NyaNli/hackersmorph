package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import mchorse.blockbuster_pack.morphs.StructureMorph;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.metamorph.client.gui.editor.GuiAnimation;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.GuiStructureMorph;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.element.GuiCurveTool;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager.CurveKeyframeChannel;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager.ExtraProps;

public class GuiStructureMorphPanel extends GuiMorphPanel<StructureMorph, GuiStructureMorph> {

	private GuiAnimation anim;
	private GuiButtonElement reload;
	private GuiToggleElement topLevel;
	private GuiToggleElement noNormal;
	private GuiToggleElement standalone;
	private GuiToggleElement custom;
	private GuiButtonElement sky;
	private GuiButtonElement block;
	private GuiCurveTool curve;
	private GuiButtonElement reset;
	private GuiTransformations trans;
	
	private GuiSearchListElement<String> biomes;
	
	private ExtraProps prop;
	
	public GuiStructureMorphPanel(Minecraft mc, GuiStructureMorph editor) {
		super(mc, editor);
		
		this.anim = new GuiAnimation(mc, false);
		this.anim.flex().relative(this).xy(120, 0).w(130);
		
		this.reload = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.structure.reload"), btn -> StructureMorphExtraManager.removeRenderer(this.morph));
		this.reload.flex().relative(this).xy(10, 10).w(110);
		this.reload.tooltip(IKey.lang("hackersmorph.gui.structure.reload.tooltip"));
		
		this.topLevel = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.structure.toplevel"), toggle -> this.prop.topLevel = toggle.isToggled());
		this.topLevel.flex().relative(this.reload).y(25).w(110);
		this.topLevel.tooltip(IKey.lang("hackersmorph.gui.structure.toplevel.tooltip"));
		
		this.noNormal = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.structure.nonormal"), toggle -> this.prop.fakeNormal = toggle.isToggled());
		this.noNormal.flex().relative(this.topLevel).y(20).w(110);
		this.noNormal.tooltip(IKey.lang("hackersmorph.gui.structure.nonormal.tooltip"));
		
		this.standalone = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.structure.lighting"), this::toggle);
		this.standalone.flex().relative(this.noNormal).y(20).w(110);
		this.standalone.tooltip(IKey.lang("hackersmorph.gui.structure.lighting.tooltip"));
		
		this.custom = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.structure.custom"), this::toggle);
		this.custom.flex().relative(this.standalone).y(20).w(110);
		this.custom.tooltip(IKey.lang("hackersmorph.gui.structure.custom.tooltip"));
		
		this.sky = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.structure.custom.sky"), btn -> setCurve(false));
		this.sky.flex().relative(this.custom).y(25).w(50);
		
		this.block = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.structure.custom.block"), btn -> setCurve(true));
		this.block.flex().relative(this.sky).x(50).w(50);
		
		this.curve = new GuiCurveTool(mc, this, 240);
		this.curve.flex().relative(this.sky).y(20).wh(200, 200);
		this.curve.resize();
		
		this.reset = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.structure.custom.reset"), btn -> reset());
		this.reset.flex().relative(this.curve).y(200).w(200);
		
		this.biomes = new GuiSearchListElement<String>(mc, str -> this.prop.biome = str.get(0)) {

			@Override
			protected GuiListElement<String> createList(Minecraft mc, Consumer<List<String>> callback) {
				return new GuiListElement<String>(mc, callback) {
					
					@Override
					protected boolean sortElements() {
						Collections.<String>sort(this.list, (a, b) -> a.compareTo(b));
						return true;
					}

					@Override
					protected String elementToString(String element) {
						return I18n.format("biome.minecraft." + element);
					}
				};
			}
			
		};
		for (ResourceLocation location : Biome.REGISTRY.getKeys())
			this.biomes.list.add(location.getPath());
		this.biomes.list.sort();
		this.biomes.flex().relative(this).x(1.0f).w(180).h(1.0f).anchorX(1.0f);
		this.biomes.list.background(0x80000000);
		this.biomes.resize();
		this.biomes.list.scroll.scrollSpeed = 15;
		
		this.trans = new GuiTransformations(mc) {

			@Override
			public void setT(double x, double y, double z) {
				prop.translate.x = (float) x;
				prop.translate.y = (float) y;
				prop.translate.z = (float) z;
			}

			@Override
			public void setS(double x, double y, double z) {
				prop.scale.x = (float) x;
				prop.scale.y = (float) y;
				prop.scale.z = (float) z;
			}

			@Override
			public void setR(double x, double y, double z) {
				prop.rotate.x = (float) x;
				prop.rotate.y = (float) y;
				prop.rotate.z = (float) z;
			}
			
		};
		this.trans.flex().relative(this.area).x(0.5f, -95).y(1.0f, -10).wh(190, 70).anchorY(1.0f);
		
		this.add(this.anim, this.reload, this.topLevel, this.noNormal, this.standalone, this.custom, this.sky, this.block, this.curve, this.reset, this.biomes, this.trans);
	}
	
	@Override
	public void fillData(StructureMorph morph) {
		super.fillData(morph);
		
		this.prop = StructureMorphExtraManager.getExProps(morph);
		
		this.anim.fill(this.prop.anim);
		
		this.topLevel.toggled(this.prop.topLevel);
		this.noNormal.toggled(this.prop.fakeNormal);
		this.standalone.toggled(!this.prop.acceptLighting);
		this.custom.toggled(this.prop.custom);
		
		this.biomes.filter("", true);
		this.biomes.list.setCurrent(this.prop.biome);
		
		this.trans.fillT(prop.translate.x, prop.translate.y, prop.translate.z);
		this.trans.fillS(prop.scale.x, prop.scale.y, prop.scale.z);
		this.trans.fillR(prop.rotate.x, prop.rotate.y, prop.rotate.z);
		
		showCustomPanel(!this.prop.acceptLighting);
		setCurve(false);
	}

	public void toggle(GuiToggleElement element) {
		if (element == this.standalone) {
			this.prop.acceptLighting = !element.isToggled();
			showCustomPanel(element.isToggled());
		} else {
			this.prop.custom = element.isToggled();
			showCustomArgs(element.isToggled());
		}
	}
	
	public void setCurve(boolean isBlock) {
		if (isBlock) {
			this.sky.setEnabled(true);
			this.block.setEnabled(false);
			this.curve.setChannel(this.prop.blockCurve, 0xFFFF80);
		} else {
			this.sky.setEnabled(false);
			this.block.setEnabled(true);
			this.curve.setChannel(this.prop.skyCurve, 0x80FFFF);
		}
	}
	
	public void reset() {
		KeyframeChannel channel = this.curve.sheet.channel;
		channel.copy(new CurveKeyframeChannel(240));
	}
	
	private void showCustomPanel(boolean show) {
		if (show) {
			this.custom.setVisible(true);
			showCustomArgs(this.prop.custom);
		} else {
			this.custom.setVisible(false);
			showCustomArgs(false);
		}
	}
	
	private void showCustomArgs(boolean show) {
		this.sky.setVisible(show);
		this.block.setVisible(show);
		this.curve.setVisible(show);
		this.reset.setVisible(show);
	}

}
