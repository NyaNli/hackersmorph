package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui;

import mchorse.blockbuster.utils.mclib.BBIcons;
import mchorse.blockbuster_pack.morphs.StructureMorph;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import net.minecraft.client.Minecraft;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel.GuiStructureMorphPanel;

// 结构伪装GUI
public class GuiStructureMorph extends GuiAbstractMorph<StructureMorph> {

	public GuiStructureMorph(Minecraft mc) {
		super(mc);
		this.defaultPanel = new GuiStructureMorphPanel(mc, this);
		this.registerPanel(this.defaultPanel, IKey.lang("morph.category.blockbuster_structures"), BBIcons.EDITOR);
	}
	
	@Override
	public boolean canEdit(AbstractMorph morph) {
		return morph instanceof StructureMorph;
	}

}
