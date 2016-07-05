package KaboVillageMarker.settings;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KaboModSettings {

    public static KaboModSettings instance = new KaboModSettings(Minecraft.getMinecraft(), (File) null);
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };
    public static final String[] KVM_DOTSIZE = new String[]{"Small", "Medium", "Large"};
    public boolean drawGolemArea = true;
    public boolean drawSphere = true;
    public boolean drawVillages = true;
    public float sphereDensity = 0.22535211F;
    public int dotSize = 0;
    public boolean debug = false;
    public KeyBinding[] keyBindsHotbar = new KeyBinding[0];
    public KeyBinding[] keyBindings = (KeyBinding[]) ((KeyBinding[]) ArrayUtils.addAll(new KeyBinding[0], new KeyBinding[0]));
    protected Minecraft mc;
    private File optionsFile;


    public KaboModSettings(Minecraft par1Minecraft, File par2File) {
        this.mc = par1Minecraft;
        this.optionsFile = new File(par2File == null ? par1Minecraft.mcDataDir : par2File, "kaboModOptions.txt");
        this.loadOptions();
    }

    public KaboModSettings() {
    }

    public static String getKeyDisplayString(int par0) {
        return par0 < 0 ? I18n.format("key.mouseButton", new Object[]{Integer.valueOf(par0 + 101)}) : Keyboard.getKeyName(par0);
    }

    public static boolean isKeyDown(KeyBinding par0KeyBinding) {
        return par0KeyBinding.getKeyCode() == 0 ? false : (par0KeyBinding.getKeyCode() < 0 ? Mouse.isButtonDown(par0KeyBinding.getKeyCode() + 100) : Keyboard.isKeyDown(par0KeyBinding.getKeyCode()));
    }

    public void setKeyCodeSave(KeyBinding p_151440_1_, int p_151440_2_) {
        p_151440_1_.setKeyCode(p_151440_2_);
        this.saveOptions();
    }

    public void setOptionFloatValue(KaboModSettings.Options par1EnumOptions, float par2) {
        if (par1EnumOptions == KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY) {
            this.sphereDensity = par2;
        }

    }

    public void setOptionValue(KaboModSettings.Options par1EnumOptions, int par2) {
        if (par1EnumOptions == KaboModSettings.Options.KVM_DRAW_VILLAGES) {
            this.drawVillages = !this.drawVillages;
        }

        if (par1EnumOptions == KaboModSettings.Options.KVM_DRAW_GOLEM_AREA) {
            this.drawGolemArea = !this.drawGolemArea;
        }

        if (par1EnumOptions == KaboModSettings.Options.KVM_DRAW_VILLAGESPHERE) {
            this.drawSphere = !this.drawSphere;
        }

        if (par1EnumOptions == KaboModSettings.Options.KVM_DOTSIZE) {
            this.dotSize = (this.dotSize + 1) % 3;
        }

        this.saveOptions();
    }

    public float getOptionFloatValue(KaboModSettings.Options par1EnumOptions) {
        return par1EnumOptions == KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY ? this.sphereDensity : 0.0F;
    }

    public boolean getOptionOrdinalValue(KaboModSettings.Options par1EnumOptions) {
        switch (KaboModSettings.SwitchOptions.optionIds[par1EnumOptions.ordinal()]) {
            case 1:
                return this.drawVillages;
            case 2:
                return this.drawGolemArea;
            case 3:
                return this.drawSphere;
            case 5:
                return this.debug;
            default:
                return false;
        }
    }

    private static String getTranslation(String[] par0ArrayOfStr, int par1) {
        if (par1 < 0 || par1 >= par0ArrayOfStr.length) {
            par1 = 0;
        }

        return I18n.format(par0ArrayOfStr[par1], new Object[0]);
    }

    public String getKeyBinding(KaboModSettings.Options par1EnumOptions) {
        String var2 = I18n.format(par1EnumOptions.getEnumString(), new Object[0]) + ": ";
        if (par1EnumOptions.getEnumFloat()) {
            float var51 = this.getOptionFloatValue(par1EnumOptions);
            float var4 = par1EnumOptions.normalizeValue(var51);
            return par1EnumOptions == KaboModSettings.Options.KVM_VILLAGE_SPHERE_DENSITY ? (24 + (int) (this.sphereDensity * 72.0F)) * (24 + (int) (this.sphereDensity * 72.0F) / 2 + 1) + " dots" :
                (var4 == 0.0F ? var2 + I18n.format("options.off", new Object[0]) : var2 + (int) (var4 * 100.0F) + "%");
        } else if (par1EnumOptions.getEnumBoolean()) {
            boolean var5 = this.getOptionOrdinalValue(par1EnumOptions);
            return var5 ? var2 + I18n.format("options.on", new Object[0]) : var2 + I18n.format("options.off", new Object[0]);
        } else {
            return par1EnumOptions == KaboModSettings.Options.KVM_DOTSIZE ? "Dot Size: " + KVM_DOTSIZE[this.dotSize] : var2;
        }
    }

    public void loadOptions() {
        try {
            if (!this.optionsFile.exists()) {
                // return;
                // Try creating a clean config first
                this.saveOptions();
                return;
            }

            BufferedReader var9 = new BufferedReader(new FileReader(this.optionsFile));
            String var2 = "";

            while ((var2 = var9.readLine()) != null) {
                try {
                    String[] var8 = var2.split(":");
                    if (var8[0].equals("VillageMarker-DrawVillages")) {
                        this.drawVillages = var8[1].equals("true");
                    }

                    if (var8[0].equals("VillageMarker-DrawGolemArea")) {
                        this.drawGolemArea = var8[1].equals("true");
                    }

                    if (var8[0].equals("VillageMarker-DrawVillageSphere")) {
                        this.drawSphere = var8[1].equals("true");
                    }

                    if (var8[0].equals("VillageMarker-SphereDensity")) {
                        this.sphereDensity = this.parseFloat(var8[1]);
                    }

                    if (var8[0].equals("VillageMarker-DotSize")) {
                        this.dotSize = Integer.parseInt(var8[1]);
                    }

                    if (var8[0].equals("VillageMarker-Debug")) {
                        this.debug = Boolean.parseBoolean(var8[1]);
                    }

                    KeyBinding[] var4 = this.keyBindings;
                    int var5 = var4.length;

                    for (int var6 = 0; var6 < var5; ++var6) {
                        KeyBinding var7 = var4[var6];
                        if (var8[0].equals("key_" + var7.getKeyDescription())) {
                            var7.setKeyCode(Integer.parseInt(var8[1]));
                        }
                    }
                } catch (Exception var81) {
                    logger.warn("Skipping bad option: " + var2);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
            var9.close();
        } catch (Exception var91) {
            logger.error("Failed to load options", var91);
        }

    }

    private float parseFloat(String par1Str) {
        return par1Str.equals("true") ? 1.0F : (par1Str.equals("false") ? 0.0F : Float.parseFloat(par1Str));
    }

    public void saveOptions() {
        try {
            PrintWriter var6 = new PrintWriter(new FileWriter(this.optionsFile));
            var6.println("VillageMarker-DrawVillages:" + this.drawVillages);
            var6.println("VillageMarker-DrawGolemArea:" + this.drawGolemArea);
            var6.println("VillageMarker-DrawVillageSphere:" + this.drawSphere);
            var6.println("VillageMarker-SphereDensity:" + this.sphereDensity);
            var6.println("VillageMarker-DotSize:" + this.dotSize);
            var6.println("VillageMarker-Debug:" + this.debug);

            KeyBinding[] var2 = this.keyBindings;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                KeyBinding var5 = var2[var4];
                var6.println("key_" + var5.getKeyDescription() + ":" + var5.getKeyCode());
            }

            var6.close();
        } catch (Exception var61) {
            logger.error("Failed to save options", var61);
        }

        this.sendSettingsToServer();
    }

    public void sendSettingsToServer() {
        if (this.mc.thePlayer != null) {
            ;
        }

    }


    static final class SwitchOptions {

        static final int[] optionIds = new int[KaboModSettings.Options.values().length];


        static {
            try {
                optionIds[KaboModSettings.Options.KVM_DRAW_VILLAGES.ordinal()] = 1;
            } catch (NoSuchFieldError var3) {
                ;
            }

            try {
                optionIds[KaboModSettings.Options.KVM_DRAW_GOLEM_AREA.ordinal()] = 2;
            } catch (NoSuchFieldError var2) {
                ;
            }

            try {
                optionIds[KaboModSettings.Options.KVM_DRAW_VILLAGESPHERE.ordinal()] = 3;
            } catch (NoSuchFieldError var1) {
                ;
            }

            try {
                optionIds[KaboModSettings.Options.KVM_DEBUG.ordinal()] = 5;
            } catch (NoSuchFieldError var1) {
                ;
            }

        }
    }

    public enum Options {

        KVM_DRAW_VILLAGES("KVM_DRAW_VILLAGES", 0, "DRAW_VILLAGES", 0, "Draw Villages", false, true),
        KVM_DRAW_GOLEM_AREA("KVM_DRAW_GOLEM_AREA", 1, "DRAW_GOLEM_AREA", 1, "Draw Golem Spawn Area", false, true),
        KVM_VILLAGE_SPHERE_DENSITY("KVM_VILLAGE_SPHERE_DENSITY", 2, "VILLAGE_SPHERE_DENSITY", 2, "Sphere Density", true, false),
        KVM_DRAW_VILLAGESPHERE("KVM_DRAW_VILLAGESPHERE", 3, "DRAW_VILLAGESPHERE", 3, "Draw Dots", false, true),
        KVM_DOTSIZE("KVM_DOTSIZE", 4, "DOT_SIZE", 16, "Dot Size", false, false),
        KVM_DEBUG("KVM_DEBUG", 5, "DEBUG", 16, "Debug mode", false, true);
        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private float valueMin;
        private float valueMax;


        public static KaboModSettings.Options getEnumOptions(int par0) {
            KaboModSettings.Options[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                KaboModSettings.Options var4 = var1[var3];
                if (var4.returnEnumOrdinal() == par0) {
                    return var4;
                }
            }

            return null;
        }

        Options(String var1, int var2, String par1Str, int par2, String par3Str, boolean par4, boolean par5) {
            this(var1, var2, par1Str, par2, par3Str, par4, par5, 0.0F, 1.0F, 0.0F);
        }

        Options(String var1, int var2, String p_i45004_1_, int p_i45004_2_, String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_) {
            this.enumString = p_i45004_3_;
            this.enumFloat = p_i45004_4_;
            this.enumBoolean = p_i45004_5_;
            this.valueMin = p_i45004_6_;
            this.valueMax = p_i45004_7_;
            this.valueStep = p_i45004_8_;
        }

        public boolean getEnumFloat() {
            return this.enumFloat;
        }

        public boolean getEnumBoolean() {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal() {
            return this.ordinal();
        }

        public String getEnumString() {
            return this.enumString;
        }

        public float getValueMax() {
            return this.valueMax;
        }

        public float getValueMin() {
            return valueMin;
        }

        public void setValueMax(float p_148263_1_) {
            this.valueMax = p_148263_1_;
        }

        public float normalizeValue(float p_148266_1_) {
            return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_) {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_) {
            p_148268_1_ = this.snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
        }

        protected float snapToStep(float p_148264_1_) {
            if (this.valueStep > 0.0F) {
                p_148264_1_ = this.valueStep * (float) Math.round(p_148264_1_ / this.valueStep);
            }

            return p_148264_1_;
        }
    }
}
