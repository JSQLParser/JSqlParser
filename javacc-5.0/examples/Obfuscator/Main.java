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

public class Main extends Globals {

  /*
   * This main program collects the input arguments.  If one or more of the three
   * latter arguments are present, then these files are opened and parsed with the
   * appropriate parsers.  Then the method Obfuscator.start() is called that does
   * the actual obfuscation.  Finally, the file map.log is generated.
   */
  public static void main(String[] args) throws FileNotFoundException {
    IdsFile idparser = null;
    if (args.length < 2 || args.length > 5) {
      System.out.println("Usage is \"java Main <inputdir> <outputdir> <mapsfile> <nochangeidsfile> <useidsfile>\"");
      System.out.println("  <inputdir> must be the CLASSPATH directory.");
      System.out.println("  <mapsfile>, <nochangeidsfile>, and <useidsfile> are optional, but if any of");
      System.out.println("  these are present, then the ones preceding them must also be present.");
      return;
    }
    inpDir = new File(args[0]);
    if (!inpDir.isDirectory()) {
      System.out.println("Error: " + args[0] + " is not a directory.");
      return;
    }
    outDir = new File(args[1]);
    if (outDir.exists()) {
      if (!outDir.isDirectory()) {
        System.out.println("Error: " + args[1] + " is not a directory.");
        return;
      }
    } else {
      System.out.println(args[1] + " does not exist.  Will create it.");
      if (!outDir.mkdirs()) {
        System.out.println("Could not create directory " + args[1]);
        return;
      }
    }
    if (args.length >= 4) {
      try {
        idparser = new IdsFile(new FileInputStream(args[3]));
        idparser.input(true, args[3]);
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[3]);
        return;
      }
    }
    if (args.length >= 3) {
      try {
        MapFile mapparser = new MapFile(new FileInputStream(args[2]));
        mapparser.input();
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[2]);
        return;
      }
    }
    if (args.length == 5) {
      try {
        idparser.ReInit(new FileInputStream(args[4]));
        idparser.input(false, args[4]);
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[4]);
        return;
      }
    }
    mappings.put("main", "main");
    Obfuscator.start();
    System.out.println("Dumping mappings used into map.log.");
    PrintWriter mstr;
    try {
      mstr = new PrintWriter(new FileWriter("map.log"));
    } catch (IOException e) {
      System.out.println("Could not create file map.log");
      throw new Error();
    }
    for (Enumeration enumeration = mappings.keys(); enumeration.hasMoreElements();) {
      String from = (String)enumeration.nextElement();
      String to = (String)mappings.get(from);
      mstr.println(from + " -> " + to + ";");
    }
    mstr.close();
  }

}
