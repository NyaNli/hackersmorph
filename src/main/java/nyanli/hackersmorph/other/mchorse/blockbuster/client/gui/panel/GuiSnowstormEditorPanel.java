package nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.panel;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.sections.GuiSnowstormGeneralSection;
import mchorse.blockbuster.client.gui.dashboard.panels.snowstorm.sections.GuiSnowstormSection;
import mchorse.blockbuster.client.particles.BedrockLibrary;
import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.client.particles.emitter.BedrockEmitter;
import mchorse.blockbuster_pack.morphs.SnowstormMorph;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import nyanli.hackersmorph.other.mchorse.blockbuster.client.gui.GuiSnowstormMorphExtra;
import nyanli.hackersmorph.other.mchorse.blockbuster.common.manager.SnowstormMorphExtraManager;

// 暴雪粒子伪装编辑器
public class GuiSnowstormEditorPanel extends GuiMorphPanel<SnowstormMorph, GuiSnowstormMorphExtra> {
	
	private GuiToggleElement standalone;
	private GuiIconElement save;
	private GuiElement save_panel;
	private GuiSearchListElement<String> presets;

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
		
		standalone = new GuiToggleElement(mc, IKey.lang("hackersmorph.gui.snowstorm.standalone"), e -> this.switchMode(e.isToggled()));
		standalone.flex().relative(this).x(20).y(30).w(100);
		
		save = new GuiIconElement(mc, Icons.SAVED, e -> this.saveAs());
		save.tooltip(IKey.lang("hackersmorph.gui.snowstorm.save"));
		save.flex().relative(this);
		
		save_panel = new GuiElement(mc);
		save_panel.flex().relative(this).y(20).w(160).hTo(this.area, 1.0f, -16);
		
		presets = new GuiSearchListElement<String>(mc, s -> this.setPreset(s.get(0))) {

			@Override
			protected GuiListElement<String> createList(Minecraft mc, Consumer<List<String>> callback) {
				return new GuiListElement<String>(mc, callback) {
					
					@Override
					protected boolean sortElements() {
						Collections.<String>sort(this.list, (a, b) -> a.compareTo(b));
						return true;
					}
					
				};
			}
			
		};
		presets.list.sort();
		presets.flex().relative(this).x(1.0f).w(180).h(1.0f).anchorX(1.0f);
		presets.list.background(0x80000000);
		presets.resize();
		presets.list.scroll.scrollSpeed = 15;
		
		this.add(new GuiDrawable(this::drawOverlay), editor.getProxy().editor, standalone, save, save_panel, presets);
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
        
        standalone.toggled(SnowstormMorphExtraManager.getStandalone(morph));
        this.switchMode(SnowstormMorphExtraManager.getStandalone(morph));
    }
	
    @Override
	public void finishEditing() {
		for (GuiSnowstormSection section : editor.getProxy().sections)
        	section.beforeSave(this.morph.getEmitter().scheme);
		super.finishEditing();
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
	
	private void setPreset(String preset) {
		if (!SnowstormMorphExtraManager.getStandalone(morph)) {
			morph.scheme = preset;
    		SnowstormMorphExtraManager.updateScheme(morph, Blockbuster.proxy.particles.load(preset));
		}
        for (GuiSnowstormSection section : editor.getProxy().sections)
        	section.setScheme(this.morph.getEmitter().scheme);
        editor.getProxy().editor.resize();
		editor.getProxy().renderer.setScheme(morph.getEmitter().scheme);
	}
	
	private void switchMode(boolean toggled) {
		SnowstormMorphExtraManager.setStandalone(this.morph, toggled);
		save.setVisible(toggled);
		save_panel.setVisible(toggled);
		editor.getProxy().editor.setVisible(toggled);
		presets.setVisible(!toggled);
		if (!toggled && !Blockbuster.proxy.particles.presets.containsKey(this.morph.scheme))
        		this.morph.scheme = "default_rain";
        setPreset(this.morph.scheme);
    	if (!toggled) {
        	presets.list.clear();
        	for (String preset : Blockbuster.proxy.particles.presets.keySet())
        		presets.list.add(preset);
        	presets.list.setCurrent(this.morph.scheme);
        	presets.list.update();
        }
	}

}
