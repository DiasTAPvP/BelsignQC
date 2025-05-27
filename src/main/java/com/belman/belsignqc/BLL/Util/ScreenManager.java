package com.belman.belsignqc.BLL.Util;

import com.belman.belsignqc.GUI.Controller.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

//Singleton style class to manage screens
public class ScreenManager extends StackPane {

    private Map<String, Node> screens = new HashMap<>();
    private Map<String, BaseController> controllers = new HashMap<>();
    private String currentScreen;

    public ScreenManager() {
        super();
    }

    /**
     * Adds a screen to the manager.
     *
     * @param name  The name of the screen.
     * @param screen The Node representing the screen.
     *
     * @param name
     * @param screen
     */
    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }

    /**
     * Associates a controller with a screen name.
     *
     * @param name The name of the screen.
     * @param controller The controller for the screen.
     */
    public void addController(String name, BaseController controller) {
        controllers.put(name, controller);
    }

    public boolean removeScreen(String name) {
        if (name != null && screens.containsKey(name)) {
            if (screens.remove(name) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean setScreen(String name) {
        if (screens.get(name) != null) {
            // If there's a screen already being displayed, remove it
            if (currentScreen != null && !currentScreen.equals(name)) {
                getChildren().remove(screens.get(currentScreen));
            }

            // If the screen isn't already displayed, add it
            if (!getChildren().contains(screens.get(name))) {
                getChildren().add(screens.get(name));
            }

            currentScreen = name;

            // Notify the controller that its screen is now active
            BaseController controller = controllers.get(name);
            if (controller != null) {
                controller.onScreenActivated();
            }

            return true;
        }
        return false;
    }

    public String getCurrentScreen() {
        return currentScreen;
    }
}
