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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import org.apache.http.client.methods.CloseableHttpResponse;

/**
 *
 * @author Alok Ranjan Meher
 */
public class HttpUtilities implements Serializable{

    private static final long serialVersionUID = 5352181304632650021L;

    private HttpUtilities() {
        
    }
    public static String extractDataFromOutput(CloseableHttpResponse response) {
        String output;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            while ((output = br.readLine()) != null) {
                if (!output.isEmpty()) {
                    builder.append(output);
                }
            }
        } catch (IOException ex) {
            builder.append("");
        } finally {
            try {
                response.close();
            } catch (IOException ex) {
            }
        }
        return builder.toString();
    }
}
