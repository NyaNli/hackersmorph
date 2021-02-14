package nyanli.hackersmorph.other.mchorse.aperture.client.gui;

import java.util.function.Consumer;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.mclib.client.gui.framework.elements.keyframes.GuiSheet;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nyanli.hackersmorph.other.mchorse.aperture.client.camera.fixture.CameraMorphFixture;
import nyanli.hackersmorph.other.mchorse.aperture.client.manager.FlightManager;

// 相机伪装ApertureGui
public class GuiCameraMorphFixturePanel extends GuiKeyframeFixturePanel {
	
	private IKey[] titleValues;
	
	public GuiCameraMorphFixturePanel(Minecraft mc, GuiCameraEditor editor) {
		super(mc, editor);
		this.name.tooltip(IKey.lang("hackersmorph.gui.fixture.cameramorph.tooltip"));
		Consumer<String> callback = this.name.callback;
		this.name.callback = str -> {
			callback.accept(str);
			this.editor.haveScrubbed();
		};
		this.titleValues = new IKey[this.titles.length];
		for (int i = 0; i < this.titleValues.length; i++)
			this.titleValues[i] = IKey.str("");
	}

	@Override
	public void select(KeyframeFixture fixture, long duration) {
		super.select(fixture, duration);
		this.editor.haveScrubbed();
		FlightManager.clearLast(this.editor.flight);
	}

	@Override
	public void editFixture(Position position) {
		CameraMorphFixture fixture = (CameraMorphFixture) this.fixture;
		Position origin = fixture.getCurrent();
		Position result = new Position();
		result.set(position);
		result.point.x -= origin.point.x;
		result.point.y -= origin.point.y;
		result.point.z -= origin.point.z;
		result.angle.pitch -= origin.angle.pitch;
		result.angle.yaw -= origin.angle.yaw;
		result.angle.roll -= origin.angle.roll;
		
		long tick = this.editor.timeline.value - this.currentOffset();
		Position prev = new Position();
        if (!fixture.x.isEmpty()) prev.point.x = fixture.x.interpolate(tick);
        if (!fixture.y.isEmpty()) prev.point.y = fixture.y.interpolate(tick);
        if (!fixture.z.isEmpty()) prev.point.z = fixture.z.interpolate(tick);
        if (!fixture.pitch.isEmpty()) prev.angle.pitch = (float) fixture.pitch.interpolate(tick);
        if (!fixture.yaw.isEmpty()) prev.angle.yaw = (float) fixture.yaw.interpolate(tick);
        if (!fixture.roll.isEmpty()) prev.angle.roll = (float) fixture.roll.interpolate(tick);
    	result.angle.pitch += prev.angle.pitch;
    	result.angle.pitch = MathHelper.clamp(result.angle.pitch, -90f, 90f);
        result.angle.yaw += prev.angle.yaw;
        result.angle.roll += prev.angle.roll;
        
        FlightManager.Mode mode = FlightManager.getModeAndUpdate(this.editor.flight);
        if (mode == FlightManager.Mode.O) {
        	/**
        	 * Reference from Aperture Mod
        	 * Url: https://github.com/mchorse/aperture
        	 * Author: mchorse
        	 * License: MIT
        	 * 
        	 * mchorse.aperture.client.gui.Flight.calculateOrigin() after v1.5
        	 */;
        	Point center = new Point(0, 0, 0);
        	center.set(prev.point);
        	Vec3d vec = new Vec3d(0, 0, FlightManager.getLastDistanceAndUpdate(this.editor.flight));
        	vec = vec.rotatePitch((float) Math.toRadians(-prev.angle.pitch));
        	vec = vec.rotateYaw((float) Math.toRadians(-prev.angle.yaw));
    		center.x += vec.x;
    		center.y += vec.y;
    		center.z += vec.z;
    		
    		vec = new Vec3d(0, 0, -FlightManager.getLastDistanceAndUpdate(this.editor.flight));
        	vec = vec.rotatePitch((float) Math.toRadians(-result.angle.pitch));
        	vec = vec.rotateYaw((float) Math.toRadians(-result.angle.yaw));
			result.point.set(center.x + vec.x, center.y + vec.y, center.z + vec.z);
        } else {
        	/**
        	 * Reference from Aperture Mod
        	 * Url: https://github.com/mchorse/aperture
        	 * Author: mchorse
        	 * License: MIT
        	 * 
        	 * mchorse.aperture.client.gui.Flight.animate(GuiContext, Position)
        	 */
    		Vec3d offset = new Vec3d(result.point.x, result.point.y, result.point.z);
    		offset = offset.rotateYaw((float) Math.toRadians(position.angle.yaw));
    		if (mode == FlightManager.Mode.V) {
    			offset = offset.rotatePitch((float) Math.toRadians(position.angle.pitch));
    			offset = offset.rotatePitch((float) Math.toRadians(-result.angle.pitch));
    		}
    		offset = offset.rotateYaw((float) Math.toRadians(-result.angle.yaw));
    		result.point.x = offset.x + prev.point.x;
    		result.point.y = offset.y + prev.point.y;
    		result.point.z = offset.z + prev.point.z;
        }
        
		super.editFixture(result);
		
		this.editor.getProfile().applyProfile(tick, 0, this.editor.position);
		
		this.editor.position.apply(this.editor.getCamera());
		ClientProxy.control.roll = this.editor.position.angle.roll;
		this.editor.mc.gameSettings.fovSetting = this.editor.position.angle.fov;
	}

	@Override
	public void draw(GuiContext context) {
		if (Aperture.editorDisplayPosition.get()) {
			float t = this.editor.timeline.value - this.currentOffset() + Minecraft.getMinecraft().getRenderPartialTicks();
			for (int i = 0; i < this.dope.graph.sheets.size(); i++) {
				GuiSheet sheet = this.dope.graph.sheets.get(i);
				double value = sheet.channel.isEmpty() ? 0 : sheet.channel.interpolate(t);
				this.titleValues[i].set(String.format("%.3f %s", value, this.titles[i + 1].get()));
				sheet.title = this.titleValues[i];
			}
		} else
			for (int i = 0; i < this.dope.graph.sheets.size(); i++)
				this.dope.graph.sheets.get(i).title = this.titles[i + 1];
		super.draw(context);
	}

}
