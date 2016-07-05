package KaboVillageMarker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import KaboVillageMarker.settings.KaboModSettings;

import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

import org.lwjgl.opengl.GL11;

import static com.mumfrey.liteloader.gl.GL.glPopMatrix;
import static com.mumfrey.liteloader.gl.GL.glPushMatrix;

public class KaboVillageMarkerClientRenderer {

    public static Minecraft mc = Minecraft.getMinecraft();


    public static void renderVillageMarker(Tessellator tess, Entity renderEntity, float partialTicks) {
        if (mc.isIntegratedServerRunning()) {
            if (KaboModSettings.instance.drawVillages) {
                renderSpheresAndLines(tess, renderEntity, partialTicks);
            }
        } else if (!KaboVillageMarkerClient.instance.getVillageListForDimension(mc.thePlayer.dimension).isEmpty() && KaboModSettings.instance.drawVillages) {
            renderSpheresAndLinesMP(tess, renderEntity, partialTicks);
        }

    }

    private static double getPlayerXGuess(Entity renderEntity, float renderTick) {
        return renderEntity.prevPosX + ((renderEntity.posX - renderEntity.prevPosX) * renderTick);
    }

    private static double getPlayerYGuess(Entity renderEntity, float renderTick) {
        return renderEntity.prevPosY + ((renderEntity.posY - renderEntity.prevPosY) * renderTick);
    }

    private static double getPlayerZGuess(Entity renderEntity, float renderTick) {
        return renderEntity.prevPosZ + ((renderEntity.posZ - renderEntity.prevPosZ) * renderTick);
    }

    @SuppressWarnings("unchecked")
    private static void renderSpheresAndLines(Tessellator tess, Entity renderEntity, float partialTicks) {

        int c = 0;
        VertexBuffer vb = tess.getBuffer();

        for (Village village : (List<Village>) mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension).getVillageCollection().getVillageList()) {
            int vRad = village.getVillageRadius();
            BlockPos vCen = village.getCenter();
            List<VillageDoorInfo> vDoors = new ArrayList<VillageDoorInfo>();
            vDoors.addAll(village.getVillageDoorInfoList());


            if (LiteModKaboVillageMarker.fullbright) {
                RenderHelper.disableStandardItemLighting();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            }

            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
            setColor(c % 6);
            ++c;
            GL11.glPointSize((float) (KaboModSettings.instance.dotSize + 1));
            GlStateManager.glLineWidth(3.0F);
            GL11.glEnable(GL11.GL_POINT_SMOOTH);
            GlStateManager.depthMask(false);
            glPushMatrix();


            vb.setTranslation(-getPlayerXGuess(renderEntity, partialTicks),
                              -getPlayerYGuess(renderEntity, partialTicks),
                              -getPlayerZGuess(renderEntity, partialTicks));
            // tess.getWorldRenderer().setTranslation(-xOff, -yOff, -zOff);

            if (KaboModSettings.instance.drawSphere) {
                float villageDoors = KaboModSettings.instance.sphereDensity;
                int i$ = 24 + (int) (villageDoors * 72.0F);
                double[] dObj = new double[i$ * (i$ / 2 + 1)];
                double[] doorInfo = new double[i$ * (i$ / 2 + 1)];
                double[] ys = new double[i$ * (i$ / 2 + 1)];

                for (double t = 0.0D; t < 6.283185307179586D; t += 6.283185307179586 / (double) i$) {
                    for (double theta = 0.0D; theta < Math.PI; theta += Math.PI / (double) (i$ / 2)) {
                        double dx = (double) vRad * Math.sin(t) * Math.cos(theta);
                        double dz = (double) vRad * Math.sin(t) * Math.sin(theta);
                        double dy = (double) vRad * Math.cos(t);
                        int index = (int) (t / (6.283185307179586D / (double) i$) + (double) i$ * theta / (3.141592653589793D / (double) (i$ / 2)));
                        dObj[index] = (double) vCen.getX() + dx;
                        doorInfo[index] = (double) vCen.getZ() + dz;
                        ys[index] = (double) vCen.getY() + dy;
                    }
                }


                // tess.getWorldRenderer().startDrawing(GL11.GL_POINTS);
                vb.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);

                for (int var34 = 0; var34 < i$ * (i$ / 2 + 1); ++var34) {
                    vb.pos(dObj[var34], ys[var34], doorInfo[var34]).endVertex();
                }

                tess.draw();
            }

            // tess.getWorldRenderer().startDrawing(GL11.GL_LINES);
            vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

