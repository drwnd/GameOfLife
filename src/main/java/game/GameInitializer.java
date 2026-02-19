package game;

import core.settings.FloatSetting;
import core.settings.optionSettings.Option;

import java.util.Arrays;

import static game.Renderer.MASK;
import static game.Renderer.SIZE_BITS;

public enum GameInitializer implements Option {

    RANDOM(() -> randomizeBoard(FloatSetting.RANDOMIZER_THRESHOLD.value())),
    EMPTY(GameInitializer::getEmptyBoard),
    VERTICAL_STRIPES(GameInitializer::fillBoardWidthVerticalStripes),
    HORIZONTAL_STRIPES(GameInitializer::fillBoardWidthHorizontalStripes),
    DIAGONAL_GLIDERS(GameInitializer::fillBoardWithDiagonalGliders),
    STRAIGHT_GLIDERS(GameInitializer::fillBoardWithStraightGliders),
    SMALL_STRAIGHT_GLIDERS(GameInitializer::fillBoardWithSmallStraightGliders);


    GameInitializer(BoardInitializer initializer) {
        this.initializer = initializer;
    }

    public int[] getInitializedBoard() {
        return initializer.getInitializedBoard();
    }

    private static int[] getEmptyBoard() {
        return null;
    }

    private static int[] fillBoardWidthHorizontalStripes() {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < 1 << SIZE_BITS; y += 2)
            for (int x = 0; x < 1 << SIZE_BITS; x++) changePixel(board, x, y);
        return board;
    }

    private static int[] fillBoardWidthVerticalStripes() {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < 1 << SIZE_BITS; y++)
            for (int x = 0; x < 1 << SIZE_BITS; x += 2) changePixel(board, x, y);
        return board;
    }

    private static int[] fillBoardWithSmallStraightGliders() {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < (1 << SIZE_BITS) - 6; y += 6)
            for (int x = 0; x < (1 << SIZE_BITS) - 7; x += 7) {
                changePixel(board, x, y);
                changePixel(board, x + 3, y);
                changePixel(board, x, y + 2);
                changePixel(board, x + 4, y + 1);
                changePixel(board, x + 4, y + 2);
                changePixel(board, x + 4, y + 3);
                changePixel(board, x + 1, y + 3);
                changePixel(board, x + 2, y + 3);
                changePixel(board, x + 3, y + 3);
            }
        return board;
    }

    private static int[] fillBoardWithStraightGliders() {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < (1 << SIZE_BITS) - 7; y += 7)
            for (int x = 0; x < (1 << SIZE_BITS) - 9; x += 9) {
                changePixel(board, x + 2, y);
                changePixel(board, x + 3, y);
                changePixel(board, x, y + 1);
                changePixel(board, x + 5, y + 1);
                changePixel(board, x + 6, y + 2);
                changePixel(board, x, y + 3);
                changePixel(board, x + 6, y + 3);
                changePixel(board, x + 1, y + 4);
                changePixel(board, x + 2, y + 4);
                changePixel(board, x + 3, y + 4);
                changePixel(board, x + 4, y + 4);
                changePixel(board, x + 5, y + 4);
                changePixel(board, x + 6, y + 4);
            }
        return board;
    }

    private static int[] fillBoardWithDiagonalGliders() {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < (1 << SIZE_BITS) - 5; y += 5)
            for (int x = 0; x < (1 << SIZE_BITS) - 5; x += 5) {
                changePixel(board, x, y);
                changePixel(board, x + 1, y + 1);
                changePixel(board, x + 1, y + 2);
                changePixel(board, x + 2, y);
                changePixel(board, x + 2, y + 1);
            }
        return board;
    }

    private static int[] randomizeBoard(double threshold) {
        int[] board = new int[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 32];

        for (int y = 0; y < 1 << SIZE_BITS; y++)
            for (int x = 0; x < 1 << SIZE_BITS; x++) {
                if (Math.random() < threshold) continue;
                changePixel(board, x, y);
            }

        Arrays.fill(board, -1);
        return board;
    }

    private static void changePixel(int[] board, int x, int y) {
        int index = (y & MASK) << SIZE_BITS | x & MASK;
        board[index >> 5] ^= 1 << index;
    }


    private final BoardInitializer initializer;

    private interface BoardInitializer {
        int[] getInitializedBoard();
    }
}
