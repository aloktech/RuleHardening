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
public class RuleExecution extends ExecutionImpl {

    private final ExecutionPhase executionPhase;
    private CheckBoxTreeItem<TestcaseNode> testcaseNode = null;

    public RuleExecution(TreeView<TestcaseNode> treeView) {
        this.executionPhase = new ExecutionPhaseImpl();
        this.treeView = treeView;
    }

    @Override
    public void execute(CheckBoxTreeItem<TestcaseNode> ruleNode) throws TerminationException {
        UIUtils.appendEmptyLines(1);
        UIUtils.updateConsole("\n########");
        try {
            long outerIntialTime = System.currentTimeMillis(), innerIntialTime;
            checkExecution();
            executionPhase.loginPhase();

            checkExecution();
            executionPhase.analysisPhase();

            IndexCounter.TESTCASE_INDEX.setCounter(0);
            UIUtils.refreshUILater(selectedAndExpanded(ruleNode));
            for (TreeItem<TestcaseNode> treeNode : ruleNode.getChildren()) {
                testcaseNode = (CheckBoxTreeItem<TestcaseNode>) treeNode;
                treeView.getSelectionModel().select(testcaseNode);
                UIUtils.refreshUILater(nodeSelected(testcaseNode));

                TestcaseNode node = testcaseNode.getValue();
                innerIntialTime = System.currentTimeMillis();

                checkExecution();
                JSONObject result = executionPhase.executionPhase(node);

                checkExecution();
                executionPhase.issuePhase(result, node);
                long time = testcaseExecutionTime(innerIntialTime, node.getTestcaseName());
                node.setRuleExecutionTime(time);
                testcaseExecutionStatus = true;
                UIUtils.refreshUI(statusUpdated(testcaseNode, true));
                Platform.runLater(() -> {
                    testcaseNode.setGraphic(new ImageView(success));
                    Event.fireEvent(testcaseNode, new TreeItem.TreeModificationEvent<>(TreeItem.graphicChangedEvent(),
                            testcaseNode, testcaseNode.getValue()));
                });
                UIUtils.appendEmptyLines(1);
            }
            ruleNode.setGraphic(new ImageView(ruleNode.isIndeterminate() ? failure : success));
            ruleExecutionTime(outerIntialTime, ruleNode.getValue().getRuleName());
            UIUtils.showInfoWindow("Execution of rule " + ruleNode.getValue().getRuleName() + " is over");
        } catch (ConfigurationException | ExecutionException | JSONException ex) {
            testcaseExecutionStatus = false;
            log.log(Level.SEVERE, ex.getMessage());
            if (testcaseNode != null) {
                UIUtils.refreshUI(statusUpdated(testcaseNode, false));
            }
        } catch (TerminationException ex) {
            testcaseExecutionStatus = false;
            if (testcaseNode != null) {
                UIUtils.refreshUI(statusUpdated(testcaseNode, false));
            }
            throw ex;
        } finally {
            testcaseNode.getValue().setExecutionResult(testcaseExecutionStatus);
            testcaseNode.setGraphic(new ImageView(testcaseExecutionStatus ? success : failure));
        }
    }

    private void checkExecution() throws TerminationException {
        if (!executionPhase.doContinueExecution()) {
            throw new TerminationException("Terminate Execution");
        }
    }
}
