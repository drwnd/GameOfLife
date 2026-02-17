package core.settings;

import static org.lwjgl.glfw.GLFW.*;

public enum KeySetting implements KeyBound {

    SHIFT_UP(GLFW_KEY_UP),
    SHIFT_RIGHT(GLFW_KEY_RIGHT),
    SHIFT_DOWN(GLFW_KEY_DOWN),
    SHIFT_LEFT(GLFW_KEY_LEFT),
    ZOOM_IN(GLFW_KEY_U),
    ZOOM_OUT(GLFW_KEY_I),
    OPEN_SETTINGS(GLFW_KEY_O),
    SHIFT_BIGGER_DISTANCE(GLFW_KEY_LEFT_CONTROL),

    RESIZE_WINDOW(GLFW_KEY_F11),
    RELOAD_ASSETS(GLFW_KEY_F10),
    RELOAD_SETTINGS(GLFW_KEY_F9),
    RELOAD_LANGUAGE(GLFW_KEY_F8),
    RELOAD_FONT(GLFW_KEY_F7);

    public static void setIfPresent(String name, String value) {
        try {
            valueOf(name).keybind = Integer.parseInt(value);
        } catch (IllegalArgumentException ignore) {

        }
    }

    KeySetting(int defaultValue) {
        this.defaultKeybind = defaultValue;
        this.keybind = defaultValue;
    }


    @Override
    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    @Override
    public int keybind() {
        return keybind;
    }

    @Override
    public int defaultKeybind() {
        return defaultKeybind;
    }

    private final int defaultKeybind;
    private int keybind;
}
