package nyanli.hackersmorph.other.optifine.client.shader;

public interface IOptionUniform {
	
	// isEnabled
	boolean isUniform();
	
	String getUniformName();
	
	Type getUniformType();

	// ShaderOption methods
	String getName();
	
	String getNameText();
	
	String getValue();
	
	public static enum Type {
		INT, FLOAT;
		
		public static boolean contains(String str) {
			if (str == null) return false;
			for (Type type : Type.values())
				if (type.toString().equals(str))
					return true;
			return false;
		}
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	
}
