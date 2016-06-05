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
package com.invicara.tools.rh.service;

import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.exception.ConfigurationException;
import com.invicara.tools.rh.exception.ExecutionException;
import com.invicara.tools.rh.utils.Status;
import com.invicara.tools.rh.model.TestcaseNode;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class ExecutionPhaseImpl implements ExecutionPhase {

    ExecutionCommon execution;

    public ExecutionPhaseImpl() {
        this.execution = ExecutionCommon.INSTANCE;
    }

    @Override
    public boolean doContinueExecution() {
        return Status.RUNNING.isActive();
    }

    @Override
    public void loginPhase() throws ConfigurationException {
        execution.login();
    }

    @Override
    public void analysisPhase() throws ExecutionException {
        execution.analysisConfiguration(DatabaseData.PROJECT.getId(),
                DatabaseData.PACKAGE_MASTER.getId());
    }

    @Override
    public JSONObject executionPhase(TestcaseNode node) throws ExecutionException {
        if (node == null) {
            throw new ExecutionException("Selected Node is null");
        }
        return execution.executeTestcase(node,
                DatabaseData.ACCOUNT.getId(),
                DatabaseData.ANALYSIS.getId());
    }

    @Override
    public void issuePhase(JSONObject analyisResult, TestcaseNode node) throws ExecutionException {
        if (analyisResult == null) {
            throw new ExecutionException("Issue Execution phase failed");
        } else if (analyisResult.keySet().isEmpty()) {
            throw new ExecutionException("Issue Execution phase failed");
        }
        
        execution.extractIssues(analyisResult,
                DatabaseData.PACKAGE_MASTER.getId(),
                DatabaseData.ANALYSIS.getId(),
                DatabaseData.RULE.getId(), node);
    }

}
