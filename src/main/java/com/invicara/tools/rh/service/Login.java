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
import com.invicara.tools.rh.configuration.Console;
import com.invicara.tools.rh.configuration.DatabaseData;
import com.invicara.tools.rh.configuration.RESTfulURL;
import com.invicara.tools.rh.configuration.Server;
import com.invicara.tools.rh.utils.REST_URLS;
import com.invicara.tools.rh.utils.HttpUtilities;
import com.invicara.tools.rh.exception.ConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class Login {

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;

    public void userSignin() throws ConfigurationException {
        try {
            httpClient = HttpClientBuilder.create().build();
            String url = Server.TOMCAT_PASSPORT.getServerUrl() + RESTfulURL.PASSPORT.getBasePath() + REST_URLS.USER_SIGNIN;
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Accept", "application/json");
            StringEntity input = new StringEntity(DatabaseData.USER.getUserLoginData());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            response = httpClient.execute(postRequest);

            int status = response.getStatusLine().getStatusCode();
            if (status != 201) {
                Console.INSTANCE.setConsoleMessage("User Login Failed " + status);
                throw new ConfigurationException("User Login Failed " + status);
            }

            String strToken = HttpUtilities.extractDataFromOutput(response);
            JSONObject token = new JSONObject(strToken);

            AuthenticationToken.USER.setToken(token.getString("token"));
            Console.INSTANCE.setConsoleMessage("User signin done");
        } catch (UnsupportedEncodingException ex) {
            Console.INSTANCE.setConsoleMessage("User Login Failed " + ex.getMessage());
            throw new ConfigurationException("User Login Failed " + ex.getMessage());
        } catch (IOException ex) {
            Console.INSTANCE.setConsoleMessage("User Login Failed " + ex.getMessage());
            throw new ConfigurationException("User Login Failed " + ex.getMessage());
        } finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void accountSignin(int accountId) throws ConfigurationException {
        try {
            httpClient = HttpClientBuilder.create().build();
            String url = Server.TOMCAT_PASSPORT.getServerUrl() + RESTfulURL.PASSPORT.getBasePath() + REST_URLS.ACCOUNT_SIGNIN;
            url = url.replace("{{accountid}}", String.valueOf(accountId));
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Authorization", AuthenticationToken.USER.getToken());

            response = httpClient.execute(postRequest);

            int status = response.getStatusLine().getStatusCode();
            if (status != 201) {
                Console.INSTANCE.setConsoleMessage("Account Login Failed " + status);
                throw new ConfigurationException("Account Login Failed " + status);
            }

            String strToken = HttpUtilities.extractDataFromOutput(response);
            JSONObject json = new JSONObject(strToken);

            AuthenticationToken.ACCOUNT.setToken(json.getString("token"));
            Console.INSTANCE.setConsoleMessage("Account signin done");
        } catch (IOException ex) {
            Console.INSTANCE.setConsoleMessage("Account Login Failed " + ex.getMessage());
            throw new ConfigurationException("Account Login Failed " + ex.getMessage());
        } finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
