import java.io.*;

class Main
{
   private static int parseFilesFromFileList(String fileName)
   {
      LineNumberReader str = null;
      int cnt = 0;
      try
      {
         str = new LineNumberReader(new FileReader(new File(fileName))
);
         String s;

         while ((s = str.readLine()) != null)
         {
            try
            {
               cnt++;
               System.out.println("Parsing: " + s);
               new JavaParser(s).CompilationUnit();
            }
            catch(ParseException e) { e.printStackTrace(); }
            catch(TokenMgrError tme) { tme.printStackTrace(); }
         }
      }
      catch(Exception e) { e.printStackTrace(); }
      finally { if (str != null) try { str.close(); } catch(Exception e) {}  }

      return cnt;
   }

   public static void main(String[] args) throws Throwable
   {
      int cnt = 1;
      long l = System.currentTimeMillis();
      JavaParser parser;
      if (args[0].charAt(0) == '@')
      {
         cnt = parseFilesFromFileList(args[0].substring(1));
      }
      else
      {
         JavaParser.main(args);
      }
      System.out.println("Parsed " + cnt + " files in: " + (System.currentTimeMillis() - l));
   }
}
