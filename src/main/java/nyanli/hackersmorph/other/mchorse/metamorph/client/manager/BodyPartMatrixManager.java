package nyanli.hackersmorph.other.mchorse.metamorph.client.manager;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;

import mchorse.mclib.client.Draw;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.GuiBodyPartEditor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BodyPartMatrixManager {
	
	private static WeakHashMap<BodyPart, Matrix4f> map = new WeakHashMap<>();
	private static WeakHashMap<BodyPart, Boolean> doCalc = new WeakHashMap<>();
	private static WeakReference<BodyPart> current;
	
	public static void glPushMatrix(BodyPart bodyPart) {
		if (GuiModelRenderer.isRendering()) {
			Matrix4f current = MatrixUtils.readModelView(new Matrix4f());
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			Matrix4f limb = MatrixUtils.readModelView(new Matrix4f());
			limb.invert();
			limb.mul(current);
			
			if (map.containsKey(bodyPart) && Boolean.TRUE.equals(doCalc.get(bodyPart))) {
				doCalc.put(bodyPart, false);
				Matrix4f lastLimb = map.get(bodyPart);
				
				Matrix4f convertMat = new Matrix4f(limb);
				convertMat.invert();
				convertMat.mul(lastLimb);
				
				// I hate radians
				Matrix4f bodyPartTransform = new Matrix4f(
						1, 0, 0, bodyPart.translate.x,
						0, 1, 0, bodyPart.translate.y,
						0, 0, 1, bodyPart.translate.z,
						0, 0, 0, 1
						);
				convertMat.mul(bodyPartTransform);
				bodyPartTransform.rotZ((float) Math.toRadians(bodyPart.rotate.z));
				convertMat.mul(bodyPartTransform);
				bodyPartTransform.rotY((float) Math.toRadians(bodyPart.rotate.y));
				convertMat.mul(bodyPartTransform);
				bodyPartTransform.rotX((float) Math.toRadians(bodyPart.rotate.x));
				convertMat.mul(bodyPartTransform);
				
				TransformPack pack = calcTransform(convertMat);
				if (pack != null) {
					bodyPart.translate.set(pack.translate);
					bodyPart.rotate.set(pack.rotate);
					bodyPart.scale.x *= pack.scale.x;
					bodyPart.scale.y *= pack.scale.y;
					bodyPart.scale.z *= pack.scale.z;
				}
			}
			if (!map.containsKey(bodyPart))
				map.put(bodyPart, new Matrix4f());
			map.get(bodyPart).set(limb);
			current.transpose();
			MatrixUtils.loadModelView(current);
		}
		GL11.glPushMatrix();
	}
	
	// 不是很好用……
//	public static void glPopMatrix(BodyPart bodyPart) {
//		GL11.glPopMatrix();
//		
//	    /**
//		 * Reference from Metamorph Mod
//		 * Url: https://github.com/mchorse/metamorph
//		 * Author: mchorse
//		 * License: MIT
//		 * 
//		 * mchorse.metamorph.bodypart.BodyPart.render(EntityLivingBase, float)
//		 */
//		if (GuiModelRenderer.isRendering() && current != null && current.get() == bodyPart)
//        {
//    		GL11.glPushMatrix();
//            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//            GlStateManager.disableTexture2D();
//            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//            GlStateManager.disableTexture2D();
//
//            GlStateManager.disableDepth();
//            GlStateManager.disableLighting();
//
//            GL11.glTranslatef(bodyPart.translate.x, bodyPart.translate.y, bodyPart.translate.z);
//            
//    	    /**
//    		 * Reference from Mclib Mod
//    		 * Url: https://github.com/mchorse/mclib
//    		 * Author: mchorse
//    		 * License: MIT
//    		 * 
//    		 * mchorse.mclib.client.Draw.axis(float)
//    		 */
//    		Tessellator tessellator = Tessellator.getInstance();
//    		BufferBuilder buffer = tessellator.getBuffer();
//
//    		// Rotate Z
//    		GL11.glLineWidth(5);
//    		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//    		buffer.pos(0, 0, 0).color(0.5f, 0.5f, 1F, 1F).endVertex();
//    		buffer.pos(0, 0, MathHelper.clamp(0.25 * bodyPart.scale.z, 0.25, 1.0)).color(0f, 1f, 1f, 1F).endVertex();
//    		tessellator.draw();
//    		
//    		// Rotate Y
//            GL11.glRotatef(bodyPart.rotate.z, 0, 0, 1);
//    		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//    		buffer.pos(0, 0, 0).color(0.5f, 1F, 0.5f, 1F).endVertex();
//    		buffer.pos(0, MathHelper.clamp(0.25 * bodyPart.scale.y, 0.25, 1.0), 0).color(1f, 1f, 0f, 1F).endVertex();
//    		tessellator.draw();
//    		
//    		// Rotate X
//            GL11.glRotatef(bodyPart.rotate.y, 0, 1, 0);
//    		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//    		buffer.pos(0, 0, 0).color(1F, 0.5f, 0.5f, 1F).endVertex();
//    		buffer.pos(MathHelper.clamp(0.25 * bodyPart.scale.x, 0.25, 1.0), 0, 0).color(1f, 0f, 1f, 1F).endVertex();
//    		tessellator.draw();
//    		
//    		GL11.glLineWidth(1);
//    		Draw.point(0, 0, 0);
//
//            GlStateManager.enableLighting();
//            GlStateManager.enableDepth();
//
//            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//            GlStateManager.enableTexture2D();
//            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//            GlStateManager.enableTexture2D();
//    		GL11.glPopMatrix();
//        }
//	}
	
	public static BodyPart onPickLimb(BodyPart bodyPart) {
		if (GuiScreen.isCtrlKeyDown())
			doCalc.put(bodyPart, true);
		return bodyPart;
	}
	
	public static void onGuiDraw(GuiBodyPartEditor editor, BodyPart bodyPart) {
		if (Boolean.FALSE.equals(doCalc.get(bodyPart))) {
			doCalc.remove(bodyPart);
			editor.fillBodyPart(bodyPart);
		}
		current = new WeakReference<BodyPart>(bodyPart);
	}

	public static void clearCurrent() {
		current = null;
	}
	
	public static TransformPack calcTransform(Matrix4f mat) {
		mat = new Matrix4f(mat);
		Vector4f matX = new Vector4f();
		mat.getColumn(0, matX);
		Vector4f matY = new Vector4f();
		mat.getColumn(1, matY);
		Vector4f matZ = new Vector4f();
		mat.getColumn(2, matZ);
		
		if (Math.abs(matX.dot(matY)) > 1E-5 || Math.abs(matX.dot(matZ)) > 1E-5 || Math.abs(matY.dot(matZ)) > 1E-5) {
			return null; // illegal
		}
		
		float Tx = mat.m03;
		float Ty = mat.m13;
		float Tz = mat.m23;
		// Remove glTranslate
		mat.m03 = 0;
		mat.m13 = 0;
		mat.m23 = 0;
		
		float Rx;
		float Ry;
		float Rz;
		boolean invSy = false;
		Matrix4f matInvSy = new Matrix4f(
				1, 0, 0, 0,
				0, -1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
				);
		
		Vector3f x = new Vector3f(matX.x, matX.y, matX.z);
		Vector3f y = new Vector3f(matY.x, matY.y, matY.z);
		Vector3f z = new Vector3f(matZ.x, matZ.y, matZ.z);
		
		Vector3f crossY = new Vector3f();
		Vector3f originalY = new Vector3f();
		originalY.normalize(y);
		crossY.cross(z, x);
		crossY.normalize();
		if (crossY.dot(originalY) < 0) {
			invSy = true;
			mat.mul(matInvSy);
		}

		Matrix4f rot = new Matrix4f();
		Vector4f test = new Vector4f(1, 0, 0, 1);
		Vector4f result = new Vector4f();
		mat.transform(test, result);
		if (Math.abs(result.y) > 1E-7 || Math.abs(result.x) > 1E-7) {
			double radian;
			radian = Math.atan2(result.y, result.x);
			Rz = round3(Math.toDegrees(radian));
			rot.rotZ((float) -radian);
			rot.mul(mat);
			mat.set(rot);
			mat.transform(test, result);
			
			radian = Math.atan2(-result.z, result.x);
			Ry = round3(Math.toDegrees(radian));
			rot.rotY((float) -radian);
			rot.mul(mat);
			mat.set(rot);
			test.x = 0;
			test.y = 1;
			mat.transform(test, result);
			
			radian = Math.atan2(result.z, result.y);
			Rx = round3(Math.toDegrees(radian));
			rot.rotX((float) -radian);
			rot.mul(mat);
			mat.set(rot);
		} else {
			if (result.length() > 1E-7) {
				Rz = 0;
				double radianX;
				float sign = -Math.signum(result.z);
				Ry = sign * 90f;
				test.x = 0;
				test.z = sign;
				mat.transform(test, result);
				radianX = sign * -Math.atan2(result.y, result.x);
				Rx = round3(Math.toDegrees(radianX));
				// Rz = 0, do nothing
				rot.rotY((float) -Math.toRadians(Ry));
				rot.mul(mat);
				mat.set(rot);
				rot.rotX((float) -radianX);
				rot.mul(mat);
				mat.set(rot);
			} else {
				return null; // Scale = 0
			}
		}
		
		Vector4f scale = new Vector4f();
		mat.transform(new Vector4f(1, invSy ? -1 : 1, 1, 1), scale);
		
		TransformPack pack = new TransformPack();
		pack.translate = new Vector3f(Tx, Ty, Tz);
		pack.rotate = new Vector3f(Rx, Ry, Rz);
		pack.scale = new Vector3f(round3(scale.x), round3(scale.y), round3(scale.z));
		
		return pack;
	}
	
	private static float round3(double d) {
		return Math.round(d * 1000) / 1000f;
	}
	
	public static class TransformPack {
		public Vector3f translate;
		public Vector3f rotate;
		public Vector3f scale;
	}

}
