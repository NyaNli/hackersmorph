package nyanli.hackersmorph.other.mchorse.aperture;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {
	
	private static final String GuiCurves = "mchorse/aperture/client/gui/GuiCurves";
//	private static final String AbstractFixture = "mchorse/aperture/camera/fixtures/AbstractFixture";
	private static final String CameraProfile = "mchorse/aperture/camera/CameraProfile";
	private static final String GuiCameraEditor = "mchorse/aperture/client/gui/GuiCameraEditor";
	
	private static final String CameraEditorManager = "nyanli/hackersmorph/other/mchorse/aperture/client/manager/CameraEditorManager";
	private static final String GuiMoreCurves = "nyanli/hackersmorph/other/mchorse/aperture/client/gui/GuiMoreCurves";
	private static final String CameraMorphFixture = "nyanli/hackersmorph/other/mchorse/aperture/client/camera/fixture/CameraMorphFixture";
	
	@Patcher("mchorse.aperture.client.gui.GuiCameraEditor")
	public static class GuiCameraEditorPatcher {
		
		@Patcher.Method("updateCameraEditor(Lnet/minecraft/entity/player/EntityPlayer;)V")
		public static void updateCameraEditor(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, CameraEditorManager, "onGuiOpen", "(Lmchorse/aperture/client/gui/GuiCameraEditor;)V", false)
			);
		}
		
		@Patcher.Method("closeScreen()V")
		public static void closeScreen(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, CameraEditorManager, "onGuiClose", "()V", false)
			);
		}

		@Patcher.Method("func_73863_a(IIF)V")
		@Patcher.Method("drawScreen(IIF)V")
		public static void drawScreen(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new InsnNode(Opcodes.FCONST_0),
					new FieldInsnNode(Opcodes.PUTFIELD, GuiCameraEditor, "lastPartialTick", "F")
			);
		}
		
	}
	
	@Patcher("mchorse.aperture.client.gui.GuiProfilesManager")
	public static class GuiProfilesManagerPatcher {
		
		@Patcher.Method("<init>(Lnet/minecraft/client/Minecraft;Lmchorse/aperture/client/gui/GuiCameraEditor;)V")
		public static void constructor(MethodNode method) {
			insertNode(method, 
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW, 
							node -> GuiCurves.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, GuiMoreCurves)
			);
			insertNode(method, 
					queryContinue( 
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> GuiCurves.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, GuiMoreCurves, "<init>", "(Lnet/minecraft/client/Minecraft;Lmchorse/aperture/client/gui/GuiCameraEditor;)V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.aperture.camera.CameraProfile")
	public static class CameraProfilePatcher {
		
		@Patcher.Method("applyCurves(JF)V")
		public static void applyCurves(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, "mchorse/aperture/camera/CameraProfile", "curves", "Ljava/util/Map;"),
					new VarInsnNode(Opcodes.LLOAD, 1),
					new VarInsnNode(Opcodes.FLOAD, 3),
					new MethodInsnNode(Opcodes.INVOKESTATIC, CameraEditorManager, "onApplyCurves", "(Ljava/util/Map;JF)V", false)
			);
		}
		
		@Patcher.Method("copyFrom(Lmchorse/aperture/camera/CameraProfile;)V")
		public static void copyFrom(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1),
					new FieldInsnNode(Opcodes.GETFIELD, CameraProfile, "curves", "Ljava/util/Map;"),
					new FieldInsnNode(Opcodes.PUTFIELD, CameraProfile, "curves", "Ljava/util/Map;")
			);
		}
		
	}
	
	@Patcher("mchorse.aperture.camera.CameraRenderer")
	public static class CameraRendererPatcher {
		
		@Patcher.Method("onLastRender(Lnet/minecraftforge/client/event/RenderWorldLastEvent;)V")
		public static void onLastRender(MethodNode method) {
			queryNode(method,
					node -> node.getOpcode() == Opcodes.INVOKEINTERFACE,
					node -> "next".equals(((MethodInsnNode)node).name),
					node -> "java/util/Iterator".equals(((MethodInsnNode)node).owner)
			);
			VarInsnNode store = (VarInsnNode) queryContinue(node -> node.getOpcode() == Opcodes.ASTORE);
			AbstractInsnNode frame = store;
			while (!(frame instanceof FrameNode)) frame = frame.getPrevious();
			AbstractInsnNode label = frame;
			while (!(label instanceof LabelNode)) label = label.getPrevious();
			insertNode(method,
					store,
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, store.var),
					new TypeInsnNode(Opcodes.INSTANCEOF, CameraMorphFixture),
					new JumpInsnNode(Opcodes.IFNE, (LabelNode) label)
			);
		}
		
	}

}
