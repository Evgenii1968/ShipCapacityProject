package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Распределяем машины на 4 группы по округлённой площади:
 *   KEY:    round < 6
 *   SMALL:  round < 7  (иначе говоря, именно 6)
 *   MEDIUM: round < 10 (значит 7, 8, 9)
 *   LARGE:  всё остальное (>= 10)
 *
 * Линейно раскладываем группы (KEY -> SMALL -> MEDIUM -> LARGE) по палубам.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1) Читаем все авто из Excel
        String excelPath = "C:/Users/Evgenii/eclipse-workspace/ship-capacity-calculator11/vehicles.xlsx";
        List<Vehicle> vehicles = readVehiclesFromExcel(excelPath);

        if (vehicles.isEmpty()) {
            System.out.println("В Excel-файле нет валидных записей или файл не найден.");
            scanner.close();
            return;
        }

        // 2) Группируем:
        //    KEY:    <6
        //    SMALL:  <7  (то есть именно 6)
        //    MEDIUM: <10 (7..9)
        //    LARGE:  >=10
        List<Vehicle> keyList    = new ArrayList<>();
        List<Vehicle> smallList  = new ArrayList<>();
        List<Vehicle> mediumList = new ArrayList<>();
        List<Vehicle> largeList  = new ArrayList<>();

        for (Vehicle v : vehicles) {
            double area = v.getArea();
            long rounded = Math.round(area);

            if (rounded < 6) {
                keyList.add(v);
            } else if (rounded < 7) {
                // значит rounded=6
                smallList.add(v);
            } else if (rounded < 10) {
                // значит 7,8,9
                mediumList.add(v);
            } else {
                // 10, 11, ...
                largeList.add(v);
            }
        }

        System.out.println("\nВсего машин: " + vehicles.size());
        System.out.println("KEY (round<6):   " + keyList.size());
        System.out.println("SMALL (round=6): " + smallList.size());
        System.out.println("MEDIUM (7..9):   " + mediumList.size());
        System.out.println("LARGE (>=10):    " + largeList.size());

        // 3) Спросим про палубы
        System.out.print("\nСколько палуб у судна? ");
        int deckCount = scanner.nextInt();

        double[] deckCapacities = new double[deckCount];
        double sumDeckArea = 0.0;

        for (int i = 0; i < deckCount; i++) {
            System.out.printf("Введите площадь палубы №%d: ", (i+1));
            double area = scanner.nextDouble();
            deckCapacities[i] = area;
            sumDeckArea += area;
        }

        // Для фиксации результатов: countPlaced[deckIndex][groupIndex]
        //  groupIndex: 0=KEY, 1=SMALL, 2=MEDIUM, 3=LARGE
        int[][] countPlaced = new int[deckCount][4];

        // 4) Линейное раскладывание
        int notFittedKey    = placeVehiclesGroup(keyList,    deckCapacities, countPlaced, 0);
        int notFittedSmall  = placeVehiclesGroup(smallList,  deckCapacities, countPlaced, 1);
        int notFittedMedium = placeVehiclesGroup(mediumList, deckCapacities, countPlaced, 2);
        int notFittedLarge  = placeVehiclesGroup(largeList,  deckCapacities, countPlaced, 3);

        // 5) Общая площадь всех машин
        double totalVehiclesArea = 0.0;
        for (Vehicle v : vehicles) {
            totalVehiclesArea += v.getArea();
        }

        // 6) Вывод
        System.out.println("\n=== РЕЗУЛЬТАТ РАСПРЕДЕЛЕНИЯ ===");
        for (int i = 0; i < deckCount; i++) {
            System.out.printf(
                "Палуба #%d: KEY=%d, SMALL=%d, MEDIUM=%d, LARGE=%d, остаток площади=%.2f\n",
                (i + 1),
                countPlaced[i][0],
                countPlaced[i][1],
                countPlaced[i][2],
                countPlaced[i][3],
                deckCapacities[i]
            );
        }

        System.out.println("\n=== ИТОГО ПО ГРУППАМ ===");
        System.out.printf("KEY:    всего %d, не влезли %d => влезли %d\n",
                keyList.size(), notFittedKey, (keyList.size() - notFittedKey));
        System.out.printf("SMALL:  всего %d, не влезли %d => влезли %d\n",
                smallList.size(), notFittedSmall, (smallList.size() - notFittedSmall));
        System.out.printf("MEDIUM: всего %d, не влезли %d => влезли %d\n",
                mediumList.size(), notFittedMedium, (mediumList.size() - notFittedMedium));
        System.out.printf("LARGE:  всего %d, не влезли %d => влезли %d\n",
                largeList.size(), notFittedLarge, (largeList.size() - notFittedLarge));

        System.out.println("\n=== ОБЩАЯ СВОДКА ===");
        System.out.printf("Суммарная площадь палуб: %.2f\n", sumDeckArea);
        System.out.printf("Суммарная площадь ВСЕХ машин: %.2f\n", totalVehiclesArea);

        int totalNotFitted = notFittedKey + notFittedSmall + notFittedMedium + notFittedLarge;
        int totalFitted = vehicles.size() - totalNotFitted;
        System.out.printf("Влезло машин: %d из %d\n", totalFitted, vehicles.size());

        if (totalVehiclesArea <= sumDeckArea) {
            System.out.println("По сумме площадей все машины могли бы уместиться (теоретически).");
        } else {
            double diff = totalVehiclesArea - sumDeckArea;
            double ratio = (sumDeckArea / totalVehiclesArea) * 100.0;
            System.out.printf("Дефицит площади: %.2f, можно уложить ~%.1f%%\n", diff, ratio);
        }

        scanner.close();
    }

    /**
     * Линейно кладём машины одной группы (groupList) на палубы deckCapacities.
     * Если машина не влезла ни на одну палубу, увеличиваем notFitted.
     * groupIndex: 0=KEY, 1=SMALL, 2=MEDIUM, 3=LARGE
     */
    private static int placeVehiclesGroup(List<Vehicle> groupList,
                                          double[] deckCapacities,
                                          int[][] countPlaced,
                                          int groupIndex) {
        int notFitted = 0;

        for (Vehicle v : groupList) {
            double area = v.getArea();
            boolean placed = false;

            // Ищем первую палубу, где ещё хватает площади
            for (int i = 0; i < deckCapacities.length; i++) {
                if (deckCapacities[i] >= area) {
                    deckCapacities[i] -= area;
                    countPlaced[i][groupIndex]++;
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                notFitted++;
            }
        }
        return notFitted;
    }

    /**
     * Читаем Excel (первый лист), начиная со 2-й строки (row=1):
     *  - A(0): Название
     *  - E(4): ширина
     *  - F(5): длина
     */
    private static List<Vehicle> readVehiclesFromExcel(String excelPath) {
        List<Vehicle> list = new ArrayList<>();
        File file = new File(excelPath);

        if (!file.exists()) {
            System.out.println("Файл Excel не найден: " + excelPath);
            return list;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                Cell cellName   = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell cellWidth  = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell cellLength = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (cellName == null || cellWidth == null || cellLength == null) {
                    continue;
                }

                String name  = cellName.getStringCellValue();
                double width = cellWidth.getNumericCellValue();
                double length= cellLength.getNumericCellValue();

                list.add(new Vehicle(name, width, length));
            }
        } catch (Exception e) {
            System.out.println("Ошибка при чтении Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
