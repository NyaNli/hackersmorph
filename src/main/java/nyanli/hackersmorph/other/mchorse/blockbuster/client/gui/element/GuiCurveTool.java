package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.element;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.lwjgl.opengl.GL11;

import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiGraphView;
import mchorse.mclib.client.gui.framework.elements.keyframes.Selection;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Scale;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import nyanli.hackersmorph.HackersMorph;

public class GuiCurveTool extends GuiGraphView {
	
	private static Method methodViewOffset;
	private static Field fieldMult;
	
	static {
		try {
			fieldMult = Scale.class.getDeclaredField("mult");
			fieldMult.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			HackersMorph.throwCommonException(e);
		}
		try {
			methodViewOffset = Scale.class.getMethod("viewOffset", double.class, double.class, double.class, double.class);
		} catch (NoSuchMethodException | SecurityException e) {
			try {
				methodViewOffset = Scale.class.getMethod("view", double.class, double.class, double.class, double.class);
			} catch (NoSuchMethodException | SecurityException e1) {
				HackersMorph.throwCommonException(e1);
			}
		}
	}
	
	private long lastClickTime;
	private int lastMouseButton;
	private Scale scaleY;

	public GuiCurveTool(Minecraft mc, GuiElement parent, int duration) {
		super(mc, null);
		this.parent = parent;
		this.lastClickTime = System.currentTimeMillis();
		this.lastMouseButton = -1;
		this.setDuration(duration);
		this.scaleY = null;
		try {
			Field field = GuiGraphView.class.getDeclaredField("scaleY");
			field.setAccessible(true);
			this.scaleY = (Scale) field.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
	}

	@Override
	public void setTick(double tick, boolean opposite) {
		opposite = false;
		switch (this.which) {
		case KEYFRAME:
			long min = 0;
			long max = this.duration;
			Keyframe key = this.getCurrent();
			if (key != null) {
				if (key.prev != null && key.prev != key)
					min = key.prev.tick;
				if (key.next != null && key.next != key)
					max = key.next.tick;
			}
			super.setTick(MathHelper.clamp(tick, min, max), opposite);
			break;
		default:
			super.setTick(tick, opposite);
		}
	}

	@Override
	public void setValue(double value, boolean opposite) {
		opposite = false;
		if (this.which == Selection.KEYFRAME)
			super.setValue(MathHelper.clamp(value, 0, this.duration), opposite);
		else super.setValue(value, opposite);
	}

	@Override
	protected void drawGraph(final GuiContext context, final int mouseX, final int mouseY) {
		if (this.duration <= 0 || this.sheet.channel == null || this.sheet.channel.isEmpty())
			return;
		KeyframeChannel channel = this.sheet.channel;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		GuiGraphView.COLOR.set(this.sheet.color, false);
		final float r = GuiGraphView.COLOR.r;
		final float g = GuiGraphView.COLOR.g;
		final float b = GuiGraphView.COLOR.b;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(this.area.x, this.toGraphY(MathHelper.clamp(channel.interpolate(-1), 0, this.duration)), 0.0).color(r, g, b, 1.0f).endVertex();
		for (int i = 0; i < channel.getKeyframes().size() - 1; i++) {
			Keyframe key = channel.get(i);
			Keyframe next = channel.get(i + 1);
			buffer.pos(this.toGraphX(key.tick), this.toGraphY(MathHelper.clamp(key.value, 0, this.duration)), 0.0).color(r, g, b, 1.0f).endVertex();
			for (long j = key.tick + 1; j < next.tick; j++)
				buffer.pos(this.toGraphX(j), this.toGraphY(MathHelper.clamp(channel.interpolate(j), 0, this.duration)), 0.0).color(r, g, b, 1.0f).endVertex();
		}
		Keyframe last = channel.get(channel.getKeyframes().size() - 1);
		buffer.pos(this.toGraphX(last.tick), this.toGraphY(MathHelper.clamp(last.value, 0, this.duration)), 0.0).color(r, g, b, 1.0f).endVertex();
		buffer.pos(this.area.ex(), this.toGraphY(MathHelper.clamp(last.value, 0, this.duration)), 0.0).color(r, g, b, 1.0f).endVertex();
		GL11.glLineWidth(1.0f);
		Tessellator.getInstance().draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		for (Keyframe frame : channel.getKeyframes()) {
			this.drawRect(buffer, this.toGraphX(frame.tick), this.toGraphY(frame.value), 3, 0xFFFFFF);
		}
		for (int i = 0; i < channel.getKeyframes().size(); i++) {
			Keyframe key = channel.get(i);
			this.drawRect(buffer, this.toGraphX((double) key.tick), this.toGraphY(key.value), 2,
					(this.which == Selection.KEYFRAME && this.sheet.selected.contains(i)) ? 0x0080FF : 0);
		}
		Tessellator.getInstance().draw();
	}

	@Override
	protected void drawBackground(GuiContext context) {
		super.drawBackground(context);
		if (this.duration > 0) {
			final int topBorder = this.toGraphY(this.duration);
			final int bottomBorder = this.toGraphY(0.0);
			if (bottomBorder > this.area.y) {
				Gui.drawRect(this.area.x, bottomBorder, this.area.ex(), this.area.ey(), 0x88000000);
			}
			if (topBorder < this.area.ey()) {
				Gui.drawRect(this.area.x, this.area.y, this.area.ex(), topBorder, 0x88000000);
			}
		}
	}

	@Override
	public boolean mouseClicked(final GuiContext context) {
		int mouseX = context.mouseX;
		int mouseY = context.mouseY;
		int button = context.mouseButton;
		if (this.area.isInside(mouseX, mouseY)) {
			if (this.checkDoubleClick(button)) {
				if (button == 0 && this.which == Selection.NOT_SELECTED) {
					this.addCurrent(mouseX, mouseY);
					return true;
				} else if (button == 1 && this.which == Selection.KEYFRAME) {
					this.removeSelectedKeyframes();
					return true;
				}
			}
			this.lastX = mouseX;
			this.lastY = mouseY;
			if (!this.pickKeyframe(context, mouseX, mouseY, false)) {
				this.clearSelection();
				this.setKeyframe(null);
			}
			if (button == 0)
				this.dragging = true;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseScrolled(GuiContext context) {
		return false;
	}

	@Override
	public void resetView() {
		try {
			if (this.duration > 0) {
				this.scaleX.set(0.0, 1.0);
				methodViewOffset.invoke(this.scaleX, 0, this.duration, this.area.w, 15.0);
				if (this.scaleY != null) {
					this.scaleY.set(0.0, 1.0);
					methodViewOffset.invoke(this.scaleY, 0, this.duration, this.area.h, 15.0);
				}
			}
			int mult = Math.max(this.duration, 4) / 4;
			fieldMult.setInt(this.scaleX, mult);
			if (this.scaleY != null)
				fieldMult.setInt(this.scaleY, mult);
		} catch (Exception e) {}
	}

	private boolean checkDoubleClick(int button) {
		boolean flag = false;
		long time = System.currentTimeMillis();
		if (time - this.lastClickTime < 500L && this.lastMouseButton == button) {
			flag = true;
			button = -1;
		}
		this.lastClickTime = time;
		this.lastMouseButton = button;
		return flag;
	}

}
