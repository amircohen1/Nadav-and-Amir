import java.awt.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.tree.VariableHeightLayoutCache;

import com.sun.org.apache.xalan.internal.xsltc.trax.OutputSettings;


public class HackAssembler {
	
	//////////////////////////////////////////
	//Possible computation commands:
	static String ZERO = "0";
	static String ONE = "1";
	static String MINUS_ONE = "\\-1";
	static String D = "D";
	static String AM = "[AM]";
	static String NOT_D = "\\!D";
	static String NOT_AM = "\\![AM]";
	static String MINUS_D = "\\-D";
	static String MINUS_AM = "\\-[AM]";
	static String D_PLUS_ONE = "D\\+1";
	static String AM_PLUS_ONE = "[AM]\\+1";
	static String D_MINUS_ONE = "D\\-1";
	static String AM_MINUS_ONE = "[AM]\\-1";
	static String D_PLUS_AM = "D\\+[AM]";
	static String D_MINUS_AM = "D\\-[AM]";
	static String AM_MINUS_D = "[AM]\\-D";
	static String D_AND_AM = "D\\&[AM]";
	static String D_OR_AM = "D\\|[AM]";
	static String D_SHIFT_RIGHT = "D\\>\\>";
	static String D_SHIFT_LEFT = "D\\<\\<";
	static String A_SHIFT_RIGHT = "A\\>\\>";
	//static String A_SHIFT_LEFT = "A\\<\\<";
	//static String M_SHIFT_RIGHT = "M\\>\\>";
	//static String M_SHIFT_LEFT = "M\\<\\<";
	//static String D_TIMES_AM = "D\\*[AM]";
	//////////////////////////////////////////
	
	
	
	//////////////////////////////////////////
	//Possible Jump commands:
	static String JGT = "JGT";
	static String JEQ = "JEQ";
	static String JGE = "JGE";
	static String JLT = "JLT";
	static String JNE = "JNE";
	static String JLE = "JLE";
	static String JMP = "JMP";
	//////////////////////////////////////////
	
	static int variableCounter;

	static HashMap symbols;
	
	/**
	 * This function get's an ACommand line 
	 * @param line
	 * @return
	 */
	public static String parseACommand(String line){
		String variable = line.substring(1);
		int num;
		
		//check if it's a number or a variable
		boolean isNumber = line.substring(1).matches("[0-9]+");
		
		if (!isNumber) {
			//check if symbol table contains this variable already
			if (!symbols.containsKey(variable)){
				symbols.put(variable, variableCounter);
				num = variableCounter;
				variableCounter++;
			} else { //the variables already exists
				num = (int) symbols.get(variable);
			}
		} else{ //if the variable is a number
			String numToConvert = line.substring(1);
			num = Integer.parseInt(numToConvert);
		}
		
		String binaryNumber = Integer.toBinaryString(num);
		String binaryCommand = new String(new char[16 - binaryNumber.length()]).replace('\0', '0') ;
		binaryCommand += binaryNumber;
		return binaryCommand;
	}
	
	public static String parseDest(String dest){
		String destBits = "";
		if (dest.indexOf("A") >= 0){
			destBits+= "1";
		} else {
			destBits += "0";
		}
		if (dest.indexOf("D") >= 0){
			destBits += "1";
		} else {
			destBits += "0";
		}
		if (dest.indexOf("M") >= 0){
			destBits += "1";
		} else {
			destBits += "0";
		}
		return destBits;
	}
	
