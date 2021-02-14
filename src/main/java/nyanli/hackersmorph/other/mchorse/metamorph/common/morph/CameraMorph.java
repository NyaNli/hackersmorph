package nyanli.hackersmorph.other.mchorse.metamorph.common.morph;

import java.awt.Color;
import java.util.Random;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Objects;

import mchorse.aperture.camera.data.Position;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.mclib.client.Draw;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import nyanli.hackersmorph.other.minecraft.common.manager.RenderManager;
import nyanli.hackersmorph.other.optifine.client.manager.ShaderManager;

// 相机伪装Morph
public class CameraMorph extends AbstractMorph {
	
	private static final Matrix4f buffer = new Matrix4f();
	
	private static final Position camera = new Position();
	private static final Position offset = new Position();

	private static boolean enabledDraw = true;
	private static String usingCamera = null;
	private static EntityLivingBase theCamera = null;
	private static boolean cameraMode = false;
	private static boolean failed = false;
	
	public static void setEnabledDraw(boolean enabled) {
		enabledDraw = enabled;
	}

	public static void setCamera(String label) {
		usingCamera = label;
		if (usingCamera == null || usingCamera.isEmpty())
			theCamera = null;
	}
	
	public static Position getCameraData(Position camOffset, float partialTicks) {
		failed = true;
		if (theCamera != null && !theCamera.isDead) {
			failed = false;
			cameraMode = true;
			if (camOffset != null)
				offset.set(camOffset);
			RenderManager.disableRender();
			Render<EntityLivingBase> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(theCamera);
			float yaw = theCamera.prevRotationYaw + (theCamera.rotationYaw - theCamera.prevRotationYaw) * partialTicks;
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			render.doRender(theCamera, 0, 0, 0, yaw, partialTicks);
			GL11.glPopMatrix();
			GlStateManager.disableLighting();
			
			RenderManager.enableRender();
			
			if (cameraMode) {
				cameraMode = false;
				theCamera = null;
				failed = true;
			}
		} else
			theCamera = null;
		if (!failed) {
			Position ret = new Position();
			ret.set(camera);
			return ret;
		} else
			return null;
	}
	
	public static Position calcCamera(Matrix4f matf) {
		Matrix4d mat = new Matrix4d(matf);
		Vector4d matX = new Vector4d();
		mat.getColumn(0, matX);
		Vector4d matY = new Vector4d();
		mat.getColumn(1, matY);
		Vector4d matZ = new Vector4d();
		mat.getColumn(2, matZ);
		
		if (Math.abs(matX.dot(matY)) > 1E-5 || Math.abs(matX.dot(matZ)) > 1E-5 || Math.abs(matY.dot(matZ)) > 1E-5) {
			return null; // illegal
		}
		
		double Tx = mat.m03;
		double Ty = mat.m13;
		double Tz = mat.m23;
		// Remove glTranslate
		mat.m03 = 0;
		mat.m13 = 0;
		mat.m23 = 0;
		mat.invert();
		
		double Rx;
		double Ry;
		double Rz;
		Matrix4d matInvSy = new Matrix4d(
				1, 0, 0, 0,
				0, -1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
				);
		
		Vector3d x = new Vector3d(matX.x, matX.y, matX.z);
		Vector3d y = new Vector3d(matY.x, matY.y, matY.z);
		Vector3d z = new Vector3d(matZ.x, matZ.y, matZ.z);
		
		Vector3d crossY = new Vector3d();
		Vector3d originalY = new Vector3d();
		originalY.normalize(y);
		crossY.cross(z, x);
		crossY.normalize();
		if (crossY.dot(originalY) < 0)
			mat.mul(matInvSy);

		Matrix4d rot = new Matrix4d();
		Vector4d test = new Vector4d(0, 1, 0, 1); // Rz*Rx*Ry*test
		Vector4d result = new Vector4d();
		mat.transform(test, result);
		if (Math.abs(result.y) > 1E-7 || Math.abs(result.x) > 1E-7) {
			double radian;
			radian = Math.atan2(-result.x, result.y);
			Rz = Math.toDegrees(radian);
			rot.rotZ(-radian);
			rot.mul(mat);
			mat.set(rot);
			mat.transform(test, result);
			
			radian = Math.atan2(result.z, result.y);
			Rx = Math.toDegrees(radian);
			rot.rotX(-radian);
			rot.mul(mat);
			mat.set(rot);
			test.y = 0;
			test.z = 1;
			mat.transform(test, result);
			
			radian = Math.atan2(result.x, result.z);
			Ry = Math.toDegrees(radian);
		} else {
			if (result.length() > 1E-7) {
				double sign = Math.signum(result.z);
				Rx = 90 * sign;
				test.x = 1;
				test.y = 0;
				mat.transform(test, result);
				Ry = sign * Math.toDegrees(Math.atan2(result.y, result.x));
				Rz = 0;
			} else {
				return null; // Scale = 0
			}
		}
		
		Position pos = new Position();
		pos.point.x = Tx;
		pos.point.y = Ty;
		if (Minecraft.getMinecraft().getRenderViewEntity() != null)
			pos.point.y -= Minecraft.getMinecraft().getRenderViewEntity().getEyeHeight();
		pos.point.z = Tz;
		pos.angle.pitch = (float) -Rx;
		pos.angle.yaw = (float) Ry; // i don't know why they are negative, but it's right.
		pos.angle.roll = (float) -Rz;
		
		return pos;
	}
	
