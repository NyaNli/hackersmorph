package nyanli.hackersmorph.other.mchorse.metamorph.common;

import mchorse.metamorph.api.creative.categories.MorphCategory;
import mchorse.metamorph.api.creative.sections.MorphSection;
import net.minecraft.world.World;
import nyanli.hackersmorph.other.mchorse.metamorph.common.morph.CameraMorph;

public class HackersMorphSection extends MorphSection {

	private MorphCategory category;
	
	public HackersMorphSection(String title) {
		super(title);
		
		this.category = new MorphCategory(this, "hackersmorph.aperturehelper");
		category.add(new CameraMorph());
	}

	@Override
	public void update(World world) {
		super.update(world);
		this.categories.clear();
		this.add(category);
	}

}
