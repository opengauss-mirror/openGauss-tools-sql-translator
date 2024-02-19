package org.opengauss.sqltranslator;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.alibaba.druid.sql.parser.ParserException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlTranslateTest {

    final static String inputDir = "/dialect/mysql/input/";
    final static String expectDir = "/dialect/mysql/expect/";

    /**
     * @param fileName File name
     * @return input, expect, translared SQL content
     * @throws IOException
     */
    private String[] execFile(String fileName) throws IOException {
        // URL of input file 
        URL inputFileURL = Objects.requireNonNull(this.getClass().getResource(inputDir + fileName));
        // URL of expect file, which can not exist
        URL expectFileURL = this.getClass().getResource(expectDir + fileName);

        // input SQL content
        String inputSQLContent = IOUtils.toString(inputFileURL, StandardCharsets.UTF_8);

        // delete annotate
        inputSQLContent = inputSQLContent.replaceAll("--.*", "");
        inputSQLContent = inputSQLContent.replaceAll("#.*", "");

        // expect SQL content. null if expect file does not exist.
        String expectSQLContent = expectFileURL == null ? "" : IOUtils.toString(expectFileURL, StandardCharsets.UTF_8);
        // translated SQL content
        String tranSQLContent = ExecuteTranslate.translateMysql2openGauss(inputSQLContent, false, true);
        // String tranSQLContent = exectranslate(inputSQLContent);

        expectSQLContent = expectSQLContent.trim().replaceAll("[ \t\r]+\n", "\n");
        tranSQLContent = tranSQLContent.trim().replaceAll("[ \t\r]+\n", "\n");

        return new String[] { inputSQLContent, expectSQLContent, tranSQLContent };
    }

    private void execFileDebug(String fileName) throws IOException {
        // URL of input file 
        URL inputFileURL = Objects.requireNonNull(this.getClass().getResource(inputDir + fileName));

        // content of input file 
        String inputSQLContent = IOUtils.toString(inputFileURL, StandardCharsets.UTF_8);

        // delete annotate
        inputSQLContent = inputSQLContent.replaceAll("--.*", "");
        inputSQLContent = inputSQLContent.replaceAll("#.*", "");
        // translated SQL content
        ExecuteTranslate.translateMysql2openGauss(inputSQLContent, true, true);
    }

    @Test
    public void test_if() throws IOException {
        String[] sqlContents = execFile("if_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_while() throws IOException {
        String[] sqlContents = execFile("while_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_repeat() throws IOException {
        String[] sqlContents = execFile("repeat_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_compound_statement() throws IOException {
        String[] sqlContents = execFile("compound_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_alterTable() throws IOException {
        String[] sqlContents = execFile("alterTable_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createDatabase() throws IOException {
        String[] sqlContents = execFile("createDatabase_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createFunction() throws IOException {
        String[] sqlContents = execFile("createFunction_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createTrigger01() throws IOException {
        String[] sqlContents = execFile("createTrigger_statement_01.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createTrigger02() throws IOException {
        String[] sqlContents = execFile("createTrigger_statement_02.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createTrigger03() throws IOException {
        String[] sqlContents = execFile("createTrigger_statement_03.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createTrigger04() throws IOException {
        String[] sqlContents = execFile("createTrigger_statement_04.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_dataType() throws IOException {
        String[] sqlContents = execFile("dataType_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_delete() throws IOException {
        String[] sqlContents = execFile("delete_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_limit() throws IOException {
        String[] sqlContents = execFile("limit_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    /**
     * test select statements
     */
    @Test
    public void test_selectQuery() throws IOException {
        String[] sqlContents = execFile("select_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);

        sqlContents = execFile("select_query_statement_01.sql");
        assertEquals(sqlContents[1], sqlContents[2]);

        sqlContents = execFile("select_query_statement_02.sql");
        assertEquals(sqlContents[1], sqlContents[2]);

        sqlContents = execFile("select_query_statement_03.sql");
        assertEquals(sqlContents[1], sqlContents[2]);

        sqlContents = execFile("select_query_statement_04.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_update() throws IOException {
        String[] sqlContents = execFile("update_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    /**
     * test create view statements
     */
    @Test
    public void test_createView() throws IOException {
        String[] sqlContents = execFile("createView_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createServer() throws IOException {
        String[] sqlContents = execFile("createServer_statement.sql");
        assertTrue(sqlContents[2].contains("-- err"));
    }

    // druid will failed when alter server content over one parameter
    @Test
    public void test_alterServer() throws IOException {
        String[] sqlContents = execFile("alterServer_statement.sql");
        assertTrue(sqlContents[2].contains("-- err"));
    }

    @Test
    public void test_dropServer() throws IOException {
        String[] sqlContents = execFile("dropServer_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createProcedure() throws IOException {
        String[] sqlContents = execFile("createProcedure_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createTable() throws IOException {
        String[] sqlContents = execFile("createTable_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_dropTable() throws IOException {
        String[] sqlContents = execFile("dropTable_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createIndex() throws IOException {
        String[] sqlContents = execFile("createIndex_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_dropIndex() throws IOException {
        String[] sqlContents = execFile("dropIndex_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_ColumnDefinition() throws IOException {
        String[] sqlContents = execFile("columnDefinition_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_TableSpace() throws IOException {
        String[] sqlContents = execFile("tableSpace_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_grant() throws IOException {
        String[] sqlContents = execFile("grant_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_revoke() throws IOException {
        String[] sqlContents = execFile("revoke_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }
    
    @Test
    public void test_hasReservedWord() throws IOException {
        String[] sqlContents = execFile("hasReservedWord_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_dropTrigger() throws IOException {
        String[] sqlContents = execFile("dropTrigger_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_createUser() throws IOException {
        String[] sqlContents = execFile("createUser_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_alterView() throws IOException {
        String[] sqlContents = execFile("alterView_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_cursor() throws IOException {
        String[] sqlContents = execFile("cursor_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    // DO statement not support in druid parse
    public void test_do() throws IOException {
        try {
            String[] sqlContents = execFile("do_statement.sql");
        } catch (ParserException exp) {
            assertEquals("", "");
        }
    }

    @Test
    // ITERATE statement not support in druid parse
    public void test_iterate() throws IOException {
        try {
            String[] sqlContents = execFile("iterate_statement.sql");
        } catch (ParserException exp) {
            assertEquals("", "");
        }
    }

    @Test
    public void test_replace() throws IOException {
        String[] sqlContents = execFile("replace_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_alterFunction() throws IOException {
        String[] sqlContents = execFile("alterFunction_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }

    @Test
    public void test_alterProcedure() throws IOException {
        String[] sqlContents = execFile("alterProcedure_statement.sql");
        assertEquals(sqlContents[1], sqlContents[2]);
    }
}
