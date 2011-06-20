/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.io.*;
import java.util.*;

public class Obfuscator extends Globals {

  // These data structures implement a stack that is used to recursively
  // walk the input directory structure looking for Java files.

  static String[][] dirStack = new String[100][];
  static int[] dirStackIndex = new int[100];
  static int dirStackSize;
  static File[] dirFile = new File[100];

  static {
    dirFile[0] = inpDir;
    dirStackSize = 1;
    dirStack[dirStackSize] = dirFile[dirStackSize-1].list();
    dirStackIndex[dirStackSize] = 0;
  }

  // Returns true if this is a Java file.
  static boolean javaFile(String name) {
    if (name.length() < 6) {
      return false;
    }
    if (name.substring(name.length() - 5).equals(".java")) {
      return true;
    }
    return false;
  }

  // The iterator.  This uses the above datastructures to walk the input
  // directory tree.  Everytime it finds a Java file or when it cannot find
  // any more Java file, it returns to the caller.
  static void nextJavaFile() {
    while (true) {
      if (dirStackIndex[dirStackSize] == dirStack[dirStackSize].length) {
        dirStackSize--;
        if (dirStackSize == 0) {
          return;
        } else {
          dirStackIndex[dirStackSize]++;
        }
      } else {
        dirFile[dirStackSize] = new File(dirFile[dirStackSize-1],
                                         dirStack[dirStackSize][dirStackIndex[dirStackSize]]);
        if (dirStack[dirStackSize][dirStackIndex[dirStackSize]].equals("SCCS")) {
          dirStackIndex[dirStackSize]++;
        } else if (dirFile[dirStackSize].isDirectory()) {
          dirStackSize++;
          dirStack[dirStackSize] = dirFile[dirStackSize-1].list();
          dirStackIndex[dirStackSize] = 0;
        } else if (javaFile(dirStack[dirStackSize][dirStackIndex[dirStackSize]])) {
          dirStackIndex[dirStackSize]++;
          return;
        } else {
          dirStackIndex[dirStackSize]++;
        }
      }
    }
  }

  // The main Obfuscator routine.  It calls the iterator for each Java file to
  // work on.  It then creates the output file, then parses the input Java file
  // to determine whether or not it has a main program and also to collect the
  // tokens that make up this file.  It then calls printOutputFile that takes
  // first token and walks the next field chain printing tokens as it goes along.
  // Finally a main program is created if necessary.
  static public void start() {
    boolean parserInitialized = false;
    JavaParser parser = null;
    Token first;
    while (true) {
      nextJavaFile();
      if (dirStackSize == 0) {
        break;
      }
      createOutputFile();
      System.out.println("Obfuscating " + dirFile[dirStackSize].getPath());
      System.out.println("       into " + outFile.getPath());
      try {
	if (parserInitialized) {
	  parser.ReInit(new FileInputStream(dirFile[dirStackSize]));
	} else {
	  parser = new JavaParser(new FileInputStream(dirFile[dirStackSize]));
	  parserInitialized = true;
	}
        first = parser.CompilationUnit(dirStack[dirStackSize][dirStackIndex[dirStackSize]-1]);
      } catch (ParseException e1) {
        System.out.println("Parse error in file " + dirFile[dirStackSize].getPath());
        throw new Error();
      } catch (IOException e2) {
        System.out.println("Could not open file " + dirFile[dirStackSize].getPath());
        throw new Error();
      }
      printOutputFile(first);
      if (mainExists) {
        createMainClass();
      }
    }
  }

  static File outFile;
  static PrintWriter ostr;

