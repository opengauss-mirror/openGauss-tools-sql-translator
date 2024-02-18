package org.opengauss.sqltranslator.dialect.mysql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.*;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.*;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.visitor.VisitorFeature;
import com.alibaba.druid.util.FnvHash.Constants;
import org.opengauss.sqltranslator.dialect.mysql.util.OpenGaussDataTypeTransformUtil;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MySqlToOpenGaussOutputVisitor extends MySqlOutputVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MySqlToOpenGaussOutputVisitor.class);
    private static final String err = "err";
    private boolean column_case_sensitive;
    private final Map<Integer, String> SQLSetQuantifierMap = new HashMap<Integer, String>() {
        {
            put(0, "");
            put(1, isUppCase() ? "ALL" : "all");
            put(2, isUppCase() ? "DISTINCT" : "distinct");
            put(3, isUppCase() ? "UNIQUE" : "unique");
            put(4, "");
        }
    };

    private static final HashSet<String> incompatiblePrivilegeSet = new HashSet<>();
    private static final HashSet<String> commonSchemaPrivilegeSet = new HashSet<>();
    private static final HashSet<String> tablePrivilegeSet = new HashSet<>();
    private static final HashSet<String> routinePrivilegeSet = new HashSet<>();
    private static final HashSet<String> reservedwordSet = new HashSet<>();
    static {
        incompatiblePrivilegeSet.add("PROXY");
        incompatiblePrivilegeSet.add("TRIGGER");
        incompatiblePrivilegeSet.add("SHOW VIEW");
        incompatiblePrivilegeSet.add("LOCK TABLES");
        incompatiblePrivilegeSet.add("EVENT");
        incompatiblePrivilegeSet.add("CREATE VIEW");
        incompatiblePrivilegeSet.add("CREATE TEMPORARY TABLES");
        incompatiblePrivilegeSet.add("CREATE ROUTINE");
        incompatiblePrivilegeSet.add("ALTER ROUTINE");
        commonSchemaPrivilegeSet.add("CREATE");
        commonSchemaPrivilegeSet.add("USAGE");
        commonSchemaPrivilegeSet.add("ALTER");
        commonSchemaPrivilegeSet.add("DROP");
        commonSchemaPrivilegeSet.add("ALL");
        commonSchemaPrivilegeSet.add("ALL PRIVILEGES");
        tablePrivilegeSet.add("SELECT");
        tablePrivilegeSet.add("INSERT");
        tablePrivilegeSet.add("UPDATE");
        tablePrivilegeSet.add("DELETE");
        tablePrivilegeSet.add("REFERENCES");
        tablePrivilegeSet.add("ALTER");
        tablePrivilegeSet.add("DROP");
        tablePrivilegeSet.add("INDEX");
        tablePrivilegeSet.add("ALL");
        tablePrivilegeSet.add("ALL PRIVILEGES");
        routinePrivilegeSet.add("EXECUTE");
        routinePrivilegeSet.add("DROP");
        routinePrivilegeSet.add("ALTER");
        routinePrivilegeSet.add("ALL");
        routinePrivilegeSet.add("ALL PRIVILEGES");
        reservedwordSet.add("number");
        reservedwordSet.add("user");
        reservedwordSet.add("for");
        reservedwordSet.add("check");
        reservedwordSet.add("all");
    }
    private final StringBuilder sb = (StringBuilder) appender;

    public MySqlToOpenGaussOutputVisitor(Appendable appender) {
        super(appender);
    }

    public MySqlToOpenGaussOutputVisitor(Appendable appender, boolean column_case_sensitive) {
        super(appender);
        this.column_case_sensitive = column_case_sensitive;
    }

    private void printNotSupportWord(String word) {
        if (sb.charAt(sb.length() - 1) != '\n') {
            print('\n');
        }
        print("-- " + word + "\n");
    }

    private void printUcaseNotSupportWord(String word) {
        if (sb.charAt(sb.length() - 1) != '\n') {
            print('\n');
        }
        printUcase("-- " + word + "\n");
    }

    private static void gaussFeatureNotSupportLog(String feat) {
        logger.warn("openGauss does not support " + feat);
    }

    private static void chameFeatureNotSupportLog(String feat) {
        logger.warn("chameleon does not support " + feat);
    }

    private static void errHandle(SQLObject x) {
        SQLObject root = x;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        root.putAttribute(err, "-- " + err);
    }

    private static void errHandle(SQLObject x, String errStr) {
        SQLObject root = x;
        if (x instanceof SQLPrivilegeItem) {
            root = ((SQLPrivilegeItem) x).getAction();
            while (root.getParent() != null) {
                root = root.getParent();
            }
        } else {
            while (root.getParent() != null) {
                root = root.getParent();
            }
        }
        root.putAttribute(err, "-- " + errStr);
    }

    private SQLObject getRoot(SQLObject child) {
        while (child.getParent() != null)
            child = child.getParent();
        return child;
    }

    private String getTypeAttribute(SQLObject x) {
        SQLObject parent = getRoot(x);
        String Attribute = " ";
        if (parent instanceof SQLCreateViewStatement) {
            Attribute = ",view name is " + ((SQLCreateViewStatement) parent).getTableSource().getName();
        } else if (parent instanceof SQLCreateTriggerStatement) {
            Attribute = ",trigger name is " + ((SQLCreateTriggerStatement) parent).getName();
        } else if (parent instanceof SQLCreateFunctionStatement) {
            Attribute = ",function name is " + ((SQLCreateFunctionStatement) parent).getName();
        } else if (parent instanceof SQLCreateProcedureStatement) {
            Attribute = ",procedure name is " + ((SQLCreateProcedureStatement) parent).getName();
        }
        return Attribute;
    }

    @Override
    public boolean visit(SQLCreateDatabaseStatement x) {
        if (x.getHeadHintsDirect() != null) {
            for (SQLCommentHint hint : x.getHeadHintsDirect()) {
                hint.accept(this);
            }
        }
        if (x.hasBeforeComment()) {
            printlnComments(x.getBeforeCommentsDirect());
        }
        printUcase("create schema ");
        x.getName().accept(this);
        if (x.getCharacterSet() != null || x.getCollate() != null) {
            println();
            if (x.getCharacterSet() != null) {
                println("-- " + x.getCharacterSet());
                logger.warn("the CHARACTER SET is incompatible with openGauss" + getTypeAttribute(x));
            }
            final String collate = x.getCollate();
            if (collate != null) {
                println("-- " + x.getCollate());
                logger.warn("the COLLATE is incompatible with openGauss" + getTypeAttribute(x));
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterDatabaseStatement x) {
        logger.error("alter database statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(SQLDropDatabaseStatement x) {
        printUcase("drop schema ");
        if (x.isIfExists())
            printUcase("if exists ");
        x.getDatabase().accept(this);
        return false;
    }

    @Override
    public boolean visit(SQLUseStatement x) {
        printUcase("set current_schema = ");
        x.getDatabase().accept(this);
        return false;
    }

    @Override
    public boolean visit(SQLBlockStatement x) {
        List<SQLStatement> statementList = x.getStatementList();
        int currentStatement = 0;
        int statementSize = statementList.size();
        boolean isBeginPosition = false;
        for (; currentStatement < statementSize; ++currentStatement) {
            if (!(statementList.get(currentStatement) instanceof MySqlDeclareStatement)
                    && !(statementList.get(currentStatement) instanceof MySqlCursorDeclareStatement)) {
                break;
            }
            if (currentStatement != 0) {
                println();
            }

            SQLStatement stmt = statementList.get(currentStatement);
            if (stmt instanceof MySqlCursorDeclareStatement && !isBeginPosition) {
                isBeginPosition = true;
                printUcase("begin");
                println();
            }
            stmt.accept(this);
            if (statementList.get(currentStatement) instanceof MySqlCursorDeclareStatement) {
                print(';');
            }
        }
        if (currentStatement > 0) {
            println();
        }
        if (!isBeginPosition) {
            printUcase("begin");
        }
        if (!x.isEndOfCommit()) {
            this.indentCount++;
        } else {
            print(';');
        }
        println();
        analyseSqlStatement(statementList, currentStatement, statementSize);
        this.indentCount--;
        println();
        printUcase("end");
        return false;
    }

    private void analyseSqlStatement(List<SQLStatement> statementList, int currentStatement, int statementSize) {
        for (int startStatement = currentStatement; currentStatement < statementSize; ++currentStatement) {
            if (currentStatement != startStatement) {
                println();
            }
            SQLStatement stmt = statementList.get(currentStatement);
            stmt.accept(this);
            if (statementList.get(currentStatement) instanceof MySqlDeclareConditionStatement
                    || statementList.get(currentStatement) instanceof MySqlDeclareHandlerStatement
                    || statementList.get(currentStatement) instanceof MySqlCursorDeclareStatement) {
                print(';');
            }
            if (statementList.get(currentStatement) instanceof MySqlDeclareHandlerStatement) {
                if (sb.toString().toLowerCase(Locale.ROOT).endsWith(";;")) {
                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
        }
    }

    @Override
    public boolean visit(MySqlDeclareStatement x) {
        List<SQLDeclareItem> varList = x.getVarList();
        if (varList.size() == 1) {
            printUcase("declare ");
            printAndAccept(x.getVarList(), ", ");
        } else {
            for (int i = 0, size = varList.size(); i < size; ++i) {
                printUcase("declare ");
                SQLDeclareItem var = varList.get(i);
                SQLDataType dataType = var.getDataType();
                SQLExpr value = var.getValue();
                if (var.getDataType() == null) {
                    SQLDeclareItem lastVar = varList.get(varList.size() - 1);
                    var.setDataType(lastVar.getDataType());
                    var.setValue(lastVar.getValue());
                }
                var.accept(this);
                if (i != varList.size() - 1) {
                    println(";");
                }
                var.setDataType(dataType);
                var.setValue(value);
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLReturnStatement x) {
        printUcase("return");
        if (x.getExpr() != null) {
            print(' ');
            if (x.getExpr() instanceof SQLQueryExpr) {
                print('(');
                x.getExpr().accept(this);
                print(')');
            } else {
                x.getExpr().accept(this);
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLIfStatement.ElseIf x) {
        printUcase("elsif ");
        x.getCondition().accept(this);
        printUcase(" then");
        this.indentCount++;
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        this.indentCount--;
        println();
        return false;
    }

    @Override
    public boolean visit(MySqlLeaveStatement x) {
        printUcase("exit ");
        print0(x.getLabelName());
        print(';');
        return false;
    }

    @Override
    public boolean visit(MySqlRepeatStatement x) {
        if (x.getLabelName() != null && !x.getLabelName().equals("")) {
            print0("<<" + x.getLabelName() + ">>");
        }

        printUcase("loop ");
        this.indentCount++;
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        printUcase("if ");
        x.getCondition().accept(this);
        printUcase(" then ");
        this.indentCount++;
        println();
        printUcase("exit; ");
        this.indentCount--;
        println();
        printUcase("end if; ");
        this.indentCount--;
        println();
        printUcase("end loop");
        if (x.getLabelName() != null && !x.getLabelName().equals("")) {
            print(' ');
            print0(x.getLabelName());
            print(';');
        }
        return false;
    }

    @Override
    public boolean visit(SQLWhileStatement x) {
        String label = x.getLabelName();
        if (label != null && label.length() != 0) {
            print0("<<" + x.getLabelName() + ">>");
        }
        printUcase("while ");
        x.getCondition().accept(this);
        printUcase(" loop");
        this.indentCount++;
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        this.indentCount--;
        println();
        printUcase("end loop");
        if (label != null && label.length() != 0) {
            print(' ');
            print0(label);
        }
        return false;
    }

    @Override
    public boolean visit(SQLLoopStatement x) {
        if (x.getLabelName() != null && !x.getLabelName().equals("")) {
            print0("<<" + x.getLabelName() + ">>");
        }
        printUcase("loop ");
        this.indentCount++;
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; i++) {
            SQLStatement item = x.getStatements().get(i);
            item.accept(this);
            if (i != size - 1)
                println();
        }
        this.indentCount--;
        println();
        printUcase("end loop");
        if (x.getLabelName() != null && !x.getLabelName().equals("")) {
            print0(" ");
            print0(x.getLabelName());
        }
        return false;
    }

    @Override
    public boolean visit(MySqlCaseStatement x) {
        printUcase("case");
        SQLExpr valueExpr = x.getCondition();
        if (valueExpr != null) {
            print(' ');
            printExpr(valueExpr, this.parameterized);
        }
        println();
        for (int i = 0; i < x.getWhenList().size(); i++) {
            ((MySqlCaseStatement.MySqlWhenStatement) x.getWhenList().get(i)).accept(this);
        }
        if (x.getElseItem() != null) {
            x.getElseItem().accept(this);
        }
        this.indentCount--;
        printUcase("end case");
        print(';');
        return false;
    }

    @Override
    public boolean visit(MySqlCaseStatement.MySqlWhenStatement x) {
        printUcase("when ");
        x.getCondition().accept(this);
        printUcase(" then");
        this.indentCount++;
        println();
        for (int i = 0; i < x.getStatements().size(); i++) {
            ((SQLStatement) x.getStatements().get(i)).accept(this);
            if (i != x.getStatements().size() - 1) {
                println();
            }
        }
        this.indentCount--;
        println();
        return false;
    }

    // 13.7.6 SET Statements
    @Override
    public boolean visit(SQLAssignItem x) {
        String tagetString = x.getTarget().toString();
        boolean mysqlSpecial = false;
        if (DbType.mysql == this.dbType)
            mysqlSpecial = ("NAMES".equalsIgnoreCase(tagetString) || "CHARACTER SET".equalsIgnoreCase(tagetString)
                    || "CHARSET".equalsIgnoreCase(tagetString) || "AUTOCOMMIT".equalsIgnoreCase(tagetString)
                    || tagetString.startsWith("@"));
        if (!mysqlSpecial) {
            x.getTarget().accept(this);
            print0(" := ");
            x.getValue().accept(this);
        } else {
            if ("AUTOCOMMIT".equalsIgnoreCase(tagetString)) {
                print0("SET ");
                x.getTarget().accept(this);
                print0(" = ");
                x.getValue().accept(this);
            } else {
                print0("SET ");
                x.getTarget().accept(this);
                print0(" = ");
                x.getValue().accept(this);
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLVariantRefExpr x) {
        int index = x.getIndex();
        if (this.inputParameters != null && index < this.inputParameters.size())
            return super.visit(x);
        if (x.isGlobal()) {
            logger.error("openGauss does not support @@global." + getTypeAttribute(x));
            errHandle(x);
        } else if (x.isSession()) {
            logger.error("openGauss does not support @@session." + getTypeAttribute(x));
            errHandle(x);
        }
        String varName = x.getName();
        printName0(varName);
        String collate = (String) x.getAttribute("COLLATE");
        if (collate != null) {
            logger.error("the COLLATE is incompatible with openGauss" + getTypeAttribute(x));
            errHandle(x);
        }
        return false;
    }

    @Override
    public boolean visit(SQLSetStatement x) {
        SQLSetStatement.Option option = x.getOption();
        if (option != null) {
            logger.error("openGauss does not support " + "set " + option.toString() + getTypeAttribute(x));
            errHandle(x);
        }
        printAndAccept(x.getItems(), "; ");
        if (x.getHints() != null && x.getHints().size() > 0) {
            print(' ');
            printAndAccept(x.getHints(), " ");
        }
        return false;
    }

    @Override
    public boolean visit(MySqlUpdateStatement x) {
        printUcase("update ");
        if (x.isLowPriority()) {
            println();
            println("-- " + getText("low_priority "));
            logger.warn("openGauss does not support LOW_PRIORITY when it updates" + getTypeAttribute(x));
        }
        if (x.isIgnore()) {
            println();
            println("-- " + getText("ignore "));
            logger.warn("openGauss does not support IGNORE when it updates" + getTypeAttribute(x));
        }
        if (x.getHints() != null && x.getHints().size() > 0) {
            List<SQLCommentHint> hints = x.getHints();
            println();
            print("-- ");
            printAndAccept(x.getHints(), " ");
            println();
            logger.warn("chameleon does not support optimizer hint conversions" + getTypeAttribute(x));
        }
        if (x.getTableSource() instanceof SQLJoinTableSource) {
            logger.error(
                    "openGauss does not support updating multiple tables at the same time" + getTypeAttribute(x));
            errHandle(x, "update multi-table");
        }
        this.printTableSource(x.getTableSource());
        this.println();
        printUcase("set ");
        int i = 0;
        for (int size = x.getItems().size(); i < size; ++i) {
            if (i != 0) {
                this.print0(", ");
            }
            SQLUpdateSetItem item = (SQLUpdateSetItem) x.getItems().get(i);
            this.visit((SQLUpdateSetItem) item);
        }
        SQLTableSource from = x.getFrom();
        if (from != null) {
            this.println();
            printUcase("from ");
            this.printTableSource(from);
        }
        SQLExpr where = x.getWhere();
        if (where != null) {
            this.println();
            ++this.indentCount;
            printUcase("where ");
            this.printExpr(where);
            --this.indentCount;
        }
        SQLOrderBy orderBy = x.getOrderBy();
        if (Objects.nonNull(orderBy)) {
            println();
            print("-- ");
            visit(orderBy);
            println();
            logger.warn("openGauss doesn't support ORDER BY when it updates" + getTypeAttribute(x));
        }
        SQLLimit limit = x.getLimit();
        if (Objects.nonNull(limit)) {
            println();
            print("-- ");
            visit(limit);
            println();
            logger.warn("openGauss doesn't support LIMIT when it updates" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public void postVisit(SQLObject x) {
        super.postVisit(x);
        if (x instanceof SQLCreateFunctionStatement) {
            println();
            print("$$language plpgsql;");
            println();
        }
        if (x instanceof SQLCreateProcedureStatement) {
            println();
            print("/");
        }
        if (x instanceof SQLStatement && x.containsAttribute(err)) {
            StringBuilder sb = (StringBuilder) appender;
            sb.delete(0, sb.length());
            sb.append((String) x.getAttribute(err));
        }
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        this.printTableSourceExpr(x.getExpr());
        SQLTableSampling sampling = x.getSampling();
        if (sampling != null) {
            this.print(' ');
            sampling.accept(this);
        }
        String alias = x.getAlias();
        List<SQLName> columns = x.getColumnsDirect();
        if (alias != null) {
            this.print(' ');
            if (columns != null && columns.size() > 0) {
                printUcase(" as ");
            }
            printnamewithquote(alias.toString());
        }
        if (columns != null && columns.size() > 0) {
            this.print(" (");
            this.printAndAccept(columns, ", ");
            this.print(')');
        }
        for (int i = 0; i < x.getHintsSize(); ++i) {
            this.print(' ');
            ((SQLHint) x.getHints().get(i)).accept(this);
        }
        if (x.getPartitionSize() > 0) {
            printUcase(" partition (");
            if (x.getPartitionSize() > 1) {
                logger.error("openGauss does not support more than one partition_name" + getTypeAttribute(x));
                errHandle(x);
            } else {
                ((SQLObject) x.getPartitions().get(0)).accept(this);
            }
            this.print(')');
        }
        return false;
    }

    @Override
    public boolean visit(MySqlUseIndexHint x) {
        println();
        print("-- ");
        printUcase("use index ");
        if (x.getOption() != null) {
            printUcase("for ");
            print0(x.getOption().name);
            print(' ');
        }
        print('(');
        printAndAccept(x.getIndexList(), ", ");
        print(')');
        println();
        logger.warn("index hint is incompatible with openGauss" + getTypeAttribute(x));
        return false;
    }

    @Override
    public boolean visit(SQLCreateFunctionStatement x) {
        printUcase("create ");
        if (x.getDefiner() != null) {
            printUcase("definer = ");
            x.getDefiner().accept(this);
            println();
        }
        if (x.isOrReplace()) {
            printUcase("or replace ");
        }
        printUcase("function ");
        x.getName().accept(this);
        int paramSize = x.getParameters().size();
        print('(');
        if (paramSize > 0) {
            this.indentCount++;
            this.indentCount++;
            println();
            for (int i = 0; i < paramSize; ++i) {
                if (i != 0) {
                    print0(", ");
                    println();
                }
                SQLParameter param = x.getParameters().get(i);
                param.accept(this);
            }
            this.indentCount--;
            println();
        }
        print(')');
        println();
        printUcase("returns ");
        x.getReturnDataType().accept(this);
        String comment = x.getComment();
        if (comment != null) {
            println();
            printUcase(" comment ");
            print("'" + comment + "'");
            println();
        }
        if (x.isDeterministic()) {
            printUcase(" deterministic ");
        }
        SQLName authid = x.getAuthid();
        if (authid != null) {
            this.println();
            printUcase("SQL SECURITY ");
            printUcase(authid.toString());
        }
        println();
        print0("AS $$");
        println();
        if ((x.getBlock().getClass()) == SQLBlockStatement.class) {
            x.getBlock().accept(this);
        } else {
            println("BEGIN");
            x.getBlock().accept(this);
            println();
            printUcase("END");
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterFunctionStatement x) {
        printUcase("alter function ");
        print(x.getName().toString());
        if (x.isContainsSql()) {
            printUcase(" contains sql");
        }
        if (x.getComment() != null) {
            print(getText(" comment ") + x.getComment());
        }
        if (x.getSqlSecurity() != null) {
            print(getText(" sql security ") + x.getSqlSecurity());
        }
        if (x.isLanguageSql()) {
            print(" language sql");
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterProcedureStatement x) {
        printUcase("alter procedure ");
        print(x.getName().toString());
        if (x.isContainsSql()) {
            printUcase(" contains sql");
        }
        if (x.getComment() != null) {
            print(getText(" comment ") + x.getComment());
        }
        if (x.getSqlSecurity() != null) {
            print(getText(" sql security ") + x.getSqlSecurity());
        }
        if (x.isLanguageSql()) {
            print(" language sql");
        }
        return false;
    }

    @Override
    public boolean visit(SQLCreateProcedureStatement x) {
        printUcase("create ");
        if (x.getDefiner() != null) {
            printUcase("definer = ");
            x.getDefiner().accept(this);
            println();
        }
        printUcase("procedure ");
        x.getName().accept(this);
        int paramSize = x.getParameters().size();
        this.print0(" (");
        if (paramSize > 0) {
            ++this.indentCount;
            this.println();
            for (int i = 0; i < paramSize; ++i) {
                if (i != 0) {
                    this.print0(", ");
                    this.println();
                }
                SQLParameter param = (SQLParameter) x.getParameters().get(i);
                param.accept(this);
            }
            --this.indentCount;
            this.println();
        }
        this.print(')');

        if (x.getComment() != null) {
            String comment = x.getComment().toString();
            println();
            printUcase(" comment ");
            print(comment);
            println();
        }
        if (x.isLanguageSql()) {
            printUcase(" language sql ");
        }
        if (x.isDeterministic()) {
            boolean isModifyDatabase = false;
            SQLBlockStatement block = (SQLBlockStatement) x.getBlock();
            for (int i = 0; i < block.getStatementList().size(); i++) {
                SQLStatement statement = block.getStatementList().get(i);
                if (statement instanceof SQLDDLStatement || statement instanceof SQLUpdateStatement
                        || statement instanceof SQLPrivilegeStatement
                        || statement instanceof SQLDeleteStatement) {
                    isModifyDatabase = true;
                    break;
                }
            }
            this.println();
            if (isModifyDatabase) {
                logger.warn(
                        "a stable and immutable function cannot modify the database,deterministic will be translated to volatile"
                                + getTypeAttribute(x));
                printUcase("volatile ");
            } else {
                printUcase("deterministic ");
            }
        }
        if (x.isContainsSql()) {
            printUcase("contains sql ");
        }
        if (x.isNoSql()) {
            printUcase("no sql ");
        }
        if (x.isReadSqlData()) {
            printUcase("reads sql data ");
        }
        if (x.isModifiesSqlData()) {
            printUcase("modifies sql data ");
        }
        SQLName authid = x.getAuthid();
        if (authid != null) {
            this.println();
            printUcase("SQL SECURITY ");
            printUcase(authid.toString());
        }
        println();
        print0("AS ");
        println();
        if ((x.getBlock().getClass()) == SQLBlockStatement.class) {
            x.getBlock().accept(this);
        } else {
            println("BEGIN");
            x.getBlock().accept(this);
            if ((x.getBlock().getClass()) == SQLSelectStatement.class) {
                print0(";");
            }
            println();
            printUcase("END");
        }
        return false;
    }

    /**
     * translate mysql SELECT_STATEMENT statement
     *
     * @param x SELECT_STATEMENT statement
     * @return false
     * @see <a href="https://dev.mysql.com/doc/refman/5.6/en/select.html">MySQL
     *      SELECT_STATEMENT syntax</a>
     * @see <a href="https://www.postgresql.org/docs/9.5/sql-select.html">PostgreSQL
     *      SELECT_STATEMENT syntax</a>
     */
    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        // Check if SELECT_STATEMENT is enclosed in parentheses
        final boolean bracket = x.isParenthesized();
        if (bracket) {
            print('(');
        }
        // print comments in the SQL statement
        if ((!isParameterized()) && isPrettyFormat() && x.hasBeforeComment()) {
            printlnComments(x.getBeforeCommentsDirect());
        }
        // translate QUERY_CACHE statement
        String cachedSelectList = x.getCachedSelectList();
        if (cachedSelectList != null) {
            if (!isEnabled(VisitorFeature.OutputSkipSelectListCacheString)) {
                print0(cachedSelectList);
            }
        } else {
            printUcase("SELECT ");
            // translate INDEX_HINT statement
            printAndAccept(x.getHints(), " ");
            // translate ALL、DISTINCT、UNIQUE、DISTINCTROW field, DISTINCTROW set to empty
            if (!SQLSetQuantifierMap.get(x.getDistionOption()).isEmpty()) {
                print0(SQLSetQuantifierMap.get(x.getDistionOption()));
                print0(" ");
            }
            if (x.isHignPriority()
                    || x.isStraightJoin()
                    || x.isSmallResult()
                    || x.isBigResult()
                    || x.isBufferResult()
                    || x.getCache() != null
                    || x.isCalcFoundRows()) {
                println();
                // translate HIGH_PRIORITY field, set to empty
                if (x.isHignPriority()) {
                    print("-- ");
                    printUcase("high_priority ");
                    println();
                    gaussFeatureNotSupportLog("HIGH_PRIORITY when it selects" + getTypeAttribute(x));
                }
                // translate STRAIGHT_JOIN field, set to empty
                if (x.isStraightJoin()) {
                    print("-- ");
                    printUcase("straight_join ");
                    println();
                    gaussFeatureNotSupportLog("STRAIGHT_JOIN when it selects" + getTypeAttribute(x));
                }
                // translate SQL_SMALL_RESULT、SQL_BIG_RESULT、SQL_BUFFER_RESULT field, set to
                // empty
                if (x.isSmallResult()) {
                    println();
                    print("-- ");
                    printUcase("sql_small_result ");
                    println();
                    gaussFeatureNotSupportLog("SQL_SMALL_RESULT when it selects" + getTypeAttribute(x));
                }
                if (x.isBigResult()) {
                    print("-- ");
                    printUcase("sql_big_result ");
                    println();
                    gaussFeatureNotSupportLog("SQL_BIG_RESULT when it selects" + getTypeAttribute(x));
                }
                if (x.isBufferResult()) {
                    print("-- ");
                    printUcase("sql_buffer_result ");
                    println();
                    gaussFeatureNotSupportLog("SQL_BUFFER_RESULT when it selects" + getTypeAttribute(x));
                }
                // translate SQL_CACHE、SQL_NO_CACHE field, set to empty
                if (x.getCache() != null)
                    if (x.getCache().booleanValue()) {
                        print("-- ");
                        printUcase("sql_cache ");
                        println();
                        gaussFeatureNotSupportLog("SQL_CACHE when it selects" + getTypeAttribute(x));
                    } else {
                        print("-- ");
                        printUcase("sql_no_cache ");
                        println();
                        gaussFeatureNotSupportLog("SQL_NO_CACHE when it selects" + getTypeAttribute(x));
                    }
                // translate SQL_CALC_FOUND_ROWS field, set to empty
                if (x.isCalcFoundRows()) {
                    print("-- ");
                    printUcase("sql_calc_found_rows ");
                    println();
                    gaussFeatureNotSupportLog("SQL_CALC_FOUND_ROWS when it selects" + getTypeAttribute(x));
                }
            }
            // translate SELECT_EXPR statement
            printSelectList(x.getSelectList());
            // translate FORCE_PARTITION statement
            SQLName forcePartition = x.getForcePartition();
            if (forcePartition != null) {
                println();
                printUcase("FORCE PARTITION ");
                printExpr(forcePartition, parameterized);
            }
            // translate INTO_OPTION field
            // SELECT_STATEMENT statement on CREATE_VIEW statement could not contain
            // INTO_OPTION field
            SQLExprTableSource into = x.getInto();
            if (x.getParent() instanceof SQLCreateViewStatement) {
                logger.error(
                        "create view statement could not contain select into statement" + getTypeAttribute(x));
            } else if (into != null) {
                println();
                printUcase("INTO ");
                if (into.toString().startsWith("@")) {
                    logger.error("openGauss does not support user-defined variable" + getTypeAttribute(x));
                    errHandle(x);
                }
                printTableSource(into);
            }
        }
        // translate FROM statement
        SQLTableSource from = x.getFrom();
        if (from != null) {
            println();
            printUcase("FROM ");
            printTableSource(from);
        }
        // translate WHERE statement
        SQLExpr where = x.getWhere();
        if (where != null) {
            println();
            printUcase("WHERE ");
            printExpr(where);
        }
        // translate GROUP_BY statement
        // GROUP_BY statement contain HAVING statement
        SQLSelectGroupByClause groupBy = x.getGroupBy();
        if (groupBy != null) {
            println();
            groupBy.accept(this);
        }
        // translate WINDOW statement
        List<SQLWindow> windows = x.getWindows();
        if (windows != null && windows.size() > 0) {
            println();
            printUcase("WINDOW ");
            printAndAccept(windows, ", ");
        }
        // translate ORDER_BY statement
        SQLOrderBy orderBy = x.getOrderBy();
        if (orderBy != null) {
            println();
            orderBy.accept(this);
        }
        // translate LIMIT statement
        SQLLimit limit = x.getLimit();
        if (limit != null) {
            println();
            limit.accept(this);
        }
        // mysql SELECT_STATEMENT could call procedure, however openGauss donot support
        SQLName procedureName = x.getProcedureName();
        if (procedureName != null) {
            println();
            print("-- ");
            printUcase(" procedure ");
            procedureName.accept(this);
            if (!x.getProcedureArgumentList().isEmpty()) {
                print('(');
                printAndAccept(x.getProcedureArgumentList(), ", ");
                print(')');
            }
            println();
            gaussFeatureNotSupportLog("PROCEDURE when it selects" + getTypeAttribute(x));
        }
        // translate FOR_UPDATE、FOE_SHARE field
        if (x.isForUpdate()) {
            println();
            printUcase("FOR UPDATE ");
            // translate NOWAIT field
            if (x.isNoWait()) {
                printUcase("NOWAIT");
            } else if (x.getWaitTime() != null) {
                printUcase(" wait ");
                x.getWaitTime().accept(this);
            }
        } else if (x.isLockInShareMode()) {
            println();
            printUcase("FOR SHARE");
        }
        // translate FOR_UPDATE_OF statement
        List<SQLExpr> forUpdateOf = x.getForUpdateOf();
        if (forUpdateOf != null && forUpdateOf.size() > 0)
            chameFeatureNotSupportLog("for_update_of statement" + getTypeAttribute(x));
        if (bracket) {
            print(')');
        }
        return false;
    }

    @Override
    public boolean visit(SQLSelectItem x) {
        if (x.isConnectByRoot()) {
            gaussFeatureNotSupportLog("CONNECT_BY_ROOT in select item" + getTypeAttribute(x));
        }
        SQLExpr expr = x.getExpr();
        if (expr instanceof SQLIdentifierExpr) {
            printName0(((SQLIdentifierExpr) expr).getName());
        } else if (expr instanceof SQLPropertyExpr) {
            visit((SQLPropertyExpr) expr);
        } else if (expr != null) {
            printExpr(expr, parameterized);
        }
        String alias = x.getAlias();
        if (alias != null && alias.length() > 0) {
            printUcase(" as ");
            char c0 = alias.charAt(0);
            boolean special = false;
            if (c0 != '"' && c0 != '\'' && c0 != '`' && c0 != '[') {
                for (int i = 1; i < alias.length(); ++i) {
                    char ch = alias.charAt(i);
                    if (ch < 256) {
                        if (ch >= '0' && ch <= '9') {
                        } else if (ch >= 'a' && ch <= 'z') {
                        } else if (ch >= 'A' && ch <= 'Z') {
                        } else if (ch == '_' || ch == '$') {

                        } else {
                            special = true;
                        }
                    }
                }
            }
            if ((!printNameQuote) && (!special)) {
                printnamewithquote(alias);
            } else {
                print(quote);
                String unquoteAlias = null;
                if (c0 == '`' && alias.charAt(alias.length() - 1) == '`') {
                    unquoteAlias = alias.substring(1, alias.length() - 1);
                } else if (c0 == '\'' && alias.charAt(alias.length() - 1) == '\'') {
                    unquoteAlias = alias.substring(1, alias.length() - 1);
                } else if (c0 == '"' && alias.charAt(alias.length() - 1) == '"') {
                    unquoteAlias = alias.substring(1, alias.length() - 1);
                } else {
                    printnamewithquote(alias);
                }
                if (unquoteAlias != null) {
                    print0(unquoteAlias);
                }
                print(quote);
            }
            return false;
        }
        List<String> aliasList = x.getAliasList();
        if (aliasList == null) {
            return false;
        }
        println();
        printUcase("as (");
        int aliasSize = aliasList.size();
        if (aliasSize > 5) {
            this.indentCount++;
            println();
        }
        for (int i = 0; i < aliasSize; ++i) {
            if (i != 0) {
                if (aliasSize > 5) {
                    println(",");
                } else {
                    print0(", ");
                }
            }
            printnamewithquote(aliasList.get(i));
        }
        if (aliasSize > 5) {
            this.indentCount--;
            println();
        }
        print(')');
        return false;
    }

    @Override
    public boolean visit(MySqlOutFileExpr x) {
        logger.error("openGauss does not support OUTFILE" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    /**
     * translate join statement
     * mysql support INNER、CROSS、LEFT [OUTER]、RIGHT [OUTER]、NATURAL、STRAIGHT_JOIN
     * field
     * openGauss only support STRAIGHT_JOIN field
     * <p>
     * Since mysql and openGauss only have the difference of STRAIGHT_JOIN in the
     * join clause,
     * the translation function can be realized by rewriting printJoinType
     *
     * @param x JOIN_STATEMENT statement
     * @return false
     * @see MySqlToOpenGaussOutputVisitor#printJoinType(SQLJoinTableSource.JoinType)
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/join.html">MySQL
     *      JOIN_STATEMENT syntax</a>
     * @see <a href="https://www.postgresql.org/docs/9.1/sql-select.html">PostgreSQL
     *      JOIN_STATEMENT syntax</a>
     */
    @Override
    public boolean visit(SQLJoinTableSource x) {
        return super.visit(x);
    }

    /**
     * Special treatment for STRAIGHT_JOIN join type, STRAIGHT_JOIN function is
     * similar to JOIN, use JOIN instead
     *
     * @param joinType table link type
     */
    @Override
    protected void printJoinType(SQLJoinTableSource.JoinType joinType) {
        if (joinType.equals(SQLJoinTableSource.JoinType.STRAIGHT_JOIN)) {
            printUcase(SQLJoinTableSource.JoinType.JOIN.name);
        } else {
            super.printJoinType(joinType);
        }
    }

    /**
     * translate limit statement
     * When druid reads the mysql limit clause, it converts it to limit [
     * offset_count ] row_count format
     * openGauss only supports limit row_count offset offset_count format
     *
     * @param x limit statement
     * @return false
     */
    @Override
    public boolean visit(SQLLimit x) {
        // translate limit row_count
        SQLExpr rowCount = x.getRowCount();
        if (rowCount != null) {
            printUcase("LIMIT");
            print0(" ");
            printExpr(rowCount);
        }
        // translate offset offset_count
        SQLExpr offset = x.getOffset();
        if (offset != null) {
            print0(" ");
            printUcase("OFFSET");
            print0(" ");
            printExpr(offset);
        }
        return false;
    }

    @Override
    public boolean visit(MySqlCreateTableStatement x) {
        SQLPartitionBy partitionBy = x.getPartitioning();
        boolean isPartition = partitionBy != null;
        printUcase("create ");
        if (SQLCreateTableStatement.Type.GLOBAL_TEMPORARY.equals(x.getType())) {
            if (!isPartition) {
                printUcase("TEMPORARY ");
            } else {
                printUcaseNotSupportWord("temporary");
                gaussFeatureNotSupportLog("temporary and partition at the same time" + getTypeAttribute(x));
            }
        }
        printUcase("table ");
        if (x.isIfNotExists()) {
            printUcase("if not exists ");
        }
        this.printTableSourceExpr(x.getName());
        if (x.getLike() != null) {
            this.print0(" (");
            printUcase("like ");
            x.getLike().accept(this);
            /* mysql like 默认保留 默认值表达式，存储引擎属性，check 约束,注释 */
            String lIncluding = " including defaults including constraints including indexes including storage";
            printUcase(lIncluding);
            this.print0(")");
            return false;
        }
        this.printTableElements(x.getTableElementList());
        List<SQLAssignItem> tableOptions = x.getTableOptions();
        Iterator var16 = tableOptions.iterator();
        while (var16.hasNext()) {
            SQLAssignItem option = (SQLAssignItem) var16.next();
            String key = ((SQLIdentifierExpr) option.getTarget()).getName();
            if ("TABLESPACE".equals(key)) {
                this.print(' ');
                printUcase(key.toLowerCase());
                this.print(' ');
                option.getValue().accept(this);
            } else {
                printUcaseNotSupportWord(option.toString());
                gaussFeatureNotSupportLog("tableOption " + key + " when it creates table" + getTypeAttribute(x));
            }
        }
        SQLExpr comment = x.getComment();
        if (comment != null) {
            printUcaseNotSupportWord("comment " + x.getComment());
            gaussFeatureNotSupportLog("COMMENT in column_definition when it creates table" + getTypeAttribute(x));
        }
        if (isPartition) {
            println();
            printUcase("partition by ");
            partitionBy.accept(this);
        }

        if (x.isReplace()) {
            printUcaseNotSupportWord("replace");
            gaussFeatureNotSupportLog("REPLACE when it creates table" + getTypeAttribute(x));
        } else if (x.isIgnore()) {
            printUcaseNotSupportWord("ignore");
            gaussFeatureNotSupportLog("IGNORE when it creates table" + getTypeAttribute(x));
        }
        if (x.getSelect() != null) {
            logger.error(
                    "AS query_expression in create table statement have essentially different between mysql and openGuass"
                            + getTypeAttribute(x));
            errHandle(x);
        }
        Iterator var21 = x.getOptionHints().iterator();
        while (var21.hasNext()) {
            SQLCommentHint hint = (SQLCommentHint) var21.next();
            this.print(' ');
            hint.accept(this);
        }

        List<SQLTableElement> tableElementList = x.getTableElementList();
        for (SQLTableElement element : tableElementList) {
            if (element instanceof MySqlTableIndex
                    && !(element instanceof MySqlUnique || element instanceof MySqlPrimaryKey)) {
                println(";");
                SQLIndexDefinition indexdefinition = ((MySqlTableIndex) element).getIndexDefinition();
                printUcase("CREATE INDEX ");
                if (((MySqlTableIndex) element).getName() != null) {
                    String text = ((MySqlTableIndex) element).getName().toString();
                    printnamewithquote(text);
                    print(" ");
                }
                print("ON ");
                printnamewithquote(x.getName().toString());
                String using = indexdefinition.hasOptions() ? indexdefinition.getOptions().getIndexType()
                        : null;
                if (using != null) {
                    printUcase(" using ");
                    print0(using.toLowerCase());
                }
                List<SQLSelectOrderByItem> indexColumns = ((MySqlTableIndex) element).getColumns();
                print0("(");
                this.printAndAccept(indexColumns, ", ");
                print(')');
            }
            if (element instanceof MySqlKey
                    && !(element instanceof MySqlUnique || element instanceof MySqlPrimaryKey)) {
                println(";");
                SQLIndexDefinition indexdefinition = ((MySqlKey) element).getIndexDefinition();
                printUcase("CREATE INDEX ");
                if (((MySqlKey) element).getName() != null) {
                    String text = ((MySqlKey) element).getName().toString();
                    printnamewithquote(text);
                    print(" ");
                }
                print("ON ");
                printnamewithquote(x.getName().toString());
                String using = indexdefinition.hasOptions() ? indexdefinition.getOptions().getIndexType()
                        : null;
                if (using != null) {
                    printUcase(" using ");
                    print0(using.toLowerCase());
                }
                List<SQLSelectOrderByItem> indexColumns = ((MySqlKey) element).getColumns();
                print0("(");
                this.printAndAccept(indexColumns, ", ");
                print(')');
            }
        }
        return false;
    }

    public void printnamewithquote(String text) {
        char c0 = text.charAt(0);
        if (c0 == '"' && text.charAt(text.length() - 1) == '"') {
            text = text.substring(1, text.length() - 1);
            if (hasReservedword(text) || column_case_sensitive || text.trim().contains(" ")) {
                print("\"" + text + "\"");
            } else {
                print(text);
            }
        } else if (c0 == '`' && text.charAt(text.length() - 1) == '`') {
            text = text.substring(1, text.length() - 1);
            if (hasReservedword(text) || column_case_sensitive) {
                print("\"" + text + "\"");
            } else {
                print(text);
            }
        } else if ((hasUpper(text) && column_case_sensitive) || hasReservedword(text))
            print("\"" + text + "\"");
        else
            print(text);

    }

    @Override
    public boolean visit(SQLPartitionByHash x) {
        if (x.getColumns().size() > 1 || x.getColumns().size() == 0) {
            logger.error("the number of columns of 'partition by' hash cannot exceed 1 or equal 0 at openGauss "
                    + getTypeAttribute(x));
            errHandle(x);
        }
        if (!checkColumnName(x)) {
            logger.error("chameleon does not support partition by hash(methodInvokeExpr)" + getTypeAttribute(x));
            errHandle(x);
            return false;
        }
        if (x.isLinear()) {
            printUcaseNotSupportWord("linear");
            gaussFeatureNotSupportLog("linear hash of partition" + getTypeAttribute(x));
        }
        printUcase("hash (");
        this.printAndAccept(x.getColumns(), ", ");
        this.print(')');
        this.printPartitionsCountAndSubPartitions(x);
        if (x.getPartitions().size() > 0) {
            this.printSQLPartitions(x.getPartitions());
        } else {
            logger.error("partition name must be specified in openGauss" + getTypeAttribute(x));
            errHandle(x);
        }
        return false;
    }

    @Override
    public boolean visit(SQLSubPartitionByHash x) {
        if (x.getExpr() instanceof SQLMethodInvokeExpr) {
            logger.error(
                    "chameleon does not support partition by hash(methodInvokeExpr)" + getTypeAttribute(x));
            errHandle(x, "hash(methodInvoke)");
            return false;
        }
        if (x.isLinear()) {
            printUcaseNotSupportWord("linear");
            gaussFeatureNotSupportLog("linear hash of partition" + getTypeAttribute(x));
        } else {
            printUcase("SUBPARTITION BY HASH ");
        }
        this.print('(');
        x.getExpr().accept(this);
        this.print(')');
        if (x.getSubPartitionsCount() != null) {
            printUcaseNotSupportWord("subpartitions " + x.getSubPartitionsCount());
            gaussFeatureNotSupportLog("specifying subpartition count" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(MySqlPartitionByKey x) {
        gaussFeatureNotSupportLog("key of partition, hash will be used" + getTypeAttribute(x));
        if (x.getColumns().size() > 1 || x.getColumns().size() == 0) {
            logger.error("the number of columns of partition by hash cannot exceed 1 or equal 0 at openGauss ");
            errHandle(x);
        }
        if (x.isLinear()) {
            printUcaseNotSupportWord("linear");
            gaussFeatureNotSupportLog("linear hash of partition" + getTypeAttribute(x));
        }
        printUcase("hash");
        if (!"".equals(x.getAlgorithm() + "")) {
            printUcaseNotSupportWord("algorithm=" + x.getAlgorithm());
            gaussFeatureNotSupportLog("algorithm of partition by key" + getTypeAttribute(x));
        }
        this.print('(');
        this.printAndAccept(x.getColumns(), ", ");
        this.print(')');
        this.printPartitionsCountAndSubPartitions(x);
        if (x.getPartitions().size() > 0) {
            this.printSQLPartitions(x.getPartitions());
        } else {
            logger.error("partition name must be specified in openGauss" + getTypeAttribute(x));
            errHandle(x);
        }
        return false;
    }

    @Override
    public boolean visit(MySqlSubPartitionByKey x) {
        gaussFeatureNotSupportLog("key of subPartition, hash will be used" + getTypeAttribute(x));
        if (x.getColumns().size() > 1 || x.getColumns().size() == 0) {
            logger.error("the number of columns of subPartition by hash cannot exceed 1 or equal 0 at openGauss "
                    + getTypeAttribute(x));
            errHandle(x, "improper number of columns");
            return false;
        }
        if (x.isLinear()) {
            printUcaseNotSupportWord("linear");
            gaussFeatureNotSupportLog("linear key of subPartition" + getTypeAttribute(x));
        }
        printUcase("subPartition by hash (");
        this.printAndAccept(x.getColumns(), ", ");
        this.print(')');
        if (x.getSubPartitionsCount() != null) {
            printUcaseNotSupportWord("subpartitions " + x.getSubPartitionsCount());
            gaussFeatureNotSupportLog("specifying subPartition count" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(SQLPartitionByRange x) {
        if (!checkColumnName(x)) {
            logger.error("chameleon does not support partition by range(methodInvokeExpr)" + getTypeAttribute(x));
            errHandle(x);
        }
        if (x.getColumns().size() > 4 || x.getColumns().size() == 0) {
            logger.error("the number of columns of partition by range cannot exceed 4 or equal 0 at openGauss "
                    + getTypeAttribute(x));
            errHandle(x);
        }
        printUcase("range (");
        this.printAndAccept(x.getColumns(), ", ");
        this.print(')');
        this.printPartitionsCountAndSubPartitions(x);
        if (x.getPartitions().size() > 0) {
            this.print(" (");
            ++this.indentCount;
            int i = 0;
            for (int size = x.getPartitions().size(); i < size; ++i) {
                if (i != 0) {
                    this.print(',');
                }
                this.println();
                ((SQLPartition) x.getPartitions().get(i)).accept(this);
            }
            --this.indentCount;
            this.println();
            this.print(')');
        }
        return false;
    }

    @Override
    public boolean visit(SQLPartitionByList x) {
        if (x.getColumns().size() > 1 || x.getColumns().size() == 0) {
            logger.error("the number of columns of 'partition by' list cannot exceed 1 or equal 0 at openGauss "
                    + getTypeAttribute(x));
            errHandle(x);
        }
        if (!checkColumnName(x)) {
            logger.error("chameleon does not support partition by list(methodInvokeExpr)" + getTypeAttribute(x));
            errHandle(x);
        }
        printUcase("list ");
        if (x.getColumns().size() == 1) {
            this.print('(');
            ((SQLExpr) x.getColumns().get(0)).accept(this);
            this.print0(")");
        } else {
            printUcase("columns (");
            this.printAndAccept(x.getColumns(), ", ");
            this.print0(")");
        }
        this.printPartitionsCountAndSubPartitions(x);
        this.printSQLPartitions(x.getPartitions());
        return false;
    }

    @Override
    public boolean visit(SQLPartitionValue x) {
        if (x.getOperator() == SQLPartitionValue.Operator.LessThan && DbType.oracle != this.getDbType()
                && x.getItems().size() == 1 && x.getItems().get(0) instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr ident = (SQLIdentifierExpr) x.getItems().get(0);
            if ("MAXVALUE".equalsIgnoreCase(ident.getName())) {
                printUcase("values less than maxvalue");
                return false;
            }
        }
        if (x.getOperator() == SQLPartitionValue.Operator.LessThan) {
            printUcase("values less than (");
        } else if (x.getOperator() == SQLPartitionValue.Operator.In) {
            printUcase("values (");
        } else {
            printUcase("values (");
        }
        this.printAndAccept(x.getItems(), ", ", false);
        this.print(')');
        return false;
    }

    private boolean checkColumnName(SQLPartitionBy x) {
        Iterator var4 = x.getColumns().iterator();
        while (var4.hasNext()) {
            SQLExpr column = (SQLExpr) var4.next();
            if (!(column instanceof SQLName)) {
                return false;
            }
        }
        return true;
    }

    public void printSQLPartitions(List<SQLPartition> partitions) {
        int partitionsSize = partitions.size();
        if (partitionsSize > 0) {
            this.print0(" (");
            ++this.indentCount;
            for (int i = 0; i < partitionsSize; ++i) {
                this.println();
                ((SQLPartition) partitions.get(i)).accept(this);
                if (i != partitionsSize - 1) {
                    this.print0(", ");
                }
            }
            --this.indentCount;
            this.println();
            this.print(')');
        }

    }

    public boolean visit(SQLPartition x) {
        printUcase("partition ");
        x.getName().accept(this);
        if (x.getValues() != null) {
            this.print(' ');
            x.getValues().accept(this);
        }
        if (x.getDataDirectory() != null) {
            printNotSupportWord(getText("data directory ") + x.getDataDirectory());
            gaussFeatureNotSupportLog("specifying data directory at partition definition" + getTypeAttribute(x));
        }
        if (x.getIndexDirectory() != null) {
            printNotSupportWord(getText("index directory ") + x.getIndexDirectory());
            gaussFeatureNotSupportLog("specifying index directory at partition definition" + getTypeAttribute(x));
        }
        ++this.indentCount;
        this.printOracleSegmentAttributes(x);
        if (x.getEngine() != null) {
            printNotSupportWord(getText("storage engine ") + x.getEngine());
            gaussFeatureNotSupportLog("specifying engine at partition definition" + getTypeAttribute(x));
        }
        --this.indentCount;
        if (x.getMaxRows() != null) {
            printNotSupportWord(getText(" max_rows ") + x.getMaxRows());
            gaussFeatureNotSupportLog("specifying max_rows at partition definition" + getTypeAttribute(x));
        }
        if (x.getMinRows() != null) {
            printNotSupportWord(getText(" min_rows ") + x.getMinRows());
            gaussFeatureNotSupportLog("specifying min_rows at partition definition" + getTypeAttribute(x));
        }
        if (x.getComment() != null) {
            printNotSupportWord(getText(" comment ") + x.getComment());
            gaussFeatureNotSupportLog("specifying comment at partition definition" + getTypeAttribute(x));
        }
        if (x.getSubPartitionsCount() != null) {
            printUcaseNotSupportWord("subpartitions " + x.getSubPartitionsCount());
            gaussFeatureNotSupportLog("specifying subPartition at partition definition" + getTypeAttribute(x));
        }
        SQLObject parent = x.getParent();
        if (x.getSubPartitions().size() > 0) {
            this.print(" (");
            ++this.indentCount;
            for (int i = 0; i < x.getSubPartitions().size(); ++i) {
                if (i != 0) {
                    this.print(',');
                }
                this.println();
                ((SQLSubPartition) x.getSubPartitions().get(i)).accept(this);
            }
            --this.indentCount;
            this.println();
            this.print(')');
        } else if (parent instanceof SQLPartitionBy && ((SQLPartitionBy) parent).getSubPartitionBy() != null) {
            logger.error("subPartition name must be specified in openGauss" + getTypeAttribute(x));
            errHandle(x, "subPartition name");
        }
        return false;
    }

    @Override
    public boolean visit(SQLSubPartition x) {
        printUcase("subpartition ");
        x.getName().accept(this);
        if (x.getValues() != null) {
            this.print(' ');
            x.getValues().accept(this);
        }
        if (x.getDataDirectory() != null) {
            printNotSupportWord(getText("data directory ") + x.getDataDirectory());
            gaussFeatureNotSupportLog("specifying data directory at partition definition" + getTypeAttribute(x));
        }
        if (x.getIndexDirectory() != null) {
            printNotSupportWord(getText("index directory ") + x.getIndexDirectory());
            gaussFeatureNotSupportLog("specifying index directory at partition definition" + getTypeAttribute(x));
        }
        ++this.indentCount;
        this.printOracleSegmentAttributes(x);
        if (x.getEngine() != null) {
            printNotSupportWord(getText("storage engine ") + x.getEngine());
            gaussFeatureNotSupportLog("specifying engine at partition definition" + getTypeAttribute(x));
        }
        --this.indentCount;
        if (x.getMaxRows() != null) {
            printNotSupportWord(getText(" max_rows ") + x.getMaxRows());
            gaussFeatureNotSupportLog("specifying max_rows at partition definition" + getTypeAttribute(x));
        }
        if (x.getMinRows() != null) {
            printNotSupportWord(getText(" min_rows ") + x.getMinRows());
            gaussFeatureNotSupportLog("specifying min_rows at partition definition" + getTypeAttribute(x));
        }
        if (x.getComment() != null) {
            printNotSupportWord(getText(" comment ") + x.getComment());
            gaussFeatureNotSupportLog("specifying comment at partition definition" + getTypeAttribute(x));
        }
        SQLName tableSpace = x.getTablespace();
        if (tableSpace != null) {
            printUcase(" tablespace ");
            tableSpace.accept(this);
        }
        return false;
    }

    @Override
    protected void printPartitionsCountAndSubPartitions(SQLPartitionBy x) {
        SQLExpr partitionsCount = x.getPartitionsCount();
        if (partitionsCount != null && x.getParent() instanceof MySqlCreateTableStatement) {
            printUcaseNotSupportWord("partitions " + partitionsCount);
            gaussFeatureNotSupportLog("specifying partition count" + getTypeAttribute(x));
        }
        if (x.getSubPartitionBy() != null) {
            this.println();
            x.getSubPartitionBy().accept(this);
        }
    }

    @Override
    public boolean visit(MySqlCreateTableStatement.TableSpaceOption x) {
        x.getName().accept(this);
        if (x.getStorage() != null) {
            printUcaseNotSupportWord("storage " + x.getStorage());
            gaussFeatureNotSupportLog("specifying storage of tablespace" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(SQLColumnDefinition x) {
        boolean parameterized = this.parameterized;
        this.parameterized = false;
        x.getName().setParent(x);
        x.getName().accept(this);
        SQLDataType dataType = x.getDataType();
        if (dataType != null) {
            this.print(' ');
            dataType.accept(this);
        }
        SQLExpr generatedAlawsAs = x.getGeneratedAlawsAs();
        if (generatedAlawsAs != null) {
            printUcaseNotSupportWord("GENERATED ALWAYS AS (" + generatedAlawsAs + ")");
            gaussFeatureNotSupportLog("GENERATED ALWAYS AS in column definition" + getTypeAttribute(x));
        }
        if (x.isVirtual()) {
            printUcaseNotSupportWord("virtual");
            gaussFeatureNotSupportLog("VIRTUAL in column definition" + getTypeAttribute(x));
        }
        if (x.isVisible()) {
            printUcaseNotSupportWord("visible");
            gaussFeatureNotSupportLog("VISIBLE in column definition" + getTypeAttribute(x));
        }
        SQLExpr charsetExpr = x.getCharsetExpr();
        if (charsetExpr != null) {
            printUcaseNotSupportWord("character set " + charsetExpr);
            gaussFeatureNotSupportLog("CHARACTER SET in column definition" + getTypeAttribute(x));
        }
        SQLExpr collateExpr = x.getCollateExpr();
        if (collateExpr != null) {
            printUcaseNotSupportWord("collate " + collateExpr);
            gaussFeatureNotSupportLog("collate in column definition" + getTypeAttribute(x));
        }
        Iterator var7 = x.getConstraints().iterator();
        while (var7.hasNext()) {
            SQLColumnConstraint item = (SQLColumnConstraint) var7.next();
            if (!(item instanceof SQLColumnReference)) {
                this.print(' ');
                item.accept(this);
            }
        }
        SQLExpr defaultExpr = x.getDefaultExpr();
        if (defaultExpr != null) {
            printUcase(" default ");
            defaultExpr.accept(this);
        }
        if (x.getComment() != null) {
            this.print(getText(" comment ") + x.getComment());
        }
        SQLExpr format = x.getFormat();
        if (format != null) {
            printNotSupportWord(getText("column_format ") + format);
            gaussFeatureNotSupportLog("COLUMN FORMAT in column definition" + getTypeAttribute(x));
        }
        SQLExpr storage = x.getStorage();
        if (storage != null) {
            printUcaseNotSupportWord("storage " + storage);
            gaussFeatureNotSupportLog("STORAGE in column definition" + getTypeAttribute(x));
        }
        SQLExpr onUpdate = x.getOnUpdate();
        if (onUpdate != null) {
            printUcase(" on update ");
            onUpdate.accept(this);
        }
        if (x.getAsExpr() != null) {
            printUcaseNotSupportWord("as");
            gaussFeatureNotSupportLog("AS in column definition" + getTypeAttribute(x));
        }
        Iterator var17 = x.getConstraints().iterator();
        while (var17.hasNext()) {
            SQLColumnConstraint item = (SQLColumnConstraint) var17.next();
            if (item instanceof SQLColumnReference) {
                this.print(' ');
                item.accept(this);
            }
        }
        this.parameterized = parameterized;
        return false;
    }

    @Override
    public boolean visit(MysqlForeignKey x) {
        if (x.isHasConstraint()) {
            printUcase("constraint ");
            if (x.getName() != null) {
                x.getName().accept(this);
                this.print(' ');
            }
        }
        printUcase("foreign key");
        if (x.getIndexName() != null) {
            printNotSupportWord(getText("foreign key name ") + x.getIndexName());
            gaussFeatureNotSupportLog("specifying the index name of foreign key" + getTypeAttribute(x));
        }
        this.print0(" (");
        this.printAndAccept(x.getReferencingColumns(), ", ");
        this.print(')');
        printUcase(" references ");
        x.getReferencedTableName().accept(this);
        this.print0(" (");
        this.printAndAccept(x.getReferencedColumns(), ", ");
        this.print(')');
        SQLForeignKeyImpl.Match match = x.getReferenceMatch();
        if (match != null) {
            printUcase(" match ");
            printUcase(match.name_lcase);
        }
        if (x.getOnDelete() != null) {
            printUcase(" on delete ");
            printUcase(x.getOnDelete().name_lcase);
        }
        if (x.getOnUpdate() != null) {
            printUcase(" on update ");
            printUcase(x.getOnUpdate().name_lcase);
        }
        return false;
    }

    @Override
    /* index fulltext spatial */
    public boolean visit(MySqlTableIndex x) {
        gaussFeatureNotSupportLog("specifying index when it creates table" + getTypeAttribute(x));
        return false;
    }

    @Override
    public boolean visit(MySqlKey x) {
        gaussFeatureNotSupportLog("specifying key when it creates table" + getTypeAttribute(x));
        return false;
    }

    @Override
    public boolean visit(SQLCheck x) {
        SQLName name = x.getName();
        if (name != null) {
            printUcase("constraint ");
            name.accept(this);
            this.print(' ');
        }
        printUcase("check (");
        ++this.indentCount;
        x.getExpr().accept(this);
        --this.indentCount;
        this.print(')');
        Boolean enforced = x.getEnforced();
        if (enforced != null) {
            printUcaseNotSupportWord(enforced ? " ENFORCED" : " NOT ENFORCED");
            gaussFeatureNotSupportLog("check enforced in column definition" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(SQLDataType x) {
        this.printDataType(x);
        if (x instanceof SQLDataTypeImpl) {
            SQLDataTypeImpl dataTypeImpl = (SQLDataTypeImpl) x;
            if (dataTypeImpl.isUnsigned()) {
                printUcaseNotSupportWord("unsigned");
                gaussFeatureNotSupportLog("unsigned" + getTypeAttribute(x));
            }
            if (dataTypeImpl.isZerofill()) {
                printUcaseNotSupportWord("zerofill");
                gaussFeatureNotSupportLog("zerofill" + getTypeAttribute(x));
            }
        }
        if (x instanceof SQLCharacterDataType) {
            SQLCharacterDataType charType = (SQLCharacterDataType) x;
            if (charType.getCharSetName() != null) {
                printNotSupportWord(getText(" character set ") + charType.getCharSetName());
                logger.warn("openGauss does not support character set" + getTypeAttribute(x));
                if (charType.getCollate() != null) {
                    printNotSupportWord(getText(" character set ") + charType.getCollate());
                }
            }
            List<SQLCommentHint> hints = ((SQLCharacterDataType) x).hints;
            if (hints != null) {
                this.print(' ');
                Iterator var4 = hints.iterator();
                while (var4.hasNext()) {
                    SQLCommentHint hint = (SQLCommentHint) var4.next();
                    hint.accept(this);
                }
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLCharacterDataType x) {
        this.printDataType(x);
        if (x.getCharSetName() != null) {
            printNotSupportWord(getText(" character set ") + x.getCharSetName());
            logger.warn("openGauss does not support character set" + getTypeAttribute(x));
            if (x.getCollate() != null) {
                printNotSupportWord(getText(" character set ") + x.getCollate());
            }
        }
        List<SQLCommentHint> hints = x.hints;
        if (hints != null) {
            this.print(' ');
            Iterator var3 = hints.iterator();
            while (var3.hasNext()) {
                SQLCommentHint hint = (SQLCommentHint) var3.next();
                hint.accept(this);
            }
        }
        return false;
    }

    @Override
    protected void printTableElements(List<SQLTableElement> tableElementList) {
        int size = tableElementList.size();
        if (size != 0) {
            this.print0(" (");
            ++this.indentCount;
            this.println();
            // Indicates whether this element is printed
            boolean[] noPrints = new boolean[size];
            for (int i = 0; i < size; i++) {
                SQLTableElement element = tableElementList.get(i);
                noPrints[i] = (element instanceof MySqlTableIndex || element instanceof MySqlKey &&
                        !(element instanceof MySqlUnique || element instanceof MySqlPrimaryKey));
            }
            // Indicates whether to print commas
            boolean[] noDots = new boolean[size];
            boolean prev = true;
            for (int i = size - 1; i > 0; i--) {
                noDots[i] = noPrints[i] && prev;
                prev = noDots[i];
            }
            for (int i = 0; i < size; ++i) {
                SQLTableElement element = tableElementList.get(i);
                element.accept(this);
                boolean printDot = (i != size - 1 && !noDots[i + 1] && !noPrints[i]);
                if (printDot) {
                    this.print(',');
                }
                if (this.isPrettyFormat() && element.hasAfterComment()) {
                    this.print(' ');
                    this.printlnComment(element.getAfterCommentsDirect());
                }
                if (printDot) {
                    this.println();
                }
            }
            --this.indentCount;
            this.println();
            this.print(')');
        }
    }

    @Override
    protected void printDataType(SQLDataType x) {
        boolean parameterized = this.parameterized;
        this.parameterized = false;
        SQLDataType dataType = OpenGaussDataTypeTransformUtil.transformOpenGaussToMysql(x);
        this.print0(dataType.getName());
        List<SQLExpr> arguments = dataType.getArguments();
        if (arguments.size() > 0) {
            this.print('(');
            int i = 0;
            for (int size = arguments.size(); i < size; ++i) {
                if (i != 0) {
                    this.print0(", ");
                }
                this.printExpr((SQLExpr) arguments.get(i), false);
            }
            this.print(')');
        }
        long nameHash = dataType.nameHashCode64();
        if (nameHash == Constants.TIME || nameHash == Constants.TIMESTAMP) {
            printUcase(" without time zone");
        }
        this.parameterized = parameterized;
    }

    @Override
    public boolean visit(SQLDropTableStatement x) {
        List<SQLCommentHint> headHints = x.getHeadHintsDirect();
        if (headHints != null) {
            Iterator var3 = headHints.iterator();
            while (var3.hasNext()) {
                SQLCommentHint hint = (SQLCommentHint) var3.next();
                this.visit(hint);
                this.println();
            }
        }
        if (x.hasBeforeComment()) {
            this.printlnComments(x.getBeforeCommentsDirect());
        }
        printUcase("drop ");
        List<SQLCommentHint> hints = x.getHints();
        if (hints != null) {
            this.printAndAccept(hints, " ");
            this.print(' ');
        }
        printUcase("table ");
        if (x.isTemporary())
            printUcaseNotSupportWord("temporary");
        gaussFeatureNotSupportLog("TEMPORARY when it drops table" + getTypeAttribute(x));
        if (x.isIfExists()) {
            printUcase("if exists ");
        }
        this.printAndAccept(x.getTableSources(), ", ");
        if (x.isCascade()) {
            this.printCascade();
        }
        if (x.isRestrict()) {
            printUcase(" restrict");
        }
        return false;
    }

    @Override
    public boolean visit(SQLCreateTriggerStatement x) {
        // openGauss has no DEFINER field
        if (Objects.nonNull(x.getDefiner())) {
            print("-- " + getText("definer "));
            x.getDefiner().accept(this);
            println();
            gaussFeatureNotSupportLog("DEFINER when it creates trigger" + getTypeAttribute(x));
        }
        // Since openGauss can support up to 64 character function names, UUID can be
        // used to generate unique function names
        String id = UUID.randomUUID().toString();
        String function_name = "createFunction_" + id.replace("-", "");
        // first convert the execution body into a custom function
        println("CREATE OR REPLACE FUNCTION " + function_name + "()" + " RETURNS TRIGGER AS");
        if (x.getBody() instanceof SQLBlockStatement) {
            // extract declare statement
            SQLBlockStatement body = (SQLBlockStatement) x.getBody();
            List<SQLStatement> declareStatementList = new ArrayList<>();
            for (SQLStatement sqlStatement : body.getStatementList()) {
                if (sqlStatement instanceof MySqlDeclareStatement)
                    declareStatementList.add(sqlStatement);
            }
            println("$$");
            // translate declare statement
            if (!declareStatementList.isEmpty()) {
                for (SQLStatement sqlStatement : declareStatementList) {
                    sqlStatement.accept(this);
                    println();
                }
            } else {
                println("DECLARE");
            }
            // translate trigger body expect declare statement;
            println("BEGIN");
            for (SQLStatement sqlStatement : body.getStatementList()) {
                if (!(sqlStatement instanceof MySqlDeclareStatement)) {
                    sqlStatement.accept(this);
                    println();
                }

            }
        } else {
            println("$$");
            if (x.getBody() instanceof MySqlDeclareStatement) {
                x.getBody().accept(this);
                println();
                println("BEGIN");
            } else {
                println("DECLARE");
                println("BEGIN");
                x.getBody().accept(this);
                println();
            }
        }
        if (x.isUpdate() || x.isInsert()) {
            println("RETURN NEW;");
        } else {
            println("RETURN OLD;");
        }
        println("END;");
        println("$$ LANGUAGE plpgsql;");
        println();
        // create a trigger through a custom function
        print0("CREATE TRIGGER ");
        x.getName().accept(this);
        println();
        String triggerTypeName = x.getTriggerType().name();
        printUcase(triggerTypeName.toLowerCase());
        if (x.isInsert())
            printUcase(" INSERT ");
        if (x.isDelete()) {
            if (x.isInsert())
                printUcase(" OR");
            printUcase(" DELETE ");
        }
        if (x.isUpdate()) {
            if (x.isInsert() || x.isDelete())
                printUcase(" OR");
            printUcase(" UPDATE ");
        }
        print("ON ");
        printnamewithquote(x.getOn().toString());
        println();
        println("FOR EACH ROW");
        println("EXECUTE PROCEDURE " + function_name + "();");
        return false;
    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {
        SQLTableSource from = x.getFrom();
        if (from == null) {
            printUcase("delete ");
            if (x.isLowPriority() || x.isIgnore() || x.isQuick()) {
                println();
                print("-- ");
                if (x.isLowPriority()) {
                    printUcase("low_priority ");
                    gaussFeatureNotSupportLog("LOW_PRIORITY when it deletes" + getTypeAttribute(x));
                }
                if (x.isIgnore()) {
                    printUcase("ignore ");
                    gaussFeatureNotSupportLog("IGNORE when it deletes" + getTypeAttribute(x));
                }
                if (x.isQuick()) {
                    printUcase("quick ");
                    gaussFeatureNotSupportLog("QUICK when it deletes" + getTypeAttribute(x));
                }
                println();
            }
            if (x.getHints() != null && x.getHints().size() > 0) {
                println();
                print("-- ");
                for (int i = 0, size = x.getHintsSize(); i < size; ++i) {
                    SQLCommentHint hint = x.getHints().get(i);
                    hint.accept(this);
                    print(' ');
                }
                println();
                chameFeatureNotSupportLog("optimizer hint" + getTypeAttribute(x));
            }
            printUcase("from ");
            if (x.getTableSource().toString().split(",").length > 1) {
                logger.error("openGauss does not support multiple-table syntax of delete" + getTypeAttribute(x));
                errHandle(x);
            } else {
                x.getTableSource().accept(this);
            }
            SQLExpr where = x.getWhere();
            if (where != null) {
                println();
                this.indentCount++;
                printUcase("where ");
                printExpr(where, parameterized);
                this.indentCount--;
            }
            SQLOrderBy orderBy = x.getOrderBy();
            if (Objects.nonNull(orderBy)) {
                println();
                print("-- ");
                x.getOrderBy().accept(this);
                println();
                gaussFeatureNotSupportLog("ORDER BY... when it deletes" + getTypeAttribute(x));
            }
            SQLLimit limit = x.getLimit();
            if (Objects.nonNull(limit)) {
                print("-- ");
                x.getLimit().accept(this);
                println();
                gaussFeatureNotSupportLog("LIMIT when it deletes" + getTypeAttribute(x));
            }
        } else {
            logger.error("openGauss does not support multiple-table syntax of delete" + getTypeAttribute(x));
            errHandle(x, " Delete Multiple-Table Syntax");
        }
        return false;
    }

    @Override
    public boolean visit(MySqlInsertStatement x) {
        final List<SQLCommentHint> headHints = x.getHeadHintsDirect();
        if (headHints != null) {
            for (SQLCommentHint hint : headHints) {
                hint.accept(this);
                println();
            }
        }
        if (this.isPrettyFormat() && x.hasBeforeComment()) {
            this.printlnComments(x.getBeforeCommentsDirect());
        }
        printUcase("insert ");
        if (x.getHints() != null && x.getHints().size() > 0) {
            println();
            print("-- ");
            for (int i = 0, size = x.getHintsSize(); i < size; ++i) {
                SQLCommentHint hint = x.getHints().get(i);
                hint.accept(this);
                print(' ');
            }
            println();
            chameFeatureNotSupportLog("optimizer hint" + getTypeAttribute(x));
        }
        if (x.isLowPriority()) {
            println();
            print("-- ");
            printUcase("low_priority ");
            gaussFeatureNotSupportLog("LOW_PRIORITY when it inserts" + getTypeAttribute(x));
        }
        if (x.isDelayed()) {
            println();
            print("-- ");
            printUcase("delayed ");
            gaussFeatureNotSupportLog("DELAYED when it inserts" + getTypeAttribute(x));
        }
        if (x.isHighPriority()) {
            println();
            print("-- ");
            printUcase("high_priority ");
            gaussFeatureNotSupportLog("HIGH_PRIORITY when it inserts" + getTypeAttribute(x));
        }
        if (x.isIgnore()) {
            println();
            print("-- ");
            printUcase("ignore ");
            gaussFeatureNotSupportLog("IGNORE when it inserts" + getTypeAttribute(x));
        }
        println();
        boolean outputIntoKeyword = true;
        if (outputIntoKeyword) {
            printUcase("into ");
        }
        SQLExprTableSource tableSource = x.getTableSource();
        if (tableSource != null) {
            if (tableSource.getClass() == SQLExprTableSource.class) {
                visit(tableSource);
            } else {
                tableSource.accept(this);
            }
        }
        List<SQLAssignItem> partitions = x.getPartitions();
        if (partitions != null) {
            if (partitions.size() > 0) {
                if (partitions.size() > 1) {
                    logger.error("openGauss does not support more than one partition_name" + getTypeAttribute(x));
                    errHandle(x);
                } else {
                    printUcase(" partition (");
                    for (int i = 0; i < partitions.size(); ++i) {
                        if (i != 0) {
                            print0(", ");
                        }
                        SQLAssignItem assign = partitions.get(i);
                        assign.getTarget().accept(this);

                        if (assign.getValue() != null) {
                            print('=');
                            assign.getValue().accept(this);
                        }
                    }
                    print(')');
                }
            }
        }
        List<SQLExpr> columns = x.getColumns();
        if (columns.size() > 0) {
            this.indentCount++;
            print0(" (");
            for (int i = 0, size = columns.size(); i < size; ++i) {
                if (i != 0) {
                    print0(", ");
                }
                SQLExpr column = columns.get(i);
                if (column instanceof SQLIdentifierExpr) {
                    printName0(((SQLIdentifierExpr) column).getName());
                } else {
                    printExpr(column, parameterized);
                }
            }
            print(')');
            this.indentCount--;
        }
        List<SQLInsertStatement.ValuesClause> valuesList = x.getValuesList();
        if (!valuesList.isEmpty()) {
            println();
            printValuesList(valuesList);
        }
        if (x.getQuery() != null) {
            println();
            x.getQuery().accept(this);
        }
        List<SQLExpr> duplicateKeyUpdate = x.getDuplicateKeyUpdate();
        if (duplicateKeyUpdate.size() != 0) {
            println();
            printUcase("on duplicate key update ");
            for (int i = 0, size = duplicateKeyUpdate.size(); i < size; ++i) {
                if (i != 0) {
                    if (i % 5 == 0) {
                        println();
                    }
                    print0(", ");
                }
                duplicateKeyUpdate.get(i).accept(this);
            }
        }
        return false;
    }

    @Override
    protected void printName0(String text) {
        if (this.appender == null || text.length() == 0)
            return;
        this.quote = '"';
        try {
            if ((text.charAt(0) != '"' || text.charAt(0) != '`')
                    && (text.toUpperCase().equals("NEW") || text.toUpperCase().equals("OLD")
                            || text.toUpperCase().equals("MAXVALUE"))) {
                this.appender.append(text);
                return;
            }
            char c0 = text.charAt(0);
            if (c0 == '"' && text.charAt(text.length() - 1) == '"') {
                text = text.substring(1, text.length() - 1);
                if (hasReservedword(text) || column_case_sensitive || text.trim().contains(" ")) {
                    this.appender.append(this.quote);
                    this.appender.append(text);
                    this.appender.append(this.quote);
                } else {
                    this.appender.append(text);
                }
            } else if (c0 == '`' && text.charAt(text.length() - 1) == '`') {
                text = text.substring(1, text.length() - 1);
                if (hasReservedword(text) || column_case_sensitive) {
                    this.appender.append(this.quote);
                    this.appender.append(text);
                    this.appender.append(this.quote);
                } else {
                    this.appender.append(text);
                }
            } else if ((hasUpper(text) && column_case_sensitive) || hasReservedword(text)) {
                this.appender.append(this.quote);
                this.appender.append(text);
                this.appender.append(this.quote);
            } else {
                this.appender.append(text);
            }
        } catch (IOException e) {
            throw new RuntimeException("println error", e);
        }
    }

    public static boolean hasReservedword(String str) {
        if (reservedwordSet.contains(str.toLowerCase()))
            return true;
        else
            return false;
    }

    public static boolean hasUpper(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c0 = str.charAt(i);
            if (Character.isUpperCase(c0))
                return true;
        }
        return false;
    }

    @Override
    public boolean visit(SQLCreateViewStatement x) {
        printUcase("create ");
        if (x.isOrReplace()) {
            printUcase("or replace ");
        }
        this.indentCount++;
        if (x.getAlgorithm() != null && x.getAlgorithm().length() > 0
                || x.getDefiner() != null
                || x.getSqlSecurity() != null && x.getSqlSecurity().length() > 0) {
            println();
            String algorithm = x.getAlgorithm();
            if (algorithm != null && algorithm.length() > 0) {
                printUcase("algorithm = ");
                print0(algorithm);
                println();
            }
            SQLName definer = x.getDefiner();
            if (definer != null) {
                printUcase("definer = ");
                definer.accept(this);
                println();
            }
            String sqlSecurity = x.getSqlSecurity();
            if (sqlSecurity != null && sqlSecurity.length() > 0) {
                printUcase("sql security ");
                print0(sqlSecurity);
                println();
            }
        }
        this.indentCount--;
        printUcase("view ");
        x.getTableSource().accept(this);
        List<SQLTableElement> columns = x.getColumns();
        if (columns.size() > 0) {
            print0(" (");
            this.indentCount++;
            println();
            for (int i = 0; i < columns.size(); ++i) {
                if (i != 0) {
                    print0(", ");
                    println();
                }
                columns.get(i).accept(this);
            }
            this.indentCount--;
            println();
            print(')');
        }
        println();
        printUcase("as");
        println();
        SQLSelect subQuery = x.getSubQuery();
        if (subQuery != null) {
            subQuery.accept(this);
        }
        SQLBlockStatement script = x.getScript();
        if (script != null) {
            script.accept(this);
        }
        if (x.isWithCheckOption()
                || x.isWithCascaded()
                || x.isWithLocal()) {
            println();
            printUcase("with ");
            if (x.isWithCascaded()) {
                printUcase("cascaded ");
                println();
            }
            if (x.isWithLocal()) {
                printUcase("local ");
                println();
            }
            if (x.isWithCheckOption()) {
                printUcase("check option");
                println();
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterViewStatement x) {
        printUcase("alter ");
        this.indentCount++;
        String algorithm = x.getAlgorithm();
        if (algorithm != null && algorithm.length() > 0) {
            printUcase("algorithm = ");
            print0(algorithm);
            println();
        }
        SQLName definer = x.getDefiner();
        if (definer != null) {
            printUcase("definer = ");
            definer.accept(this);
            println();
        }
        String sqlSecurity = x.getSqlSecurity();
        if (sqlSecurity != null && sqlSecurity.length() > 0) {
            printUcase("sql security ");
            print0(sqlSecurity);
            println();
        }
        this.indentCount--;
        printUcase("view ");
        x.getTableSource().accept(this);
        List<SQLTableElement> columns = x.getColumns();
        if (columns.size() > 0) {
            print0(" (");
            this.indentCount++;
            println();
            for (int i = 0; i < columns.size(); ++i) {
                if (i != 0) {
                    print0(", ");
                    println();
                }
                columns.get(i).accept(this);
            }
            this.indentCount--;
            println();
            print(')');
        }
        println();
        printUcase("as");
        println();
        SQLSelect subQuery = x.getSubQuery();
        if (subQuery != null) {
            subQuery.accept(this);
        }
        if (x.isWithCheckOption()) {
            println();
            printUcase("with ");
            println();
            if (x.isWithCascaded()) {
                printUcase("cascaded ");
                println();
            }
            if (x.isWithLocal()) {
                printUcase("local ");
                println();
            }
            printUcase("check option");
        }
        return false;
    }

    @Override
    public boolean visit(SQLDropViewStatement x) {
        printUcase("drop view ");
        if (x.isIfExists())
            printUcase("if exists ");
        printAndAccept(x.getTableSources(), ", ");
        if (x.isCascade())
            printCascade();
        if (x.isRestrict())
            printUcase(" restrict");
        return false;
    }

    // 13.1.33 RENAME TABLE Statement
    @Override
    public boolean visit(MySqlRenameTableStatement x) {
        printUcase("alter table ");
        if (x.getItems().size() > 1)
            printAndAccept(x.getItems(), ";ALTER TABLE ");
        else
            printAndAccept(x.getItems(), " ");
        return false;
    }

    @Override
    public boolean visit(MySqlRenameTableStatement.Item x) {
        x.getName().accept(this);
        printUcase(" rename to ");
        x.getTo().accept(this);
        return false;
    }

    @Override
    public boolean visit(SQLTruncateStatement x) {
        List<SQLCommentHint> headHints = x.getHeadHintsDirect();
        if (headHints != null)
            for (SQLCommentHint hint : headHints) {
                visit(hint);
                println();
            }
        printUcase("truncate table ");
        printAndAccept(x.getTableSources(), ", ");
        return false;
    }

    @Override
    public boolean visit(SQLCreateIndexStatement x) {
        printUcase("create ");
        String type = x.getType();
        if (type != null) {
            if (type.toLowerCase().equals("unique")) {
                printUcase("unique");
            } else if (type.toLowerCase().equals("spatial")) {
                printUcaseNotSupportWord("spatial");
                gaussFeatureNotSupportLog("spatial index" + getTypeAttribute(x));
            } else if (type.toLowerCase().equals("fulltext")) {
                printUcaseNotSupportWord("fulltext");
                gaussFeatureNotSupportLog("fulltext index" + getTypeAttribute(x));
            } else {
                printUcaseNotSupportWord(type);
                logger.warn("unrecognized keyword " + type + getTypeAttribute(x));
            }
            this.print(' ');
        }
        printUcase("index ");
        if (x.getIndexDefinition().hasOptions()) {
            String algorithm = x.getIndexDefinition().getOptions().getAlgorithm();
            String lock = x.getIndexDefinition().getOptions().getLock();
            if (lock != null) {
                lock = lock.toLowerCase();
                if (lock.equals("none")) {
                    printUcase("concurrently ");
                } else if (!(algorithm != null && algorithm.equals("inplace") && lock.equals("default")))
                    printUcaseNotSupportWord("lock " + lock);
                gaussFeatureNotSupportLog("specifying lock " + lock + " on index" + getTypeAttribute(x));
            } else
                lock = "default";
            if (algorithm != null) {
                algorithm = algorithm.toLowerCase();
                if (algorithm.equals("inplace") && lock.equals("default")) {
                    printUcase("concurrently ");
                } else
                    printUcaseNotSupportWord("algorithm " + algorithm);
                gaussFeatureNotSupportLog(
                        "specifying algorithm " + algorithm + " on index" + getTypeAttribute(x));
            }
        }
        x.getName().accept(this);
        printUcase(" on ");
        x.getTable().accept(this);
        if (x.getIndexDefinition().hasOptions()) {
            String using = x.getIndexDefinition().getOptions().getIndexType();
            if (using != null) {
                printUcase(" using ");
                printUcase(using.toLowerCase());
            }
            x.getIndexDefinition().getOptions().accept(this);
        }
        this.print0(" (");
        this.printAndAccept(x.getItems(), ", ");
        this.print(')');
        return false;
    }

    @Override
    public boolean visit(SQLIndexOptions x) {
        SQLExpr keyBlockSize = x.getKeyBlockSize();
        if (keyBlockSize != null) {
            printUcaseNotSupportWord(" KEY_BLOCK_SIZE = " + keyBlockSize);
            gaussFeatureNotSupportLog("specifying keyBlockSize on index" + getTypeAttribute(x));
        }
        String parserName = x.getParserName();
        if (parserName != null) {
            printUcaseNotSupportWord(" WITH PARSER " + parserName);
            gaussFeatureNotSupportLog("specifying parserName on index" + getTypeAttribute(x));
        }
        SQLExpr comment = x.getComment();
        if (comment != null) {
            printUcaseNotSupportWord(" COMMENT " + comment);
            gaussFeatureNotSupportLog("specifying comment on index" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(SQLSelectOrderByItem x) {
        SQLExpr expr = x.getExpr();
        SQLObject parent = x.getParent();
        boolean isParentCreateIndex = (parent != null && parent instanceof SQLCreateIndexStatement);
        boolean isParentTableIndex = (parent != null && parent instanceof MySqlTableIndex);
        boolean isParentTableKey = (parent != null && parent instanceof MySqlKey);
        if ((isParentCreateIndex || isParentTableIndex || isParentTableKey) && expr instanceof SQLMethodInvokeExpr) {
            gaussFeatureNotSupportLog("prefix length of columnName on index" + getTypeAttribute(x));
            printnamewithquote(((SQLMethodInvokeExpr) expr).getMethodName());
        } else if (expr instanceof SQLIntegerExpr) {
            this.print(((SQLIntegerExpr) expr).getNumber().longValue());
        } else {
            this.printExpr(expr, this.parameterized);
        }
        SQLOrderingSpecification type = x.getType();
        if (type != null) {
            if (isParentCreateIndex || isParentTableIndex || isParentTableKey) {
                String using = "";
                if (isParentCreateIndex) {
                    SQLIndexOptions options = ((SQLCreateIndexStatement) parent).getIndexDefinition().getOptions();
                    using = options.getIndexType();
                } else if (isParentTableIndex) {
                    SQLIndexOptions options = ((MySqlTableIndex) parent).getIndexDefinition().getOptions();
                    using = options.getIndexType();
                } else if (isParentTableKey) {
                    using = ((MySqlKey) parent).getIndexType();
                }
                if (using != "" && using.toLowerCase().equals("hash")) {
                    logger.error(
                            "method hash does not support ASC/DESC options in openGauss" + getTypeAttribute(x));
                    errHandle(x);
                }
            }
            this.print(' ');
            printUcase(type.name_lcase);
        }
        SQLCommentHint hint = x.getHint();
        if (hint != null) {
            this.visit(hint);
        }
        return false;
    }

    @Override
    public boolean visit(SQLDropIndexStatement x) {
        printUcase("drop index ");
        gaussFeatureNotSupportLog("specifying table name" + " ," + getTypeAttribute(x));
        SQLExpr algorithm = x.getAlgorithm();
        String algorithmName = null;
        if (algorithm != null) {
            if (algorithm instanceof SQLIdentifierExpr) {
                algorithmName = ((SQLIdentifierExpr) algorithm).getName().toLowerCase();
            }
        }
        SQLExpr lockOption = x.getLockOption();
        String lockName;
        if (lockOption != null) {
            if (lockOption instanceof SQLDefaultExpr) {
                lockName = "default";
            } else {
                lockName = ((SQLIdentifierExpr) lockOption).getName().toLowerCase();
            }
        } else {
            lockName = "default";
        }
        if (lockName.equals("none")
                || (algorithmName != null && algorithmName.equals("inplace") && lockName.equals("default"))) {
            printUcase(" concurrently ");
        } else if (algorithm != null) {
            printUcaseNotSupportWord("algorithm " + algorithmName);
            gaussFeatureNotSupportLog("specifying algorithm keyword" + ", " + getTypeAttribute(x));
        } else if (x.getLockOption() != null) {
            printUcaseNotSupportWord("lock = " + lockName);
            gaussFeatureNotSupportLog("specifying lock keyword" + ", " + getTypeAttribute(x));
        }
        if (x.getIndexName().getSimpleName().equals("`PRIMARY`")) {
            print0(x.getTableName().getTableName() + "_pkey");
        } else {
            x.getIndexName().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableStatement x) {
        List<SQLCommentHint> headHints = x.getHeadHintsDirect();
        if (headHints != null) {
            Iterator var3 = headHints.iterator();
            while (var3.hasNext()) {
                SQLCommentHint hint = (SQLCommentHint) var3.next();
                hint.accept(this);
                this.println();
            }
        }
        printUcase("alter ");
        printUcase("table ");
        this.printTableSourceExpr(x.getName());
        ++this.indentCount;
        int i;
        for (i = 0; i < x.getItems().size(); ++i) {
            SQLAlterTableItem item = (SQLAlterTableItem) x.getItems().get(i);
            if (item instanceof SQLAlterTableRename) {
                if (i != 0) {
                    printUcase(";\nalter table ");
                    this.printTableSourceExpr(x.getName());
                    print(' ');
                }
                item.accept(this);
                if (i != x.getItems().size() - 1) {
                    printUcase(";\nalter table ");
                    this.printTableSourceExpr(((SQLAlterTableRename) item).getToName());
                }
                continue;
            }
            if (i != 0 && !(x.getItems().get(i - 1) instanceof SQLAlterTableRename)) {
                this.print(',');
            }
            this.println();
            if (item instanceof SQLAlterTableAnalyzePartition || item instanceof SQLAlterTableCheckPartition
                    || item instanceof SQLAlterTableReOrganizePartition || item instanceof SQLAlterTableRebuildPartition
                    || item instanceof SQLAlterTableRepairPartition || item instanceof SQLAlterTableOptimizePartition
                    || item instanceof SQLAlterTableCoalescePartition || item instanceof SQLAlterTableDiscardPartition
                    || item instanceof SQLAlterTableImportPartition) {
                logger.error("unknown keyword about alter table partition" + getTypeAttribute(x));
                errHandle(x, "unknown partition keyword");
            }
            item.accept(this);
        }
        if (x.isRemovePatiting()) {
            logger.error("openGauss does not support removing partition" + getTypeAttribute(x));
            errHandle(x, "unsupported removing partition");
        }
        if (x.isUpgradePatiting()) {
            logger.error("openGauss does not support upgrading partition" + getTypeAttribute(x));
            errHandle(x, "unsupported upgrading partition");
        }
        if (x.getTableOptions().size() > 0) {
            if (x.getItems().size() > 0) {
                this.print(',');
            }
            this.println();
        }
        --this.indentCount;
        i = 0;
        Iterator var9 = x.getTableOptions().iterator();
        while (var9.hasNext()) {
            SQLAssignItem item = (SQLAssignItem) var9.next();
            SQLExpr key = item.getTarget();
            if (i != 0) {
                this.print(' ');
            }
            if ("TABLESPACE".equals(key.toString().toUpperCase())) {
                printUcase("set tablespace ");
                item.getValue().accept(this);
            } else {
                logger.error("openGauss does not support tableOption " + key.toString() + " when it alters table"
                        + getTypeAttribute(x));
                errHandle(x, key.toString());
            }
        }
        SQLPartitionBy partitionBy = x.getPartition();
        if (partitionBy != null) {
            logger.error("openGauss does not support partition by when it alters table" + getTypeAttribute(x));
            errHandle(x, "partition by");
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableRename x) {
        printUcase(" rename to ");
        x.getTo().accept(this);
        return false;
    }

    /* primary key and unique */
    @Override
    public boolean visit(SQLIndexDefinition x) {
        if (x.getParent() instanceof SQLAlterTableAddIndex) {
            printUcase("index ");
            if (x.getType() != null) {
                printUcaseNotSupportWord(x.getType());
                gaussFeatureNotSupportLog(x.getType() + " index" + getTypeAttribute(x));
            }
            SQLName name = x.getName();
            if (name != null) {
                name.accept(this);
            } else {
                logger.error("openGauss needs index name to add index" + getTypeAttribute(x));
                errHandle(x, "lack of index name");
            }
            if (x.getColumns().size() > 0) {
                if (x.getColumns().size() > 1) {
                    logger.error("openGauss alters table add index on only one column" + getTypeAttribute(x));
                    errHandle(x, "too much Column");
                }
                this.print('(');
                this.printAndAccept(x.getColumns(), ", ");
                this.print(')');
            }
            if (x.getOptions().getIndexType() != null) {
                printUcaseNotSupportWord("using " + x.getOptions().getIndexType());
                gaussFeatureNotSupportLog("specifying index type when it adds index" + getTypeAttribute(x));
            }
            if (x.getOptions().getComment() != null) {
                printUcaseNotSupportWord(" COMMENT " + x.getOptions().getComment());
                gaussFeatureNotSupportLog("specifying comment when it adds index" + getTypeAttribute(x));
            }
            if (x.getOptions().getKeyBlockSize() != null) {
                printUcaseNotSupportWord(" KEY_BLOCK_SIZE = " + x.getOptions().getKeyBlockSize());
                gaussFeatureNotSupportLog("specifying keyBlockSize when it adds index" + getTypeAttribute(x));
            }
            if (x.getOptions().getParserName() != null) {
                printUcaseNotSupportWord(" WITH PARSER " + x.getOptions().getParserName());
                gaussFeatureNotSupportLog("specifying parser name when it adds index" + getTypeAttribute(x));
            }
        } else {
            String type = x.getType();
            boolean typeNotNull = type != null;
            boolean isPrimary = typeNotNull && type.toLowerCase().equals("primary");
            if (typeNotNull) {
                if ((isPrimary) && x.getName() != null && x.hasConstraint()) {
                    printUcase("constraint ");
                    x.getName().accept(this);
                    this.print(' ');
                }
                printUcase(type.toLowerCase());
                this.print(' ');
            }
            if (x.isKey() && isPrimary) {
                printUcase("key ");
            }
            if (x.getName() != null && (type == null || !type.equalsIgnoreCase("primary"))) {
                printUcaseNotSupportWord(x.getName().toString());
                gaussFeatureNotSupportLog("specifying index name" + getTypeAttribute(x));
            }
            if (x.getColumns().size() > 0) {
                this.print('(');
                this.printAndAccept(x.getColumns(), ", ");
                this.print(')');
            }
            if (x.getOptions().getIndexType() != null)
                printUcaseNotSupportWord("using " + x.getOptions().getIndexType());
            gaussFeatureNotSupportLog("specifying index type" + getTypeAttribute(x));
            if (x.getOptions() != null && !x.getOptions().toString().equals(""))
                gaussFeatureNotSupportLog("specifying index option" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTableOption x) {
        if (x.getName().toLowerCase().equals("algorithm")) {
            logger.error("openGauss does not support ALGORITHM when it alters table" + getTypeAttribute(x));
            errHandle(x);
            return false;
        }
        this.print0(x.getName());
        this.print0(" = ");
        this.print0(x.getValue().toString());
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableAddColumn x) {
        printUcase("add ");
        if (x.getColumns().size() > 1) {
            this.print('(');
        }
        this.printAndAccept(x.getColumns(), ", ");
        if (x.getFirstColumn() != null) {
            gaussFeatureNotSupportLog("first " + getTypeAttribute(x));
            printUcaseNotSupportWord("first " + x.getFirstColumn());
        } else if (x.getAfterColumn() != null) {
            gaussFeatureNotSupportLog("alter" + getTypeAttribute(x));
            printUcaseNotSupportWord("alter " + x.getAfterColumn());
        } else if (x.isFirst()) {
            printUcaseNotSupportWord("first");
            gaussFeatureNotSupportLog("first" + getTypeAttribute(x));
        }
        if (x.getColumns().size() > 1) {
            this.print(')');
        }
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTableChangeColumn x) {
        printUcase("drop ");
        x.getColumnName().accept(this);
        printUcase(" , add ");
        x.getNewColumnDefinition().accept(this);
        if (x.getFirstColumn() != null) {
            gaussFeatureNotSupportLog("first " + getTypeAttribute(x));
            printUcaseNotSupportWord("first " + x.getFirstColumn());
        } else if (x.getAfterColumn() != null) {
            gaussFeatureNotSupportLog("alter" + getTypeAttribute(x));
            printUcaseNotSupportWord("alter " + x.getAfterColumn());
        } else if (x.isFirst()) {
            printUcaseNotSupportWord("first");
            gaussFeatureNotSupportLog("first" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTableModifyColumn x) {
        printUcase("drop ");
        x.getNewColumnDefinition().getName().accept(this);
        printUcase(" ,add ");
        x.getNewColumnDefinition().accept(this);
        if (x.getFirstColumn() != null) {
            gaussFeatureNotSupportLog("first " + getTypeAttribute(x));
            printUcaseNotSupportWord("first " + x.getFirstColumn());
        } else if (x.getAfterColumn() != null) {
            gaussFeatureNotSupportLog("alter" + getTypeAttribute(x));
            printUcaseNotSupportWord("alter " + x.getAfterColumn());
        } else if (x.isFirst()) {
            printUcaseNotSupportWord("first");
            gaussFeatureNotSupportLog("first" + getTypeAttribute(x));
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableConvertCharSet x) {
        logger.error("openGauss does not support converting to character set when it alters table"
                + getTypeAttribute(x));
        errHandle(x, "convert to character set");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableEnableKeys x) {
        logger.error("openGauss does not support enabling keys when it alters table" + getTypeAttribute(x));
        errHandle(x, "enable keys");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableDisableKeys x) {
        logger.error("openGauss does not support disabling keys when it alters table" + getTypeAttribute(x));
        errHandle(x, "disable keys");
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTableDiscardTablespace x) {
        logger.error("openGauss does not support discard tablespace when it alters table" + getTypeAttribute(x));
        errHandle(x, "discard tablespace");
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTableImportTablespace x) {
        logger.error(
                "openGauss does not support importing tablespace when it alters table" + getTypeAttribute(x));
        errHandle(x, "import tablespace");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableDropIndex x) {
        logger.error("openGauss does not support dropping index when it alters table" + getTypeAttribute(x));
        errHandle(x, "drop index");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableDropPrimaryKey x) {
        logger.error(
                "openGauss does not support dropping primary key when it alters table" + getTypeAttribute(x));
        errHandle(x, "drop primary key");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableDropForeignKey x) {
        logger.error(
                "openGauss does not support dropping foreign key when it alters table" + getTypeAttribute(x));
        errHandle(x, "drop foreign key");
        return false;
    }

    public boolean visit(MySqlAlterTableForce x) {
        logger.error("openGauss does not support force when it alters table" + getTypeAttribute(x));
        errHandle(x, "force");
        return false;
    }

    public boolean visit(MySqlAlterTableLock x) {
        logger.error("openGauss does not support lock when it alters table" + getTypeAttribute(x));
        errHandle(x, "lock");
        return false;
    }

    public boolean visit(MySqlAlterTableOrderBy x) {
        logger.error("openGauss does not support order by when it alters table" + getTypeAttribute(x));
        errHandle(x, "order by");
        return false;
    }

    public boolean visit(MySqlAlterTableValidation x) {
        logger.error("openGauss does not support validation when it alters table" + getTypeAttribute(x));
        errHandle(x, "validation");
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableAddPartition x) {
        printUcase("add ");
        if (x.getPartitions().size() > 0) {
            this.printAndAccept(x.getPartitions(), ", ");
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableDropPartition x) {
        for (int i = 0; i < x.getPartitions().size(); i++) {
            printUcase("drop ");
            printUcase("partition ");
            x.getPartitions().get(i).accept(this);
            if (i != x.getPartitions().size() - 1) {
                print(',');
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableExchangePartition x) {
        printUcase("exchange partition ");
        this.print('(');
        this.printAndAccept(x.getPartitions(), ", ");
        this.print(')');
        printUcase(" with table ");
        x.getTable().accept(this);
        Boolean validation = x.getValidation();
        if (validation != null) {
            if (validation) {
                printUcase(" with validation");
            } else {
                printUcase(" without validation");
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableTruncatePartition x) {
        if (x.getPartitions().size() == 1
                && "ALL".equalsIgnoreCase(((SQLName) x.getPartitions().get(0)).getSimpleName())) {
            logger.error("openGauss does not support truncate all partition when it alters table"
                    + getTypeAttribute(x));
            errHandle(x, "unsupported keyword all");
            return false;
        }
        for (int i = 0; i < x.getPartitions().size(); i++) {
            printUcase("truncate partition ");
            x.getPartitions().get(i).accept(this);
            if (i != x.getPartitions().size() - 1) {
                print(',');
            }
        }
        return false;
    }

    @Override
    public boolean visit(MySqlAlterLogFileGroupStatement x) {
        logger.error("openGauss does not support alter logfile group statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlCreateAddLogFileGroupStatement x) {
        logger.error("openGauss does not support create logfile group statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(SQLDropLogFileGroupStatement x) {
        logger.error("openGauss does not support drop logfile group statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlAlterEventStatement x) {
        logger.error("openGauss does not support alter event statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlCreateEventStatement x) {
        logger.error("openGauss does not support alter event statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(SQLDropEventStatement x) {
        logger.error("openGauss does not support drop event statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlCreateUserStatement x) {
        printUcase("create user ");
        if (x.isIfNotExists()) {
            printUcase("if not exists ");
        }
        printAndAccept(x.getUsers(), " ");
        return false;
    }

    @Override
    public boolean visit(MySqlCreateUserStatement.UserSpecification x) {
        x.getUser().accept(this);
        if (x.getAuthPlugin() != null) {
            println();
            print("-- ");
            printUcase(" identified with ");
            x.getAuthPlugin().accept(this);
            println();
            gaussFeatureNotSupportLog("auth_plugin when it creates user" + getTypeAttribute(x));
        }
        if (x.getPassword() != null) {
            printUcase(" PASSWORD ");
            x.getPassword().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(MySqlUserName x) {
        StringBuilder buf = new StringBuilder();
        String userName = x.getUserName();
        String host = x.getHost();
        if (userName.length() != 0 && (userName.charAt(0) == '\'' || userName.charAt(0) == '`')) {
            buf.append(userName);
        } else {
            buf.append('\'');
            buf.append(userName);
            buf.append('\'');
        }

        if (host != null) {
            buf.append('@');
            if (host.length() != 0 && (host.charAt(0) == '\'' || host.charAt(0) == '`')) {
                buf.append(host);
            } else {
                buf.append('\'');
                buf.append(host);
                buf.append('\'');
            }
        }

        String identifiedBy = x.getIdentifiedBy();
        if (identifiedBy != null) {
            buf.append(getText(" identified by '"));
            buf.append(identifiedBy);
            buf.append("'");
        }

        print(buf.toString());
        return false;
    }

    @Override
    public boolean visit(MySqlAlterUserStatement x) {
        for (int i = 0; i < x.getAlterUsers().size(); i++) {
            MySqlAlterUserStatement.AlterUser alterUser = x.getAlterUsers().get(i);
            if (!alterUser.getUser().toString().equals("user()")) {
                if (i != 0) {
                    print(';');
                }
                printUcase("alter user ");
                alterUser.getUser().accept(this);
            } else {
                println();
                print("-- ");
                printUcase("user()");
                println();
                gaussFeatureNotSupportLog("user() when it alters user" + getTypeAttribute(x));
                return false;
            }
            if (alterUser.getAuthOption() != null) {
                print(" IDENTIFIED BY ");
                SQLCharExpr authString = alterUser.getAuthOption().getAuthString();
                authString.accept(this);
            }
        }
        MySqlAlterUserStatement.PasswordOption passwordOption = x.getPasswordOption();
        if (passwordOption != null) {
            switch (passwordOption.getExpire()) {
                case PASSWORD_EXPIRE:
                    printUcase(" password expire");
                    return false;
                case PASSWORD_EXPIRE_DEFAULT:
                    println();
                    print("-- ");
                    printUcase(" password expire default");
                    println();
                    gaussFeatureNotSupportLog(
                            "PASSWORD EXPIRE DEFAULT when it alters user" + getTypeAttribute(x));
                    return false;
                case PASSWORD_EXPIRE_NEVER:
                    println();
                    print("-- ");
                    printUcase(" password expire never");
                    println();
                    gaussFeatureNotSupportLog("PASSWORD EXPIRE NEVER when it alters user" + getTypeAttribute(x));
                    return false;
                case PASSWORD_EXPIRE_INTERVAL:
                    println();
                    print("-- ");
                    printUcase(" password expire interval ");
                    passwordOption.getIntervalDays().accept(this);
                    printUcase(" day");
                    println();
                    gaussFeatureNotSupportLog(
                            "PASSWORD_EXPIRE_INTERVAL N DAY when it alters user" + getTypeAttribute(x));
                    return false;
            }
        }
        return false;
    }

    @Override
    public boolean visit(SQLRenameUserStatement x) {
        printUcase("alter user ");
        x.getName().accept(this);
        printUcase(" rename to ");
        x.getTo().accept(this);
        return false;
    }

    @Override
    public boolean visit(SQLRollbackStatement x) {
        printUcase("rollback");
        if (x.getChain() != null)
            if (x.getChain().booleanValue()) {
                println();
                print("-- ");
                printUcase(" and chain");
                println();
                gaussFeatureNotSupportLog("AND CHAIN when it rollbacks" + getTypeAttribute(x));
            } else {
                println();
                print("-- ");
                printUcase(" and no chain");
                println();
                gaussFeatureNotSupportLog("AND NO CHAIN when it rollbacks" + getTypeAttribute(x));
            }
        if (x.getRelease() != null)
            if (x.getRelease().booleanValue()) {
                println();
                print("-- ");
                printUcase(" and release");
                println();
                gaussFeatureNotSupportLog("RELEASE when it rollbacks" + getTypeAttribute(x));
            } else {
                println();
                print("-- ");
                printUcase(" and no release");
                println();
                gaussFeatureNotSupportLog("NO RELEASE when it rollbacks" + getTypeAttribute(x));
            }
        return false;
    }

    @Override
    public boolean visit(SQLCommitStatement x) {
        printUcase("commit");
        if (x.isWork())
            printUcase(" work");
        if (x.getChain() != null)
            if (x.getChain().booleanValue()) {
                println();
                print("-- ");
                printUcase(" and chain");
                println();
                gaussFeatureNotSupportLog("AND CHAIN when it commits" + getTypeAttribute(x));
            } else {
                println();
                print("-- ");
                printUcase(" and no chain");
                println();
                gaussFeatureNotSupportLog("AND NO CHAIN when it commits" + getTypeAttribute(x));
            }
        if (x.getRelease() != null)
            if (x.getRelease().booleanValue()) {
                println();
                print("-- ");
                printUcase(" and release");
                println();
                gaussFeatureNotSupportLog("RELEASE when it commits" + getTypeAttribute(x));
            } else {
                println();
                print("-- ");
                printUcase(" and no release");
                println();
                gaussFeatureNotSupportLog("NO RELEASE when it commits" + getTypeAttribute(x));
            }
        return false;
    }

    @Override
    public boolean visit(SQLStartTransactionStatement x) {
        printUcase("start transaction");
        if (x.isConsistentSnapshot()) {
            println();
            print("-- ");
            printUcase(" with consistent snapshot");
            println();
            gaussFeatureNotSupportLog(
                    "WITH CONSISTENT SNAPSHOT when it starts transaction" + getTypeAttribute(x));
        }
        if (x.isReadOnly())
            printUcase(" read only");
        return false;
    }

    @Override
    public boolean visit(MySqlSetTransactionStatement x) {
        printUcase("set ");
        if (x.getGlobal() != null && x.getGlobal().booleanValue()) {
            println();
            print("-- ");
            printUcase("global ");
            println();
            gaussFeatureNotSupportLog("GLOBAL when it sets transaction" + getTypeAttribute(x));
        }
        if (x.getSession() != null && x.getSession().booleanValue()) {
            printUcase("SESSION CHARACTERISTICS AS TRANSACTION ");
        } else
            printUcase("LOCAL TRANSACTION ");
        if (x.getIsolationLevel() != null) {
            printUcase("isolation level ");
            if ("read uncommitted".equalsIgnoreCase(x.getIsolationLevel())) {
                println();
                print("-- ");
                printUcase("read committed ");
                println();
                gaussFeatureNotSupportLog("READ UNCOMMITTED when it sets transaction" + getTypeAttribute(x));
            } else
                print0(x.getIsolationLevel());
        }
        String accessModel = x.getAccessModel();
        if (accessModel != null) {
            printUcase("read ");
            print0(accessModel);
        }
        return false;
    }

    @Override
    public boolean visit(SQLPurgeLogsStatement x) {
        logger.error("openGauss does not support purge binary log statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlDeclareHandlerStatement x) {
        println(x.toString());
        return false;
    }

    @Override
    public boolean visit(MySqlCheckTableStatement x) {
        logger.error("openGauss does not support check table statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlChecksumTableStatement x) {
        logger.error("openGauss does not support checksum table statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlOptimizeStatement x) {
        logger.error("openGauss does not support optimize table statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlCreateServerStatement x) {
        logger.error("the create server statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlAlterServerStatement x) {
        logger.error("the alter server statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlBinlogStatement x) {
        logger.error("openGauss does not support binlog statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlFlushStatement x) {
        logger.error("openGauss does not support flush statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlKillStatement x) {
        logger.error("openGauss does not support kill statement" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlResetStatement x) {
        logger.error("the reset statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlCreateTableSpaceStatement x) {
        logger.error("the create tablespace statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x, "createTableSpace");
        return false;
    }

    @Override
    public boolean visit(MySqlAlterTablespaceStatement x) {
        logger.error("the alter tablespace statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x, "alterTableSpace");
        return false;
    }

    @Override
    public boolean visit(SQLDropTableSpaceStatement x) {
        printUcase("drop tablespace ");
        if (x.isIfExists()) {
            printUcase("if exists ");
        }
        x.getName().accept(this);
        SQLExpr engine = x.getEngine();
        if (engine != null) {
            printUcaseNotSupportWord("engine " + engine);
            gaussFeatureNotSupportLog("specifying engine" + getTypeAttribute(x));
        }
        return false;
    }

    // 1->schema 2->table 3->function 4->procedure
    private static final ThreadLocal<Integer> threadLocalForResourceState = new ThreadLocal<Integer>();

    @Override
    public boolean visit(SQLGrantStatement x) {
        List<SQLPrivilegeItem> rawPrivileges = x.getPrivileges();
        try {
            List<SQLPrivilegeItem> commonSchemaPrivilegeList = Collections.emptyList();
            List<SQLPrivilegeItem> executePrivilegeList = Collections.emptyList();
            List<SQLPrivilegeItem> tablePrivilegeList = Collections.emptyList();
            List<SQLPrivilegeItem> unsupportedPrivilegeList = Collections.emptyList();
            // db_name.* , Consider case by case
            SQLObject resource = x.getResource();
            if (resource instanceof SQLPropertyExpr) {
                logger.error("\"db_name.routine_name\" privilege level in grant is incompatible with openGauss"
                        + getTypeAttribute(x));
                errHandle(x, "single routine");
                return false;
            }
            SQLExpr expr = ((SQLExprTableSource) resource).getExpr();
            if (expr instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
                SQLExpr owner = propertyExpr.getOwner();
                if (owner instanceof SQLIdentifierExpr
                        && propertyExpr.getName().trim().equals("*")) {
                    List<SQLPrivilegeItem> privileges = x.getPrivileges();
                    // db_name.*->on Schema schema_name syntax
                    commonSchemaPrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return commonSchemaPrivilegeSet.contains(privilegeName)
                                        && !privilegeName.equals("EXECUTE");
                            })
                            .collect(Collectors.toList());
                    // db_name.*->all tables in schema schema_name syntax
                    tablePrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return tablePrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                    // db_name.*->all function
                    // db_name.*->all procedure
                    executePrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return routinePrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                    unsupportedPrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return !commonSchemaPrivilegeSet.contains(privilegeName)
                                        && !tablePrivilegeSet.contains(privilegeName)
                                        && !routinePrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                }
            }
            if (!commonSchemaPrivilegeList.isEmpty() || !tablePrivilegeList.isEmpty() || !executePrivilegeList.isEmpty()
                    || !unsupportedPrivilegeList.isEmpty()) {
                Field privilegeField = x.getClass().getSuperclass().getDeclaredField("privileges");
                privilegeField.setAccessible(true);
                if (!commonSchemaPrivilegeList.isEmpty()) {
                    privilegeField.set(x, commonSchemaPrivilegeList);
                    threadLocalForResourceState.set(1);
                    visitGrant(x);
                    if (!tablePrivilegeList.isEmpty() || !executePrivilegeList.isEmpty()) {
                        println(";");
                    }
                }
                if (!tablePrivilegeList.isEmpty()) {
                    privilegeField.set(x, tablePrivilegeList);
                    threadLocalForResourceState.set(2);
                    visitGrant(x);
                    if (!executePrivilegeList.isEmpty()) {
                        println(";");
                    }
                }
                if (!executePrivilegeList.isEmpty()) {
                    privilegeField.set(x, executePrivilegeList);
                    // make use of threadlocal flag: visit twice，all function and all procedure
                    threadLocalForResourceState.set(3);
                    visitGrant(x);
                    println(";");
                    threadLocalForResourceState.set(4);
                    // postVisit will add the semicolon
                    visitGrant(x);
                }
                if (!unsupportedPrivilegeList.isEmpty()) {
                    // not supported
                    threadLocalForResourceState.set(5);
                    privilegeField.set(x, unsupportedPrivilegeList);
                    visitGrant(x);
                }
            } else {
                visitGrant(x);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        } finally {
            threadLocalForResourceState.remove();
            try {
                Field privileges = x.getClass().getSuperclass().getDeclaredField("privileges");
                privileges.set(x, rawPrivileges);
                privileges.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
        }
        return false;
    }

    private void visitGrant(SQLGrantStatement x) {
        printUcase("grant ");
        printAndAccept(x.getPrivileges(), ", ");
        printGrantOn(x);
        if (x.getUsers() != null) {
            printUcase(" to ");
            printAndAccept(x.getUsers(), ",");
        }
        if (x.getWithGrantOption()) {
            printUcase(" with grant option");
        }
        if (x.getMaxConnectionsPerHour() != null ||
                x.getMaxQueriesPerHour() != null ||
                x.getMaxUpdatesPerHour() != null ||
                x.getMaxUserConnections() != null) {
            println();
            print("-- ");
        }
        boolean with = false;
        SQLExpr maxQueriesPerHour = x.getMaxQueriesPerHour();
        if (maxQueriesPerHour != null) {
            if (!with) {
                printUcase(" with");
                with = true;
            }
            printUcase(" max_queries_per_hour ");
            maxQueriesPerHour.accept(this);
            logger.warn("the maxQueriesPerHour property of grant is incompatible with openGauss"
                    + getTypeAttribute(x));
        }
        SQLExpr maxUpdatesPerHour = x.getMaxUpdatesPerHour();
        if (maxUpdatesPerHour != null) {
            if (!with) {
                printUcase(" with");
                with = true;
            }
            printUcase(" max_updates_per_hour ");
            maxUpdatesPerHour.accept(this);
            logger.warn("the maxQueriesPerHour property of grant is incompatible with openGauss"
                    + getTypeAttribute(x));
        }
        SQLExpr maxConnectionsPerHour = x.getMaxConnectionsPerHour();
        if (maxConnectionsPerHour != null) {
            if (!with) {
                printUcase(" with");
                with = true;
            }
            printUcase(" max_connections_per_hour ");
            maxConnectionsPerHour.accept(this);
            logger.warn("the maxQueriesPerHour property of grant is incompatible with openGauss"
                    + getTypeAttribute(x));
        }
        SQLExpr maxUserConnections = x.getMaxUserConnections();
        if (maxUserConnections != null) {
            if (!with) {
                printUcase(" with");
                with = true;
            }
            printUcase(" max_user_connections ");
            logger.warn("the maxQueriesPerHour property of grant is incompatible with openGauss"
                    + getTypeAttribute(x));
        }
        if (x.getIdentifiedBy() != null) {
            logger.error("the 'identified by' in grant is incompatible with openGauss" + getTypeAttribute(x));
            errHandle(x);
        }
    }

    @Override
    public boolean visit(SQLPrivilegeItem x) {
        SQLExpr action = x.getAction();
        String name = ((SQLIdentifierExpr) action).getName().toUpperCase().trim();
        if (incompatiblePrivilegeSet.contains(name)) {
            logger.error(
                    String.format("Privilege %s is incompatible with OpenGauss", name) + getTypeAttribute(x));
            errHandle(x, "incompatible privilege");
        }
        print(action.toString());
        if (!x.getColumns().isEmpty()) {
            print0("(");
            printAndAccept(x.getColumns(), ", ");
            print0(")");
        }
        return false;
    }

    @Override
    public void printGrantOn(SQLGrantStatement x) {
        if (x.getResource() != null) {
            printUcase(" on ");
            SQLObjectType resourceType = x.getResourceType();
            if (resourceType != null) {
                printUcase(resourceType.name_lcase);
                print(' ');
            }
            SQLObject resource = x.getResource();
            SQLExpr expr = ((SQLExprTableSource) resource).getExpr();
            if (expr instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
                SQLExpr owner = propertyExpr.getOwner();
                if (owner instanceof SQLAllColumnExpr && propertyExpr.getName().equals("*")) {
                    // *.*
                    logger.error(
                            "privilege level *.* in grant is incompatible with openGauss" + getTypeAttribute(x));
                    errHandle(x, "*.*");
                } else {
                    if (propertyExpr.getName().equals("*")) {
                        // db_name.*
                        SQLIdentifierExpr identOwner = (SQLIdentifierExpr) owner;
                        List<SQLPrivilegeItem> privileges = x.getPrivileges();
                        String firstPrivilege = ((SQLIdentifierExpr) privileges.get(0).getAction()).getName()
                                .toUpperCase().trim();
                        switch (threadLocalForResourceState.get() != null ? threadLocalForResourceState.get()
                                : Integer.MAX_VALUE) {
                            case 1:
                                printUcase("SCHEMA ");
                                break;
                            case 2:
                                printUcase("ALL TABLES IN SCHEMA ");
                                break;
                            case 3:
                                printUcase("ALL FUNCTIONS IN SCHEMA ");
                                break;
                            case 4:
                                printUcase("ALL PROCEDURE IN SCHEMA ");
                                break;
                            case 5:
                                break;
                            default: {
                                logger.error("unkown threadLocalForResourceState" + getTypeAttribute(x));
                                errHandle(x);
                            }
                        }
                        printName0(identOwner.getName());
                    } else {
                        if (resourceType == null
                                || resourceType.name.toUpperCase().trim().equals("TABLE")) {
                            // db_name.tbl_name
                            List<SQLPrivilegeItem> privileges = x.getPrivileges();
                            Set<String> privilegeSet = privileges
                                    .stream()
                                    .map(e -> ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase().trim())
                                    .collect(Collectors.toSet());
                            if (privileges.contains("USAGE") || privileges.contains("CREATE")) {
                                // when usage and create appear with db_name.tb_name,they cannot be translated
                                // into opengauss syntax
                                logger.error(
                                        "\"Usage\" and \"create\" with privilege level \"db_name.*\" in grant is incompatible with openGauss"
                                                + getTypeAttribute(x));
                                errHandle(x, "incompatible privilege");
                            }
                            // db_name.routine_name
                            if (resourceType != null && resourceType.name.toUpperCase().trim().equals("TABLE")) {
                                logger.error("grant single function or procedure is incompatible with openGauss"
                                        + getTypeAttribute(x));
                                errHandle(x, "single routine");
                            }
                            printName0(propertyExpr.getName());
                        } else {
                            // db_name.routine_name
                            logger.error(
                                    "\"db_name.routine_name\" privilege level in grant is incompatible with openGauss"
                                            + getTypeAttribute(x));
                            errHandle(x, "single routine");
                        }
                    }
                }
            } else if (expr instanceof SQLAllColumnExpr) {
                // *
                logger.error("privilege level * in grant is incompatible with openGauss" + getTypeAttribute(x));
                errHandle(x, "*");
            } else {
                // table_name
                expr.accept(this);
            }
        }
    }

    @Override
    public boolean visit(SQLRevokeStatement x) {
        List<SQLPrivilegeItem> rawPrivileges = x.getPrivileges();
        try {
            List<SQLPrivilegeItem> SchemaPrivilegeList = Collections.emptyList();
            List<SQLPrivilegeItem> routinePrivilegeList = Collections.emptyList();
            List<SQLPrivilegeItem> tablePrivilegeList = Collections.emptyList();
            // db_name.* consider case by case
            SQLObject resource = x.getResource();
            if (resource == null) {
                visitRevoke(x);
                return false;
            }
            if (resource instanceof SQLPropertyExpr) {
                logger.error("\"db_name.routine_name\" privilege level in revoke is incompatible with openGauss"
                        + getTypeAttribute(x));
                errHandle(x, "single rountine");
                return false;
            }
            SQLExpr expr = ((SQLExprTableSource) resource).getExpr();
            if (expr instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
                SQLExpr owner = propertyExpr.getOwner();
                if (owner instanceof SQLIdentifierExpr
                        && propertyExpr.getName().trim().equals("*")) {
                    List<SQLPrivilegeItem> privileges = x.getPrivileges();
                    // db_name.*->on Schema schema_name syntax
                    SchemaPrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return commonSchemaPrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                    // db_name.*->all tables in schema schema_name syntax
                    tablePrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return tablePrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                    // db_name.*->all function
                    // db_name.*->all procedure
                    routinePrivilegeList = privileges
                            .stream()
                            .filter(e -> {
                                String privilegeName = ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase()
                                        .trim();
                                return routinePrivilegeSet.contains(privilegeName);
                            })
                            .collect(Collectors.toList());
                }
            }
            if (!SchemaPrivilegeList.isEmpty() || !routinePrivilegeList.isEmpty() || !tablePrivilegeList.isEmpty()) {
                Field privilegeField = x.getClass().getSuperclass().getDeclaredField("privileges");
                privilegeField.setAccessible(true);
                if (!SchemaPrivilegeList.isEmpty()) {
                    privilegeField.set(x, SchemaPrivilegeList);
                    threadLocalForResourceState.set(1);
                    visitRevoke(x);
                    if (!routinePrivilegeList.isEmpty() || !tablePrivilegeList.isEmpty()) {
                        println(";");
                    }
                }
                if (!routinePrivilegeList.isEmpty()) {
                    privilegeField.set(x, routinePrivilegeList);
                    threadLocalForResourceState.set(3);
                    // access twice using threadlocal flag, all function and all procedure
                    visitRevoke(x);
                    println(";");
                    // postVisit add semicolon
                    threadLocalForResourceState.set(4);
                    visitRevoke(x);
                    if (!tablePrivilegeList.isEmpty()) {
                        println(";");
                    }
                }
                if (!tablePrivilegeList.isEmpty()) {
                    privilegeField.set(x, tablePrivilegeList);
                    threadLocalForResourceState.set(2);
                    visitRevoke(x);
                }
            } else {
                visitRevoke(x);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        } finally {
            try {
                Field privileges = x.getClass().getSuperclass().getDeclaredField("privileges");
                privileges.set(x, rawPrivileges);
                privileges.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
        }
        return false;
    }

    private void visitRevoke(SQLRevokeStatement x) {
        printUcase("revoke ");
        if (x.getResource() != null) {
            for (int i = 0; i < x.getPrivileges().size(); i++) {
                x.getPrivileges().get(i).setParent(x);
            }
            this.printAndAccept(x.getPrivileges(), ", ");
            printRevokeOn(x);
        } else {
            printUcase(" ALL PRIVILEGE ");
        }
        if (x.getUsers() != null) {
            printUcase(" from ");
            this.printAndAccept(x.getUsers(), ", ");
        }
    }

    private void printRevokeOn(SQLRevokeStatement x) {
        if (x.getResource() != null) {
            printUcase(" on ");
            SQLObjectType resourceType = x.getResourceType();
            if (resourceType != null) {
                printUcase(resourceType.name_lcase);
                print(' ');
            }
            SQLExpr expr = ((SQLExprTableSource) x.getResource()).getExpr();
            if (expr instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
                SQLExpr owner = propertyExpr.getOwner();
                if (owner instanceof SQLAllColumnExpr && propertyExpr.getName().equals("*")) {
                    // *.*
                    logger.error(
                            "privilege level *.* in revoke is incompatible with OpenGauss" + getTypeAttribute(x));
                    errHandle(x, "*.*");
                } else {
                    if (propertyExpr.getName().equals("*")) {
                        // db_name.*
                        SQLIdentifierExpr identOwner = (SQLIdentifierExpr) owner;
                        switch (threadLocalForResourceState.get()) {
                            case 1:
                                printUcase("SCHEMA ");
                                break;
                            case 2:
                                printUcase("ALL TABLES IN SCHEMA ");
                                break;
                            case 3:
                                printUcase("ALL FUNCTIONS IN SCHEMA ");
                                break;
                            case 4:
                                printUcase("ALL PROCEDURE IN SCHEMA ");
                                break;
                            default: {
                                logger.error("unkown threadLocalForResourceState" + getTypeAttribute(x));
                                errHandle(x);
                            }
                        }
                        printName0(identOwner.getName());
                    } else {
                        if (resourceType == null
                                || resourceType.name.toUpperCase().trim().equals("TABLE")) {
                            // db_name.tbl_name
                            List<SQLPrivilegeItem> privileges = x.getPrivileges();
                            Set<String> privilegeSet = privileges
                                    .stream()
                                    .map(e -> ((SQLIdentifierExpr) e.getAction()).getName().toUpperCase().trim())
                                    .collect(Collectors.toSet());
                            if (privilegeSet.contains("USAGE") || privilegeSet.contains("CREATE")) {
                                // usage, create cannot be converted to opengauss syntax when db_name.tb_name
                                logger.error(
                                        "\"Usage\" and \"create\" with privilege level \"db_name.*\" in revoke is incompatible with openGauss"
                                                + getTypeAttribute(x));
                                errHandle(x);
                            }
                            // db_name.routine_name
                            if (resourceType != null && resourceType.name.toUpperCase().trim().equals("TABLE")) {
                                logger.error("revoke single function or procedure is incompatible with openGauss"
                                        + getTypeAttribute(x));
                                errHandle(x);
                            }
                            printName0(propertyExpr.getName());
                        } else {
                            // db_name.routine_name
                            logger.error(
                                    "\"db_name.routine_name\" privilege level in revoke is incompatible with openGauss"
                                            + getTypeAttribute(x));
                            errHandle(x);
                        }
                    }

                }
            } else if (expr instanceof SQLAllColumnExpr) {
                // *
                logger.error("privilege level * in revoke is incompatible with openGauss" + getTypeAttribute(x));
                errHandle(x);
            } else {
                // table_name
                expr.accept(this);
            }
        }
    }

    @Override
    public boolean visit(MySqlPrepareStatement x) {
        logger.error("the prepare statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    @Override
    public boolean visit(MySqlExecuteStatement x) {
        logger.error("the execute statement is incompatible with openGauss" + getTypeAttribute(x));
        errHandle(x);
        return false;
    }

    private String getText(String text) {
        return ucase ? text.toUpperCase() : text.toLowerCase();
    }
}
