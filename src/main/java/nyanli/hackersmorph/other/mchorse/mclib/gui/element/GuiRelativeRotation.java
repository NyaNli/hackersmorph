package nyanli.hackersmorph.other.mchorse.mclib.gui.element;

import java.util.List;
import java.util.function.Consumer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector4d;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiCirculateElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public abstract class GuiRelativeRotation extends GuiTrackpadElement {
	
	public static void constructor(GuiTransformations gui) {
		GuiElement first = null, second = null, third = null;
		for (IGuiElement element : gui.getChildren()) {
			if (element.getClass() == GuiElement.class) {
				for (IGuiElement element2 : ((GuiElement) element).getChildren()) {
					if (element2 == gui.tx) {
						first = (GuiElement) element;
						break;
					} else if (element2 == gui.ty) {
						second = (GuiElement) element;
						break;
					} else if (element2 == gui.tz) {
						third = (GuiElement) element;
						break;
					}
				}
			}
		}
		if (first != null && second != null && third != null) {
			GuiToggleElement origin = new GuiToggleElement(Minecraft.getMinecraft(), IKey.EMPTY, null);
			origin.tooltip(IKey.lang("hackersmorph.gui.transformation.origin"));
			GuiRelativeRotation rrx = new RotateX(Minecraft.getMinecraft(), origin, gui);
			rrx.tooltip(IKey.lang("hackersmorph.gui.transformation.rx"));
			GuiRelativeRotation rry = new RotateY(Minecraft.getMinecraft(), origin, gui);
			rry.tooltip(IKey.lang("hackersmorph.gui.transformation.ry"));
			GuiRelativeRotation rrz = new RotateZ(Minecraft.getMinecraft(), origin, gui);
			rrz.tooltip(IKey.lang("hackersmorph.gui.transformation.rz"));
			origin.flex().relative(rrx).x(1.0f).y(-13).wh(11, 11).anchorX(1.0f);
			first.add(rrx);
			second.add(rry);
			third.add(rrz);
			gui.add(origin);
		}
	}

	private final Matrix4d matrix = new Matrix4d();
	private final Matrix4d matrix2 = new Matrix4d();
	private final Vector4d vector = new Vector4d();
	
	private final GuiTransformations gui;
	private final GuiToggleElement origin;
	private final GuiTrackpadElement tx;
	private final GuiTrackpadElement ty;
	private final GuiTrackpadElement tz;
	private final GuiTrackpadElement rx;
	private final GuiTrackpadElement ry;
	private final GuiTrackpadElement rz;

	private boolean modeLoaded;
	private GuiCirculateElement mode;
	private boolean isZYX;
	private double lastTx;
	private double lastTy;
	private double lastTz;
	private double lastRx;
	private double lastRy;
	private double lastRz;
	
	public GuiRelativeRotation(Minecraft mc, GuiToggleElement origin, GuiTransformations gui) {
		super(mc, (Consumer<Double>)null);
		this.field.setEnabled(false);
		this.field.setDisabledTextColour(0xE0E0E0);
		this.callback = this::doRotate;
		this.degrees();
		this.gui = gui;
		this.origin = origin;
		this.tx = gui.tx;
		this.ty = gui.ty;
		this.tz = gui.tz;
		this.rx = gui.rx;
		this.ry = gui.ry;
		this.rz = gui.rz;
		this.modeLoaded = false;
	}

	@Override
	public boolean mouseClicked(GuiContext context) {
		if (!this.modeLoaded)
			tryLoadMode();
		this.isZYX = this.mode != null && "ZYX".equals(this.mode.getLabel());
		if (origin.isToggled()) {
			lastTx = tx.value;
			lastTy = ty.value;
			lastTz = tz.value;
		} else
			lastTx = lastTy = lastTz = 0;
		lastRx = rx.value;
		lastRy = ry.value;
		lastRz = rz.value;
		return super.mouseClicked(context);
	}

	@Override
	public void mouseReleased(GuiContext context) {
		super.mouseReleased(context);
		this.setValue(0);
	}
	
	protected abstract void setupMatrix(Matrix4d mat, double degrees);
	
	private void tryLoadMode() {
		if (gui.getParent() != null) {
			List<GuiCirculateElement> list = gui.getParent().getChildren(GuiCirculateElement.class);
			for (GuiCirculateElement element : list)
				if ("ZYX".equals(element.getLabel()) || "XYZ".equals(element.getLabel())) {
					this.mode = element;
					break;
				}
			this.modeLoaded = true;
		}
	}

	private void doRotate(double degrees) {
		setupMatrix(this.matrix, degrees);
		this.matrix2.setIdentity();
		this.matrix2.setColumn(3, this.lastTx, this.lastTy, this.lastTz, 1);
		this.matrix.mul(this.matrix2); // We can get current Txyz here
		if (this.isZYX) {
			this.matrix2.rotX(Math.toRadians(this.lastRx));
			this.matrix.mul(this.matrix2);
			this.matrix2.rotY(Math.toRadians(this.lastRy));
			this.matrix.mul(this.matrix2);
			this.matrix2.rotZ(Math.toRadians(this.lastRz));
			this.matrix.mul(this.matrix2);
		} else {
			this.matrix2.rotZ(Math.toRadians(this.lastRz));
			this.matrix.mul(this.matrix2);
			this.matrix2.rotY(Math.toRadians(this.lastRy));
			this.matrix.mul(this.matrix2);
			this.matrix2.rotX(Math.toRadians(this.lastRx));
			this.matrix.mul(this.matrix2);
		}
		
		double Tx = this.lastTx, Ty = this.lastTy, Tz = this.lastTz, 
				Rx = this.lastRx, Ry = this.lastRy, Rz = this.lastRz;
		Tx = this.matrix.m03;
		Ty = this.matrix.m13;
		Tz = this.matrix.m23;
		this.matrix.m03 = this.matrix.m13 = this.matrix.m23 = 0;
		
		// rotX * rotY * rotZ == transpose(rot-Z * rot-Y * rot-X)
		double s = 1;
		if (this.isZYX) {
			this.matrix.transpose();
			s = -1;
		}

		double radian;
		this.vector.set(1, 0, 0, 1);
		this.matrix.transform(this.vector);
		if (Math.abs(this.vector.y) > 1E-7 || Math.abs(this.vector.x) > 1E-7) {
			radian = Math.atan2(this.vector.y, this.vector.x);
			Rz = getMinimumChanges(s * this.rz.value, Math.toDegrees(radian), 180);
			this.matrix2.rotZ(-Math.toRadians(Rz));
			this.matrix2.mul(this.matrix);
			this.matrix.set(this.matrix2);
			this.vector.set(1, 0, 0, 1);
			this.matrix.transform(this.vector);
			
			radian = Math.atan2(-this.vector.z, this.vector.x);
			Ry = getMinimumChanges(s * this.ry.value, Math.toDegrees(radian), 180);
			this.matrix2.rotY(-Math.toRadians(Ry));
			this.matrix2.mul(this.matrix);
			this.matrix.set(this.matrix2);
			this.vector.set(0, 1, 0, 1);
			this.matrix.transform(this.vector);
			
			radian = Math.atan2(this.vector.z, this.vector.y);
			Rx = Math.toDegrees(radian);
		} else {
			Rz = this.tz.value;
			double sign = -Math.signum(this.vector.z);
			Ry = sign * 90f;
			this.vector.set(0, 0, sign, 1);
			this.matrix.transform(this.vector);
			radian = Math.atan2(this.vector.y, this.vector.x);
			Rx = sign * (Rz - Math.toDegrees(radian));
		}
		
		if (this.isZYX) {
			Rx = -Rx;
			Ry = -Ry;
			Rz = -Rz;
		}
		
		Rx = getMinimumChanges(this.rx.value, Rx, 360);
		Ry = getMinimumChanges(this.ry.value, Ry, 360);
		Rz = getMinimumChanges(this.rz.value, Rz, 360);
		
		if (this.origin.isToggled()) {
			this.tx.setValueAndNotify(Tx);
			this.ty.setValueAndNotify(Ty);
			this.tz.setValueAndNotify(Tz);
		}
		this.rx.setValueAndNotify(Rx);
		this.ry.setValueAndNotify(Ry);
		this.rz.setValueAndNotify(Rz);
	}
	
	private double getMinimumChanges(double origin, double changed, double cycle) {
		double sub = changed - origin;
		sub = sub - (int)(sub / cycle) * cycle;
		if (Math.abs(sub) > cycle / 2.0)
			sub -= Math.signum(sub) * cycle;
		return origin + sub;
	}
	
	public static class RotateX extends GuiRelativeRotation {

		public RotateX(Minecraft mc, GuiToggleElement origin, GuiTransformations gui) {
			super(mc, origin, gui);
		}

		@Override
		protected void setupMatrix(Matrix4d mat, double degrees) {
			mat.rotX(Math.toRadians(degrees));
		}
		
	}
	
	public static class RotateY extends GuiRelativeRotation {

		public RotateY(Minecraft mc, GuiToggleElement origin, GuiTransformations gui) {
			super(mc, origin, gui);
		}

		@Override
		protected void setupMatrix(Matrix4d mat, double degrees) {
			mat.rotY(Math.toRadians(degrees));
		}
		
	}
	
	public static class RotateZ extends GuiRelativeRotation {

		public RotateZ(Minecraft mc, GuiToggleElement origin, GuiTransformations gui) {
			super(mc, origin, gui);
		}

		@Override
		protected void setupMatrix(Matrix4d mat, double degrees) {
			mat.rotZ(Math.toRadians(degrees));
		}
		
	}
	
}
