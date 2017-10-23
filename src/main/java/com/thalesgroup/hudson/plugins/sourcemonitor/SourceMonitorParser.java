/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.sourcemonitor;

import com.thoughtworks.xstream.mapper.Mapper;
import hudson.AbortException;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.nashorn.internal.objects.annotations.Function;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jenkinsci.remoting.RoleChecker;

public class SourceMonitorParser implements FilePath.FileCallable<SourceMonitorReport> {
    private static final long serialVersionUID = 1L;

    private int maxComplexityThresholdMaximum = 0;
    private int maxComplexityThresholdMinimum = 0;
    private double averageComplexityThresholdMaximum = 0;
    private double averageComplexityThresholdMinimum = 0;
    private int commentCoverageThresholdMaximum = 0;
    private int commentCoverageThresholdMinimum = 0;
    private final FilePath summaryFilePath;
    private final FilePath detailsFilePath;
    private int maxStatementsThresholdMaximum = 0;
    private int maxStatementsThresholdMinimum = 0;
    private static final Logger LOGGER = Logger.getLogger(SourceMonitorParser.class.getName());

    public SourceMonitorParser(FilePath summaryFilePath) {
        this.summaryFilePath = summaryFilePath;
        this.detailsFilePath = null;
    }

    public SourceMonitorParser(){
        this.summaryFilePath = null;
        this.detailsFilePath = null;
    }

    public SourceMonitorParser(FilePath summaryFilePath, FilePath detailsFilePath){
        this.summaryFilePath = summaryFilePath;
        this.detailsFilePath = detailsFilePath;
    }

    public SourceMonitorReport invoke(java.io.File workspace, VirtualChannel channel) throws IOException {

        SourceMonitorReport sourceMonitorReport = new SourceMonitorReport();

        if (detailsFilePath != null){
            parseDetailsFile(sourceMonitorReport);
        }

        parseSummaryFile(sourceMonitorReport);
        setHealthParameters(sourceMonitorReport);

        return sourceMonitorReport;
    }

    public void setMaxStatementsThresholdMaximum(int maxStatementsThresholdMaximum) {
        this.maxStatementsThresholdMaximum = maxStatementsThresholdMaximum;
    }

    public void setMaxStatementsThresholdMinimum(int maxStatementsThresholdMinimum) {
        this.maxStatementsThresholdMinimum = maxStatementsThresholdMinimum;
    }

    /** Parsing Helper Methods */

