package game;

import core.rendering_api.Input;
import core.rendering_api.Window;

import static org.lwjgl.glfw.GLFW.*;

public final class GameInput extends Input {

    public GameInput(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setInputMode() {

    }

    @Override
    public void cursorPosCallback(long window, double xPos, double yPos) {

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

    private final Renderer renderer;
}
