import java.io.*;
import java.util.*;
import java.lang.String;



public class CodeWriter {

    private String filename;
    private PrintWriter writeToFile;
    private Integer eqCount = new Integer(0);
    private Integer ltCount = new Integer(0);
    private Integer gtCount = new Integer(0);
    private Boolean ifBootstrap = false;
    private String functLabel = new String("");

    private HashMap<String, Integer> numCalls = new HashMap<>();

    public CodeWriter(String fileToWrite) throws Exception{
        filename = setFileName(fileToWrite);
        writeToFile = new PrintWriter(filename);

    }

    private String setFileName(String filename) {
        String localFilename = new String("");

        if(filename.contains(".vm")) {
         localFilename = filename.substring(0, filename.indexOf(".")) + ".asm";
         ifBootstrap = true;

      }
        else {
          String[] localFilenameSplit = filename.split(File.separator);
          localFilename = localFilenameSplit[0];
          for(int i = 1; i < localFilenameSplit.length; i++) {

            localFilename = localFilename+"/"+localFilenameSplit[i];
          }
          localFilename = localFilename+"/"+localFilenameSplit[localFilenameSplit.length - 1]+".asm";


      }
      return localFilename;
    }

    private String writeLabel(String label) {
      String line = new String("");
      label = functLabel+"$"+label;
      line = "("+label+")\n";
      return line;
    }

    private String writeGoto(String label) {
      String line = new String("");
      label = functLabel+"$"+label;
      line = "// GOTO "+label+"         //\n"+"@"+label+"\n"+"0;JMP\n";
      return line;

    }

    private String writeIfGoto(String label) {
      String line = new String("");
      label = functLabel+"$"+label;
      line = "// IF-GOTO "+label+"            //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@"+label+"\n"+"D;JNE\n";
      return line;

    }

