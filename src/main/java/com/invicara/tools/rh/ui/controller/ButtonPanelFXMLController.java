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

import com.invicara.tools.rh.ui.service.SelectedRulesExecution;
import com.invicara.tools.rh.ui.service.AllRulesExecution;
import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.FileFormat;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.ui.service.Execution;
import com.invicara.tools.rh.utils.Status;
import com.invicara.tools.rh.utils.UIComponentConfig;
import com.invicara.tools.rh.utils.UIMessageConfig;
import com.invicara.tools.rh.service.DatabaseManagement;
import com.invicara.tools.rh.service.AllRulesExcelJSONManagement;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.service.ExcelJSONManagement;
import com.invicara.tools.rh.service.MultipleConditionExcelJSONManagement;
import com.invicara.tools.rh.utils.IOManagement;
import com.invicara.tools.rh.utils.RulesHardeningHelper;
import com.invicara.tools.rh.utils.UIUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import org.aeonbits.owner.ConfigFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author Alok Ranjan Meher
 */
public class ButtonPanelFXMLController extends MasterController implements Initializable {

    @FXML
    Button executeAllRules, executeSelected, exportAll, exportSelected, executionStop, reset, generateReport;

    private TreeView<TestcaseNode> treeView;
    private CheckBoxTreeItem<TestcaseNode> rootNode;
    private DatabaseManagement dbMgmt;
    private TreeViewFXMLController treeViewController;
    private UIComponentConfig compConfig;
    private UIMessageConfig msgConfig;
    private boolean multipleProperties;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        exportAll.setGraphic(new ImageView(new Image("/images/excel.png")));
        exportSelected.setGraphic(new ImageView(new Image("/images/excel.png")));

        compConfig = ConfigFactory.create(UIComponentConfig.class);
        executeAllRules.setText(compConfig.executeAllRules());

        msgConfig = ConfigFactory.create(UIMessageConfig.class);

        ControllerUtilities.INSTANCE.addController(ButtonPanelFXMLController.class, this);

