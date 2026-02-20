package game;

import core.languages.UiMessage;
import core.renderables.CoreSettingsRenderable;
import core.settings.FloatSetting;
import core.settings.KeySetting;
import core.settings.OptionSetting;
import core.settings.ToggleSetting;
import core.utils.Message;

public class SettingsMenu extends CoreSettingsRenderable {

    public SettingsMenu() {
        addOption(OptionSetting.INITIALIZER, UiMessage.GAME_INITIALIZER);
        addSlider(FloatSetting.SIZE_BITS, UiMessage.SIZE_BITS);
        addSlider(FloatSetting.RANDOMIZER_THRESHOLD, UiMessage.RANDOMIZER_THRESHOLD);
        addToggle(ToggleSetting.SIMULATION_RUNNING, UiMessage.TOGGLE_SIMULATION);
        addOption(OptionSetting.BACKGROUND_COLOR, UiMessage.BACKGROUND_COLOR);
        addOption(OptionSetting.CELL_COLOR, UiMessage.CELL_COLOR);

        addKeySelector(KeySetting.SHIFT_UP, UiMessage.SHIFT_UP);
        addKeySelector(KeySetting.SHIFT_RIGHT, UiMessage.SHIFT_RIGHT);
        addKeySelector(KeySetting.SHIFT_DOWN, UiMessage.SHIFT_DOWN);
        addKeySelector(KeySetting.SHIFT_LEFT, UiMessage.SHIFT_LEFT);
        addKeySelector(KeySetting.ZOOM_IN, UiMessage.ZOOM_IN);
        addKeySelector(KeySetting.ZOOM_OUT, UiMessage.ZOOM_OUT);
        addKeySelector(KeySetting.SHIFT_BIGGER_DISTANCE, UiMessage.SHIFT_BIGGER_DISTANCE);
        addKeySelector(KeySetting.RESET_BOARD, UiMessage.RESET_BOARD);

        addSlider(FloatSetting.GUI_SIZE, UiMessage.GUI_SIZE);
        addSlider(FloatSetting.TEXT_SIZE, UiMessage.TEXT_SIZE);
        addSlider(FloatSetting.RIM_THICKNESS, UiMessage.RIM_THICKNESS);

        addOption(OptionSetting.FONT, UiMessage.FONT);
        addOption(OptionSetting.LANGUAGE, UiMessage.LANGUAGE);
        addOption(OptionSetting.TEXTURE_PACK, UiMessage.TEXTURE_PACK);

        addKeySelector(KeySetting.RESIZE_WINDOW, UiMessage.RESIZE_WINDOW);
        addKeySelector(KeySetting.RELOAD_ASSETS, new Message("Reload Assets"));
        addKeySelector(KeySetting.RELOAD_SETTINGS, new Message("Reload Settings"));
        addKeySelector(KeySetting.RELOAD_LANGUAGE, new Message("Reload Language"));
        addKeySelector(KeySetting.RELOAD_FONT, new Message("Reload Font"));
    }
}
