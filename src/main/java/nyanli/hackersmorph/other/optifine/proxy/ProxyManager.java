package nyanli.hackersmorph.other.optifine.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.WeakHashMap;

public class ProxyManager {
	
	private static final HashMap<Class<?>, WeakHashMap<Object, Object>> cache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T> T generateProxy(Object obj, Class<T> itf) {
		if (!cache.containsKey(itf))
			cache.put(itf, new WeakHashMap<>());
		if (!cache.get(itf).containsKey(obj))
			cache.get(itf).put(obj, Proxy.newProxyInstance(ProxyManager.class.getClassLoader(), new Class[]{ itf }, new ProxyInvoker(obj)));
		return (T) cache.get(itf).get(obj);
	}
	
	private static class ProxyInvoker implements InvocationHandler {
		
		private Object target;
		
		public ProxyInvoker(Object target) {
			this.target = target;
		}

		@Override
		public Object invoke(Object instance, Method method, Object[] args) throws Throwable {
			return this.target.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(target, args);
		}
		
	}

}
