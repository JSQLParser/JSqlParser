package net.sf.jsqlparser.test.select;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

public class LargeTreeHelper {

    protected static Expression[] buildBase1() {
        Expression[] result = new Expression[44];
        result[0] = new DoubleValue("3.0");
        result[1] = new DoubleValue("4.0");
        result[2] = new DoubleValue("7.0");
        result[3] = new DoubleValue("8.0");
        result[4] = new DoubleValue("13.0");
        result[5] = new DoubleValue("14.0");
        result[6] = new DoubleValue("3.0");
        result[7] = new DoubleValue("4.0");
        result[8] = new DoubleValue("7.0");
        result[9] = new DoubleValue("8.0");
        result[10] = new DoubleValue("15.0");
        result[11] = new DoubleValue("16.0");
        result[12] = new DoubleValue("17.0");
        result[13] = new DoubleValue("18.0");
        result[14] = new DoubleValue("3.0");
        result[15] = new DoubleValue("4.0");
        result[16] = new DoubleValue("7.0");
        result[17] = new DoubleValue("8.0");
        result[18] = new DoubleValue("15.0");
        result[19] = new DoubleValue("16.0");
        result[20] = new DoubleValue("19.0");
        result[21] = new DoubleValue("20.0");
        result[22] = new DoubleValue("3.0");
        result[23] = new DoubleValue("4.0");
        result[24] = new DoubleValue("9.0");
        result[25] = new DoubleValue("10.0");
        result[26] = new DoubleValue("13.0");
        result[27] = new DoubleValue("14.0");
        result[28] = new DoubleValue("3.0");
        result[29] = new DoubleValue("4.0");
        result[30] = new DoubleValue("9.0");
        result[31] = new DoubleValue("10.0");
        result[32] = new DoubleValue("15.0");
        result[33] = new DoubleValue("16.0");
        result[34] = new DoubleValue("17.0");
        result[35] = new DoubleValue("18.0");
        result[36] = new DoubleValue("3.0");
        result[37] = new DoubleValue("4.0");
        result[38] = new DoubleValue("9.0");
        result[39] = new DoubleValue("10.0");
        result[40] = new DoubleValue("15.0");
        result[41] = new DoubleValue("16.0");
        result[42] = new DoubleValue("19.0");
        result[43] = new DoubleValue("20.0");
        return result;
    }
    
    protected static Expression[] buildBase2() {
        Expression[] result = new Expression[44];
        result[0] = new DoubleValue("3.0");
        result[1] = new DoubleValue("4.0");
        result[2] = new DoubleValue("11.0");
        result[3] = new DoubleValue("12.0");
        result[4] = new DoubleValue("13.0");
        result[5] = new DoubleValue("14.0");
        result[6] = new DoubleValue("3.0");
        result[7] = new DoubleValue("4.0");
        result[8] = new DoubleValue("11.0");
        result[9] = new DoubleValue("12.0");
        result[10] = new DoubleValue("15.0");
        result[11] = new DoubleValue("16.0");
        result[12] = new DoubleValue("17.0");
        result[13] = new DoubleValue("18.0");
        result[14] = new DoubleValue("3.0");
        result[15] = new DoubleValue("4.0");
        result[16] = new DoubleValue("11.0");
        result[17] = new DoubleValue("12.0");
        result[18] = new DoubleValue("15.0");
        result[19] = new DoubleValue("16.0");
        result[20] = new DoubleValue("19.0");
        result[21] = new DoubleValue("20.0");
        result[22] = new DoubleValue("5.0");
        result[23] = new DoubleValue("6.0");
        result[24] = new DoubleValue("7.0");
        result[25] = new DoubleValue("8.0");
        result[26] = new DoubleValue("13.0");
        result[27] = new DoubleValue("14.0");
        result[28] = new DoubleValue("5.0");
        result[29] = new DoubleValue("6.0");
        result[30] = new DoubleValue("7.0");
        result[31] = new DoubleValue("8.0");
        result[32] = new DoubleValue("15.0");
        result[33] = new DoubleValue("16.0");
        result[34] = new DoubleValue("17.0");
        result[35] = new DoubleValue("18.0");
        result[36] = new DoubleValue("5.0");
        result[37] = new DoubleValue("6.0");
        result[38] = new DoubleValue("7.0");
        result[39] = new DoubleValue("8.0");
        result[40] = new DoubleValue("15.0");
        result[41] = new DoubleValue("16.0");
        result[42] = new DoubleValue("19.0");
        result[43] = new DoubleValue("20.0");
        return result;
    }
    
