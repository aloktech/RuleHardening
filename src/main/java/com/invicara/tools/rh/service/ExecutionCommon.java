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

import com.invicara.tools.rh.configuration.AuthenticationToken;
import com.invicara.tools.rh.configuration.Database;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.exception.ConfigurationException;
import com.invicara.tools.rh.utils.IndexCounter;
import com.invicara.tools.rh.exception.ExecutionException;
import static com.invicara.tools.rh.utils.JSONConstants.DESCRIPTION;
import static com.invicara.tools.rh.utils.JSONConstants.DESCRIPTION_MSG;
import static com.invicara.tools.rh.utils.JSONConstants.ISSUES;
import static com.invicara.tools.rh.utils.JSONConstants.ISSUE_ELEMENTS;
import static com.invicara.tools.rh.utils.JSONConstants.NUM_ELEMENTS;
import static com.invicara.tools.rh.utils.JSONConstants.RUNTIME;
import static com.invicara.tools.rh.utils.JSONConstants.STATUS;
import static com.invicara.tools.rh.utils.JSONConstants.TOTAL_ELEMENTS;
import com.invicara.tools.rh.utils.Status;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.utils.UIUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum ExecutionCommon {

    INSTANCE;

    private DatabaseManagement dbMgmt;
    private RuleAnalysis analysis;
    private Login login;

    @Getter
    @Setter
    int analysisId, accountId, projectId, packageMasterId, ruleId;

    private ExecutionCommon() {
        try {
            initialize();
        } catch (ConfigurationException ex) {
            Logger.getLogger(ExecutionCommon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initialize() throws ConfigurationException {
        dbMgmt = new DatabaseManagement();
        if (Database.PASSPORT.getConnection() == null) {
            Database.PASSPORT.setConnection(dbMgmt.configureToEmpireManageDatabase("passportdb"));
        }

        if (Database.EMPIRE_MANAGE.getConnection() == null) {
            Database.EMPIRE_MANAGE.setConnection(dbMgmt.configureToEmpireManageDatabase("empiremanagedb"));
        }

        login = new Login();
        analysis = new RuleAnalysis();

        Status.ALL_RULES_EXECUTION.setActive(false);
        Status.SKIP_RULE_EXECUTION.setActive(false);
        Status.SKIP_TESTCASE_EXECUTION.setActive(false);
    }

    public void login() throws ConfigurationException {
        // Login
        if (AuthenticationToken.USER.isTokenEmpty()) {
            login.userSignin();
        } else {
            UIUtils.updateConsole("User login is already done");
        }

        // Account
        login.accountSignin(DatabaseData.ACCOUNT.getId());

        UIUtils.updateConsole("Token : \n" + AuthenticationToken.ACCOUNT.getToken());
    }

    public JSONObject executeTestcase(final TestcaseNode node,
            int accountId, int analysisId) throws JSONException {
        long ruleTime;
        String testCaseName, ruleName = node.getRuleName(), modelName = node.getModelName(), output;
        JSONObject obj = node.generateTestcaseResult();
        try {
            testCaseName = node.getTestcaseName();
            UIUtils.updateConsole(IndexCounter.TESTCASE_INDEX.getIncrementValue()
                    + " : " + ruleName + " : " + testCaseName);

            // Get Info from Configuration.Database
            dbMgmt.setConnection(Database.EMPIRE_MANAGE.getConnection());
            int systemRuleId = dbMgmt.getSystemRuleId(ruleName);
            int accountRuleId = dbMgmt.getAccountRuleId(systemRuleId, accountId);
            RuleInstance ruleInstance = new RuleInstance.RuleInstanceBuilder()
                    .ruleDefId(accountRuleId)
                    .description(ruleName)
                    .name(ruleName)
                    .groupname(testCaseName)
                    .actualparams(node.getActualArgument())
                    .build();
            UIUtils.updateConsole("Actuals Argument : " + node.getActualArgument());
            
            //Create  Analysis Instance
            output = analysis.createAnalysisInstance(analysisId, ruleInstance.getAsJSON());
            JSONObject ruleData = new JSONObject(output);
            if (ruleData.has("id")) {
                DatabaseData.RULE.setId(ruleData.getInt("id"));
                ruleTime = System.currentTimeMillis();
                output = analysis.executeAnalysisInstance(analysisId);
                long time = executionTimeInWorkFlow(ruleTime, ruleName, testCaseName);
                node.setWorkflowRunTime(time);
                JSONObject analyisResult = new JSONObject(output);
                return analyisResult;
            } else {
                UIUtils.updateConsole("Analysis Instance creation failed");
                throw new ExecutionException("Analysis Instance creation failed");
            }
        } catch (JSONException | ExecutionException e) {
            obj.put("modelName", modelName);
            node.setModelName(modelName);
            UIUtils.updateConsole("Testcase execution Failed : " + e.getMessage());
        }
        return null;
    }

    public void extractIssues(JSONObject analyisResult, int packageMasterId, int analysisId, int ruleId,
            final TestcaseNode node) throws ExecutionException, JSONException {
        int analysisRunId;
        if (analyisResult.has("analysisrunids")) {
            analysisRunId = analyisResult.getJSONArray("analysisrunids").getInt(0);
        } else {
            analysisRunId = dbMgmt.getAnalysisRunId(packageMasterId, analysisId);
        }
        if (dbMgmt.checkRuleExecutionStatus(analysisRunId)) {
            ruleResult(analysis, analysisRunId, ruleId, node);
        } else {
            throw new ExecutionException("Rule Execution not completed in time less than 1 minute");
        }
    }

    public void ruleResult(RuleAnalysis analysis, int analysisRunId, int ruleId, TestcaseNode node) throws JSONException, ExecutionException {
        String output;
        // Rule Result
        boolean breakWhileLoop = true;
        int resultCounter = 0;
        while (breakWhileLoop) {
            if (resultCounter++ > 59) {
                UIUtils.updateConsole("Timeout : Unable to find rule_id : " + ruleId + " for analysis_run_id " + analysisRunId);
                throw new ExecutionException("Timeout : Unable to find rule_id : " + ruleId + " for analysis_run_id " + analysisRunId);
            }
            output = analysis.getRuleResult(analysisRunId);
            JSONObject ruleIssue = new JSONObject(output);
            JSONArray list = ruleIssue.getJSONArray("list");
            int ruleResultId;
            for (int p = 0; p < list.length(); p++) {
                JSONObject result = list.getJSONObject(p);
                if (ruleId == result.getInt("ruleid")) {
                    try {
                        node.setRuntime(result.getInt(RUNTIME));

                        // Rule Output
                        JSONObject jsonOutput = new JSONObject();
                        jsonOutput.put(TOTAL_ELEMENTS, result.getInt(TOTAL_ELEMENTS));
                        jsonOutput.put(NUM_ELEMENTS, result.getInt(NUM_ELEMENTS));
                        jsonOutput.put(RUNTIME, result.getInt(RUNTIME));

                        // Rule Issues
                        ruleResultId = result.getInt("id");
                        output = analysis.getRuleIssues(ruleResultId);

                        jsonOutput = addIssues(output, jsonOutput);
                        node.setExpectedOutput(jsonOutput.toString());
                        UIUtils.updateConsole(jsonOutput.toString());
                        breakWhileLoop = false;
                        break;
                    } catch (JSONException e) {
                        failedToExecuteTestcase(e, node);
                        breakWhileLoop = true;
                        UIUtils.updateConsole("Exception : Unable to find rule_id : " + ruleId + " for analysis_run_id " + analysisRunId);
                    }
                }
            }
            if (!breakWhileLoop) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ExecutionCommon.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public int analysisConfiguration(int projectId, int packageMasterId) throws JSONException, ExecutionException {
        String data = "{ \n"
                + "  \"name\": \"Analysis "
                + 26
                + "\",\n"
                + "  \"description\": \"Analysis Study\"\n"
                + "}";
        String output;
        //        Create  Analysis
        output = analysis.createAnalysis(projectId, data);
        JSONObject analysisJSON = new JSONObject(output);
        analysisId = analysisJSON.getInt("id");
        DatabaseData.ANALYSIS.setId(analysisId);

        //Create  Analysis Member
        data = "{\n"
                + "  \"analysisids\" : ["
                + analysisId
                + "]\n"
                + "}";
        analysis.createAnalysisMember(packageMasterId, data);
        return analysisId;
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

    private void failedToExecuteTestcase(Exception e, TestcaseNode node) throws JSONException {
        JSONObject expectedOutput;
        JSONArray issues = new JSONArray();
        expectedOutput = new JSONObject();
        JSONObject issue = new JSONObject();
        expectedOutput.put(TOTAL_ELEMENTS, 0);
        expectedOutput.put(NUM_ELEMENTS, 0);

        issue.put(ISSUE_ELEMENTS, 0);
        issue.put(DESCRIPTION_MSG, "Failed to execute : " + e.getMessage());
        issue.put(DESCRIPTION, 0);
        issue.put(STATUS, 0);

        issues.put(issue);

        expectedOutput.put(ISSUES, issues);
        issues.put(expectedOutput);
        node.setExpectedOutput(expectedOutput.toString());
    }

    private JSONObject addIssues(String output, JSONObject outputResult) throws JSONException {
        JSONObject jsonOutput = new JSONObject(output);
        JSONArray msgList = jsonOutput.getJSONObject("resources").getJSONObject("en").getJSONArray("issues");
        JSONObject issues = jsonOutput.getJSONObject("issues");
        JSONArray issueList = issues.getJSONArray("list");
        JSONArray issuess = new JSONArray();
        int counter = 0, value = 0, tempValue = 0, tempIssueElement = 0;
        boolean issueElementIsGreaterThanICL = false;

        for (int q = 0; q < issueList.length(); q++) {
            tempIssueElement = addJSONObject(issueList, q, msgList, issuess);

            counter++;
            if (counter > 5) {
                break;
            }
        }
        outputResult.put(ISSUES, issuess);
        return outputResult;
    }

    private int addJSONObject(JSONArray resultList, int q, JSONArray msgList, JSONArray issues) throws JSONException {
        JSONObject issue;
        JSONObject result = resultList.getJSONObject(q);
        issue = new JSONObject();
        try {
            issue.put(DESCRIPTION_MSG, msgList.getString(result.getInt(DESCRIPTION) - 1));
        } catch (Exception e) {
            issue.put(DESCRIPTION_MSG, "Failed to extract : " + e.getMessage());
        }
        issue.put(DESCRIPTION, result.getInt(DESCRIPTION));
        issue.put(STATUS, result.getInt(STATUS));

        issues.put(issue);
        return result.getInt(ISSUE_ELEMENTS);
    }
}