	private String label;

	private int renderTimer;
	
	public CameraMorph() {
		this.name = "cameramorph";
		this.label = null;

		this.renderTimer = 0;
	}

	@Override
	public String getDisplayName() {
		return this.label == null || this.label.isEmpty() ? I18n.format("hackersmorph.morph.cameramorph") : this.label;
	}

	@Override
	public void update(EntityLivingBase target) {
		super.update(target);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			this.renderTimer++;
	}

	@Override
	public void renderOnScreen(EntityPlayer player, int x, int y, float scale, float alpha) {
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		this.renderTimer++;
		GL11.glPushMatrix();
		GlStateManager.disableDepth();
		GL11.glTranslated(x, y - 15, 0);
		GL11.glPushMatrix();
		GL11.glTranslated(-5, 0, 0);
		GlStateManager.rotate(-45.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(-45.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
		GL11.glScaled(scale * 2, scale * 2, scale * 2);
		renderPointer();
		GL11.glPopMatrix();
		String str = this.label != null && !this.label.isEmpty() ? this.label : "Camera" + (int)(new Random().nextFloat() * 1000);
		font.drawString(str, -font.getStringWidth(str) / 2, 5, 0xFFFFFFFF);
		GlStateManager.enableDepth();
		GL11.glPopMatrix();
	}

	@Override
	public void render(EntityLivingBase entity, double x, double y, double z,
			float entityYaw, float partialTicks) {
		if ((Minecraft.isGuiEnabled() && enabledDraw || GuiModelRenderer.isRendering()) && !ShaderManager.isShadowPass() && !cameraMode) {
	        GlStateManager.disableLighting();
			renderPointer();
			GlStateManager.enableLighting();
			renderLabel(entity, partialTicks);
		}
		
		if (this.label != null && !this.label.isEmpty() && this.label.equals(usingCamera)) {
			if (cameraMode) {
				if (theCamera == entity) {
					// default value
		            double baseX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		            double baseY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		            double baseZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		            
		            // The angle actually is used for Objects not for Camera
		            // so the order is inversed...? but the translate...
		            // what a mass, it already gives a right results, i don't care
		            GL11.glPushMatrix();
		            GL11.glTranslated(offset.point.x, offset.point.y, offset.point.z);
		            GL11.glRotated(-offset.angle.yaw, 0, 1, 0);
		            GL11.glRotated(offset.angle.pitch, 1, 0, 0);
		            GL11.glRotated(offset.angle.roll, 0, 0, 1);
					MatrixUtils.readModelView(buffer);
					GL11.glPopMatrix();
					
					Position pos = calcCamera(buffer);
					failed = pos == null;
					if (!failed) {
						camera.set(pos);
						camera.point.x += baseX;
						camera.point.y += baseY;
						camera.point.z += baseZ;
					}
					cameraMode = false;
				}
			} else if (checkEntity(entity) && theCamera == null)
				theCamera = entity;
		}
	}

	@Override
	public AbstractMorph create() {
		return new CameraMorph();
	}

	@Override
	public float getWidth(EntityLivingBase p0) {
		return 0;
	}

	@Override
	public float getHeight(EntityLivingBase p0) {
		return 0;
	}

	@Override
	public void copy(AbstractMorph from) {
		super.copy(from);
		if (from instanceof CameraMorph) {
			this.label = ((CameraMorph)from).label;
		}
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof CameraMorph && Objects.equal(((CameraMorph)obj).label, this.label);
	}

	@Override
	public boolean canMerge(AbstractMorph morph) {
		if (morph instanceof CameraMorph) {
			this.copy(morph);
			return true;
		}
		return super.canMerge(morph);
	}

	@Override
	public void fromNBT(NBTTagCompound tag) {
		super.fromNBT(tag);
		if (tag.hasKey("label", 8)) {
			this.label = tag.getString("label");
		}
	}

	@Override
	public void toNBT(NBTTagCompound tag) {
		super.toNBT(tag);
		if (label != null)
			tag.setString("label", this.label);
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	private void renderPointer() {
		GL11.glPushMatrix();
		
		this.renderTimer %= 50;
		int rgb = Color.HSBtoRGB(this.renderTimer / 50.0f, 1.0f, 1.0f);
		int rgb2 = Color.HSBtoRGB(this.renderTimer / 50.0f + 0.5f, 1.0f, 1.0f);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableTexture2D();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GL11.glLineWidth(5.0f);
		buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(0.0, 0.0, 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
		buffer.pos(0.0, 0.0, 1.0).color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb >> 0 & 0xFF, 255).endVertex();
		buffer.pos(0.0, 0.0, 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
		buffer.pos(0.0, 0.5, 0.0).color(rgb2 >> 16 & 0xFF, rgb2 >> 8 & 0xFF, rgb2 >> 0 & 0xFF, 255).endVertex();
		tessellator.draw();
		GL11.glLineWidth(1.0f);
		Draw.point(0, 0, 0);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
		
		GL11.glPopMatrix();
	}
	
	@Override
	public boolean hasCustomName() {
		return this.label != null && !this.label.isEmpty();
	}

	private void renderLabel(EntityLivingBase entity, float partialTicks) {
		if (this.label != null && !this.label.isEmpty() && !GuiModelRenderer.isRendering() && checkEntity(entity)) {
			Minecraft mc = Minecraft.getMinecraft();
			net.minecraft.client.renderer.entity.RenderManager render = mc.getRenderManager();
			FontRenderer font = render.getFontRenderer();
			Entity camera = mc.getRenderViewEntity();
			
			double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - camera.lastTickPosX - (camera.posX - camera.lastTickPosX) * partialTicks;
			double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - camera.lastTickPosY - (camera.posY - camera.lastTickPosY) * partialTicks;
			double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - camera.lastTickPosZ - (camera.posZ - camera.lastTickPosZ) * partialTicks;
			
			Matrix4f matrix4f = MatrixUtils.readModelView(buffer);
			Matrix4f parent = new Matrix4f();
			if (MatrixUtils.matrix == null)
				parent.setIdentity();
			else parent.set(MatrixUtils.matrix);
			parent.invert();
			parent.mul(matrix4f);
			x += parent.m03;
			y += parent.m13 + font.FONT_HEIGHT / 48.0 + 0.1;
			z += parent.m23;
			
			GlStateManager.pushMatrix();
			if (RenderManager.setupCameraTransform(partialTicks))
				EntityRenderer.drawNameplate(font, this.label, (float)x, (float)y, (float)z, 0, render.playerViewY, render.playerViewX, render.options.thirdPersonView == 2, false);
			GlStateManager.popMatrix();
		}
	}
	
	private boolean checkEntity(Entity entity) {
		return entity instanceof EntityActor || entity instanceof EntityActor.EntityFakePlayer;
	}

}
