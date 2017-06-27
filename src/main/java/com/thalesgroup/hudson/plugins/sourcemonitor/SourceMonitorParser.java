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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jenkinsci.remoting.RoleChecker;

public class SourceMonitorParser implements FilePath.FileCallable<SourceMonitorReport> {

	private static final long serialVersionUID = 1L;

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

		Document document = null;
		try {
			SAXBuilder sxb = new SAXBuilder();
			document = sxb.build(new InputStreamReader(new FileInputStream(new File(resultFilePath.toURI())), "UTF-8"));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Parsing file error :" + e.toString());
			throw new AbortException("Parsing file error");
		}
		
		Map<String, String> metricsSummaryMap = new HashMap<String, String>();
		Map<String, String> metricNameMap = new HashMap<String, String>();
		
		Element root = document.getRootElement();
		Element projectElt = root.getChild("project");

		// Parse the Metric Names.
		Element metricNames = projectElt.getChild("metric_names");
		List metricNamesEltList = metricNames.getChildren();
		for (int i = 0; i < metricNamesEltList.size(); i++) {
		    Element metricNameElt = (Element)metricNamesEltList.get(i);
		    metricNameMap.put(metricNameElt.getAttributeValue("id"), metricNameElt.getValue());
        }

		// Parse Summary checkpoint data.
        Element checkpoints = projectElt.getChild("checkpoints");
        List checkpointsEltList = checkpoints.getChildren();
        for (int i = 0; i < checkpointsEltList.size(); i++) {
            Element checkpoint = (Element) checkpointsEltList.get(i);
            Element metricsElt = checkpoint.getChild("metrics");
            List metricsEltList = metricsElt.getChildren();
            for (int j = 0; j < metricsEltList.size(); j++) {
                Element metricElt = (Element) metricsEltList.get(j);
                metricsSummaryMap.put(metricNameMap.get(metricElt.getAttributeValue("id")), metricElt.getValue());
            }
        }

		sourceMonitorReport.setSummaryMetrics(metricsSummaryMap);

		return sourceMonitorReport;
	}

	public FilePath getResultFilePath() {
		return resultFilePath;
	}

	public void setResultFilePath(FilePath resultFilePath) {
		this.resultFilePath = resultFilePath;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
	}
}
