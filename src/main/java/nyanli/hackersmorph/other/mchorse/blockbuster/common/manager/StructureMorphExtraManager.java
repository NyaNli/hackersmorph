package nyanli.hackersmorph.other.mchorse.blockbuster.common.manager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.client.ClientHandlerStructure.FakeWorld;
import mchorse.blockbuster.network.common.structure.PacketStructureRequest;
import mchorse.blockbuster_pack.morphs.StructureMorph;
import mchorse.blockbuster_pack.morphs.StructureMorph.StructureRenderer;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.NBTUtils;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import mchorse.metamorph.api.models.IMorphProvider;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.Animation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.util.MatrixTools;

// 存储结构伪装的生物群系NBT，以及光照修正
public class StructureMorphExtraManager {

	private static HashMap<String, NBTTagCompound> structures = new HashMap<>();
	private static HashSet<String> waiting = new HashSet<>();
	private static HashMap<String, StructureVertexRenderer> renderers = new HashMap<>();
	
	public static void fromNBT(StructureMorph self, NBTTagCompound nbt) {
		ExtraProps prop = getExProps(self);
		if (nbt.hasKey("Extra", 10))
			prop.deserializeNBT(nbt.getCompoundTag("Extra"));
		if (nbt.hasKey("Lighting"))
			nbt.removeTag("Lighting");
	}
	
	public static void toNBT(StructureMorph self, NBTTagCompound nbt) {
		try {
			self.getClass().getField("lighting").setBoolean(self, false);
		} catch (Exception e) {}
		ExtraProps prop = getExProps(self);
		nbt.setTag("Extra", prop.serializeNBT());
	}
	
	public static StructureMorph copy(StructureMorph from, StructureMorph to) {
		ExtraProps fromProp = getExProps(from);
		ExtraProps toProp = getExProps(to);
		toProp.copy(fromProp);
		return from;
	}
	
	public static boolean equalsObj(boolean last, StructureMorph a, Object b) {
		return last && b instanceof StructureMorph && getExProps(a).equals(getExProps((StructureMorph) b));
	}
	
	@SideOnly(Side.CLIENT)
	public static Object getRenderer(Map<String, StructureRenderer> map, String structure, StructureMorph morph) {
		if (structures.get(structure) == null) {
			if (!waiting.contains(structure)) {
				Dispatcher.sendToServer(new PacketStructureRequest(structure));
				waiting.add(structure);
			}
			return StructureLoadingRenderer.getRenderer(structure, false);
		}
		ExtraProps prop = getExProps(morph);
		String key = structure + '>' + prop.biome;
		if (renderers.get(key) == null) {
			StructureVertexRenderer renderer = generateRenderer(structure, prop.biome);
			if (renderer == null || renderer == StructureVertexRenderer.EMPTY)
				return StructureLoadingRenderer.getRenderer(structure, renderer == StructureVertexRenderer.EMPTY);
			renderers.put(key, renderer);
		}
		return renderers.get(key).props(prop);
	}
	