            for (VillageDoorInfo vdi : vDoors) {
                BlockPos var33 = vdi.getDoorBlockPos();
                vb.pos((double) var33.getX(), (double) var33.getY(), (double) var33.getZ()).endVertex();
                vb.pos((double) vCen.getX(),  (double) vCen.getY(),  (double) vCen.getZ() ).endVertex();
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
            // tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
            vb.setTranslation(0.0D, 0.0D, 0.0D);

        }

    }

    @SuppressWarnings("unchecked")
    private static void renderSpheresAndLinesMP(Tessellator tess, Entity renderEntity, float partialTicks) {

        int c = 0;
        VertexBuffer vb = tess.getBuffer();

        for (KaboVillageMarkerClient.KaboVillageMarkerVillage village : KaboVillageMarkerClient.instance.getVillageListForDimension(mc.thePlayer.dimension)) {
            int vRad = village.radius;
            BlockPos vCen = village.getCenter();
            List vDoors = village.villageDoors;

            if (LiteModKaboVillageMarker.fullbright) {
                RenderHelper.disableStandardItemLighting();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            }

            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);

            glPushMatrix();
            vb.setTranslation(-getPlayerXGuess(renderEntity, partialTicks),
                              -getPlayerYGuess(renderEntity, partialTicks),
                              -getPlayerZGuess(renderEntity, partialTicks));

            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
            setColor(c % 6);
            ++c;
            GL11.glPointSize((float) (KaboModSettings.instance.dotSize + 1));
            GlStateManager.glLineWidth(2.0F);
            GL11.glEnable(GL11.GL_POINT_SMOOTH);

            if (KaboModSettings.instance.drawSphere) {
                float i$ = KaboModSettings.instance.sphereDensity;
                int door = 24 + (int) (i$ * 72.0F);
                double[] xs = new double[door * (door / 2 + 1)];
                double[] zs = new double[door * (door / 2 + 1)];
                double[] ys = new double[door * (door / 2 + 1)];

                for (double t = 0.0D; t < 6.283185307179586D; t += 6.283185307179586D / (double) door) {
                    for (double theta = 0.0D; theta < 3.141592653589793D; theta += 3.141592653589793D / (double) (door / 2)) {
                        double dx = (double) vRad * Math.sin(t) * Math.cos(theta);
                        double dz = (double) vRad * Math.sin(t) * Math.sin(theta);
                        double dy = (double) vRad * Math.cos(t);
                        int index = (int) (t / (6.283185307179586D / (double) door) + (double) door * theta / (3.141592653589793D / (double) (door / 2)));
                        xs[index] = (double) vCen.getX() + dx;
                        zs[index] = (double) vCen.getZ() + dz;
                        ys[index] = (double) vCen.getY() + dy;
                    }
                }

                // tess.getWorldRenderer().startDrawing(GL11.GL_POINTS);
                vb.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);

                for (int var32 = 0; var32 < door * (door / 2 + 1); ++var32) {
                    vb.pos(xs[var32], ys[var32], zs[var32]).endVertex();
                }

                tess.draw();
            }

            // tess.getWorldRenderer().startDrawing(GL11.GL_LINES);
            vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
            Iterator var31 = vDoors.iterator();

            while (var31.hasNext()) {
                KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition var30 = (KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition) var31.next();
                vb.pos((double) var30.x,     (double) var30.y,     (double) var30.z    ).endVertex();
                vb.pos((double) vCen.getX(), (double) vCen.getY(), (double) vCen.getZ()).endVertex();
            }

            tess.draw();
            if (KaboModSettings.instance.drawGolemArea) {
                renderGolemBox(tess, vCen, c);
            }

            glPopMatrix();

            if (LiteModKaboVillageMarker.fullbright) {
                RenderHelper.enableStandardItemLighting();
            }

            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
            // tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
            vb.setTranslation(0.0D, 0.0D, 0.0D);
        }

    }

    private static void renderGolemBox(Tessellator tess, BlockPos vCen, int c) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.disableCull();
        // tess.getWorldRenderer().startDrawing(GL11.GL_QUADS);
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        GlStateManager.glLineWidth(2.0F);
        setWallColor((c - 1) % 6);
        GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE);
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        tess.draw();
        setColor((c - 1) % 6);
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_CONSTANT_COLOR);
        // tess.getWorldRenderer().startDrawing(GL11.GL_QUADS);
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() + 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() + 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() + 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        vb.pos((double) vCen.getX() - 7.999D, (double) vCen.getY() - 2.999D, (double) vCen.getZ() - 7.999D).endVertex();
        tess.draw();
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        GL11.glLineStipple(5, (short) 0xFFFF8888);
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        for (int xi = -8; xi <= 8; ++xi) {
            int yi;
            for (yi = -3; yi <= 3; ++yi) {
                if (xi == -8 || xi == 8 || yi == -3 || yi == 3) {
                    vb.pos((double) (vCen.getX() + xi), (double) (vCen.getY() + yi), (double) (vCen.getZ() - 8)).endVertex();
                    vb.pos((double) (vCen.getX() + xi), (double) (vCen.getY() + yi), (double) (vCen.getZ() + 8)).endVertex();
                    vb.pos((double) (vCen.getX() - 8),  (double) (vCen.getY() + yi), (double) (vCen.getZ() + xi)).endVertex();
                    vb.pos((double) (vCen.getX() + 8),  (double) (vCen.getY() + yi), (double) (vCen.getZ() + xi)).endVertex();
                }
            }

            for (yi = -8; yi <= 8; ++yi) {
                if (xi == -8 || xi == 8 || yi == -8 || yi == 8) {
                    vb.pos((double) (vCen.getX() + xi), (double) (vCen.getY() + 3), (double) (vCen.getZ() + yi)).endVertex();
                    vb.pos((double) (vCen.getX() + xi), (double) (vCen.getY() - 3), (double) (vCen.getZ() + yi)).endVertex();
                }
            }
        }

        tess.draw();
        // tess.getWorldRenderer().setTranslation(0.0D, 0.0D, 0.0D);
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
