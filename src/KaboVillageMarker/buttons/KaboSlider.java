package KaboVillageMarker.buttons;

import KaboVillageMarker.settings.KaboModSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;

public class KaboSlider extends GuiButton {
    public float sliderValue;
    public boolean dragging;
    private KaboModSettings.Options options;
    private final float sliderMinValue;
    private final float sliderMaxValue;

    public KaboSlider(int p_i45016_1_, int p_i45016_2_, int p_i45016_3_, KaboModSettings.Options p_i45016_4_) {
        this(p_i45016_1_, p_i45016_2_, p_i45016_3_, p_i45016_4_, 0.0F, 1.0F);
    }

    public KaboSlider(int p_i45017_1_, int p_i45017_2_, int p_i45017_3_, KaboModSettings.Options p_i45017_4_, float p_i45017_5_, float p_i45017_6_) {
        super(p_i45017_1_, p_i45017_2_, p_i45017_3_, 150, 20, "");
        this.sliderValue = 1.0F;
        this.options = p_i45017_4_;
        this.sliderMinValue = p_i45017_5_;
        this.sliderMaxValue = p_i45017_6_;

        this.sliderValue = p_i45017_4_.normalizeValue(KaboModSettings.instance.getOptionFloatValue(p_i45017_4_));
        this.displayString = KaboModSettings.instance.getKeyBinding(p_i45017_4_);
        // Minecraft var7 = Minecraft.getMinecraft();
        // this.sliderValue = p_i45017_4_.normalizeValue(var7.gameSettings.getOptionFloatValue(p_i45017_4_));
        // this.displayString = var7.gameSettings.getKeyBinding(p_i45017_4_);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                float var4 = this.options.denormalizeValue(this.sliderValue);
                this.sliderValue = this.options.normalizeValue(var4);
                KaboModSettings.instance.setOptionFloatValue(this.options, this.sliderValue);
                this.displayString = KaboModSettings.instance.getKeyBinding(this.options);
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            // mc.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
            KaboModSettings.instance.setOptionFloatValue(this.options, this.sliderValue);
            this.displayString = KaboModSettings.instance.getKeyBinding(this.options);
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }
}
