package game;

import core.assets.AssetManager;
import core.languages.Language;
import core.languages.UiMessage;
import core.renderables.Renderable;
import core.rendering_api.Input;
import core.rendering_api.Window;
import core.rendering_api.shaders.ComputeShader;
import core.rendering_api.shaders.GuiShader;
import core.settings.KeySetting;
import core.settings.OptionSetting;
import core.settings.ToggleSetting;
import org.joml.Vector2f;

import javax.swing.*;
import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

public final class Renderer extends Renderable {

    public static final int SIZE_BITS = 10;
    public static final int MASK = (1 << SIZE_BITS) - 1;


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

//        if (button == KeySetting.CHANGE_BACKGROUND_COLOR.keybind()) backColor = chooseColor(backColor, UiMessage.CHANGE_BACKGROUND_COLOR);
//        if (button == KeySetting.CHANGE_CELL_COLOR.keybind()) cellColor = chooseColor(cellColor, UiMessage.CHANGE_CELL_COLOR);
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

    private void resetBoard() {
        if (texture0 != 0) glDeleteTextures(texture0);
        if (texture1 != 0) glDeleteTextures(texture1);

        GameInitializer initializer = (GameInitializer) OptionSetting.INITIALIZER.value();
        texture0 = genTexture(null);
        texture1 = genTexture(initializer.getInitializedBoard());
    }


    private static Color chooseColor(Color previous, UiMessage message) {
        return JColorChooser.showDialog(null, Language.getUiMessage(message), previous);
    }


    private int texture0 = 0, texture1 = 0;

    private int startX = 0, startY = 0;
    private int cellSize = 1;
    private Color cellColor = Color.WHITE, backColor = Color.BLACK;
}
