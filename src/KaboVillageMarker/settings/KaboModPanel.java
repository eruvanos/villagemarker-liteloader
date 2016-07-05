package KaboVillageMarker.settings;

import KaboVillageMarker.LiteModKaboVillageMarker;
import KaboVillageMarker.buttons.KaboSlider;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guntherdw
 */
public class KaboModPanel extends Gui implements ConfigPanel {

    private static final int CONTROL_SPACING = 12;

    private Minecraft mc;
    private List<GuiButton> controlList = new ArrayList<GuiButton>();
    // private List<KaboModSettings.Options> controlList = new ArrayList<GuiButton>();
    private LiteModKaboVillageMarker mod;
    private GuiButton activeControl;

    public KaboModPanel() {
        this.mc = Minecraft.getMinecraft();
    }

    /**
     * Panels should return the text to display at the top of the config panel window
     */
    @Override
    public String getPanelTitle() {
        return "Kabo's Villages Marker mod Settings";
    }

    /**
     * Get the height of the content area for scrolling purposes, return -1 to disable scrolling
     */
    @Override
    public int getContentHeight() {
        return KaboModSettings.Options.values().length * CONTROL_SPACING;
    }

    /**
     * Called when the panel is displayed, initialise the panel (read settings, etc)
     *
     * @param host panel host
     */
    @Override
    public void onPanelShown(ConfigPanelHost host) {
        this.mod = host.getMod();
        int id = 0;
        int spot = 0;

        this.controlList.clear();
        int top = 13;

        for(KaboModSettings.Options option : KaboModSettings.Options.values()) {
            if(option.getEnumBoolean()) {
                GuiCheckbox cb = new GuiCheckbox(id, 24, top + spot * CONTROL_SPACING, option.getEnumString());
                cb.checked = KaboModSettings.instance.getOptionOrdinalValue(option);
                controlList.add(cb);
                spot++;
            } else if(option.getEnumFloat()) {
                // y = 75
                KaboSlider slider =  new KaboSlider(id + 100, 110, 75, KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY);
                // slider.displayString = KaboModSettings.instance.getKeyBinding(KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY);
                controlList.add(slider);
            } else {
                // if(playerName.equals(Alex))
                // y = 99
                GuiButton gb = new GuiButton(id + 200, 110, 99, 150, 20, option.getEnumString());
                gb.displayString = KaboModSettings.KVM_DOTSIZE[KaboModSettings.instance.dotSize];

                controlList.add(gb);
            }
            id++;
        }


    }

    /**
     * Called when the window is resized whilst the panel is active
     *
     * @param host panel host
     */
    @Override
    public void onPanelResize(ConfigPanelHost host) {

    }

    /**
     * Called when the panel is closed, panel should save settings
     */
    @Override
    public void onPanelHidden() {
        KaboModSettings.instance.saveOptions();
    }

    /**
     * Called every tick
     *
     * @param host
     */
    @Override
    public void onTick(ConfigPanelHost host) {

    }

    /**
     * Draw the configuration panel
     *
     * @param host
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    @Override
    public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks) {

        this.drawString(this.mc.fontRendererObj, "Sphere density",  24, 80, 0xFFFFFF);
        this.drawString(this.mc.fontRendererObj, "Dot size",  24, 105, 0xFFFFFF);

        for (GuiButton control : this.controlList) {
            control.drawButton(this.mc, mouseX, mouseY);
        }
    }

    /**
     * Called when a mouse button is pressed
     *
     * @param host
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {
        boolean makeActive = true;

        for (GuiButton control : this.controlList) {
            if (control.mousePressed(this.mc, mouseX, mouseY)) {
                if (makeActive) {
                    makeActive = false;
                    this.activeControl = control;
                    this.actionPerformed(control);
                }
            }
        }
    }

    private void actionPerformed(GuiButton control) {
        if (control instanceof GuiCheckbox) {
            GuiCheckbox chk = (GuiCheckbox) control;
            chk.checked = !chk.checked;
            KaboModSettings.Options option = KaboModSettings.Options.getEnumOptions(control.id);
            KaboModSettings.instance.setOptionValue(option, 0);
        }

        if(control.id > 200) { // BUTTON
            KaboModSettings.instance.setOptionValue(KaboModSettings.Options.KVM_DOTSIZE, 0);
            control.displayString = KaboModSettings.KVM_DOTSIZE[KaboModSettings.instance.dotSize];
        }
    }

    /**
     * Called when a mouse button is released
     *
     * @param host
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {
        if (this.activeControl != null) {
            this.activeControl.mouseReleased(mouseX, mouseY);
            this.activeControl = null;
        }
    }

    /**
     * Called when the mouse is moved
     *
     * @param host
     * @param mouseX
     * @param mouseY
     */
    @Override
    public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY) {
        /*if(this.activeControl != null) {
            GuiButton control = this.activeControl;
            if (control instanceof KaboSlider) {
                KaboSlider slider = (KaboSlider) control;
                System.out.println("Moved in the slider! new value : "+slider.sliderValue);
                KaboModSettings.instance.setOptionFloatValue(KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY, slider.sliderValue);
                control.displayString = KaboModSettings.instance.getKeyBinding(KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY);
            }
        }*/
    }

    /**
     * Called when a key is pressed
     *
     * @param host
     * @param keyChar
     * @param keyCode
     */
    @Override
    public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            host.close();
            return;
        }
    }
}
