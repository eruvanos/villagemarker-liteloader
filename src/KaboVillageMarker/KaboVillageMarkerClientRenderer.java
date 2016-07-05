package KaboVillageMarker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import KaboVillageMarker.settings.KaboModSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;

import net.minecraft.util.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

import org.lwjgl.opengl.GL11;

import static com.mumfrey.liteloader.gl.GL.glPopMatrix;
import static com.mumfrey.liteloader.gl.GL.glPushMatrix;
import static com.mumfrey.liteloader.gl.GL.glTranslated;

public class KaboVillageMarkerClientRenderer {

    public static Minecraft mc = Minecraft.getMinecraft();


    public static void renderVillageMarker(Tessellator tess, EntityPlayerSP thePlayer, float partialTicks) {

        // if(!LiteModKaboVillageMarker.visibility) return;

        if (mc.isIntegratedServerRunning()) {
            if (KaboModSettings.instance.drawVillages) {
                renderSpheresAndLines(tess, thePlayer, partialTicks);
            }
        } else if (!KaboVillageMarkerClient.instance.getVillageListForDimension(mc.thePlayer.dimension).isEmpty() && KaboModSettings.instance.drawVillages) {
            renderSpheresAndLinesMP(tess, thePlayer, partialTicks);
        }

    }

    private static double getPlayerXGuess(EntityPlayerSP thePlayer, float renderTick) {
        return thePlayer.prevPosX + ((thePlayer.posX - thePlayer.prevPosX) * renderTick);
    }

    private static double getPlayerYGuess(EntityPlayerSP thePlayer, float renderTick) {
        return thePlayer.prevPosY + ((thePlayer.posY - thePlayer.prevPosY) * renderTick);
    }

    private static double getPlayerZGuess(EntityPlayerSP thePlayer, float renderTick) {
        return thePlayer.prevPosZ + ((thePlayer.posZ - thePlayer.prevPosZ) * renderTick);
    }

    private static void renderSpheresAndLines(Tessellator tess, EntityPlayerSP thePlayer, float partialTicks) {
        ArrayList villageList = new ArrayList();
        villageList.addAll(mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension).getVillageCollection().getVillageList());
        Iterator villageListIterator = villageList.iterator();
        int c = 0;

