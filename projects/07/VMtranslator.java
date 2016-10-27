import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.regex.Matcher;


public class VMtranslator {
    static FileOutputStream asmFileStream;
    static int condCounter;
    
    //////////////////////////////////Regexe's/////////////////////////////////////
    static String COMMENT = "//.*";
    static String ARITHMETIC_REGEX = "\\s*(\\w+)\\s*";
    static String POP_REGEX = "\\s*" + "pop" + "\\s+(\\w+)\\s+(\\d+)\\s*";
    static String PUSH_REGEX = "\\s*" + "push" + "\\s+(\\w+)\\s+(\\d+)\\s*";
    
    int POP_SEG = 1;
    int POP_INDEX = 2;
    static int PUSH_SEG = 1;
    static int PUSH_INDEX = 2;
    ///////////////////////////////////////////////////////////////////////////////
    
    
    ///////////////////////////assembly code constants//////////////////////////////
    //Take the value from D and push it to the top of the stake, and advance the SP by 1
    static String PUSH_TO_STACK = "@SP" + "\n" + "A=M" + "\n" + "M=D" + "\n" + "@SP" + "\n" + "M=M+1" + "\n";
    
    //put in R15 the value from the top of the stack, and take the SP 1 backwards
    static String POP_FROM_STACK = "@SP" + "\n" + "M=M-1" + "\n" + "A=M" + "\n" + "D=M" + "\n" + "@R15" + "\n" + "M=D" + "\n";
    
    static String ADD = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M+D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
    
    static String SUBTRACT = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M-D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
    
    static String NEG = "@SP" + "\n" + "A=M-1" + "\n" + "M=-M" + "\n";
    
    static String AND = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M&D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
    
    static String OR = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M|D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
    
    static String NOT = "@SP" + "\n" + "A=M-1" + "\n" + "M=!M" + "\n";
    
    static String BEFORE_COMPARE = "@SP" + "\n" + "A=M-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "D= M-D" + "\n";
    ///////////////////////////////////////////////////////////////////////////////
    
    
    private static final HashMap<String, String> arithmeticMap;
    static
    {
        arithmeticMap = new HashMap<String, String>();
        
        arithmeticMap.put("add", ADD);
        arithmeticMap.put("sub", SUBTRACT);
        arithmeticMap.put("neg", NEG);
        arithmeticMap.put("or", OR);
        arithmeticMap.put("not", NOT);
        arithmeticMap.put("and", AND);
        
    }
    
    static String RAM_TMP = "5";
    private static final HashMap<String, String> segmentMap;
    static
    {
        segmentMap = new HashMap<String, String>();
        segmentMap.put("this", "THIS");
        segmentMap.put("that", "THAT");
        segmentMap.put("argument", "ARG");
        segmentMap.put("local", "LCL");
        segmentMap.put("temp", RAM_TMP);
    }
    
    /**
     * parses an arithmetic condition line and returns its corresponding assembly code
     * @param line
     * @return
     */
    public static String conditionParser(String condition) {
        String compare = "@" + condition + "_LABEL_" + condCounter + "\n" + "D; J" + condition + "\n" + "D = 0" + "\n" + "@END_COND_LABEL_" + condCounter + "\n" + "0; JEQ" + "\n" + "(" + condition + "_LABEL_" + condCounter + ")" + "\n" + "D=-1" + "\n";
        String AFTER_COMPARE = "(END_COND_LABEL_" + condCounter + ")" + "\n" + "@SP" + "\n" + "M =M-1" + "\n" + "A=M-1" + "\n" + "M=D" + "\n";
        condCounter++;
        return BEFORE_COMPARE + compare + AFTER_COMPARE;
    }
    
    /**
     * parses an arithmetic command line and returns its corresponding assembly code
     * @param line
     * @return
     */
    public static String arithmeticParser(String line) {
        String res;
        //check if it's a condition line (eq/gt/lt)
        if (line.equals("eq")) {
            res = conditionParser("EQ");
        }else if (line.equals("gt")) {
            res = conditionParser("GT");
        }else if (line.equals("lt")) {
            res = conditionParser("LT");
        }else { // its not a condition line
            res = arithmeticMap.get(line);
        }
        return res;
    }
    
