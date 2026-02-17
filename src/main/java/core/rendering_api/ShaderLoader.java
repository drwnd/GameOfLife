package core.rendering_api;

import core.assets.identifiers.ShaderIdentifier;
import core.rendering_api.shaders.*;
import core.assets.CoreShaders;

import game.Shaders;

public final class ShaderLoader {

    private ShaderLoader() {
    }


    public static Shader loadShader(ShaderIdentifier identifier) {
        return switch (identifier) {
            case CoreShaders.GUI -> new GuiShader("Gui.vert", "Gui.frag", identifier);
            case CoreShaders.GUI_BACKGROUND -> new GuiShader("Gui.vert", "GuiBackground.frag", identifier);
            case CoreShaders.TEXT -> new TextShader("Text.vert", "Text.frag", identifier);
            case Shaders.GAME_OF_LIFE -> new ComputeShader("GameOfLife.comp", identifier);
            case Shaders.CHANGE_CELL -> new ComputeShader("ChangeCell.comp", identifier);
            case Shaders.RENDERING -> new GuiShader("Gui.vert", "Render.frag", identifier);
            default -> throw new IllegalStateException("Unexpected value: " + identifier);
        };
    }
}
