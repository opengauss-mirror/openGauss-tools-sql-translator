/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-tools-sql-translator is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.sqltranslator.dialect.mysql.util;

import com.alibaba.druid.sql.SQLTransformUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.util.FnvHash.Constants;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenGaussDataTypeTransformUtil extends SQLTransformUtils {
    private static final Logger logger= LoggerFactory.getLogger(OpenGaussDataTypeTransformUtil.class);
    enum T {
        NEW,CLONE, NONSUPPORT;
    }
    private static final Map<String, Pair<T,String>> typeMap = new HashMap<String,Pair<T,String>>(){
        {
            put("TINYINT",new Pair<T, String>(T.NEW,"SMALLINT"));
            put("MEDIUMINT",new Pair<T, String>(T.NEW,"INTEGER"));
            put("SMALLINT",new Pair<T, String>(T.NEW,"SMALLINT"));
            put("INT",new Pair<T, String>(T.NEW,"INTEGER"));
            put("BIGINT",new Pair<T, String>(T.NEW,"BIGINT"));
            put("INTEGER",new Pair<T, String>(T.NEW,"INTEGER"));
            put("YEAR",new Pair<T, String>(T.CLONE,"INTEGER"));
            put("DECIMAL",new Pair<T, String>(T.CLONE,"DECIMAL"));
            put("FLOAT",new Pair<T, String>(T.CLONE,"FLOAT"));
            put("REAL",new Pair<T, String>(T.CLONE,"REAL"));
            put("DOUBLE",new Pair<T,String>(T.NEW,"DOUBLE PRECISION"));
            put("DATETIME",new Pair<T, String>(T.CLONE,"TIMESTAMP"));
            put("TINYTEXT",new Pair<T, String>(T.NEW,"TEXT"));
            put("MEDIUMTEXT",new Pair<T, String>(T.NEW,"TEXT"));
            put("LONGTEXT",new Pair<T, String>(T.NEW,"TEXT"));
            put("TEXT",new Pair<T, String>(T.NEW,"TEXT"));
            put("BINARY",new Pair<T, String>(T.NEW,"BYTEA"));
            put("VARBINARY",new Pair<T, String>(T.NEW,"BYTEA"));
            put("MULTIPOINT",new Pair<T, String>(T.NEW,"BYTEA"));
            put("GEOMETRYCOLLECTION",new Pair<T, String>(T.NEW,"BYTEA"));
            put("MULTILINESTRING",new Pair<T, String>(T.NEW,"BYTEA"));
            put("MULTIPOLYGON",new Pair<T, String>(T.NEW,"BYTEA"));
            put("TINYBLOB",new Pair<T, String>(T.NEW,"BLOB"));
            put("BLOB",new Pair<T, String>(T.NEW,"BLOB"));
            put("LONGBLOB",new Pair<T, String>(T.NEW,"BLOB"));
            put("MEDIUMBLOB",new Pair<T, String>(T.NEW,"BLOB"));
            put("BIT",new Pair<T, String>(T.CLONE,"INTEGER"));
            put("BOOLEAN",new Pair<T, String>(T.NEW,"BOOLEAN"));
            put("BOOL",new Pair<T, String>(T.NEW,"BOOLEAN"));
            put("GEOMETRY",new Pair<T, String>(T.NEW,"POINT"));
            put("LINESTRING",new Pair<T, String>(T.NEW,"PATH"));
            put("VARCHAR",new Pair<T, String>(T.CLONE,"VARCHAR"));
            put("CHAR",new Pair<T, String>(T.CLONE,"CHAR"));
            put("TIME",new Pair<T, String>(T.CLONE,"TIME"));
            put("DATE",new Pair<T, String>(T.NEW,"DATE"));
            put("TIMESTAMP",new Pair<T, String>(T.CLONE,"TIMESTAMP"));
            put("POINT",new Pair<T, String>(T.NEW,"POINT"));
            put("POLYGON",new Pair<T, String>(T.NEW,"POLYGON"));
            put("JSON",new Pair<T, String>(T.NEW,"JSON"));
            put("ENUM",new Pair<T, String>(T.NONSUPPORT,""));
            put("SET",new Pair<T, String>(T.NONSUPPORT,""));
        }
    };

    public static SQLDataType transformOpenGaussToMysql(SQLDataType type) {
        String uName = type.getName().toUpperCase();
        long nameHash = type.nameHashCode64();
        List<SQLExpr> arguments = type.getArguments();
        SQLDataType dataType;
        SQLExpr arg0;
        int precision = -1;
        SQLObject parent = type.getParent();
        boolean isAutoIncrement = parent instanceof SQLColumnDefinition && ((SQLColumnDefinition) parent).isAutoIncrement();
        if(typeMap.containsKey(uName)) {
            if ((type.isInt() || uName.equals("MEDIUMINT")) && isAutoIncrement) {
                dataType = new SQLDataTypeImpl("BIGSERIAL");
            }
            else if(isFloatPoint(nameHash) && arguments.size() > 0){
                arg0 = arguments.get(0);
                precision = ((SQLIntegerExpr)arg0).getNumber().intValue();
                dataType = new SQLCharacterDataType("FLOAT", precision);
            }
            else{
                Pair<T,String> p = typeMap.get(uName);
                if(p.getKey() == T.NEW){
                    dataType = new SQLDataTypeImpl(p.getValue());
                } else if(p.getKey() == T.CLONE){
                    dataType = type.clone(); //Clone is to retain the content of parentheses
                    dataType.setName(p.getValue());
                }else {
                    logger.error("NOT SUPPORT TYPE " + type.getName());
                    dataType = type.clone();
                }
            }
            if (isAutoIncrement && isFloatPoint(nameHash)) {
                logger.warn("openGauss only support int autoincrement");
            }
        }
        else{
            logger.error("UNKNOWN TYPE:"+uName);
            dataType = type.clone();
        }
        dataType.setParent(type.getParent());
        return dataType;

    }

    private static boolean isFloatPoint(long nameHash){
        return nameHash == Constants.FLOAT || nameHash == Constants.DOUBLE || nameHash == Constants.DOUBLE_PRECISION ||
                nameHash == Constants.REAL;
    }
}
