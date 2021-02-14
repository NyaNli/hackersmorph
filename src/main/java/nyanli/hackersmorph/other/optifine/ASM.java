package nyanli.hackersmorph.other.optifine;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import nyanli.hackersmorph.asm.ClassTransformer;
import nyanli.hackersmorph.asm.Patcher;

public class ASM extends ClassTransformer {
	
	private static final String ShaderOptionVariable = "net/optifine/shaders/config/ShaderOptionVariable";
//	private static final String ShaderOptionVariableConst = "net/optifine/shaders/config/ShaderOptionVariableConst";
	
	private static final String ShaderManager = "nyanli/hackersmorph/other/optifine/client/manager/ShaderManager";
	private static final String ShaderOptionVariableUniform = "nyanli/hackersmorph/other/optifine/client/shader/ShaderOptionVariableUniform";
//	private static final String ShaderOptionVariableConstUniform = "nyanli/hackersmorph/other/optifine/client/shader/ShaderOptionVariableConstUniform";
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.startsWith("nyanli.hackersmorph.other.optifine.router"))
			return super.transform("OptifineRouter", transformedName, basicClass);
		return super.transform(name, transformedName, basicClass);
	}
	
	@Patcher("net.optifine.shaders.Shaders")
	public static class ShadersPatcher {
		
		@Patcher.Method("setProgramUniform1i(Lnet/optifine/shaders/uniform/ShaderUniform1i;I)V")
//		@Patcher.Method("setProgramUniform2i(Lnet/optifine/shaders/uniform/ShaderUniform2i;II)V")
		@Patcher.Method("setProgramUniform1f(Lnet/optifine/shaders/uniform/ShaderUniform1f;F)V")
//		@Patcher.Method("setProgramUniform3f(Lnet/optifine/shaders/uniform/ShaderUniform3f;FFF)V")
		public static void setProgramUniform(MethodNode method) {
			insertNode(method,
					queryNode(method, node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, method.name.replace("set", "hook"), "(Ljava/lang/Object;" + method.desc.substring(method.desc.length() - 3), false)
			);
		}
		
//		@Patcher.Method("loadShaderPackOptions()[Lnet/optifine/shaders/config/ShaderOption;")
//		public static void loadShaderPackOptions(MethodNode method) {
//			insertNode(method,
//					method.instructions.getFirst(),
//					InsertPos.BEFORE,
//					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "clearOptionUniforms", "()V", false)
//			);
//		}
		
		@Patcher.Method("useProgram(Lnet/optifine/shaders/Program;)V")
		public static void useProgram(MethodNode method) {
			insertNode(method,
					queryNode(method,
							node -> node.getOpcode() == Opcodes.LDC,
							node -> "end useProgram".equals(((LdcInsnNode)node).cst)
					),
					InsertPos.BEFORE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "updateOptionUniforms", "()V", false)
			);
		}
		
		@Patcher.Method("createFragShader(Lnet/optifine/shaders/Program;Ljava/lang/String;)I")
		@Patcher.Method("createGeomShader(Lnet/optifine/shaders/Program;Ljava/lang/String;)I")
		@Patcher.Method("createVertShader(Lnet/optifine/shaders/Program;Ljava/lang/String;)I")
		public static void createShader(MethodNode method) {
			queryNode(method,
					node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
					node -> "java/lang/StringBuilder".equals(((MethodInsnNode)node).owner),
					node -> "<init>".equals(((MethodInsnNode)node).name),
					node -> "(I)V".equals(((MethodInsnNode)node).desc)
			);
			int builder = ((VarInsnNode)queryContinue(node -> node.getOpcode() == Opcodes.ASTORE)).var;
			insertNode(method,
					queryContinue(
							node -> node.getOpcode() == Opcodes.GETSTATIC,
							node -> "net/optifine/shaders/Shaders".equals(((FieldInsnNode)node).owner),
							node -> "saveFinalShaders".equals(((FieldInsnNode)node).name)
					),
					InsertPos.BEFORE,
					new VarInsnNode(Opcodes.ALOAD, builder),
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "postProcessShader", "(Ljava/lang/StringBuilder;)V", false)
			);
		}
		
		@Patcher.Method("init()V")
		public static void init(MethodNode method) {;
			insertNode(method, 
					queryNode(method, node -> node.getOpcode() == Opcodes.RETURN),
					InsertPos.BEFORE,
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "afterInit", "()V", false)
			);
		}
		
