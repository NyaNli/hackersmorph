package nyanli.hackersmorph.other.optifine.client.shader;

import java.util.regex.Pattern;

import nyanli.hackersmorph.HackersMorph;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;
import nyanli.hackersmorph.other.optifine.router.shaders.config.ShaderOptionVariable;

public class ShaderOptionVariableUniform extends ShaderOptionVariable implements IOptionUniform {
	
	private final String uniformName;
	
	private final String constChecker;
	private final String defineChecker;

	private Type type;
	private boolean delConst = false;
	private String lastLine = null;

	public ShaderOptionVariableUniform(String name, String description, String value, String[] values, String path) {
		super(name, description, value, values, path);
		this.uniformName = "def" + name;
		
		this.constChecker = String.format("\\s*const .*=(:?.*\\W)?%s(:?\\W.*)?", name);
		this.defineChecker = String.format("\\s*#(:?if|IF|elif|ELIF) \\W*%s(:?\\W.*)?", name);
		
		if (value != null && values.length > 1) {
			boolean canInt = true;
			boolean canFloat = true;
			for (String val : values) {
				canInt = canInt && checkInt(val);
				canFloat = canFloat && checkFloat(val);
			}
			type = canInt ? Type.INT : (canFloat ? Type.FLOAT : null);
		} else {
			type = null;
		}
	}
	
	@Override
	public boolean matchesLine(String line) {
		boolean matched = super.matchesLine(line);
		if(!matched && this.isUniform()) {
			if (Pattern.matches(this.constChecker, line)) {
				this.delConst = true;
				this.lastLine = line.replaceFirst("const ", "") + " // Remove const for " + this.getName();
				return true;
			} else if (Pattern.matches(this.defineChecker, line)) {
				HackersMorph.LOGGER.info("Shader's option {} is not variable.", this.getName());
				this.type = null;
				ShaderManager.requestReload();
			}
		}
		return matched;
	}

	@Override
	public String getSourceLine() {
		if (this.isUniform()) {
			if (this.delConst) {
				this.delConst = false;
				return this.lastLine;
			}
			ShaderManager.addOptionUniform(this);
			return "#define " + this.getName() + " " + this.getUniformName() + " // " + super.getSourceLine();
		}
		return super.getSourceLine();
	}
	
	@Override
	public boolean isChanged() {
		return this.isUniform() || super.isChanged();
	}

	@Override
	public String getUniformName() {
		return this.uniformName;
	}

	@Override
	public Type getUniformType() {
		return type;
	}

	@Override
	public boolean isUniform() {
		return this.type != null && HackersMorph.getConfig().canConvertDefine();
	}
	
	private boolean checkInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private boolean checkFloat(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}

