package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui;

import java.util.ArrayList;
import java.util.List;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.GuiSnowstorm;
import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.utils.mclib.BBIcons;
import mchorse.blockbuster_pack.client.gui.GuiSnowstormMorph;
import mchorse.blockbuster_pack.morphs.SnowstormMorph;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.utils.Label;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel.GuiSnowstormEditorPanel;

// 暴雪粒子GUI
public class GuiSnowstormMorphExtra extends GuiSnowstormMorph {
	
	private GuiSnowstorm proxy;

	public GuiSnowstormMorphExtra(Minecraft mc) {
		super(mc);
		
		this.defaultPanel = new GuiSnowstormEditorPanel(mc, this);
		this.registerPanel(this.defaultPanel, IKey.lang("blockbuster.gui.snowstorm.title"), BBIcons.EDITOR);
	}

//	@Override
//	public List<Label<NBTTagCompound>> getPresets(SnowstormMorph arg0) {
//		final List<Label<NBTTagCompound>> labels = new ArrayList<Label<NBTTagCompound>>();
//		for (final String preset : Blockbuster.proxy.particles.presets.keySet()) {
//			final NBTTagCompound tag = new NBTTagCompound();
//			tag.setString("Data", BedrockScheme.JSON_PARSER.toJson(Blockbuster.proxy.particles.presets.get(preset)));
//			this.addPreset(morph, labels, preset, tag);
//		}
//		return labels;
//	}
	
	@Override
	protected GuiModelRenderer createMorphRenderer(Minecraft mc) {
		return getProxy().renderer;
	}

	public GuiSnowstorm getProxy() {
		if (this.proxy == null) {
			this.proxy = new GuiSnowstorm(mc, null);
		}
		return this.proxy;
	}

}
