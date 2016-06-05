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

import static com.invicara.tools.rh.utils.JSONConstants.*;
import com.invicara.tools.rh.utils.NodeType;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class TestcaseNode {

    private SimpleStringProperty defaultActualArgumentProperty, schemaProperty, descriptorProperty,
            actualArgumentProperty, ruleNameProperty, testcaseNameProperty,
            expectedOutputProperty, modelNameProperty;
    private SimpleLongProperty workflowRunTimeProperty, ruleExecutionTimeProperty, runtimeProperty;
    private SimpleObjectProperty<TestcaseNode> parentRuleProperty;
    private SimpleObjectProperty<NodeType> nodeTypeProperty;
    @Setter
    @Getter
    private Set<TestcaseNode> children;
    private SimpleBooleanProperty executionStatusProperty, executionResultProperty;

    public TestcaseNode() {

        actualArgumentProperty = new SimpleStringProperty();
        ruleNameProperty = new SimpleStringProperty();
        testcaseNameProperty = new SimpleStringProperty();
        expectedOutputProperty = new SimpleStringProperty();
        modelNameProperty = new SimpleStringProperty();

        workflowRunTimeProperty = new SimpleLongProperty(0);
        ruleExecutionTimeProperty = new SimpleLongProperty(0);
        runtimeProperty = new SimpleLongProperty(0);

        parentRuleProperty = new SimpleObjectProperty<>();
        nodeTypeProperty = new SimpleObjectProperty<>();

        defaultActualArgumentProperty = new SimpleStringProperty();
        schemaProperty = new SimpleStringProperty();
        descriptorProperty = new SimpleStringProperty();
        
        executionStatusProperty = new SimpleBooleanProperty(false);
        executionResultProperty = new SimpleBooleanProperty(false);

        children = new TreeSet<>((TestcaseNode o1, TestcaseNode o2) -> {
            if (o1.getNodeType() == o2.getNodeType()) {
                if (o1.getNodeType() == NodeType.RULE) {
                    return o1.getRuleName().compareTo(o2.getRuleName());
                }
                if (o1.getNodeType() == NodeType.TESTCASE) {
                    return o1.getTestcaseName().compareTo(o2.getTestcaseName());
                }
            }
            return 0;
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof TestcaseNode) {
                TestcaseNode node = (TestcaseNode) obj;
                if (getRuleName() != null && node.getRuleName() != null) {
                    if (getRuleName().equals(node.getRuleName())) {
                        if (getTestcaseName() != null && node.getTestcaseName() != null) {
                            if (getTestcaseName().equals(node.getTestcaseName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.ruleNameProperty);
        hash = 83 * hash + Objects.hashCode(this.testcaseNameProperty);
        return hash;
    }

    public JSONObject generateTestcaseResult() {
        JSONObject data = new JSONObject();
        data.put(MODEL_NAME, modelNameProperty.get());
        data.put(RULE_NAME, ruleNameProperty.get());
        data.put(TESTCASE_NAME, testcaseNameProperty.get());
        data.put(ACTUAL_ARGUMENT, new JSONObject(actualArgumentProperty.get()));
        data.put(EXPECTED_OUTPUT, new JSONObject(expectedOutputProperty.get() == null
                ? "{}" : expectedOutputProperty.get()));
        data.put(WORKFLOW_RUNTIME, workflowRunTimeProperty.get());
        data.put(RULE_RUNTIME, ruleExecutionTimeProperty.get());
        data.put(RUNTIME, runtimeProperty.get());
        return data;
    }

    public JSONObject generateReport() {
        JSONObject data = new JSONObject();
        data.put(MODEL_NAME, modelNameProperty.get());
        data.put(RULE_NAME, ruleNameProperty.get());
        data.put(TESTCASE_NAME, testcaseNameProperty.get());
        data.put(ACTUAL_ARGUMENT, new JSONObject(actualArgumentProperty.get()));
        data.put(EXPECTED_OUTPUT, new JSONObject(expectedOutputProperty.get() == null
                ? "{}" : expectedOutputProperty.get()));
        data.put(WORKFLOW_RUNTIME, workflowRunTimeProperty.get());
        data.put(RULE_RUNTIME, ruleExecutionTimeProperty.get());
        data.put(RUNTIME, runtimeProperty.get());
        return data;
    }

    public void jsonToString() {
        System.out.println(generateTestcaseResult().toString());
    }

    @Override
    public String toString() {
        return testcaseNameProperty.get() == null ? 
                (ruleNameProperty.get() == null ? modelNameProperty.get(): ruleNameProperty.get()) 
                : testcaseNameProperty.get();
    }

    public boolean getExecutionResult() {
        return executionResultProperty.get();
    }

    public void setExecutionResult(boolean executionResultProperty) {
        this.executionResultProperty.set(executionResultProperty);
    }
    
    public boolean getExecutionStatus() {
        return executionStatusProperty.get();
    }

    public void setExecutionStatus(boolean executionStatusProperty) {
        this.executionStatusProperty.set(executionStatusProperty);
    }
    
    public NodeType getNodeType() {
        return nodeTypeProperty.get();
    }

    public void setNodeType(NodeType nodeType) {
        nodeTypeProperty.setValue(nodeType);
    }

    public String getRuleName() {
        return ruleNameProperty.get();
    }

    public void setRuleName(String ruleName) {
        ruleNameProperty.set(ruleName);
    }

    public String getTestcaseName() {
        return testcaseNameProperty.get();
    }

    public void setTestcaseName(String testcaseName) {
        testcaseNameProperty.set(testcaseName);
    }

    public String getExpectedOutput() {
        return expectedOutputProperty.get();
    }

    public void setExpectedOutput(String expectedOutput) {
        expectedOutputProperty.set(expectedOutput);
    }

    public String getModelName() {
        return modelNameProperty.get();
    }

    public void setModelName(String modelName) {
        modelNameProperty.set(modelName);
    }

    public TestcaseNode getParentRule() {
        return parentRuleProperty.get();
    }

    public void setParentRule(TestcaseNode parentRule) {
        parentRuleProperty.set(parentRule);
    }

    public long getRuleExecutionTime() {
        return ruleExecutionTimeProperty.get();
    }

    public void setRuleExecutionTime(long ruleExecutionTime) {
        ruleExecutionTimeProperty.set(ruleExecutionTime);
    }

    public long getWorkflowRunTime() {
        return workflowRunTimeProperty.get();
    }

    public void setWorkflowRunTime(long workflowRunTime) {
        workflowRunTimeProperty.set(workflowRunTime);
    }

    public String getActualArgument() {
        return actualArgumentProperty.get();
    }

    public void setActualArgument(String actualArgument) {
        actualArgumentProperty.set(actualArgument);
    }

    public SimpleStringProperty getActualArgumentProperty() {
        return actualArgumentProperty;
    }

    public void setActualArgumentProperty(SimpleStringProperty actualArgumentProperty) {
        this.actualArgumentProperty = actualArgumentProperty;
    }

    public SimpleStringProperty getRuleNameProperty() {
        return ruleNameProperty;
    }

    public void setRuleNameProperty(SimpleStringProperty ruleNameProperty) {
        this.ruleNameProperty = ruleNameProperty;
    }

    public SimpleStringProperty getTestcaseNameProperty() {
        return testcaseNameProperty;
    }

    public void setTestcaseNameProperty(SimpleStringProperty testcaseNameProperty) {
        this.testcaseNameProperty = testcaseNameProperty;
    }

    public SimpleStringProperty getExpectedOutputProperty() {
        return expectedOutputProperty;
    }

    public void setExpectedOutputProperty(SimpleStringProperty expectedOutputProperty) {
        this.expectedOutputProperty = expectedOutputProperty;
    }

    public SimpleStringProperty getModelNameProperty() {
        return modelNameProperty;
    }

    public void setModelNameProperty(SimpleStringProperty modelNameProperty) {
        this.modelNameProperty = modelNameProperty;
    }

    public SimpleLongProperty getWorkflowRunTimeProperty() {
        return workflowRunTimeProperty;
    }

    public void setWorkflowRunTimeProperty(SimpleLongProperty workflowRunTimeProperty) {
        this.workflowRunTimeProperty = workflowRunTimeProperty;
    }

    public SimpleLongProperty getRuleExecutionTimeProperty() {
        return ruleExecutionTimeProperty;
    }

    public void setRuleExecutionTimeProperty(SimpleLongProperty ruleExecutionTimeProperty) {
        this.ruleExecutionTimeProperty = ruleExecutionTimeProperty;
    }

    public SimpleObjectProperty<NodeType> getNodeTypeProperty() {
        return nodeTypeProperty;
    }

    public void setNodeTypeProperty(SimpleObjectProperty<NodeType> nodeTypeProperty) {
        this.nodeTypeProperty = nodeTypeProperty;
    }

    public String getSchema() {
        return schemaProperty.get();
    }

    public void setSchema(String schema) {
        schemaProperty.set(schema);
    }

    public String getDescriptor() {
        return descriptorProperty.get();
    }

    public void setDescriptor(String descriptor) {
        descriptorProperty.set(descriptor);
    }

    public String getDefaultActualArgument() {
        return defaultActualArgumentProperty.get();
    }

    public void setDefaultActualArgument(String defaultActualArgument) {
        defaultActualArgumentProperty.set(defaultActualArgument);
    }

    public SimpleStringProperty getDefaultActualArgumentProperty() {
        return defaultActualArgumentProperty;
    }

    public void setDefaultActualArgumentProperty(SimpleStringProperty defaultActualArgumentProperty) {
        this.defaultActualArgumentProperty = defaultActualArgumentProperty;
    }

    public SimpleStringProperty getSchemaProperty() {
        return schemaProperty;
    }

    public void setSchemaProperty(SimpleStringProperty schemaProperty) {
        this.schemaProperty = schemaProperty;
    }

    public SimpleStringProperty getDescriptorProperty() {
        return descriptorProperty;
    }

    public void setDescriptorProperty(SimpleStringProperty descriptorProperty) {
        this.descriptorProperty = descriptorProperty;
    }

    public SimpleObjectProperty<TestcaseNode> getParentRuleProperty() {
        return parentRuleProperty;
    }

    public void setParentRuleProperty(SimpleObjectProperty<TestcaseNode> parentRuleProperty) {
        this.parentRuleProperty = parentRuleProperty;
    }

    public long getRuntime() {
        return runtimeProperty.get();
    }

    public void setRuntime(long time) {
        this.runtimeProperty.set(time);
    }

    public SimpleLongProperty getRuntimeProperty() {
        return runtimeProperty;
    }

    public void setRuntimeProperty(SimpleLongProperty runtimeProperty) {
        this.runtimeProperty = runtimeProperty;
    }

}