	public static String parseComp(String comp){
		String res = "";
		// figure out the 'a' bit
		if (comp.indexOf('M') < 0) {
			res = "0";
		} else {
			res = "1";
		}

		
		if (Pattern.matches(ZERO, comp)){
			res += "101010";
		} else if (Pattern.matches(ONE, comp)){
			res += "111111";
		} else if (Pattern.matches(MINUS_ONE, comp)){
			res += "111010";
		} else if (Pattern.matches(D, comp)){
			res += "001100";
		} else if (Pattern.matches(AM, comp)){
			res += "110000";
		} else if (Pattern.matches(NOT_D, comp)){
			res += "001101";
		} else if (Pattern.matches(NOT_AM, comp)){
			res += "110001";
		} else if (Pattern.matches(MINUS_D, comp)){
			res += "001111";
		} else if (Pattern.matches(MINUS_AM, comp)){
			res += "110011";
		} else if (Pattern.matches(D_PLUS_ONE, comp)){
			res += "011111";
		} else if (Pattern.matches(AM_PLUS_ONE, comp)){
			res += "110111";
		} else if (Pattern.matches(D_MINUS_ONE, comp)){
			res += "001110";
		} else if (Pattern.matches(AM_MINUS_ONE, comp)){
			res += "110010";
		} else if (Pattern.matches(D_PLUS_AM, comp)){
			res += "000010";
		} else if (Pattern.matches(D_MINUS_AM, comp)){
			res += "010011";
		} else if (Pattern.matches(AM_MINUS_D, comp)){
			res += "000111";
		} else if (Pattern.matches(D_AND_AM, comp)){
			res += "000000";
		} else if (Pattern.matches(D_OR_AM, comp)){
			res += "010101";
		}
		
//		String D_SHIFT_RIGHT = "D\\>\\>";
//		String D_SHIFT_LEFT = "D\\<\\<";
//		String A_SHIFT_RIGHT = "A\\>\\>";
//		String A_SHIFT_LEFT = "A\\<\\<";
//		String M_SHIFT_RIGHT = "M\\>\\>";
//		String M_SHIFT_LEFT = "M\\<\\<";
//		String D_TIMES_AM = "D\\*[AM]";
		
		return res;
	}
	
	public static String parseJump(String comp){
		String res = "";
		
		if (Pattern.matches(JGT, comp)){
			res = "001";
		} else if (Pattern.matches(JEQ, comp)){
			res = "010";
		}  else if (Pattern.matches(JGE, comp)){
			res = "011";
		}  else if (Pattern.matches(JLT, comp)){
			res = "100";
		}  else if (Pattern.matches(JNE, comp)){
			res = "101";
		}  else if (Pattern.matches(JLE, comp)){
			res = "110";
		}  else if (Pattern.matches(JMP, comp)){
			res = "111";
		} else {
			res = "000";
		}
		
		return res;
	}

	
	public static String parseCCommand(String line){
		String destBits;
		String compBits;
		String jumpBits;
		
		// check if it's a 'jump instruction' or 'dest instruction' and get the relevant bits accordingly
		String[] lineSplit;
		
		int equalIndex = line.indexOf("=");
		int colonIndex = line.indexOf(";");

		if (equalIndex > 0) {
			lineSplit = line.split("=");
			destBits = parseDest(lineSplit[0]);
			compBits = parseComp(lineSplit[1]);
			jumpBits = "000";
		} else { // it's a jump instruction
			lineSplit = line.split(";");
			jumpBits = parseJump(lineSplit[1]);
			compBits = parseComp(lineSplit[0]);
			destBits = "000";
		}
		
		return compBits + destBits + jumpBits;
	}

	public static void initializeSymbols(){
		symbols = new HashMap<>();
		for (int i = 0; i < 16; i++){
		    symbols.put("R" + i, i);
		}
		symbols.put("SCREEN", 16384);
		symbols.put("KBD", 24576);
		symbols.put("SP", 0);
		symbols.put("LCL", 1);
		symbols.put("ARG", 2);
		symbols.put("THIS", 3);
		symbols.put("THAT", 4);
	}
	
	public static ArrayList<String> scanLables(String[] lines, ArrayList<String> result){
		int lineNumber = 0;
		for (String line : lines){
			
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
			
		    //remove all white spaces from the line:
		    line = line.replaceAll("\\s+","");
		    
		    //Check if the line is a Label declaration
		    if (line.startsWith("(")){
		    	String label = line.substring(1, line.length() - 1);
		    	symbols.put(label, lineNumber);
		    	continue;
		    }
		    result.add(line);
		    lineNumber++;
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException{
		variableCounter = 16;
	
		initializeSymbols();
		ArrayList<String> cleanCode = new ArrayList<String>();
		String outputFileName = args[0].replace("asm", "hack");
		FileOutputStream output = new FileOutputStream(outputFileName);
		String bits = "";
		InputStream f = new FileInputStream(args[0]);
	    int size = f.available();
	    String file_content = "";
	    for(int i=0; i< size; i++){
	       file_content += (char)f.read();
	    }
	    f.close();
	    String lines[] = file_content.split("\\r?\\n"); //split the file contents to lines
	    
	    //'clean' the code
		cleanCode = scanLables(lines, cleanCode);
	    
	    for (String line : cleanCode){
		    //else - figure out if the line is a 'C command' or an 'A command'
		    if (line.startsWith("@")){
		    	bits = parseACommand(line);
		    }
		    else{ // it's a C command
		    	bits = "111" + parseCCommand(line);
		    }
		    bits += "\n";
		    output.write(bits.getBytes());
	    }
	    output.flush();
	    output.close();
	}
}