//		@Patcher.Method("setCameraShadow(F)V")
//		public static void setCameraShadow(MethodNode method) {
//			insertNode(method,
//					queryNode(method, 
//							node -> node.getOpcode() == Opcodes.INVOKESTATIC,
//							node -> "glOrtho".equals(((MethodInsnNode)node).name)
//					),
//					InsertPos.REPLACE,
//					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "setShadowCameraOrtho", "(DDDDDD)V", false)
//			);
//		}
		
	}
	
	@Patcher("net.optifine.shaders.config.ShaderOptionVariable")
	public static class ShaderOptionVariablePatcher {
		
		@Patcher.Method("parseOption(Ljava/lang/String;Ljava/lang/String;)Lnet/optifine/shaders/config/ShaderOption;")
		public static void parseOption(MethodNode method) {
			insertNode(method, 
					queryNode(method, 
							node -> node.getOpcode() == Opcodes.NEW, 
							node -> ShaderOptionVariable.equals(((TypeInsnNode) node).desc)
					),
					InsertPos.REPLACE,
					new TypeInsnNode(Opcodes.NEW, ShaderOptionVariableUniform)
			);
			insertNode(method, 
					queryContinue( 
							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
							node -> ShaderOptionVariable.equals(((MethodInsnNode) node).owner)
					),
					InsertPos.REPLACE,
					new MethodInsnNode(Opcodes.INVOKESPECIAL, ShaderOptionVariableUniform, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)V", false)
			);
		}
		
	}

// Const Options cannot modify in real time
// they are used for Optifine, not GLSL
//	@Patcher("net.optifine.shaders.config.ShaderOptionVariableConst")
//	public static class ShaderOptionVariableConstPatcher {
//		
//		@Patcher.Method("parseOption(Ljava/lang/String;Ljava/lang/String;)Lnet/optifine/shaders/config/ShaderOption;")
//		public static void parseOption(MethodNode method) {
//			insertNode(method, 
//					queryNode(method, 
//							node -> node.getOpcode() == Opcodes.NEW, 
//							node -> ShaderOptionVariableConst.equals(((TypeInsnNode) node).desc)
//					),
//					InsertPos.REPLACE,
//					new TypeInsnNode(Opcodes.NEW, ShaderOptionVariableConstUniform)
//			);
//			insertNode(method, 
//					queryContinue( 
//							node -> node.getOpcode() == Opcodes.INVOKESPECIAL,
//							node -> ShaderOptionVariableConst.equals(((MethodInsnNode) node).owner)
//					),
//					InsertPos.REPLACE,
//					new MethodInsnNode(Opcodes.INVOKESPECIAL, ShaderOptionVariableConstUniform, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)V", false)
//			);
//		}
//		
//	}
	
	@Patcher("net.optifine.shaders.uniform.ShaderUniforms")
	public static class ShaderUniformsPatcher {
		
		@Patcher.Method("reset()V")
		public static void reset(MethodNode method) {
			insertNode(method,
					method.instructions.getFirst(),
					InsertPos.AFTER,
					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "resetOptionUniforms", "()V", false)
			);
		}
		
	}
	
	@Patcher("OptifineRouter")
	public static class RouterPatcher {
		
		@Patcher.Class
		public static void patch(ClassNode clazz) {
			String route = clazz.name.replace("nyanli/hackersmorph/other/optifine/router", "net/optifine");
			clazz.superName = route;
			clazz.fields.clear();
			Iterator<MethodNode> iterator = clazz.methods.iterator();
			while (iterator.hasNext()) {
				MethodNode node = iterator.next();
				if ("<init>".equals(node.name))
					patchInit(node, route);
				else
					iterator.remove();
			}
		}
		
		private static void patchInit(MethodNode method, String route) {
			method.maxStack = method.maxLocals;
			while (method.instructions.get(1) != method.instructions.getLast()) method.instructions.remove(method.instructions.get(1));
			for (LocalVariableNode local : method.localVariables)
				method.instructions.insertBefore(method.instructions.getLast(), new VarInsnNode(getLoad(local.desc), local.index));
			method.instructions.insertBefore(method.instructions.getLast(), new MethodInsnNode(Opcodes.INVOKESPECIAL, route, method.name, method.desc, false));
			method.instructions.insertBefore(method.instructions.getLast(), new InsnNode(Opcodes.RETURN));
		}
		
		private static int getLoad(String type) {
			switch (type) {
			case "Z":
			case "B":
			case "C":
			case "S":
			case "I":
				return Opcodes.ILOAD;
			case "J":
				return Opcodes.LLOAD;
			case "F":
				return Opcodes.FLOAD;
			case "D":
				return Opcodes.DLOAD;
			default:
				return Opcodes.ALOAD;
			}
		}
		
	}
	
//	@Patcher("net.optifine.shaders.ShadersRender")
//	public static class ShadersRenderPatcher {
//		
//		@Patcher.Method("renderShadowMap(Lbuq;IFJ)V")
//		public static void renderShadowMap(MethodNode method) {
//			insertNode(method,
//					queryNode(method,
//							node -> node.getOpcode() == Opcodes.INVOKESTATIC,
//							node -> "k".equals(((MethodInsnNode) node).name),
//							node -> "()V".equals(((MethodInsnNode) node).desc),
//							node -> "bus".equals(((MethodInsnNode) node).owner)
//					),
//					InsertPos.REPLACE,
//					new MethodInsnNode(Opcodes.INVOKESTATIC, ShaderManager, "shadowDisableCull", "()V", false)
//			);
//		}
//		
//	}
	
}
