package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.client.particles.emitter.BedrockEmitter;
import mchorse.blockbuster_pack.morphs.SnowstormMorph;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.nbt.NBTTagCompound;
import nyanli.hackersmorph.HackersMorph;

public class SnowstormMorphExtraManager {
	
	private static BedrockScheme defaultScheme;
	private static Field emitter;
	
	static {
		try {
			emitter = SnowstormMorph.class.getDeclaredField("emitter");
			emitter.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			HackersMorph.throwCommonException(e);
		}
	}
	
	public static void fromNBT(SnowstormMorph self, NBTTagCompound tag) {
		self.scheme = "default_rain";
		if (tag.hasKey("Scheme"))
			tag.removeTag("Scheme");
		if (tag.hasKey("Data")) {
			BedrockScheme scheme = BedrockScheme.parse(tag.getString("Data"));
			updateScheme(self, scheme);
		}
	}
	
	public static void toNBT(SnowstormMorph self, NBTTagCompound tag) {
		tag.setString("Scheme", "default_rain");
		if (self.getEmitter().scheme != null)
			tag.setString("Data", BedrockScheme.JSON_PARSER.toJson(self.getEmitter().scheme));
	}
	
	public static void copy(SnowstormMorph self, SnowstormMorph from) {
		updateScheme(self, BedrockScheme.dupe(from.getEmitter().scheme));
	}
	
	public static boolean equalsObj(boolean last, SnowstormMorph a, Object b) {
		return last && b instanceof SnowstormMorph && 
				BedrockScheme.toJson(a.getEmitter().scheme).equals(
						BedrockScheme.toJson(((SnowstormMorph)b).getEmitter().scheme)
						);
	}
	
	public static boolean merge(SnowstormMorph self, AbstractMorph from) {
		updateScheme(self, BedrockScheme.dupe(((SnowstormMorph) from).getEmitter().scheme));
		return true;
	}
	
	public static BedrockScheme getScheme(Map<String, BedrockScheme> presets, String name) {
		return Blockbuster.proxy.particles.factory.get("default_rain");
	}
	
	private static void updateScheme(SnowstormMorph self, BedrockScheme scheme) {
		self.getEmitter().running = false;
		self.getLastEmitters().add(self.getEmitter());
		try {
			emitter.set(self, new BedrockEmitter());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Set Field \"emitter\" failed. please remove this mod.", e);
		}
		self.getEmitter().setScheme(scheme);
	}

}