	@SideOnly(Side.CLIENT)
	public static void removeRenderer(StructureMorph morph) {
		String structure = morph.structure;
		TemplateLoader.removeTemplate(structures.remove(structure));
		Iterator<Entry<String, StructureVertexRenderer>> iterator = renderers.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, StructureVertexRenderer> entry = iterator.next();
			if (entry.getKey().startsWith(structure + ">") && entry.getValue() != null) {
				entry.getValue().delete();
				iterator.remove();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void updateRenderer(String structure, NBTTagCompound tag) {
		waiting.remove(structure);
		NBTTagCompound old = structures.put(structure, tag);
		TemplateLoader.addTemplate(tag, old);
		Iterator<Entry<String, StructureVertexRenderer>> iterator = renderers.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, StructureVertexRenderer> entry = iterator.next();
			if (entry.getKey().startsWith(structure + ">") && entry.getValue() != null) {
				entry.getValue().delete();
				iterator.remove();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void clearAll() {
		waiting.clear();
		TemplateLoader.removeAll();
		structures.clear();
		renderers.clear();
	}
	
	public static ExtraProps getExProps(StructureMorph morph) {
		// It do not support FastAsyncWorldEdit, i think maybe it called clone.
//		Side threadSide = FMLCommonHandler.instance().getEffectiveSide();
//		if (!extraPropMap.containsKey(threadSide))
//			extraPropMap.put(threadSide, new HashMap<>());
//		if (!extraPropMap.get(threadSide).containsKey(morph)) {
//			HackersMorph.LOGGER.debug("New ? {} {}", morph, Thread.currentThread());
//			extraPropMap.get(threadSide).put(morph, new ExtraProps());
//		}
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
	
	public static void beforeRender(StructureMorph morph, EntityLivingBase entity, float partialTicks) {
		ExtraProps prop = getExProps(morph);
		prop.anim.calcTSR(prop, partialTicks);
		float entityYaw = Interpolations.lerpYaw(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
		if (renderers.get(morph.structure + '>' + prop.biome) == null)
			entityYaw = 180;
		GL11.glRotatef(180f - entityYaw, 0, 1, 0);
	}
	
	// Animation
	
	public static void update(StructureMorph self, EntityLivingBase target) {
		if (target.world.isRemote) {
			ExtraProps prop = getExProps(self);
			prop.anim.update();
			prop.anim.calcTSR(prop, 0);
		}
	}
	
	public static boolean canMerge(StructureMorph self, AbstractMorph morph) {
		if (morph instanceof StructureMorph) {
			try {
				Method m = AbstractMorph.class.getDeclaredMethod("mergeBasic", AbstractMorph.class);
				m.setAccessible(true);
				m.invoke(self, morph);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
			
			ExtraProps prop = getExProps(self);
//			ExtraProps prop2 = getExProps((StructureMorph) morph);
			TransAnimation anim = prop.anim;
//			if (!prop2.anim.ignored) {
//			anim.calcTSR(prop, 0);
//			Vector3f lastTranslate = new Vector3f(anim.translate);
//			Vector3f lastScale = new Vector3f(anim.scale);
//			Vector3f lastRotate = new Vector3f(anim.rotate);
			
			// As same as emoticon animation
			Vector3f lastTranslate = new Vector3f(prop.translate);
			Vector3f lastScale = new Vector3f(prop.scale);
			Vector3f lastRotate = new Vector3f(prop.rotate);
//			}
			self.copy(morph);
			anim.progress = 0;
//			if (!anim.ignored) {
			anim.lastTranslate.set(lastTranslate);
			anim.lastScale.set(lastScale);
			anim.lastRotate.set(lastRotate);
//			}
			return true;
		}
		return false;
	}
	
	public static boolean isPause(StructureMorph self) {
		return getExProps(self).anim.paused;
	}
	
	public static void pause(StructureMorph self, AbstractMorph previous, int offset) {
		ExtraProps prop = getExProps(self);
		prop.anim.pause(offset);
		if (previous instanceof IMorphProvider) {
			previous = ((IMorphProvider) previous).getMorph();
		}
		if (previous instanceof StructureMorph) {
			ExtraProps pp = getExProps((StructureMorph) previous);
//			pp.anim.calcTSR(pp, 0);
//			prop.anim.lastTranslate.set(pp.anim.translate);
//			prop.anim.lastScale.set(pp.anim.scale);
//			prop.anim.lastRotate.set(pp.anim.rotate);
			prop.anim.lastTranslate.set(pp.translate);
			prop.anim.lastScale.set(pp.scale);
			prop.anim.lastRotate.set(pp.rotate);
		}
	}
	
	public static Animation getAnimation(StructureMorph self) {
		return getExProps(self).anim;
	}

    /**
     * Reference from Blockbuster Mod
	 * Url: https://github.com/mchorse/blockbuster
	 * Author: mchorse
	 * License: MIT
	 * 
	 * mchorse.blockbuster.network.client.ClientHandlerStructure.createListFromTemplate(PacketStructure)
	 */
	private static StructureVertexRenderer generateRenderer(String structure, String biome) {
		String key = structure + '>' + biome;
		if (renderers.containsKey(key))
			renderers.get(key).delete();

		TemplateLoader world = TemplateLoader.getInstance();
		BlockPos size = world.loadTemplate(structures.get(structure));
		if (size == null)
			return null;
		if (size.equals(BlockPos.ORIGIN))
			return StructureVertexRenderer.EMPTY;
		
		BlockPos origin = TemplateLoader.origin;
		world.setBiome(biome);

        List<TileEntity> tes = new ArrayList<>();
        tes.addAll(world.loadedTileEntityList);
        Map<TileEntity, Integer> teMap = tes.isEmpty() ? null : new LinkedHashMap<>(); // Linked for sort, maybe unnecessary.

        for (TileEntity te : tes) {
        	int light = world.getCombinedLight(te.getPos(), 0);
        	teMap.put(te, light);
        }
        
        List<Entity> es = new ArrayList<>();
        es.addAll(world.loadedEntityList);
        Map<Entity, Integer> eMap = es.isEmpty() ? null : new LinkedHashMap<>(); // Linked for sort, maybe unnecessary.
        
        for (Entity e : es)
        	eMap.put(e, e.getBrightnessForRender());

        /* Create display list */
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        /* Centerize the geometry */
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-size.getX() / 2F, -64, -size.getZ() / 2F);
        
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(origin, origin.add(size)))
        {
        	IBlockState state = world.getBlockState(pos);
        	if (state.isFullCube())
        		dispatcher.renderBlock(world.getBlockState(pos), pos, world, buffer);
        }
        
        buffer.setTranslation(0, 0, 0);
        buffer.finishDrawing();
        
        int[] fullcubes = new int[buffer.getVertexCount() * buffer.getVertexFormat().getIntegerSize()];
        buffer.getByteBuffer().asIntBuffer().get(fullcubes);
        buffer.reset();
        
        /* Centerize the geometry */
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-size.getX() / 2F, -64, -size.getZ() / 2F);
        
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(origin, origin.add(size)))
        {
        	IBlockState state = world.getBlockState(pos);
        	if (!state.isFullCube())
        		dispatcher.renderBlock(world.getBlockState(pos), pos, world, buffer);
        }
        
        buffer.setTranslation(0, 0, 0);
        buffer.finishDrawing();
        
        StructureVertexRenderer renderer = new StructureVertexRenderer(buffer, fullcubes, size, teMap, eMap);
        buffer.reset();
        return renderer;
	}
	
//	@SideOnly(Side.CLIENT)
//	private static int[] processVertexes(BufferBuilder buffer) {
//		int count = buffer.getVertexCount();
//		VertexFormat format = buffer.getVertexFormat();
//		ByteBuffer bytes = buffer.getByteBuffer();
//		int inputSize = format.getSize(); // bytes
//		int outputSize = 7; // 7 * 4 = 28 bytes
//		int vertexPos = -1; // Maybe it is always 0?
//		int colorPos = format.getColorOffset();
//		int texPos = format.getUvOffsetById(0);
//		int lightPos = format.getUvOffsetById(1);
//		for (int i = 0; i < format.getElementCount(); i++) {
//			VertexFormatElement element = format.getElement(i);
//			if (element.equals(DefaultVertexFormats.POSITION_3F)) {
//				vertexPos = format.getOffset(i);
//				break;
//			}
//		}
//		
//		int[] aint = new int[count * outputSize];
//		for (int i = 0; i < count; i++) {
//			int j = 0;
//			bytes.position(i * inputSize + vertexPos);
//			aint[i * outputSize + j++] = bytes.getInt();
//			aint[i * outputSize + j++] = bytes.getInt();
//			aint[i * outputSize + j++] = bytes.getInt();
//			bytes.position(i * inputSize + colorPos);
//			aint[i * outputSize + j++] = bytes.getInt();
//			bytes.position(i * inputSize + texPos);
//			aint[i * outputSize + j++] = bytes.getInt();
//			aint[i * outputSize + j++] = bytes.getInt();
//			bytes.position(i * inputSize + lightPos);
//			aint[i * outputSize + j++] = bytes.getInt();
//		}
//		
//		return aint;
//	}
	
	public static class ExtraProps implements INBTSerializable<NBTTagCompound> {
		
		public String biome = "ocean";
		public boolean acceptLighting = true;
		public boolean custom = false;
		public CurveKeyframeChannel skyCurve = new CurveKeyframeChannel(240);
		public CurveKeyframeChannel blockCurve = new CurveKeyframeChannel(240);
		public boolean topLevel = false;
		public boolean fakeNormal = false;
		public Vector3f normal = new Vector3f(0, 1, 0);
		public Vector3f translate = new Vector3f(0, 0, 0);
		public Vector3f scale = new Vector3f(1, 1, 1);
		public Vector3f rotate = new Vector3f(0, -180, 0); // Compatible with older versions
		public TransAnimation anim = new TransAnimation(this);
		
		private byte[] cacheSky = new byte[256];
		private int cacheSkyHash = -1;
		private byte[] cacheBlock = new byte[256];
		private int cacheBlockHash = -1;
		
		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof ExtraProps) {
				ExtraProps p = (ExtraProps) o;
				return Objects.equals(this.biome, p.biome)
						&& this.acceptLighting == p.acceptLighting
						&& this.custom == p.custom
						&& (!this.custom || this.skyCurve.equals(p.skyCurve) && this.blockCurve.equals(p.blockCurve))
						&& this.topLevel == p.topLevel
						&& this.fakeNormal == p.fakeNormal
						&& (!this.fakeNormal || this.normal.equals(p.normal))
						&& this.translate.equals(p.translate)
						&& this.scale.equals(p.scale)
						&& this.rotate.equals(p.rotate)
						&& this.anim.equals(p.anim);
			}
			return false;
		}

		public void copy(ExtraProps prop) {
			this.biome = prop.biome;
			this.acceptLighting = prop.acceptLighting;
			this.custom = prop.custom;
			this.skyCurve.copy(prop.skyCurve);
			this.blockCurve.copy(prop.blockCurve);
			this.topLevel = prop.topLevel;
			this.fakeNormal = prop.fakeNormal;
			this.normal.set(prop.normal);
			this.translate.set(prop.translate);
			this.scale.set(prop.scale);
			this.rotate.set(prop.rotate);
			this.anim.copy(prop.anim);
			this.anim.reset(this);
			
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Biome", this.biome);
			nbt.setBoolean("Lighting", this.acceptLighting);
			nbt.setBoolean("Custom", this.custom);
			nbt.setString("SkyCurve", CurveKeyframeChannel.toJson(this.skyCurve));
			nbt.setString("BlockCurve", CurveKeyframeChannel.toJson(this.blockCurve));
			nbt.setBoolean("TopLevel", this.topLevel);
			nbt.setBoolean("FakeNormal", this.fakeNormal);
			nbt.setTag("Normal", NBTUtils.writeFloatList(new NBTTagList(), this.normal));
			nbt.setTag("T", NBTUtils.writeFloatList(new NBTTagList(), this.translate));
			nbt.setTag("S", NBTUtils.writeFloatList(new NBTTagList(), this.scale));
			nbt.setTag("R", NBTUtils.writeFloatList(new NBTTagList(), this.rotate));
			NBTTagCompound anim = this.anim.toNBT();
			if (!anim.isEmpty())
				nbt.setTag("Animation", anim);
			
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			if (nbt.hasKey("Biome", 8))
				this.biome = nbt.getString("Biome");
			if (nbt.hasKey("Lighting", 1))
				this.acceptLighting = nbt.getBoolean("Lighting");
			if (nbt.hasKey("Custom", 1))
				this.custom = nbt.getBoolean("Custom");
			if (nbt.hasKey("SkyCurve", 8)) {
				CurveKeyframeChannel channel = CurveKeyframeChannel.parseJson(nbt.getString("SkyCurve"));
				if (channel != null)
					this.skyCurve = channel;
			}
			if (nbt.hasKey("BlockCurve", 8)) {
				CurveKeyframeChannel channel = CurveKeyframeChannel.parseJson(nbt.getString("BlockCurve"));
				if (channel != null)
					this.blockCurve = channel;
			}
			if (nbt.hasKey("TopLevel", 1))
				this.topLevel = nbt.getBoolean("TopLevel");
			if (nbt.hasKey("FakeNormal", 1))
				this.fakeNormal = nbt.getBoolean("FakeNormal");
			NBTUtils.readFloatList(nbt.getTagList("Normal", 5), this.normal);
			NBTUtils.readFloatList(nbt.getTagList("T", 5), this.translate);
			NBTUtils.readFloatList(nbt.getTagList("S", 5), this.scale);
			NBTUtils.readFloatList(nbt.getTagList("R", 5), this.rotate);
			if (nbt.hasKey("Animation", 10)) {
				this.anim.fromNBT(nbt.getCompoundTag("Animation"));
				this.anim.reset(this);
			}
			
		}
		
	}
	
	public static class TransAnimation extends Animation {

		public Vector3f lastTranslate = new Vector3f();
		public Vector3f lastScale = new Vector3f();
		public Vector3f lastRotate = new Vector3f();
		
		public Vector3f translate = new Vector3f();
		public Vector3f scale = new Vector3f();
		public Vector3f rotate = new Vector3f();
		
		public TransAnimation(ExtraProps prop) {
			reset(prop);
		}
		
		@Override
		public float getFactor(float partialTicks) {
			if (this.duration <= 0f)
				return 1.0f;
			return super.getFactor(partialTicks);
		}

		public void reset(ExtraProps prop) {
			this.reset();
			lastTranslate.set(prop.translate);
			lastScale.set(prop.scale);
			lastRotate.set(prop.rotate);
			translate.set(prop.translate);
			scale.set(prop.scale);
			rotate.set(prop.rotate);
		}
		
		public void calcTSR(ExtraProps prop, float partialTicks) {
			if (this.isInProgress()) {
				float factor = this.getFactor(partialTicks);
				this.translate.x = this.interp.interpolate(this.lastTranslate.x, prop.translate.x, factor);
				this.translate.y = this.interp.interpolate(this.lastTranslate.y, prop.translate.y, factor);
				this.translate.z = this.interp.interpolate(this.lastTranslate.z, prop.translate.z, factor);
				this.scale.x = this.interp.interpolate(this.lastScale.x, prop.scale.x, factor);
				this.scale.y = this.interp.interpolate(this.lastScale.y, prop.scale.y, factor);
				this.scale.z = this.interp.interpolate(this.lastScale.z, prop.scale.z, factor);
				this.rotate.x = this.interp.interpolate(this.lastRotate.x, prop.rotate.x, factor);
				this.rotate.y = this.interp.interpolate(this.lastRotate.y, prop.rotate.y, factor);
				this.rotate.z = this.interp.interpolate(this.lastRotate.z, prop.rotate.z, factor);
			} else {
				this.translate.set(prop.translate);
				this.scale.set(prop.scale);
				this.rotate.set(prop.rotate);
			}
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class TemplateLoader extends FakeWorld implements Runnable {

		public static final BlockPos origin = BlockPos.ORIGIN.add(0, 64, 0);
		
		private static final TemplateLoader instance = new TemplateLoader();
		private static final ReentrantLock lock = new ReentrantLock();
		private static HashMap<NBTTagCompound, TemplateDataPack> cache = new HashMap<>();
		
		static {
			new Thread(SidedThreadGroups.CLIENT, new TemplateLoader(), "Client Template Loader").start(); // create a new world for avoid conflict of chunk data
		}
		
		public static TemplateLoader getInstance() {
			return instance;
		}
		
		public static void addTemplate(NBTTagCompound nbt, NBTTagCompound old) {
			if (nbt == null)
				return;
			lock.lock();
			HashMap<NBTTagCompound, TemplateDataPack> map = new HashMap<>(cache);
			if (old != null)
				map.remove(old);
			map.put(nbt, null);
			cache = map;
			lock.unlock();
		}
		
		public static void removeTemplate(NBTTagCompound nbt) {
			lock.lock();
			HashMap<NBTTagCompound, TemplateDataPack> map = new HashMap<>(cache);
			map.remove(nbt);
			cache = map;
			lock.unlock();
		}
		
		public static void removeAll() {
			lock.lock();
			cache = new HashMap<>();
			lock.unlock();
		}
		
		private Biome biome;
		private TemplateChunkProvider templateProvider;
		
		private TemplateLoader() {
			super(null,
					new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "StructureRenderer"),
					new WorldProviderSurface(),
					new Profiler(),
					true
					);
			this.chunkProvider = createChunkProvider();
			this.provider.setWorld(this);
			this.biome = Biomes.DEFAULT;
		}

		@Override
		public Biome getBiome(BlockPos pos) {
			return biome;
		}

		@Override
		protected IChunkProvider createChunkProvider() {
			return (this.templateProvider = new TemplateChunkProvider(this));
		}

		@Override
		protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
			return allowEmpty || x >= -1 && x <= 2 && z >= -1 && z <= 2;
		}
		
		// Recovery origin light calculate method from blockbuster 2.2
		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
	        int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
	        int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);

	        if (j < lightValue)
	        {
	            j = lightValue;
	        }

	        return i << 20 | j << 4;
		}

