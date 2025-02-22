package com.corosus.watut;

import com.corosus.watut.spritesets.SpriteSetPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SpriteInfo {

    private final String name;
    private TextureAtlasSprite sprite;
    private SpriteSetPlayer spriteSetPlayer;

    public SpriteInfo(String name, int size, int tickDelay) {
        this.name = name;
        if (size > 0) this.spriteSetPlayer = new SpriteSetPlayer(tickDelay, size);
    }

    public boolean isSpriteSet() {
        return spriteSetPlayer != null;
    }

    public ResourceLocation getResLocationName() {
        return getResLocationName(0);
    }

    public ResourceLocation getResLocationName(int index) {
        if (isSpriteSet()) {
            return ResourceLocation.parse(WatutMod.MODID + ":particles/" + name + index);
        } else {
            return ResourceLocation.parse(WatutMod.MODID + ":particles/" + name);
        }
    }

    public void setupSprites(TextureAtlas textureAtlas) {
        if (isSpriteSet()) {
            List<TextureAtlasSprite> list = new ArrayList<>();
            for (int i = 0; i < spriteSetPlayer.getFrames(); i++) {
                TextureAtlasSprite textureAtlasSprite = textureAtlas.getSprite(getResLocationName(i));
                if (textureAtlasSprite != null) {
                    list.add(textureAtlasSprite);
                } else {
                    System.out.println("failed to find " + getResLocationName(i));
                }
            }
            this.spriteSetPlayer.setList(list);
            sprite = list.get(0);
        } else {
            sprite = textureAtlas.getSprite(getResLocationName());
            if (sprite == null) {
                System.out.println("failed to find " + getResLocationName());
            }
        }
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public String getName() {
        return name;
    }

    public SpriteSetPlayer getSpriteSet() {
        return spriteSetPlayer;
    }

    public void setSpriteSetPlayer(SpriteSetPlayer spriteSetPlayer) {
        this.spriteSetPlayer = spriteSetPlayer;
    }
}
