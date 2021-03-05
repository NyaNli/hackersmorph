package nyanli.hackersmorph.other.mchorse.blockbuster;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.client.renderer.GlStateManager;
import nyanli.hackersmorph.asm.*;

public class ASM extends ClassTransformer {
	
	private static final String GuiSnowstormMorph = "mchorse/blockbuster_pack/client/gui/GuiSnowstormMorph";
	private static final String BedrockLibrary = "mchorse/blockbuster/client/particles/BedrockLibrary";
	private static final String SnowstormMorph = "mchorse/blockbuster_pack/morphs/SnowstormMorph";
	private static final String BlockGreen = "mchorse/blockbuster/common/block/BlockGreen";
	private static final String GuiSequencerMorphPanel = "mchorse/blockbuster_pack/client/gui/GuiSequencerMorph$GuiSequencerMorphPanel";
	private static final String GuiMorphActionPanel = "mchorse/blockbuster/client/gui/dashboard/panels/recording_editor/actions/GuiMorphActionPanel";
	private static final String TileEntityModel = "mchorse/blockbuster/common/tileentity/TileEntityModel";
	private static final String AbstractMorph = "mchorse/metamorph/api/morphs/AbstractMorph";
	private static final String GuiRecordingEditorPanel = "mchorse/blockbuster/client/gui/dashboard/panels/recording_editor/GuiRecordingEditorPanel";
//	private static final String GuiRecordSelector = "mchorse/blockbuster/client/gui/dashboard/panels/recording_editor/GuiRecordSelector";
	private static final String GuiDelegateElement = "mchorse/mclib/client/gui/framework/elements/GuiDelegateElement";
	private static final String GuiActionPanel = "mchorse/blockbuster/client/gui/dashboard/panels/recording_editor/actions/GuiActionPanel";
	private static final String Record = "mchorse/blockbuster/recording/data/Record";
//	private static final String FoundAction = "mchorse/blockbuster/recording/data/Record$FoundAction";
	private static final String EntityActor = "mchorse/blockbuster/common/entity/EntityActor";
	private static final String SequencerMorph = "mchorse/blockbuster_pack/morphs/SequencerMorph";
	private static final String Scene = "mchorse/blockbuster/recording/scene/Scene";
	private static final String RecordPlayer = "mchorse/blockbuster/recording/RecordPlayer";
	private static final String CameraHandler = "mchorse/blockbuster/aperture/CameraHandler";
	private static final String ClientProxy = "mchorse/blockbuster/ClientProxy";
	private static final String RecordManager = "mchorse/blockbuster/recording/RecordManager";
	private static final String PacketRequestedFrames = "mchorse/blockbuster/network/common/recording/PacketRequestedFrames";
	private static final String StructureRenderer = "mchorse/blockbuster_pack/morphs/StructureMorph$StructureRenderer";
	private static final String GuiModelRenderer = "mchorse/mclib/client/gui/framework/elements/GuiModelRenderer";
	
	private static final String TypeClientHandlerStructure = "Lmchorse/blockbuster/network/client/ClientHandlerStructure;";
	
	private static final String SubCommandRecordBlank = "nyanli/hackersmorph/other/mchorse/blockbuster/common/command/SubCommandRecordBlank";
	private static final String ScenePlayingManager = "nyanli/hackersmorph/other/mchorse/blockbuster/common/manager/ScenePlayingManager";
	private static final String GuiSnowstormMorphExtra = "nyanli/hackersmorph/other/mchorse/blockbuster/client/gui/GuiSnowstormMorphExtra";
	private static final String GuiStructureMorph = "nyanli/hackersmorph/other/mchorse/blockbuster/client/gui/GuiStructureMorph";
	private static final String StructureMorphExtraManager = "nyanli/hackersmorph/other/mchorse/blockbuster/common/manager/StructureMorphExtraManager";
	private static final String SnowstormMorphExtraManager = "nyanli/hackersmorph/other/mchorse/blockbuster/common/manager/SnowstormMorphExtraManager";
	private static final String BlockGreenExtra = "nyanli/hackersmorph/other/mchorse/blockbuster/common/block/BlockGreenExtra";
	private static final String GuiSequencerMorphExtraPanel = "nyanli/hackersmorph/other/mchorse/blockbuster/client/gui/panel/GuiSequencerMorphExtraPanel";
	private static final String SequencerMorphManager = "nyanli/hackersmorph/other/mchorse/blockbuster/common/manager/SequencerMorphManager";
	private static final String GuiMorphActionExtraPanel = "nyanli/hackersmorph/other/mchorse/blockbuster/client/gui/panel/GuiMorphActionExtraPanel";
	private static final String ShaderManager = "nyanli/hackersmorph/other/optifine/client/manager/ShaderManager";
	private static final String RecordExtraManager = "nyanli/hackersmorph/other/mchorse/blockbuster/common/manager/RecordExtraManager";
	
