package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public class SlidingPuzzleScreen extends Screen {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/sliding_puzzle.json");

    private final DynamicTexture texture = new DynamicTexture(256, 256, false);

    private final PostProcessingEffect effect;

    @Nullable
    private final Screen parent;

    private int columns;

    private int rows;

    public SlidingPuzzleScreen(final Screen parent) {
        super(NarratorChatListener.EMPTY);
        this.parent = parent;
        Minecraft.getInstance().getTextureManager().loadTexture(new ResourceLocation(MinecraftMania.ID, "textures/effect/puzzle.png"), this.texture);
        this.effect = new PostProcessingEffect(SHADER_LOCATION);
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/sliding_puzzle.fsh"), this.effect::reload);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init(final Minecraft mc, final int width, final int height) {
        super.init(mc, width, height);
        this.rows = 5;
        this.columns = (width * this.rows + height - 1) / height;
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            for (int y = 0; y < this.rows; y++) {
                for (int x = 0; x < this.columns; x++) {
                    image.setPixelRGBA(x, y, NativeImage.getCombined(x == this.columns / 2 && y == this.rows / 2 ? 0 : 255, 0, y, x));
                }
            }
        }
        this.texture.updateDynamicTexture();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        if (this.parent != null) {
            this.parent.render(mouseX, mouseY, delta);
        }
        this.post(delta);
    }

    private Vec2i cellAt(final double mx, final double my) {
        final int x = (int) mx;
        final int y = (int) (this.height - my - 1);
        final int s = this.height / this.rows;
        final int ox = (this.width - this.columns * s) / 2;
        final int oy = (this.height - this.rows * s) / 2;
        final int cx = (x - ox) / s;
        final int cy = (y - oy) / s;
        return new Vec2i(cx, cy);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final Vec2i cp = this.cellAt(mouseX, mouseY);
        final NativeImage image = this.texture.getTextureData();
        if (image != null && this.contains(cp)) {
            final int c = image.getPixelRGBA(cp.x, cp.y);
            if (NativeImage.getAlpha(c) != 0) {
                for (final int[] dir : new int[][] { { 1, 0 }, { 0, -1 }, { -1, 0 }, { 0, 1 } }) {
                    final Vec2i np = cp.add(dir[0], dir[1]);
                    if (this.contains(np)) {
                        final int nc = image.getPixelRGBA(np.x, np.y);
                        if (NativeImage.getAlpha(nc) == 0) {
                            image.setPixelRGBA(cp.x, cp.y, nc);
                            image.setPixelRGBA(np.x, np.y, c);
                            if (cp.equals(this.lastHover)) {
                                this.setHighlight(np, 0);
                                this.lastHover = null;
                            }
                            this.texture.updateDynamicTexture();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    Vec2i lastHover = null;

    private void setHighlight(final Vec2i pos, final int value) {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            final int lhs = image.getPixelRGBA(pos.x, pos.y);
            image.setPixelRGBA(pos.x, pos.y, NativeImage.getCombined(NativeImage.getAlpha(lhs), value, NativeImage.getGreen(lhs), NativeImage.getRed(lhs)));
        }
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            final Vec2i lh = this.lastHover;
            final Vec2i h = this.cellAt(mouseX, mouseY);
            if (!h.equals(lh)) {
                if (lh != null) {
                    this.setHighlight(lh, 0);
                }
                this.setHighlight(h, 255);
                this.lastHover = h;
                this.texture.updateDynamicTexture();
            }
        }
    }

    private boolean contains(final Vec2i p) {
        return p.x >= 0 && p.y >= 0 && p.x < this.columns && p.y < this.rows;
    }

    private void post(final float delta) {
        this.effect.render(delta);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.effect.close();
        this.texture.close();
    }

    static class Vec2i {
        final int x, y;

        Vec2i(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Vec2i)  {
                return this.x == ((Vec2i) o).x && this.y == ((Vec2i) o).y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y);
        }

        Vec2i add(final int x, final int y) {
            return new Vec2i(this.x + x, this.y + y);
        }
    }
}
