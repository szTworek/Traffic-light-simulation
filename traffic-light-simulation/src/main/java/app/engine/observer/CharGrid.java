package app.engine.observer;

import java.util.*;

public class CharGrid {
    private final char[][] grid;
    private final int width;
    private final int height;
    private final Map<Integer, String> colors;

    public CharGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        this.colors = new HashMap<>();
        for (char[] row : grid) Arrays.fill(row, ' ');
    }

    public void putChar(int row, int col, char c) {
        if (inBounds(row, col)) grid[row][col] = c;
    }

    public void putString(int row, int col, String text) {
        for (int i = 0; i < text.length(); i++) {
            if (inBounds(row, col + i)) grid[row][col + i] = text.charAt(i);
        }
    }

    public void putColoredString(int row, int col, String text, String ansiColor) {
        putString(row, col, text);
        for (int i = 0; i < text.length(); i++) {
            if (inBounds(row, col + i)) {
                colors.put(key(row, col + i), ansiColor);
            }
        }
    }

    public void hLine(int row, int col, int len, char c) {
        for (int i = 0; i < len; i++) putChar(row, col + i, c);
    }

    public void vLine(int row, int col, int len, char c) {
        for (int i = 0; i < len; i++) putChar(row + i, col, c);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < height && c >= 0 && c < width;
    }

    private int key(int row, int col) {
        return row * width + col;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String reset = "\033[0m";
        for (int r = 0; r < height; r++) {
            StringBuilder line = new StringBuilder();
            String currentColor = null;
            for (int c = 0; c < width; c++) {
                String color = colors.get(key(r, c));
                if (!Objects.equals(color, currentColor)) {
                    if (currentColor != null) line.append(reset);
                    if (color != null) line.append(color);
                    currentColor = color;
                }
                line.append(grid[r][c]);
            }
            if (currentColor != null) line.append(reset);
            sb.append(line.toString().stripTrailing());
            sb.append('\n');
        }
        return sb.toString();
    }
}
