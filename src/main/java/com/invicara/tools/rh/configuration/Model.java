/**
 * ***********************************************************************
 * INVICARA INC CONFIDENTIAL
 * ***********************************************************************
 *
 * Copyright (C) [2012] - [2014] INVICARA INC, INVICARA Pte Ltd, INVICARA INDIA
 * PVT LTD All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Invicara Inc and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to Invicara Inc and its suppliers
 * and may be covered by U.S. and Foreign Patents, patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Invicara Inc.
 */
package com.invicara.tools.rh.configuration;

import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.model.TestcaseNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum Model {

    INSTANCE;

    @Setter
    private String name;

    @Setter
    @Getter
    private BIMModel currentModel;

    @Setter
    @Getter
    private CategoryAxis xAxis;

    @Getter
    private final Set<XYChart.Series<String, Number>> listOfSeries;

    @Getter
    private final Set<BIMModel> listOfModel;

    @Getter
    private final Set<TestcaseNode> listOfSelectedRules;

    @Getter
    private final Map<String, BIMModel> modelMap = new HashMap<>();

    @Getter
    private final Map<String, Integer> modelVersionMap = new HashMap<>();

    private final Set<String> testcases = new TreeSet<>();
    private final ObservableList<String> obs = FXCollections.observableArrayList();

    Model() {
        listOfModel = new TreeSet<>((BIMModel o1, BIMModel o2) -> {
            if (o1.getName().equals(o2.getName())) {
                return o1.getVersion() > o2.getVersion() ? 1 : -1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });
        listOfSeries = new HashSet<>();
        listOfSelectedRules = new HashSet<>();
    }

    public void addValueToXAxis(String value) {
        testcases.add(value);
        obs.clear();
        obs.addAll(testcases);
        xAxis.setCategories(obs);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void addModel(String modelName, BIMModel model) {
        modelMap.put(modelName, model);
    }

    public void removeModel(String modelName) {
        modelMap.remove(modelName);
    }

    public BIMModel getModel(String modelName) {
        return modelMap.get(modelName);
    }
}
