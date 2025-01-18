package com.example;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Пример простого приложения на jMonkeyEngine 3.2.4-stable,
 * которое загружает STL-файл "deck.stl" из папки resources/Models/
 * и показывает в 3D-окне.
 */
public class MainJmeApp extends SimpleApplication {

    public static void main(String[] args) {
        MainJmeApp app = new MainJmeApp();
        app.start(); // Запуск jME-приложения (окно)
    }

    @Override
    public void simpleInitApp() {
        // 1) Загрузим модель "deck.stl" из папки "src/main/resources/Models/"
        //    Убедитесь, что файл лежит в "/Models/deck.stl" (регистр важен).
        Spatial deckModel = assetManager.loadModel("Models/deck.stl");

        // 2) Добавляем модель в корень сцены
        rootNode.attachChild(deckModel);

        // 3) Добавим простой источник света (DirectionalLight),
        //    чтобы мы видели модель (иначе она будет чёрной).
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-1f, -2f, -2f).normalizeLocal());
        rootNode.addLight(sun);

        // 4) Настройка камеры (flyCam)
        //    Скорость движения камеры (WASD / мышь)
        flyCam.setMoveSpeed(10f);

        //    Поставим камеру повыше и подальше
        cam.setLocation(new Vector3f(0, 5, 15));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
    }
}