    protected static Expression[] buildBase3() {
        Expression[] result = new Expression[44];
        result[0] = new DoubleValue("5.0");
        result[1] = new DoubleValue("6.0");
        result[2] = new DoubleValue("9.0");
        result[3] = new DoubleValue("10.0");
        result[4] = new DoubleValue("13.0");
        result[5] = new DoubleValue("14.0");
        result[6] = new DoubleValue("5.0");
        result[7] = new DoubleValue("6.0");
        result[8] = new DoubleValue("9.0");
        result[9] = new DoubleValue("10.0");
        result[10] = new DoubleValue("15.0");
        result[11] = new DoubleValue("16.0");
        result[12] = new DoubleValue("17.0");
        result[13] = new DoubleValue("18.0");
        result[14] = new DoubleValue("5.0");
        result[15] = new DoubleValue("6.0");
        result[16] = new DoubleValue("9.0");
        result[17] = new DoubleValue("10.0");
        result[18] = new DoubleValue("15.0");
        result[19] = new DoubleValue("16.0");
        result[20] = new DoubleValue("19.0");
        result[21] = new DoubleValue("20.0");
        result[22] = new DoubleValue("5.0");
        result[23] = new DoubleValue("6.0");
        result[24] = new DoubleValue("11.0");
        result[25] = new DoubleValue("12.0");
        result[26] = new DoubleValue("13.0");
        result[27] = new DoubleValue("14.0");
        result[28] = new DoubleValue("5.0");
        result[29] = new DoubleValue("6.0");
        result[30] = new DoubleValue("11.0");
        result[31] = new DoubleValue("12.0");
        result[32] = new DoubleValue("15.0");
        result[33] = new DoubleValue("16.0");
        result[34] = new DoubleValue("17.0");
        result[35] = new DoubleValue("18.0");
        result[36] = new DoubleValue("5.0");
        result[37] = new DoubleValue("6.0");
        result[38] = new DoubleValue("11.0");
        result[39] = new DoubleValue("12.0");
        result[40] = new DoubleValue("15.0");
        result[41] = new DoubleValue("16.0");
        result[42] = new DoubleValue("19.0");
        result[43] = new DoubleValue("20.0");
        return result;
    }
    
    public static Expression[] buildLogical1(Expression[] array) {
        Expression[] result = new Expression[22];
        BinaryExpression e133 = new GreaterThanEquals();
        e133.setLeftExpression(array[0]);
        e133.setRightExpression(array[1]);
        result[0] = e133;
        BinaryExpression e134 = new MinorThan();
        e134.setLeftExpression(array[2]);
        e134.setRightExpression(array[3]);
        result[1] = e134;
        BinaryExpression e135 = new NotEqualsTo();
        e135.setLeftExpression(array[4]);
        e135.setRightExpression(array[5]);
        result[2] = new NotExpression(e135);
        BinaryExpression e137 = new GreaterThanEquals();
        e137.setLeftExpression(array[6]);
        e137.setRightExpression(array[7]);
        result[3] = e137;
        BinaryExpression e138 = new MinorThan();
        e138.setLeftExpression(array[8]);
        e138.setRightExpression(array[9]);
        result[4] = e138;
        BinaryExpression e139 = new LikeExpression();
        e139.setLeftExpression(array[10]);
        e139.setRightExpression(array[11]);
        result[5] = new NotExpression(e139);
        BinaryExpression e141 = new EqualsTo();
        e141.setLeftExpression(array[12]);
        e141.setRightExpression(array[13]);
        result[6] = new NotExpression(e141);
        BinaryExpression e143 = new GreaterThanEquals();
        e143.setLeftExpression(array[14]);
        e143.setRightExpression(array[15]);
        result[7] = e143;
        BinaryExpression e144 = new MinorThan();
        e144.setLeftExpression(array[16]);
        e144.setRightExpression(array[17]);
        result[8] = e144;
        BinaryExpression e145 = new LikeExpression();
        e145.setLeftExpression(array[18]);
        e145.setRightExpression(array[19]);
        result[9] = new NotExpression(e145);
        BinaryExpression e147 = new GreaterThan();
        e147.setLeftExpression(array[20]);
        e147.setRightExpression(array[21]);
        result[10] = new NotExpression(e147);
        BinaryExpression e149 = new GreaterThanEquals();
        e149.setLeftExpression(array[22]);
        e149.setRightExpression(array[23]);
        result[11] = e149;
        BinaryExpression e150 = new GreaterThan();
        e150.setLeftExpression(array[24]);
        e150.setRightExpression(array[25]);
        result[12] = e150;
        BinaryExpression e151 = new NotEqualsTo();
        e151.setLeftExpression(array[26]);
        e151.setRightExpression(array[27]);
        result[13] = new NotExpression(e151);
        BinaryExpression e153 = new GreaterThanEquals();
        e153.setLeftExpression(array[28]);
        e153.setRightExpression(array[29]);
        result[14] = e153;
        BinaryExpression e154 = new GreaterThan();
        e154.setLeftExpression(array[30]);
        e154.setRightExpression(array[31]);
        result[15] = e154;
        BinaryExpression e155 = new LikeExpression();
        e155.setLeftExpression(array[32]);
        e155.setRightExpression(array[33]);
        result[16] = new NotExpression(e155);
        BinaryExpression e157 = new EqualsTo();
        e157.setLeftExpression(array[34]);
        e157.setRightExpression(array[35]);
        result[17] = new NotExpression(e157);
        BinaryExpression e159 = new GreaterThanEquals();
        e159.setLeftExpression(array[36]);
        e159.setRightExpression(array[37]);
        result[18] = e159;
        BinaryExpression e160 = new GreaterThan();
        e160.setLeftExpression(array[38]);
        e160.setRightExpression(array[39]);
        result[19] = e160;
        BinaryExpression e161 = new LikeExpression();
        e161.setLeftExpression(array[40]);
        e161.setRightExpression(array[41]);
        result[20] = new NotExpression(e161);
        BinaryExpression e163 = new GreaterThan();
        e163.setLeftExpression(array[42]);
        e163.setRightExpression(array[43]);
        result[21] = new NotExpression(e163);
        return result;
    }
    
