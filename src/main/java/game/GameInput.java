package game;

import core.rendering_api.Input;
import core.rendering_api.Window;
import core.settings.KeySetting;

import static org.lwjgl.glfw.GLFW.*;

public class GameInput extends Input {

    @Override
    public void setInputMode() {

    }

    @Override
    public void cursorPosCallback(long window, double xPos, double yPos) {

    }

    @Override
    public void mouseButtonCallback(long window, int button, int action, int mods) {

    }

    @Override
    public void scrollCallback(long window, double xScroll, double yScroll) {

    }

    @Override
    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(Window.getWindow(), true);
        if (key == KeySetting.OPEN_SETTINGS.keybind() && action == GLFW_PRESS) Window.pushRenderable(new SettingsMenu());
    }

    @Override
    public void charCallback(long window, int codePoint) {

    }
}
