import java.util.*;
import java.lang.String;
import java.io.*;
import java.util.Scanner;


public class Parser {

    private String filename;
    public CodeWriter codeWriter;


    public Parser(CodeWriter codeWriter1) {


        codeWriter = codeWriter1;
    }

    private String commandType(String[] line) {

        HashMap<String, String> commandTypeS = new HashMap<>();
        commandTypeS.put("add", "C_ARITHMETIC");
        commandTypeS.put("sub", "C_ARITHMETIC");
        commandTypeS.put("neg", "C_ARITHMETIC");
        commandTypeS.put("eq", "C_ARITHMETIC");
        commandTypeS.put("gt", "C_ARITHMETIC");
        commandTypeS.put("lt", "C_ARITHMETIC");
        commandTypeS.put("and", "C_ARITHMETIC");
        commandTypeS.put("or", "C_ARITHMETIC");
        commandTypeS.put("not", "C_ARITHMETIC");
        commandTypeS.put("push", "C_PUSH");
        commandTypeS.put("pop", "C_POP");
        commandTypeS.put("label", "C_LABEL");
        commandTypeS.put("goto", "C_GOTO");
        commandTypeS.put("if-goto", "C_IF-GOTO");
        commandTypeS.put("function", "C_FUNCTION");
        commandTypeS.put("return", "C_RETURN");
        commandTypeS.put("call", "C_CALL");

        if(commandTypeS.containsKey(line[0])) return commandTypeS.get(line[0]);
        else return "NOT A COMMAND";

    }

    private String arg1(String[] line1, String commandType) {
        if(commandType.equals("C_ARITHMETIC")) return line1[0];
        else return line1[1];
    }

    private Integer arg2(String[] line2) {
        return Integer.parseInt(line2[2]);

    }
    public void readFile(File fileToRead) throws Exception{
        String[] lineSplit = new String[3];
        String commandTypeS = new String("");
        String argString = new String("");
        Integer argInteger = new Integer(-1);
        String[] argIntReturn = {"C_POP", "C_FUNCTION", "C_PUSH", "C_CALL"};
        Scanner scanner = new Scanner(fileToRead);
        NOT_A_COMMAND: while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.length() == 0) continue NOT_A_COMMAND;
            if(line.substring(0, 2).equals("//")) continue NOT_A_COMMAND;
            if(line.contains("//")) {
            line = line.substring(0, line.indexOf("//"));
            }
            lineSplit = line.split(" ");
            for(int i = 0; i < lineSplit.length; i++) {
              lineSplit[i] = lineSplit[i].replaceAll("\\s+", "");
            }
            commandTypeS = commandType(lineSplit);
            if(!commandTypeS.equals("C_RETURN")) argString = arg1(lineSplit, commandTypeS);
            for(int i = 0; i < 4; i++) {
                if(commandTypeS.equals(argIntReturn[i])) {
                    argInteger = arg2(lineSplit);
                }
            }
            codeWriter.assemblyBuilder(fileToRead, commandTypeS, argString, argInteger);
        }
    }
}
