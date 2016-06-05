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
package com.invicara.tools.rh.utils;

/**
 *
 * @author Alok Ranjan Meher
 */
public class REST_URLS {
    
    private REST_URLS(){
        
    }
    
    public final static  String HTTP_PROTOCOL = "http";
    public final static  String HTTPS_PROTOCOL = "https";
    public final static  String PORT = "8080";
    public final static  String LOCAL_HOST = "http://localhost:8080";
    
    public final static  String BASE_PASSPORT_URL = "/xospassport/api/r1";
    public final static  String USER_SIGNIN = "/users/signin";
    public final static  String ACCOUNT_SIGNIN = "/accounts/{{accountid}}/signin";
    
    public final static  String BASE_EMPIRE_MANAGE_URL = "/empiremanage/api/r1";
    public final static  String CREATE_ANALYSIS = "/projects/{{projectid}}/analyses";
    public final static  String CREATE_ANALYSIS_MEMBER = "/pkgmasters/{{packagemasterid}}/analysismembers";
    public final static  String CREATE_ANALYSIS_INSTANCE = "/analyses/{{analysisid}}/rule";
    public final static  String EXECUTE_RULE = "/analyses/{{analysisid}}/run";
    public final static  String RULE_RESULT = "/analysisruns/{{analysesrunsid}}/ruleresults";
    public final static  String RULE_ISSUES = "/ruleresults/{{ruleresultid}}/ruleissues";
}
