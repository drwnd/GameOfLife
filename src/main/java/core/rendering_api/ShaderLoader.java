package core.rendering_api;

import core.assets.identifiers.ShaderIdentifier;
import core.rendering_api.shaders.Shader;
import core.assets.CoreShaders;
import core.rendering_api.shaders.GuiShader;
import core.rendering_api.shaders.TextShader;

import game.Shaders;

public final class ShaderLoader {

    private ShaderLoader() {
    }


    public static Shader loadShader(ShaderIdentifier identifier) {
        return switch (identifier) {
            case CoreShaders.GUI -> new GuiShader("Gui.vert", "Gui.frag", identifier);
            case CoreShaders.GUI_BACKGROUND -> new GuiShader("Gui.vert", "GuiBackground.frag", identifier);
            case CoreShaders.TEXT -> new TextShader("Text.vert", "Text.frag", identifier);
            case Shaders.COMPUTE -> new Shader("Material.vert", "Opaque.frag", identifier);
            default -> throw new IllegalStateException("Unexpected value: " + identifier);
        };
    }
}
