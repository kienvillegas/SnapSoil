package com.example.snapsoil;


import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import java.time.format.TextStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class DataAggregator {
    public static Map<String, Double> averages = new HashMap<>();

    public static Map<Integer, Map<String, Double>> getAverageByYear(List<Nutrients> nutrientsList){
        Map<Integer, Map<String, Double>> averagesByYear = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            averagesByYear =  nutrientsList.stream()
                    .collect(Collectors.groupingBy(data -> data.getCreatedAt().getYear(),
                            Collectors.collectingAndThen(Collectors.toList(),
                                    nutrient -> {
                                        double avgN = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getNitrogen));
                                        double avgP = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPhosphorus));
                                        double avgK = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPotassium));
                                        double avgpH = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getpH));

                                        averages.put("nitrogen", avgN);
                                        averages.put("phosphorus", avgP);
                                        averages.put("potassium", avgK);
                                        averages.put("pH", avgpH);

                                        return averages;
                            })
                    ));
        }
        return averagesByYear;
    }

    public static Map<Integer, Map<String, Double>> getAverageByMonth(List<Nutrients> nutrientsList){
        Map<Integer, Map<String, Double>> averagesByMonth = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            averagesByMonth =  nutrientsList.stream()
                    .collect(Collectors.groupingBy(data -> data.getCreatedAt().getMonthValue(),
                            Collectors.collectingAndThen(Collectors.toList(),
                                    nutrient -> {
                                        double avgN = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getNitrogen));
                                        double avgP = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPhosphorus));
                                        double avgK = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPotassium));
                                        double avgpH = nutrient.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getpH));

                                        averages.put("nitrogen", avgN);
                                        averages.put("phosphorus", avgP);
                                        averages.put("potassium", avgK);
                                        averages.put("pH", avgpH);

                                        return averages;                                    })
                    ));
        }
        return averagesByMonth;
    }

    public static Map<Integer, Map<String, Double>> getAverageByWeek(List<Nutrients> nutrientsList) {
        TemporalField weekOfYear;
        Map<Integer, Map<String, Double>> averagesByWeek = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            weekOfYear = WeekFields.of(Locale.getDefault()).weekOfYear();

            averagesByWeek = nutrientsList.stream()
                    .collect(Collectors.groupingBy(data -> data.getCreatedAt().get(weekOfYear),
                            Collectors.collectingAndThen(Collectors.toList(),
                                    nutrientList -> {

                                        Map<String, Double> averages = new HashMap<>();
                                        double avgN = nutrientList.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getNitrogen));
                                        double avgP = nutrientList.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPhosphorus));
                                        double avgK = nutrientList.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getPotassium));
                                        double avgpH = nutrientList.stream()
                                                .collect(Collectors.averagingDouble(Nutrients::getpH));

                                        averages.put("nitrogen", avgN);
                                        averages.put("phosphorus", avgP);
                                        averages.put("potassium", avgK);
                                        averages.put("ph", avgpH);
                                        return averages;
                                    })
                    ));
        }
        return averagesByWeek;
    }

}
