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

import org.aeonbits.owner.Config;

/**
 *
 * @author Alok Ranjan Meher
 */
@Config.Sources("file:src/main/resources/properties/messages.properties")
public interface UIComponentConfig extends Config {
    
    @Key("ui.button.executeAll")
    String executeAllRules();
    
    @Key("ui.button.executeSelected")
    String executeSelectedRules();
    
    @Key("ui.button.executeAll")
    String exportAllRules();
    
    @Key("ui.button.executeAll")
    String exportSelectedRules();
    
    @Key("ui.button.executeAll")
    String reset();
    
    @Key("ui.button.executeAll")
    String clear();
    
    @Key("ui.button.executeAll")
    String generateReport();
}
