package game;

import core.assets.AssetManager;
import core.renderables.Renderable;
import core.rendering_api.Input;
import core.rendering_api.Window;
import core.rendering_api.shaders.ComputeShader;
import core.rendering_api.shaders.GuiShader;
import core.settings.FloatSetting;
import core.settings.KeySetting;
import core.settings.OptionSetting;
import core.settings.ToggleSetting;
import core.settings.optionSettings.ColorOption;
import org.joml.Vector2f;
import org.joml.Vector2i;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

public final class Renderer extends Renderable {

    public static int SIZE_BITS;
    public static int MASK;

    public Renderer() {
        super(new Vector2f(1.0F), new Vector2f(0.0F));

        resetBoard();
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
        if (button == KeySetting.RESET_BOARD.keybind()) resetBoard();

        if (button == (GLFW_MOUSE_BUTTON_LEFT | Input.IS_MOUSE_BUTTON)) change(Input.getCursorPos());
    }

    public void incCellSize() {
        if (cellSize == 64.0F) return;
        cellSize *= 2.0F;

        addStart(Window.getWidth() / 2, Window.getHeight() / 2);
    }

    public void decCellSize() {
        if (cellSize == 0.015625f) return;
        cellSize *= 0.5F;

        addStart(-Window.getWidth() / 4, -Window.getHeight() / 4);
    }

    public void addStart(int x, int y) {
        startX += (int) (x / cellSize);
        startY += (int) (y / cellSize);
    }


    @Override
    public void setOnTop() {
        Window.setInput(input = new GameInput(this));
    }

    @Override
    public void renderSelf(Vector2f position, Vector2f size) {
        Vector2i movement = input.getCursorMovement();
        if (Input.isKeyPressed(GLFW_MOUSE_BUTTON_LEFT | Input.IS_MOUSE_BUTTON)
                || Input.isKeyPressed(GLFW_MOUSE_BUTTON_RIGHT | Input.IS_MOUSE_BUTTON)) addStart(-movement.x, -movement.y);

        if (ToggleSetting.SIMULATION_RUNNING.value()) {
            ComputeShader computeShader = (ComputeShader) AssetManager.get(Shaders.GAME_OF_LIFE);
            computeShader.bind();
            computeShader.setUniform("mask", MASK);
            glBindImageTexture(0, texture0, 0, false, 0, GL_WRITE_ONLY, GL_R32I);
            glBindImageTexture(1, texture1, 0, false, 0, GL_READ_ONLY, GL_R32I);
            glDispatchCompute(1 << SIZE_BITS - 5, 1 << SIZE_BITS - 6, 1);
            glMemoryBarrier(GL_ALL_BARRIER_BITS);
        }

        ComputeShader changeShader = (ComputeShader) AssetManager.get(Shaders.CHANGE_CELL);
        changeShader.bind();

        glBindImageTexture(0, texture1, 0, false, 0, GL_READ_WRITE, GL_R32I);
        while (!toChangePixels.isEmpty()) {
            Vector2i pixelCoordinate = toChangePixels.removeLast();
            changeShader.setUniform("position", pixelCoordinate.x, pixelCoordinate.y);
            glDispatchCompute(1, 1, 1);
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
        shader.setUniform("cellColor", ((ColorOption) OptionSetting.CELL_COLOR.value()).getColor());
        shader.setUniform("backColor", ((ColorOption) OptionSetting.BACKGROUND_COLOR.value()).getColor());

        shader.flipNextDrawVertically();
        shader.drawFullScreenQuad();


        if (ToggleSetting.SIMULATION_RUNNING.value()) {
            int temp = texture0;
            texture0 = texture1;
            texture1 = temp;
        }
    }


    private static int genTexture(int[] data) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32UI, 1 << SIZE_BITS - 5, 1 << SIZE_BITS, 0, GL_RED_INTEGER, GL_UNSIGNED_INT, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
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

    private void resetBoard() {
        SIZE_BITS = (int) FloatSetting.SIZE_BITS.value();
        MASK = (1 << SIZE_BITS) - 1;

        if (texture0 != 0) glDeleteTextures(texture0);
        if (texture1 != 0) glDeleteTextures(texture1);

        GameInitializer initializer = (GameInitializer) OptionSetting.INITIALIZER.value();
        texture0 = genTexture(null);
        texture1 = genTexture(initializer.getInitializedBoard());
    }

    private void change(Vector2i cursorPos) {
        if (ToggleSetting.SIMULATION_RUNNING.value()) return;

        int x = (int) (startX + cursorPos.x / cellSize) & MASK;
        int y = (int) (startY + cursorPos.y / cellSize) & MASK;

        toChangePixels.add(new Vector2i(x, y));
    }


    private int texture0 = 0, texture1 = 0;
    private int startX = 0, startY = 0;
    private float cellSize = 1;

    private GameInput input;
    private final ArrayList<Vector2i> toChangePixels = new ArrayList<>();
}