	private static final String TypeClientHandlerStructureBiome = "Lnyanli/hackersmorph/other/mchorse/blockbuster/client/network/ClientHandlerStructureBiome;";
	
	@Patcher("mchorse.blockbuster.commands.CommandRecord")
	public static class CommandRecordPatcher {

		// 添加record blank子命令
		@Patcher.Method("<init>()V")
		public static void constructor(MethodNode method) {
			// this.add(new SubCommandRecordBlank());
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new TypeInsnNode(Opcodes.NEW, SubCommandRecordBlank),
					new InsnNode(Opcodes.DUP),
					new MethodInsnNode(Opcodes.INVOKESPECIAL, SubCommandRecordBlank, "<init>", "()V", false),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "mchorse/blockbuster/commands/CommandRecord", "add", "(Lnet/minecraft/command/CommandBase;)V", false)
			);
		}
		
	}
	
	// 以下三个是在播放回放的玩家屏幕左上角显示提示
	@Patcher("mchorse.blockbuster.common.item.ItemPlayback")
	public static class ItemPlaybackPatcher {
		
		@Patcher.Method("func_77659_a(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;")
		@Patcher.Method("onItemRightClick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;")
		public static void onItemRightClick(MethodNode method) {
			// ScenePlayingManager.toggle(sceneMgr, sceneName, world, player)
			insertNode(method, 
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL, 
							node -> "toggle".equals(((MethodInsnNode) node).name),
							node -> "mchorse/blockbuster/recording/scene/SceneManager".equals(((MethodInsnNode) node).owner)
							),
					InsertPos.REPLACE,
					new VarInsnNode(Opcodes.ALOAD, 2),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ScenePlayingManager, "toggle", "(Lmchorse/blockbuster/recording/scene/SceneManager;Ljava/lang/String;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Z", false)
			);
		}
		
	}

	@Patcher("mchorse.blockbuster.network.server.scene.ServerHandlerScenePlayback")
	public static class ServerHandlerScenePlaybackPatcher {
		
		@Patcher.Method("run(Lnet/minecraft/entity/player/EntityPlayerMP;Lmchorse/blockbuster/network/common/scene/PacketScenePlayback;)V")
		public static void run(MethodNode method) {
			// ScenePlayingManager.toggle(sceneMgr, sceneName, world, player)
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "toggle".equals(((MethodInsnNode) node).name),
							node -> "mchorse/blockbuster/recording/scene/SceneManager".equals(((MethodInsnNode) node).owner)
							),
					InsertPos.REPLACE,
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ScenePlayingManager, "toggle", "(Lmchorse/blockbuster/recording/scene/SceneManager;Ljava/lang/String;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Z", false)
			);
		}
		
	}
	
	@Patcher("mchorse.blockbuster.recording.scene.Scene")
	public static class ScenePatcher {
		
		@Patcher.Method("stopPlayback()V")
		@Patcher.Method("stopPlayback(Z)V")
		public static void stopPlayback(MethodNode method) {
			// ScenePlayingManager.stopPlayback(this.id)
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "setPlaying".equals(((MethodInsnNode) node).name),
							node -> Scene.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, "mchorse/blockbuster/recording/scene/Scene", "id", "Ljava/lang/String;"),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ScenePlayingManager, "stopPlayback", "(Ljava/lang/String;)V", false)
			);
		}
		
		@Patcher.Method("goTo(IZ)V")
		public static void goTo(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "goTo".equals(((MethodInsnNode) node).name),
							node -> RecordPlayer.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "sceneGoTo", "(Lmchorse/blockbuster/recording/RecordPlayer;IZ)V", false)
			);
		}
		
	}
	
	// 添加结构伪装的GUI，修改暴雪粒子GUI
	@Patcher("mchorse.blockbuster_pack.BlockbusterFactory")
	public static class BlockbusterFactoryPatcher {
		
		@Patcher.Method("registerMorphEditors(Lnet/minecraft/client/Minecraft;Ljava/util/List;)V")
		public static void registerMorphEditors(MethodNode method) {
			// GuiSnowstormMorph -> GuiSnowstormMorphExtra
			insertNode(method, 
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW, 
							node -> GuiSnowstormMorph.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, GuiSnowstormMorphExtra)
			);
			insertNode(method, 
					queryContinue( 
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> GuiSnowstormMorph.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, GuiSnowstormMorphExtra, "<init>", "(Lnet/minecraft/client/Minecraft;)V", false)
			);

			// editor.add(new GuiStructureMorph(mc))
			insertNode(method, 
					queryContinue(node -> node.getOpcode() == Opcodes.POP),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 2),
					new TypeInsnNode(Opcodes.NEW, GuiStructureMorph),
					new InsnNode(Opcodes.DUP),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESPECIAL, GuiStructureMorph, "<init>", "(Lnet/minecraft/client/Minecraft;)V", false),
					new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true),
					new InsnNode(Opcodes.POP)
			);
		}
		
	}
	
	// 结构伪装大修改
	@Patcher("mchorse.blockbuster_pack.morphs.StructureMorph")
	public static class StructureMorphPatcher {
		
		@Patcher.Class
		public static void addExtraProp(ClassNode clazz) {
			clazz.visitField(Opcodes.ACC_PUBLIC, "asmExtraProps", "Lnyanli/hackersmorph/other/mchorse/blockbuster/common/manager/StructureMorphExtraManager$ExtraProps;", null, null).visitEnd();
			clazz.interfaces.add("mchorse/metamorph/api/morphs/utils/IAnimationProvider");
			clazz.interfaces.add("mchorse/metamorph/api/morphs/utils/ISyncableMorph");
			// Generate by Bytecode Outline
			{
				MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "update",
						"(Lnet/minecraft/entity/EntityLivingBase;)V", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(15, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "mchorse/metamorph/api/morphs/AbstractMorph", "update",
						"(Lnet/minecraft/entity/EntityLivingBase;)V", false);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLineNumber(16, l1);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						StructureMorphExtraManager,
						"update",
						"(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lnet/minecraft/entity/EntityLivingBase;)V",
						false);
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitLineNumber(17, l2);
				mv.visitInsn(Opcodes.RETURN);
				Label l3 = new Label();
				mv.visitLabel(l3);
				mv.visitLocalVariable("this", "Lmchorse/blockbuster_pack/morphs/StructureMorph;", null, l0, l3, 0);
				mv.visitLocalVariable("target", "Lnet/minecraft/entity/EntityLivingBase;", null, l0, l3, 1);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			{
				MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "canMerge",
						"(Lmchorse/metamorph/api/morphs/AbstractMorph;)Z", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(21, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						StructureMorphExtraManager,
						"canMerge",
						"(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lmchorse/metamorph/api/morphs/AbstractMorph;)Z",
						false);
				mv.visitInsn(Opcodes.IRETURN);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLocalVariable("this", "Lmchorse/blockbuster_pack/morphs/StructureMorph;", null, l0, l1, 0);
				mv.visitLocalVariable("morph", "Lmchorse/metamorph/api/morphs/AbstractMorph;", null, l0, l1, 1);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			{
				MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "pause",
						"(Lmchorse/metamorph/api/morphs/AbstractMorph;I)V", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(26, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitVarInsn(Opcodes.ILOAD, 2);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						StructureMorphExtraManager,
						"pause",
						"(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lmchorse/metamorph/api/morphs/AbstractMorph;I)V",
						false);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLineNumber(27, l1);
				mv.visitInsn(Opcodes.RETURN);
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitLocalVariable("this", "Lmchorse/blockbuster_pack/morphs/StructureMorph;", null, l0, l2, 0);
				mv.visitLocalVariable("previous", "Lmchorse/metamorph/api/morphs/AbstractMorph;", null, l0, l2, 1);
				mv.visitLocalVariable("offset", "I", null, l0, l2, 2);
				mv.visitMaxs(3, 3);
				mv.visitEnd();
			}
			{
				MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "isPaused", "()Z", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(31, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						StructureMorphExtraManager,
						"isPause", "(Lmchorse/blockbuster_pack/morphs/StructureMorph;)Z", false);
				mv.visitInsn(Opcodes.IRETURN);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLocalVariable("this", "Lmchorse/blockbuster_pack/morphs/StructureMorph;", null, l0, l1, 0);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			{
				MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "getAnimation",
						"()Lmchorse/metamorph/api/morphs/utils/Animation;", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(36, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						StructureMorphExtraManager,
						"getAnimation",
						"(Lmchorse/blockbuster_pack/morphs/StructureMorph;)Lmchorse/metamorph/api/morphs/utils/Animation;",
						false);
				mv.visitInsn(Opcodes.ARETURN);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLocalVariable("this", "Lmchorse/blockbuster_pack/morphs/StructureMorph;", null, l0, l1, 0);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
		}
		
		@Patcher.Method("fromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		@Patcher.Method("toNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		public static void nbt(MethodNode method) {
			// StructureMorphExtraManager.from/toNBT(...);
			insertNode(method, 
					method.instructions.getFirst(), 
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, method.name, "(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lnet/minecraft/nbt/NBTTagCompound;)V", false)
			);
		}
		
		@Patcher.Method("copy(Lmchorse/metamorph/api/morphs/AbstractMorph;)V")
		public static void copy(MethodNode method) {
			// this.??? = StructureMorphExtraManager.copy(morph, this).???;
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.GETFIELD),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, "copy", "(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lmchorse/blockbuster_pack/morphs/StructureMorph;)Lmchorse/blockbuster_pack/morphs/StructureMorph;", false)
			);
		}
		
		@Patcher.Method("equals(Ljava/lang/Object;)Z")
		public static void equals(MethodNode method) {
			// result = StructureMorphExtraManager.objEquals(super.equals(morph), this, morph)
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKESPECIAL),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, "equalsObj", "(ZLmchorse/blockbuster_pack/morphs/StructureMorph;Ljava/lang/Object;)Z", false)
			);
		}
		
		@Patcher.Method("render(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")
		@Patcher.Method("renderOnScreen(Lnet/minecraft/entity/player/EntityPlayer;IIFF)V")
		public static void render(MethodNode method) {
			// GL11.glRotatef(180 - entityYaw, 0, 1, 0);
			// StructureMorphExtraManager.getRenderer(STRUCTURES, this.structure, this)
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKEINTERFACE),
					InsertPos.REPLACE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, "getRenderer", "(Ljava/util/Map;Ljava/lang/String;Lmchorse/blockbuster_pack/morphs/StructureMorph;)Ljava/lang/Object;", false)
			);
			if ("render".equals(method.name)) {
				LabelNode label = new LabelNode();
				insertNode(method,
						queryContinue(
								node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
								node -> "render".equals(((MethodInsnNode) node).name),
								node -> StructureRenderer.equals(((MethodInsnNode) node).owner)
						),
						InsertPos.BEFORE,
						new VarInsnNode(Opcodes.ALOAD, 0),
						new VarInsnNode(Opcodes.ALOAD, 1),
						new VarInsnNode(Opcodes.FLOAD, 9),
						new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, "beforeRender", "(Lmchorse/blockbuster_pack/morphs/StructureMorph;Lnet/minecraft/entity/EntityLivingBase;F)V", false)
				);
			} else {
				String name = isDeobfEnv ? "disableFog" : "func_179106_n";
				insertNode(method,
						queryContinue(
								node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
								node -> "renderTEs".equals(((MethodInsnNode) node).name),
								node -> StructureRenderer.equals(((MethodInsnNode) node).owner)
						),
						InsertPos.AFTER,
						new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", name, "()V", false)
				);
			}
		}
		
		@Patcher.Method("cleanUp()V")
		public static void clearUp(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, StructureMorphExtraManager, "clearAll", "()V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.blockbuster.network.Dispatcher$1")
	public static class DispatcherInnerClassPatcher {
		
		@Patcher.Method("register()V")
		public static void register(MethodNode method) {
			// ClientHandlerStructure -> ClientHandlerStructureBiome
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.LDC,
							node -> Type.getType(TypeClientHandlerStructure).equals(((LdcInsnNode)node).cst)
					),
					InsertPos.REPLACE,
					new LdcInsnNode(Type.getType(TypeClientHandlerStructureBiome))
			);
		}
		
	}
	
	// 暴雪粒子大修改
	@Patcher("mchorse.blockbuster_pack.morphs.SnowstormMorph")
	public static class SnowstormMorphPatcher {
		
		@Patcher.Method("render(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")
		public static void render(MethodNode method) {
			// this.lastUpdate = BedrockLibrary.lastUpdate; 禁用Scheme更新
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.GETFIELD),
					InsertPos.BEFORE,
					new InsnNode(Opcodes.DUP),
					new FieldInsnNode(Opcodes.GETSTATIC, BedrockLibrary, "lastUpdate", "J"),
					new FieldInsnNode(Opcodes.PUTFIELD, SnowstormMorph, "lastUpdate", "J")
			);
		}

		@Patcher.Method("fromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		public static void fromNBT(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "fromNBT", "(Lmchorse/blockbuster_pack/morphs/SnowstormMorph;Lnet/minecraft/nbt/NBTTagCompound;)V", false)
			);
		}
		
		@Patcher.Method("toNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		public static void toNBT(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "toNBT", "(Lmchorse/blockbuster_pack/morphs/SnowstormMorph;Lnet/minecraft/nbt/NBTTagCompound;)V", false)
			);
		}
		
		@Patcher.Method("copy(Lmchorse/metamorph/api/morphs/AbstractMorph;)V")
		public static void copy(MethodNode method) {
			AbstractInsnNode target = insertNode(method,
					queryNode(method,
							node -> node.getPrevious() != null,
							node -> node.getPrevious().getOpcode() == Opcodes.GETFIELD,
							node -> "scheme".equals(((FieldInsnNode)node.getPrevious()).name),
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "copy", "(Lmchorse/blockbuster_pack/morphs/SnowstormMorph;Lmchorse/blockbuster_pack/morphs/SnowstormMorph;)V", false)
			);
			method.instructions.remove(target.getPrevious());
		}
		
		@Patcher.Method("equals(Ljava/lang/Object;)Z")
		public static void equals(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKESPECIAL),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "equalsObj", "(ZLmchorse/blockbuster_pack/morphs/SnowstormMorph;Ljava/lang/Object;)Z", false)
			);
		}
		
		@Patcher.Method("canMerge(Lmchorse/metamorph/api/morphs/AbstractMorph;)Z")
		public static void canMerge(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "equals".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new InsnNode(Opcodes.POP2),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "merge", "(Lmchorse/blockbuster_pack/morphs/SnowstormMorph;Lmchorse/metamorph/api/morphs/AbstractMorph;)Z", false)
			);
		}
		
		@Patcher.Method("getScheme(Ljava/lang/String;)Lmchorse/blockbuster/client/particles/BedrockScheme;")
		public static void getScheme(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEINTERFACE
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, SnowstormMorphExtraManager, "getScheme", "(Ljava/util/Map;Ljava/lang/String;)Lmchorse/blockbuster/client/particles/BedrockScheme;", false)
			);
		}
		
	}
	
	// 色度方块不传播光照，其实跟没改没啥区别= =
	@Patcher("mchorse.blockbuster.CommonProxy")
	public static class CommonProxyPatcher {
		
		@Patcher.Method("preLoad(Lnet/minecraftforge/fml/common/event/FMLPreInitializationEvent;)V")
		public static void preLoad(MethodNode method) {
			// new BlockGreen() -> new BlockGreenExtra()
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.NEW,
							node -> BlockGreen.equals(((TypeInsnNode)node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, BlockGreenExtra)
			);
			insertNode(method,
					queryContinue(
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> "<init>".equals(((MethodInsnNode)node).name),
							node -> BlockGreen.equals(((MethodInsnNode)node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, BlockGreenExtra, "<init>", "()V", false)
			);
		}
		
	}
	
	// 模型方块不渲染阴影
	@Patcher("mchorse.blockbuster.client.render.tileentity.TileEntityModelRenderer")
	public static class TileEntityModelRendererPatcher {
		
		@Patcher.Method("render(Lmchorse/blockbuster/common/tileentity/TileEntityModel;DDDFIF)V")
		public static void render(MethodNode method) {
			LabelNode label = new LabelNode();
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 1),
					new FieldInsnNode(Opcodes.GETFIELD, TileEntityModel, "shadow", "Z"),
					new JumpInsnNode(Opcodes.IFNE, label),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "isShadowPass", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new InsnNode(Opcodes.RETURN),
					label,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
	}
	
	// 序列发生器增加洋葱皮与循环位移
	@Patcher("mchorse.blockbuster_pack.client.gui.GuiSequencerMorph")
	public static class GuiSequencerMorphPatcher {
		
		@Patcher.Method("<init>(Lnet/minecraft/client/Minecraft;)V")
		public static void constructor(MethodNode method) {
			insertNode(method, 
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW, 
							node -> GuiSequencerMorphPanel.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, GuiSequencerMorphExtraPanel)
			);
			insertNode(method, 
					queryContinue( 
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> GuiSequencerMorphPanel.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, GuiSequencerMorphExtraPanel, "<init>", "(Lnet/minecraft/client/Minecraft;Lmchorse/blockbuster_pack/client/gui/GuiSequencerMorph;)V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.blockbuster_pack.morphs.SequencerMorph")
	public static class SequencerMorphPatcher {
		
		@Patcher.Class
		public static void addExtraProp(ClassNode clazz) {
			clazz.visitField(Opcodes.ACC_PUBLIC, "asmExtraProps", "Lnyanli/hackersmorph/other/mchorse/blockbuster/common/manager/SequencerMorphManager$ExtraProps;", null, null).visitEnd();
		}

		@Patcher.Method("fromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		@Patcher.Method("toNBT(Lnet/minecraft/nbt/NBTTagCompound;)V")
		public static void nbt(MethodNode method) {
			insertNode(method, 
					method.instructions.getFirst(), 
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, method.name, "(Lmchorse/blockbuster_pack/morphs/SequencerMorph;Lnet/minecraft/nbt/NBTTagCompound;)V", false)
			);
		}
		

		@Patcher.Method("canMerge(Lmchorse/metamorph/api/morphs/AbstractMorph;)Z")
		public static void canMerge(MethodNode method) {
			insertNode(method, 
					method.instructions.getFirst(), 
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, "copy", "(Lmchorse/blockbuster_pack/morphs/SequencerMorph;Lmchorse/metamorph/api/morphs/AbstractMorph;)V", false)
			);
			// fix bug: the first entry will run twice after canMerge
			// this.duration = 0f;
			// this.updateMorph(this.timer);
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.ICONST_1,
							node -> node.getNext().getOpcode() == Opcodes.IRETURN
					),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new InsnNode(Opcodes.FCONST_0),
					new FieldInsnNode(Opcodes.PUTFIELD, SequencerMorph, "duration", "F")
//					new VarInsnNode(Opcodes.ALOAD, 0),
//					new InsnNode(Opcodes.DUP),
//					new FieldInsnNode(Opcodes.GETFIELD, SequencerMorph, "timer", "I"),
//					new InsnNode(Opcodes.I2F),
//					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, SequencerMorph, "updateMorph", "(F)V", false)
			);
		}
		
		@Patcher.Method("copy(Lmchorse/metamorph/api/morphs/AbstractMorph;)V")
		public static void copy(MethodNode method) {
			insertNode(method, 
					method.instructions.getFirst(), 
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, "copy", "(Lmchorse/blockbuster_pack/morphs/SequencerMorph;Lmchorse/metamorph/api/morphs/AbstractMorph;)V", false)
			);
		}
		
		@Patcher.Method("equals(Ljava/lang/Object;)Z")
		public static void equals(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKESPECIAL),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, "equalsObj", "(ZLmchorse/blockbuster_pack/morphs/SequencerMorph;Ljava/lang/Object;)Z", false)
			);
		}
		
		@Patcher.Method("render(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")
		public static void render(MethodNode method) {
			insertNode(method, 
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "render".equals(((MethodInsnNode)node).name),
							node -> AbstractMorph.equals(((MethodInsnNode)node).owner),
							node -> "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V".equals(((MethodInsnNode)node).desc)
					), 
					InsertPos.REPLACE, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, "render", "(Lmchorse/metamorph/api/morphs/AbstractMorph;Lnet/minecraft/entity/EntityLivingBase;DDDFFLmchorse/blockbuster_pack/morphs/SequencerMorph;)V", false)
			);
		}
		
		@Patcher.Method("pause(Lmchorse/metamorph/api/morphs/AbstractMorph;I)V")
		public static void pause(MethodNode method) {
			insertNode(method, 
					method.instructions.getFirst(), 
					InsertPos.AFTER, 
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ILOAD, 2),
					new MethodInsnNode(Opcodes.INVOKESTATIC, SequencerMorphManager, "pause", "(Lmchorse/blockbuster_pack/morphs/SequencerMorph;I)V", false)
			);
		}
		
		// DANGER CODE
		@Patcher.Method("updateMorph(F)V")
		public static void updateMorph(MethodNode method) {
			AbstractInsnNode node = queryNode(method, n -> n.getOpcode() == Opcodes.FCONST_0);
			node = insertNode(method,
					node,
					InsertPos.REPLACE,
					new LdcInsnNode(new Float(0.1f))
			).getNext();
			node = insertNode(method,
					node,
					InsertPos.REPLACE,
					new InsnNode(Opcodes.FCMPG)
			).getNext();
			insertNode(method,
					node,
					InsertPos.REPLACE,
					new JumpInsnNode(Opcodes.IFGT, ((JumpInsnNode)node).label)
			);
		}
		
	}
	
	// 回放编辑器 伪装动作编辑时渲染场景
	// 也可以监听ActionPanelRegisterEvent然后替换
	@Patcher("mchorse.blockbuster.client.gui.dashboard.panels.recording_editor.GuiRecordingEditorPanel")
	public static class GuiRecordingEditorPanelPatcher {
		
		@Patcher.Method("open()V")
		public static void open(MethodNode method) {
			insertNode(method, 
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW, 
							node -> GuiMorphActionPanel.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, GuiMorphActionExtraPanel)
			);
			insertNode(method, 
					queryContinue( 
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> GuiMorphActionPanel.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, GuiMorphActionExtraPanel, "<init>", "(Lnet/minecraft/client/Minecraft;Lmchorse/blockbuster/client/gui/dashboard/panels/recording_editor/GuiRecordingEditorPanel;)V", false)
			);
		}
		
		// call disappear to finish the GuiCreativeMorphsMenu
		@Patcher.Method("selectRecord(Lmchorse/blockbuster/recording/data/Record;)V")
		public static void selectRecord(MethodNode method) {
			LabelNode label = new LabelNode();
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new JumpInsnNode(Opcodes.IFNULL, label),
					
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new TypeInsnNode(Opcodes.CHECKCAST, GuiActionPanel),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiActionPanel, "disappear", "()V", false),
					
					label,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
		// call disappear to finish the GuiCreativeMorphsMenu
		@Patcher.Method("removeAction()V")
		public static void removeAction(MethodNode method) {
			LabelNode label = new LabelNode();
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new JumpInsnNode(Opcodes.IFNULL, label),
					
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new TypeInsnNode(Opcodes.CHECKCAST, GuiActionPanel),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiActionPanel, "disappear", "()V", false),
					
					label,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
