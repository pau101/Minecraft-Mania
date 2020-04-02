package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

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
        this.rows = 3;
        this.columns = (width * this.rows + height - 1) / height;
        Vec2i cur = new Vec2i(this.columns / 2, this.rows / 2);
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.columns; x++) {
                final Vec2i p = new Vec2i(x, y);
                this.set(p, NativeImage.getCombined(cur.equals(p) ? 0 : 255, 0, y, x));
            }
        }
//        this.cur = cur;
//        this.prev = null;
//        this.rem = this.columns * this.rows * 2;
        final Random rng = new Random();
        Vec2i prev = null;
        for (int rem = this.columns * this.rows * 2; rem --> 0; ) {
            final Vec2i from = cur;
            do {
                cur = from.add(Vec2i.CARDINAL[rng.nextInt(Vec2i.CARDINAL.length)]);
            } while (cur.equals(prev) || !this.contains(cur));
            new Slide(from).to(cur).move();
            prev = cur;
        }
        this.texture.updateDynamicTexture();
    }

    /*final Random rng = new Random();
    Vec2i cur, prev;
    int rem;*/

    @Override
    public void tick() {
        super.tick();
        /*if (this.rem >= 0) {
            final Vec2i from = this.cur;
            do {
                this.cur = from.add(Vec2i.CARDINAL[this.rng.nextInt(Vec2i.CARDINAL.length)]);
            } while (this.cur.equals(this.prev) || !this.contains(this.cur));
            new Slide(from).to(this.cur).move();
            this.prev = this.cur;
            this.rem--;
            this.texture.updateDynamicTexture();
        }*/
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        if (this.parent != null) {
            this.parent.render(mouseX, mouseY, delta);
        }
        this.post(delta);
    }

    private Vec2i cellAt() {
        final Minecraft mc = this.minecraft;
        final int w = mc.getMainWindow().getWidth();
        final int h = mc.getMainWindow().getHeight();
        final double x = mc.mouseHelper.getMouseX();
        final double y = h - 1 - mc.mouseHelper.getMouseY();
        final int s = (h + this.rows - 1) / this.rows;
        final int ox = (w - this.columns * s) / 2;
        final int oy = (h - this.rows * s) / 2;
        final int cx = (int) ((x - ox) / s);
        final int cy = (int) ((y - oy) / s);
        return new Vec2i(cx, cy);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            final Vec2i cp = this.cellAt();
            final Slide slide = this.getMove(cp);
            if (slide != null) {
                this.move(slide);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void move(final Slide slide) {
        this.minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_WOOD_PLACE, 1.0F));
        slide.move();
        if (slide.from.equals(this.lastHover)) {
            this.lastHover = null;
        }
        this.updateHover();
        this.texture.updateDynamicTexture();
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        final Vec2i dir;
        if (key == GLFW.GLFW_KEY_W || key == GLFW.GLFW_KEY_UP) {
            dir = Vec2i.DOWN;
        } else if (key == GLFW.GLFW_KEY_S || key == GLFW.GLFW_KEY_DOWN) {
            dir = Vec2i.UP;
        } else if (key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_LEFT) {
            dir = Vec2i.RIGHT;
        } else if (key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_RIGHT) {
            dir = Vec2i.LEFT;
        } else {
            dir = null;
        }
        if (dir != null) {
            for (int y = 0; y < this.rows; y++) {
                for (int x = 0; x < this.columns; x++) {
                    final Vec2i p = new Vec2i(x, y);
                    final Slide s = new Slide(p);
                    if (NativeImage.getAlpha(s.fromState) == 0) {
                        final Vec2i n = p.add(dir);
                        if (this.contains(n)) {
                            this.move(s.to(p.add(dir)));
                            return true;
                        }
                    }
                }
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Nullable
    private Slide getMove(final Vec2i cp) {
        if (this.contains(cp)) {
            final Slide slide = new Slide(cp);
            if (NativeImage.getAlpha(slide.fromState) != 0) {
                for (final Vec2i dir : Vec2i.CARDINAL) {
                    final Vec2i np = cp.add(dir);
                    if (this.contains(np)) {
                        slide.to(np);
                        if (NativeImage.getAlpha(slide.toState) == 0) {
                            return slide;
                        }
                    }
                }
            }
        }
        return null;
    }

    class Slide {
        final Vec2i from;
        final int fromState;
        Vec2i to;
        int toState;

        Slide(final Vec2i from) {
            this.from = from;
            this.fromState = SlidingPuzzleScreen.this.get(from);
        }

        Slide to(final Vec2i to) {
            this.to = to;
            this.toState = SlidingPuzzleScreen.this.get(to);
            return this;
        }

        void move() {
            SlidingPuzzleScreen.this.set(this.from, this.toState & 0xFF00FFFF);
            SlidingPuzzleScreen.this.set(this.to, this.fromState & 0xFF00FFFF);
        }
    }

    @Nullable
    Vec2i lastHover = null;

    private int get(final Vec2i pos) {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            return image.getPixelRGBA(pos.x, pos.y);
        }
        return 0;
    }

    private void set(final Vec2i pos, final int value) {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            image.setPixelRGBA(pos.x, pos.y, value);
        }
    }

    private void setHighlight(final Vec2i pos, final int value) {
        this.set(pos, this.get(pos) & 0xFF00FFFF | value << 16);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        this.updateHover();
        this.texture.updateDynamicTexture();
    }

    private void updateHover() {
        final Vec2i lh = this.lastHover;
        final Vec2i h = this.cellAt();
        if (!h.equals(lh)) {
            if (lh != null) {
                this.setHighlight(lh, 0);
            }
            if (this.getMove(h) != null) {
                this.setHighlight(h, 255);
            }
            this.lastHover = h;
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
        static final Vec2i UP = new Vec2i(0, 1);
        static final Vec2i DOWN = new Vec2i(0, -1);
        static final Vec2i LEFT = new Vec2i(-1, 0);
        static final Vec2i RIGHT = new Vec2i(1, 0);
        static final Vec2i[] CARDINAL = { RIGHT, UP, LEFT, DOWN };

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

        Vec2i add(final Vec2i other) {
            return new Vec2i(this.x + other.x, this.y + other.y);
        }
    }
}
