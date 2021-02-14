package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.sections.GuiSnowstormGeneralSection;
import mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.sections.GuiSnowstormSection;
import mchorse.blockbuster.client.particles.BedrockLibrary;
import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.client.particles.emitter.BedrockEmitter;
import mchorse.blockbuster_pack.morphs.SnowstormMorph;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiConfirmModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiMessageModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.GuiSnowstormMorphExtra;

// 暴雪粒子伪装编辑器
public class GuiSnowstormEditorPanel extends GuiMorphPanel<SnowstormMorph, GuiSnowstormMorphExtra> {
	
	private GuiIconElement save;
	private GuiElement save_panel;

	public GuiSnowstormEditorPanel(Minecraft mc, GuiSnowstormMorphExtra editor) {
		super(mc, editor);
		editor.getProxy().editor.flex().relative(this);
		
		for (GuiSnowstormSection section : editor.getProxy().sections) {
			if (section instanceof GuiSnowstormGeneralSection) {
				GuiSnowstormGeneralSection general = (GuiSnowstormGeneralSection) section;
//				general.identifier.setVisible(false);
				general.identifier.removeFromParent();
				break;
			}
		}
		
		save = new GuiIconElement(mc, Icons.SAVED, e -> this.saveAs());
		save.tooltip(IKey.lang("hackersmorph.gui.snowstorm.save"));
		save.flex().relative(this);
		
		save_panel = new GuiElement(mc);
		save_panel.flex().relative(this).y(20).w(160).hTo(this.area, 1.0f, -16);
		
		this.add(new GuiDrawable(this::drawOverlay), editor.getProxy().editor, save, save_panel);
	}
	
	/**
     * Reference from Blockbuster Mod
	 * Url: https://github.com/mchorse/blockbuster
	 * Author: mchorse
	 * License: MIT
	 * 
	 * mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.GuiSnowstorm.setScheme(String, BedrockScheme)
	 */
    @Override
    public void fillData(SnowstormMorph morph) {
        super.fillData(morph);
        BedrockScheme scheme = morph.getEmitter().scheme;
        editor.getProxy().renderer.setScheme(scheme);
        for (GuiSnowstormSection section : editor.getProxy().sections)
        	section.setScheme(scheme);
        editor.getProxy().editor.resize();
    }
	
    /**
     * Reference from Blockbuster Mod
	 * Url: https://github.com/mchorse/blockbuster
	 * Author: mchorse
	 * License: MIT
	 * 
	 * mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.GuiSnowstorm.drawOverlay(GuiContext)
	 */
	private void drawOverlay(GuiContext context) {
		/* Draw debug info */
		BedrockEmitter emitter = editor.getProxy().renderer.emitter;
		String label = emitter.particles.size() + "P - " + emitter.age + "A";

		this.font.drawStringWithShadow(label, this.area.x + 4, this.area.ey() - 12, 0xffffff);
	}
	
	private void saveAs() {
		GuiModal.addFullModal(save_panel,
				() -> new GuiPromptModal(mc,
						IKey.lang("hackersmorph.gui.snowstorm.save.tip"), 
						name -> this.saveAs(name)).filename());
	}
	
	private void saveAs(String name) {
		BedrockLibrary library = Blockbuster.proxy.particles;
		if (library.hasEffect(name)) {
			if (library.factory.containsKey(name))
				GuiModal.addFullModal(save_panel,
						() -> new GuiMessageModal(mc,
								IKey.lang("hackersmorph.gui.snowstorm.save.error")) {
									@Override
									public void removeFromParent() {
										super.removeFromParent();
										saveAs();
									}
						});
			else
				GuiModal.addFullModal(save_panel,
						() -> new GuiConfirmModal(mc,
								IKey.lang("hackersmorph.gui.snowstorm.override.tip"),
								force -> this.saveAsForce(name, force)));
		} else
			this.saveAsForce(name, true);
	}
	
	private void saveAsForce(String name, boolean force) {
		BedrockLibrary library = Blockbuster.proxy.particles;
		if (force) {
			BedrockScheme scheme = morph.getEmitter().scheme;
			scheme.identifier = "snowstorm:" + name;
			for (GuiSnowstormSection section : editor.getProxy().sections)
	        	section.beforeSave(scheme);
			library.save(name, scheme);
		} else
			GuiModal.addFullModal(save_panel,
					() -> new GuiPromptModal(mc,
							IKey.lang("blockbuster.gui.snowstorm.add_modal"), 
							n -> this.saveAs(n)).setValue(name).filename());
	}

}
