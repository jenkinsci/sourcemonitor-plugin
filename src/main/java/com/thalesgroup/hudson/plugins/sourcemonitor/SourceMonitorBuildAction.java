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

import hudson.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jenkins.tasks.SimpleBuildStep;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;


public class SourceMonitorBuildAction implements HealthReportingAction, Serializable, StaplerProxy, SimpleBuildStep.LastBuildAction {

    public static final String URL_NAME = "sourcemonitor";

    private Run<?, ?> build;
    private SourceMonitorResult result;

    public SourceMonitorBuildAction(Run<?, ?> build, SourceMonitorResult result){
        this.build = build;
        this.result = result;
    }

    public String getIconFileName() {
        return "/plugin/sourcemonitor/icons/sourcemonitor-24.png";
    }

    public String getDisplayName() {
        return "SourceMonitor Results";
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public String getSummary(){
        return SourceMonitorSummary.createReportSummary(result.getReport(), this.getPreviousReport());
    }

    public String getDetails(){
        return SourceMonitorSummary.createReportSummaryDetails(result.getReport(), this.getPreviousReport());
    }

    public SourceMonitorResult getResult(){
        return this.result;
    }

    private SourceMonitorReport getPreviousReport(){
        SourceMonitorResult previous = this.getPreviousResult();
        if(previous == null){
            return null;
        }else{
           return previous.getReport();
        }
    }

    SourceMonitorResult getPreviousResult(){
        SourceMonitorBuildAction previousAction = this.getPreviousAction();
        SourceMonitorResult previousResult = null;
        if(previousAction != null){
            previousResult = previousAction.getResult();
        }
        
        return previousResult;
    }

    SourceMonitorBuildAction getPreviousAction(){
        Run<?, ?> previousBuild = this.build.getPreviousBuild();
        if(previousBuild != null){
            return previousBuild.getAction(SourceMonitorBuildAction.class);
        }
        return null;
    }

    Run<?, ?> getBuild(){
        return this.build;
    }

    public Object getTarget() {
        return this.result;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        List<SourceMonitorProjectAction> projectActions = new ArrayList<>();
        projectActions.add(new SourceMonitorProjectAction(build.getParent()));
        return projectActions;
    }

    @Override
    public HealthReport getBuildHealth() {
        SourceMonitorReport report = result.getReport();
        int maxComplexity = Integer.parseInt(report.getSummaryMetrics().get("Complexity of Most Complex Function"));
        int maxComplexityHealth;
        double commentCoverage = Double.parseDouble(report.getSummaryMetrics().get("Percent Lines with Comments"));
        int commentCoverageHealth;
        double averageComplexity = Double.parseDouble(report.getSummaryMetrics().get("Percent Lines with Comments"));
        int averageComplexityHealth;
        int minimumHealth;
        Localizable description;

        maxComplexityHealth = calculateHealthReverse(maxComplexity, report.getMaxComplexityThresholdMinimum(), report.getMaxComplexityThresholdMaximum());
        commentCoverageHealth = calculateHealth(commentCoverage, report.getCommentCoverageThresholdMinimum(), report.getCommentCoverageThresholdMaximum());
        averageComplexityHealth = calculateHealthReverse(averageComplexity, report.getAverageComplexityThresholdMinimum(), report.getAverageComplexityThresholdMaximum());

        if ((maxComplexityHealth < commentCoverageHealth) && (maxComplexityHealth < averageComplexityHealth)) {
            minimumHealth = maxComplexityHealth;
            description = Messages._SourceMonitorBuildAction_healthReportMaxComplexityDescription(maxComplexity);
        } else if ((commentCoverageHealth < maxComplexityHealth) && (commentCoverageHealth < averageComplexityHealth)) {
            minimumHealth = commentCoverageHealth;
            description = Messages._SourceMonitorBuildAction_healthReportCommentCoverageDescription(commentCoverage);
        } else {
            minimumHealth = averageComplexityHealth;
            description = Messages._SourceMonitorBuildAction_healthReportAverageComplexityDescription(averageComplexity);
        }

        return new HealthReport(minimumHealth, description);
    }

    private int calculateHealthReverse(double value, double valueMin, double valueMax) {
        double boundedValue = value;

        // Limit the max complexity to the bounds of the thresholds
        boundedValue = Math.max(boundedValue, valueMin);
        boundedValue = Math.min(boundedValue, valueMax);

        boundedValue = ((valueMax - boundedValue) * 100) / (valueMax - valueMin);

        return (int)boundedValue;
    }

    private int calculateHealth(double value, double valueMin, double valueMax) {
        double boundedValue = value;

        // Limit the max complexity to the bounds of the thresholds
        boundedValue = Math.max(boundedValue, valueMin);
        boundedValue = Math.min(boundedValue, valueMax);

        boundedValue = ((boundedValue - valueMin) * 100) / (valueMax - valueMin);

        return (int)boundedValue;
    }
}
