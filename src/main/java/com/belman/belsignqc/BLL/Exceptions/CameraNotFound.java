package com.belman.belsignqc.BLL.Exceptions;

public class CameraNotFound extends Exception {
    public CameraNotFound() {
        super("No camera found");
    }
}
