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

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.Initializable;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum ControllerUtilities {

    INSTANCE;

    private final Map<Class,Initializable> controllerMap = new HashMap<>();
    
    public void addController(Class cls, Initializable obj) {
      controllerMap.put(cls, obj);
    }
    
    public void removeController(Class cls) {
        controllerMap.remove(cls);
    }
    
    public void clearMap() {
        controllerMap.clear();
    }
    
    public Initializable getController(Class cls) {
      return controllerMap.get(cls);
    }
}
