package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.ScreenManager;

public abstract class BaseController {
    protected ScreenManager screenManager;

    public void setScreenManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    public void receiveData(Object data) {
        // This method can be overridden by subclasses to handle data passed from other controllers
        // Default implementation does nothing
    }
}