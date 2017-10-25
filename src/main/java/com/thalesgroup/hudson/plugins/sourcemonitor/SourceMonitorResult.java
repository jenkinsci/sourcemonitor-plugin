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
import hudson.model.ModelObject;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class SourceMonitorResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private SourceMonitorReport report;
    private Run owner;
    private Map<String, FileStats> urlKeys = new HashMap<String, FileStats>();

    public SourceMonitorResult(SourceMonitorReport report, Run<?, ?> owner){
        this.report = report;
        report.setParentFile(owner.getParent().getRootDir());
        this.owner = owner;

        ArrayList<FileStats> files = report.getDetailsFileOutput();

        for (int i=0; i<files.size(); i++){
            urlKeys.put(files.get(i).getUrlTransform().toLowerCase(),files.get(i));
        }
    }

    public SourceMonitorReport getReport(){
        return report;
    }

    public Run<?, ?> getOwner(){
        return owner;
    }

    public String getDetailedMetrics(){
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<report.getDetailedMetrics().size();i++){
            builder.append("<tr><td>");
            builder.append(report.getDetailedMetrics().get(i).getFunction());
            builder.append("</td><td>");
            builder.append(report.getDetailedMetrics().get(i).getComplexity());
            builder.append("</td><td>");
            builder.append(report.getDetailedMetrics().get(i).getStatements());
            builder.append("</td></tr>");
        }
        return builder.toString();
    }

    public String getDetailsFileOutput(){
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<report.getDetailsFileOutput().size();i++){
            String str1 = "<tr><td><a href = \"#ref"+i+"\">";
            builder.append(str1);
            builder.append(report.getDetailsFileOutput().get(i).getFileName());
            builder.append("</a></td><td>");
            builder.append(report.getDetailsFileOutput().get(i).getNumFunctions());
            builder.append("</td><td>");
            builder.append(report.getDetailsFileOutput().get(i).getNumStatements());
            builder.append("</td>");

            int maxState = report.getDetailsFileOutput().get(i).getMaxStatements();
            int paramHealth = report.getStateHealth(maxState);

            if (paramHealth > 80){
                str1 = "<td style=\"color:darkgreen\">"+maxState;
                builder.append(str1);
            }
            else if(paramHealth> 60){
                str1 = "<td style=\"color:darkorange\">"+maxState;
                builder.append(str1);
            }
            else {
                str1 = "<td style=\"color:red;font-weight:bold\">" + maxState;
                builder.append(str1);
            }

            builder.append("</td>");

            int maxComp = report.getDetailsFileOutput().get(i).getMaxComplexity();
            paramHealth = report.getCompHealth(maxComp);

            if (paramHealth > 80){
                str1 = "<td style=\"color:darkgreen\">"+maxComp;
                builder.append(str1);
            }
            else if(paramHealth > 60){
                str1 = "<td style=\"color:darkorange\">"+maxComp;
                builder.append(str1);
            }
            else {
                str1 = "<td style=\"color:red;font-weight:bold\">" + maxComp;
                builder.append(str1);
            }
            builder.append("</td></tr>");
        }
        return builder.toString();
    }

    public Object getDynamic(String token, StaplerRequest req, StaplerResponse resp) throws IOException {
        token = token.toLowerCase();

        if(urlKeys.containsKey(token)){
            return new SourceMonitorCodeDisplay(owner, urlKeys.get(token));
        }

        return null;
    }

    private static class BreadCrumbResult extends SourceMonitorResult implements ModelObject {
        private static final long serialVersionUID = 1L;

        private String displayName = null;
        
        public BreadCrumbResult(SourceMonitorReport report, Run<?, ?> owner, String displayName){
            super(report, owner);
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}
