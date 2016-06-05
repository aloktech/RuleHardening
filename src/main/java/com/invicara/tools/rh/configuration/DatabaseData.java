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
package com.invicara.tools.rh.configuration;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum DatabaseData {

    USER, ACCOUNT, PROJECT, ANALYSIS, PACKAGE_MASTER, RULE;

    @Getter
    @Setter
    private int userLoginId, id;
    @Setter
    private String userLoginData;
    @Getter
    @Setter
    private String usersData;

    public String getUserLoginData() {
        return userLoginData == null ? "" : userLoginData;
    }
}
