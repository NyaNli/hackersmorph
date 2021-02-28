package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;
import org.lwjgl.opengl.GL11;

import mchorse.blockbuster_pack.morphs.SequencerMorph;
import mchorse.blockbuster_pack.morphs.SequencerMorph.FoundMorph;
import mchorse.blockbuster_pack.morphs.SequencerMorph.SequenceEntry;
import mchorse.emoticons.cda04eccaf059893b;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MathUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Loader;
import nyanli.hackersmorph.util.MorphAnimUtils;

public class SequencerMorphManager {
	
	private static Random random = new Random();
	private static boolean supportEmoticons = false;
	
	static {
		if (Loader.isModLoaded("emoticons")) {
			ClassLoader cl = SequencerMorphManager.class.getClassLoader();
			if (cl instanceof LaunchClassLoader) {
				try {
					supportEmoticons = ((LaunchClassLoader) cl).getClassBytes("mchorse.emoticons.cda04eccaf059893b") != null;
				} catch (IOException e1) {}
			}
		}
	}

	public static void fromNBT(SequencerMorph self, NBTTagCompound nbt) {
		if (nbt.hasKey("Extra", 10))
			getExProps(self).deserializeNBT(nbt.getCompoundTag("Extra"));
	}
	
	public static void toNBT(SequencerMorph self, NBTTagCompound nbt) {
		nbt.setTag("Extra", getExProps(self).serializeNBT());
	}
	
	public static void copy(SequencerMorph self, AbstractMorph from) {
		if (from instanceof SequencerMorph)
			getExProps(self).copy(getExProps((SequencerMorph)from));
	}
	
	public static boolean equalsObj(boolean last, SequencerMorph a, Object b) {
		return last && b instanceof SequencerMorph && getExProps(a).equals(getExProps((SequencerMorph)b));
	}
	
	public static void render(AbstractMorph renderMorph, EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, SequencerMorph morph) {
		ExtraProps prop = getExProps(morph);
		int times = 0;
		boolean doOffset = false;
		double prevX = entity.prevPosX;
		double prevY = entity.prevPosY;
		double prevZ = entity.prevPosZ;
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;
		if (prop.repeatTimes > 0 && (times = getRepeatTimes(morph)) > 0) {
			doOffset = true;
			times %= prop.repeatTimes + 1;
			Vec3d offset = new Vec3d(prop.offsetX * 0.0625 * times, prop.offsetY * 0.0625 * times, prop.offsetZ * 0.0625 * times);
			float yaw = Interpolations.lerpYaw(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
			offset = offset.rotateYaw((float) Math.toRadians(-yaw));
//			GL11.glPushMatrix();
//			GL11.glTranslated(offset.x, offset.y, offset.z);
			x += offset.x;
			y += offset.y;
			z += offset.z;
			entity.prevPosX += offset.x;
			entity.prevPosY += offset.y;
			entity.prevPosZ += offset.z;
			entity.posX += offset.x;
			entity.posY += offset.y;
			entity.posZ += offset.z;
		}
		// update emoticons texture
		if (supportEmoticons && renderMorph instanceof cda04eccaf059893b) {
			cda04eccaf059893b emtMorph = (cda04eccaf059893b) renderMorph;
			if (emtMorph.userConfigChanged && emtMorph.animator != null) {
				emtMorph.updateAnimator();
				emtMorph.userConfigChanged = false;
			}
		}
		if (renderMorph != null && morph.current < morph.morphs.size() && morph.current >= 0) {
			SequenceEntry entry = morph.morphs.get(morph.current);
			if (entry != null && entry.setDuration) {
				float duration = getEntryDuration(morph);
				MorphAnimUtils.setDuration(renderMorph, duration);
			}
		}
		renderMorph.render(entity, x, y, z, entityYaw, partialTicks);
		if (doOffset) {
//			GL11.glPopMatrix();
			entity.prevPosX = prevX;
			entity.prevPosY = prevY;
			entity.prevPosZ = prevZ;
			entity.posX = posX;
			entity.posY = posY;
			entity.posZ = posZ;
		}
	}
	
	public static void pause(SequencerMorph morph, int offset) {
		FoundMorph found = morph.getMorphAt(offset);
		if (found == null)
			return;
		SequencerMorph.SequenceEntry entry = found.current;
		for (int i = 0; i < morph.morphs.size(); i++) {
			if (morph.morphs.get(i) == entry) {
				morph.current = i;
				morph.timer = offset;
				morph.duration = found.totalDuration;
				return;
			}
		}
	}
	
	public static ExtraProps getExProps(SequencerMorph morph) {
		// It do not support FastAsyncWorldEdit, i think maybe it called clone.
//		Side threadSide = FMLCommonHandler.instance().getEffectiveSide();
//		if (!extraPropMap.containsKey(threadSide))
//			extraPropMap.put(threadSide, new WeakHashMap<>());
//		if (!extraPropMap.get(threadSide).containsKey(morph))
//			extraPropMap.get(threadSide).put(morph, new ExtraProps());
//		return extraPropMap.get(threadSide).get(morph);
		try {
			Field f = morph.getClass().getField("asmExtraProps");
			Object o = f.get(morph);
			if (o == null) {
				o = new ExtraProps();
				f.set(morph, o);
			}
			return (ExtraProps) o;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return null; // Dead code
		}
	}
	
	public static class ExtraProps implements INBTSerializable<NBTTagCompound> {
		
		public int repeatTimes = 0;
		
		public double offsetX = 0;
		public double offsetY = 0;
		public double offsetZ = 0;
		
		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("repeatTimes", this.repeatTimes);
			nbt.setDouble("offsetX", this.offsetX);
			nbt.setDouble("offsetY", this.offsetY);
			nbt.setDouble("offsetZ", this.offsetZ);
			return nbt;
		}
		
		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			this.repeatTimes = nbt.getInteger("repeatTimes");
			this.offsetX = nbt.getDouble("offsetX");
			this.offsetY = nbt.getDouble("offsetY");
			this.offsetZ = nbt.getDouble("offsetZ");
		}
		
