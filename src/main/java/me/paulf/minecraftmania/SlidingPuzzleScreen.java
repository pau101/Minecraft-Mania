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
import java.util.Random;

public class SlidingPuzzleScreen extends Screen {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/sliding_puzzle.json");

    private final DynamicTexture texture = new DynamicTexture(256, 256, false);

    private final PostProcessingEffect effect;

    @Nullable
    private final Screen parent;

    private int columns;

    private int rows;

    private Board board;

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
        this.board = new Board(this.columns, this.rows);
        final Random r = new Random();
        for (int n = this.rows * this.rows * this.columns * this.columns; n > 0; ) {
            switch (r.nextInt(4)) {
                case 0:
                    if (this.board.up()) n--;
                    break;
                case 1:
                    if (this.board.down()) n--;
                    break;
                case 2:
                    if (this.board.left()) n--;
                    break;
                case 3:
                    if (this.board.right()) n--;
                    break;
                default:
                    n = 0;
                    break;
            }
        }
        this.hover = -1;
        this.updateHover(
            this.minecraft.mouseHelper.getMouseX() * this.minecraft.getMainWindow().getScaledWidth() / this.minecraft.getMainWindow().getWidth(),
            this.minecraft.mouseHelper.getMouseY() * this.minecraft.getMainWindow().getScaledHeight() / this.minecraft.getMainWindow().getHeight()
        );
        this.upload();
/*        Vec2i cur = new Vec2i(this.columns / 2, this.rows / 2);
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
        this.texture.updateDynamicTexture();*/
    }

    static class Board {
        final int columns;
        final int rows;
        final int[] state;
        int blank;

        Board(final int columns, final int rows) {
            this.columns = columns;
            this.rows = rows;
            this.state = new int[columns * rows];
            for (int i = 0; i < columns * rows; i++) {
                this.state[i] = i;
            }
            this.blank = 0;
        }

        int get(final int index) {
            return this.state[index];
        }

        int x(final int index) {
            return index < 0 ? -1 : index % this.columns;
        }

        int y(final int index) {
            return index < 0 ? -1 : index / this.columns;
        }

        int index(final int x, final int y) {
            return x + y * this.columns;
        }

        int swap(final int first, final int second) {
            final int[] s = this.state;
            final int temp = s[first];
            s[first] = s[second];
            s[second] = temp;
            return first;
        }

        boolean up() {
            if (this.y(this.blank) > 0) {
                this.blank = this.swap(this.blank - this.columns, this.blank);
                return true;
            }
            return false;
        }

        boolean down() {
            if (this.y(this.blank) < this.rows - 1) {
                this.blank = this.swap(this.blank + this.columns, this.blank);
                return true;
            }
            return false;
        }

        boolean left() {
            if (this.x(this.blank) < this.columns - 1) {
                this.blank = this.swap(this.blank + 1, this.blank);
                return true;
            }
            return false;
        }

        boolean right() {
            if (this.x(this.blank) > 0) {
                this.blank = this.swap(this.blank - 1, this.blank);
                return true;
            }
            return false;
        }

        boolean moveable(final int index) {
            final int x = this.x(index);
            final int y = this.y(index);
            return this.contains(x, y) && Math.abs(x - this.x(this.blank)) + Math.abs(y - this.y(this.blank)) == 1;
        }

        boolean tap(final int index) {
            if (this.moveable(index)) {
                this.blank = this.swap(index, this.blank);
                return true;
            }
            return false;
        }

        boolean contains(final int x, final int y) {
            return x >= 0 && y >= 0 && x < this.columns && y < this.rows;
        }
    }

    int hover = -1;

    private void upload() {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            for (int y = 0; y < this.board.rows; y++) {
                for (int x = 0; x < this.board.columns; x++) {
                    final int i = this.board.index(x, y);
                    final int pos = this.board.get(i);
                    image.setPixelRGBA(x, y, NativeImage.getCombined(i == this.board.blank ? 0 : 255, i == this.hover ? 255 : 0, this.board.y(pos), this.board.x(pos)));
                }
            }
            this.texture.updateDynamicTexture();
        }
    }

    /*final Random rng = new Random();
    Vec2i cur, prev;
    int rem;*/

    @Override
    public void tick() {
        if (this.parent != null) {
            this.parent.tick();
        }
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

    private int cell(final double x, final double y) {
        final int w = this.width;
        final int h = this.height;
        final int s = (h + this.rows - 1) / this.rows;
        final int ox = (w - this.columns * s) / 2;
        final int oy = (h - this.rows * s) / 2;
        final int cx = (int) ((x - ox) / s);
        final int cy = (int) ((h - 1 - y - oy) / s);
        return this.board.index(cx, cy);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            if (this.board.tap(this.cell(mouseX, mouseY))) {
                this.onMove();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onMove() {
        this.minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_WOOD_PLACE, 1.0F));
        this.upload();
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (key == GLFW.GLFW_KEY_W || key == GLFW.GLFW_KEY_UP) {
            if (this.board.up()) {
                this.onMove();
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_S || key == GLFW.GLFW_KEY_DOWN) {
            if (this.board.down()) {
                this.onMove();
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_LEFT) {
            if (this.board.left()) {
                this.onMove();
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_RIGHT) {
            if (this.board.right()) {
                this.onMove();
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        if (this.updateHover(mouseX, mouseY)) {
            this.upload();
        }
    }

    private boolean updateHover(final double x, final double y) {
        final int h = this.cell(x, y);
        if (h != this.hover) {
            this.hover = this.board.moveable(h) ? h : -1;
            return true;
        }
        return false;
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
}
