package KaboVillageMarker;

import KaboVillageMarker.settings.KaboModPanel;
import KaboVillageMarker.settings.KaboModSettings;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.*;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.util.log.LiteLoaderLogger;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author guntherdw
 */
public class LiteModKaboVillageMarker implements InitCompleteListener, JoinGameListener, Configurable, PluginChannelListener, PostRenderListener {

    protected final String POLL_CHANNEL = "KVM|Poll";
    protected final String DATA_CHANNEL = "KVM|Data";
    protected final String DATA_CHANNEL_COMPRESSED = "KVM|DataComp";

    protected final String ANSWER_CHANNEL = "KVM|Answer";

    private static boolean hasReceivedUpdate = false;
    private static boolean delayedCheck = false;

    private static int DELAYED_CHECK_TICKS = 80;
    private static int checkTime = 0;

    protected static boolean fullbright = true;
    // protected static boolean visibility = true;

    /* protected KeyBinding markerToggle_vis = new KeyBinding("Toggle VillageMarker visibility", Keyboard.KEY_V, "VillageMarker");
    protected KeyBinding markerToggle_fb  = new KeyBinding("Toggle VillageMarker fullbright", Keyboard.KEY_B, "VillageMarker");
    protected KeyBinding markerToggle_reload  = new KeyBinding("Reload options", Keyboard.KEY_X, "VillageMarker"); */

