package nyanli.hackersmorph.other.mchorse.aperture.client.camera.fixture;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import nyanli.hackersmorph.other.mchorse.metamorph.common.morph.CameraMorph;

// 相机伪装Aperture
public class CameraMorphFixture extends KeyframeFixture {
	
	private Position current = new Position();
	
	public CameraMorphFixture() {
		super();
	}
	
	public CameraMorphFixture(long duration) {
		super(duration);
	}

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {
        float t = ticks + previewPartialTick;

        Position offset = new Position();
        
        if (!this.x.isEmpty()) offset.point.x = this.x.interpolate(t);
        if (!this.y.isEmpty()) offset.point.y = this.y.interpolate(t);
        if (!this.z.isEmpty()) offset.point.z = this.z.interpolate(t);
        if (!this.pitch.isEmpty()) offset.angle.pitch = MathHelper.clamp((float)this.pitch.interpolate(t), -90f, 90f);
        if (!this.yaw.isEmpty()) offset.angle.yaw = (float) this.yaw.interpolate(t);
        if (!this.roll.isEmpty()) offset.angle.roll = (float) this.roll.interpolate(t);

        CameraMorph.setCamera(this.name);
        Position origin = CameraMorph.getCameraData(offset, partialTick);
        
        if (origin != null)
        	pos.set(origin);
        
        if (!this.fov.isEmpty()) pos.angle.fov = (float) this.fov.interpolate(t);
        this.current.set(pos);
    }

	@Override
    public AbstractFixture create(long duration)
    {
        return new CameraMorphFixture(duration);
    }

	@Override
	public void fromPlayer(EntityPlayer player) {
		this.x.insert(0, 0);
		this.y.insert(0, 0);
		this.z.insert(0, 0);
		this.pitch.insert(0, 0);
		this.yaw.insert(0, 0);
		this.roll.insert(0, 0);
		this.fov.insert(0, Minecraft.getMinecraft().gameSettings.fovSetting);
	}

	public Position getCurrent() {
		return current;
	}
	
}
