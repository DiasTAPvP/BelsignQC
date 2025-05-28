package com.belman.belsignqc.BLL.Util;

import com.belman.belsignqc.GUI.Controller.BaseController;
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
    public void addScreen(String name, Node screen, BaseController controller) {
        screens.put(name, screen);
        controllers.put(name, controller);
    }

    /**
     * Gets the controller associated with a screen.
     *
     * @param name The name of the screen.
     * @return The controller for the screen, or null if not found.
     */
    public BaseController getController(String name) {
        return controllers.get(name);
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
            return true;
        }
        return false;
    }

    // Method to get the controller of the current screen
    // This is to help pass along information between screens
    public void setScreenWithData(String name, Object data) {
        if (screens.containsKey(name)) {
            BaseController controller = getController(name);
            if (controller != null) {
                controller.receiveData(data);
            }
            setScreen(name);
        }
    }

    public String getCurrentScreen() {
        return currentScreen;
    }
}