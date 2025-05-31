package com.belman.belsignqc.BLL.Exceptions;

/**
 * Exception thrown when no camera is found.
 */
public class CameraNotFound extends Exception {
    public CameraNotFound() {
        super("No camera found");
    }
}
