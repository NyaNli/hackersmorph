package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.lang.reflect.Field;
import java.util.Map;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.particles.BedrockLibrary;
import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.client.particles.emitter.BedrockEmitter;
import mchorse.blockbuster_pack.morphs.SnowstormMorph;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.nbt.NBTTagCompound;
import nyanli.hackersmorph.HackersMorph;

public class SnowstormMorphExtraManager {

	private static Field asmStandalone;
	private static Field emitter;
	
	static {
		try {
			asmStandalone = SnowstormMorph.class.getField("asmStandalone");
			emitter = SnowstormMorph.class.getDeclaredField("emitter");
			emitter.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			HackersMorph.throwCommonException(e);
		}
	}
	
	public static void fromNBT(SnowstormMorph self, NBTTagCompound tag) {
		if (tag.getBoolean("Standalone") && tag.hasKey("Data")) {
			setStandalone(self, true);
			self.scheme = "default_rain";
			if (tag.hasKey("Scheme"))
				tag.removeTag("Scheme");
			if (tag.hasKey("Data")) {
				BedrockScheme scheme = BedrockScheme.parse(tag.getString("Data"));
				updateScheme(self, scheme);
			}
		} else
			setStandalone(self, false);
	}
	
	public static void toNBT(SnowstormMorph self, NBTTagCompound tag) {
		tag.setBoolean("Standalone", getStandalone(self));
		if (getStandalone(self)) {
			tag.setString("Scheme", "default_rain");
			if (self.getEmitter().scheme != null)
				tag.setString("Data", BedrockScheme.JSON_PARSER.toJson(self.getEmitter().scheme));
		}
	}
	
	public static void copy(SnowstormMorph self, SnowstormMorph from) {
		setStandalone(self, getStandalone(from));
		if (getStandalone(self))
			updateScheme(self, BedrockScheme.dupe(from.getEmitter().scheme));
		else
			self.setScheme(from.scheme);
	}
	
	public static boolean equalsObj(boolean last, SnowstormMorph a, Object b) {
		boolean check1 = b instanceof SnowstormMorph && getStandalone(a) == getStandalone((SnowstormMorph) b);
		if (last && check1 && getStandalone(a))
			return BedrockScheme.toJson(a.getEmitter().scheme).equals(
					BedrockScheme.toJson(((SnowstormMorph)b).getEmitter().scheme)
					);
		return last && check1;
	}
	
	public static boolean merge(SnowstormMorph self, AbstractMorph from) {
		if (getStandalone((SnowstormMorph) from)) {
			updateScheme(self, ((SnowstormMorph) from).getEmitter().scheme);
			setStandalone(self, true);
			return true;
		} else if (getStandalone(self)) {
			setStandalone(self, false);
			return false;
		} else
			return self.scheme.equals(((SnowstormMorph)from).scheme);
	}
	
//	public static BedrockScheme getScheme(Map<String, BedrockScheme> presets, String name) {
//		return Blockbuster.proxy.particles.factory.get("default_rain");
//	}
	
	public static long getLastUpdate(SnowstormMorph self, long lastUpdate) {
		return getStandalone(self) ? BedrockLibrary.lastUpdate : lastUpdate;
	}
	
	public static boolean getStandalone(SnowstormMorph self) {
		try {
			return asmStandalone.getBoolean(self);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return false;
		}
	}
	
	public static void setStandalone(SnowstormMorph self, boolean value) {
		try {
			asmStandalone.setBoolean(self, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
	
	public static void updateScheme(SnowstormMorph self, BedrockScheme scheme) {
		self.getEmitter().running = false;
		self.getLastEmitters().add(self.getEmitter());
		try {
			emitter.set(self, new BedrockEmitter());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Set Field \"emitter\" failed. please remove this mod.", e);
		}
		self.getEmitter().setScheme(scheme);
		self.getEmitter().parseVariables(self.variables);
	}

}
