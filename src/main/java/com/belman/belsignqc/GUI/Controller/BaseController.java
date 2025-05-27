package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.ScreenManager;

public abstract class BaseController {
    protected ScreenManager screenManager;

    public void setScreenManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Called when this controller's screen is activated.
     * Override this method in subclasses to perform actions when the screen becomes active.
     */
    public void onScreenActivated() {
        // Default implementation does nothing
    }
}
