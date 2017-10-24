package com.thalesgroup.hudson.plugins.sourcemonitor;

import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.TextFile;

import java.io.*;
import java.util.*;

// serializable required for multibranch pipeline
public class FileStats implements Serializable {

    private String fileName;
    private int numFunctions;
    private int numStatements;
    private int maxComplexity;
    private int maxStatements;
    private SourceMonitorReport report;
    private ArrayList<FunctionStats> functionStats;


    public FileStats(SourceMonitorReport report) {
        functionStats = new ArrayList<FunctionStats>();
        maxStatements = 0;
        this.report = report;
    }

    public void addFunction(FunctionStats newFunction){
        int statements = newFunction.getStatements();
        if (statements > this.maxStatements){
            this.maxStatements = statements;
        }
        functionStats.add(newFunction);
    }

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

    public String getFunctionOutput(){
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<functionStats.size();i++){
            builder.append("<tr><td>");
            builder.append(functionStats.get(i).getFunction());
            builder.append("</td>");
            String str1;

            int numState = functionStats.get(i).getStatements();
            int paramHealth = report.getStateHealth(numState);

            if (paramHealth >= 80){
                str1 = "<td style=\"color:darkgreen\">"+numState;
                builder.append(str1);
            }
            else if (paramHealth >= 60){
                str1 = "<td style=\"color:darkorange\">"+numState;
                builder.append(str1);
            }
            else{
                str1 = "<td style=\"color:red;font-weight:bold\">"+numState;
                builder.append(str1);
            }
            builder.append("</td>");

            int maxComp = functionStats.get(i).getComplexity();
            paramHealth = report.getCompHealth(maxComp);
            if (paramHealth >= 80){
                str1 = "<td style=\"color:darkgreen\">"+maxComp;
                builder.append(str1);
            }
            else if (paramHealth >= 60){
                str1 = "<td style=\"color:darkorange\">"+maxComp;
                builder.append(str1);
            }
            else{
                str1 = "<td style=\"color:red;font-weight:bold\">"+maxComp;
                builder.append(str1);
            }
            builder.append("</td>");
            builder.append("</td></tr>");
        }
        return builder.toString();
    }

    //public boolean hasPermission() {
    //    return owner.hasPermission(Item.WORKSPACE);
    //}

    private File getSourceFile() throws IOException, InterruptedException{

        return new File(report.getParentFile(), "/workspace/src/" + fileName);
    }

    public String getSourceFileContent() throws IOException, InterruptedException{

        return createCodeTable(getSourceFile());
    }

    public String createCodeTable(File source) throws IOException, InterruptedException {

        FileInputStream inputStream = null;
        InputStreamReader isReader = null;
        BufferedReader input = null;
        StringBuilder builder = new StringBuilder();
        int line = 0;

        try {
            inputStream = new FileInputStream(source);
            isReader = new InputStreamReader(inputStream, "UTF-8");
            input = new BufferedReader(isReader);
            String content;
            while ((content = input.readLine()) != null) {
                line++;

                builder.append("<tr style = \"background-color:white\" class=\"noCover\">\n");
                builder.append("<td class=\"line\"><a name='" + line + "'/>" + line + "</td>\n");
                builder.append("<td class=\"code\">"
                        + content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "").replace("\r", "").replace(" ",
                        "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;") + "</td>\n");
                builder.append("</tr>\n");
            }
            input.close();
            inputStream.close();
            isReader.close();
        } catch(Exception e) {

        }

        return builder.toString();
    }
}
