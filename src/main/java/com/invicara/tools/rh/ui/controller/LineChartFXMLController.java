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
package com.invicara.tools.rh.ui.controller;

import com.invicara.tools.rh.configuration.Chart;
import com.invicara.tools.rh.configuration.FileFormat;
import com.invicara.tools.rh.configuration.FileType;
import com.invicara.tools.rh.configuration.Model;
import com.invicara.tools.rh.service.AllRulesExcelJSONManagement;
import com.invicara.tools.rh.utils.ControllerUtilities;
import com.invicara.tools.rh.model.BIMModel;
import com.invicara.tools.rh.model.ChartNodeData;
import com.invicara.tools.rh.model.HtmlData;
import com.invicara.tools.rh.model.TestcaseNode;
import com.invicara.tools.rh.ui.DisplayPanel;
import com.invicara.tools.rh.utils.RulesHardeningHelper;
import com.invicara.tools.rh.utils.UIUtils;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javax.imageio.ImageIO;
import lombok.extern.java.Log;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Alok Ranjan Meher
 */
@Log
public class LineChartFXMLController implements Initializable {

    @FXML
    private HBox modelPanel;

    @FXML
    private LineChart lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button exportPdf, exportExcel, exportImage, exportHtml;

    private final Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Image imagePdf = new Image(getClass().getClassLoader().getResourceAsStream("images/pdf.png"));
        exportPdf.setGraphic(new ImageView(imagePdf));
        exportPdf.setTooltip(new Tooltip("Generate Report in PDF format"));

        Image imageExcel = new Image(getClass().getClassLoader().getResourceAsStream("images/excel.png"));
        exportExcel.setGraphic(new ImageView(imageExcel));
        exportExcel.setTooltip(new Tooltip("Generate Report in Excel format"));

        Image imagePicture = new Image(getClass().getClassLoader().getResourceAsStream("images/image.png"));
        exportImage.setGraphic(new ImageView(imagePicture));
        exportImage.setTooltip(new Tooltip("Generate Report in Image format"));

        Image imageHtml = new Image(getClass().getClassLoader().getResourceAsStream("images/html.png"));
        exportHtml.setGraphic(new ImageView(imageHtml));
        exportHtml.setTooltip(new Tooltip("Generate Report in HTML format"));

        uploadModelData();
        uploadModelList();

