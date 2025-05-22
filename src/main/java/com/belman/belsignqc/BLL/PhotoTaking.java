package com.belman.belsignqc.BLL;

import javafx.scene.image.Image;

public interface PhotoTaking {

    void start() throws Exception;

    void stop() throws Exception;

    Image takePhoto() throws Exception;
}