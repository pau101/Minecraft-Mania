package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;

import java.util.Random;

public class JigsawPuzzleScreen extends TilePuzzleScreen<Board> {
    private int selected;

    public JigsawPuzzleScreen(final Screen parent) {
        super(parent);
    }

    @Override
    public void init(final Minecraft mc, final int width, final int height) {
        this.selected = -1;
        super.init(mc, width, height);
    }

    @Override
    protected Board createBoard(final int columns, final int rows) {
        final Board board = new Board(columns, rows);
        final Random rng = new Random();
        for (int i = columns * rows; i > 1; i--) {
            board.swap(i - 1, rng.nextInt(i));
        }
        return board;
    }

    @Override
    protected boolean isBlank(final int index) {
        return false;
    }

    @Override
    protected boolean isSelected(final int index) {
        return index == this.selected;
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int button) {
        final int c = this.cell(x, y);
        if (this.selected == -1) {
            this.selected = c;
            this.play(SimpleSound.master(SoundEvents.BLOCK_WOOD_HIT, 0.9F));
            this.upload();
            return true;
        } else if (this.selected == c) {
            this.selected = -1;
            this.play(SimpleSound.master(SoundEvents.BLOCK_WOOD_HIT, 0.7F));
            this.upload();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(final double x, final double y, final int button) {
        if (this.selected != -1) {
            final int c = this.cell(x, y);
            if (this.selected != c) {
                this.board.swap(this.selected, c);
                this.selected = -1;
                this.onMove();
                return true;
            }
        }
        return super.mouseReleased(x, y, button);
    }
}