        ControllerUtilities.INSTANCE.addController(LineChartFXMLController.class, this);
    }

    private void uploadModelList() {
        Model.INSTANCE.getListOfModel().stream().map((BIMModel root) -> {
            final CheckBox checkBox = new CheckBox(root.toString());
            checkBox.setId(root.toString());
            checkBox.setSelected(true);
            checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                XYChart.Series<String, Number> series = seriesMap.get(checkBox.getId());
                if (checkBox.isSelected()) {
                    if (!lineChart.getData().contains(series)) {
                        lineChart.getData().add(series);
                    }
                    UIUtils.updateConsole("Model " + series.getName() + " is added.");
                } else {
                    if (lineChart.getData().size() > 1) {
                        lineChart.getData().remove(series);
                        UIUtils.updateConsole("Model " + series.getName() + " is removed.");
                    } else {
                        Platform.runLater(() -> {
                            checkBox.setSelected(true);
                            UIUtils.showWarningWindow("Number of selected Model should not be zero");
                        });
                        UIUtils.updateConsole("Number of selected Model should not be zero");
                    }
                }
            });
            return checkBox;
        }).forEach((checkBox) -> {
            modelPanel.getChildren().add(checkBox);
        });
    }

    private void uploadModelData() {
        int ruleCount, index = 0;
        String preName, name;
        Set<TestcaseNode> testcaseSet = new TreeSet<>((TestcaseNode o1, TestcaseNode o2) -> o1.getRuleName().equals(o2.getRuleName())
                ? o1.getTestcaseName().compareTo(o2.getTestcaseName())
                : o1.getRuleName().compareTo(o2.getRuleName()));

        testcaseSet.addAll(Chart.INSTANCE.getTestcasesMap().keySet());

        Map<TestcaseNode, List<ChartNodeData>> map = Chart.INSTANCE.getTestcasesMap();
        XYChart.Data data;
        for (BIMModel model : Model.INSTANCE.getListOfModel()) {
            final ObservableList<XYChart.Data<String, Number>> dataset = FXCollections.observableArrayList();
            ruleCount = 0;
            preName = null;
            for (TestcaseNode testcase : testcaseSet) {
                name = testcase.getRuleName();
                if (preName == null) {
                    ruleCount = 1;
                    preName = name;
                } else if (!preName.equals(name)) {
                    ruleCount++;
                    preName = name;
                }
                ChartNodeData chartData = map.get(testcase).get(index);
                TestcaseNode nodeData = chartData.getNode();

                if (nodeData == null) {
                    data = createDisplayPanel(ruleCount + ":" + testcase.getTestcaseName(), 0, testcase, model);
                } else {
                    data = createDisplayPanel(ruleCount + ":" + testcase.getTestcaseName(), nodeData.getWorkflowRunTime(), nodeData, model);
                }
                dataset.add(data);
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(model.toString());
            series.setData(dataset);
            series.getData().sort((XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) -> {
                String value1 = o1.getXValue();
                int ruleIndex1 = Integer.parseInt(value1.substring(0, value1.indexOf(":")));
                String value2 = o2.getXValue();
                int ruleIndex2 = Integer.parseInt(value2.substring(0, value2.indexOf(":")));
                if (ruleIndex1 == ruleIndex2) {
                    String testcase1 = value1.substring(value1.indexOf(":") + 1);
                    String testcase2 = value2.substring(value2.indexOf(":") + 1);
                    return testcase1.compareTo(testcase2);
                }
                return ruleIndex1 < ruleIndex2 ? -1 : 1;
            });
            lineChart.getData().add(series);
            seriesMap.put(series.getName(), series);
            index++;
        }
    }

    private XYChart.Data createDisplayPanel(String name, long time, TestcaseNode nodeData, BIMModel model) {
        XYChart.Data data = new XYChart.Data(name, time);
        nodeData.setModelName(model.toString());
        data.setNode(new DisplayPanel(nodeData));
        return data;
    }

    @FXML
    public void exportAsPdf(ActionEvent event) {
    }

    @FXML
    public void exportAsHtml(ActionEvent event) {
        Configuration cfg = new Configuration(new Version("2.3.23"));

        cfg.setClassForTemplateLoading(LineChartFXMLController.class, "/templates");

        cfg.setIncompatibleImprovements(new Version(2, 3, 23));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        Map<String, Object> input = new HashMap<>();

        input.put("title", "Model Report");
        input.put("time", RulesHardeningHelper.getTimeAsName());

        List<String> models = new ArrayList<>();
        List<HtmlData> modelData = new ArrayList<>();
        Model.INSTANCE.getListOfModel().stream().forEach((model) -> {
            models.add(model.toString());
        });
        input.put("models", models);

        List<Long> list;
        int counter = 1;
        int modelCount = Model.INSTANCE.getListOfModel().size(), index;
        for (Map.Entry<TestcaseNode, List<ChartNodeData>> entry : Chart.INSTANCE.getTestcasesMap().entrySet()) {
            HtmlData data = new HtmlData();

            data.setSeriaNo(counter);
            data.setRuleName(entry.getKey().getRuleName());
            data.setTestcaseName(entry.getKey().getTestcaseName());
            list = new ArrayList<>();
            index = 0;
            for (ChartNodeData value : entry.getValue()) {
                if (index == modelCount) {
                    break;
                }
                if (value.getNode() == null) {
                    list.add(0L);
                } else {
                    list.add(value.getNode().getWorkflowRunTime());
                }
                index++;
            }
            data.setWorkflowTime(list);
            modelData.add(data);
            counter++;
        }
        input.put("rows", modelData);

        Template template;
        try {
            template = cfg.getTemplate("htmlReport.ftl");

            String fileName = "Report_" + RulesHardeningHelper.getTimeAsName() + ".html";
            String filePath = FileFormat.HTML.getFilePath() + File.separator + fileName;
            try (Writer fileWriter = new FileWriter(new File(filePath))) {
                template.process(input, fileWriter);
                UIUtils.showInfoWindow("HTML file " + fileName + " is created at location : " + filePath);
            }
        } catch (MalformedTemplateNameException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.showWarningWindow("Failed to create the HTML file.");
        } catch (ParseException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.showWarningWindow("Failed to create the HTML file.");
        } catch (IOException | TemplateException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            UIUtils.showWarningWindow("Failed to create the HTML file.");
        }
    }

    @FXML
    public void exportAsExcel(ActionEvent event) {
        AllRulesExcelJSONManagement mgmt = new AllRulesExcelJSONManagement();
        String fileName = "Report_" + RulesHardeningHelper.getTimeAsName() + ".xlsx";
        String filePath = FileFormat.HTML.getFilePath() + File.separator + fileName;
        Workbook workbook;
        try {
            workbook = mgmt.openExcelFile(filePath);
            Sheet sheet = workbook.createSheet("testcaseData");
            final Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Seril No.");
            cell = row.createCell(1);
            cell.setCellValue("Rule Name");
            cell = row.createCell(2);
            cell.setCellValue("Testcase Name");
            AtomicInteger index = new AtomicInteger(3);
            Model.INSTANCE.getListOfModel().stream().forEach((BIMModel model) -> {
                Cell icell = row.createCell(index.incrementAndGet());
                icell.setCellValue(model.getName() + ":" + model.getVersion());
            });

            AtomicInteger rowIndex = new AtomicInteger(0);
            Chart.INSTANCE.getTestcasesMap().entrySet().stream().forEach((entry) -> {
                final Row row1 = sheet.createRow(rowIndex.incrementAndGet());
                Cell cell1 = row1.createCell(0);
                cell1.setCellValue(rowIndex.get());
                cell1 = row1.createCell(1);
                TestcaseNode node = entry.getKey();
                cell1.setCellValue(node.getRuleName());
                cell1 = row1.createCell(2);
                cell1.setCellValue(node.getTestcaseName());
                int mindex = 3;
                for (ChartNodeData value : entry.getValue()) {
                    cell1 = row1.createCell(mindex++);
                    cell1.setCellValue(value.getNode().getModelName());
                }
            });

            mgmt.saveAsNewExcelFile(filePath, workbook);
        } catch (IOException | InvalidFormatException ex) {
            Logger.getLogger(LineChartFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void exportAsImage(ActionEvent event) {
        WritableImage wim = new WritableImage((int) lineChart.getWidth(), (int) lineChart.getHeight());
        lineChart.snapshot(new SnapshotParameters(), wim);
        String filePath = FileFormat.IMAGE.getFilePath() + File.separator + "Report_" + RulesHardeningHelper.getTimeAsName() + FileType.IMAGE.getExtension();
        File file = new File(filePath);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wim, null), FileType.IMAGE.getExtension(), file);

            UIUtils.showInfoWindow("Image is created and saved at location : " + filePath);
        } catch (Exception e) {
            UIUtils.showWarningWindow("Failed to create save the Image.");
        }
    }
}
