package nyanli.hackersmorph.util;

import java.lang.reflect.Field;
import java.util.List;

import javax.vecmath.Vector3f;

import mchorse.blockbuster.api.formats.obj.ShapeKey;
import mchorse.blockbuster_pack.morphs.CustomMorph;
import mchorse.blockbuster_pack.morphs.ImageMorph;
import mchorse.blockbuster_pack.morphs.SequencerMorph;
import mchorse.blockbuster_pack.morphs.StructureMorph;
import mchorse.blockbuster_pack.morphs.ImageMorph.ImageAnimation;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.emoticons.c1825d609334c20b7;
import mchorse.emoticons.c3ef299d518504d1f;
import mchorse.emoticons.c6b4499a686f3e59b;
import mchorse.emoticons.c8a097fed29d47e38;
import mchorse.emoticons.cda04eccaf059893b;
import mchorse.mclib.utils.Interpolation;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.Animation;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.BodyPartManager;
import mchorse.metamorph.bodypart.IBodyPartProvider;
import net.minecraftforge.fml.common.Loader;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager.ExtraProps;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.StructureMorphExtraManager.TransAnimation;

public class MorphAnimUtils {
	
	private static Field fieldAnimation;
	private static boolean doBodyPartSupportAnim;
	
	static {
		try {
			fieldAnimation = SequencerMorph.class.getDeclaredField("animation");
			fieldAnimation.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			fieldAnimation = null;
		}
		try {
			BodyPart.class.getField("animate");
			doBodyPartSupportAnim = true;
		} catch (NoSuchFieldException | SecurityException e) {
			doBodyPartSupportAnim = false;
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
	
	public static void setProgress(AbstractMorph morph, int progress) {
//		if (tick <= 0) return;
		if (morph instanceof IAnimationProvider) {
			Animation anim = ((IAnimationProvider) morph).getAnimation();
			anim.progress = Math.min(progress, anim.duration);
		}
		if (morph instanceof IBodyPartProvider)
			for (BodyPart part : ((IBodyPartProvider) morph).getBodyPart().parts)
				if (!part.morph.isEmpty())
					setProgress(part.morph.get(), progress);
	}
	
	public static boolean canGenerateKeyframe(AbstractMorph morph) {
		return morph instanceof StructureMorph
				|| morph instanceof ImageMorph
				|| morph instanceof CustomMorph
				|| Loader.isModLoaded("chameleon_morph") && morph instanceof ChameleonMorph
				|| Loader.isModLoaded("emoticons") && morph instanceof cda04eccaf059893b;
	}
	
	public static AbstractMorph generateKeyframe(AbstractMorph morph, AbstractMorph previous, float time) {
		int progress = (int) time;
		time -= progress;
		AbstractMorph m = MorphUtils.copy(previous);
		if (m.canMerge(morph))
			return generateKeyframe(m, progress, time);
		return null;
	}
	
	private static AbstractMorph generateKeyframe(AbstractMorph morph, int progress, float partialTicks) {
		if (!canGenerateKeyframe(morph))
			return morph;

		Animation anim = ((IAnimationProvider) morph).getAnimation();
		if (anim == null)
			return morph;
		anim.progress = Math.min(anim.duration, progress);

		if (morph instanceof IBodyPartProvider) {
			BodyPartManager mgr = ((IBodyPartProvider) morph).getBodyPart();
			for (BodyPart part : mgr.parts) {
				part.morph.setDirect(generateKeyframe(part.morph.get(), progress, partialTicks));
				if (doBodyPartSupportAnim) {
					float tx = part.translate.x;
					float ty = part.translate.y;
					float tz = part.translate.z;
					float sx = part.scale.x;
					float sy = part.scale.y;
					float sz = part.scale.z;
					float rx = part.rotate.x;
					float ry = part.rotate.y;
					float rz = part.rotate.z;
					try {
						Field t = BodyPart.class.getDeclaredField("lastTranslate");
						t.setAccessible(true);
						Vector3f lastTranslate = (Vector3f) t.get(part);
						Field s = BodyPart.class.getDeclaredField("lastScale");
						s.setAccessible(true);
						Vector3f lastScale = (Vector3f) s.get(part);
						Field r = BodyPart.class.getDeclaredField("lastRotate");
						r.setAccessible(true);
						Vector3f lastRotate = (Vector3f) r.get(part);
						
						if (anim.isInProgress() && t.get(part) != null) {
							final Interpolation inter = anim.interp;
							float factor = anim.getFactor(partialTicks);
							tx = inter.interpolate(lastTranslate.x, tx, factor);
							ty = inter.interpolate(lastTranslate.y, ty, factor);
							tz = inter.interpolate(lastTranslate.z, tz, factor);
							sx = inter.interpolate(lastScale.x, sx, factor);
							sy = inter.interpolate(lastScale.y, sy, factor);
							sz = inter.interpolate(lastScale.z, sz, factor);
							rx = inter.interpolate(lastRotate.x, rx, factor);
							ry = inter.interpolate(lastRotate.y, ry, factor);
							rz = inter.interpolate(lastRotate.z, rz, factor);
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
					part.translate.x = tx;
					part.translate.y = ty;
					part.translate.z = tz;
					part.scale.x = sx;
					part.scale.y = sy;
					part.scale.z = sz;
					part.rotate.x = rx;
					part.rotate.y = ry;
					part.rotate.z = rz;
				}
			}
		}
		if (morph instanceof StructureMorph) {
			ExtraProps prop = StructureMorphExtraManager.getExProps((StructureMorph) morph);
			TransAnimation a = (TransAnimation) anim;
			a.calcTSR(prop, partialTicks);
			prop.translate.set(a.translate);
			prop.scale.set(a.scale);
			prop.rotate.set(a.rotate);
		} else if (morph instanceof ImageMorph) {
			ImageMorph m = (ImageMorph) morph;
			ImageAnimation a = (ImageAnimation) anim;
			m.image.from(m);
			a.apply(m.image, partialTicks);
			m.color = m.image.color.getRGBAColor();
			m.crop.set(m.image.crop);
			m.pose.copy(m.image.pose);
			m.offsetX = m.image.x;
			m.offsetY = m.image.y;
			m.rotation = m.image.rotation;
		} else if (morph instanceof CustomMorph) {
			CustomMorph m = (CustomMorph) morph;
			mchorse.blockbuster_pack.morphs.CustomMorph.PoseAnimation a = (mchorse.blockbuster_pack.morphs.CustomMorph.PoseAnimation)anim;
			m.customPose = a.calculatePose(m.getCurrentPose(), partialTicks);
			try {
				CustomMorph.class.getDeclaredField("shapes");
				List<ShapeKey> list = m.getShapes();
				List<ShapeKey> shapes = a.calculateShapes(m, partialTicks);
				list.clear();
				list.addAll(shapes);
			} catch (NoSuchFieldException | SecurityException e) {}
		} else if (Loader.isModLoaded("chameleon_morph") && morph instanceof ChameleonMorph) {
			ChameleonMorph m = (ChameleonMorph) morph;
			mchorse.chameleon.metamorph.pose.PoseAnimation a = (mchorse.chameleon.metamorph.pose.PoseAnimation) anim;
			m.pose = a.calculatePose(m.pose, m.getModel(), partialTicks);
		} else if (Loader.isModLoaded("emoticons") && morph instanceof cda04eccaf059893b) {
			cda04eccaf059893b m = (cda04eccaf059893b) morph;
			c6b4499a686f3e59b a = (c6b4499a686f3e59b) anim;
			m.initiateAnimator();
			c3ef299d518504d1f animator = m.animator;
			c1825d609334c20b7 v41b40a16252b1ada = m.animator.fff5d0891fef069fa.f532f94916d3b94ca.get(0);
			c8a097fed29d47e38 v1fa135d16e70cf9f = v41b40a16252b1ada.m4ce50c01ed7bad52();
			c8a097fed29d47e38 vcab450e9a4d60719 = animator.fa7e67f4982acef4d.m865f84019893d7ef(v1fa135d16e70cf9f);
			m.pose = a.calculatePose(m.pose, vcab450e9a4d60719, partialTicks);
		}
		if (progress + partialTicks < anim.duration)
			anim.duration = progress;
		return MorphUtils.copy(morph);
	}
	
}
