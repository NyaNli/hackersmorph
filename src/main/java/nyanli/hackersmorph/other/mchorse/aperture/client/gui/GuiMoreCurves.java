package nyanli.hackersmorph.other.mchorse.aperture.client.gui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiCurves;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.Keyframe;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import nyanli.hackersmorph.other.mchorse.aperture.client.manager.CameraEditorManager;

public class GuiMoreCurves extends GuiCurves {
	
	private LinkedHashMap<String, String> strMap;
	private String selected;
	
	private GuiButtonElement curveSelector;
	private GuiSearchListElement<String> curveList;
	
	public GuiMoreCurves(Minecraft mc, GuiCameraEditor editor) {
		super(mc, editor);
		
		this.strMap = null;
		this.selected = null;
		
		this.curveSelector = new GuiButtonElement(mc, IKey.str("HelloWorld"), gui -> toggleCurveList());
		this.curveSelector.flex().relative(this).y(1f).w(1f).anchorY(1f);
		
		this.curveList = new GuiSearchListElement<String>(mc, list -> selectCurve(list.get(0))) {
			
			@Override
			protected GuiListElement<String> createList(Minecraft mc, Consumer<List<String>> callback) {
				GuiListElement<String> list = new GuiListElement<String>(mc, callback) {

					@Override
					protected boolean sortElements() {
						return false;
					}

					@Override
					protected String elementToString(String element) {
						KeyframeChannel channel = editor.getProfile().getCurves().get(element);
						String hasKey = channel != null && !channel.isEmpty() ? "*" : "";
						if (strMap == null)
							return hasKey + element;
						return hasKey + strMap.getOrDefault(element, element);
					}
					
				};
				list.background(0xC0000000);
				list.scroll.scrollSpeed = 20;
				return list;
			}
		};
		this.curveList.flex().relative(this).x(1f).y(1.0f, -this.curveSelector.flex().getH()).w(1.0f).h(1.0f, -this.curveSelector.flex().getH()).anchorX(1f).anchorY(1f);
		this.curveList.resize();
		
		this.add(this.curveSelector, this.curveList);
		
		Consumer<Keyframe> callback = this.keyframes.graph.callback;
		this.keyframes.graph.callback = key -> {
			callback.accept(key);
			this.editor.haveScrubbed();
		};
	}
	
	@Override
	public void update() {
		this.strMap = new LinkedHashMap<String, String>();
		this.strMap.put("brightness", I18n.format("hackersmorph.gui.curve.brightness"));
		this.strMap.putAll(CameraEditorManager.getAddonCurves());

		this.curveList.list.clear();
		this.curveList.list.add(strMap.keySet());
		
		if (!this.strMap.containsKey(this.selected))
			selectCurve("brightness");
		else
			selectCurve(this.selected);
	}
	
	public void toggleCurveList() {
		this.curveList.toggleVisible();
	}
	
	public void selectCurve(String selected) {
		this.selected = selected;
		this.curveList.list.setCurrent(this.selected);
		this.curveList.setVisible(false);
		this.curveSelector.label.set(this.strMap.get((this.selected)));
		Map<String, KeyframeChannel> channels = this.editor.getProfile().getCurves();
		KeyframeChannel channel = channels.get(this.selected);
		if (channel == null) {
			channel = new KeyframeChannel();
			channels.put(this.selected, channel);
		}
		this.keyframes.graph.duration = (int) this.editor.getProfile().getDuration();
		Random random = new Random();
		this.keyframes.setChannel(channel, Color.HSBtoRGB(random.nextFloat(), 1.0f, 1.0f));
	}
	
}
