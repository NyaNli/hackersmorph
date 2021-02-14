package nyanli.hackersmorph.asm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Patcher {

	String value();

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Class {}
	
	/**
	 * @Usage @Patcher.Method("methodNameWithDesc(Ljava/lang/Object;)Z") or @Patcher.Method("methodNameOnly")
	 */
	@Documented
	@Repeatable(Methods.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Method {
		String value();
	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Methods {
		Method[] value();
	}
	
}
