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
	static String COMMENT = "\\/\\/.*";
	static String ARITHMETIC_REGEX = "\\s*(\\w+)\\s*";
	static String POP_REGEX = "\\s*" + "pop" + "\\s+(\\w+)\\s+(\\d+)\\s*";
	static String PUSH_REGEX = "\\s*" + "push" + "\\s+(\\w+)\\s+(\\d+)\\s*";

	int POP_SEG = 1;
	int POP_INDEX = 2;
	static int PUSH_SEG = 1;
	static int PUSH_INDEX = 2;
	///////////////////////////////////////////////////////////////////////////////

	
	
	
	///////////////////////////assembly code constants//////////////////////////////
	static String PUSH_TO_STACK = "@SP" + "\n" + "A=M" + "\n" + "M=D" + "\n" + "@SP" + "\n" + "M=M+1" + "\n";
	
	static String ADD = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M+D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
	
	static String SUBTRACT = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M-D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";

	static String NEG = "@SP" + "\n" + "A=M-1" + "\n" + "M=-M" + "\n";
	
	static String AND = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M&D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
	
	static String OR = "@SP" + "\n" + "A=M" + "\n" + "A=A-1" + "\n" + "D=M" + "\n" + "A=A-1" + "\n" + "M=M|D" + "\n" + "@SP" + "\n" + "M=M-1" + "\n";
	
	static String NOT = "@SP" + "\n" + "A=M-1" + "\n" + "M=!M" + "\n";
	
	static String BEFORE_COMPARE = "@SP" + "\n" + "A = M - 1" + "\n" + "D = M" + "\n" + "A = A - 1" + "\n" + "D = M - D" + "\n";
	
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
    
    /**
     * parses an arithmetic condition line and returns its corresponding assembly code
     * @param line
     * @return
     */
	public static String conditionParser(String condition) {
		String compare = "@" + condition + "_LABEL_" + condCounter + "\n" + "D; J" + condition + "\n" + "D = 0" + "\n" + "@END_COND_LABEL_" + condCounter + "\n" + "0; JEQ" + "\n" + "(" + condition + "_LABEL_" + condCounter + ")" + "\n" + "D = -1" + "\n";
		
		String AFTER_COMPARE = "(END_COND_LABEL_" + condCounter + ")" + "\n" + "@SP" + "\n" + "M = M - 1" + "\n" + "A = M - 1" + "\n" + "M = D" + "\n";
		
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
	public static String pushParser(String segment, String index) {
		
		if (segment.equals("constant")){
			return "@" + index + "\n" + "D=A" + "\n" + PUSH_TO_STACK;
		}
//		TODO: change this:
		return "";
	}
	
	/**
	 * gets a stream to VM code, goes over each line checks what kind of line it is and send it to the relevant method
	 * @return 
	 * @throws IOException 
	 */
	public static void parseVMFile(String line) throws IOException{
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
		
//	    //remove all white spaces from the line:
//	    line = line.replaceAll("\\s+","");
	    
	    //check if it is a push command:
		Pattern pushPattern = Pattern.compile(PUSH_REGEX);
		Matcher pushMatcher = pushPattern.matcher(line);
		
	    //check if it is a push command:
		Pattern arithmeticPattern = Pattern.compile(ARITHMETIC_REGEX);
		Matcher arithmeticMatcher = arithmeticPattern.matcher(line);
	
		
		
		//check if it's a push command
		if (pushMatcher.find()){ 
			String segment = pushMatcher.group(PUSH_SEG);
			String index = pushMatcher.group(PUSH_INDEX);
			assemblyLine = pushParser(segment, index);
			
		//check if it's a push command
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
		String asmFileName = args[0].replace("vm", "asm");
		asmFileStream = new FileOutputStream(asmFileName);
		
		try {
			//send each line in the VM code for parsing
			InputStream is = new FileInputStream(args[0]);
			InputStreamReader isr = new InputStreamReader(is);
			
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				parseVMFile(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
