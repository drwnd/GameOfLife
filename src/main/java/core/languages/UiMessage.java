package core.languages;

import core.utils.StringGetter;

public enum UiMessage implements StringGetter {

    SETTINGS,
    BACK,
    APPLY_SETTINGS,
    RESET_ALL_SETTINGS,
    RESET_SETTING,
    LANGUAGE,
    FONT,
    GUI_SIZE,
    TEXT_SIZE,
    RIM_THICKNESS,
    RESIZE_WINDOW,
    TEXTURE_PACK,
    KEYBIND,

    SHIFT_UP,
    SHIFT_RIGHT,
    SHIFT_DOWN,
    SHIFT_LEFT,
    ZOOM_IN,
    ZOOM_OUT,
    PAUSE,
    OPEN_SETTINGS,
    TOGGLE_SIMULATION;

    @Override
    public String get() {
        return Language.getUiMessage(this);
    }
}
