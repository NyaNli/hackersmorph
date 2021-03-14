package nyanli.hackersmorph.other.minecraft;

import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {

	private static final String BufferBuilder;
	private static final String WorldVertexBufferUploader;
	private static final String WorldVertexBufferUploader_draw;
	private static final String WorldProvider;
	private static final String Entity;
	private static final String Vec3d;
	private static final String GlStateManager_setFogDensity;
	private static final String GlStateManager_setFogStart;
	private static final String GlStateManager_setFogEnd;
	
	private static final String RenderManager = "nyanli/hackersmorph/other/minecraft/common/manager/RenderManager";
	
	static {
		if (isDeobfEnv) {
			BufferBuilder = "net/minecraft/client/renderer/BufferBuilder";
			WorldVertexBufferUploader = "net/minecraft/client/renderer/WorldVertexBufferUploader";
			WorldVertexBufferUploader_draw = "draw";
			WorldProvider = "net/minecraft/world/WorldProvider";
			Entity = "net/minecraft/entity/Entity";
			Vec3d = "net/minecraft/util/math/Vec3d";
			GlStateManager_setFogDensity = "setFogDensity";
			GlStateManager_setFogStart = "setFogStart";
			GlStateManager_setFogEnd = "setFogEnd";
		} else {
			BufferBuilder = "buk";
			WorldVertexBufferUploader = "bul";
			WorldVertexBufferUploader_draw = "a";
			WorldProvider = "aym";
			Entity = "vg";
			Vec3d = "bhe";
			GlStateManager_setFogDensity = "a";
			GlStateManager_setFogStart = "b";
			GlStateManager_setFogEnd = "c";
		}
	}
	
	@Patcher("net.minecraft.client.renderer.Tessellator")
	public static class TessellatorPatcher {
		
		@Patcher.Method("b()V")
		@Patcher.Method("draw()V")
		public static void draw(MethodNode method) {
			String desc = String.format("(L%s;L%s;)V", WorldVertexBufferUploader, BufferBuilder);
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> WorldVertexBufferUploader_draw.equals(((MethodInsnNode)node).name),
							node -> WorldVertexBufferUploader.equals(((MethodInsnNode)node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "doRender", desc, false)
			);
		}
		
	}
	
	@Patcher("net.minecraft.world.World")
	public static class WorldPatcher {
		
		@Patcher.Method("a(Lvg;F)Lbhe;")
		@Patcher.Method("getSkyColor(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;")
		public static void getSkyColor(MethodNode method) {
			String desc = String.format("(L%s;L%s;F)L%s;", WorldProvider, Entity, Vec3d);
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "hookSkyColor", desc, false)
			);
		}
		
		@Patcher.Method("f(F)Lbhe;")
		@Patcher.Method("getFogColor(F)Lnet/minecraft/util/math/Vec3d;")
		public static void getFogColor(MethodNode method) {
			String desc = String.format("(L%s;FF)L%s;", WorldProvider, Vec3d);
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> WorldProvider.equals(((MethodInsnNode)node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "hookFogColor", desc, false)
			);
		}
		
		@Patcher.Method("e(F)Lbhe;")
		@Patcher.Method("getCloudColour(F)Lnet/minecraft/util/math/Vec3d;")
		public static void getCloudColour(MethodNode method) {
			String desc = String.format("(L%s;F)L%s;", WorldProvider, Vec3d);
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "hookCloudColor", desc, false)
			);
		}
		
		@Patcher.Method("c(F)F")
		@Patcher.Method("getCelestialAngle(F)F")
		public static void getCelestialAngle(MethodNode method) {
			String desc = String.format("(L%s;JF)F", WorldProvider);
			insertNode(method,
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> WorldProvider.equals(((MethodInsnNode)node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "hookCelestialAngle", desc, false)
			);
		}
		
	}
	
	@Patcher("net.minecraft.client.renderer.GlStateManager")
	public static class GlStateManagerPatcher {
		
		private static HashMap<String, String> methodMap = new HashMap<>();
		static {
			methodMap.put(GlStateManager_setFogDensity, "hookFogDensity");
			methodMap.put(GlStateManager_setFogStart, "hookFogStart");
			methodMap.put(GlStateManager_setFogEnd, "hookFogEnd");
		}
		
		@Patcher.Method("a(F)V")
		@Patcher.Method("setFogDensity(F)V")
		@Patcher.Method("b(F)V")
		@Patcher.Method("setFogStart(F)V")
		@Patcher.Method("c(F)V")
		@Patcher.Method("setFogEnd(F)V")
		public static void setFog(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.FLOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, methodMap.get(method.name), "(F)F", false),
					new VarInsnNode(Opcodes.FSTORE, 0)
			);
		}
		
		@Patcher.Method("c(FFFF)V")
		@Patcher.Method("color(FFFF)V")
		public static void color(MethodNode method) {
			LabelNode label = new LabelNode();
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RenderManager, "canGlColor", "()Z", false),
					new JumpInsnNode(Opcodes.IFNE, label),
					new InsnNode(Opcodes.RETURN),
					label,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
	}
	
	@Patcher("net.minecraft.network.play.server.SPacketCustomPayload")
	public static class SPacketCustomPayloadPatcher {
		
		@Patcher.Method("<init>(Ljava/lang/String;Lnet/minecraft/network/PacketBuffer;)V")
		@Patcher.Method("<init>(Ljava/lang/String;Lgy;)V")
		public static void constructor(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.LDC,
							node -> new Integer(1048576).equals(((LdcInsnNode) node).cst)
					),
					InsertPos.REPLACE,
//					new MethodInsnNode(Opcodes.INVOKESTATIC, "mchorse/mclib/utils/PayloadASM", "getPayloadSize", "()I", false)
					new LdcInsnNode(Integer.MAX_VALUE)
			);
		}
		
	}
	
	@Patcher("net.minecraft.network.PacketBuffer")
	public static class PacketBufferPatcher {
		
		@Patcher.Method("readCompoundTag()Lnet/minecraft/nbt/NBTTagCompound;")
		@Patcher.Method("j()Lfy;")
		public static void constructor(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.LDC,
							node -> new Long(2097152L).equals(((LdcInsnNode) node).cst)
					),
					InsertPos.REPLACE,
					new LdcInsnNode(Long.MAX_VALUE)
			);
		}
		
	}

}
