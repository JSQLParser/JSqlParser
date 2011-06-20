import java.io.*;

class Test
{
   public static void main(String[] args) throws Exception
   {
      Reader fr = null;
      if (args.length == 2)
         fr = new InputStreamReader(new FileInputStream(new File(args[0])), args[1]);
      else
         fr = new InputStreamReader(new FileInputStream(new File(args[0])));
      JavaParser jp = new JavaParser(fr);
      jp.CompilationUnit();
   }
}
