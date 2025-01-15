package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationTest {
    @Test
    void serializeWithItem() throws JSQLParserException, IOException, ClassNotFoundException {
        String sqlStr =
                "with sample_data(day, value) as (values ((0, 13), (1, 12), (2, 15), (3, 4), (4, 8), (5, 16))), test2 as (values (1,2,3)) \n"
                        + "select day, value from sample_data as a";

        // Parse the SQL string into a PlainSelect object
        PlainSelect originalSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        // Serialize the object to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(originalSelect);
        }

        // Deserialize the object from the byte array
        PlainSelect deserializedSelect;
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            deserializedSelect = (PlainSelect) in.readObject();
        }

        // Verify that the original and deserialized objects are equal
        Assertions.assertEquals(originalSelect.toString(), deserializedSelect.toString(),
                "The deserialized object should be equal to the original");
    }

}
