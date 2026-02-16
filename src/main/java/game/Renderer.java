package game;

import core.renderables.Renderable;
import core.rendering_api.Window;
import org.joml.Vector2f;

public class Renderer extends Renderable {

    public Renderer() {
        super(new Vector2f(1.0F), new Vector2f(0.0F));
    }

    @Override
    public void setOnTop() {
        Window.setInput(new GameInput());
    }

    @Override
    public void renderSelf(Vector2f position, Vector2f size) {

    }
}
