package me.paulf.minecraftmania;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class SlidingPuzzleScreen extends TilePuzzleScreen<SlidingPuzzleScreen.SlidingBoard> {
    public SlidingPuzzleScreen(final Screen parent) {
        super(parent);
    }

    @Override
    protected SlidingBoard createBoard(final int width, final int height) {
        final int rows = 3;
        final int columns = (width * rows + height - 1) / height;
        final SlidingBoard board = new SlidingPuzzleScreen.SlidingBoard(columns, rows);
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
        return index == this.board.blank;
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

    static class SlidingBoard extends Board {
        int blank;

        SlidingBoard(final int columns, final int rows) {
            super(columns, rows);
            this.blank = 0;
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

        boolean movable(final int index) {
            final int x = this.x(index);
            final int y = this.y(index);
            return this.contains(x, y) && Math.abs(x - this.x(this.blank)) + Math.abs(y - this.y(this.blank)) == 1;
        }

        boolean tap(final int index) {
            if (this.movable(index)) {
                this.blank = this.swap(index, this.blank);
                return true;
            }
            return false;
        }
    }
}
