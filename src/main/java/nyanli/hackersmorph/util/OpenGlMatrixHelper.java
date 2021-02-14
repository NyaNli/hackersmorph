package nyanli.hackersmorph.util;

import java.util.Stack;

import org.lwjgl.opengl.GL11;

public class OpenGlMatrixHelper {
	
	private static final Stack<Integer> lastMatrixMode = new Stack<>();
	
	public static void pushAllMatrix() {
		int mode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
		lastMatrixMode.push(mode);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPushMatrix();
		GL11.glMatrixMode(mode);
	}
	
	public static void popAllMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPopMatrix();
		GL11.glMatrixMode(lastMatrixMode.pop());
	}

}
