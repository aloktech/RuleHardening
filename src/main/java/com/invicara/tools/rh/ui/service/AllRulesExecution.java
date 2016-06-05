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
public class AllRulesExecution extends ExecutionImpl {

    private final ExecutionPhase executionPhase;

    public AllRulesExecution() {
        this.executionPhase = new ExecutionPhaseImpl();
    }

    public AllRulesExecution(TreeView<TestcaseNode> treeView) {
        this.executionPhase = new ExecutionPhaseImpl();
        this.treeView = treeView;
    }

    @Override
    public void execute(CheckBoxTreeItem<TestcaseNode> allRules) throws TerminationException {
        long allRulesIntialTime = System.currentTimeMillis(), ruleIntialTime;
        UIUtils.appendEmptyLines(1);
        UIUtils.updateConsole("\n########");
        try {
            checkExecution();
            executionPhase.loginPhase();

            IndexCounter.TESTCASE_INDEX.setCounter(0);

            for (TreeItem<TestcaseNode> ruleNode : allRules.getChildren()) {
                rule = (CheckBoxTreeItem<TestcaseNode>) ruleNode;
                ruleIntialTime = System.currentTimeMillis();

                UIUtils.refreshUILater(selectedAndExpanded(rule));
                executeRule(ruleIntialTime);
                UIUtils.appendEmptyLines(1);
                UIUtils.updateConsole("\n########");
            }
            UIUtils.showInfoWindow("Execution of all rules are over");
        } catch (ConfigurationException | JSONException ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } catch (TerminationException ex) {
            throw ex;
        } finally {
            allRuleExecutionTime(allRulesIntialTime);
        }
    }

    private void executeRule(long ruleIntialTime) throws TerminationException {
        rule.getValue().setExecutionStatus(false);
        try {
            checkExecution();
            executionPhase.analysisPhase();

            for (TreeItem<TestcaseNode> testcaseNode : rule.getChildren()) {
                testcase = (CheckBoxTreeItem<TestcaseNode>) testcaseNode;
                executeTestcase(testcase);
                UIUtils.appendEmptyLines(1);
            }
            rule.getValue().setExecutionStatus(true);
            rule.getValue().setExecutionResult(true);
        } catch (ExecutionException | JSONException ex) {
            rule.getValue().setExecutionResult(false);
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.refreshUI(statusUpdated(rule, false));
        } finally {
            ruleExecutionTime(ruleIntialTime, rule.getValue().getRuleName());
            rule.setExpanded(false);
            rule.getValue().setExecutionResult(testcaseExecutionStatus);
            rule.setGraphic(new ImageView(testcaseExecutionStatus ? success : failure));
        }
    }

    private void executeTestcase(CheckBoxTreeItem<TestcaseNode> testcaseNode) throws TerminationException {
        long testcaseIntialTime;
        testcaseNode.getValue().setExecutionStatus(false);
        
        try {
            treeView.getSelectionModel().select(testcaseNode);
            UIUtils.refreshUILater(nodeSelected(testcaseNode));

            testcaseIntialTime = System.currentTimeMillis();
            TestcaseNode node = testcaseNode.getValue();

            checkExecution();
            JSONObject result = executionPhase.executionPhase(node);

            checkExecution();
            executionPhase.issuePhase(result, node);

            long time = testcaseExecutionTime(testcaseIntialTime, node.getTestcaseName());
            node.setRuleExecutionTime(time);
            testcaseNode.getValue().setExecutionStatus(true);
            testcaseNode.getValue().setExecutionResult(true);
            UIUtils.refreshUI(statusUpdated(testcaseNode, true));
            testcaseExecutionStatus = true;
            Platform.runLater(() -> {
                testcaseNode.setGraphic(new ImageView(success));
                Event.fireEvent(testcaseNode, new TreeItem.TreeModificationEvent<>(TreeItem.graphicChangedEvent(),
                        testcaseNode, testcaseNode.getValue()));
            });
        } catch (ExecutionException | JSONException ex) {
            testcaseExecutionStatus = false;
            testcaseNode.getValue().setExecutionResult(false);
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.updateConsole("Testcase execution failed");
            UIUtils.refreshUI(statusUpdated(testcaseNode, false));
        } catch(TerminationException e) {
            testcaseExecutionStatus = false;
            throw e;
        }
    }

    public void checkExecution() throws TerminationException {
        if (!executionPhase.doContinueExecution()) {
            throw new TerminationException("Terminate Execution");
        }
    }
}
