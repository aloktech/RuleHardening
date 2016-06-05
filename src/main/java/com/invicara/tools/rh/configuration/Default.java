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

import freemarker.template.Configuration;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum Default implements Serializable {

    INSTANCE;
    
     @Getter
    private final String userDir;

    private Default() {
        userDir = System.getProperty("user.dir");
    }
    
    @Getter
    @Setter
    private Configuration configuration;
}
