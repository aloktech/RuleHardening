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

import com.invicara.tools.rh.service.Command;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.UIUtils;
import javafx.event.Event;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Alok Ranjan Meher
 */
public abstract class ExecutionImpl implements Execution {

    protected Image success, failure;
    protected TreeView<TestcaseNode> treeView;
    protected CheckBoxTreeItem<TestcaseNode> rule = null, testcase = null;
    protected boolean testcaseExecutionStatus;

    protected ExecutionImpl() {
        success = new Image(getClass().getClassLoader().getResourceAsStream("images/correct.png"));
        failure = new Image(getClass().getClassLoader().getResourceAsStream("images/error.png"));
    }

    public Command selectedAndExpanded(CheckBoxTreeItem<TestcaseNode> treeNode) {
        return () -> {
            treeNode.setExpanded(true);
            treeView.getSelectionModel().select(treeNode);
        };
    }
    
    public Command nodeSelected(CheckBoxTreeItem<TestcaseNode> treeNode) {
        return () -> {
            if (treeNode != null) {
                treeNode.setSelected(Boolean.TRUE);
            }
        };
    }

    public Command statusUpdated(CheckBoxTreeItem<TestcaseNode> treeNode, boolean status) {
        return () -> {
            if (treeNode != null) {
                treeNode.setGraphic(new ImageView(status ? success : failure));
                Event.fireEvent(treeNode, 
                            new TreeItem.TreeModificationEvent<>(TreeItem.graphicChangedEvent(), 
                            treeNode, treeNode.getValue()));
            }
        };
    }

    public void allRuleExecutionTime(long allIntialTime) {
        long allFinalTime;
        allFinalTime = System.currentTimeMillis();
        if ((allFinalTime - allIntialTime) > 100) {
            long timeTaken = (allFinalTime - allIntialTime) / (1000 * 60);
            long minTimeTaken = (allFinalTime - allIntialTime) / (1000 * 60);
            long secTimeTaken = (((allFinalTime - allIntialTime) / 1000) % 60);
            if (timeTaken == 0) {
                timeTaken = (allFinalTime - allIntialTime);
                UIUtils.updateConsole("All Rules takes " + timeTaken + " milliseconds");
            } else {
                UIUtils.updateConsole("All Rules takes " + minTimeTaken + ":" + secTimeTaken + " minutes");
            }
        }
    }

    public long ruleExecutionTime(long ruleInnerTime, String ruleName) {
        long ruleFinalTime = System.currentTimeMillis();
        long finalTime = ruleFinalTime - ruleInnerTime;
        if (finalTime > 100) {
            long timeTaken = (ruleFinalTime - ruleInnerTime) / (1000 * 60);
            long minTimeTaken = (ruleFinalTime - ruleInnerTime) / (1000 * 60);
            long secTimeTaken = ((ruleFinalTime - ruleInnerTime) / 1000) % 60;
            if (timeTaken == 0) {
                timeTaken = ruleFinalTime - ruleInnerTime;
                UIUtils.updateConsole("Rule " + ruleName + "execution takes " + timeTaken + " milliseconds");
            } else {
                UIUtils.updateConsole("Rule " + ruleName + " execution takes " + minTimeTaken + ":" + secTimeTaken + " minutes");
            }
        }
        return finalTime;
    }

    public long executionTimeInWorkFlow(long ruleInnerTime, String ruleName, String testcase) {
        long ruleFinalTime = System.currentTimeMillis();
        long finalTime = ruleFinalTime - ruleInnerTime;
        if (finalTime > 100) {
            long timeTaken = (ruleFinalTime - ruleInnerTime) / (1000 * 60);
            long minTimeTaken = (ruleFinalTime - ruleInnerTime) / (1000 * 60);
            long secTimeTaken = ((ruleFinalTime - ruleInnerTime) / 1000) % 60;
            if (timeTaken == 0) {
                timeTaken = ruleFinalTime - ruleInnerTime;
                UIUtils.updateConsole("Rule " + ruleName + "(" + testcase + ") takes " + timeTaken + " milliseconds in workflow");
            } else {
                UIUtils.updateConsole("Rule " + ruleName + " takes " + minTimeTaken + ":" + secTimeTaken + " minutes in workflow");
            }
        }
        return finalTime;
    }

    public long testcaseExecutionTime(long innerIntialTime, String testCaseName) {
        long innerFinalTime;
        innerFinalTime = System.currentTimeMillis();
        long time = innerFinalTime - innerIntialTime;
        if (time > 100) {
            long timeTaken = (innerFinalTime - innerIntialTime) / (1000 * 60);
            long minTimeTaken = (innerFinalTime - innerIntialTime) / (1000 * 60);
            long secTimeTaken = ((innerFinalTime - innerIntialTime) / 1000) % 60;
            if (timeTaken == 0) {
                timeTaken =innerFinalTime - innerIntialTime;
                UIUtils.updateConsole(testCaseName + " takes " + timeTaken + " milliseconds");
            } else {
                UIUtils.updateConsole(testCaseName + " takes " + minTimeTaken + ":" + secTimeTaken + " minutes");
            }
        }
        return time;
    }
}
