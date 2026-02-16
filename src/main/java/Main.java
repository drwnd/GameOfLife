import core.rendering_api.Window;
import core.settings.Settings;
import game.Renderer;

void main() {
    Settings.loadFromFile();
    Window.init("Game Of Life");
    Window.pushRenderable(new Renderer());
    Window.renderLoop();
    Window.cleanUp();
}
