package game;

import core.assets.AssetManager;
import core.renderables.Renderable;
import core.rendering_api.Input;
import core.rendering_api.Window;
import core.rendering_api.shaders.ComputeShader;
import core.rendering_api.shaders.GuiShader;
import core.settings.KeySetting;
import core.settings.ToggleSetting;
import org.joml.Vector2f;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.opengl.GL46.*;

public final class Renderer extends Renderable {

    public Renderer() {
        super(new Vector2f(1.0F), new Vector2f(0.0F));

        texture0 = genTexture(null);
        texture1 = genTexture(randomizeBoard(0.25));
    }


    public void handleInput(int button, int action) {
        if (action != GLFW_PRESS) return;
        if (button == KeySetting.OPEN_SETTINGS.keybind()) Window.pushRenderable(new SettingsMenu());
        if (button == KeySetting.ZOOM_IN.keybind()) incCellSize();
        if (button == KeySetting.ZOOM_OUT.keybind()) decCellSize();
        if (button == KeySetting.SHIFT_RIGHT.keybind()) addStart(Input.isKeyPressed(KeySetting.SHIFT_BIGGER_DISTANCE) ? 256 : 128, 0);
        if (button == KeySetting.SHIFT_LEFT.keybind()) addStart(Input.isKeyPressed(KeySetting.SHIFT_BIGGER_DISTANCE) ? -256 : -128, 0);
        if (button == KeySetting.SHIFT_UP.keybind()) addStart(0, Input.isKeyPressed(KeySetting.SHIFT_BIGGER_DISTANCE) ? 256 : 128);
        if (button == KeySetting.SHIFT_DOWN.keybind()) addStart(0, Input.isKeyPressed(KeySetting.SHIFT_BIGGER_DISTANCE) ? -256 : -128);
    }

    public void incCellSize() {
        if (cellSize == 1 << 6) return;
        cellSize <<= 1;
    }

    public void decCellSize() {
        if (cellSize == 1) return;
        cellSize >>= 1;
    }

    public void addStart(int x, int y) {
        startX += x / cellSize;
        startY += y / cellSize;
    }


    @Override
    public void setOnTop() {
        Window.setInput(new GameInput(this));
    }

    @Override
    public void renderSelf(Vector2f position, Vector2f size) {

        if (ToggleSetting.SIMULATION_RUNNING.value()) {
            ComputeShader computeShader = (ComputeShader) AssetManager.get(Shaders.GAME_OF_LIFE);
            computeShader.bind();
            computeShader.setUniform("mask", MASK);
            glBindImageTexture(0, texture0, 0, false, 0, GL_WRITE_ONLY, GL_R8);
            glBindImageTexture(1, texture1, 0, false, 0, GL_READ_ONLY, GL_R8);
            glDispatchCompute(1 << SIZE_BITS - 3, 1 << SIZE_BITS - 3, 1);
            glMemoryBarrier(GL_ALL_BARRIER_BITS);
        }

        GuiShader shader = (GuiShader) AssetManager.get(Shaders.RENDERING);
        shader.bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture1);

        shader.setUniform("board", 0);
        shader.setUniform("start", (float) startX, startY);
        shader.setUniform("viewSize", getSizeX(), getSizeY());
        shader.setUniform("boardSize", 1 << SIZE_BITS);
        shader.setUniform("cellColor", cellColor);
        shader.setUniform("backColor", backColor);

        shader.flipNextDrawVertically();
        shader.drawFullScreenQuad();


        if (ToggleSetting.SIMULATION_RUNNING.value()) {
            int temp = texture0;
            texture0 = texture1;
            texture1 = temp;
        }
    }


    private int genTexture(short[] data) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, 1 << SIZE_BITS, 1 << SIZE_BITS, 0, GL_RED, GL_UNSIGNED_BYTE, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        return texture;
    }

    private float getSizeX() {
        return (float) Window.getWidth() / cellSize;
    }

    private float getSizeY() {
        return (float) Window.getHeight() / cellSize;
    }

    private short[] fillBoardWithSmallStraightGliders() {
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

    private short[] fillBoardWithStraightGliders() {
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

    private short[] fillBoardWithDiagonalGliders() {
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

    private short[] randomizeBoard(double threshold) {
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

    private int texture0, texture1;

    private int startX = 0, startY = 0;
    private int cellSize = 1;
    private Color cellColor = Color.WHITE, backColor = Color.BLACK;

    private static final int SIZE_BITS = 10;
    private static final int MASK = (1 << SIZE_BITS) - 1;
}
