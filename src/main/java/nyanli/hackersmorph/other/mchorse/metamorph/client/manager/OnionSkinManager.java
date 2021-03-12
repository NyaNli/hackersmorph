package nyanli.hackersmorph.other.mchorse.metamorph.client.manager;

import java.util.ArrayList;
import java.util.Stack;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.shaders.Shader;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import nyanli.hackersmorph.other.minecraft.common.manager.RenderManager;

public class OnionSkinManager {
	
	private static final String VERT = "#version 120\nuniform vec4 onionskin;\nvarying vec4 color;\nvarying vec4 texcoord;\nvarying vec4 lmcoord;\nvoid main()\n{\ngl_Position=ftransform();\ncolor=gl_Color*onionskin;\ntexcoord=gl_TextureMatrix[0]*gl_MultiTexCoord0;\nlmcoord=gl_TextureMatrix[1]*gl_MultiTexCoord1;\n}";
	private static final String FRAG = "#version 120\nuniform sampler2D texture;\nuniform sampler2D lightmap;\nvarying vec4 color;\nvarying vec4 texcoord;\nvarying vec4 lmcoord;\nvoid main()\n{\nvec4 lm=texture2D(lightmap,lmcoord.st);\ngl_FragColor=mix(vec4(1),lm,lm.a)*texture2D(texture,texcoord.st)*color;\n}";
	
	private static ArrayList<OnionSkin> current = new ArrayList<>();
	private static ArrayList<OnionSkin> last = null;
	
	private static Stack<ArrayList<OnionSkin>> stack = new Stack<>();
	
	private static Shader shader;
	private static int uniform = -1;
	
	static {
		try {
			shader = new Shader();
			shader.compile(VERT, FRAG, true);
			uniform = GL20.glGetUniformLocation(shader.programId, "onionskin");
			GL20.glUniform1i(GL20.glGetUniformLocation(shader.programId, "texture"), 0);
			GL20.glUniform1i(GL20.glGetUniformLocation(shader.programId, "lightmap"), 1);
		} catch (Exception e) {
			shader = null;
		}
	}
	
	public static void afterRenderModel(GuiModelRenderer renderer, GuiContext context) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(1f, 1f);
		int program = -1;
		if (shader != null) {
			program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			GL20.glUseProgram(shader.programId);
		}
		if (last != null)
			for (OnionSkin skin : last)
				renderOnionSkin(renderer, skin);
		if (current != null)
			for (OnionSkin skin : current)
				renderOnionSkin(renderer, skin);
		if (program >= 0)
			GL20.glUseProgram(program);
		GlStateManager.disablePolygonOffset();
		GlStateManager.disableBlend();
	}
	
	public static void setOnionSkins(OnionSkin...skins) {
		current.clear();
		for (OnionSkin skin : skins)
			current.add(skin);
	}
	
	public static void push() {
		stack.push(last);
		last = current;
		current = new ArrayList<>();
	}
	
	public static void pop() {
		current = last;
		last = stack.pop();
	}
	
	private static void renderOnionSkin(GuiModelRenderer renderer, OnionSkin skin) {
		if (skin.morph == null) return;
		if (!skin.light)
			GlStateManager.disableLighting();
		if (skin.color != null) {
			if (shader != null)
				GL20.glUniform4f(uniform, skin.color.getX(), skin.color.getY(), skin.color.getZ(), skin.color.getW());
			else {
				GlStateManager.color(skin.color.getX(), skin.color.getY(), skin.color.getZ(), skin.color.getW());
				RenderManager.lockColor();
			}
		}
		EntityLivingBase entity = renderer.getEntity();
		float prevPitch = entity.prevRotationPitch;
		float prevYawOffset = entity.prevRenderYawOffset;
		float prevYaw = entity.prevRotationYaw;
		float prevYawHead = entity.prevRotationYawHead;
		float pitch = entity.rotationPitch;
		float yawOffset = entity.renderYawOffset;
		float yaw = entity.rotationYaw;
		float yawHead = entity.rotationYawHead;
		entity.prevRotationPitch = entity.rotationPitch = skin.pitch;
		entity.prevRenderYawOffset = entity.renderYawOffset = skin.yawOffset;
		entity.prevRotationYaw = entity.rotationYaw = entity.prevRotationYawHead = entity.rotationYawHead = skin.yaw;
		GlStateManager.pushMatrix();
		MorphUtils.render(skin.morph, entity, skin.offset.x, skin.offset.y, skin.offset.z, 0, 0);
		GlStateManager.popMatrix();
		entity.prevRotationPitch = prevPitch;
		entity.prevRenderYawOffset = prevYawOffset;
		entity.prevRotationYaw = prevYaw;
		entity.prevRotationYawHead = prevYawHead;
		entity.rotationPitch = pitch;
		entity.renderYawOffset = yawOffset;
		entity.rotationYaw = yaw;
		entity.rotationYawHead = yawHead;
		if (skin.color != null && shader == null)
			RenderManager.unlockColor();
		if (!skin.light)
			GlStateManager.enableLighting();
	}
	
	public static class OnionSkin {

		private Color4f color = null;
		
		private AbstractMorph morph;
		
		private boolean light = true;
		
		private Vector3d offset = new Vector3d(0, 0, 0);
		
		private float pitch = 0f;
		
		private float yawOffset = 0f;
		
		private float yaw = 0f;
		
		public OnionSkin color(float r, float g, float b, float a) {
			if (this.color == null)
				this.color = new Color4f();
			this.color.set(r, g, b, a);
			return this;
		}
		
		public OnionSkin morph(AbstractMorph morph) {
			this.morph = morph;
			return this;
		}
		
		public OnionSkin light(boolean enable) {
			this.light = enable;
			return this;
		}
		
		public OnionSkin offset(double x, double y, double z, float pitch, float yaw, float yawOffset) {
			this.offset.set(x, y, z);
			this.pitch = pitch;
			this.yawOffset = yawOffset;
			this.yaw = yaw;
			return this;
		}
		
	}

}
