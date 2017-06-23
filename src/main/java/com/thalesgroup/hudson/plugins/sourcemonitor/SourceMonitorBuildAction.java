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

import hudson.model.AbstractBuild;
import hudson.model.Action;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;


public class SourceMonitorBuildAction implements Action, Serializable, StaplerProxy, SimpleBuildStep.LastBuildAction {

    public static final String URL_NAME = "sourcemonitor";

    private Run<?, ?> build;
    private SourceMonitorResult result;
    private List<SourceMonitorProjectAction> projectActions;

    public SourceMonitorBuildAction(Run<?, ?> build, SourceMonitorResult result){
        this.build = build;
        this.result = result;

        List<SourceMonitorProjectAction> projectActions = new ArrayList<>();
        projectActions.add(new SourceMonitorProjectAction(build.getParent()));
        this.projectActions = projectActions;
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
        return this.projectActions;
    }
}
