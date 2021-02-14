package nyanli.hackersmorph.other.mchorse.metamorph;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {
	
private static final String GuiBodyPartEditor = "mchorse/metamorph/bodypart/GuiBodyPartEditor";
	
	private static final String BodyPartMatrixManager = "nyanli/hackersmorph/other/mchorse/metamorph/client/manager/BodyPartMatrixManager";
	private static final String OnionSkinManager = "nyanli/hackersmorph/other/mchorse/metamorph/client/manager/OnionSkinManager";

	@Patcher("mchorse.metamorph.bodypart.BodyPart")
	public static class BodyPartPatcher {
		
		@Patcher.Method("render(Lnet/minecraft/entity/EntityLivingBase;F)V")
		@Patcher.Method("render(Lmchorse/metamorph/api/morphs/AbstractMorph;Lnet/minecraft/entity/EntityLivingBase;F)V") // 2.2
		public static void render(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKESTATIC,
							node -> "glPushMatrix".equals(((MethodInsnNode)node).name)
					),
					InsertPos.REPLACE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, BodyPartMatrixManager, "glPushMatrix", "(Lmchorse/metamorph/bodypart/BodyPart;)V", false)
			);
//			insertNode(method,
//					queryContinue(
//							node -> node.getOpcode() == Opcodes.INVOKESTATIC,
//							node -> "glPopMatrix".equals(((MethodInsnNode)node).name)
//					),
//					InsertPos.REPLACE,
//					new VarInsnNode(Opcodes.ALOAD, 0),
//					new MethodInsnNode(Opcodes.INVOKESTATIC, BodyPartMatrixManager, "glPopMatrix", "(Lmchorse/metamorph/bodypart/BodyPart;)V", false)
//			);
		}
		
	}
	
	@Patcher("mchorse.metamorph.bodypart.GuiBodyPartEditor")
	public static class GuiBodyPartEditorPatcher {
		
		@Patcher.Class
		public static void addMethod(ClassNode clazz) {
			MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "finishEditing", "()V", null, null);
			mv.visitCode();
			Label begin = new Label();
			mv.visitLabel(begin);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "mchorse/metamorph/client/gui/editor/GuiMorphPanel", "finishEditing", "()V", false);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "nyanli/hackersmorph/other/mchorse/metamorph/client/manager/BodyPartMatrixManager", "clearCurrent", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			Label end = new Label();
			mv.visitLabel(end);
			mv.visitLocalVariable("this", "Lmchorse/metamorph/bodypart/GuiBodyPartEditor;", null, begin, end, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		
		@Patcher.Method("pickLimb(Ljava/lang/String;)V")
		public static void pickLimb(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.GETFIELD,
							node -> "part".equals(((FieldInsnNode)node).name),
							node -> "Lmchorse/metamorph/bodypart/BodyPart;".equals(((FieldInsnNode)node).desc)
					),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, BodyPartMatrixManager, "onPickLimb", "(Lmchorse/metamorph/bodypart/BodyPart;)Lmchorse/metamorph/bodypart/BodyPart;", false)
			);
		}
		
		@Patcher.Method("draw(Lmchorse/mclib/client/gui/framework/elements/utils/GuiContext;)V")
		public static void draw(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> "draw".equals(((MethodInsnNode)node).name)
					),
					InsertPos.AFTER,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new InsnNode(Opcodes.DUP),
					new FieldInsnNode(Opcodes.GETFIELD, GuiBodyPartEditor, "part", "Lmchorse/metamorph/bodypart/BodyPart;"),
					new MethodInsnNode(Opcodes.INVOKESTATIC, BodyPartMatrixManager, "onGuiDraw", "(Lmchorse/metamorph/bodypart/GuiBodyPartEditor;Lmchorse/metamorph/bodypart/BodyPart;)V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.mclib.client.gui.framework.elements.GuiModelRenderer")
	public static class GuiModelRendererPatcher {
		
		@Patcher.Method("drawModel(Lmchorse/mclib/client/gui/framework/elements/utils/GuiContext;)V")
		public static void drawModel(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL,
							node -> "drawUserModel".equals(((MethodInsnNode)node).name),
							node -> "mchorse/mclib/client/gui/framework/elements/GuiModelRenderer".equals(((MethodInsnNode)node).owner)
					),
					InsertPos.BEFORE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, OnionSkinManager, "beforeRenderModel", "(Lmchorse/mclib/client/gui/framework/elements/GuiModelRenderer;Lmchorse/mclib/client/gui/framework/elements/utils/GuiContext;)V", false),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 1)
			);
		}
		
	}
	
	@Patcher("mchorse.metamorph.client.gui.creative.GuiCreativeMorphsList")
	public static class GuiCreativeMorphsListPatcher {
		
		@Patcher.Method("nestEdit(Lmchorse/metamorph/api/morphs/AbstractMorph;ZLjava/util/function/Consumer;)V")
		public static void nestEdit(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, OnionSkinManager, "push", "()V", false)
			);
		}
		
		@Patcher.Method("restoreEdit()V")
		public static void restoreEdit(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, OnionSkinManager, "pop", "()V", false)
			);
		}
		
	}
	
}
