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
package com.invicara.tools.rh.ui.service;

import com.invicara.tools.rh.exception.TerminationException;
import com.invicara.tools.rh.exception.ConfigurationException;
import com.invicara.tools.rh.utils.IndexCounter;
import com.invicara.tools.rh.exception.ExecutionException;
import com.invicara.tools.rh.service.ExecutionPhase;
import com.invicara.tools.rh.service.ExecutionPhaseImpl;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.UIUtils;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
@Log
public class SelectedRulesExecution extends ExecutionImpl {

    private final ExecutionPhase executionPhase;

    public SelectedRulesExecution() {
        this.executionPhase = new ExecutionPhaseImpl();
    }

    public SelectedRulesExecution(TreeView<TestcaseNode> treeView) {
        this.executionPhase = new ExecutionPhaseImpl();
        this.treeView = treeView;
    }

    @Override
    public void execute(CheckBoxTreeItem<TestcaseNode> selectedNode) throws TerminationException {
        CheckBoxTreeItem<TestcaseNode> selectedTestcase = null;
        UIUtils.appendEmptyLines(1);
        UIUtils.updateConsole("\n########");
        try {
            long allRulesIntialTime = System.currentTimeMillis();
            checkExecution();
            executionPhase.loginPhase();

            IndexCounter.TESTCASE_INDEX.setCounter(0);

            executeSelectedRules(selectedNode, selectedTestcase);

            allRuleExecutionTime(allRulesIntialTime);

            UIUtils.showInfoWindow("Execution of all selected rules are over");
        } catch (ConfigurationException | JSONException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.refreshUI(statusUpdated(selectedTestcase, false));
        } catch (TerminationException ex) {
            UIUtils.refreshUI(statusUpdated(selectedTestcase, false));
            throw ex;
        }
    }

    private CheckBoxTreeItem<TestcaseNode> executeSelectedRules(CheckBoxTreeItem<TestcaseNode> selectedNode, CheckBoxTreeItem<TestcaseNode> selectedTestcase) throws TerminationException {
        long ruleIntialTime;
        for (TreeItem<TestcaseNode> ruleNode : selectedNode.getChildren()) {
            CheckBoxTreeItem<TestcaseNode> selectedRule = (CheckBoxTreeItem<TestcaseNode>) ruleNode;
            if (!selectedRule.isSelected()) {
                continue;
            }
            ruleIntialTime = System.currentTimeMillis();

            UIUtils.refreshUILater(selectedAndExpanded(selectedRule));

            executeRule(selectedRule, ruleIntialTime);

            selectedRule.setExpanded(false);
            selectedRule.setGraphic(new ImageView(rule.isIndeterminate() ? failure : success));
        }
        return selectedTestcase;
    }

    private void executeRule(CheckBoxTreeItem<TestcaseNode> selectedRule, long ruleIntialTime) throws TerminationException {
        CheckBoxTreeItem<TestcaseNode> selectedTestcase = null;
        try {
            checkExecution();
            executionPhase.analysisPhase();

            for (TreeItem<TestcaseNode> testcaseNode : selectedRule.getChildren()) {
                selectedTestcase = (CheckBoxTreeItem<TestcaseNode>) testcaseNode;
                if (!selectedTestcase.isSelected()) {
                    continue;
                }
                treeView.getSelectionModel().select(testcaseNode);

                executeTestcase(selectedTestcase);
                UIUtils.appendEmptyLines(1);
                UIUtils.updateConsole("\n########");
            }
            ruleExecutionTime(ruleIntialTime, selectedRule.getValue().getRuleName());
        } catch (ExecutionException | JSONException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.refreshUILater(statusUpdated(selectedTestcase, false));
        }  finally {
            ruleExecutionTime(ruleIntialTime, rule.getValue().getRuleName());
            rule.setExpanded(false);
            rule.getValue().setExecutionResult(testcaseExecutionStatus);
            rule.setGraphic(new ImageView(testcaseExecutionStatus ? success : failure));
        }
    }

    private void executeTestcase(CheckBoxTreeItem<TestcaseNode> testcaseNode) throws TerminationException {
        long testcaseIntialTime;
        try {
            testcaseIntialTime = System.currentTimeMillis();
            TestcaseNode node = testcaseNode.getValue();
            UIUtils.refreshUILater(nodeSelected(testcase));

            checkExecution();
            JSONObject result = executionPhase.executionPhase(node);

            checkExecution();
            executionPhase.issuePhase(result, node);
            long time = testcaseExecutionTime(testcaseIntialTime, node.getTestcaseName());
            node.setRuleExecutionTime(time);
            testcaseExecutionStatus = true;
            UIUtils.refreshUILater(statusUpdated(testcaseNode, true));
            Platform.runLater(() -> {
                testcaseNode.setGraphic(new ImageView(success));
                Event.fireEvent(testcaseNode, new TreeItem.TreeModificationEvent<>(TreeItem.graphicChangedEvent(),
                        testcaseNode, testcaseNode.getValue()));
            });
        } catch (ExecutionException | JSONException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.refreshUILater(statusUpdated(testcaseNode, false));
            testcaseExecutionStatus = false;
        } catch(TerminationException e) {
            testcaseExecutionStatus = false;
            throw e;
        }
    }

    private void checkExecution() throws TerminationException {
        if (!executionPhase.doContinueExecution()) {
            throw new TerminationException("Terminate Execution");
        }
    }
}