//		// clear record field after close
//		@Patcher.Method("close()V")
//		public static void close(MethodNode method) {
//			insertNode(method,
//					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
//					InsertPos.BEFORE,
//					new VarInsnNode(Opcodes.ALOAD, 0),
//					new InsnNode(Opcodes.ACONST_NULL),
//					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiRecordingEditorPanel, "selectRecord", "(Lmchorse/blockbuster/recording/data/Record;)V", false)
//			);
//		}
		
		@Patcher.Method("appear()V")
		public static void appear(MethodNode method) {
//			if (this.record != null) {
//				if (this.editor.delegate != null) {
//					this.editor.delegate.disappear();
//					this.editor.setDelegate(null);
//				}
//				this.selectRecord(this.record.filename);
//			}
			LabelNode label1 = new LabelNode(), label2 = new LabelNode();
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "record", "Lmchorse/blockbuster/recording/data/Record;"),
					new JumpInsnNode(Opcodes.IFNULL, label1),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new JumpInsnNode(Opcodes.IFNULL, label2),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new InsnNode(Opcodes.DUP),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new FieldInsnNode(Opcodes.GETFIELD, GuiDelegateElement, "delegate", "Lmchorse/mclib/client/gui/framework/elements/GuiElement;"),
					new TypeInsnNode(Opcodes.CHECKCAST, GuiActionPanel),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiActionPanel, "disappear", "()V", false),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "editor", "Lmchorse/mclib/client/gui/framework/elements/GuiDelegateElement;"),
					new InsnNode(Opcodes.ACONST_NULL),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiDelegateElement, "setDelegate", "(Lmchorse/mclib/client/gui/framework/elements/GuiElement;)V", false),
					label2,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new InsnNode(Opcodes.DUP),
					new FieldInsnNode(Opcodes.GETFIELD, GuiRecordingEditorPanel, "record", "Lmchorse/blockbuster/recording/data/Record;"),
					new FieldInsnNode(Opcodes.GETFIELD, Record, "filename", "Ljava/lang/String;"),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GuiRecordingEditorPanel, "selectRecord", "(Ljava/lang/String;)V", false),
					label1,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
	}
	
	// 优化预览，获取帧上最后一个伪装动作，而不是第一个
	@Patcher("mchorse.blockbuster.recording.data.Record")
	public static class RecordPatcher {
		
//		@Patcher.Method("seekMorphAction(I)Lmchorse/blockbuster/recording/data/Record$FoundAction;")
//		public static void seekMorphAction(MethodNode method) {
//			insertNode(method,
//					queryNode(method,
//							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
//							node -> "set".equals(((MethodInsnNode)node).name),
//							node -> FoundAction.equals(((MethodInsnNode)node).owner)
//					),
//					InsertPos.REPLACE,
//					new VarInsnNode(Opcodes.ALOAD, 0),
//					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "seekMorphAction", "(Lmchorse/blockbuster/recording/data/Record$FoundAction;ILmchorse/blockbuster/recording/actions/MorphAction;Lmchorse/blockbuster/recording/data/Record;)V", false)
//			);
//		}
		
		@Patcher.Method("applyPreviousMorph(Lnet/minecraft/entity/EntityLivingBase;Lmchorse/blockbuster/recording/scene/Replay;ILmchorse/blockbuster/recording/data/Record$MorphType;)V")
		public static void applyPreviousMorph(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "seekMorphAction".equals(((MethodInsnNode)node).name),
							node -> Record.equals(((MethodInsnNode)node).owner)
							),
					InsertPos.REPLACE,
					new InsnNode(Opcodes.ICONST_0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "seekMorphAction", "(Lmchorse/blockbuster/recording/data/Record;IZ)Lmchorse/blockbuster/recording/data/Record$FoundAction;", false)
			);
			insertNode(method,
					queryContinue(
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "seekMorphAction".equals(((MethodInsnNode)node).name),
							node -> Record.equals(((MethodInsnNode)node).owner)
							),
					InsertPos.REPLACE,
					new InsnNode(Opcodes.ICONST_1),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "seekMorphAction", "(Lmchorse/blockbuster/recording/data/Record;IZ)Lmchorse/blockbuster/recording/data/Record$FoundAction;", false)
			);
		}
		
	}
	
	// 演员不渲染阴影
	@Patcher("mchorse.blockbuster.client.render.RenderActor")
	public static class RenderActorPatcher {
		
		@Patcher.Method("doRender(Lmchorse/blockbuster/common/entity/EntityActor;DDDFF)V")
		public static void render(MethodNode method) {
			String getCustomNameTag = isDeobfEnv ? "getCustomNameTag" : "func_95999_t";
			LabelNode label = new LabelNode();
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EntityActor, getCustomNameTag, "()Ljava/lang/String;", false),
					new JumpInsnNode(Opcodes.IFNULL, label),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EntityActor, getCustomNameTag, "()Ljava/lang/String;", false),
					new LdcInsnNode("#ghost"),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "isShadowPass", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new InsnNode(Opcodes.RETURN),
					label,
					new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			);
		}
		
	}
	
	// Aperture播放SequencerMorph优化
	@Patcher("mchorse.blockbuster.recording.RecordPlayer")
	public static class RecordPlayerPatcher {
		
		@Patcher.Method("resume(I)V")
		public static void resume(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "applyPreviousMorph".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "playerResume", "(Lmchorse/blockbuster/recording/data/Record;Lnet/minecraft/entity/EntityLivingBase;Lmchorse/blockbuster/recording/scene/Replay;ILmchorse/blockbuster/recording/data/Record$MorphType;)V", false)
			);
		}
		
		@Patcher.Method("goTo(IZ)V")
		public static void goTo(MethodNode method) {
			MethodInsnNode target = (MethodInsnNode)queryNode(method,
					node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
					node -> "applyFrame".equals(((MethodInsnNode)node).name),
					node -> Record.equals(((MethodInsnNode)node).owner)
			);
			String desc = target.desc.replace("(", "(Lmchorse/blockbuster/recording/data/Record;").replace(")V", "Lmchorse/blockbuster/recording/RecordPlayer;)V");
			insertNode(method,
					target,
					InsertPos.REPLACE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "playerGoTo", desc, false)
			);
		}
		
	}
	
	// Aperture播放SequencerMorph优化
	@Patcher("mchorse.blockbuster.common.entity.EntityActor")
	public static class EntityActorPatcher {
		
		@Patcher.Method("applyModifyPacket(Lmchorse/blockbuster/network/common/PacketModifyActor;)V")
		public static void applyModifyPacket(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "applyPause".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "actorApplyModifyPacketPause", "(Lmchorse/blockbuster/common/entity/EntityActor;Lmchorse/metamorph/api/morphs/AbstractMorph;ILmchorse/metamorph/api/morphs/AbstractMorph;I)V", false)
			);
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "modify".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "actorApplyModifyPacketModify", "(Lmchorse/blockbuster/common/entity/EntityActor;Lmchorse/metamorph/api/morphs/AbstractMorph;ZZ)V", false)
			);
		}
		
		@Patcher.Method("readSpawnData(Lio/netty/buffer/ByteBuf;)V")
		public static void readSpawnData(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEINTERFACE,
							node -> "containsKey".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "actorReadSpawnData", "(Ljava/util/Map;Ljava/lang/String;)Z", false)
			);
		}
		
	}
	
	// Aperture强制同步