    /**
     * Called when a custom payload packet arrives on a channel this mod has registered
     *
     * @param channel Channel on which the custom payload was received
     * @param data    Custom payload data
     */
    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        // System.out.println("Received "+data.array().length+" bytes on "+channel+"!");
        if (POLL_CHANNEL.equals(channel)) {
            String uuidString = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();

            try {
                byte[] bytes = uuidString.getBytes(Charsets.UTF_8);
                PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
                buff.writeBytes(bytes);

                C17PacketCustomPayload var7 = new C17PacketCustomPayload(ANSWER_CHANNEL, buff);
                Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(var7);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (DATA_CHANNEL_COMPRESSED.equals(channel)) {
            try {
                // System.out.println("Recevied packet ("+channel+") : "+(new String(data.array())));
                if (!hasReceivedUpdate) hasReceivedUpdate = true;

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.array());
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = bufferedReader.readLine();

                bufferedReader.close();
                inputStreamReader.close();
                gzipInputStream.close();
                byteArrayInputStream.close();

                if (KaboModSettings.instance.debug)
                    LiteLoaderLogger.getLogger().log(Level.INFO, line);

                KaboVillageMarkerClient.instance.bufferVillageDataString(line);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (DATA_CHANNEL.equals(channel)) {
            try {
                // System.out.println("Recevied packet ("+channel+") : "+(new String(data.array())));
                if (!hasReceivedUpdate) hasReceivedUpdate = true;
                String dataString = new String(data.array());

                if (KaboModSettings.instance.debug)
                    LiteLoaderLogger.getLogger().log(Level.INFO, dataString);

                KaboVillageMarkerClient.instance.bufferVillageDataString(dataString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Return a list of the plugin channels the mod wants to register
     *
     * @return plugin channel names as a list, it is recommended to use {@link ImmutableList#of} for this purpose
     */
    @Override
    public List<String> getChannels() {
        return Arrays.asList(new String[] {POLL_CHANNEL, DATA_CHANNEL, DATA_CHANNEL_COMPRESSED, ANSWER_CHANNEL});
    }

    private void addChat(Minecraft minecraft, String message) {
        minecraft.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("[VillageMarker] §6" + message));
    }

    /**
     * Called every frame
     *
     * @param minecraft    Minecraft instance
     * @param partialTicks Partial tick value
     * @param inGame       True if in-game, false if in the menu
     * @param clock        True if this is a new tick, otherwise false if it's a regular frame
     */
    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if(!inGame) return;

        if (clock && delayedCheck) {
            if (checkTime == 0) {
                if (hasReceivedUpdate)
                    LiteLoaderLogger.getLogger().log(Level.INFO, "Client received VM updates normally");
                else {
                    LiteLoaderLogger.getLogger().log(Level.INFO, "Client didn't see any update packets in the check time.");
                    LiteLoaderLogger.getLogger().log(Level.INFO, "Check to see if the plugin is running serverside!");
                }
                delayedCheck = false;
            } else {
                checkTime--;
            }
        }

        /* if(!minecraft.inGameHasFocus) return;
        if(markerToggle_fb.isPressed()) {
            fullbright = !fullbright;
            addChat(minecraft, "Marker fullbright is now "+(fullbright?"on":"off")+"!");
        }
        if(markerToggle_vis.isPressed()) {
            visibility = !visibility;
            addChat(minecraft, "Marker visibility is now "+(visibility?"on":"off")+"!");
        }
        if (markerToggle_reload.isPressed()) {
            addChat(minecraft, "Reloading VillageMarker settings file!");
            KaboModSettings.instance.loadOptions();
        } */
    }

    /**
     * Get the mod version string
     *
     * @return the mod version as a string
     */
    @Override
    public String getVersion() {
        String version = LiteLoader.getInstance().getModMetaData(this, "version", "");
        String build = LiteLoader.getInstance().getModMetaData(this, "revision", "");

        return version + (!(build.equals("")) ? " (build: " + build + ")" : "");
    }

    /**
     * Do startup stuff here, minecraft is not fully initialised when this function is called so mods *must not*
     * interact with minecraft in any way here
     *
     * @param configPath Configuration path to use
     */
    @Override
    public void init(File configPath) {
        /* LiteLoader.getInput().registerKeyBinding(markerToggle_vis);
        LiteLoader.getInput().registerKeyBinding(markerToggle_fb);
        LiteLoader.getInput().registerKeyBinding(markerToggle_reload); */
    }

    /**
     * Called when the loader detects that a version change has happened since this mod was last loaded
     *
     * @param version       new version
     * @param configPath    Path for the new version-specific config
     * @param oldConfigPath Path for the old version-specific config
     */
    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {

    }

    /**
     * Get the display name
     *
     * @return display name
     */
    @Override
    public String getName() {
        return "Kabo's village marker";
    }

    /**
     * Called as soon as the game is initialised and the main game loop is running
     *
     * @param minecraft Minecraft instance
     * @param loader    LiteLoader instance
     */
    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {

    }

    /**
     * Called after entities are rendered but before particles
     *
     * @param partialTicks
     */
    @Override
    public void onPostRenderEntities(float partialTicks) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        KaboVillageMarkerClientRenderer.renderVillageMarker(Tessellator.getInstance(), player, partialTicks);
    }

    /**
     * Called after all world rendering is completed
     *
     * @param partialTicks
     */
    @Override
    public void onPostRender(float partialTicks) {

    }

    /**
     * Called on join game
     *
     * @param netHandler     Net handler
     * @param joinGamePacket Join game packet
     * @param serverData     ServerData object representing the server being connected to
     * @param realmsServer   If connecting to a realm, a reference to the RealmsServer object
     */
    @Override
    public void onJoinGame(INetHandler netHandler, S01PacketJoinGame joinGamePacket, ServerData serverData, RealmsServer realmsServer) {

        Logger log = LiteLoaderLogger.getLogger();

        log.log(Level.INFO, "KVM|Poll enabled serverside? "+LiteLoader.getClientPluginChannels().isRemoteChannelRegistered(POLL_CHANNEL));
        log.log(Level.INFO, "KVM|Data enabled serverside? "+LiteLoader.getClientPluginChannels().isRemoteChannelRegistered(DATA_CHANNEL));
        log.log(Level.INFO, "KVM|DataComp enabled serverside? "+LiteLoader.getClientPluginChannels().isRemoteChannelRegistered(DATA_CHANNEL_COMPRESSED));
        log.log(Level.INFO, "KVM|Answer enabled serverside? "+LiteLoader.getClientPluginChannels().isRemoteChannelRegistered(ANSWER_CHANNEL));

        checkTime = DELAYED_CHECK_TICKS;
        delayedCheck = true;
        hasReceivedUpdate = false;

        KaboVillageMarkerClient.instance.translateStringToVillageData("0:");
        KaboVillageMarkerClient.instance.translateStringToVillageData("1:");
        KaboVillageMarkerClient.instance.translateStringToVillageData("-1:");
    }

    /**
     * Get the class of the configuration panel to use, the returned class must have a
     * default (no-arg) constructor
     *
     * @return configuration panel class
     */
    @Override
    public Class<? extends ConfigPanel> getConfigPanelClass() {
        return KaboModPanel.class;
    }
}
