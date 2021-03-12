package nyanli.hackersmorph.other.mchorse.chameleon;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {
	
	@Patcher("mchorse.chameleon.metamorph.ChameleonMorph")
	public static class ChameleonMorphPatcher {
		
		@Patcher.Method("updateClient(Lnet/minecraft/entity/EntityLivingBase;)V")
		public static void updateClient(MethodNode method) {
			try {
				FieldInsnNode n = (FieldInsnNode) queryNode(method, 
						node -> node.getOpcode() == Opcodes.PUTFIELD,
						node -> "lastScale".equals(((FieldInsnNode) node).name)
				);
				n.name = "lastUpdate";
				n.desc = "J";
				method.instructions.remove(n.getPrevious());
			} catch (Exception e) {}
		}
		
	}

}
