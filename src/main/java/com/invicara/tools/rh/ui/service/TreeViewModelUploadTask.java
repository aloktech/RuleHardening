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

import com.invicara.tools.rh.model.TestcaseNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 *
 * @author Alok Ranjan Meher
 */
public class TreeViewModelUploadTask extends Task<ObservableList<TestcaseNode>> {
 
    private int count;
    public TreeViewModelUploadTask(int count) {
        this.count = count;
    }
    @Override
    protected ObservableList<TestcaseNode> call() throws Exception {
        for (int i = 0; i < 500; i++) {
            updateProgress(i, 500);
            Thread.sleep(5);
        }
        ObservableList<TestcaseNode> testcases = FXCollections.observableArrayList();
        testcases.add(new TestcaseNode());
        return testcases;
    }
}
