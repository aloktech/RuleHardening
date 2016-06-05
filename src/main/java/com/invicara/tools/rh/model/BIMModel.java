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
package com.invicara.tools.rh.model;

import com.invicara.tools.rh.configuration.Model;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok Ranjan Meher
 */
public class BIMModel {
    
    @Getter @Setter
    private TestcaseNode node;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private int version;
    
    private BIMModel() {
        version = 1;
    }

    public BIMModel(TestcaseNode node) {
        this.node = node;
        Map<String,Integer> modelVersionMap = Model.INSTANCE.getModelVersionMap();
        Integer value = modelVersionMap.get(node.getModelName());
        if (value == null) {
            modelVersionMap.put(node.getModelName(), 1);
            this.version = 1;
        } else {
            this.version = value + 1;
            modelVersionMap.put(node.getModelName(), this.version);
        }
        this.name = node.getModelName();
    }
    
    @Override
    public String toString() {
        return name + ":" + version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof BIMModel)) {
            BIMModel model = (BIMModel) obj;
            return model.getName().equals(getName()) && model.getVersion() == getVersion();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + this.version;
        return hash;
    }
    
}
