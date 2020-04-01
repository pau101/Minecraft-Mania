package me.paulf.minecraftmania;

import com.google.common.base.Throwables;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShaderPostProcessing {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/sliding_puzzle.json");

    private final DynamicTexture texture = new DynamicTexture(9, 5, false);

    private final PostProcessingEffect effect;

    public ShaderPostProcessing() {
        Minecraft.getInstance().getTextureManager().loadTexture(new ResourceLocation(MinecraftMania.ID, "textures/effect/puzzle.png"), this.texture);
        this.effect = new PostProcessingEffect(SHADER_LOCATION);
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/sliding_puzzle.fsh"), this.effect::reload);
    }

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            final MainWindow win = Minecraft.getInstance().getMainWindow();
            final int w = win.getFramebufferWidth();
            final int h = win.getFramebufferHeight();
            final int rows = 5;
            final int cols = (w * rows + h - 1) / h;
            NativeImage img = this.texture.getTextureData();
            if (img != null && (img.getWidth() != cols || img.getHeight() != rows)) {
                img = new NativeImage(cols, rows, true);
                try {
                    this.texture.setTextureData(img);
                } catch (final Exception ex) {
                    Throwables.throwIfUnchecked(ex);
                    throw new AssertionError(ex);
                }
            }
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    img.setPixelRGBA(x, y, NativeImage.getCombined(x == cols / 2 && y == rows / 2 ? 0 : 255, 0, y, x));
                }
            }
            this.texture.updateDynamicTexture();
            this.effect.render(e.renderTickTime);
        }
    }
}
