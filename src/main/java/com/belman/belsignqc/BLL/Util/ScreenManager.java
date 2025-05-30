package com.belman.belsignqc.BLL.Util;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.GUI.Controller.BaseController;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

//Singleton style class to manage screens
public class ScreenManager extends StackPane {

    // Maps to hold screens and their associated controllers
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

    /**
     * Sets the current screen to the specified screen name.
     * If the screen is already displayed, it will not be added again.
     *
     * @param name The name of the screen to set as current.
     */
    public void setScreen(String name) {
        // Get current user from session
        Users currentUser = UserSession.getInstance().getUser();
        System.out.println("User in ScreenManager when switching to " + name + ": " +
                (currentUser != null ? currentUser.getUserID() : "null"));

        if (screens.get(name) != null) {
            // Load the new controller
            BaseController controller = controllers.get(name);
            if (controller != null) {
                // Pass the user to the new controller
                controller.receiveUserData(currentUser);

                // Notify controller its screen is being activated
                controller.onScreenActivated();
            }

            // If there's a screen already being displayed, remove it
            if (currentScreen != null && !currentScreen.equals(name)) {
                getChildren().remove(screens.get(currentScreen));
            }

            // If the screen isn't already displayed, add it
            if (!getChildren().contains(screens.get(name))) {
                getChildren().add(screens.get(name));
            }

            currentScreen = name;
        }
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