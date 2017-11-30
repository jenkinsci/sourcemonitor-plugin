package com.thalesgroup.hudson.plugins.sourcemonitor;

import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.TextFile;

import java.io.*;
import java.util.*;

// serializable required for multibranch pipeline
public class FileStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private int numFunctions;
    private int numStatements;
    private int maxComplexity;
    private int maxStatements;
    private String sourceFilePath;
    private ConfigurableParameters parameters;
    private ArrayList<FunctionStats> functionStats;


    public FileStats() {
        functionStats = new ArrayList<FunctionStats>();
        maxStatements = 0;
    }

    public void addFunction(FunctionStats newFunction){
        int statements = newFunction.getStatements();
        if (statements > this.maxStatements){
            this.maxStatements = statements;
        }
        functionStats.add(newFunction);
    }

    //region Getters and Setters
    public ArrayList<FunctionStats> getFunctionStats() {
        return functionStats;
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNumFunctions() {
        return numFunctions;
    }

    public void setNumFunctions(int numFunctions) {
        this.numFunctions = numFunctions;
    }

    public int getNumStatements() {
        return numStatements;
    }

    public void setNumStatements(int numStatements) {
        this.numStatements = numStatements;
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public void setMaxComplexity(int maxComplexity) {
        this.maxComplexity = maxComplexity;
    }

    public void setParameters(ConfigurableParameters parameters) {
        this.parameters = parameters;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }
    //endregion

    //region HTML Generation
    public String getFunctionOutput(){
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<functionStats.size();i++){
            builder.append("<tr><td>");
            builder.append(functionStats.get(i).getFunction());
            builder.append("</td>");

            int numState = functionStats.get(i).getStatements();
            int paramHealth = SourceMonitorUtility.getStatementsHealth(parameters, numState);
            builder.append(SourceMonitorUtility.getColoredString(paramHealth, numState));
            builder.append("</td>");

            int maxComp = functionStats.get(i).getComplexity();
            paramHealth = SourceMonitorUtility.getComplexityHealth(parameters, maxComp);
            builder.append(SourceMonitorUtility.getColoredString(paramHealth, maxComp));
            builder.append("</td>");
            builder.append("</td></tr>");
        }
        return builder.toString();
    }

    public String createCodeTable(File source) throws IOException, InterruptedException {

        StringBuilder builder = new StringBuilder();
        int line = 0;

        try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"))){
            String content;
            while ((content = input.readLine()) != null) {
                line++;

                builder.append("<tr style = \"background-color:white\" class=\"noCover\">\n");
                builder.append("<td style=\"padding-left:2px;text-align:right\"><a name='" + line + "'/>" + line + "</td>\n");
                builder.append("<td style=\"padding-left:6px;padding-right:4px\">"
                        + content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "").replace("\r", "").replace(" ",
                        "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;") + "</td>\n");
                builder.append("</tr>\n");
            }
        }
        return builder.toString();
    }
    //endregion

    //region Source File Fetching Helpers
    public String getUrlTransform() {
        String name = getStringPath();
        StringBuilder buf = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (('0' <= c && '9' >= c)
                    || ('A' <= c && 'Z' >= c)
                    || ('a' <= c && 'z' >= c)) {
                buf.append(c);
            } else {
                buf.append('_');
            }
        }
        return buf.toString();
    }

    private File getSourceFile() throws IOException, InterruptedException{

        return new File(getStringPath());
    }

    private String getStringPath(){
        return sourceFilePath + "/" + fileName;
    }

    public String getSourceFileContent() throws IOException, InterruptedException{

        return createCodeTable(getSourceFile());
    }
    //endregion
}
