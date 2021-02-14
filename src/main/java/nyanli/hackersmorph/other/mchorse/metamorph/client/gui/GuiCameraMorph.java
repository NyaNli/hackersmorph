package nyanli.hackersmorph.other.mchorse.metamorph.client.gui;

import mchorse.blockbuster.utils.mclib.BBIcons;
import mchorse.blockbuster_pack.morphs.StructureMorph;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import nyanli.hackersmorph.other.mchorse.metamorph.common.morph.CameraMorph;

// 相机伪装Gui
public class GuiCameraMorph extends GuiAbstractMorph<StructureMorph> {

	public GuiCameraMorph(Minecraft mc) {
		super(mc);
		this.defaultPanel = new GuiCameraMorphPanel(mc, this);
		this.registerPanel(this.defaultPanel, IKey.lang("morph.category.blockbuster_structures"), BBIcons.EDITOR);
	}
	
	@Override
	public boolean canEdit(AbstractMorph morph) {
		return morph instanceof CameraMorph;
	}
	
	private static class GuiCameraMorphPanel extends GuiMorphPanel<CameraMorph, GuiCameraMorph> {

		private GuiTextElement text;
		
		public GuiCameraMorphPanel(Minecraft mc, GuiCameraMorph editor) {
			super(mc, editor);
			this.text = new GuiTextElement(mc, str -> this.morph.setLabel(str));
			this.text.flex().relative(this).w(100).x(0.5f).y(1f, -20).anchorX(0.5f).anchorY(1);
			this.text.tooltip(IKey.lang("hackersmorph.morph.cameramorph.label.tooltip"));
			
			this.add(text);
		}

		@Override
		public void fillData(CameraMorph morph) {
			super.fillData(morph);
			if (morph.getLabel() == null || morph.getLabel().isEmpty())
				morph.setLabel("Camera" + Minecraft.getSystemTime() % 1000L);
			this.text.setText(morph.getLabel());
		}
		
	}

}