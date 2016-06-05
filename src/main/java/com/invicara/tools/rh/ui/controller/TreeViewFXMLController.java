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

import com.invicara.tools.rh.ui.service.RuleExecution;
import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.FileFormat;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.configuration.RuleInfo;
import com.invicara.tools.rh.ui.service.Execution;
import com.invicara.tools.rh.utils.JSONConstants;
import static com.invicara.tools.rh.utils.JSONConstants.ACTUAL_ARGUMENT;
import static com.invicara.tools.rh.utils.JSONConstants.EXPECTED_OUTPUT;
import static com.invicara.tools.rh.utils.JSONConstants.MODEL_NAME;
import static com.invicara.tools.rh.utils.JSONConstants.RULE_RUNTIME;
import static com.invicara.tools.rh.utils.JSONConstants.WORKFLOW_RUNTIME;
import static com.invicara.tools.rh.utils.JSONConstants.RUNTIME;
import com.invicara.tools.rh.utils.NodeType;
import com.invicara.tools.rh.utils.Status;
import com.invicara.tools.rh.utils.UIMessageConfig;
import com.invicara.tools.rh.service.DatabaseManagement;
import com.invicara.tools.rh.service.AllRulesExcelJSONManagement;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.ui.InputDialogBox;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.ui.MultiConditionsDialogBox;
import com.invicara.tools.rh.ui.service.TestcaseExecution;
import com.invicara.tools.rh.utils.IOManagement;
import com.invicara.tools.rh.utils.UIUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import lombok.Getter;
import org.aeonbits.owner.ConfigFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class TreeViewFXMLController extends MasterController implements Initializable {

    private TestcaseNode root, rule, testcase;
    private DatabaseManagement dbMgmt;
    private String ruleName = "";
    private final ContextMenu contextMenu = new ContextMenu();
    private ButtonPanelFXMLController btnController;

    @FXML
    @Getter
    TreeView<TestcaseNode> treeView;

    @FXML
    @Getter
    CheckBoxTreeItem<TestcaseNode> treeRootNode;

    private UIMessageConfig msgConfig;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbMgmt = new DatabaseManagement();

        treeRootNode.getChildren().sort((TreeItem<TestcaseNode> o1, TreeItem<TestcaseNode> o2)
                -> o1.getValue().getRuleName().compareTo(o2.getValue().getRuleName()));

        treeView.setCellFactory(CheckBoxTreeCell.<TestcaseNode>forTreeView());

        ControllerUtilities.INSTANCE.addController(TreeViewFXMLController.class, this);

        msgConfig = ConfigFactory.create(UIMessageConfig.class);

        treeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<TestcaseNode>> observable,
                TreeItem<TestcaseNode> oldValue, TreeItem<TestcaseNode> newValue) -> {
                    CheckBoxTreeItem<TestcaseNode> item = (CheckBoxTreeItem<TestcaseNode>) observable.getValue();
                    if (item != null) {
                        if (item.isSelected()) {
                            TestcaseNode node = item.getValue();
                            if (!Model.INSTANCE.getListOfSelectedRules().contains(node)) {
                                Model.INSTANCE.getListOfSelectedRules().add(node);
                            }
                        } else {
                            TestcaseNode node = item.getValue();
                            if (Model.INSTANCE.getListOfSelectedRules().contains(node)) {
                                Model.INSTANCE.getListOfSelectedRules().remove(node);
                            }
                        }
                    }
                });

    }

    @FXML
    public void populateTreeOnDropFile(DragEvent event) {
        File file = null;
        Dragboard db = event.getDragboard();
        boolean status = false;
        String filePath = null;
        if (db.hasFiles()) {
            status = true;
            for (File files : db.getFiles()) {
                file = files;
                filePath = files.getAbsolutePath();
            }
            if (file != null && file.getAbsolutePath().endsWith(".json")) {
                FileFormat.JSON.setFileName(file.getName());
                final String data = IOManagement.openExternalFile(file.getAbsolutePath());
                Platform.runLater(() -> {
                    populateTreeFromData(data);
                });
            } else if (file != null && (file.getAbsolutePath().endsWith(".xlsx") || file.getAbsolutePath().endsWith(".xls"))) {
                FileFormat.EXCEL.setFileName(file.getName());
                final String tempFilePath = filePath;
                Platform.runLater(() -> {
                    try {
                        excelToTree(tempFilePath);
                    } catch (JSONException | IOException | InvalidFormatException ex) {
                        UIUtils.updateConsole(msgConfig.failedToLoad(tempFilePath));
                    }
                });

            }
        } else if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.setDropCompleted(status);
        event.consume();
    }

    @FXML
    public void createContextualMenu(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && treeView.getSelectionModel() != null) {
            if (Status.RUNNING.isActive()) {
                UIUtils.showWarningWindow("Wait for few moments, Rules execution is on progress");
                return;
            }
            contextMenu.getItems().clear();
            CheckBoxTreeItem<TestcaseNode> node = (CheckBoxTreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            TestcaseNode n = (TestcaseNode) treeView.getSelectionModel().getSelectedItem().getValue();
            if (n.getNodeType() == NodeType.RULE) {
                addRuleContextMenu(node, treeView);
            } else if (n.getNodeType() == NodeType.TESTCASE) {
                addTestcaseContextMenu(node, treeView);
            }
            treeView.setContextMenu(contextMenu);
        }

        if (event.getClickCount() == 2) {
            if (Status.RUNNING.isActive()) {
                UIUtils.showWarningWindow("Wait for few moments, Rules are executing");
                return;
            }
            TestcaseNode node = (TestcaseNode) treeView.getSelectionModel().getSelectedItem().getValue();
            if (node != null && node.getNodeType() == NodeType.TESTCASE) {
                InputDialogBox inputDialogBox = new InputDialogBox(treeView, (CheckBoxTreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem());
                inputDialogBox.showDialog();
            }
        }
    }

    private void addTestcaseContextMenu(final CheckBoxTreeItem<TestcaseNode> node, final TreeView<TestcaseNode> treeView) {
        MenuItem executeTestcase = new MenuItem("Execute the Testcase");
        executeTestcase.setOnAction((ActionEvent event) -> {
            if (!UIUtils.validateConfiguration()) {
                return;
            }
            btnController = (ButtonPanelFXMLController) ControllerUtilities.INSTANCE.getController(ButtonPanelFXMLController.class);
            btnController.buttonDisable();
            Status.RUNNING.setActive(true);
            UIUtils.updateConsole("Execution of Testcase " + node.getValue().getTestcaseName() + " started.");
            new Thread(() -> {
                Execution execution = new TestcaseExecution();
                execute(btnController, execution, node);
            }).start();
        });
        contextMenu.getItems().add(executeTestcase);

        MenuItem addNewTestcase = new MenuItem("Add New Testcase");
        addNewTestcase.setOnAction((ActionEvent event) -> {
            CheckBoxTreeItem<TestcaseNode> selectedItem = (CheckBoxTreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            TreeItem<TestcaseNode> parentTreeNode = selectedItem.getParent();
            CheckBoxTreeItem<TestcaseNode> parentNode = (CheckBoxTreeItem<TestcaseNode>) parentTreeNode;

            rule = (TestcaseNode) parentNode.getValue();

            TestcaseNode newTestcase = new TestcaseNode();
            newTestcase.setTestcaseName("New Testcase");
            newTestcase.setRuleName(rule.getRuleName());
            newTestcase.setActualArgument(rule.getDefaultActualArgument());
            newTestcase.setNodeType(NodeType.TESTCASE);
            newTestcase.setParentRule(rule);

            rule.getChildren().add(newTestcase);

            CheckBoxTreeItem<TestcaseNode> newTestcaseNode = new CheckBoxTreeItem<>(newTestcase);
            parentNode.getChildren().add(newTestcaseNode);

            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            BIMModel model = sceneController.getModelList().getSelectionModel().getSelectedItem();
            model.setNode(rule.getParentRule());

            UIUtils.updateConsole("New Testcases is added");
        });
        contextMenu.getItems().add(addNewTestcase);

        MenuItem deleteTheTestcase = new MenuItem("Delete the Testcase");
        deleteTheTestcase.setOnAction((ActionEvent event) -> {
            CheckBoxTreeItem<TestcaseNode> selectedItem = (CheckBoxTreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            TreeItem<TestcaseNode> parent = selectedItem.getParent();
            parent.getChildren().remove(selectedItem);
            UIUtils.updateConsole("Testcase " + selectedItem.getValue().getTestcaseName() + " is deleted");
        });
        contextMenu.getItems().add(deleteTheTestcase);
    }

    private void addRuleContextMenu(final CheckBoxTreeItem<TestcaseNode> node, final TreeView<TestcaseNode> treeView) {
        MenuItem executeAllTestcase = new MenuItem("Execute All Testcases");
        executeAllTestcase.setOnAction((ActionEvent event) -> {
            try {
                if (!UIUtils.validateConfiguration()) {
                    return;
                }
                UIUtils.updateConsole("All Testcases of rule " + node.getValue().getRuleName() + " execution started.");
                Status.RUNNING.setActive(true);
                btnController = (ButtonPanelFXMLController) ControllerUtilities.INSTANCE.getController(ButtonPanelFXMLController.class);
                btnController.buttonDisable();
                new Thread(() -> {
                    Execution execution = new RuleExecution(treeView);
                    execute(btnController, execution, node);
                }).start();
            } catch (Exception ex) {
                Logger.getLogger(TreeViewFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        contextMenu.getItems().add(executeAllTestcase);

        MenuItem addNewRule = new MenuItem("Add New Rule");
        addNewRule.setOnAction((ActionEvent event) -> {
            TreeItem<TestcaseNode> selectedItem = (TreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            CheckBoxTreeItem<TestcaseNode> parent = (CheckBoxTreeItem<TestcaseNode>) selectedItem;

            rule = (TestcaseNode) parent.getValue();

            TestcaseNode newTestcase = new TestcaseNode();
            newTestcase.setTestcaseName("New Rule");
            newTestcase.setRuleName(rule.getRuleName());
            newTestcase.setActualArgument(rule.getDefaultActualArgument());
            newTestcase.setNodeType(NodeType.TESTCASE);
            newTestcase.setParentRule(rule);

            rule.getChildren().add(newTestcase);

            CheckBoxTreeItem<TestcaseNode> newTestcaseNode = new CheckBoxTreeItem<>(newTestcase);
            parent.getChildren().add(newTestcaseNode);

            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            BIMModel model = sceneController.getModelList().getSelectionModel().getSelectedItem();
            model.setNode(rule.getParentRule());

            UIUtils.updateConsole("New Testcases is added");
        });
        contextMenu.getItems().add(addNewRule);

        MenuItem addNewTestcase = new MenuItem("Add New Testcase");
        addNewTestcase.setOnAction((ActionEvent event) -> {
            TreeItem<TestcaseNode> selectedItem = (TreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            CheckBoxTreeItem<TestcaseNode> parent = (CheckBoxTreeItem<TestcaseNode>) selectedItem;

            rule = (TestcaseNode) parent.getValue();

            TestcaseNode newTestcase = new TestcaseNode();
            newTestcase.setTestcaseName("New TestCase");
            newTestcase.setRuleName(rule.getRuleName());
            newTestcase.setActualArgument(rule.getDefaultActualArgument());
            newTestcase.setNodeType(NodeType.TESTCASE);
            newTestcase.setParentRule(rule);

            rule.getChildren().add(newTestcase);

            CheckBoxTreeItem<TestcaseNode> newTestcaseNode = new CheckBoxTreeItem<>(newTestcase);
            parent.getChildren().add(newTestcaseNode);

            SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
            BIMModel model = sceneController.getModelList().getSelectionModel().getSelectedItem();
            model.setNode(rule.getParentRule());

            UIUtils.updateConsole("New Testcases is added");
        });
        contextMenu.getItems().add(addNewTestcase);

        MenuItem deleteTheRule = new MenuItem("Delete the Rule");
        deleteTheRule.setOnAction((ActionEvent event) -> {
            CheckBoxTreeItem<TestcaseNode> selectedItem = (CheckBoxTreeItem<TestcaseNode>) treeView.getSelectionModel().getSelectedItem();
            treeView.getRoot().getChildren().remove(selectedItem);
            UIUtils.updateConsole("Rule " + selectedItem.getValue().getRuleName() + " is deleted");
        });
        contextMenu.getItems().add(deleteTheRule);
    }

    public void excelToTree(String excelFilePath) throws JSONException, IOException, FileNotFoundException, InvalidFormatException {
        AllRulesExcelJSONManagement mgmt = new AllRulesExcelJSONManagement();
        JSONArray testCases = mgmt.excelToJSON(excelFilePath);
        populateTreeFromData(testCases.toString());

    }

    public void populateTreeFromRootNode(TestcaseNode rootNode) throws JSONException {
        CheckBoxTreeItem<TestcaseNode> treeRuleNode, treeTestcaseNode;
        treeRootNode.getChildren().clear();
        for (TestcaseNode ruleNode : rootNode.getChildren()) {
            treeRuleNode = new CheckBoxTreeItem<>(ruleNode);
            treeRootNode.getChildren().add(treeRuleNode);
            treeRuleNode.getChildren().sort((TreeItem<TestcaseNode> o1, TreeItem<TestcaseNode> o2)
                    -> o1.getValue().getTestcaseName().compareTo(o2.getValue().getTestcaseName()));
            for (TestcaseNode testcaseNode : ruleNode.getChildren()) {
                treeTestcaseNode = new CheckBoxTreeItem<>(testcaseNode);
                treeRuleNode.getChildren().add(treeTestcaseNode);
            }
        }
    }

    public void populateTreeFromData(String data) throws JSONException {
        JSONArray allData = new JSONArray(data);
        String tempRuleName = null, testcasesName;
        boolean addRule;
        CheckBoxTreeItem<TestcaseNode> treeRuleNode = null, treeTestcaseNode;
        treeRootNode.getChildren().clear();
        int ruleCounter = 0;
        Map<String, String> maps = RuleInfo.INSTANCE.getMaps();
        BIMModel model;
        for (int index = 0; index < allData.length(); index++) {
            JSONObject json = allData.getJSONObject(index);
            ruleName = json.getString(JSONConstants.RULE_NAME);
            if (ruleName.isEmpty()) {
                continue;
            }
            addRule = false;
            if (tempRuleName == null) {
                tempRuleName = ruleName;
                addRule = true;

                root = new TestcaseNode();
                root.setNodeType(NodeType.ROOT);
                treeRootNode.setValue(root);

            } else if (!tempRuleName.equals(ruleName)) {
                tempRuleName = ruleName;
                addRule = true;
            }
            /*
             Rule is added
             */
            if (addRule && maps.containsKey(ruleName)) {
                rule = new TestcaseNode();
                rule.setRuleName(ruleName);
                rule.setNodeType(NodeType.RULE);

                JSONObject ruleData = new JSONObject(maps.get(ruleName));
                rule.setDefaultActualArgument(ruleData.getJSONObject("actuals").toString());
                rule.setSchema(ruleData.getJSONObject("schema").toString());
                rule.setDescriptor(ruleData.getJSONObject("descriptor").toString());
                rule.setParentRule(root);

                treeRootNode.getValue().getChildren().add(rule);

                treeRuleNode = new CheckBoxTreeItem<>(rule);
                treeRootNode.getChildren().add(treeRuleNode);
                treeRuleNode.getChildren().sort((TreeItem<TestcaseNode> o1, TreeItem<TestcaseNode> o2)
                        -> o1.getValue().getTestcaseName().compareTo(o2.getValue().getTestcaseName()));
                treeRuleNode.selectedProperty().addListener((ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) -> {
                            TestcaseNode node = treeRootNode.getValue();
                            Set<TestcaseNode> snode = Model.INSTANCE.getListOfSelectedRules();
                            if (observable.getValue()) {
                                if (!snode.contains(node)) {
                                    snode.add(node);
                                }
                            } else {
                                if (snode.contains(node)) {
                                    snode.remove(node);
                                }
                            }
                        });
                ruleCounter++;
            }

            /*
             Testcase is added
             */
            if (rule != null && treeRuleNode != null) {
                testcasesName = json.getString(JSONConstants.TESTCASE_NAME);
                testcase = new TestcaseNode();
                testcase.setRuleName(ruleName);
                testcase.setNodeType(NodeType.TESTCASE);
                if (!json.isNull(MODEL_NAME)) {
                    testcase.setModelName(json.getString(MODEL_NAME));
                    Model.INSTANCE.setName(json.getString(MODEL_NAME));
                }
                testcase.setTestcaseName(testcasesName);
                testcase.setActualArgument(json.getJSONObject(ACTUAL_ARGUMENT).toString());
                testcase.setExpectedOutput(json.getJSONObject(EXPECTED_OUTPUT).toString());
                if (json.has(WORKFLOW_RUNTIME)) {
                    testcase.setWorkflowRunTime(json.getInt(WORKFLOW_RUNTIME));
                }
                if (json.has(RULE_RUNTIME)) {
                    testcase.setRuleExecutionTime(json.getInt(RULE_RUNTIME));
                }
                if (json.has(RUNTIME)) {
                    testcase.setRuntime(json.getInt(RUNTIME));
                }
                testcase.setParentRule(rule);
                rule.getChildren().add(testcase);

                treeTestcaseNode = new CheckBoxTreeItem<>(testcase);
                treeRuleNode.getChildren().add(treeTestcaseNode);
            }
        }

        root.setModelName(Model.INSTANCE.getName() == null ? "" : Model.INSTANCE.getName());
        model = new BIMModel(root);
        Model.INSTANCE.setCurrentModel(model);
        Model.INSTANCE.addModel(root.getModelName(), model);
        treeView.setRoot(treeRootNode);
        SceneFXMLController sceneController = (SceneFXMLController) ControllerUtilities.INSTANCE.getController(SceneFXMLController.class);
        sceneController.getModelList().getItems().add(model);
        sceneController.getModelList().getSelectionModel().select(model);
        sceneController.getModelVersion().setText(String.valueOf(model.getVersion()));
        UIUtils.updateConsole("Rule Counts : " + ruleCounter);
        UIUtils.updateConsole("Testcase Counts : " + ruleCounter);
        UIUtils.showInfoWindow(msgConfig.modelLoaded(root.getModelName()));
    }

    void setModelName() {
        dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
        if (UIUtils.validateConfiguration()) {
            String name = dbMgmt.getModelName(DatabaseData.PACKAGE_MASTER.getId(), DatabaseData.PROJECT.getId());
            if (name.endsWith(".bimpk")) {
                name = name.substring(0, name.lastIndexOf(".bimpk"));
            }
            Model.INSTANCE.setName(name);
            root.setModelName(Model.INSTANCE.getName());
        }
    }

}
