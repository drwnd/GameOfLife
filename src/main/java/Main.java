import core.rendering_api.CrashAction;
import core.rendering_api.Window;
import core.settings.Settings;
import game.Renderer;

public final class Main {
    public static void main(String[] args) {
        Settings.loadFromFile();
        Window.init("Game Of Life");
        Window.setCrashCallback((exception -> CrashAction.PRINT_AND_CLOSE));
        Window.pushRenderable(new Renderer());
        Window.renderLoop();
        Window.cleanUp();
    }
}
