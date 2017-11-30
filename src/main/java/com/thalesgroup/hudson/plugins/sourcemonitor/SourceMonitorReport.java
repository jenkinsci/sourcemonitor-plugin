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

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.*;
import hudson.model.Run;
import java.io.File;

public class SourceMonitorReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private ConfigurableParameters parameters;
    private List<Map<String,String>> checkpoints;
    private Map<String, String> summaryMetrics;
    private ArrayList<FunctionStats> detailedMetrics = null;
    private ArrayList<FileStats> detailsFileOutput = null;

    private static final Map<String, String> oldMetricNames = new ImmutableMap.Builder<String, String>()
            .put("M0", "Lines")
            .put("M1", "Statements")
            .put("M2", "Percent Branch Statements")
            .put("M3", "Percent Lines with Comments")
            .put("M4", "Functions")
            .put("M5", "Average Statements per Function")
            .put("M6", "Line Number of Most Complex Function")
            .put("M7", "Name of Most Complex Function")
            .put("M8", "Complexity of Most Complex Function")
            .put("M9", "Line Number of Deepest Block")
            .put("M10", "Maximum Block Depth")
            .put("M11", "Average Block Depth")
            .put("M12", "Average Complexity")
            .build();

    //region Getters and Setters
    public ArrayList<FunctionStats> getDetailedMetrics() {
        return detailedMetrics;
    }

    public void setDetailedMetrics(ArrayList<FunctionStats> detailedMetrics) {
        this.detailedMetrics = detailedMetrics;
    }

    public ConfigurableParameters getParameters() {
        if (parameters == null){
            return new ConfigurableParameters();
        }
        return parameters;
    }

    public void setParameters(ConfigurableParameters parameters) {
        this.parameters = parameters;
    }

    @Deprecated
    public List<Map<String,String>> getCheckpoints() {
        return checkpoints;
    }

    @Deprecated
    public void setCheckpoints(List<Map<String,String>> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public void setSummaryMetrics(Map<String, String> summaryMetrics) {
        this.summaryMetrics = summaryMetrics;
    }

    public Map<String, String> getSummaryMetrics() {
	    if (summaryMetrics == null) {
	        return buildSummaryMetricsFromCheckpoints();
        }

        return summaryMetrics;
    }

    public ArrayList<FileStats> getDetailsFileOutput() {
        return detailsFileOutput;
    }

    public void setDetailsFileOutput(ArrayList<FileStats> detailsFileOutput) {
        this.detailsFileOutput = detailsFileOutput;
    }
    //endregion

    private Map<String, String> buildSummaryMetricsFromCheckpoints() {
        Map<String, String> summaryMetrics = new HashMap<String, String>();

        if (checkpoints.size() > 0) {
            for (Map.Entry<String, String> entry : checkpoints.get(0).entrySet()) {
                String key = entry.getKey();
                String metric = oldMetricNames.get(key);

                if (metric != null) {
                    summaryMetrics.put(metric, entry.getValue());
                }
            }
        }

        return summaryMetrics;
    }
}