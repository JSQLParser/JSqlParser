package net.baozi.jmysqlparser.test;

import java.io.*;

public class Query{
    
    /*
     * @param String [] args
     *        args[0]  input_file    cleaned by clean.sh
     *        args[1]  output_file   record statement parsed               ("type"  "table name ...")
     *        args[2]  err_file      record statement failed parse         ("origin statement" 
     *                                                                      "exception name "
     *                                                                      "exception details")
     */
    public static void main(String [] args){
        SimpleQuery q = null;
        
      // TODO Auto-generated method stub
//        File fileIn = new File(Query.class.getResource("/") + "complex_sql");
        
        //java Query in out err
        //           0  1   2
        File fileIn = new File(args[0]);
        File fileOut = new File(args[1]);
        File fileErr = new File(args[2]);
    
        BufferedReader reader = null;
        BufferedWriter writerOut = null;
        BufferedWriter writerErr = null;
    
        try{
            reader = new BufferedReader(new FileReader(fileIn));
            writerOut = new BufferedWriter(new FileWriter(fileOut));
            writerErr = new BufferedWriter(new FileWriter(fileErr));
            
            String QueryString = null;
            String tmpString = null;
            
            while((QueryString = reader.readLine()) != null){
                
                try{
                    q = new SimpleQuery(QueryString);
                    
                    tmpString = q.get_type();
                    for(String tablename : q.get_tables()){
                        tmpString += " ";
                        tmpString += tablename;
                    }
                    
                    writerOut.write(tmpString + "\n");
                    
                }catch(Exception e1){
                    writerErr.write(QueryString + "\n" + 
                                    e1.toString()+ "\n" + 
                                    e1.getMessage() + "\n\n");
                }
            }
            
            reader.close();
            writerOut.close();
            writerErr.close();
            
        }catch(Exception e){
//            e.printStackTrace();        
        }
    }
}