		@Override
		public void run() {
			ArrayList<NBTTagCompound> shouldLoad = new ArrayList<>();
			PlacementSettings settings = new PlacementSettings();
			while (true) {
				shouldLoad.clear();
				for (Map.Entry<NBTTagCompound, TemplateDataPack> entry : cache.entrySet()) {
					if (entry.getValue() == null && entry.getKey() != null)
						shouldLoad.add(entry.getKey());
				}
				for (NBTTagCompound nbt : shouldLoad) {
					this.loadedEntityList.clear();
					this.loadedTileEntityList.clear();
					this.tickableTileEntities.clear();
					Template template = new Template();
					template.read(nbt);
					template.addBlocksToWorld(this, origin, settings);
					
					for (TileEntity te : this.tickableTileEntities) {
						ITickable ite = (ITickable) te;
		        		ite.update();
					}
					
					TemplateDataPack pack = new TemplateDataPack();
					pack.chunks = this.templateProvider.getChunks();
					pack.entities = new ArrayList<>();
					for (Entity entity : this.loadedEntityList) {
						entity.world = instance;
						pack.entities.add(entity);
					}
					pack.tileEntities = new ArrayList<>();
					for (TileEntity te : this.loadedTileEntityList) {
						te.setWorld(instance);
						pack.tileEntities.add(te);
					}
					pack.tickableTileEntities = new ArrayList<>(this.tickableTileEntities);
					pack.size = template.getSize();
					lock.lock();
					HashMap<NBTTagCompound, TemplateDataPack> map = new HashMap<>(cache);
					if (map.containsKey(nbt)) {
						map.put(nbt, pack);
						cache = map;
					}
					lock.unlock();
				}
				try {
					Thread.sleep(0L);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		public void setBiome(String biome) {
			this.biome = Biome.REGISTRY.getObject(new ResourceLocation(biome));
			if (this.biome == null)
				this.biome = Biomes.DEFAULT;
		}
		
		public BlockPos loadTemplate(NBTTagCompound nbt) {
			TemplateDataPack pack = cache.get(nbt);
			if (pack != null) {
				this.loadedEntityList.clear();
				this.loadedTileEntityList.clear();
				this.tickableTileEntities.clear();
				
				this.templateProvider.setChunks(pack.chunks);
				this.loadedEntityList.addAll(pack.entities);
				this.loadedTileEntityList.addAll(pack.tileEntities);
				this.tickableTileEntities.addAll(pack.tickableTileEntities);
				return pack.size;
			}
	        return null;
		}
		
		private static class TemplateChunkProvider implements IChunkProvider {

			private static final Field FIELDWORLD;
			
			static {
				Field fWorld = null;
				try {
					fWorld = Chunk.class.getDeclaredField("field_76637_e");
				} catch (NoSuchFieldException | SecurityException e) {
					try {
						fWorld = Chunk.class.getDeclaredField("world");
					} catch (NoSuchFieldException | SecurityException e1) {}
				}
				FIELDWORLD = fWorld;
			}
			
			private final Chunk[] chunks = new Chunk[16];
			private final World world;
			private final Chunk empty;
			
			public TemplateChunkProvider(World world) {
				this.world = world;
				this.empty = new EmptyChunk(world, 255, 255);
			}
			
			@Override
			public Chunk getLoadedChunk(int x, int z) {
				if (isChunkGeneratedAt(x, z)) {
					int index = getIndex(x, z);
					if (chunks[index] == null) {
						chunks[index] = new Chunk(this.world, x, z);
						chunks[index].markLoaded(true);
					}
					return chunks[index];
				}
				return empty;
			}

			@Override
			public Chunk provideChunk(int x, int z) {
				return this.getLoadedChunk(x, z);
			}

			@Override
			public boolean tick() {
				return false;
			}

			@Override
			public String makeString() {
				return "Structure Renderer Chunk Provider";
			}

			@Override
			public boolean isChunkGeneratedAt(int x, int z) {
				return x <= 2 && x >= -1 && z <= 2 && z >= -1;
			}
			
			private int getIndex(int x, int z) {
				return x + 1 << 2 | z + 1;
			}
			
			public Chunk[] getChunks() {
				Chunk[] chunks = new Chunk[16];
				for (int i = 0; i < 16; i++) {
					chunks[i] = this.chunks[i];
					this.chunks[i] = null;
					setWorld(chunks[i], instance);
				}
				return chunks;
			}
			
			public void setChunks(Chunk[] chunks) {
				for (int i = 0; i < 16; i++)
					this.chunks[i] = chunks[i];
			}
			
			private void setWorld(Chunk chunk, World world) {
				if (FIELDWORLD != null) {
					try {
						FIELDWORLD.set(chunk, world);
					} catch (IllegalArgumentException | IllegalAccessException e) {}
				}
			}
			
		}
		
		private static class TemplateDataPack {
			public BlockPos size;
			public Chunk[] chunks;
			public List<Entity> entities;
			public List<TileEntity> tileEntities;
			public List<TileEntity> tickableTileEntities;
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class StructureLoadingRenderer extends StructureRenderer {
		
		private static final StructureLoadingRenderer instance = new StructureLoadingRenderer();
		
		public static StructureLoadingRenderer getRenderer(String structure, boolean empty) {
			instance.structure = structure;
			instance.empty = empty;
			return instance;
		}
		
		private String structure;
		private boolean empty;
		
		private StructureLoadingRenderer() {
			try {
				StructureRenderer.class.getField("list").setInt(this, 0);
			} catch (Exception e) {}
			try {
				StructureRenderer.class.getField("vbo").setInt(this, 0); // 2.2
				StructureRenderer.class.getField("world").set(this, TemplateLoader.getInstance()); // 2.2
			} catch (Exception e) {}
			this.size = new BlockPos(0, 1, 0);
		}

		@Override
		public void render() {
			Minecraft mc = Minecraft.getMinecraft();
			if (!GuiModelRenderer.isRendering() && (empty && !mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI))
				return;
			
        	/**
        	 * Reference from Blockbuster Mod
        	 * Url: https://github.com/mchorse/blockbuster
        	 * Author: mchorse
        	 * License: MIT
        	 * 
        	 * mchorse.blockbuster.client.render.RenderActor.renderPlayerRecordingName(EntityActor, double, double, double)
        	 */
			String str = I18n.format(empty ? "hackersmorph.gui.structure.empty" : "hackersmorph.gui.structure.loading", this.structure);
			FontRenderer font = mc.fontRenderer;
			RenderManager mgr = mc.getRenderManager();
			float viewerYaw = mgr.playerViewY;
			float viewerPitch = mgr.playerViewX;
			boolean isThirdPersonFrontal = mgr.options.thirdPersonView == 2;
			int verticalShift = -font.FONT_HEIGHT * 3 / 2;

            int shader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

            if (shader != 0)
            {
                OpenGlHelper.glUseProgram(0);
            }

            GlStateManager.pushMatrix();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.05F, -0.05F, 0.05F);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int i = font.getStringWidth(str) / 2;
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos((double)(-i - 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos((double)(-i - 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos((double)(i + 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos((double)(i + 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();

            font.drawString(str, -i, verticalShift, -1);

            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();

            if (shader != 0)
            {
                OpenGlHelper.glUseProgram(shader);
            }
		}

		@Override
		public void renderTEs() {}

		@Override
		public void delete() {}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class StructureVertexRenderer extends StructureRenderer {
		
		public static final StructureVertexRenderer EMPTY = new StructureVertexRenderer();

		private VertexFormat lightOffNormal;
		private VertexFormat lightOnNormal;
		private VertexFormat lightOff;
		private VertexFormat lightOn;
		private int offsetLight;
		private int[] vertexData;
		private int[] fullCubeData;
		private Map<TileEntity, Integer> tes;
		private Map<Entity, Integer> es;
		private ExtraProps prop;
		
		private int renderTimes = 0;
		
		private StructureVertexRenderer() {}
		
		public StructureVertexRenderer(BufferBuilder buff, int[] fullCubeData, BlockPos size, Map<TileEntity, Integer> tes, Map<Entity, Integer> es) {
			try {
				StructureRenderer.class.getField("list").setInt(this, 0);
			} catch (Exception e) {}
			try {
				StructureRenderer.class.getField("vbo").setInt(this, 0); // 2.2
				StructureRenderer.class.getField("world").set(this, TemplateLoader.getInstance()); // 2.2
			} catch (Exception e) {}
			this.size = size;
			lightOffNormal = buff.getVertexFormat();
			offsetLight = lightOffNormal.getUvOffsetById(1) / 4;
			lightOnNormal = new VertexFormat();
			lightOff = new VertexFormat();
			lightOn = new VertexFormat();
			for (VertexFormatElement element : lightOffNormal.getElements()) {
				if (element.getUsage() == EnumUsage.UV && element.getIndex() == 1) {
					lightOnNormal.addElement(new VertexFormatElement(0, EnumType.INT, EnumUsage.PADDING, 1));
					lightOn.addElement(new VertexFormatElement(0, EnumType.INT, EnumUsage.PADDING, 1));
					lightOff.addElement(element);
				} else {
					lightOnNormal.addElement(element);
					lightOff.addElement(element);
					lightOn.addElement(element);
				}
			}
			// If buff.getVertexFormat().getIntegerSize() equals 14, the Optifine will try to fill vertex normal data
			// into the location of buff.getVertexFormat().getNormalOffset() whether it has space for vertex normal data
			// or not.
			// The addElement method records the location for vertex normal and give it to Optifine, so what i did is give
			// it a empty place to write data, and then replace the VertexFormatElement for drop these vertex normal data
			// when rendering.
			for (int i = 0; i < lightOff.getElementCount(); i++) {
				VertexFormatElement element = lightOff.getElement(i);
				if (element.getUsage() == EnumUsage.NORMAL) {
					lightOff.getElements().set(i, new VertexFormatElement(0, EnumType.BYTE, EnumUsage.PADDING, 3));
					lightOn.getElements().set(i, new VertexFormatElement(0, EnumType.BYTE, EnumUsage.PADDING, 3));
					break;
				}
			}
			vertexData = new int[buff.getVertexCount() * buff.getVertexFormat().getIntegerSize()];
			buff.getByteBuffer().asIntBuffer().get(vertexData);
			
			this.fullCubeData = fullCubeData;
			this.tes = tes;
			this.es = es;
			this.prop = new ExtraProps();
		}

		@Override
		public void render() {
			if (this.renderTimes >= 10 || GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) >= GL11.glGetInteger(GL11.GL_MAX_MODELVIEW_STACK_DEPTH) - 4)
				return;
			
			TransAnimation anim = prop.anim;
//			GL11.glPushMatrix();
			GL11.glTranslatef(anim.translate.x, anim.translate.y, anim.translate.z);
			GL11.glRotatef(anim.rotate.z, 0, 0, 1);
			GL11.glRotatef(anim.rotate.y, 0, 1, 0);
			GL11.glRotatef(anim.rotate.x, 1, 0, 0);
			GL11.glScaled(anim.scale.x, anim.scale.y, anim.scale.z);
			
			RenderHelper.disableStandardItemLighting();
			if (GuiModelRenderer.isRendering() && !this.prop.acceptLighting)
				Minecraft.getMinecraft().entityRenderer.enableLightmap();
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buff = tess.getBuffer();
			if (processVertex(this.vertexData, buff))
				tess.draw();
			GlStateManager.enablePolygonOffset();
			float offset = this.prop.topLevel ? -1f : 1f;
			GlStateManager.doPolygonOffset(offset, offset);
			if (processVertex(this.fullCubeData, buff))
				tess.draw();
			GlStateManager.doPolygonOffset(0f, 0f);
			GlStateManager.disablePolygonOffset();
            GlStateManager.enableLighting();
            GlStateManager.enableLight(0);
            GlStateManager.enableLight(1);
            GlStateManager.enableColorMaterial();
			if (!this.prop.acceptLighting) {
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
				if (GuiModelRenderer.isRendering())
					Minecraft.getMinecraft().entityRenderer.disableLightmap();
			}
			
//			GL11.glPopMatrix();
		}

		@Override
		public void renderTEs() {
			if (this.tes == null && this.es == null || this.renderTimes >= 10 || GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) >= GL11.glGetInteger(GL11.GL_MAX_MODELVIEW_STACK_DEPTH) - 4)
				return;

//			TransAnimation anim = prop.anim;
//			GL11.glPushMatrix();
//			GL11.glTranslatef(anim.translate.x, anim.translate.y, anim.translate.z);
//			GL11.glRotatef(anim.rotate.z, 0, 0, 1);
//			GL11.glRotatef(anim.rotate.y, 0, 1, 0);
//			GL11.glRotatef(anim.rotate.x, 1, 0, 0);
//			GL11.glScaled(anim.scale.x, anim.scale.y, anim.scale.z);
			
			if (GuiModelRenderer.isRendering() && !this.prop.acceptLighting)
				Minecraft.getMinecraft().entityRenderer.enableLightmap();
			float lastX = OpenGlHelper.lastBrightnessX;
			float lastY = OpenGlHelper.lastBrightnessY;
			this.renderTimes++;
			GlStateManager.enableAlpha();
			if (this.tes != null) {
				this.tes.forEach((te, light) -> {
					if (!this.prop.acceptLighting) {
						int block = light % 65536;
						int sky = light / 65536;
						if (this.prop.custom) {
							block = this.prop.cacheBlock[block] & 0xFF;
							sky = this.prop.cacheSky[sky] & 0xFF;
						}
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, block, sky);
					}
					BlockPos pos = te.getPos();
					TileEntityRendererDispatcher.instance.render(te, pos.getX() - this.size.getX() / 2.0,
							(double) pos.getY() - 64.0, pos.getZ() - this.size.getZ() / 2.0, 0.0f);
				});
				if (GuiModelRenderer.isRendering())
					GlStateManager.disableFog(); // Beacon
			}
			if (this.es != null) {
				this.es.forEach((e, light) -> {
					if (!this.prop.acceptLighting) {
						int block = light % 65536;
						int sky = light / 65536;
						if (this.prop.custom) {
							block = this.prop.cacheBlock[block] & 0xFF;
							sky = this.prop.cacheSky[sky] & 0xFF;
						}
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, block, sky);
					}
					Minecraft.getMinecraft().getRenderManager().renderEntity(e, e.prevPosX - this.size.getX() / 2.0,
							e.prevPosY - 64.0, e.prevPosZ - this.size.getZ() / 2.0, e.rotationYaw, 1f, false);
				});
			}
			GlStateManager.disableAlpha();
			this.renderTimes--;
			if (!this.prop.acceptLighting) {
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
				if (GuiModelRenderer.isRendering())
					Minecraft.getMinecraft().entityRenderer.disableLightmap();
			}
			
//			GL11.glPopMatrix();
		}

		@Override
		public void delete() {
			this.vertexData = null;
			this.fullCubeData = null;
			this.tes = null;
		}
		
		public StructureRenderer props(ExtraProps prop) {
			this.prop = prop;
			int hash = hashCheck(this.prop.skyCurve.getKeyframes());
//			HackersMorph.LOGGER.debug(hash);
			if (this.prop.cacheSkyHash != hash) {
				this.prop.cacheSkyHash = hash;
				for (int i = 0; i < 256; i++)
					this.prop.cacheSky[i] = (byte) MathHelper.clamp((int) this.prop.skyCurve.interpolate(i), 0, 240);
			}
			hash = hashCheck(this.prop.blockCurve.getKeyframes());
			if (this.prop.cacheBlockHash != hash) {
				this.prop.cacheBlockHash = hash;
				for (int i = 0; i < 256; i++)
					this.prop.cacheBlock[i] = (byte) MathHelper.clamp((int) this.prop.blockCurve.interpolate(i), 0, 240);
			}
			return this;
		}
		
		@SuppressWarnings("rawtypes")
		private int hashCheck(List list) {
			int hash = 0;
			for (Object obj : list)
				hash ^= obj.hashCode();
			return hash;
		}
		
		private boolean processVertex(int[] v, BufferBuilder buff) {
			if (v == null || v.length == 0)
				return false;
			if (this.prop.acceptLighting) {
				if (this.prop.fakeNormal) {
					GlStateManager.glNormal3f(this.prop.normal.x, this.prop.normal.y, this.prop.normal.z);
					buff.begin(GL11.GL_QUADS, lightOn);
				} else
					buff.begin(GL11.GL_QUADS, lightOnNormal);
				buff.addVertexData(v);
			} else {
				if (this.prop.fakeNormal) {
					GlStateManager.glNormal3f(this.prop.normal.x, this.prop.normal.y, this.prop.normal.z);
					buff.begin(GL11.GL_QUADS, lightOff);
				} else
					buff.begin(GL11.GL_QUADS, lightOffNormal);
				if (this.prop.custom) {
					int[] vertex;
					int size = lightOff.getIntegerSize();
					for (int i = 0; i < v.length / size; i++) {
						vertex = Arrays.copyOfRange(v, i * size, i * size + size);
						// Smoooooooooooooooooooooooooooth lighting is enabled!
						int block = this.prop.cacheBlock[vertex[this.offsetLight] % 65536] & 0xFF;
						int sky = this.prop.cacheSky[vertex[this.offsetLight] / 65536] & 0xFF;
						vertex[this.offsetLight] = sky << 16 | block;
						buff.addVertexData(vertex);
					}
				} else {
					buff.addVertexData(v);
				}
			}
			return true;
		}
		
	}
	
	public static class CurveKeyframeChannel extends KeyframeChannel {

		private static final Gson JSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		
		public static String toJson(CurveKeyframeChannel channel) {
			return JSON.toJson(channel);
		}
		
		public static CurveKeyframeChannel parseJson(String json) {
			KeyframeChannel channel = null;
			try {
				channel = JSON.fromJson(json, KeyframeChannel.class);
			} catch (Throwable e) {}
			if (channel == null)
				return null;
			CurveKeyframeChannel curveChannel = new CurveKeyframeChannel();
			curveChannel.copy(channel);
			return curveChannel;
		}
		
		private double[] matrix;
		private double[] vec;
		
		public CurveKeyframeChannel() {
			this(255);
		}
		
		public CurveKeyframeChannel(long size) {
			this.keyframes.add(new CurveKeyframe(0, 0, this));
			this.keyframes.add(new CurveKeyframe(size, size, this));
			this.matrix = null;
			this.vec = null;
			this.sort();
			this.reCalc();
		}

		@Override
		public double interpolate(float ticks) {
			if (this.keyframes.isEmpty()) {
				return 0.0;
			}
			double value = this.keyframes.get(0).value;
			CurveKeyframe prev = null;
			for (final Keyframe frame : this.keyframes) {
				if (ticks < frame.tick)
					break;
				prev = (CurveKeyframe) frame;
			}
			if (prev == null)
				return value;
			return prev.a * ticks * ticks * ticks + prev.b * ticks * ticks + prev.c * ticks + prev.d;
		}

		@Override
		public int insert(long tick, double value) {
			int index;
			if (this.keyframes.isEmpty())
				index = super.insert(tick, value);
			else {
				if(tick < this.get(0).tick) {
					index = 0;
					this.get(0).setTick(tick);
					this.get(0).setValue(value);
				} else if(tick > this.get(this.keyframes.size() - 1).tick) {
					index = this.keyframes.size() - 1;
					this.get(index).setTick(tick);
					this.get(index).setValue(value);
				} else
					index = super.insert(tick, value);
			}
			sort();
			reCalc();
			return index;
		}
		
		@Override
		public void remove(int index) {
			if (this.keyframes.size() > 2) {
				super.remove(index);
				sort();
				reCalc();
			}
		}

		@Override
		protected Keyframe create(long tick, double value) {
			return new CurveKeyframe(tick, value, this);
		}

		@Override
		public void copy(KeyframeChannel channel) {
			this.keyframes.clear();
			boolean recalc = false;
			for (final Keyframe frame : channel.getKeyframes()) {
				Keyframe key = this.create(frame.tick, frame.value);
				this.keyframes.add(key);
				if (frame instanceof CurveKeyframe) {
					CurveKeyframe k = (CurveKeyframe) key;
					CurveKeyframe f = (CurveKeyframe) frame;
					k.a = f.a;
					k.b = f.b;
					k.c = f.c;
					k.d = f.d;
				} else recalc = true;
			}
			if (recalc) {
				this.sort();
				reCalc();
			}
		}

		@Override
		public void sort() {
			super.sort();
			if (this.keyframes.size() > 2) {
				boolean dirty = false;
				Keyframe prev = null;
				Iterator<Keyframe> iterator = this.keyframes.iterator();
				while (iterator.hasNext()) {
					Keyframe key = iterator.next();
					if (prev != null && key.tick == prev.tick && Math.abs(key.value - prev.value) < 1) {
						dirty = true;
						iterator.remove();
						continue;
					}
					prev = key;
				}
				if (dirty) {
					super.sort();
					this.reCalc();
				}
			}
		}

		// Cubic Spline Interpolation 三次样条插值
		// 网上说PS的曲线用的就是这个
		public void reCalc() {
			if (this.keyframes.size() < 2)
				return;
			
			int length = 4 * (this.keyframes.size() - 1);
			// 三次样条插值计算的是关于边的方程，所以是顶点-1
			// ax^3+bx^2+cx+d共4个参数
			// 但tick值相同的帧两边要当作不同的曲线分开算
			if (this.matrix == null || length * length != this.matrix.length)
				this.matrix = new double[length * length];
			if (this.vec == null || length != this.vec.length)
				vec = new double[length];
			
			int begin = 0;
			Keyframe prev = this.get(0);
			for (int i = 1; i < this.keyframes.size(); i++) {
				Keyframe key = this.get(i);
				if (prev.tick == key.tick) {
					calc(begin, i - 1);
					begin = i;
				}
				prev = key;
			}
			calc(begin, this.keyframes.size() - 1);
		}
		
		private void calc(int from, int to) {
			if (from == to) {
				CurveKeyframe key = (CurveKeyframe) this.get(from);
				key.a = key.b = key.c = 0;
				key.d = key.value;
				return;
			}
			
			int length = 4 * (to - from);
			Arrays.fill(matrix, 0);
			Arrays.fill(vec, 0);
			double size = this.keyframes.get(this.keyframes.size() - 1).tick;

			Keyframe key0 = this.keyframes.get(from);
			Keyframe key1 = this.keyframes.get(from + 1);
			// 调试矩阵的时候一看我滴妈240的三次方都8位数了，除个size吧
			double tick0 = key0.tick / size;
			double tick1 = key1.tick / size;
			// 4个方程一组，每组方程计算一条边的参数，p0与p1分别为边左侧与右侧的已知点
			// 这个顺序是为了能让方阵的主对角线没有0，因为解方程组用的是LU不是LUP（懒得算P）
			// p1两侧边的二阶导为0
			// 在算式被裁剪的情况下还能计算最后一个点的自然边界
			// y''=6ax+2b equals
			MatrixTools.fillData(this.matrix, length, 0, 0, 
					6 * tick1, 2, 0, 0,  
					-6 * tick1, -2);
			// 自然边界，第一个点和最后一个点所在边的二阶导为0，即不凹也不凸
			// Natural Spline y''(p0)=0
			MatrixTools.fillData(this.matrix, length, 1, 0,
					6 * tick0, 2);
			// p1在此边上
			// y=ax^3+bx^2+cx+d for p1
			MatrixTools.fillData(this.matrix, length, 2, 0,
					tick1 * tick1 * tick1,
					tick1 * tick1, tick1, 1);
			vec[2] = key1.value;
			// p0在此边上
			// y=ax^3+bx^2+cx+d for p0
			MatrixTools.fillData(this.matrix, length, 3, 0,
					tick0 * tick0 * tick0,
					tick0 * tick0,
					tick0, 1);
			vec[3] = key0.value;
			for (int i = 1; i < to - from; i++) {
				key0 = this.keyframes.get(from + i);
				key1 = this.keyframes.get(from + i + 1);
				tick0 = key0.tick / size;
				tick1 = key1.tick / size;
				MatrixTools.fillData(this.matrix, length, 4 * i    , 4 * i, 
						6 * tick1, 2, 0, 0,  
						-6 * tick1, -2);
				MatrixTools.fillData(this.matrix, length, 4 * i + 1, 4 * i,
						tick1 * tick1 * tick1,
						tick1 * tick1, tick1, 1);
				vec[4 * i + 1] = key1.value;
				// p0两侧边的一阶导相等
				// y'=3ax^2+2bx+c equals
				MatrixTools.fillData(this.matrix, length, 4 * i + 2, 4 * i - 4,
						3 * tick0 * tick0, 2 * tick0, 1, 0,
						-3 * tick0 * tick0, -2 * tick0, -1);
				MatrixTools.fillData(this.matrix, length, 4 * i + 3, 4 * i,
						tick0 * tick0 * tick0,
						tick0 * tick0, tick0, 1);
				vec[4 * i + 3] = key0.value;
			}
			MatrixTools.solve(this.matrix, vec, length);
			for (int i = 0; i < to - from; i++) {
				CurveKeyframe key = (CurveKeyframe) this.keyframes.get(i + from);
				key.a = vec[4 * i] / size / size / size;
				key.b = vec[4 * i + 1] / size / size;
				key.c = vec[4 * i + 2] / size;
				key.d = vec[4 * i + 3];
			}
			CurveKeyframe key = (CurveKeyframe) this.keyframes.get(to);
			key.a = key.b = key.c = 0;
			key.d = key.value;
		}
		
		private static class CurveKeyframe extends Keyframe {
			
			private CurveKeyframeChannel channel;
			
			// no need to store
			// ax^3+bx^2+cx+d
			public double a;
			public double b;
			public double c;
			public double d;

			public CurveKeyframe(long tick, double value, CurveKeyframeChannel channel) {
				super(tick, value);
				this.a = this.b = this.c = 0;
				this.d = value;
				this.channel = channel;
			}

			@Override
			public void setTick(long tick) {
				super.setTick(tick);
				channel.reCalc();
			}

			@Override
			public void setValue(double value) {
				super.setValue(value);
				channel.reCalc();
			}

			@Override
			public double interpolate(Keyframe frame, float x) {
				if (frame == null)
					return this.value;
				x = x * (frame.tick - this.tick) + this.tick;
				return a * x * x * x + b * x * x + c * x + d;
			}

			@Override
			public int hashCode() {
				return super.hashCode() ^ Double.hashCode(a) ^ Double.hashCode(b) ^ Double.hashCode(c) ^ Double.hashCode(d) ^ Long.hashCode(this.tick) ^ Double.hashCode(this.value);
			}
			
		}
		
	}

}
