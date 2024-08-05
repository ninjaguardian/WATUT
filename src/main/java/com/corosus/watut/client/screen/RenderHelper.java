package com.corosus.watut.client.screen;

import com.corosus.watut.PlayerStatus;
import com.corosus.watut.WatutMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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
            try {
                lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT, GuiGraphics.class.getDeclaredMethod("m_280444_", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class));
                lookupRenderCallsToMethod.put(RenderCallType.INNER_BLIT2, GuiGraphics.class.getDeclaredMethod("m_280479_", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class));
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public static void renderWithTooltipEnd(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        /*for (PlayerStatus playerStatus : WatutMod.getPlayerStatusManagerClient().lookupPlayerToStatus.values()) {
            ScreenData screenData = playerStatus.getScreenData();
            if (screenData.needsNewRender()) {
                screenData.markNeedsNewRender(false);
                screenData.checkSetup();
                screenData.bind();

                RenderSystem.clear(16640, Minecraft.ON_OSX);

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
                    float effectiveSize = effectiveSizeX + effectiveSizeY;
                    if (effectiveSize > biggest) {
                        biggestUVRenderCallIndex = i;
                        biggest = effectiveSize;
                    }
                }

                //System.out.println("rendering biggest: " + biggest);
                if (biggestUVRenderCallIndex != -1) {
                //for (RenderCall renderCall : screenData.getListRenderCalls()) {
                    RenderCall renderCall = screenData.getListRenderCalls().get(biggestUVRenderCallIndex);
                    List<Object> listParams = renderCall.getListParams();
                    //System.out.println("params size before render: " + listParams);
                    try {
                        lookupRenderCallsToMethod.get(renderCall.getRenderCallType()).invoke(pGuiGraphics, listParams.toArray());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
                screenData.unbind();
                //System.out.println("completed new render for " + playerStatus);
            }
        }*/
    }

}
