package core.settings;

import core.settings.optionSettings.*;
import core.languages.Language;
import game.GameInitializer;

public enum OptionSetting {

    FONT(new FontOption("Default")),
    LANGUAGE(new Language("English")),
    TEXTURE_PACK(new TexturePack("Default")),

    INITIALIZER(GameInitializer.RANDOM),
    CELL_COLOR(ColorOption.WHITE),
    BACKGROUND_COLOR(ColorOption.BLACK);

    public static void setIfPresent(String name, String value) {
        try {
            OptionSetting setting = valueOf(name);

            Option savedValue = setting.defaultValue.value(value);

            if (savedValue != null) setting.value = savedValue;
        } catch (IllegalArgumentException ignore) {

        }
    }

    OptionSetting(Option defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    void setValue(Option value) {
        this.value = value;
    }

    public Option value() {
        return value;
    }

    public Option defaultValue() {
        return defaultValue;
    }

    private Option value;
    private final Option defaultValue;
}
