package me.paulf.minecraftmania;

public class Board {
    protected final int columns;

    protected final int rows;

    protected final int[] state;

    public Board(final int columns, final int rows) {
        this.columns = columns;
        this.rows = rows;
        this.state = new int[columns * rows];
        for (int i = 0; i < columns * rows; i++) {
            this.state[i] = i;
        }
    }

    public int get(final int index) {
        return this.state[index];
    }

    public int x(final int index) {
        return index < 0 ? -1 : index % this.columns;
    }

    public int y(final int index) {
        return index < 0 ? -1 : index / this.columns;
    }

    public int index(final int x, final int y) {
        return x + y * this.columns;
    }

    public int swap(final int first, final int second) {
        final int[] s = this.state;
        final int temp = s[first];
        s[first] = s[second];
        s[second] = temp;
        return first;
    }

    public boolean contains(final int x, final int y) {
        return x >= 0 && y >= 0 && x < this.columns && y < this.rows;
    }

    public boolean isSolved() {
        for (int i = this.columns * this.rows; i --> 0; ) {
            if (this.state[i] != i) {
                return false;
            }
        }
        return true;
    }
}
