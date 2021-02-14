package nyanli.hackersmorph.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.HackersMorphASM;

public class ClassTransformer implements IClassTransformer {
	
	public static final boolean isDeobfEnv;
	private static AbstractInsnNode cache = null;

	static {
		boolean dev = false;
		try {
			dev = FMLLaunchHandler.isDeobfuscatedEnvironment();
		} catch (NoSuchMethodError e) {
			ClassLoader cl = ClassTransformer.class.getClassLoader();
			if (cl instanceof LaunchClassLoader) {
				LaunchClassLoader loader = (LaunchClassLoader) cl;
				try {
					dev = loader.getClassBytes("net.minecraft.world.World") != null;
				} catch (IOException e1) {}
			}
		}
		isDeobfEnv = dev;
	}
	
	@SafeVarargs
	public static AbstractInsnNode queryNode(MethodNode method, Predicate<AbstractInsnNode>...conditions) {
		return cache = query(method.instructions.getFirst(), conditions);
	}

	@SafeVarargs
	public static AbstractInsnNode queryContinue(Predicate<AbstractInsnNode>...conditions) {
		return cache = query(cache.getNext(), conditions);
	}
	
	/**
	 * @param pos REPLACE and AFTER will update the query position for queryContinue if target equals last query result.
	 * @return The first instruction of inserted nodes
	 */
	public static AbstractInsnNode insertNode(MethodNode method, AbstractInsnNode target, InsertPos pos, AbstractInsnNode...nodes) {
		if (nodes.length == 0) return null;
		InsnList list = new InsnList();
		for (AbstractInsnNode node : nodes) list.add(node);
		switch (pos) {
		case BEFORE:
			method.instructions.insertBefore(target, list);
			break;
		case REPLACE:
		case AFTER:
			method.instructions.insert(target, list);
			if (target == cache)
				cache = nodes[nodes.length - 1];
			break;
		}
		if (pos == InsertPos.REPLACE) {
			method.instructions.remove(target);
		}
		return nodes[0];
	}
	
	@SafeVarargs
	private static AbstractInsnNode query(AbstractInsnNode from, Predicate<AbstractInsnNode>...conditions) {
		if (from == null || conditions.length == 0)
			throw new RuntimeException("Unable to find target instraction, PATCH FAILED! ");
		Predicate<AbstractInsnNode> cond = conditions[0];
		for (int i = 1; i < conditions.length; i++) cond = cond.and(conditions[i]);
		AbstractInsnNode node = null;
		for (node = from; node != null; node = node.getNext()) {
			if (cond.test(node))
				break;
		}
		if (node == null)
			throw new RuntimeException("Unable to find target instraction, PATCH FAILED! ");
		
		return node;
	}
	
	private static void saveDebugClass(String id, String className, byte[] bytes) {
        final File outFile = new File(new File(String.format("%s%c%s", "asmdebug", File.separatorChar, id)), className.replace('.', File.separatorChar) + ".class");
        final File outDir = outFile.getParentFile();
        if (!outDir.exists())
            outDir.mkdirs();
        if (outFile.exists())
            outFile.delete();
        try {
        	FileOutputStream output = new FileOutputStream(outFile);
            output.write(bytes);
            output.close();
        } catch (IOException ex) {
        	HackersMorphASM.LOGGER.catching(Level.INFO, ex);
        }
	}
	
	private HashMap<String, HashMap<String, Method>> methodPatchers = new HashMap<>();
	private HashMap<String, Method> classPatchers = new HashMap<>();
	private HashSet<String> classSet = new HashSet<>();

	public ClassTransformer() {
		for (Class<?> clazz : this.getClass().getClasses()) {
			if (Modifier.isStatic(clazz.getModifiers()) && clazz.isAnnotationPresent(Patcher.class)) {
				Patcher patcher = clazz.getAnnotation(Patcher.class);
				if (!patcher.value().isEmpty()) {
					HashMap<String, Method> methodMap = new HashMap<String, Method>();
					methodPatchers.put(patcher.value(), methodMap);
					for (Method method : clazz.getMethods()) {
						if (Modifier.isStatic(method.getModifiers())) {
							Patcher.Class cp = method.getAnnotation(Patcher.Class.class);
							Patcher.Method[] mps = method.getAnnotationsByType(Patcher.Method.class);
							Class<?>[] params = method.getParameterTypes();
							if (cp != null)
								if (params.length == 1 && params[0].equals(ClassNode.class))
									classPatchers.put(patcher.value(), method);
							if (mps.length > 0)
								if (params.length == 1 && params[0].equals(MethodNode.class))
									for (Patcher.Method mp : mps)
										if (!mp.value().isEmpty())
											methodMap.put(mp.value(), method);
						}
					}
				}
			}
		}
		classSet.addAll(classPatchers.keySet());
		classSet.addAll(methodPatchers.keySet());
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (classSet.contains(name))
			return doPatch(basicClass, name, transformedName);
		else if (classSet.contains(transformedName))
			return doPatch(basicClass, transformedName, transformedName);
		return basicClass;
	}
	
	private byte[] doPatch(byte[] basicClass, String matchedClass, String transformedName) {
		ClassReader cr = new ClassReader(basicClass);
		ClassNode node = new ClassNode();
		cr.accept(node, 0);
		try {
			if (classPatchers.containsKey(matchedClass)) {
				HackersMorphASM.LOGGER.debug("Patching Class {}({})", transformedName, matchedClass);
				classPatchers.get(matchedClass).invoke(null, node);
				cache = null;
			}
			if (methodPatchers.containsKey(matchedClass)) {
				HashMap<String, Method> methodMap = methodPatchers.get(matchedClass);
				for (MethodNode method : node.methods) {
					String key = String.format("%s%s", method.name, method.desc);
					if (methodMap.containsKey(key)) {
						HackersMorphASM.LOGGER.debug("Patching Method {}.{}({})", transformedName, key, matchedClass);
						methodMap.get(key).invoke(null, method);
						cache = null;
					} else if (methodMap.containsKey(method.name)) {
						HackersMorphASM.LOGGER.debug("Patching Method {}.{}({})", transformedName, key, matchedClass);
						methodMap.get(method.name).invoke(null, method);
						cache = null;
					}
				}
			}
		} catch (Throwable e) {
			HackersMorph.throwCommonException(e);
		}
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(cw);
		byte[] bytes = cw.toByteArray();
		if (isDeobfEnv)
			saveDebugClass(HackersMorphASM.COREMODID, cr.getClassName().replace('/', '.'), bytes);
		return bytes;
	}
	
	public static enum InsertPos {
		BEFORE,
		REPLACE,
		AFTER
	}

}
