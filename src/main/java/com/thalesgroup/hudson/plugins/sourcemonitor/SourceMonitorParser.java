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
    private FilePath resultFilePath;
    private static final Logger LOGGER = Logger.getLogger(SourceMonitorParser.class.getName());

    public SourceMonitorParser() {
        resultFilePath = null;
    }

    public SourceMonitorParser(FilePath resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public SourceMonitorReport invoke(java.io.File workspace, VirtualChannel channel) throws IOException {

        SourceMonitorReport sourceMonitorReport = new SourceMonitorReport();

        Document document;

        try {
            SAXBuilder sxb = new SAXBuilder();
            document = sxb.build(new InputStreamReader(new FileInputStream(new File(resultFilePath.toURI())), "UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Parsing file error :" + e.toString());
            throw new AbortException("Parsing file error");
        }

        Map<String, String> metricsSummaryMap = new HashMap<String, String>();
        Map<String, String> metricNameMap = new HashMap<String, String>();
        ArrayList<FunctionStats> detailedMetrics = new ArrayList<FunctionStats>();

        Element projectElt = getProject(document);
        setMetricNamesList(projectElt, metricNameMap);

        List<?> checkpointsEltList = getCheckpointsList(projectElt);
        for (int i = 0; i < checkpointsEltList.size(); i++) {
            Element checkpoint = (Element) checkpointsEltList.get(i);
            setSummaryMap(checkpoint, metricsSummaryMap, metricNameMap);
            setDetailList(checkpoint, detailedMetrics);
        }

        sourceMonitorReport.setSummaryMetrics(metricsSummaryMap);
        sourceMonitorReport.setDetailedMetrics(detailedMetrics);
        setHealthParameters(sourceMonitorReport);

        return sourceMonitorReport;
    }

    private void setHealthParameters(SourceMonitorReport sourceMonitorReport) {
        // Set the parameters for the health metrics.
        sourceMonitorReport.setAverageComplexityThresholdMaximum(averageComplexityThresholdMaximum);
        sourceMonitorReport.setAverageComplexityThresholdMinimum(averageComplexityThresholdMinimum);
        sourceMonitorReport.setCommentCoverageThresholdMaximum(commentCoverageThresholdMaximum);
        sourceMonitorReport.setCommentCoverageThresholdMinimum(commentCoverageThresholdMinimum);
        sourceMonitorReport.setMaxComplexityThresholdMaximum(maxComplexityThresholdMaximum);
        sourceMonitorReport.setMaxComplexityThresholdMinimum(maxComplexityThresholdMinimum);
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

    private void setMetricNamesList(Element project, Map<String, String> metricNameMap) {
        // Parse the Metric Names.
        Element metricNames = project.getChild("metric_names");
        List<?> metricNamesEltList = metricNames.getChildren();
        for (int i = 0; i < metricNamesEltList.size(); i++) {
            Element metricNameElt = (Element) metricNamesEltList.get(i);
            metricNameMap.put(metricNameElt.getAttributeValue("id"), metricNameElt.getValue());
        }
    }

    private List<?> getCheckpointsList(Element project) {
        Element checkpoints = project.getChild("checkpoints");
        List<?> checkpointsList = checkpoints.getChildren();
        return checkpointsList;
    }

    private void setSummaryMap(Element checkpoint, Map<String, String> metricsSummaryMap, Map<String, String> metricNameMap) {
        Element metricsElt = checkpoint.getChild("metrics");
        List<?> metricsEltList = metricsElt.getChildren();
        for (int i = 0; i < metricsEltList.size(); i++) {
            Element metricElt = (Element) metricsEltList.get(i);
            metricsSummaryMap.put(metricNameMap.get(metricElt.getAttributeValue("id")), metricElt.getValue());
        }
    }

    private void setDetailList(Element checkpoint, List<FunctionStats> detailedMetrics) {
        Element functionMetrics = checkpoint.getChild("function_metrics");
        List<?> functionMetricsEltList = functionMetrics.getChildren("function");
        int numFunctions = Integer.parseInt(functionMetrics.getAttributeValue("function_count"));

        for (int i = 0; i < numFunctions; i++) {
            detailedMetrics.add(getFunctionStats((Element) functionMetricsEltList.get(i)));
        }
    }

    public FilePath getResultFilePath() {
        return resultFilePath;
    }

    public void setResultFilePath(FilePath resultFilePath) {
        this.resultFilePath = resultFilePath;
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
