package net.baozi.jmysqlparser.test;

import java.io.StringReader;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SimpleQuery implements StatementVisitor {

    private String _type;

    private List<String> _tables;

    SimpleQuery(String sql) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement stm = parserManager.parse(new StringReader(sql));
        stm.accept(this);
    }
    
    public static void main(String[]args) throws JSQLParserException{
        SimpleQuery sq = new SimpleQuery("delete table1,table2;#SELECT * FROM mytable WHERE (trim(a), trim(b)) IN (SELECT a, b FROM mytable2)");
        
        System.out.println(sq.get_type());
        for(String tablename : sq.get_tables())
            System.out.println(tablename);
    }
    
    @Override
    public void visit(Select select) {
        // TODO Auto-generated method stub
        set_type(select.getClass().getSimpleName());
        
        TablesNamesFinder tnf = new TablesNamesFinder();
        set_tables(tnf.getTableList(select));
    }

    @Override
    public void visit(Delete delete) {
        // TODO Auto-generated method stub
        set_type(delete.getClass().getSimpleName());
        
        TablesNamesFinder tnf = new TablesNamesFinder();
        set_tables(tnf.getTableList(delete));
    }

    @Override
    public void visit(Update update) {
        // TODO Auto-generated method stub
        set_type(update.getClass().getSimpleName());
        
        TablesNamesFinder tnf = new TablesNamesFinder();
        set_tables(tnf.getTableList(update));
    }

    @Override
    public void visit(Insert insert) {
        // TODO Auto-generated method stub
        set_type(insert.getClass().getSimpleName());
        
        TablesNamesFinder tnf = new TablesNamesFinder();
        set_tables(tnf.getTableList(insert));
    }

    @Override
    public void visit(Replace replace) {
        // TODO Auto-generated method stub
        set_type(replace.getClass().getSimpleName());
        
        TablesNamesFinder tnf = new TablesNamesFinder();
        set_tables(tnf.getTableList(replace));
    }

    @Override
    public void visit(Drop drop) {
        // TODO Auto-generated method stub
        set_type(drop.getClass().getSimpleName());
    }

    @Override
    public void visit(Truncate truncate) {
        // TODO Auto-generated method stub
        set_type(truncate.getClass().getSimpleName());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        // TODO Auto-generated method stub
        set_type(createIndex.getClass().getSimpleName());
    }

    @Override
    public void visit(CreateTable createTable) {
        // TODO Auto-generated method stub
        set_type(createTable.getClass().getSimpleName());
    }

    @Override
    public void visit(CreateView createView) {
        // TODO Auto-generated method stub
        set_type(createView.getClass().getSimpleName());
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public List<String> get_tables() {
        return _tables;
    }

    public void set_tables(List<String> _tables) {
        this._tables = _tables;
    }

}
