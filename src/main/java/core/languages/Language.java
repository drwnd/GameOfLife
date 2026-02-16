package core.languages;

import core.settings.OptionSetting;
import core.settings.optionSettings.Option;
import core.utils.FileManager;

import java.io.File;


public final class Language implements Option {

    public Language(String languageName) {
        this(new File("assets/languages/" + languageName));
    }

    private Language(File languageFile) {
        this.languageFile = languageFile;

        uiMessages = new String[UiMessage.values().length];

        load();
    }

    public void load() {
        String[] thisUiMessages = FileManager.readAllLines(new File(languageFile.getPath() + "/uiMessages"));

        String[] defaultUiMessages;
        if ("English".equals(languageFile.getName())) defaultUiMessages = thisUiMessages;
        else defaultUiMessages = FileManager.readAllLines(new File("assets/languages/English/uiMessages"));

        fill(uiMessages, thisUiMessages, defaultUiMessages);
    }


    public static String getUiMessage(UiMessage message) {
        return ((Language) OptionSetting.LANGUAGE.value()).getLanguagesUiMessage(message);
    }


    @Override
    public Option next() {
        File[] languages = FileManager.getSiblings(languageFile);
        int index = (FileManager.indexOf(languageFile, languages) + 1) % languages.length;
        return new Language(languages[index]);
    }

    @Override
    public Option previous() {
        File[] languages = FileManager.getSiblings(languageFile);
        int index = (FileManager.indexOf(languageFile, languages) - 1 + languages.length) % languages.length;
        return new Language(languages[index]);
    }

    @Override
    public Option value(String name) {
        return new Language(name);
    }

    @Override
    public String name() {
        return languageFile.getName();
    }

    @Override
    public String toString() {
        return name();
    }


    private String getLanguagesUiMessage(UiMessage message) {
        return uiMessages[message.ordinal()];
    }

    private static void fill(String[] destination, String[] source, String[] backUp) {
        for (int index = 0; index < destination.length; index++) {
            if (index >= source.length || "".equals(source[index]) || source[index] == null) {
                destination[index] = backUp.length > index ? backUp[index] : "--Undefined--";
                continue;
            }
            destination[index] = source[index];
        }
    }

    private final File languageFile;
    private final String[] uiMessages;
}
