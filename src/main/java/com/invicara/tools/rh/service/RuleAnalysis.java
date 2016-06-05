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

import com.invicara.tools.rh.utils.REST_URLS;
import com.invicara.tools.rh.utils.HttpUtilities;
import com.invicara.tools.rh.configuration.Server;
import com.invicara.tools.rh.configuration.AuthenticationToken;
import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.configuration.RESTfulURL;
import com.invicara.tools.rh.exception.ExecutionException;
import com.invicara.tools.rh.exception.BreakLoopException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author Alok Ranjan Meher
 */
public class RuleAnalysis {

    private final String headerContentKey = "Content-Type";
    private final String headerContentValue = "application/json";
    private final String headerAcceptKey = "Accept";
    private final String headerAcceptValue = "application/json";
    private final String headerAuthorization = "Authorization";
    private final String failed = " FAILED : {0}";
    private final String succeed = " : SUCCEED";
    
    public String getRESTGET(String url) throws ExecutionException {
        String outputData = "";
        int count = 0;
        HttpGet getRequest = new HttpGet(url.trim());
        getRequest.addHeader(headerAcceptKey, headerAcceptValue);
        getRequest.addHeader(headerAuthorization, AuthenticationToken.ACCOUNT.getToken());

        CloseableHttpClient httpClient = null;
        try {
            while (true) {
                httpClient = HttpClientBuilder.create().build();
                CloseableHttpResponse response = httpClient.execute(getRequest);
                if (response.getStatusLine().getStatusCode() != 200) {
                    count = checkGETCount(count, response);
                } else {
                    outputData = HttpUtilities.extractDataFromOutput(response);
                    httpClient.close();
//                    Console.INSTANCE.setConsoleMessage(message);
                    break;
                }
            }
        } catch (IOException | BreakLoopException e) {
            throw new ExecutionException(e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
            }
        }

        return outputData;
    }

    private int checkGETCount(int count, HttpResponse response) throws BreakLoopException {
        count++;
        if (count > 10) {
//            Console.INSTANCE.setConsoleMessage(message);
            throw new BreakLoopException(String.valueOf(response.getStatusLine().getStatusCode()));
        }
        return count;
    }

    public String getRESTPOST(String url, String data) throws UnsupportedEncodingException, UnsupportedOperationException, IOException, ExecutionException {
        String outputData;
        HttpPost postRequest = new HttpPost(url.trim());
        postRequest.setHeader(headerAcceptKey, headerAcceptValue);
        postRequest.setHeader(headerContentKey, headerContentValue);
        postRequest.setHeader(headerAuthorization, AuthenticationToken.ACCOUNT.getToken());

        if (data != null && !data.isEmpty()) {
            StringEntity input = new StringEntity(data);
            input.setContentType(headerContentValue);
            postRequest.setEntity(input);
        }

        outputData = checkResponse(postRequest);

        return outputData;
    }

    private String checkResponse(HttpPost postRequest) throws UnsupportedOperationException, IOException, ExecutionException {
        int count = 0;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response;
        try {
            while (true) {
                httpClient = HttpClientBuilder.create().build();
                response = httpClient.execute(postRequest);
                if (response.getStatusLine().getStatusCode() != 201) {
                    count = checkPOSTCount(count, response);
                } else {
                    return HttpUtilities.extractDataFromOutput(response);
                }
            }
        } catch (BreakLoopException e) {
            throw new ExecutionException(e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
            }
        }
    }

    private int checkPOSTCount(int count, HttpResponse response) throws BreakLoopException {
        count++;
        if (count > 10) {
//            message = setComment(msg + failed, String.valueOf(response.getStatusLine().getStatusCode()));
//            Console.INSTANCE.setConsoleMessage(message);
            throw new BreakLoopException(String.valueOf(response.getStatusLine().getStatusCode()));
        }
        return count;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String getRuleResult(int analysisRunId) throws ExecutionException {
        String outputData = "";
        String comment = "Rule Result Generation";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.RULE_RESULT;
            url = url.replace("{{analysesrunsid}}", String.valueOf(analysisRunId));

            outputData = getRESTGET(url);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (ExecutionException e) {
            Console.INSTANCE.setConsoleMessage(comment + failed);
            throw new ExecutionException(setComment(comment + failed, e.getMessage()));
        }

        return outputData;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String getRuleIssues(int ruleResultId) throws ExecutionException {
        String outputData = "";
        String comment = "Rule Issue Generation";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.RULE_ISSUES;
            url = url.replace("{{ruleresultid}}", String.valueOf(ruleResultId));

            outputData = getRESTGET(url);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (ExecutionException e) {
            Console.INSTANCE.setConsoleMessage(comment + failed);
            throw new ExecutionException(comment + failed);
        }
        return outputData;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String createAnalysis(int projectId, String data) throws ExecutionException {
        String outputData = "";
        String comment = "Analysis Creation";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.CREATE_ANALYSIS;
            url = replaceVariableByValue(url, "{{projectid}}", projectId);

            outputData = getRESTPOST(url, data);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (Exception e) {
            Console.INSTANCE.setConsoleMessage(setComment(comment + failed, e.getMessage()));
            throw new ExecutionException(setComment(comment + failed, e.getMessage()));
        }

        return outputData;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String createAnalysisMember(int packagemasterid, String data) throws ExecutionException {
        String outputData = "";
        String comment = "Analysis Member Creation";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.CREATE_ANALYSIS_MEMBER;
            url = replaceVariableByValue(url, "{{packagemasterid}}", packagemasterid);

            outputData = getRESTPOST(url, data);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (Exception e) {
            Console.INSTANCE.setConsoleMessage(setComment(comment + failed, e.getMessage()));
            throw new ExecutionException(setComment(comment + failed, e.getMessage()));
        }

        return outputData;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String createAnalysisInstance(int analysisId, String data) throws ExecutionException {
        String outputData = "";
        String comment = "Analysis Instance Creation";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.CREATE_ANALYSIS_INSTANCE;
            url = replaceVariableByValue(url, "{{analysisid}}", analysisId);

            outputData = getRESTPOST(url, data);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (Exception e) {
            Console.INSTANCE.setConsoleMessage(setComment(comment + failed, e.getMessage()));
            throw new ExecutionException(setComment(comment + failed, e.getMessage()));
        }

        return outputData;
    }

    @SuppressWarnings("UseSpecificCatch")
    public String executeAnalysisInstance(int analysisId) throws ExecutionException {
        String comment = "Analysis Instance Execution";
        String outputData = "";
        try {
            String url = generateEmpireManageServerUrl() + REST_URLS.EXECUTE_RULE;
            url = replaceVariableByValue(url, "{{analysisid}}", analysisId);

            outputData = getRESTPOST(url, null);
            Console.INSTANCE.setConsoleMessage(comment + succeed);
        } catch (Exception e) {
            Console.INSTANCE.setConsoleMessage(setComment(comment + failed, e.getMessage()));
            throw new ExecutionException(setComment(comment + failed, e.getMessage()));
        }
        return outputData;
    }

    private String setComment(String comment, String value) {
        return MessageFormat.format(comment, value);
    }

    private static String generateEmpireManageServerUrl() {
        return Server.TOMCAT_EMPIRE_MANAGE.getServerUrl() + RESTfulURL.EMPIRE_MANAGE.getBasePath();
    }

    private String replaceVariableByValue(String url, String variable, int projectId) {
        return url.replace(variable, String.valueOf(projectId));
    }
}
