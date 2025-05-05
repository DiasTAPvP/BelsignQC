package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.ScreenManager;

public abstract class BaseController {
    protected ScreenManager screenManager;

    public void setScreenManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }
}