		public boolean equals(Object o) {
			ExtraProps p = (ExtraProps) o;
			if (o != null && o instanceof ExtraProps)
				return this.repeatTimes == p.repeatTimes
					&& this.offsetX == p.offsetX
					&& this.offsetY == p.offsetY
					&& this.offsetZ == p.offsetZ;
			return false;
		}
		
		public void copy(ExtraProps prop) {
			this.repeatTimes = prop.repeatTimes;
			this.offsetX = prop.offsetX;
			this.offsetY = prop.offsetY;
			this.offsetZ = prop.offsetZ;
		}
		
	}
	
	public static int getRepeatTimes(SequencerMorph morph) {
		if (morph.morphs.size() == 0 || morph.isRandom || getMaxDuration(morph) <= 0.0001)
			return 0;
		int times = 0;
		int i = morph.reverse ? (morph.morphs.size() - 1) : 0;
		float duration = morph.morphs.get(i).getDuration(getRandomSeed(0f));
		for (; duration < morph.duration; 
				duration += morph.morphs.get(i).getDuration(getRandomSeed(duration))) {
			i += morph.reverse ? -1 : 1;
			if (i < 0 || i > morph.morphs.size() - 1) {
				times++;
				i = MathUtils.cycler(i, 0, morph.morphs.size() - 1);
			}
		}
		return times;
	}
	
	public static float calcDuration(SequencerMorph morph, int index) {
		if (morph.isRandom || index < 0 || index > morph.morphs.size() - 1)
			return morph.getDuration();
		int i = morph.reverse ? (morph.morphs.size() - 1) : 0;
		float duration = morph.morphs.get(i).getDuration(getRandomSeed(0f));
		while (i != index) {
			i = MathUtils.cycler(i + (morph.reverse ? -1 : 1), 0, morph.morphs.size() - 1);
			duration += morph.morphs.get(i).getDuration(getRandomSeed(duration));
		}
		return duration;
	}
	
	public static float getEntryDuration(SequencerMorph morph) {
		if (morph.morphs.size() == 0 || getMaxDuration(morph) <= 0.0001)
			return 0;
		int i = morph.isRandom ? morph.getRandomIndex(0f) : (morph.reverse ? (morph.morphs.size() - 1) : 0);
		float duration = morph.morphs.get(i).getDuration(getRandomSeed(0f));
		float lastDuration = duration;
		while (duration < morph.duration) {
			i = morph.isRandom ? morph.getRandomIndex(duration) : MathUtils.cycler(i + (morph.reverse ? -1 : 1), 0, morph.morphs.size() - 1);
			lastDuration = morph.morphs.get(i).getDuration(getRandomSeed(duration));
			duration += lastDuration;
		}
		return lastDuration;
	}
	
	public static float getMaxDuration(SequencerMorph morph) {
		float duration = 0;
		for (SequenceEntry entry : morph.morphs)
			duration += entry.duration + Math.max(entry.random, 0);
		return duration;
	}
	
	// Blockbuster 2.1--2.2
	public static Random getRandomSeed(float duration) {
		random.setSeed((long) (duration * 100000L));
		return random;
	}

}
