package com.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class MainJmeApp extends SimpleApplication {

    public static void main(String[] args) {
        MainJmeApp app = new MainJmeApp();
        app.start(); // Запуск приложения
    }

    @Override
    public void simpleInitApp() {
        // Регистрация пути к ресурсам
        assetManager.registerLocator("src/main/resources/Models/", FileLocator.class);

        // Загрузка модели STL
        Spatial deckModel = assetManager.loadModel("deck.obj");
        deckModel.scale(0.1f); // Настройка масштаба модели
        rootNode.attachChild(deckModel); // Добавление модели в сцену

        // Добавление света
        DirectionalLight light = new DirectionalLight();
        light.setColor(ColorRGBA.White);
        light.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        rootNode.addLight(light);
    }
}