        treeViewController = (TreeViewFXMLController) ControllerUtilities.INSTANCE.getController(TreeViewFXMLController.class);
    }

    @FXML
    public void executeAllAction(ActionEvent event) {
        if (!UIUtils.validateConfiguration()) {
            return;
        }
        initializeExecution();
        checkForNewVersion();

        Status.RUNNING.setActive(true);
        buttonDisable();

        new Thread(() -> {

            Execution execution = new AllRulesExecution(treeView);
            execute(this, execution, rootNode);
        }).start();
    }

    private void checkForNewVersion() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmation Message");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setContentText("Would you like to create a new version for this model");
        alert.getDialogPane().setHeaderText("Confirmation Message");

        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            TestcaseNode oldRoot = treeViewController.getTreeRootNode().getValue();

            TestcaseNode newRoot = new TestcaseNode();
            newRoot.setModelName(oldRoot.getModelName());
            for (TestcaseNode rule : oldRoot.getChildren()) {
                TestcaseNode newRule = new TestcaseNode();
                newRule.setModelName(rule.getModelName());
                newRule.setRuleName(rule.getRuleName());
                newRule.setDefaultActualArgument(rule.getDefaultActualArgument());
                newRule.setNodeType(rule.getNodeType());
                newRule.setSchema(rule.getSchema());
                newRule.setParentRule(newRoot);
                newRoot.getChildren().add(newRule);

                rule.getChildren().stream().map((testcase) -> {
                    TestcaseNode newTestcase = new TestcaseNode();
                    newTestcase.setModelName(testcase.getModelName());
                    newTestcase.setActualArgument(testcase.getActualArgument());
                    newTestcase.setRuleName(testcase.getRuleName());
                    newTestcase.setTestcaseName(testcase.getTestcaseName());
                    newTestcase.setNodeType(testcase.getNodeType());
                    newTestcase.setExpectedOutput(testcase.getExpectedOutput());
                    newTestcase.setWorkflowRunTime(testcase.getWorkflowRunTime());
                    newTestcase.setRuleExecutionTime(testcase.getRuleExecutionTime());
                    newTestcase.setRuntime(testcase.getRuntime());
                    newTestcase.setParentRule(newRule);
                    return newTestcase;
                }).forEach((newTestcase) -> {
                    newRule.getChildren().add(newTestcase);
                });
            }
            treeView.getRoot().setValue(newRoot);
            BIMModel model = new BIMModel(newRoot);
            Model.INSTANCE.setCurrentModel(model);
            Model.INSTANCE.addModel(newRoot.getModelName(), model);
            treeViewController.populateTreeFromRootNode(newRoot);

            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            sceneController.getModelList().getItems().add(model);
            sceneController.getModelList().getSelectionModel().select(model);
            UIUtils.showInfoWindow(msgConfig.modelLoaded(oldRoot.getModelName()));
        }
    }

    @FXML
    public void executeSelectedAction(ActionEvent event) {
        if (!UIUtils.validateConfiguration()) {
            return;
        }
        Status.RUNNING.setActive(true);
        Status.SELECTED_RULES.setActive(true);
        buttonDisable();
        initializeExecution();
        new Thread(() -> {
            Execution execution = new SelectedRulesExecution();
            execute(this, execution, rootNode);
        }).start();
    }

    @FXML
    public void exportAllAction(ActionEvent event) {
        final String time = RulesHardeningHelper.getTimeAsName();
        if (!UIUtils.validateConfiguration()) {
            return;
        }
        initializeExecution();
        if (Model.INSTANCE.getName() == null) {
            setModelName();
        }
        String fileName = "TestCase_" + Model.INSTANCE.getCurrentModel().getName()
                + "_" + Model.INSTANCE.getCurrentModel().getVersion() + "_" + time + ".xlsx";
        String filePath = FileFormat.EXCEL.getFilePath() + File.separator + fileName;
        try {
            exportToExcel(filePath, fileName, false);

//            UIUtils.showInfoWindow("Excel file is saved at : \n" + filePath);
//            UIUtils.updateConsole("Excel file is saved at : \n" + filePath);
            UIUtils.updateConsole(msgConfig.fileSaved(filePath));
            UIUtils.showInfoWindow(msgConfig.fileSaved(filePath));
        } catch (JSONException | IOException | InvalidFormatException ex) {
            UIUtils.updateConsole(msgConfig.exportFailed());
        }
    }

    @FXML
    public void exportSelectedAction(ActionEvent event) {
        final String time = RulesHardeningHelper.getTimeAsName();
        if (!UIUtils.validateConfiguration()) {
            return;
        }
        initializeExecution();

        if (Model.INSTANCE.getName() == null) {
            setModelName();
        }

        String fileName = "TestCase_" + Model.INSTANCE.getCurrentModel().getName()
                + "_" + Model.INSTANCE.getCurrentModel().getVersion() + "_" + time + ".xlsx";
        String filePath = FileFormat.EXCEL.getFilePath() + File.separator + fileName;
        try {
            exportToExcel(filePath, fileName, true);
        } catch (JSONException | IOException | InvalidFormatException ex) {
            UIUtils.updateConsole("export from Excel failed");
        }

        UIUtils.showInfoWindow("Excel file is saved at : \n" + filePath);
        UIUtils.updateConsole("Excel file is saved at : \n" + filePath);
    }

    @FXML
    public void executionStopAction(ActionEvent event) {
        buttonEnable();
        buttonReset();
    }

    @FXML
    public void resetAction(ActionEvent event) {
        executeAllRules.setDisable(false);
        executeSelected.setDisable(false);

        initializeExecution();
        rootNode.getChildren().stream().map((parent) -> (CheckBoxTreeItem<TestcaseNode>) parent).map((p) -> {
            p.setSelected(false);
            return p;
        }).map((p) -> {
            p.setExpanded(false);
            p.setGraphic(null);
            return p;
        }).forEach((p) -> {
            p.getChildren().stream().map((child) -> (CheckBoxTreeItem<TestcaseNode>) child).map((c) -> {
                c.setSelected(false);
                c.setGraphic(null);
                return c;
            }).forEach((c) -> {
                c.setExpanded(false);
            });
        });
    }

    @FXML
    public void clearAction(ActionEvent event) {
        buttonEnable();
        buttonReset();

        initializeExecution();
        Platform.runLater(rootNode.getChildren()::clear);
        BIMModel model = Model.INSTANCE.getCurrentModel();
        SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
        sceneController.getModelList().getItems().remove(model);
        sceneController.getModelList().getSelectionModel().clearSelection();
        sceneController.getModelVersion().setText("");
        Model.INSTANCE.setCurrentModel(null);

        Model.INSTANCE.getModelVersionMap().remove(model.getName());

        Console.INSTANCE.setConsoleMessage("Current Model data is cleared.");
    }

    @FXML
    public void clearAllAction(ActionEvent event) {
        buttonEnable();
        buttonReset();

        Model.INSTANCE.getListOfModel().clear();
        Model.INSTANCE.getModelMap().clear();
        Model.INSTANCE.getListOfSeries().clear();

        initializeExecution();
        Platform.runLater(rootNode.getChildren()::clear);

        SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
        sceneController.getModelList().getItems().clear();
        sceneController.getModelList().getSelectionModel().select(-1);
        sceneController.getModelList().getSelectionModel().clearSelection();
        sceneController.getModelVersion().setText("");

        Model.INSTANCE.getModelVersionMap().clear();

        Console.INSTANCE.setConsoleMessage("All Model data are cleared.");
    }

    @FXML
    public void generateReportAction(ActionEvent event) {
        JSONArray report = new JSONArray();
        if (!UIUtils.validateConfiguration()) {
            return;
        }
        initializeExecution();
        final String time = RulesHardeningHelper.getTimeAsName();
        rootNode.getChildren().stream().map((parent) -> (CheckBoxTreeItem<TestcaseNode>) parent).forEach((p) -> {
            p.getChildren().stream().map((child) -> (CheckBoxTreeItem<TestcaseNode>) child).map((c) -> (TestcaseNode) c.getValue()).forEach((n) -> {
                report.put(n.generateReport());
            });
        });
        if (Model.INSTANCE.getName() == null) {
            setModelName();
        }
        final String name = Model.INSTANCE.getName() + "_" + time;
        UIUtils.updateConsole("Report " + name + "json file is saved at location : " + FileFormat.JSON.getFilePath());
        IOManagement.saveToFile(FileFormat.JSON.getFilePath() + File.separator + "Report_" + name + ".json", report.toString());
        UIUtils.showInfoWindow("Report json file is saved at : \n" + FileFormat.JSON.getFilePath() + "\\Report_"
                + name + ".json");

    }

    public void exportToExcel(String excelFilePath, String excelFileName, boolean selected) throws JSONException, IOException, InvalidFormatException {

        try {
            ExcelJSONManagement mgmt = null;
            Workbook workbook = null;
            boolean basicConfiguration = false;

            int ruleCount = 0, rowCount = 0;
            for (TreeItem<TestcaseNode> ruleNode : rootNode.getChildren()) {
                CheckBoxTreeItem<TestcaseNode> rule = (CheckBoxTreeItem<TestcaseNode>) ruleNode;
                if (rule.isSelected() || rule.isIndeterminate()) {
                    ruleCount++;
                }
                for (TreeItem<TestcaseNode> testcaseNode : rule.getChildren()) {
                    CheckBoxTreeItem<TestcaseNode> testcase = (CheckBoxTreeItem<TestcaseNode>) testcaseNode;
                    TestcaseNode node = (TestcaseNode) testcase.getValue();
                    if (!basicConfiguration) {
                        if (node.getRuleName().equals("CheckMultipleProperties")) {
                            mgmt = new MultipleConditionExcelJSONManagement();
                            workbook = mgmt.openExcelFile(excelFilePath);
                            mgmt.basicConfiguration(workbook);
                        } else {
                            mgmt = new AllRulesExcelJSONManagement();
                            workbook = mgmt.openExcelFile(excelFilePath);
                            mgmt.basicConfiguration(workbook);
                        }
                        basicConfiguration = true;
                    }
                    if (selected) {
                        if (testcase.isSelected()) {
                            node.setModelName(Model.INSTANCE.getName());
                            mgmt.exportToExcel(workbook, rowCount, node.generateTestcaseResult());
                            rowCount++;
                        }
                    } else {
                        node.setModelName(Model.INSTANCE.getName());
                        mgmt.exportToExcel(workbook, rowCount, node.generateTestcaseResult());
                        rowCount++;
                    }
                }
            }

            UIUtils.updateConsole("Rule Count : " + ruleCount);
            UIUtils.updateConsole("Row Count : " + rowCount);
            mgmt.saveAsNewExcelFile(excelFilePath, workbook);
        } catch (FileNotFoundException e) {
            UIUtils.updateConsole("Falied to save at location : " + FileFormat.EXCEL.getFilePath());
            UIUtils.updateConsole(msgConfig.failedToSave(excelFilePath));
        }

    }

    private void initializeExecution() {
        if (treeView == null) {
            treeViewController = (TreeViewFXMLController) ControllerUtilities.INSTANCE.getController(TreeViewFXMLController.class);
            treeView = treeViewController.getTreeView();
        }
        rootNode = (CheckBoxTreeItem<TestcaseNode>) treeView.getRoot();
    }

    public void setModelName() {
        dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
        if (UIUtils.validateConfiguration()) {
            String name = dbMgmt.getModelName(DatabaseData.PACKAGE_MASTER.getId(), DatabaseData.PROJECT.getId());
            if (name.endsWith(".bimpk")) {
                name = name.substring(0, name.lastIndexOf(".bimpk"));
            }
            Model.INSTANCE.setName(name);
        }
    }

    public void buttonReset() {
        Status.ALL_RULES_EXECUTION.setActive(false);
        Status.SKIP_RULE_EXECUTION.setActive(false);
        Status.SKIP_TESTCASE_EXECUTION.setActive(false);
        Status.RUNNING.setActive(false);
        Status.SELECTED_RULES.setActive(false);
    }

    public void buttonDisable() {
        Platform.runLater(() -> {
            executeAllRules.setDisable(true);
            executeSelected.setDisable(true);
            reset.setDisable(true);
        });
    }

    public void buttonEnable() {
        Platform.runLater(() -> {
            executeAllRules.setDisable(false);
            executeSelected.setDisable(false);
            reset.setDisable(false);
        });
    }

    public TreeView<TestcaseNode> getTreeView() {
        return treeView;
    }

    public void setTreeView(TreeView<TestcaseNode> treeView) {
        this.treeView = treeView;
    }
}
