package nyanli.hackersmorph.other.mchorse.metamorph.client.manager;

import java.util.ArrayList;
import java.util.Stack;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3d;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import nyanli.hackersmorph.other.minecraft.common.manager.RenderManager;

public class OnionSkinManager {
	
	private static ArrayList<OnionSkin> current = new ArrayList<>();
	private static ArrayList<OnionSkin> last = null;
	
	private static Stack<ArrayList<OnionSkin>> stack = new Stack<>();
	
	public static void beforeRenderModel(GuiModelRenderer renderer, GuiContext context) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(1f, 1f);
		if (last != null)
			for (OnionSkin skin : last)
				renderOnionSkin(renderer, skin);
		if (current != null)
			for (OnionSkin skin : current)
				renderOnionSkin(renderer, skin);
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
			// Use shader will be better
			GlStateManager.color(skin.color.getX(), skin.color.getY(), skin.color.getZ(), skin.color.getW());
			RenderManager.lockColor();
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
		if (skin.color != null)
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
