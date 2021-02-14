package nyanli.hackersmorph.other.mchorse;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASM implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// 统一为UTF-8
		// 大部分地方都是用Charset.default()
		// 但只有模型编辑器读取模型的地方是写死的UTF-8
		// 统一UTF-8总比不同系统用不同编码好一些
		if (name.startsWith("mchorse.")) {
			ClassReader cr = new ClassReader(basicClass);
			ClassNode node = new ClassNode();
			cr.accept(node, 0);
			
			for (MethodNode method : node.methods) {
				for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
					if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
						MethodInsnNode mnode = (MethodInsnNode) insn;
						if ("defaultCharset".equals(mnode.name)
								&& "java/nio/charset/Charset".equals(mnode.owner)) {
							mnode.name = "forName";
							mnode.desc = "(Ljava/lang/String;)Ljava/nio/charset/Charset;";
							method.instructions.insertBefore(mnode, new LdcInsnNode("UTF-8"));
						}
					}
				}
			}
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			node.accept(cw);
			return cw.toByteArray();
		}
		return basicClass;
	}

}
