package nyanli.hackersmorph.util;

import java.lang.reflect.Field;

import mchorse.blockbuster_pack.morphs.SequencerMorph;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.Animation;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.IBodyPartProvider;

public class MorphAnimUtils {
	
	private static Field fieldAnimation;
	
	static {
		try {
			fieldAnimation = SequencerMorph.class.getDeclaredField("animation");
			fieldAnimation.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			fieldAnimation = null;
		}
	}
	
	public static void unpauseAll(AbstractMorph morph) {
		if (morph instanceof IAnimationProvider)
			((IAnimationProvider) morph).getAnimation().paused = false;
		if (morph instanceof SequencerMorph && fieldAnimation != null) {
			try {
				Animation anim = (Animation) fieldAnimation.get(morph);
				anim.paused = false;
				if (!((SequencerMorph) morph).currentMorph.isEmpty())
					unpauseAll(((SequencerMorph) morph).currentMorph.get());
			} catch (IllegalArgumentException | IllegalAccessException e) {}
		}
		if (morph instanceof IBodyPartProvider)
			for (BodyPart part : ((IBodyPartProvider) morph).getBodyPart().parts)
				if (!part.morph.isEmpty())
					unpauseAll(part.morph.get()); // No overflow plz
	}
	
	public static void updateTick(AbstractMorph morph, int tick) {
//		if (tick <= 0) return;
		if (morph instanceof IAnimationProvider) {
			Animation anim = ((IAnimationProvider) morph).getAnimation();
			if (anim.animates && !anim.paused)
				anim.progress += tick;
		}
		if (morph instanceof SequencerMorph) {
			((SequencerMorph) morph).timer += tick;
			if (!((SequencerMorph) morph).currentMorph.isEmpty())
				updateTick(((SequencerMorph) morph).currentMorph.get(), tick);
		}
		if (morph instanceof IBodyPartProvider)
			for (BodyPart part : ((IBodyPartProvider) morph).getBodyPart().parts)
				if (!part.morph.isEmpty())
					updateTick(part.morph.get(), tick);
	}
	
	public static void setDuration(AbstractMorph morph, float duration) {
//		if (tick <= 0) return;
		if (morph instanceof IAnimationProvider) {
			Animation anim = ((IAnimationProvider) morph).getAnimation();
			anim.duration = (int) duration;
		}
		if (morph instanceof IBodyPartProvider)
			for (BodyPart part : ((IBodyPartProvider) morph).getBodyPart().parts)
				if (!part.morph.isEmpty())
					setDuration(part.morph.get(), duration);
	}
	
}