        while (villageListIterator.hasNext()) {
            Object vObj = villageListIterator.next();
            if (vObj instanceof Village) {
                int vRad = ((Village)vObj).getVillageRadius();
                BlockPos vCen = ((Village)vObj).getCenter();
                ArrayList vDoors = new ArrayList();
                vDoors.addAll(((Village)vObj).getVillageDoorInfoList());


                if (LiteModKaboVillageMarker.fullbright) {
                    RenderHelper.disableStandardItemLighting();
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                    // GL11.glDisable(GL11.GL_LIGHTING);
                }

                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
                setColor(c % 6);
                ++c;
                GL11.glPointSize((float)(KaboModSettings.instance.dotSize + 1));
                GL11.glLineWidth(3.0F);
                GL11.glEnable(GL11.GL_POINT_SMOOTH);
                GlStateManager.depthMask(false);
                glPushMatrix();


                glTranslated(-getPlayerXGuess(thePlayer, partialTicks),
                             -getPlayerYGuess(thePlayer, partialTicks),
                             -getPlayerZGuess(thePlayer, partialTicks));
                // tess.getWorldRenderer().setTranslation(-xOff, -yOff, -zOff);

                if (KaboModSettings.instance.drawSphere) {
                    float villageDoors = KaboModSettings.instance.sphereDensity;
                    int i$ = 24 + (int)(villageDoors * 72.0F);
                    double[] dObj = new double[i$ * (i$ / 2 + 1)];
                    double[] doorInfo = new double[i$ * (i$ / 2 + 1)];
                    double[] ys = new double[i$ * (i$ / 2 + 1)];

                    for (double t = 0.0D; t < 6.283185307179586D; t += 6.283185307179586D / (double)i$) {
                        for (double theta = 0.0D; theta < 3.141592653589793D; theta += 3.141592653589793D / (double)(i$ / 2)) {
                            double dx = (double)vRad * Math.sin(t) * Math.cos(theta);
                            double dz = (double)vRad * Math.sin(t) * Math.sin(theta);
                            double dy = (double)vRad * Math.cos(t);
                            int index = (int)(t / (6.283185307179586D / (double)i$) + (double)i$ * theta / (3.141592653589793D / (double)(i$ / 2)));
                            dObj[index] = (double)vCen.getX() + dx;
                            doorInfo[index] = (double)vCen.getZ() + dz;
                            ys[index] = (double)vCen.getY() + dy;
                        }
                    }

                    tess.getWorldRenderer().startDrawing(0);

                    for (int var34 = 0; var34 < i$ * (i$ / 2 + 1); ++var34) {
                        tess.getWorldRenderer().addVertex(dObj[var34], ys[var34], doorInfo[var34]);
                    }

                    tess.draw();
                }

                tess.getWorldRenderer().startDrawing(1);
                Iterator var31 = vDoors.iterator();
                Iterator var30 = vDoors.iterator();

                while (var30.hasNext()) {
                    Object var32 = var30.next();
                    if (var32 instanceof VillageDoorInfo) {
                        VillageDoorInfo vdi = (VillageDoorInfo) var32;
                        BlockPos var33 = vdi.getDoorBlockPos();
                        tess.getWorldRenderer().addVertex((double) var33.getX(), (double) var33.getY(), (double) var33.getZ());
                        tess.getWorldRenderer().addVertex((double) vCen.getX(), (double) vCen.getY(), (double) vCen.getZ());
                    }
                }

                tess.draw();
                if (KaboModSettings.instance.drawGolemArea) {
                    renderGolemBox(tess, vCen, c);
                }

                glPopMatrix();

                if (LiteModKaboVillageMarker.fullbright) {
                    // GL11.glEnable(GL11.GL_LIGHTING);
                    RenderHelper.enableStandardItemLighting();
                }

                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(true);
                tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
            }
        }

    }

    private static void renderSpheresAndLinesMP(Tessellator tess, EntityPlayerSP thePlayer, float partialTicks) {
        ArrayList villageList = new ArrayList();
        villageList.addAll(KaboVillageMarkerClient.instance.getVillageListForDimension(mc.thePlayer.dimension));
        Iterator villageListIterator = villageList.iterator();
        int c = 0;

        while (villageListIterator.hasNext()) {
            Object vObj = villageListIterator.next();
            if (vObj instanceof KaboVillageMarkerClient.KaboVillageMarkerVillage) {
                int vRad = ((KaboVillageMarkerClient.KaboVillageMarkerVillage)vObj).radius;
                BlockPos vCen = ((KaboVillageMarkerClient.KaboVillageMarkerVillage)vObj).getCenter();
                List vDoors = ((KaboVillageMarkerClient.KaboVillageMarkerVillage)vObj).villageDoors;

                if (LiteModKaboVillageMarker.fullbright) {
                    RenderHelper.disableStandardItemLighting();
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                    // GL11.glDisable(GL11.GL_LIGHTING);
                }

                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);

                glPushMatrix();
                glTranslated(-getPlayerXGuess(thePlayer, partialTicks),
                             -getPlayerYGuess(thePlayer, partialTicks),
                             -getPlayerZGuess(thePlayer, partialTicks));

                GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
                setColor(c % 6);
                ++c;
                GL11.glPointSize((float)(KaboModSettings.instance.dotSize + 1));
                GL11.glLineWidth(2.0F);
                GL11.glEnable(GL11.GL_POINT_SMOOTH);

                if (KaboModSettings.instance.drawSphere) {
                    float i$ = KaboModSettings.instance.sphereDensity;
                    int door = 24 + (int)(i$ * 72.0F);
                    double[] xs = new double[door * (door / 2 + 1)];
                    double[] zs = new double[door * (door / 2 + 1)];
                    double[] ys = new double[door * (door / 2 + 1)];

                    for (double t = 0.0D; t < 6.283185307179586D; t += 6.283185307179586D / (double)door) {
                        for (double theta = 0.0D; theta < 3.141592653589793D; theta += 3.141592653589793D / (double)(door / 2)) {
                            double dx = (double)vRad * Math.sin(t) * Math.cos(theta);
                            double dz = (double)vRad * Math.sin(t) * Math.sin(theta);
                            double dy = (double)vRad * Math.cos(t);
                            int index = (int)(t / (6.283185307179586D / (double)door) + (double)door * theta / (3.141592653589793D / (double)(door / 2)));
                            xs[index] = (double)vCen.getX() + dx;
                            zs[index] = (double)vCen.getZ() + dz;
                            ys[index] = (double)vCen.getY() + dy;
                        }
                    }

                    tess.getWorldRenderer().startDrawing(GL11.GL_POINTS);

                    for (int var32 = 0; var32 < door * (door / 2 + 1); ++var32) {
                        tess.getWorldRenderer().addVertex(xs[var32], ys[var32], zs[var32]);
                    }

                    tess.draw();
                }

                tess.getWorldRenderer().startDrawing(1);
                Iterator var31 = vDoors.iterator();

                while (var31.hasNext()) {
                    KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition var30 = (KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition)var31.next();
                    tess.getWorldRenderer().addVertex((double) var30.x, (double) var30.y, (double) var30.z);
                    tess.getWorldRenderer().addVertex((double) vCen.getX(), (double) vCen.getY(), (double) vCen.getZ());
                }

                tess.draw();
                if (KaboModSettings.instance.drawGolemArea) {
                    renderGolemBox(tess, vCen, c);
                }

                glPopMatrix();

                if (LiteModKaboVillageMarker.fullbright) {
                    // GL11.glEnable(GL11.GL_LIGHTING);
                    RenderHelper.enableStandardItemLighting();
                }

                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(true);
                tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void renderGolemBox(Tessellator tess, BlockPos vCen, int c) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.disableCull();
        tess.getWorldRenderer().startDrawing(GL11.GL_QUADS);
        GL11.glLineWidth(2.0F);
        setWallColor((c - 1) % 6);
        GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.draw();
        setColor((c - 1) % 6);
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
        tess.getWorldRenderer().startDrawing(GL11.GL_QUADS);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D);
        tess.getWorldRenderer().addVertex((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D);
        tess.draw();
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        GL11.glLineStipple(5, (short)-30584);
        tess.getWorldRenderer().startDrawing(GL11.GL_LINES);

        for (int xi = -8; xi <= 8; ++xi) {
            int yi;
            for (yi = -3; yi <= 3; ++yi) {
                if (xi == -8 || xi == 8 || yi == -3 || yi == 3) {
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() + xi), (double) (vCen.getY() + yi), (double) (vCen.getZ() - 8));
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() + xi), (double) (vCen.getY() + yi), (double) (vCen.getZ() + 8));
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() - 8), (double) (vCen.getY() + yi), (double) (vCen.getZ() + xi));
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() + 8), (double) (vCen.getY() + yi), (double) (vCen.getZ() + xi));
                }
            }

            for (yi = -8; yi <= 8; ++yi) {
                if (xi == -8 || xi == 8 || yi == -8 || yi == 8) {
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() + xi), (double) (vCen.getY() + 3), (double) (vCen.getZ() + yi));
                    tess.getWorldRenderer().addVertex((double) (vCen.getX() + xi), (double) (vCen.getY() - 3), (double) (vCen.getZ() + yi));
                }
            }
        }

        tess.draw();
        tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.enableCull();
    }

    private static void setColor(int c) {
        if (c == 0) {
            GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
        } else if (c == 4) {
            GlStateManager.color(0.0F, 1.0F, 0.0F, 1.0F);
        } else if (c == 2) {
            GlStateManager.color(0.0F, 0.0F, 1.0F, 1.0F);
        } else if (c == 5) {
            GlStateManager.color(1.0F, 1.0F, 0.0F, 1.0F);
        } else if (c == 1) {
            GlStateManager.color(1.0F, 0.0F, 1.0F, 1.0F);
        } else if (c == 3) {
            GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

    }

    private static void setWallColor(int c) {
        if (c == 0) {
            GlStateManager.color(0.12F, 0.0F, 0.0F, 0.25F);
        } else if (c == 4) {
            GlStateManager.color(0.0F, 0.1F, 0.0F, 0.25F);
        } else if (c == 2) {
            GlStateManager.color(0.0F, 0.0F, 0.125F, 0.25F);
        } else if (c == 5) {
            GlStateManager.color(0.105F, 0.105F, 0.0F, 0.25F);
        } else if (c == 1) {
            GlStateManager.color(0.105F, 0.0F, 0.105F, 0.25F);
        } else if (c == 3) {
            GlStateManager.color(0.0F, 0.105F, 0.105F, 0.25F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        }

    }

}
