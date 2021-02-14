package nyanli.hackersmorph.other.mchorse.mclib;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {
	
	private static final String GuiTrackpadElement = "mchorse/mclib/client/gui/framework/elements/input/GuiTrackpadElement";
	
	private static final String GuiRelativeRotation = "nyanli/hackersmorph/other/mchorse/mclib/gui/element/GuiRelativeRotation";
	
	// Fix a bug
	// maybe i should submit a issue
	@Patcher("mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement")
	public static class GuiTrackpadElementPatcher {
		
		@Patcher.Method("setValueAndNotify(D)V")
		public static void setValueAndNotify(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.INVOKESTATIC,
							node -> "valueOf".equals(((MethodInsnNode)node).name),
							node -> "java/lang/Double".equals(((MethodInsnNode)node).owner)
					),
					InsertPos.BEFORE,
					new InsnNode(Opcodes.POP2),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, GuiTrackpadElement, "value", "D")
			);
		}
		
	}
	
	// 2.2 GL Error 1283 1284 and for safe
	@Patcher("mchorse.mclib.client.InputRenderer")
	public static class InputRendererPatcher {
		
		@Patcher.Method("preRenderOverlay()V")
		@Patcher.Method("postRenderOverlay()V")
		public static void backupMatrix(MethodNode method) {
			Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
			while (iterator.hasNext()) {
				AbstractInsnNode node = iterator.next();
				if (node.getOpcode() == Opcodes.INVOKESTATIC) {
					MethodInsnNode m = (MethodInsnNode) node;
					switch (m.name) {
					case "func_179094_E":
					case "func_179121_F":
					case "pushMatrix":
					case "popMatrix":
						iterator.remove();
					}
				}
			}
			
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, "nyanli/hackersmorph/util/OpenGlMatrixHelper", "pushAllMatrix", "()V", false)
			);
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, "nyanli/hackersmorph/util/OpenGlMatrixHelper", "popAllMatrix", "()V", false)
			);
		}
		
	}
	
	@Patcher("mchorse.mclib.client.gui.framework.elements.input.GuiTransformations")
	public static class GuiTransformationsPatcher {
		
		@Patcher.Method("<init>(Lnet/minecraft/client/Minecraft;)V")
		public static void constructor(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, GuiRelativeRotation, "constructor", "(Lmchorse/mclib/client/gui/framework/elements/input/GuiTransformations;)V", false)
			);
		}
		
	}

}
