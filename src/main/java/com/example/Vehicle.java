package com.example;

/**
 * Класс для описания автомобиля:
 *  - name   (название/модель)
 *  - width  (ширина)
 *  - length (длина)
 *
 * Площадь считаем как width * length
 */
public class Vehicle {
    private String name;
    private double width;
    private double length;

    public Vehicle(String name, double width, double length) {
        this.name = name;
        this.width = width;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public double getArea() {
        return width * length;
    }
}
