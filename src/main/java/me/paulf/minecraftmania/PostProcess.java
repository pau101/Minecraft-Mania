package me.paulf.minecraftmania;

import com.google.common.io.ByteStreams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Util;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

public class PostProcess {
    int width, height;
    NativeImage image;
    IntBuffer pixelsBuf;
    int[] pixels;
    BufferedImage javaImage;
    JPEGImageWriteParam params = Util.make(new JPEGImageWriteParam(null), params -> {
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.1F);
    });
    ByteOutput out = new ByteOutput();
    static class ByteOutput extends ByteArrayOutputStream {
        byte[] buf() {
            return this.buf;
        }
    }
    ImageWriter jpegWriter = Util.make(ImageIO.getImageWritersByFormatName("jpg").next(), w -> w.setOutput(new MemoryCacheImageOutputStream(this.out)));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void render(final TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            final Framebuffer buf = Minecraft.getInstance().getFramebuffer();
            final int w = buf.framebufferTextureWidth, h = buf.framebufferTextureHeight;
            final NativeImage.PixelFormat format = NativeImage.PixelFormat.RGBA;
            if (w != this.width || h != this.height) {
                if (this.image != null) {
                    this.image.close();
                }
                this.image = new NativeImage(format, w, h, false);
                //noinspection ConstantConditions
                final long pointer = ObfuscationReflectionHelper.getPrivateValue(NativeImage.class, this.image, "field_195722_d");
                this.pixelsBuf = MemoryUtil.memIntBuffer(pointer, w * h * format.getPixelSize());
                //this.pixels = new int[w * h];
                this.javaImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                this.pixels = ((DataBufferInt) this.javaImage.getRaster().getDataBuffer()).getData();
            }
            RenderSystem.bindTexture(buf.framebufferTexture);
            this.image.downloadFromTexture(0, false);
            final IntBuffer b = this.pixelsBuf;
            final int[] p = this.pixels;
            b.get(p);
            b.rewind();
            /*final long time = Util.milliTime();
            for (int i = 0; i < w * h; i++) {
                final int x = i % w;
                final int y = i / w;
                final int py = y + (int) ((x + time + y / 3 * w) / 100 % 5);
                p[i] = p[x + MathHelper.clamp(py, 0, h - 1) * w];
            }*/
            //b.put(p);

            for (int i = 0; i < w * h; i++) {
                p[i] = p[i] & 0xFF00FF00 | (p[i] & 0xFF) << 16 | p[i] >> 16 & 0xFF;
            }
            this.out.reset();
            try {
                this.jpegWriter.write(null, new IIOImage(this.javaImage, null, null), this.params);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            try (final NativeImage ni = NativeImage.read(ByteStreams.limit(new ByteArrayInputStream(this.out.buf()), this.out.size()))) {
                this.image.copyImageData(ni);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            this.image.uploadTextureSub(0, 0, 0, false);
        }
    }
}
