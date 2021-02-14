package nyanli.hackersmorph.other.mchorse.metamorph.common;

import java.util.List;

import mchorse.metamorph.api.IMorphFactory;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.creative.sections.MorphSection;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import nyanli.hackersmorph.other.mchorse.metamorph.client.gui.GuiCameraMorph;
import nyanli.hackersmorph.other.mchorse.metamorph.common.morph.CameraMorph;

// HackersMorph
public class HackersMorphFactory implements IMorphFactory {
	
	private MorphSection section;

	@Override
	public void register(MorphManager mgr) {
		this.section = new HackersMorphSection("hackersmorph");
		mgr.list.register(this.section);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerMorphEditors(Minecraft mc, List<GuiAbstractMorph> editors) {
		editors.add(new GuiCameraMorph(mc));
	}

	@Override
	public boolean hasMorph(String name) {
		return "cameramorph".equals(name);
	}

	@Override
	public AbstractMorph getMorphFromNBT(NBTTagCompound nbt) {
		String name = nbt.getString("Name");
		AbstractMorph morph = null;
		if ("cameramorph".equals(name))
			morph = new CameraMorph();
		if (morph != null)
			morph.fromNBT(nbt);
		return morph;
	}

}
