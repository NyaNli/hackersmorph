package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.dashboard;

import org.lwjgl.opengl.GL11;

import mchorse.blockbuster.ClientProxy;
import mchorse.blockbuster.client.gui.dashboard.GuiBlockbusterPanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.mclib.GuiDashboard;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.Animation;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.client.gui.creative.GuiCreativeMorphsList;
import mchorse.metamorph.client.gui.creative.GuiCreativeMorphsMenu;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.util.MorphAnimUtils;

public class GuiMidKeyframeGenerator extends GuiBlockbusterPanel {

	private GuiAnimationRenderer renderer;
	private GuiTrackpadElement timeline;
	private GuiButtonElement selectA;
	private GuiButtonElement selectB;
	private GuiButtonElement generate;
	
	private AbstractMorph previous;
	private AbstractMorph current;
	private AbstractMorph preview;

	public GuiMidKeyframeGenerator(Minecraft mc, GuiDashboard dashboard) {
		super(mc, dashboard);
		
		this.renderer = new GuiAnimationRenderer(mc);
		this.renderer.flex().relative(this).wh(1.0f, 1.0f);
		this.renderer.reset();
		
		this.timeline = new GuiTrackpadElement(mc, this::setProgress).metric().limit(0, 0);
		this.timeline.flex().h(20);
		this.selectA = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.dashboard.midkeyframegenerator.btn.begin"), btn -> this.selectPrevious());
		this.selectA.flex().wh(50, 20);
		this.selectB = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.dashboard.midkeyframegenerator.btn.end"), btn -> this.selectCurrent());
		this.selectB.flex().wh(50, 20);
		
		GuiElement row = new GuiElement(mc);
		row.flex().relative(this).anchorX(0.5f).x(0.5f).y(1.0f, -50).w(0.8f).row(0);
		row.add(this.selectA, this.timeline, this.selectB);
		
		this.generate = new GuiButtonElement(mc, IKey.lang("hackersmorph.gui.dashboard.midkeyframegenerator.btn.generate"), btn -> this.generateKeyframe());
		this.generate.flex().relative(this).anchorX(0.5f).x(0.5f).y(1.0f, -100).wh(100, 30);
		
		this.add(this.renderer, row, this.generate);
		
		previous = null;
		current = null;
		preview = null;
	}

	@Override
	public void open() {
		super.open();
		this.renderer.reset();
		this.renderer.setScale(3.0f);
		this.previous = null;
		this.current = null;
		this.preview = null;
		this.renderer.morph = null;
		this.generate.setEnabled(false);
		this.timeline.limit(0, 0);
		this.timeline.setValue(0);
	}

	@Override
	public void disappear() {
		super.disappear();
		ClientProxy.panels.morphs.finish();
		ClientProxy.panels.morphs.removeFromParent();
	}
	
	public void setProgress(double t) {
		float partialTicks = (float) t;
		int progress = (int) partialTicks;
		partialTicks -= progress;
		MorphAnimUtils.setProgress(this.preview, progress);
		this.renderer.partialTicks = partialTicks;
	}
	
	public void selectPrevious() {
		ClientProxy.panels.picker(morph -> {
			if (!MorphAnimUtils.canGenerateKeyframe(morph))
				morph = null;
			this.previous = MorphUtils.copy(morph);
			this.updateMorph();
		});
		ClientProxy.panels.addMorphs(this, false, this.previous);
	}
	
	public void selectCurrent() {
		ClientProxy.panels.picker(morph -> {
			if (!MorphAnimUtils.canGenerateKeyframe(morph))
				morph = null;
			this.current = MorphUtils.copy(morph);
			this.updateMorph();
		});
		ClientProxy.panels.addMorphs(this, false, this.current);
	}
	
	public void generateKeyframe() {
		ClientProxy.panels.picker(null);
		ClientProxy.panels.addMorphs(this, false, MorphAnimUtils.generateKeyframe(this.current, this.previous, (float) this.timeline.value));
	}
	
	@Override
	public void remove(GuiElement element) {
		super.remove(element);
		if (element instanceof GuiCreativeMorphsList) {
			GuiCreativeMorphsList morphs = (GuiCreativeMorphsList) element;
			morphs.pickMorph(morphs.getSelected());
		}
	}

	private void updateMorph() {
		this.preview = MorphUtils.copy(this.previous);
		if (this.preview == null || !this.preview.canMerge(this.current))
			this.preview = this.current = null;
		this.generate.setEnabled(false);
		if (this.preview != null && ((IAnimationProvider) this.preview).getAnimation() != null) {
			this.renderer.morph = this.preview;
			IAnimationProvider provider = (IAnimationProvider) this.preview;
			Animation anim = provider.getAnimation();
			this.timeline.limit(0, anim.animates ? anim.duration : 0);
			this.timeline.setValueAndNotify(anim.duration);
			this.generate.setEnabled(anim.animates && anim.duration > 0);
		} else if (this.previous != null) {
			this.renderer.morph = this.previous;
			this.timeline.limit(0, 0);
			this.timeline.setValueAndNotify(0);
		} else
			this.renderer.morph = null;
	}
	
	public static class GuiAnimationRenderer extends GuiModelRenderer {
		
		public AbstractMorph morph;
		public float partialTicks;
		
		private FontRenderer font;

		public GuiAnimationRenderer(Minecraft mc) {
			super(mc);
			this.font = mc.fontRenderer;
		}

		@Override
		public void draw(GuiContext context) {
			if (this.morph == null) {
				GL11.glPushMatrix();
				GL11.glTranslated(this.area.x, -this.area.y, 0);
				GL11.glScaled(3, 3, 3);
				String str = I18n.format("hackersmorph.gui.dashboard.midkeyframegenerator.msg");
				int x = (this.area.w - font.getStringWidth(str) * 3) / 2;
				int y = (this.area.h - font.FONT_HEIGHT * 3) / 2;
				font.drawStringWithShadow(str, x / 3, y / 3, 0xFFFFFFFF);
				GL11.glPopMatrix();
			} else
				super.draw(context);
		}

		@Override
		protected void drawUserModel(GuiContext context) {
			if (this.morph != null)
				MorphUtils.render(this.morph, this.entity, 0.0, 0.0, 0.0, this.yaw, this.partialTicks);
		}
		
	}

}
