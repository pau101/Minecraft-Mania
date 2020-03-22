package me.paulf.minecraftmania.function;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Random;

public class CorruptFunction implements CommandFunction {
    private final Duration duration;

    public CorruptFunction(final Duration duration) {
        this.duration = duration;
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.addRunningFunction(this.duration, new RunningFunction() {
            private IntSet effected = new IntOpenHashSet();

            @Override
            public void tick() {
                final TextureManager manager = Minecraft.getInstance().getTextureManager();
                final Map<ResourceLocation, Texture> textures = ObfuscationReflectionHelper.getPrivateValue(TextureManager.class, manager, "field_110585_a");
                if (textures == null) {
                    return;
                }
                final Random r = new Random();
                for (final Texture tex : textures.values()) {
                    final int id = tex.getGlTextureId();
                    if (this.effected.contains(id)) {
                        continue;
                    }
                    if (r.nextFloat() < 0.9F) {
                        continue;
                    }
                    GlStateManager.bindTexture(id);
                    final int w = GlStateManager.getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                    final int h = GlStateManager.getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                    if (w <= 0 || h <= 0) {
                        continue;
                    }
                    this.effected.add(id);

                    final JPEGImageWriteParam params = new JPEGImageWriteParam(null);
                    params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    params.setCompressionQuality(0.3F);

                    try (final NativeImage image = new NativeImage(w, h, false)) {
                        image.downloadFromTexture(0, false);

                        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < h; y++) {
                            for (int x = 0; x < w; x++) {
                                final int rgb = image.getPixelRGBA(x, y);
                                img.setRGB(x, y, 0xFF000000 | NativeImage.getRed(rgb) << 16 | NativeImage.getGreen(rgb) << 8 | NativeImage.getBlue(rgb));
                            }
                        }
                        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        writer.setOutput(new MemoryCacheImageOutputStream(out));
                        try {
                            writer.write(null, new IIOImage(img, null, null), params);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        try (final NativeImage j = NativeImage.read(new ByteArrayInputStream(out.toByteArray()))) {
                            for (int y = 0; y < h; y++) {
                                for (int x = 0; x < w; x++) {
                                    image.setPixelRGBA(x, y, j.getPixelRGBA(x, y) & 0xFFFFFF | image.getPixelRGBA(x, y) & 0xFF000000);
                                }
                            }
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }

                        image.uploadTextureSub(0, 0, 0, false);
                    }
                }
            }
        });
    }
}