    protected static Expression[] buildLogical2(Expression[] array) {
        Expression[] result = new Expression[22];
        BinaryExpression e165 = new GreaterThanEquals();
        e165.setLeftExpression(array[0]);
        e165.setRightExpression(array[1]);
        result[0] = e165;
        BinaryExpression e166 = new EqualsTo();
        e166.setLeftExpression(array[2]);
        e166.setRightExpression(array[3]);
        result[1] = e166;
        BinaryExpression e167 = new NotEqualsTo();
        e167.setLeftExpression(array[4]);
        e167.setRightExpression(array[5]);
        result[2] = new NotExpression(e167);
        BinaryExpression e169 = new GreaterThanEquals();
        e169.setLeftExpression(array[6]);
        e169.setRightExpression(array[7]);
        result[3] = e169;
        BinaryExpression e170 = new EqualsTo();
        e170.setLeftExpression(array[8]);
        e170.setRightExpression(array[9]);
        result[4] = e170;
        BinaryExpression e171 = new LikeExpression();
        e171.setLeftExpression(array[10]);
        e171.setRightExpression(array[11]);
        result[5] = new NotExpression(e171);
        BinaryExpression e173 = new EqualsTo();
        e173.setLeftExpression(array[12]);
        e173.setRightExpression(array[13]);
        result[6] = new NotExpression(e173);
        BinaryExpression e175 = new GreaterThanEquals();
        e175.setLeftExpression(array[14]);
        e175.setRightExpression(array[15]);
        result[7] = e175;
        BinaryExpression e176 = new EqualsTo();
        e176.setLeftExpression(array[16]);
        e176.setRightExpression(array[17]);
        result[8] = e176;
        BinaryExpression e177 = new LikeExpression();
        e177.setLeftExpression(array[18]);
        e177.setRightExpression(array[19]);
        result[9] = new NotExpression(e177);
        BinaryExpression e179 = new GreaterThan();
        e179.setLeftExpression(array[20]);
        e179.setRightExpression(array[21]);
        result[10] = new NotExpression(e179);
        BinaryExpression e181 = new MinorThanEquals();
        e181.setLeftExpression(array[22]);
        e181.setRightExpression(array[23]);
        result[11] = e181;
        BinaryExpression e182 = new MinorThan();
        e182.setLeftExpression(array[24]);
        e182.setRightExpression(array[25]);
        result[12] = e182;
        BinaryExpression e183 = new NotEqualsTo();
        e183.setLeftExpression(array[26]);
        e183.setRightExpression(array[27]);
        result[13] = new NotExpression(e183);
        BinaryExpression e185 = new MinorThanEquals();
        e185.setLeftExpression(array[28]);
        e185.setRightExpression(array[29]);
        result[14] = e185;
        BinaryExpression e186 = new MinorThan();
        e186.setLeftExpression(array[30]);
        e186.setRightExpression(array[31]);
        result[15] = e186;
        BinaryExpression e187 = new LikeExpression();
        e187.setLeftExpression(array[32]);
        e187.setRightExpression(array[33]);
        result[16] = new NotExpression(e187);
        BinaryExpression e189 = new EqualsTo();
        e189.setLeftExpression(array[34]);
        e189.setRightExpression(array[35]);
        result[17] = new NotExpression(e189);
        BinaryExpression e191 = new MinorThanEquals();
        e191.setLeftExpression(array[36]);
        e191.setRightExpression(array[37]);
        result[18] = e191;
        BinaryExpression e192 = new MinorThan();
        e192.setLeftExpression(array[38]);
        e192.setRightExpression(array[39]);
        result[19] = e192;
        BinaryExpression e193 = new LikeExpression();
        e193.setLeftExpression(array[40]);
        e193.setRightExpression(array[41]);
        result[20] = new NotExpression(e193);
        BinaryExpression e195 = new GreaterThan();
        e195.setLeftExpression(array[42]);
        e195.setRightExpression(array[43]);
        result[21] = new NotExpression(e195);
        return result;
    }
    
