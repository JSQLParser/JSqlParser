package net.sf.jsqlparser.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParserKeywordsUtilsTest {

    @Test
    void main() {
    }

    @Test
    void getAllKeywords() throws IOException {
        File file = new File("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        Set<String> allKeywords =  ParserKeywordsUtils.getAllKeywords(file);

        assertFalse( allKeywords.isEmpty(), "Keyword List must not be empty!" );
    }
}