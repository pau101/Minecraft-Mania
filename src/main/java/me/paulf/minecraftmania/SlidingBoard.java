package me.paulf.minecraftmania;

public class SlidingBoard extends Board {
    private int blank;

    public SlidingBoard(final int columns, final int rows) {
        super(columns, rows);
        this.blank = 0;
    }

    public boolean isBlank(final int index) {
        return index == this.blank;
    }

    public boolean up() {
        if (this.y(this.blank) > 0) {
            this.blank = this.swap(this.blank - this.columns, this.blank);
            return true;
        }
        return false;
    }

    public boolean down() {
        if (this.y(this.blank) < this.rows - 1) {
            this.blank = this.swap(this.blank + this.columns, this.blank);
            return true;
        }
        return false;
    }

    public boolean left() {
        if (this.x(this.blank) < this.columns - 1) {
            this.blank = this.swap(this.blank + 1, this.blank);
            return true;
        }
        return false;
    }

    public boolean right() {
        if (this.x(this.blank) > 0) {
            this.blank = this.swap(this.blank - 1, this.blank);
            return true;
        }
        return false;
    }

    public boolean movable(final int index) {
        final int x = this.x(index);
        final int y = this.y(index);
        return this.contains(x, y) && Math.abs(x - this.x(this.blank)) + Math.abs(y - this.y(this.blank)) == 1;
    }

    public boolean tap(final int index) {
        if (this.movable(index)) {
            this.blank = this.swap(index, this.blank);
            return true;
        }
        return false;
    }
}
