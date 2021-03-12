package nyanli.hackersmorph.other.mchorse.emoticons;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {

	@Patcher("mchorse.emoticons.cda04eccaf059893b")
	public static class EmoticonsMorph {
		
		@Patcher.Class
		public static void addMethod(ClassNode clazz) {
			// Generate by Bytecode Outline
			MethodVisitor mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "afterMerge", "(Lmchorse/metamorph/api/morphs/AbstractMorph;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.INSTANCEOF, "mchorse/emoticons/cda04eccaf059893b");
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, l1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "mchorse/emoticons/cda04eccaf059893b", "animation", "Lmchorse/emoticons/c6b4499a686f3e59b;");
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "mchorse/emoticons/c6b4499a686f3e59b", "progress", "I");
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "mchorse/emoticons/cda04eccaf059893b", "animation", "Lmchorse/emoticons/c6b4499a686f3e59b;");
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, "mchorse/emoticons/cda04eccaf059893b");
			mv.visitFieldInsn(Opcodes.GETFIELD, "mchorse/emoticons/cda04eccaf059893b", "pose", "Lmchorse/emoticons/caa7b60d6518c09d1;");
			Label l4 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l4);
			mv.visitTypeInsn(Opcodes.NEW, "mchorse/emoticons/caa7b60d6518c09d1");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "mchorse/emoticons/caa7b60d6518c09d1", "<init>", "()V", false);
			Label l5 = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, l5);
			mv.visitLabel(l4);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"mchorse/emoticons/c6b4499a686f3e59b"});
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, "mchorse/emoticons/cda04eccaf059893b");
			mv.visitFieldInsn(Opcodes.GETFIELD, "mchorse/emoticons/cda04eccaf059893b", "pose", "Lmchorse/emoticons/caa7b60d6518c09d1;");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mchorse/emoticons/caa7b60d6518c09d1", "mc40653549738b38f", "()Lmchorse/emoticons/caa7b60d6518c09d1;", false);
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {"mchorse/emoticons/cda04eccaf059893b", "mchorse/metamorph/api/morphs/AbstractMorph"}, 2, new Object[] {"mchorse/emoticons/c6b4499a686f3e59b", "mchorse/emoticons/caa7b60d6518c09d1"});
			mv.visitFieldInsn(Opcodes.PUTFIELD, "mchorse/emoticons/c6b4499a686f3e59b", "last", "Lmchorse/emoticons/caa7b60d6518c09d1;");
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(Opcodes.RETURN);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLocalVariable("this", "Lmchorse/emoticons/cda04eccaf059893b;", null, l0, l6, 0);
			mv.visitLocalVariable("morph", "Lmchorse/metamorph/api/morphs/AbstractMorph;", null, l0, l6, 1);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
		}
		
	}
	
}