    private String writeCall(String functionName, Integer numArgs) {
        String returnAddress = new String("");
        if(numCalls.containsKey(functionName)) {
          Integer temp = numCalls.get(functionName);
          temp++;
          numCalls.put(functionName, temp);
        } else {
          numCalls.put(functionName, 0);
        }
        returnAddress = functionName +numCalls.get(functionName)+ "RETURN";
        String pushReturnAddress = new String("@" + returnAddress + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n"
        + "@SP\n" + "M=M+1\n");
        String pushLCL = new String("@LCL\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
        String pushARG = new String("@ARG\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
        String pushTHIS = new String("@THIS\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
        String pushTHAT = new String("@THAT\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
        String repositionArg = new String("@5\n" + "D=A\n" + "@" + numArgs + "\n" + "D=D+A\n" + "@SP\n"
                + "A=M\n" + "A=A-D\n" + "D=A\n" + "@ARG\n" + "M=D\n");
        String repositionLCL = new String("@SP\n" + "D=M\n" + "@LCL\n" + "M=D\n");
        String gotoF = new String("@" + functionName + "\n" + "0;JMP\n");
        String line = "// CALL "+functionName +" "+numArgs+"\n"+pushReturnAddress + pushLCL + pushARG + pushTHIS + pushTHAT +
                repositionArg + repositionLCL + gotoF + "(" + returnAddress + ")\n";


        return line;

    }

    private String writeReturn() {
      String line = new String("");
      String frameLCL = new String("@LCL\n"+"D=M\n"+"@R13\n"+"M=D\n");
      String ret5Deref = new String("@R13\n"+"D=M\n"+"@5\n"+"D=D-A\n"+"A=D\n"+"D=M\n"+"@R14\n"+"M=D\n");
      String argPop = new String("@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@ARG\n"+"A=M\n"+"M=D\n");
      String spArg = new String("@ARG\n"+"D=M\n"+"@SP\n"+"M=D+1\n");
      String thatEq = new String("@R13\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@THAT\n"+"M=D\n");
      String thisEq = new String("@R13\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@THIS\n"+"M=D\n");
      String argEq = new String("@R13\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@ARG\n"+"M=D\n");
      String lclEq = new String("@R13\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@LCL\n"+"M=D\n");
      String gotoRet = new String("@R14\n"+"A=M\n"+"0;JMP\n");
      line = "// RETURN            //\n"+frameLCL+ret5Deref+argPop+spArg+thatEq+thisEq+argEq+lclEq+gotoRet;
      return line;
    }

    private String writeFunction(String functionName, Integer numLocals) {
        String line = new String("");
        line = "// FUNCTION "+functionName +" "+numLocals+"          //\n"+"(" + functionName + ")\n"
        + "@"+numLocals+"\n"+
        "D=A\n"+"@"+functionName+"END\n"+"D;JEQ\n"+"("+functionName+"LOOP)\n"
        +"@SP\n"+"A=M\n"+"M=0\n"+"D=D-1\n"+"@SP\n"+"M=M+1\n"+"@"+functionName+"LOOP\n"
        +"D;JNE\n"+"("+functionName+"END)\n";
        functLabel = functionName;
        return line;
    }

    private String getArithmetic(String command) {

        String line = new String("");
        if(command.equals("add")) {

            line = "// "+command +"          //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+
            "D=D+M\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

        } else if(command.equals("sub")) {

            line = "// "+command +"           //\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"
            +"A=M\n"+"D=D-M\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

        } else if(command.equals("neg")){

            line = "// "+command +"          //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=-M\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

        } else if(command.equals("eq")) {
            String eqCountStr = new String(Integer.toString(eqCount));
            line = "// "+command +"        //\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"+"A=M\n"+
            "A=M\n"+"D=D-A\n"+"@EQUAL"+eqCountStr+"\n"+"D;JEQ\n"+"@SP\n"+"M=M-1\n"+"A=M\n"
            +"M=0\n"+"@ENDEQ"+eqCountStr+
            "\n"+"0;JMP\n"+"(EQUAL"+eqCountStr
            +")\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=-1\n"+"(ENDEQ"+eqCountStr+")\n"+"@SP\n"+"M=M+1\n";
            eqCount++;

        } else if(command.equals("gt")) {
            String gtCountStr = new String(Integer.toString(gtCount));
            line = "// "+command +         "//\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"+"A=M\n"+"A=M\n"+
            "D=D-A\n"+"@GREATER"+gtCountStr+"\n"+"D;JGT\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=0\n"+"@ENDGT"
            +gtCountStr+"\n"+"0;JMP\n"+"(GREATER"+gtCountStr+")\n"
            +"@SP\n"+"M=M-1\n"+"A=M\n"+"M=-1\n"+"(ENDGT"+gtCountStr+")\n"+"@SP\n"+"M=M+1\n";
            gtCount++;

        } else if(command.equals("lt")) {
            String ltCountStr = new String(Integer.toString(ltCount));
            line = "// "+command +"           //\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"+"A=M\n"+"A=M\n"+
            "D=D-A\n"+"@SMALLER"+ltCountStr+"\n"+"D;JLT\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=0\n"
            +"@ENDLT"+ltCountStr+"\n"+"0;JMP\n"+"(SMALLER"+ltCountStr+")\n"+
            "@SP\n"+"M=M-1\n"+"A=M\n"+"M=-1\n"+"(ENDLT"+ltCountStr+")\n"+"@SP\n"+"M=M+1\n";
            ltCount++;

        } else if(command.equals("and")) {

            line = "// "+command +"        //\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"+"A=M\n"
            +"D=D&M\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

        } else if(command.equals("or")) {

            line = "// "+command +"        //\n"+"@SP\n"+"M=M-1\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@SP\n"+"M=M+1\n"+"A=M\n"
            +"D=D|M\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

        } else if(command.equals("not")) {

            line = "// "+command +"         //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"M=!M\n"+"@SP\n"+"M=M+1\n";
        }

        return line;

    }
    private String getPushPop(File className, String command, String segment, Integer index) {
        String line = new String("");
        String segmentPointer = new String("");
        String indexToString = new String(Integer.toString(index));
        HashMap<String, String> SPointers = new HashMap<>();
        SPointers.put("local", "LCL");
        SPointers.put("this", "THIS");
        SPointers.put("that", "THAT");
        SPointers.put("argument", "ARG");
        if(SPointers.containsKey(segment)) segmentPointer = SPointers.get(segment);
        if(command.equals("C_PUSH")) {
            if(segment.equals("constant")) {

                line = "// "+command+" "+segment+" "+index+"       //\n"+"@"+indexToString+"\n"+"D=A\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

            } else if(segment.equals("temp")) {

                line = "// "+command+" "+segment+" "+index+"        //\n"+"@"+"5"+"\n"+"D=A\n"+"@"+indexToString+
                "\n"+"D=D+A\n"+"A=D\n"+"D=M\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

            } else if(segment.equals("pointer")) {

                String thisOrThat = new String("");

                if(indexToString.equals("0")) thisOrThat = "THIS";
                if(indexToString.equals("1")) thisOrThat = "THAT";

                line = "// "+command+" "+segment+" "+index+"       //\n"+"@"+thisOrThat+"\n"+"A=M\n"+"D=A\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";

            } else if(segment.equals("static")) {

                String temp = className.getName();
                temp = temp.substring(0, temp.indexOf("."));

                line ="// "+command+" "+segment+" "+index+"       //\n"+"@"+temp+"."+indexToString+"\n"+"D=M\n"+"@SP\n"+"A=M\n"+
                "M=D\n"+"@SP\n"+"M=M+1\n";

            } else {

                line = "// "+command+" "+segment+" "+index+"      //\n"+"@"+segmentPointer+"\n"+"D=M\n"+"@"+indexToString +
                "\n"+"D=D+A\n"+"A=D\n"+"D=M\n"+"@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";
            }

        } else if(command.equals("C_POP")) {
            if(segment.equals("temp")) {

                line = "// "+command+" "+segment+" "+index+"        //\n"+"@"+"5"+"\n"+"D=A\n"+"@"+indexToString+"\n"+"D=D+A\n"+
                "@13\n"+"M=D\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@13\n"+"A=M\n"+"M=D\n";

            } else if(segment.equals("pointer")) {

                String thisOrThat = new String("");

                if(indexToString.equals("0")) thisOrThat = "THIS";
                if(indexToString.equals("1")) thisOrThat = "THAT";

                line = "// "+command+" "+segment+" "+index+"     //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@"+thisOrThat+"\n"+"M=D\n";

            } else if(segment.equals("static")) {

                String temp = className.getName();
                temp = temp.substring(0, temp.indexOf("."));

                line = "// "+command+" "+segment+" "+index+"        //\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@"+temp+"."+indexToString+"\n"+
                "M=D\n";

            } else {

                line = "// "+command+" "+segment+" "+index+"       //\n"+"@"+segmentPointer+"\n"+"D=M\n"+"@"+indexToString+"\n"+"D=D+A\n"+
                "@13\n"+"M=D\n"+"@SP\n"+"M=M-1\n"+"A=M\n"+"D=M\n"+"@13\n"+"A=M\n"+"M=D\n";
            }
        }
        return line;

    }
    public void close() {
        writeToFile.close();
    }
    public void writeLine(String line) {
        writeToFile.print(line);

    }



    public void assemblyBuilder(File className, String commandType, String arg1, Integer arg2) {
        String stringToWrite = new String("");
        if(ifBootstrap == false)  {
          stringToWrite = "@256\n"+"D=A\n"+"@0\n"+"M=D\n"+writeCall("Sys.init", 0);
          writeLine(stringToWrite);
          ifBootstrap = true;
        }
        if(commandType.equals("C_ARITHMETIC")) {stringToWrite = getArithmetic(arg1);}

        else if(commandType.equals("C_LABEL")) {

          stringToWrite = writeLabel(arg1);

        } else if(commandType.equals("C_IF-GOTO")) {

          stringToWrite = writeIfGoto(arg1);

        } else if(commandType.equals("C_GOTO")) {

          stringToWrite = writeGoto(arg1);

        } else if(commandType.equals("C_FUNCTION")) {

            stringToWrite = writeFunction(arg1, arg2);

        } else if(commandType.equals("C_RETURN")) {

          stringToWrite = writeReturn();

        } else if (commandType.equals("C_CALL")) {

          stringToWrite = writeCall(arg1, arg2);
        }

        else  {
            stringToWrite = getPushPop(className, commandType, arg1, arg2);
        }

        writeLine(stringToWrite);

    }




}
