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

import com.invicara.tools.rh.model.ChartNodeData;
import com.invicara.tools.rh.model.TestcaseNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Getter;

/**
 *
 * @author Alok Ranjan Meher
 */
public enum Chart {

    INSTANCE;

    @Getter
    Set<String> testcases;
    @Getter
    Map<TestcaseNode, List<ChartNodeData>> testcasesMap;

    Chart() {
        testcases = new TreeSet<>();
        testcasesMap = new TreeMap<>((TestcaseNode o1, TestcaseNode o2) -> o1.getRuleName().equals(o2.getRuleName())
                ? o1.getTestcaseName().compareTo(o2.getTestcaseName())
                : o1.getRuleName().compareTo(o2.getRuleName()));
    }
}
