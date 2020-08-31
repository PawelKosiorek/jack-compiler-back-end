import java.io.*;
import java.util.*;

public class VMTranslator {

    public static void main(String args[]) throws Exception{
      File fileToRead =  new File(args[0]);
      if(fileToRead.isDirectory()) {
        CodeWriter codeWriter = new CodeWriter(args[0]);
        File[] files = fileToRead.listFiles();
        Arrays.sort(files);
        for(int i = 0; i < files.length; i++) {
          if(files[i].getName().equals("Sys.vm")) {
            Parser parser = new Parser(codeWriter);
            parser.readFile(files[i]);
          }
      }
      for(int i = 0; i < files.length; i++) {
        if(files[i].getName().endsWith(".vm") && !(files[i].getName().equals("Sys.vm")))    {
          Parser parser = new Parser(codeWriter);
          parser.readFile(files[i]);
      }
    } codeWriter.close();
  } else {
      CodeWriter codeWriter = new CodeWriter(args[0]);
      Parser parser = new Parser(codeWriter);
      parser.readFile(fileToRead);
      codeWriter.close();
    }
  }
}
