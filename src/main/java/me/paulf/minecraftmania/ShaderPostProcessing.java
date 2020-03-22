package me.paulf.minecraftmania;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;

public final class ShaderPostProcessing {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json");

    @Nullable
    private ShaderGroup shader;
    private int w, h;

    public ShaderPostProcessing() {
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/jpeg.fsh"), this::load);
    }

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            if (this.shader == null) {
                this.load();
            }
            this.render(e.renderTickTime);
        }
    }

    private void load() {
        this.load(SHADER_LOCATION);
    }

    private void load(final ResourceLocation location) {
        if (this.shader != null) {
            this.shader.close();
            this.shader = null;
            this.w = 0;
            this.h = 0;
        }
        final Minecraft mc = Minecraft.getInstance();
        try {
            this.shader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
        } catch (final IOException e) {
            LOGGER.warn("Failed to load shader: {}", location, e);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Failed to load shader: {}", location, e);
        }
        this.resize();
    }

    private void resize() {
        if (this.shader != null) {
            final MainWindow win = Minecraft.getInstance().getMainWindow();
            final int w = win.getFramebufferWidth();
            final int h = win.getFramebufferHeight();
            if (w != this.w || h != this.h) {
                this.shader.createBindFramebuffers(w, h);
                this.w = w;
                this.h = h;
            }
        }
    }

    private void render(final float delta) {
        if (this.shader == null) {
            return;
        }
        this.resize();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        this.shader.render(delta);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
    }
}
