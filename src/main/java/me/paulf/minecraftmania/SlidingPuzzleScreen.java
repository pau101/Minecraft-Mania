package me.paulf.minecraftmania;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class SlidingPuzzleScreen extends TilePuzzleScreen<SlidingBoard> {
    public SlidingPuzzleScreen(final Screen parent) {
        super(parent);
    }

    @Override
    protected SlidingBoard createBoard(final int columns, final int rows) {
        final SlidingBoard board = new SlidingBoard(columns, rows);
        final Random r = new Random();
        for (int n = rows * rows * columns * columns; n > 0; ) {
            switch (r.nextInt(4)) {
                case 0:
                    if (board.up()) n--;
                    break;
                case 1:
                    if (board.down()) n--;
                    break;
                case 2:
                    if (board.left()) n--;
                    break;
                case 3:
                    if (board.right()) n--;
                    break;
                default:
                    n = 0;
                    break;
            }
        }
        return board;
    }

    @Override
    protected boolean isBlank(final int index) {
        return this.board.isBlank(index);
    }

    @Override
    protected boolean isSelected(final int index) {
        return false;
    }

    @Override
    protected boolean isHover(final int index) {
        return super.isHover(index) && this.board.movable(index);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            if (this.board.tap(this.cell(mouseX, mouseY))) {
                this.onMove();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
}
