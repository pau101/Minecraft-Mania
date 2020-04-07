package me.paulf.minecraftmania;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;

import javax.annotation.Nullable;

public abstract class TilePuzzleScreen<B extends Board> extends Screen {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/sliding_puzzle.json");

    private final DynamicTexture texture = new DynamicTexture(256, 256, false);

    private final PostProcessingEffect effect;

    @Nullable
    private final Screen parent;

    protected B board;

    private int hover = -1;

    public TilePuzzleScreen(final Screen parent) {
        super(NarratorChatListener.EMPTY);
        this.parent = parent;
        Minecraft.getInstance().getTextureManager().loadTexture(new ResourceLocation(MinecraftMania.ID, "textures/effect/puzzle.png"), this.texture);
        this.effect = new PostProcessingEffect(SHADER_LOCATION);
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/sliding_puzzle.fsh"), this.effect::reload);
    }

    public final boolean isSolved() {
        return this.board.isSolved();
    }

    protected abstract B createBoard(final int columns, final int rows);

    protected abstract boolean isBlank(final int index);

    protected abstract boolean isSelected(final int index);

    protected boolean isHover(final int index) {
        return index == this.hover;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void init(final Minecraft mc, final int width, final int height) {
        super.init(mc, width, height);
        final int rows = 3;
        final int columns = (width * rows + height - 1) / height;
        this.board = this.createBoard(columns, rows);
        this.hover = -1;
        final MainWindow win = mc.getMainWindow();
        this.updateHover(
            mc.mouseHelper.getMouseX() * win.getScaledWidth() / win.getWidth(),
            mc.mouseHelper.getMouseY() * win.getScaledHeight() / win.getHeight()
        );
        this.upload();
    }

    protected void upload() {
        final NativeImage image = this.texture.getTextureData();
        if (image != null) {
            for (int y = 0; y < this.board.rows; y++) {
                for (int x = 0; x < this.board.columns; x++) {
                    final int i = this.board.index(x, y);
                    final int pos = this.board.get(i);
                    image.setPixelRGBA(x, y, NativeImage.getCombined(this.isBlank(i) ? 0 : 255, this.isSelected(i) ? 255 : this.isHover(i) ? 63 : 0, this.board.y(pos), this.board.x(pos)));
                }
            }
            this.texture.updateDynamicTexture();
        }
    }

    @Override
    public void tick() {
        if (this.parent != null) {
            this.parent.tick();
        }
        super.tick();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        if (this.parent != null) {
            this.parent.render(mouseX, mouseY, delta);
        }
        this.post(delta);
        super.render(mouseX, mouseY, delta);
    }

    protected int cell(final double x, final double y) {
        final int w = this.width;
        final int h = this.height;
        final int s = (h + this.board.rows - 1) / this.board.rows;
        final int ox = (w - this.board.columns * s) / 2;
        final int oy = (h - this.board.rows * s) / 2;
        final int cx = (int) ((x - ox) / s);
        final int cy = (int) ((h - 1 - y - oy) / s);
        return this.board.index(cx, cy);
    }

    protected void onMove() {
        this.play(SimpleSound.master(SoundEvents.BLOCK_WOOD_PLACE, 1.0F));
        if (this.board.isSolved()) {
            this.play(SimpleSound.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
            if (this.minecraft != null) {
                this.minecraft.displayGuiScreen(this.parent);
            }
        } else {
            this.upload();
        }
    }

    protected void play(final ISound sound) {
        if (this.minecraft != null) {
            this.minecraft.getSoundHandler().play(sound);
        }
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
            this.hover = h;
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