    private void parseDetailsFile(SourceMonitorReport sourceMonitorReport) throws IOException{
        Document document;

        try {
            SAXBuilder sxb = new SAXBuilder();
            document = sxb.build(new InputStreamReader(new FileInputStream(new File(detailsFilePath.toURI())), "UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Parsing file error :" + e.toString());
            throw new AbortException("Parsing file error");
        }

        ArrayList<FileStats> detailsFileOutput = new ArrayList<FileStats>();

        Element projectElt = getProject(document);

        sourceMonitorReport.setSourceFilePath(projectElt.getChild("project_directory").getValue().replace('\\','/'));

        List<?> checkpointsEltList = getCheckpointsList(projectElt);

        for (int i = 0; i < checkpointsEltList.size(); i++) {
            Element checkpoint = (Element) checkpointsEltList.get(i);
            Element files = checkpoint.getChild("files");
            List<?> fileEltList = files.getChildren("file");

            int numFiles = Integer.parseInt(files.getAttributeValue("file_count"));

            for (int j = 0; j < numFiles; j++) {
                FileStats newFile = getFileStats(sourceMonitorReport, (Element) fileEltList.get(j));
                if (newFile != null){
                    detailsFileOutput.add(newFile);
                }
            }
        }

        sourceMonitorReport.setDetailsFileOutput(detailsFileOutput);
    }

    private void parseSummaryFile(SourceMonitorReport sourceMonitorReport) throws IOException{
        Document document;

        try {
            SAXBuilder sxb2 = new SAXBuilder();
            document = sxb2.build(new InputStreamReader(new FileInputStream(new File(summaryFilePath.toURI())), "UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Parsing file error :" + e.toString());
            throw new AbortException("Parsing file error");
        }

        Map<String, String> metricsSummaryMap = new HashMap<String, String>();
        ArrayList<FunctionStats> detailedMetrics = new ArrayList<FunctionStats>();

        Element projectElt = getProject(document);

        Map<String, String> metricNameMap = buildMetricNameMap(projectElt);

        List<?> checkpointsEltList = getCheckpointsList(projectElt);
        for (int i = 0; i < checkpointsEltList.size(); i++) {
            Element checkpoint = (Element) checkpointsEltList.get(i);
            populateSummaryMap(checkpoint, metricsSummaryMap, metricNameMap);
            populateDetailList(checkpoint, detailedMetrics);
        }

        sourceMonitorReport.setSummaryMetrics(metricsSummaryMap);
        sourceMonitorReport.setDetailedMetrics(detailedMetrics);
    }

    private void setHealthParameters(SourceMonitorReport sourceMonitorReport) {
        // Set the parameters for the health metrics.
        sourceMonitorReport.setAverageComplexityThresholdMaximum(averageComplexityThresholdMaximum);
        sourceMonitorReport.setAverageComplexityThresholdMinimum(averageComplexityThresholdMinimum);
        sourceMonitorReport.setCommentCoverageThresholdMaximum(commentCoverageThresholdMaximum);
        sourceMonitorReport.setCommentCoverageThresholdMinimum(commentCoverageThresholdMinimum);
        sourceMonitorReport.setMaxComplexityThresholdMaximum(maxComplexityThresholdMaximum);
        sourceMonitorReport.setMaxComplexityThresholdMinimum(maxComplexityThresholdMinimum);
        sourceMonitorReport.setMaxStatementsThresholdMaximum(maxStatementsThresholdMaximum);
        sourceMonitorReport.setMaxStatementsThresholdMinimum(maxStatementsThresholdMinimum);
    }

    private FileStats getFileStats(SourceMonitorReport report, Element fileElt){
        FileStats newFile = new FileStats(report);

        newFile.setFileName(fileElt.getAttributeValue("file_name").replace('\\','/'));

        Element functionMetricsElt = fileElt.getChild("function_metrics");
        Element metricsElt = fileElt.getChild("metrics");

        populateFileMetrics(newFile, metricsElt);

        if (newFile.getNumFunctions() != 0){
            populateFileFunctions(newFile, functionMetricsElt);
        }
        else{
            newFile = null;
        }

        return newFile;
    }

    private void populateFileMetrics(FileStats newFile, Element metricsElt){

        List<?>  metricEltList = metricsElt.getChildren("metric");

        // 9th metric is max complexity
        Element metricElt = (Element)metricEltList.get(8);
        newFile.setMaxComplexity(Integer.parseInt(metricElt.getValue()));

        // 2nd metric is number of statements
        metricElt = (Element)metricEltList.get(1);
        newFile.setNumStatements(Integer.parseInt(metricElt.getValue()));

        // 5th metric is number of functions
        metricElt = (Element)metricEltList.get(4);
        newFile.setNumFunctions(Integer.parseInt(metricElt.getValue()));
    }

    private void populateFileFunctions(FileStats newFile, Element functionMetricsElt){

        List<?> functionEltList = functionMetricsElt.getChildren("function");

        for (int i=0; i<functionEltList.size(); i++){
            Element function = (Element)functionEltList.get(i);
            newFile.addFunction(getFunctionStats(function));
        }
    }

    private FunctionStats getFunctionStats(Element function) {
        int complexity = Integer.parseInt(function.getChild("complexity").getValue());
        int statements = Integer.parseInt(function.getChild("statements").getValue());
        String name = function.getAttributeValue("name");
        FunctionStats functionDetails = new FunctionStats(complexity, statements, name);

        return functionDetails;
    }

    private Element getProject(Document document) {
        Element root = document.getRootElement();
        return root.getChild("project");
    }

    private Map<String,String> buildMetricNameMap(Element project) {
        Map<String,String> metricNameMap = new HashMap<String, String>();

        // Parse the Metric Names.
        Element metricNames = project.getChild("metric_names");
        List<?> metricNamesEltList = metricNames.getChildren();
        for (int i = 0; i < metricNamesEltList.size(); i++) {
            Element metricNameElt = (Element) metricNamesEltList.get(i);
            metricNameMap.put(metricNameElt.getAttributeValue("id"), metricNameElt.getValue());
        }

        return metricNameMap;
    }

    private List<?> getCheckpointsList(Element project) {
        Element checkpoints = project.getChild("checkpoints");
        List<?> checkpointsList = checkpoints.getChildren();
        return checkpointsList;
    }

    private void populateSummaryMap(Element checkpoint, Map<String, String> metricsSummaryMap, Map<String, String> metricNameMap) {
        Element metricsElt = checkpoint.getChild("metrics");
        List<?> metricsEltList = metricsElt.getChildren();
        for (int i = 0; i < metricsEltList.size(); i++) {
            Element metricElt = (Element) metricsEltList.get(i);
            metricsSummaryMap.put(metricNameMap.get(metricElt.getAttributeValue("id")), metricElt.getValue());
        }
    }

    private void populateDetailList(Element checkpoint, List<FunctionStats> detailedMetrics) {
        Element functionMetrics = checkpoint.getChild("function_metrics");
        List<?> functionMetricsEltList = functionMetrics.getChildren("function");
        int numFunctions = Integer.parseInt(functionMetrics.getAttributeValue("function_count"));

        for (int i = 0; i < numFunctions; i++) {
            detailedMetrics.add(getFunctionStats((Element) functionMetricsEltList.get(i)));
        }
    }

    /** Getters and Setters */

    public FilePath getSummaryFilePath() {
        return summaryFilePath;
    }

    public FilePath getDetailsFilePath() {
        return detailsFilePath;
    }

    public void setMaxComplexityThresholdMaximum(int maxComplexityThresholdMaximum) {
        this.maxComplexityThresholdMaximum = maxComplexityThresholdMaximum;
    }

    public void setMaxComplexityThresholdMinimum(int maxComplexityThresholdMinimum) {
        this.maxComplexityThresholdMinimum = maxComplexityThresholdMinimum;
    }

    public void setAverageComplexityThresholdMaximum(double averageComplexityThresholdMaximum) {
        this.averageComplexityThresholdMaximum = averageComplexityThresholdMaximum;
    }

    public void setAverageComplexityThresholdMinimum(double averageComplexityThresholdMinimum) {
        this.averageComplexityThresholdMinimum = averageComplexityThresholdMinimum;
    }

    public void setCommentCoverageThresholdMaximum(int commentCoverageThresholdMaximum) {
        this.commentCoverageThresholdMaximum = commentCoverageThresholdMaximum;
    }

    public void setCommentCoverageThresholdMinimum(int commentCoverageThresholdMinimum) {
        this.commentCoverageThresholdMinimum = commentCoverageThresholdMinimum;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
    }
}
