package me.paulf.minecraftmania;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class PostProcessingEffect {
    private final ResourceLocation location;

    private State state;

    public PostProcessingEffect(final ResourceLocation location) {
        this.location = location;
        this.state = new UnloadedState();
        this.reload();
    }

    public void reload() {
        this.state = this.state.reload(this.location);
    }

    public void render(final float delta) {
        this.state = this.state.render(delta);
    }

    abstract static class State {
        State reload(final ResourceLocation location) {
            final Minecraft mc = Minecraft.getInstance();
            final ShaderGroup shader;
            try {
                shader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
            } catch (final IOException e) {
                return new ErrorState(e);
            }
            return new LoadedState(shader);
        }

        State render(final float delta) {
            return this;
        }
    }

    static class UnloadedState extends State {
    }

    static class ErrorState extends State {
        final Exception exception;

        ErrorState(final Exception exception) {
            this.exception = exception;
        }

        @Override
        State render(final float delta) {
            final int PAD = 8;
            final FontRenderer font = Minecraft.getInstance().fontRenderer;
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 100.0F);
            final MainWindow win = Minecraft.getInstance().getMainWindow();
            Screen.fill(0, 0, win.getScaledWidth(), win.getScaledHeight(), 0xC0101010);
            font.drawSplitString(this.exception.getMessage(), PAD, PAD, win.getScaledWidth() - 2 * PAD, 0xFFFFFF);
            RenderSystem.popMatrix();
            return this;
        }
    }

    static class LoadedState extends State {
        final ShaderGroup shader;
        int w, h;

        LoadedState(final ShaderGroup shader) {
            this.shader = shader;
            this.w = 0;
            this.h = 0;
        }

        @Override
        State render(final float delta) {
            final MainWindow win = Minecraft.getInstance().getMainWindow();
            final int w = win.getFramebufferWidth();
            final int h = win.getFramebufferHeight();
            if (w != this.w || h != this.h) {
                this.shader.createBindFramebuffers(w, h);
                this.w = w;
                this.h = h;
            }
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
            return this;
        }

        @Override
        State reload(final ResourceLocation location) {
            this.shader.close();
            return super.reload(location);
        }
    }
}