    protected static Expression[] buildLogical3(Expression[] array) {
        Expression[] result = new Expression[22];
        BinaryExpression e197 = new MinorThanEquals();
        e197.setLeftExpression(array[0]);
        e197.setRightExpression(array[1]);
        result[0] = e197;
        BinaryExpression e198 = new GreaterThan();
        e198.setLeftExpression(array[2]);
        e198.setRightExpression(array[3]);
        result[1] = e198;
        BinaryExpression e199 = new NotEqualsTo();
        e199.setLeftExpression(array[4]);
        e199.setRightExpression(array[5]);
        result[2] = new NotExpression(e199);
        BinaryExpression e201 = new MinorThanEquals();
        e201.setLeftExpression(array[6]);
        e201.setRightExpression(array[7]);
        result[3] = e201;
        BinaryExpression e202 = new GreaterThan();
        e202.setLeftExpression(array[8]);
        e202.setRightExpression(array[9]);
        result[4] = e202;
        BinaryExpression e203 = new LikeExpression();
        e203.setLeftExpression(array[10]);
        e203.setRightExpression(array[11]);
        result[5] = new NotExpression(e203);
        BinaryExpression e205 = new EqualsTo();
        e205.setLeftExpression(array[12]);
        e205.setRightExpression(array[13]);
        result[6] = new NotExpression(e205);
        BinaryExpression e207 = new MinorThanEquals();
        e207.setLeftExpression(array[14]);
        e207.setRightExpression(array[15]);
        result[7] = e207;
        BinaryExpression e208 = new GreaterThan();
        e208.setLeftExpression(array[16]);
        e208.setRightExpression(array[17]);
        result[8] = e208;
        BinaryExpression e209 = new LikeExpression();
        e209.setLeftExpression(array[18]);
        e209.setRightExpression(array[19]);
        result[9] = new NotExpression(e209);
        BinaryExpression e211 = new GreaterThan();
        e211.setLeftExpression(array[20]);
        e211.setRightExpression(array[21]);
        result[10] = new NotExpression(e211);
        BinaryExpression e213 = new MinorThanEquals();
        e213.setLeftExpression(array[22]);
        e213.setRightExpression(array[23]);
        result[11] = e213;
        BinaryExpression e214 = new EqualsTo();
        e214.setLeftExpression(array[24]);
        e214.setRightExpression(array[25]);
        result[12] = e214;
        BinaryExpression e215 = new NotEqualsTo();
        e215.setLeftExpression(array[26]);
        e215.setRightExpression(array[27]);
        result[13] = new NotExpression(e215);
        BinaryExpression e217 = new MinorThanEquals();
        e217.setLeftExpression(array[28]);
        e217.setRightExpression(array[29]);
        result[14] = e217;
        BinaryExpression e218 = new EqualsTo();
        e218.setLeftExpression(array[30]);
        e218.setRightExpression(array[31]);
        result[15] = e218;
        BinaryExpression e219 = new LikeExpression();
        e219.setLeftExpression(array[32]);
        e219.setRightExpression(array[33]);
        result[16] = new NotExpression(e219);
        BinaryExpression e221 = new EqualsTo();
        e221.setLeftExpression(array[34]);
        e221.setRightExpression(array[35]);
        result[17] = new NotExpression(e221);
        BinaryExpression e223 = new MinorThanEquals();
        e223.setLeftExpression(array[36]);
        e223.setRightExpression(array[37]);
        result[18] = e223;
        BinaryExpression e224 = new EqualsTo();
        e224.setLeftExpression(array[38]);
        e224.setRightExpression(array[39]);
        result[19] = e224;
        BinaryExpression e225 = new LikeExpression();
        e225.setLeftExpression(array[40]);
        e225.setRightExpression(array[41]);
        result[20] = new NotExpression(e225);
        BinaryExpression e227 = new GreaterThan();
        e227.setLeftExpression(array[42]);
        e227.setRightExpression(array[43]);
        result[21] = new NotExpression(e227);
        return result;
    }
    
}
