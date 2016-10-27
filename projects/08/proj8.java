
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import java.util.HashMap;
import java.util.regex.Matcher;


public class proj8 {
	static FileOutputStream asmFileStream;
	static int condCounter;
	//This int is a counter of the number of function calls
	static int functionCallCounter;
	
	//TODO check if this is the right way to do this...
	static String curFunction;


	//////////////////////////////////Regexe's/////////////////////////////////////
	static String SYMBOL = "[a-zA-Z_\\.\\$:][\\w_\\.\\$:]+";
	// VM code line possibilities regexe's
	static String COMMENT = "//.*";
	static String ARITHMETIC_REGEX = "\\s*(\\w+)\\s*$";
	static String POP_REGEX = "\\s*" + "pop" + "\\s+(\\w+)\\s+(\\d+)\\s*";
	static String PUSH_REGEX = "\\s*" + "push" + "\\s+(\\w+)\\s+(\\d+)\\s*";
	static String GOTO_REGEX = "\\s*" + "goto" + "\\s+" + "([\\w_]+)" + "\\s*";
	static String IF_GOTO_REGEX = "\\s*" + "if-goto" + "\\s+" +  "([\\w_]+)" + "\\s*";
	static String LABEL_REGEX = "\\s*" + "label" + "\\s+" + "([\\w_]+)" + "\\s*";
	static String FUNCTION_REGEX = "\\s*" + "function" + "\\s+(" + SYMBOL + ")\\s+(\\d+)\\s*";
	static String RETURN_REGEX = "\\s*" + "return" + "\\s*";
	static String CALL_FUNCTION_REGEX = "\\s*" + "call" + "\\s+(" + SYMBOL + ")\\s+(\\d+)\\s*";


	
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
		//remove all white spaces from line
		line = line.replaceAll("\\s+", "");
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
	public static String pushParser(String segment, String index, String curFileName) {
		String ret = "";
		if (segment.equals("constant")) {
			ret = "@" + index + "\n" + "D=A" + "\n";
		}else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
			ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=M" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "A=D" + "\n" + "D=M" + "\n";
		}else if (segment.equals("temp"))
		{
			ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=A" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "A=D" + "\n" + "D=M" + "\n";
		} else if (segment.equals("pointer")){
			if (index.equals("0")){
				ret = "@THIS" + "\n" + "D = M" + "\n";
			} else { //if the index is 1
				ret = "@THAT" + "\n" + "D = M" + "\n";
			}
		}else if (segment.equals("static")) {
			String toStatic = curFileName + "." + index;
			ret = "@" + toStatic + "\n" + "D=M" + "\n";
		}
		return ret + PUSH_TO_STACK;
	}
	
	/**
	 * parses a pop command line and returns it's corresponding assembly code
	 * @param line
	 * @return 
	 */
	public static String popParser(String segment, String index, String curFileName) {
		String ret = "";
		
		if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
			ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=M" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "@R14" + "\n" + "M=D" + "\n" + "@R15" + "\n" + "D = M" + "\n" + "@R14" + "\n" + "A=M" + "\n" + "M = D" + "\n";
		}else if (segment.equals("temp"))
		{
			ret = "@" + index + "\n" + "D=A" + "\n" + "@R13" + "\n" + "M=D" + "\n" + "@" + segmentMap.get(segment) + "\n" + "D=A" + "\n" + "@R13" + "\n" + "D=M+D" + "\n" + "@R14" + "\n" + "M=D" + "\n" + "@R15" + "\n" + "D = M" + "\n" + "@R14" + "\n" + "A=M" + "\n" + "M = D" + "\n";
		}else if (segment.equals("pointer")) {
		    //put in D R15's value
			ret = "@R15" + "\n" + "D = M" + "\n";
		    if (index.equals("0")) {
		        ret = "@" + "THIS" + "\n" + "M = D" + "\n";
		    }else { //index is 1
		       ret = "@" + "THAT" + "\n" + "M = D" + "\n";
		    }
		}else if (segment.equals("static")) {
			String toStatic = curFileName + "." + index;
			ret = "@R15" + "\n" + "D=M" + "\n" + "@" + toStatic + "\n" + "M=D" + "\n";
		}
		return POP_FROM_STACK + ret;
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function get's a VM code label and the current function's name, and returns the 
	 * corresponding assembly line 
	 * @param line
	 * @return 
	 */
	public static String writeLabel(String label){
		return "(" + curFunction + "$" + label + ")" + "\n";
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function get's a VM code label and the current function's name, and returns the 
	 * corresponding assembly line
	 * @param line
	 * @return 
	 */
	public static String writeGoTo(String label) {
		return "@" + curFunction + "$" + label + "\n" + "0;JEQ" + "\n";
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function get's a VM code label and the current function's name, and returns the 
	 * corresponding assembly line
	 * @param line
	 * @return 
	 */
	public static String writeIf(String label) {
		//pop value from stack and jump if it's not 0
		return "@SP" + "\n" + "M=M-1" + "\n" + "A=M" + "\n" + "D=M" + "\n" + "@" + curFunction + "$" + label + "\n" + "D;JNE" + "\n";
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function get's a VM code line the current function's name, and returns the 
	 * corresponding assembly line
	 * @param line
	 * @return 
	 */
	public static String writeCall(int numOfArgs ,String label) {
		String ret = "";
		String pushToStack = "@SP" + "\n" + "M=M+1" + "\n" + "A=M-1" + "\n" + "M=D" + "\n";
		
		//save return address
		ret += "@returnAddress." + functionCallCounter + "\n" + "D=A" + "\n" + pushToStack;
		
		//save LCL address
		ret += "@LCL" + "\n" + "D=M" + "\n" + pushToStack;
		
		//save ARG address
		ret += "@ARG" + "\n" + "D=M" + "\n" + pushToStack;
		
		//save THIS address
		ret += "@THIS" + "\n" + "D=M" + "\n" + pushToStack;
		
		//save THAT address
		ret += "@THAT" + "\n" + "D=M" + "\n" + pushToStack;
		
		//ARG = SP-n-5
		ret += "@" + Integer.toString(5 + numOfArgs) + "\n" + "D=A" + "\n" + "@SP" + "\n" + "D=M-D" + "\n" + "@ARG" + "\n" + "M=D" + "\n"; 
		
		//LCL = SP
		ret += "@SP" + "\n" + "D=M" + "\n" + "@LCL" + "\n" + "M=D" + "\n";
		
		//jump to function
		ret += "@" + label + "\n" + "0;JEQ" + "\n" + "(" + "returnAddress." + functionCallCounter + ")" + "\n";
		
		return ret;
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function parses a function declaration line from VM code, given the number of local variables given in the function and the function's name
	 * @param line
	 * @return 
	 */
	public static String writeFunction(int numOfLocalVars) {
		String ret = "";
		ret += "(" + curFunction + ")" + "\n";
		for(int i = 0; i < numOfLocalVars; i++) {
			ret += "@SP" + "\n" + "M=M+1" + "\n" + "A=M-1" + "\n" + "M=0" + "\n";
		}
		return ret;
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function parses a return line from VM code
	 * @param line
	 * @return 
	 */
	public static String writeReturn() {
		String ret = "";
		
		//FRAME = LCL
		ret += "@LCL" + "\n" + "D=M" + "\n" + "@R15" + "\n" + "M=D" + "\n";
		
		//return = FRAME - 5
		ret += "@5" + "\n" + "A=D-A" + "\n" + "D=M" + "\n" + "@R14" + "\n" + "M=D" + "\n";
		
		//reposition the return value to the caller
		ret += "@SP" + "\n" + "M=M-1" + "\n" + "A=M" + "\n" + "D=M" + "\n" + "@ARG" + "\n" + "A=M" + "\n" + "M=D" + "\n";
		
		//restore SP of the caller
		ret += "@ARG" + "\n" + "D=M+1" + "\n" + "@SP" + "\n" + "M=D" + "\n";
		
		//restore THAT THIS ARG and LCL to the caller
		ret += "@R15" + "\n" + "D=M" + "\n" + "@1" + "\n" + "A=D-A" + "\n" + "D=M" + "\n" + "@THAT" + "\n" + "M=D" + "\n";
		ret += "@R15" + "\n" + "D=M" + "\n" + "@2" + "\n" + "A=D-A" + "\n" + "D=M" + "\n" + "@THIS" + "\n" + "M=D" + "\n";
		ret += "@R15" + "\n" + "D=M" + "\n" + "@3" + "\n" + "A=D-A" + "\n" + "D=M" + "\n" + "@ARG" + "\n" + "M=D" + "\n";
		ret += "@R15" + "\n" + "D=M" + "\n" + "@4" + "\n" + "A=D-A" + "\n" + "D=M" + "\n" + "@LCL" + "\n" + "M=D" + "\n";
		
		//move to the return address
		ret += "@R14" + "\n" + "A=M" + "\n" + "0;JEQ" + "\n";

		return ret;
	}
	
	//TODO check if we really need to call this function like this..
	/**
	 * This function returns assembly code that affects the VM initialization
	 * @param line
	 * @return 
	 */
	public static String writeInit() {
		//initialize the SP to 0
		return "@256" + "\n" + "D=A" + "\n" + "@SP" + "\n" + "M=D" + "\n";
	}
	
	/**
	 * gets a stream to VM code, goes over each line checks what kind of line it is and send it 
	 * to the relevant method
	 * @return 
	 * @throws IOException 
	 */
	public static void parseVMFile(String curFileName, String inFilePath) throws IOException{
		
		try {
			//instantiate line String and create file input stream
			String line = null;
			InputStream is = new FileInputStream(inFilePath);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			//parse the VM code file
			while ((line = br.readLine()) != null) {
				String assemblyLine = "";
				//Check if the line is a comment
			    if (line.startsWith("//")){
			    	continue;
			    }
			    
			    //ignore blank lines:
			    if (line.equals("")){
			    	continue;
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
				
				Pattern gotoPattern = Pattern.compile(GOTO_REGEX);
				Matcher gotoMatcher = gotoPattern.matcher(line);
				
				Pattern ifgotoPattern = Pattern.compile(IF_GOTO_REGEX);
				Matcher ifgotoMatcher = ifgotoPattern.matcher(line);
				
				Pattern labelPattern = Pattern.compile(LABEL_REGEX);
				Matcher labelMatcher = labelPattern.matcher(line);
				
				Pattern functionPattern = Pattern.compile(FUNCTION_REGEX);
				Matcher functionMatcher = functionPattern.matcher(line);
				
				Pattern callFunctionPattern = Pattern.compile(CALL_FUNCTION_REGEX);
				Matcher callFunctionMatcher = callFunctionPattern.matcher(line);
				
				Pattern returnPattern = Pattern.compile(RETURN_REGEX);
				Matcher returnMatcher = returnPattern.matcher(line);
				
				if (pushMatcher.find()){
//					System.out.println("------------------push line:  " + line + "-------------------------");
					String segment = pushMatcher.group(PUSH_SEG);
					String index = pushMatcher.group(PUSH_INDEX);
					assemblyLine = pushParser(segment, index, curFileName);
//					System.out.println(assemblyLine + "\n\n");

				}else if(popMatcher.find()){
//					System.out.println("------------------pop line:  " + line + "----------------------");
					String segment = popMatcher.group(PUSH_SEG);
					String index = popMatcher.group(PUSH_INDEX);
					assemblyLine = popParser(segment, index, curFileName);	
//					System.out.println(assemblyLine + "\n\n");

				}else if(ifgotoMatcher.find()){
//					System.out.println("------------------ifgoto line:  " + line + "-------------------");
					String label = ifgotoMatcher.group(1);
					assemblyLine = writeIf(label);
//					System.out.println(assemblyLine + "\n\n");

				}else if(gotoMatcher.find()){
//					System.out.println("-------------------goto line:  " + line + "------------------");
					String label = gotoMatcher.group(1);
					assemblyLine = writeGoTo(label);
//					System.out.println(assemblyLine + "\n\n");

				}else if(labelMatcher.find()){
//					System.out.println("-------------------label line:  " + line + "------------------");
					String label = labelMatcher.group(1);
					assemblyLine = writeLabel(label);
//					System.out.println(assemblyLine + "\n\n");

				}else if(callFunctionMatcher.matches()){
//					System.out.println("-------------------call function line:  " + line + "------------------");
					String label = callFunctionMatcher.group(1);
					int numOfArgs = Integer.valueOf(callFunctionMatcher.group(2));
					assemblyLine = writeCall(numOfArgs, label);
//					System.out.println(assemblyLine + "\n\n");
					functionCallCounter += functionCallCounter;
				}else if(functionMatcher.matches()){
//					System.out.println("-----------------function line:  " + line + "------------------");
					curFunction = functionMatcher.group(1);
//					System.out.println(curFunction + "................................");
					int numOfLocalVars = Integer.valueOf(functionMatcher.group(2));
					assemblyLine = writeFunction(numOfLocalVars);
//					System.out.println(assemblyLine + "\n\n");

				}else if(returnMatcher.matches()){
//					System.out.println("------------------return line:  " + line + "------------------");
					assemblyLine = writeReturn();
//					System.out.println(assemblyLine + "\n\n");

				}else if(arithmeticMatcher.matches()){
//					System.out.println("------------------arithmetic line:  " + line + "-----------------");
					assemblyLine = arithmeticParser(line);
//					System.out.println(assemblyLine + "\n\n");

				}
				asmFileStream.write(assemblyLine.getBytes());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * open the output file and send the input file to the parser
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		//instantiate global static variables
		condCounter = 0;
		functionCallCounter = 0;
		curFunction = "main";
		
		//check if it's a file or a directory and act accordingly
		File file = new File(args[0]);
		boolean isFile = file.isFile();	// Check if it's a regular file
		
		if (isFile){
			//create output file
			String asmFilePath = args[0].replace("vm", "asm");
			asmFileStream = new FileOutputStream(asmFilePath);
			
			//get the file's name
			String asmFileName = file.getName().replace(".vm","");
			
			parseVMFile(asmFileName, args[0]);
			
		}else { //it's a directory
			String asmFileName = file.getName();
			
			//create output file
			String asmFilePath = args[0] + "/" + asmFileName + ".asm";
			asmFileStream = new FileOutputStream(asmFilePath);
			
			//write to the asm file the Init
			asmFileStream.write(writeInit().getBytes());
			//execute the Sys.init
			asmFileStream.write(writeCall(0, "Sys.init").getBytes());
			functionCallCounter += 1;
			
			//go over all the files in the directory
			File[] directoryListing = file.listFiles();
			if (directoryListing != null){ // check that directory isn't empty
				for (File child : directoryListing){
					//get the file's path and check if it's a vm file
					String childPath = child.getAbsolutePath();
					if (childPath.substring(childPath.length() - 2).equals("vm")){ // check if it's a vm file
						parseVMFile(child.getName().replace(".vm", ""), childPath);
					}
				}
			}
		}
		
		//restart global static variables
		condCounter = 0;
		functionCallCounter = 0;
	}
}
