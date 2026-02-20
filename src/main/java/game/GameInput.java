package game;

import core.rendering_api.Input;
import core.rendering_api.Window;
import org.joml.Vector2i;

import static org.lwjgl.glfw.GLFW.*;

public final class GameInput extends Input {

    public GameInput(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setInputMode() {
        setStandardInputMode();
    }

    @Override
    public void cursorPosCallback(long window, double xPos, double yPos) {
        standardCursorPosCallBack(xPos, yPos);
    }

    @Override
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        renderer.handleInput(button | Input.IS_MOUSE_BUTTON, action);
    }

    @Override
    public void scrollCallback(long window, double xScroll, double yScroll) {

    }

    @Override
    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(Window.getWindow(), true);
        renderer.handleInput(key | Input.IS_KEYBOARD_BUTTON, action);
    }

    @Override
    public void charCallback(long window, int codePoint) {

    }

    public Vector2i getCursorMovement() {
        Vector2i movement = new Vector2i(cursorPos).sub(lastCursorPos);
        lastCursorPos.set(cursorPos);
        return movement;
    }

    private final Renderer renderer;
    private final Vector2i lastCursorPos = new Vector2i(0, 0);
}
