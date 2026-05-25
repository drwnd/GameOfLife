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
        if (Input.isKeyPressed(GLFW_MOUSE_BUTTON_LEFT | Input.IS_MOUSE_BUTTON) && ToggleSetting.SIMULATION_RUNNING.value()
                || Input.isKeyPressed(GLFW_MOUSE_BUTTON_RIGHT | Input.IS_MOUSE_BUTTON)) addStart(-movement.x, -movement.y);

        long currentTime = System.nanoTime();
        long nanoTimeBetweenGenerations = (long) (1_000_000_000F / FloatSetting.MAX_GENERATIONS_PER_SECOND.value());
        long nanoTimeSinceLastGeneration = currentTime - lastGenerationNanoTime;

        boolean shouldRunGeneration = nanoTimeSinceLastGeneration > nanoTimeBetweenGenerations;
        shouldRunGeneration = shouldRunGeneration && ToggleSetting.SIMULATION_RUNNING.value();

        if (shouldRunGeneration) {
            glFinish();
            lastGenerationNanoTime = currentTime;
            long start = System.nanoTime();
            if (ToggleSetting.USE_CHUNKING.value()) runGenerationWithChunking();
            else runGenerationNoChunking();
            glFinish();
            System.out.println((System.nanoTime() - start) / 1_000);
        }

        ComputeShader changeShader = (ComputeShader) AssetManager.get(Shaders.CHANGE_CELL);
        changeShader.bind();

        glBindImageTexture(0, texture1, 0, false, 0, GL_READ_WRITE, GL_R32I);
        glBindImageTexture(1, texture0, 0, false, 0, GL_READ_WRITE, GL_R32I);
        while (!toChangePixels.isEmpty()) {
            Vector2i pixelCoordinate = toChangePixels.removeLast();
            changeShader.setUniform("position", pixelCoordinate.x, pixelCoordinate.y);
            glDispatchCompute(1, 1, 1);
            glMemoryBarrier(GL_ALL_BARRIER_BITS);
            chunkingActive = false;
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

        if (shouldRunGeneration) {
            int temp = texture0;
            texture0 = texture1;
            texture1 = temp;
        }
    }

    private void runGenerationNoChunking() {
        chunkingActive = false;
        ComputeShader computeShader = (ComputeShader) AssetManager.get(Shaders.GAME_OF_LIFE_NO_CHUNKING);
        computeShader.bind();
        computeShader.setUniform("mask", MASK);
        glBindImageTexture(0, texture0, 0, false, 0, GL_WRITE_ONLY, GL_R32UI);
        glBindImageTexture(1, texture1, 0, false, 0, GL_READ_ONLY, GL_R32UI);
        glDispatchCompute(1 << SIZE_BITS - 7, 1 << SIZE_BITS - 3, 1);
    }

    private void runGenerationWithChunking() {
        if (!chunkingActive) activateChunking();

        glNamedBufferSubData(indirectDispatchBuffer, 0, new int[]{0, 1, 1});

        ComputeShader chunkDispatcher = (ComputeShader) AssetManager.get(Shaders.CHUNK_DISPATCHER);
        chunkDispatcher.bind();
        chunkDispatcher.setUniform("chunkMask", MASK >> 6);
        glBindImageTexture(0, changedFlagTexture, 0, false, 0, GL_READ_WRITE, GL_R8UI);
        glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 1, indirectDispatchBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, startPositionsBuffer);
        glDispatchCompute(Math.max(1, 1 << SIZE_BITS - 6 - 5), 1 << SIZE_BITS - 6, 1);

        glClearTexImage(changedFlagTexture, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, new int[]{0});

        ComputeShader computeShader = (ComputeShader) AssetManager.get(Shaders.GAME_OF_LIFE_WITH_CHUNKING);
        computeShader.bind();
        computeShader.setUniform("mask", MASK);
        glBindImageTexture(0, texture0, 0, false, 0, GL_WRITE_ONLY, GL_R32UI);
        glBindImageTexture(1, texture1, 0, false, 0, GL_READ_ONLY, GL_R32UI);
        glBindImageTexture(2, changedFlagTexture, 0, false, 0, GL_WRITE_ONLY, GL_R8UI);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, startPositionsBuffer);
        glBindBuffer(GL_DISPATCH_INDIRECT_BUFFER, indirectDispatchBuffer);
        glDispatchComputeIndirect(0);
    }

    private void activateChunking() {
        chunkingActive = true;
        glCopyImageSubData(
                texture1, GL_TEXTURE_2D, 0, 0, 0, 0,
                texture0, GL_TEXTURE_2D, 0, 0, 0, 0,
                1 << SIZE_BITS - 2, 1 << SIZE_BITS - 3, 1);

        glClearTexImage(changedFlagTexture, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, new int[]{0x01010101});
    }

    private static int genTexture(int[] data) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32UI, 1 << SIZE_BITS - 2, 1 << SIZE_BITS - 3, 0, GL_RED_INTEGER, GL_UNSIGNED_INT, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        return texture;
    }

    private static int genChangedFlagTexture() {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, 1 << SIZE_BITS - 6, 1 << SIZE_BITS - 6, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        return texture;
    }

    private static int genStartPositionsBuffer() {
        int buffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (1L << SIZE_BITS - 6) * (1L << SIZE_BITS - 6) * 4, GL_DYNAMIC_COPY);
        return buffer;
    }

    private static int genIndirectDispatchBuffer() {
        int buffer = glGenBuffers();
        glBindBuffer(GL_DISPATCH_INDIRECT_BUFFER, buffer);
        glBufferData(GL_DISPATCH_INDIRECT_BUFFER, new int[]{0, 1, 1}, GL_DYNAMIC_COPY);
        return buffer;
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
        if (changedFlagTexture != 0) glDeleteTextures(changedFlagTexture);
        if (startPositionsBuffer != 0) glDeleteBuffers(startPositionsBuffer);
        if (indirectDispatchBuffer != 0) glDeleteBuffers(indirectDispatchBuffer);

        GameInitializer initializer = (GameInitializer) OptionSetting.INITIALIZER.value();
        texture0 = genTexture(null);
        texture1 = genTexture(initializer.getInitializedBoard());
        changedFlagTexture = genChangedFlagTexture();
        startPositionsBuffer = genStartPositionsBuffer();
        indirectDispatchBuffer = genIndirectDispatchBuffer();
        chunkingActive = false;
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
    private long lastGenerationNanoTime = System.nanoTime();
    private boolean chunkingActive = false;

    private int changedFlagTexture = 0, startPositionsBuffer = 0, indirectDispatchBuffer = 0;

    private GameInput input;
    private final ArrayList<Vector2i> toChangePixels = new ArrayList<>();
}