  static void createOutputFile() {
    // sets outFile and ostr
    outFile = outDir;
    for (int i = 1; i < dirStackSize; i++) {
      outFile = new File(outFile, map(dirStack[i][dirStackIndex[i]]));
      if (outFile.exists()) {
        if (!outFile.isDirectory()) {
          System.out.println("Unexpected error!");
          throw new Error();
        }
      } else {
        if (!outFile.mkdir()) {
          System.out.println("Could not create directory " + outFile.getPath());
          throw new Error();
        }
      }
    }
    String origFileName = dirStack[dirStackSize][dirStackIndex[dirStackSize]-1];
    String newFileName = map(origFileName.substring(0, origFileName.length() - 5)) + ".java";
    outFile = new File(outFile, newFileName);
    try {
      ostr = new PrintWriter(new FileWriter(outFile));
    } catch (IOException e) {
      System.out.println("Could not create file " + outFile.getPath());
      throw new Error();
    }
  }

  static void printOutputFile(Token first) {
    Token t = first;
    for (int i = 1; i < t.beginColumn; i++) {
      ostr.print(" ");
    }
    while (true) {
      if (t.kind == JavaParserConstants.IDENTIFIER) {
        t.image = map(t.image);
      }
      ostr.print(addUnicodeEscapes(t.image));
      if (t.next == null) {
        ostr.println("");
        break;
      }
      if (t.endLine != t.next.beginLine) {
        for (int i = t.endLine; i < t.next.beginLine; i++) {
          ostr.println("");
        }
        for (int i = 1; i < t.next.beginColumn; i++) {
          ostr.print(" ");
        }
      } else {
        for (int i = t.endColumn+1; i < t.next.beginColumn; i++) {
          ostr.print(" ");
        }
      }
      t = t.next;
    }
    ostr.close();
  }

  static String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (ch < 0x20 || ch > 0x7e) {
	String s = "0000" + Integer.toString(ch, 16);
	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
  }

  // This creates a main program if there was one in the original file.  This
  // main program has the same name and resides in the same package as the original
  // file and it simply calls the obfuscated main program.  This allows scripts
  // to continue to work.
  static void createMainClass() {
    PrintWriter mstr;
    boolean mustCreate = false;
    File mFile = outDir;
    for (int i = 1; i < dirStackSize; i++) {
      mFile = new File(mFile, dirStack[i][dirStackIndex[i]]);
      mustCreate = mustCreate || !map(dirStack[i][dirStackIndex[i]]).equals(dirStack[i][dirStackIndex[i]]);
      if (mFile.exists()) {
        if (!mFile.isDirectory()) {
          System.out.println("Error: Created file " + mFile.getPath() + ", but need to create a main program with the same path prefix.  Please remove identifiers from the path prefix from your <useidsfile> and run again.");
          throw new Error();
        }
      }
    }
    String origFileName = dirStack[dirStackSize][dirStackIndex[dirStackSize]-1];
    String newFileName = map(origFileName.substring(0, origFileName.length() - 5)) + ".java";
    if (!mustCreate && origFileName.equals(newFileName)) {
      return; // this main program has not been obfuscated.
    }
    if (!mFile.exists() && !mFile.mkdirs()) {
      System.out.println("Could not create " + mFile.getPath());
      throw new Error();
    }
    mFile = new File(mFile, origFileName);
    try {
      mstr = new PrintWriter(new FileWriter(mFile));
    } catch (IOException e) {
      System.out.println("Could not create " + mFile.getPath());
      throw new Error();
    }
    System.out.print("Generating main program ");
    String pname = "";
    if (dirStackSize > 1) {
      for (int i = 1; i < dirStackSize; i++) {
        pname += "." + dirStack[i][dirStackIndex[i]];
      }
      mstr.println("package " + pname.substring(1) + ";");
      System.out.print(pname.substring(1) + ".");
      mstr.println("");
    }
    System.out.println(origFileName.substring(0, origFileName.length() - 5));
    mstr.println("public class " + origFileName.substring(0, origFileName.length() - 5) + " {");
    mstr.println("");
    mstr.println("  public static void main(String[] args) {");
    pname = "";
    for (int i = 1; i < dirStackSize; i++) {
      pname += map(dirStack[i][dirStackIndex[i]]) + ".";
    }
    mstr.println("    " + pname + map(origFileName.substring(0, origFileName.length() - 5)) + ".main(args);");
    mstr.println("  }");
    mstr.println("");
    mstr.println("}");
    mstr.close();
  }

}