    /**
     * parses a push command line and returns its corresponding assembly code
     * @param line
     * @return
     */
    public static String pushParser(String segment, String index, String outFilePath) {
        String ret = "";
        if (segment.equals("constant")) {
            ret = "@" + index + "\n" + "D=A" + "\n";
        }
        else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
            ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=M" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "A=D" + "\n" + "D=M" + "\n";
        }
        else if (segment.equals("temp"))
        {
            ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=A" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "A=D" + "\n" + "D=M" + "\n";
        }
        else if (segment.equals("pointer")){
            if (index.equals("0")){
                ret = "@THIS" + "\n" + "D = M" + "\n";
            } else { //if the index is 1
                ret = "@THAT" + "\n" + "D = M" + "\n";
            }
        }
        else if (segment.equals("static")) {
            outFilePath = outFilePath.replace("asm", index);
            ret = "@" + outFilePath + "\n" + "D=M" + "\n";
        }
        return ret + PUSH_TO_STACK;
    }
    
    /**
     * parses a pop command line and returns it's corresponding assembly code
     * @param line
     * @return
     */
    public static String popParser(String segment, String index, String outFileName) {
        String ret = "";
        
        if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
            ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=M" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "@R14" + "\n" + "M=D" + "\n" + "@R15" + "\n" + "D = M" + "\n" + "@R14" + "\n" + "A=M" + "\n" + "M = D" + "\n";
        }
        else if (segment.equals("temp"))
        {
            ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=A" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "@R14" + "\n" + "M=D" + "\n" + "@R15" + "\n" + "D = M" + "\n" + "@R14" + "\n" + "A=M" + "\n" + "M = D" + "\n";
        }
        else if (segment.equals("pointer")) {
            //put in D R15's value
            ret = "@R15" + "\n" + "D = M" + "\n";
            if (index.equals("0")) {
                ret = "@" + "THIS" + "\n" + "M = D" + "\n";
            }
            else { //index is 1
                ret = "@" + "THAT" + "\n" + "M = D" + "\n";
            }
        }
        else if (segment.equals("static")) {
            outFileName = outFileName.replace("asm", index);
            ret = "@R15" + "\n" + "D=M" + "\n" + "@" + outFileName + "\n" + "M=D" + "\n";
        }
        return POP_FROM_STACK + ret;
    }
    
    /**
     * gets a stream to VM code, goes over each line checks what kind of line it is and send it to the relevant method
     * @return
     * @throws IOException
     */
    public static void parseVMFile(String line, String outFileName) throws IOException{
        String assemblyLine = "";
        //Check if the line is a comment
        if (line.startsWith("//")){
            return;
        }
        
        //ignore blank lines:
        if (line.equals("")){
            return;
        }
        
        //check if the line contains a comment, if it does remove it
        int commentIndex = line.indexOf("//");
        if (commentIndex > 0) {
            line = line.substring(0, commentIndex);
        }
        
        //check if it is a push command / arithmetic command / pop command and act accordingly
        Pattern pushPattern = Pattern.compile(PUSH_REGEX);
        Matcher pushMatcher = pushPattern.matcher(line);
        
        Pattern arithmeticPattern = Pattern.compile(ARITHMETIC_REGEX);
        Matcher arithmeticMatcher = arithmeticPattern.matcher(line);
        
        Pattern popPattern = Pattern.compile(POP_REGEX);
        Matcher popMatcher = popPattern.matcher(line);
        if (pushMatcher.find()) {
            String segment = pushMatcher.group(PUSH_SEG);
            String index = pushMatcher.group(PUSH_INDEX);
            assemblyLine = pushParser(segment, index, outFileName);
        }
        else if(popMatcher.find()){ // it's a pop command
            String segment = popMatcher.group(PUSH_SEG);
            String index = popMatcher.group(PUSH_INDEX);
            assemblyLine = popParser(segment, index, outFileName);
            
        }else if(arithmeticMatcher.find()){
            assemblyLine = arithmeticParser(line);
        }
        asmFileStream.write(assemblyLine.getBytes());
    }
    
    
    /**
     * open the output file and send the input file to the parser
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws Exception {
        String line = null;
        condCounter = 0;
        
        //create output file
        String asmFilePath = args[0].replace("vm", "asm");
        asmFileStream = new FileOutputStream(asmFilePath);
        
        String[] parts = asmFilePath.split("/");
        String asmFileName = parts[parts.length - 1];
        
        try {
            //send each line in the VM code for parsing
            InputStream is = new FileInputStream(args[0]);
            InputStreamReader isr = new InputStreamReader(is);
            
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                parseVMFile(line, asmFileName);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
