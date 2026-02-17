package game;

import core.settings.FloatSetting;
import core.settings.optionSettings.Option;

import static game.Renderer.MASK;
import static game.Renderer.SIZE_BITS;

public enum GameInitializer implements Option {

    RANDOM(() -> randomizeBoard(FloatSetting.RANDOMIZER_THRESHOLD.value())),
    EMPTY(() -> null),
    VERTICAL_STRIPES(GameInitializer::fillBoardWidthVerticalStripes),
    HORIZONTAL_STRIPES(GameInitializer::fillBoardWidthHorizontalStripes),
    DIAGONAL_GLIDERS(GameInitializer::fillBoardWithDiagonalGliders),
    STRAIGHT_GLIDERS(GameInitializer::fillBoardWithStraightGliders),
    SMALL_STRAIGHT_GLIDERS(GameInitializer::fillBoardWithSmallStraightGliders);


    GameInitializer(BoardInitializer initializer) {
        this.initializer = initializer;
    }

    public short[] getInitializedBoard() {
        return initializer.getInitializedBoard();
    }


    private static short[] fillBoardWidthVerticalStripes() {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < 1 << SIZE_BITS; x++)
            for (int y = 0; y < 1 << SIZE_BITS; y += 2) changePixel(board, x, y);
        return board;
    }

    private static short[] fillBoardWidthHorizontalStripes() {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < 1 << SIZE_BITS; x += 2)
            for (int y = 0; y < 1 << SIZE_BITS; y++) changePixel(board, x, y);
        return board;
    }

    private static short[] fillBoardWithSmallStraightGliders() {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < (1 << SIZE_BITS) - 7; x += 7)
            for (int y = 0; y < (1 << SIZE_BITS) - 6; y += 6) {
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

    private static short[] fillBoardWithStraightGliders() {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < (1 << SIZE_BITS) - 9; x += 9)
            for (int y = 0; y < (1 << SIZE_BITS) - 7; y += 7) {
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

    private static short[] fillBoardWithDiagonalGliders() {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < (1 << SIZE_BITS) - 5; x += 5)
            for (int y = 0; y < (1 << SIZE_BITS) - 5; y += 5) {
                changePixel(board, x, y);
                changePixel(board, x + 1, y + 1);
                changePixel(board, x + 1, y + 2);
                changePixel(board, x + 2, y);
                changePixel(board, x + 2, y + 1);
            }
        return board;
    }

    private static short[] randomizeBoard(double threshold) {
        short[] board = new short[(1 << SIZE_BITS) * (1 << SIZE_BITS) / 2];
        for (int x = 0; x < 1 << SIZE_BITS; x++)
            for (int y = 0; y < 1 << SIZE_BITS; y++) {
                if (Math.random() < threshold) continue;
                changePixel(board, x, y);
            }
        return board;
    }

    private static void changePixel(short[] board, int x, int y) {
        int index = (x & MASK) << SIZE_BITS | y & MASK;
        board[index >> 1] ^= (short) (1 << (index & 1) * 8);
    }


    private final BoardInitializer initializer;

    private interface BoardInitializer {
        short[] getInitializedBoard();
    }
}
