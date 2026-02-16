package core.settings;

import static org.lwjgl.glfw.GLFW.*;

public enum ToggleSetting implements KeyBound {

    V_SYNC(true),
    SIMULATION_RUNNING(true, GLFW_KEY_SPACE);

    public static void setIfPresent(String name, String value) {
        try {
            String[] values = value.split("_");
            valueOf(name).value = Boolean.parseBoolean(values[0]);
            valueOf(name).keybind = Integer.parseInt(values[1]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignore) {

        }
    }

    ToggleSetting(boolean defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.defaultKeybind = GLFW_KEY_UNKNOWN;
        this.keybind = defaultKeybind;
    }

    ToggleSetting(boolean defaultValue, int defaultKeybind) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.defaultKeybind = defaultKeybind;
        this.keybind = defaultKeybind;
    }

    void setValue(boolean value) {
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    public boolean defaultValue() {
        return defaultValue;
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

    private final boolean defaultValue;
    private boolean value;

    private final int defaultKeybind;
    private int keybind;
}
