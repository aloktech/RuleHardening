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
package com.invicara.tools.rh.ui.controller;

import com.invicara.tools.rh.configuration.Chart;
import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.model.ChartNodeData;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.ui.ChartDialogBox;
import com.invicara.tools.rh.utils.UIUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Alok Ranjan Meher
 */
public class MenuBarFXMLController implements Initializable {

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ControllerUtilities.INSTANCE.addController(MenuBarFXMLController.class, this);
    }

    @FXML
    public void open(ActionEvent event) {

    }

    @FXML
    public void save(ActionEvent event) {

    }

    @FXML
    public void saveAs(ActionEvent event) {

    }

    @FXML
    public void quit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void addModelToChart(ActionEvent event) {
        if (Model.INSTANCE.getName() == null) {
            Console.INSTANCE.setConsoleMessage("Project Id or Package Id is empty");
        } else {
            Model.INSTANCE.getListOfModel().add(Model.INSTANCE.getCurrentModel());
            Map<TestcaseNode, List<ChartNodeData>> testcasesMap = Chart.INSTANCE.getTestcasesMap();
            TestcaseNode key;
            int count = Model.INSTANCE.getListOfModel().size();
            final ObservableList<XYChart.Data<String, Number>> dataset = FXCollections.observableArrayList();
            for (TestcaseNode rule : Model.INSTANCE.getCurrentModel().getNode().getChildren()) {
                for (TestcaseNode testcase : rule.getChildren()) {
                    List<ChartNodeData> dataList;
                    key = new TestcaseNode();
                    key.setRuleName(rule.getRuleName());
                    key.setTestcaseName(testcase.getTestcaseName());
                    if (testcasesMap.containsKey(key)) {
                        dataList = testcasesMap.get(key);
                    } else {
                        ChartNodeData[] nodeData = new ChartNodeData[10];
                        Arrays.fill(nodeData, new ChartNodeData());
                        dataList = Arrays.asList(nodeData);
                    }
                    ChartNodeData data = new ChartNodeData(new TestcaseNode());
                    data.getNode().setRuleName(rule.getRuleName());
                    data.getNode().setTestcaseName(testcase.getTestcaseName());
                    data.getNode().setWorkflowRunTime(testcase.getWorkflowRunTime());
                    data.getNode().setModelName(testcase.getModelName());
                    data.getNode().setExpectedOutput(testcase.getExpectedOutput());
                    dataList.set(count - 1, data);
                    testcasesMap.put(key, dataList);
                }
            }
            Console.INSTANCE.setConsoleMessage(Model.INSTANCE.getCurrentModel() + " is loaded to Chart");
        }
    }

    @FXML
    public void clearAllModel(ActionEvent event) {
        Chart.INSTANCE.getTestcasesMap().clear();
        Model.INSTANCE.getListOfModel().clear();
        Model.INSTANCE.getModelMap().clear();
        Model.INSTANCE.getListOfSeries().clear();

        Console.INSTANCE.setConsoleMessage("All Chart data are cleared.");
    }

    @FXML
    public void editModel(ActionEvent event) {
        Platform.runLater(() -> {
            ChartDialogBox chartDialogBox = new ChartDialogBox();
            chartDialogBox.createGUI();
            chartDialogBox.showDialog();
        });
    }

    @FXML
    public void showChart(ActionEvent event) {
        if (Model.INSTANCE.getListOfModel().isEmpty()) {
            UIUtils.showWarningWindow("Model list is empty");
            return;
        }

        UIUtils.refreshUILater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LineChartFXML.fxml"));
                VBox basePanel = (VBox) fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setTitle("LineChart");
                stage.setScene(new Scene(basePanel, 1800, 1000));
                stage.show();
            } catch (IOException ex) {
                UIUtils.showWarningWindow("Failed to load LineChart");
            }
        });
    }

    @FXML
    public void configuration(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ConfigurationWindowFXML.fxml"));
                VBox basePanel = (VBox) fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setTitle("Configuration");
                stage.setScene(new Scene(basePanel));
                stage.show();
            } catch (IOException e) {
                UIUtils.showWarningWindow("Failed to load ConfigurationWindow");
            }
        });
    }

    @FXML
    public void updateRules(ActionEvent event) {

    }

    @FXML
    public void addSelectedRulesToChart(ActionEvent event) {
        if (Model.INSTANCE.getName() == null) {
            Console.INSTANCE.setConsoleMessage("Project Id or Package Id is empty");
        } else {
            Model.INSTANCE.getListOfModel().add(Model.INSTANCE.getCurrentModel());
            Map<TestcaseNode, List<ChartNodeData>> testcasesMap = Chart.INSTANCE.getTestcasesMap();
            TestcaseNode key;
            int count = Model.INSTANCE.getListOfModel().size();
            final ObservableList<XYChart.Data<String, Number>> dataset = FXCollections.observableArrayList();
            for (TestcaseNode rule : Model.INSTANCE.getCurrentModel().getNode().getChildren()) {
                if (Model.INSTANCE.getListOfSelectedRules().contains(rule)) {
                    for (TestcaseNode testcase : rule.getChildren()) {
                        List<ChartNodeData> dataList;
                        key = new TestcaseNode();
                        key.setRuleName(rule.getRuleName());
                        key.setTestcaseName(testcase.getTestcaseName());
                        if (testcasesMap.containsKey(key)) {
                            dataList = testcasesMap.get(key);
                        } else {
                            ChartNodeData[] nodeData = new ChartNodeData[10];
                            Arrays.fill(nodeData, new ChartNodeData());
                            dataList = Arrays.asList(nodeData);
                        }
                        ChartNodeData data = new ChartNodeData(new TestcaseNode());
                        data.getNode().setRuleName(rule.getRuleName());
                        data.getNode().setTestcaseName(testcase.getTestcaseName());
                        data.getNode().setWorkflowRunTime(testcase.getWorkflowRunTime());
                        data.getNode().setModelName(testcase.getModelName());
                        data.getNode().setExpectedOutput(testcase.getExpectedOutput());
                        dataList.set(count - 1, data);
                        testcasesMap.put(key, dataList);
                    }
                }
            }
            Console.INSTANCE.setConsoleMessage(Model.INSTANCE.getCurrentModel() + " is loaded to Chart");
        }
    }

    @FXML
    public void about(ActionEvent event) {

    }
}
