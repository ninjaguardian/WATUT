package com.corosus.watut.client.screen;

import com.corosus.watut.PlayerStatus;
import com.corosus.watut.WatutMod;
import com.corosus.watut.client.ScreenCapturing;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class RenderHelper {

    public static HashMap<RenderCallType, Method> lookupRenderCallsToMethod = new HashMap<>();

    static {
        try {
            lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT, GuiGraphics.class.getDeclaredMethod("innerBlit", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class));
            lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT2, GuiGraphics.class.getDeclaredMethod("innerBlit", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class));
        } catch (NoSuchMethodException e) {
            System.out.println("fallback to obfuscated lookup");
            try {
                lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT, GuiGraphics.class.getDeclaredMethod("m_280444_", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class));
                lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT2, GuiGraphics.class.getDeclaredMethod("m_280479_", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class));
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public static void renderWithTooltipEnd(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //PlayerStatus playerStatusLocal = WatutMod.getPlayerStatusManagerClient().getStatusLocal();

        for (PlayerStatus playerStatus : WatutMod.getPlayerStatusManagerClient().lookupPlayerToStatus.values()) {
            ScreenData screenData = playerStatus.getScreenData();
            if (screenData.needsNewRender()) {
                screenData.markNeedsNewRender(false);

                //ScreenCapturing.checkSetup();
                //ScreenCapturing.bind();
                screenData.checkSetup();
                screenData.bind();

                RenderSystem.clear(16640, Minecraft.ON_OSX);
                //screenData.getMainRenderTarget().clear(Minecraft.ON_OSX);
                /*screenData.getMainRenderTarget().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                screenData.getMainRenderTarget().clear(Minecraft.ON_OSX);*/
                //GL30.glClearBufferfi();

                int biggestUVRenderCallIndex = -1;
                float biggest = 0;
                for (int i = 0; i < screenData.getListRenderCalls().size(); i++) {
                    RenderCall renderCall = screenData.getListRenderCalls().get(i);
                    float x1 = (int) renderCall.getListParams().get(1);
                    float x2 = (int) renderCall.getListParams().get(2);
                    float y1 = (int) renderCall.getListParams().get(3);
                    float y2 = (int) renderCall.getListParams().get(4);
                    float minU = (float) renderCall.getListParams().get(6);
                    float maxU = (float) renderCall.getListParams().get(7);
                    float minV = (float) renderCall.getListParams().get(8);
                    float maxV = (float) renderCall.getListParams().get(9);
                    float effectiveSizeX = (x2 - x1) * (maxU - minU);
                    float effectiveSizeY = (y2 - y1) * (maxV - minV);
                    //not a perfect solution, id like to have size of texture that the UV fraction is applied to, but thats a pain to get
                    //i know most screen guis are mostly just the main gui for bound texture and its often the biggest part
                    //float sizeUV = (maxU - minU) + (maxV - minV);
                    float effectiveSize = effectiveSizeX + effectiveSizeY;
                    if (effectiveSize > biggest) {
                        biggestUVRenderCallIndex = i;
                        biggest = effectiveSize;
                    }
                }

                System.out.println("rendering biggest: " + biggest);
                int i = 0;
                if (biggestUVRenderCallIndex != -1) {
                //for (RenderCall renderCall : screenData.getListRenderCalls()) {
                    RenderCall renderCall = screenData.getListRenderCalls().get(biggestUVRenderCallIndex);
                    List<Object> listParams = renderCall.getListParams();
                    System.out.println("params size before render: " + listParams);
                    try {
                        //if (i > 90) {
                        lookupRenderCallsToMethod.get(renderCall.getRenderCallType()).invoke(pGuiGraphics, listParams.toArray());
                        //}

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    }
                    /*if (renderCall.getRenderCallType() == RenderCallType.INNER_BLIT) {
                        pGuiGraphics.innerBlit((ResourceLocation)listParams.get(0), (int)listParams.get(1), (int)listParams.get(2), (int)listParams.get(3), (int)listParams.get(4), (int)listParams.get(5), (float)listParams.get(6), (float)listParams.get(7), (float)listParams.get(8), (float)listParams.get(9));
                    } else if (renderCall.getRenderCallType() == RenderCallType.INNER_BLIT2) {
                        pGuiGraphics.innerBlit((ResourceLocation)listParams.get(0), (int)listParams.get(1), (int)listParams.get(2), (int)listParams.get(3), (int)listParams.get(4), (int)listParams.get(5), (float)listParams.get(6), (float)listParams.get(7), (float)listParams.get(8), (float)listParams.get(9), (float)listParams.get(10), (float)listParams.get(11), (float)listParams.get(12), (float)listParams.get(13));
                    }*/
                    i++;
                }

                //ScreenCapturing.getPixels();

                //ScreenCapturing.unbind();
                screenData.unbind();
                System.out.println("completed new render for " + playerStatus);

                //ScreenCapturing.renderFrameBuffer();
            }
        }
    }

}