//	@Patcher("mchorse.blockbuster.network.client.recording.ClientHandlerSyncTick")
//	public static class ClientHandlerSyncTickPatcher {
//		
//		@Patcher.Method("run(Lnet/minecraft/client/entity/EntityPlayerSP;Lmchorse/blockbuster/network/common/recording/PacketSyncTick;)V")
//		public static void run(MethodNode method) {
//			insertNode(method,
//					method.instructions.getFirst(),
//					InsertPos.AFTER,
//					new VarInsnNode(Opcodes.ALOAD, 1),
//					new VarInsnNode(Opcodes.ALOAD, 2),
//					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "clientSyncTick", "(Lnet/minecraft/client/entity/EntityPlayerSP;Lmchorse/blockbuster/network/common/recording/PacketSyncTick;)V", false)
//			);
//		}
//		
//	}

	// Aperture开启Minema录制时清除客户端旧演员
	@Patcher("mchorse.blockbuster.aperture.CameraHandler")
	public static class CameraHandlerPatcher {
		
		@Patcher.Method("onCameraRewind(Lmchorse/aperture/events/CameraEditorEvent$Rewind;)V")
		public static void onCameraRewind(MethodNode method) {
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, CameraHandler, "get", "()Lmchorse/blockbuster/recording/scene/SceneLocation;", false),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "cameraOnRewind", "(Lmchorse/blockbuster/recording/scene/SceneLocation;)V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.blockbuster.network.client.ClientHandlerActorPause")
	public static class ClientHandlerActorPause {
		
		@Patcher.Method("run(Lnet/minecraft/client/entity/EntityPlayerSP;Lmchorse/blockbuster/network/common/PacketActorPause;)V")
		public static void run(MethodNode method) {
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 1),
					new VarInsnNode(Opcodes.ALOAD, 2),
					new MethodInsnNode(Opcodes.INVOKESTATIC, RecordExtraManager, "clientActorPause", method.desc, false)
			);
		}
		
	}
	
	@Patcher("mchorse.blockbuster.network.client.recording.ClientHandlerRequestedFrames")
	public static class ClientHandlerRequestedFramesPatcher {
		
		@Patcher.Method("run(Lnet/minecraft/client/entity/EntityPlayerSP;Lmchorse/blockbuster/network/common/recording/PacketRequestedFrames;)V")
		public static void run(MethodNode method) {
			LabelNode label = new LabelNode();
			insertNode(method,
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW,
							node -> Record.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.BEFORE,
					new FieldInsnNode(Opcodes.GETSTATIC, ClientProxy, "manager", "Lmchorse/blockbuster/recording/RecordManager;"),
					new FieldInsnNode(Opcodes.GETFIELD, RecordManager, "records", "Ljava/util/Map;"),
					new VarInsnNode(Opcodes.ALOAD, 2),
					new FieldInsnNode(Opcodes.GETFIELD, PacketRequestedFrames, "filename", "Ljava/lang/String;"),
					new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true),
					new TypeInsnNode(Opcodes.CHECKCAST, Record),
					new InsnNode(Opcodes.DUP),
					new JumpInsnNode(Opcodes.IFNONNULL, label),
					new InsnNode(Opcodes.POP)
			);
			insertNode(method,
					queryContinue(
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> Record.equals(((MethodInsnNode) node).owner),
							node -> "<init>".equals(((MethodInsnNode) node).name)
					),
					InsertPos.AFTER,
					label,
					new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] { Record })
			);
		}
		
	}
	
}
