/*
 * Copyright (c) 2005-2013 Clear2Pay nv/sa. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Clear2Pay nv/sa. ("Confidential Information").
 * It may not be copied or reproduced in any manner without the express written permission of Clear2Pay nv/sa.
 *
 */
package com.clear2pay.bph.scb.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

/*This class is used for generate sql script for required tables from vsdk delta files which is provided by BA.
 * This class is maintain the sequence of generate sql scripts to delete and inserts.
 * Every statement nextLine.getCell() denotes to fetch excel column value .
 */
public class IncrementalDMLTool {

    private static final String DELETE = "Delete";

    public static void main(String[] args) {
        try {
            String rowtype;
            String filepath = null;
            /*To verify the file extension*/
            for (String path : args) {
                String ext = getFileExtension(path);
                if ("txt".equals(ext)) {
                    callReadTxtFile(path);
                } else if ("xls".equals(ext)) {
                    filepath = path;
                }
            }
            //skip if no xls file
            if (filepath == null) {
                return;
            }
            InputStream is = new FileInputStream(filepath);
            POIFSFileSystem fs = new POIFSFileSystem(is);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            int sheetCount = wb.getNumberOfSheets();

            Map<String, List<StringBuilder>> insert = new LinkedHashMap<String, List<StringBuilder>>();
            Map<String, List<StringBuilder>> del = new LinkedHashMap<String, List<StringBuilder>>();

            /*Maintaining a sequence of tables to insert and delete data*/
            String[] tableOrder = new String[] { "BANKGROUP", "BUSINESSCALENDAR", "AGENTCODE", "BICPLUSIBAN", "PERSON",
                    "BANKUSER", "BANKDEPARTMENT", "BANKROLE", "BANKPAYMENTTYPE", "BANKUSERROLE", "VISIBILITYSERVICE",
                    "VISSERVICEATTRDEFINITION", "VISSERVICEATTR", "BANKFUNCTION", "BANKFAP", "BANKDAPENTITY", "BANKDAP",
                    "IBANSTRUCTURE", "CHARGECONFIGURATION", "PARTICIPANT", "ParticipationDirectory",
                    "EXCHANGECONDITION", "EXCHANGECRITERION", "EXCHANGECRITERIONLITERAL", "ACCOUNT", "PARAMETERSERVICE",
                    "PARAMSERVICEATTRDEFINITION", "PARAMSERVICEATTR", "PROCESSINGPRODUCT", "PROCESSINGSERVICE",
                    "PROCPRODUCTSERVICE", "PROCESSINGAGREEMENT", "PROCAGREEMENTACCOUNT", "PROCAGREEMENTSERVICE",
                    "PROCSERVICEATTRDEFINITION", "PROCAGREEMENTSERVICEATTR", "PROCPRODUCTSERVICEATTR",
                    "PROCSERVICEATTR", "RATETYPEDETERMINATIONRULE", "RISKFILTER", "RISKFILTERCRITERION",
                    "RISKFILTERCRITERIONLITERAL", "ROUTINGRULE", "ROUTINGCRITERION", "SSI", "SSIBANK",
                    "SETTLEMENTTIMELINE", "SETTLEMENTTIMELINEDETAIL", "SECAUTHENTICATION", "SECPASSWORDHISTORY",
                    "TRUSTEDAGENT", "SWIFTCOUNTERPART", "SWIFTCOUNTERPARTPERMISSION", "HOLIDAYDATE", "SWIFTCURRENCY",
                    "BICNARMAP", "CURRENCYGROUP", "CURRENCYRATE", "BANKCURRENCY", "WORKQUEUE", "WORKLISTQCRITERIA",
                    "BUSINESSEVENTDEFINITION", "SCHEMEREASON", "SCHEMEREASONCODESET", "OPS_PARAMETER" };
            for (String s : tableOrder) {
                insert.put(s, new ArrayList<StringBuilder>());
            }
            for (int i = tableOrder.length - 1; i >= 0; i--) {
                del.put(tableOrder[i], new ArrayList<StringBuilder>());
            }

            System.out.println("sheetCount=" + sheetCount);
            for (int sheetNo = 0; sheetNo < sheetCount; sheetNo++) {
                String sheetName = wb.getSheetName(sheetNo);
                // check if this sheet should be processed
                System.out.println();
                System.out.println();
                System.out.println("-- " + sheetName + " statements;");
                HSSFSheet sheet = wb.getSheetAt(sheetNo);

                Row nextLine;
                Iterator<Row> iterator = sheet.iterator();

                // read first line with groups names
                nextLine = iterator.next();
                if (nextLine == null) {
                    throw new RuntimeException("no data in file");
                }
                while (iterator.hasNext()) {
                    try {
                        if ("BusinessEventDefinition".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(6).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into BUSINESSEVENTDEFINITION (BUSINESSEVENTDEFINITIONKEY,BUSINESSDOMAIN,CATALOGCODE,FUNCTIONCODE,BUSINESSEVENTENTITYTYPE,FOREACHENTITYTYPE,DESCRIPTION,VERSION,WHENMODIFIED)"
                                                + "values ((" + getBusinessEventDefinitionKey() + "),'"
                                                + nextLine.getCell(0) + "','" + nextLine.getCell(1) + "','"
                                                + nextLine.getCell(2) + "','" + nextLine.getCell(3) + "','"
                                                + nextLine.getCell(4) + "','" + nextLine.getCell(5) + "',0, sysdate); ";
                                insert.get("BUSINESSEVENTDEFINITION").add(new StringBuilder(s));
                            } else if (DELETE.equalsIgnoreCase(rowtype)) {
                                String s = "delete from BUSINESSEVENTDEFINITION where BUSINESSDOMAIN='"
                                        + nextLine.getCell(0) + "'" + " and CATALOGCODE='" + nextLine.getCell(1) + "'"
                                        + " and FUNCTIONCODE='" + nextLine.getCell(2) + "'"
                                        + " and BUSINESSEVENTENTITYTYPE='" + nextLine.getCell(3) + "'"
                                        + " and FOREACHENTITYTYPE='" + nextLine.getCell(4) + "';";
                                del.get("BUSINESSEVENTDEFINITION").add(new StringBuilder(s));
                            }
                        } else if ("SchemeReason".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(5).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into SCHEMEREASON (SCHEMEREASONKEY,SCHEME,REASONCODE,REASONPROPRIETARYCODE,REASONDISPLAYID,ADDITIONALINFORMATIONINDICATOR,VERSION,WHENMODIFIED)"
                                                + "values ((" + getSchemeReasonKey() + "),'" + nextLine.getCell(0)
                                                + "','" + nextLine.getCell(1) + "','" + nextLine.getCell(2) + "','"
                                                + nextLine.getCell(3) + "','" + nextLine.getCell(4) + "',0, sysdate); ";
                                insert.get("SCHEMEREASON").add(new StringBuilder(s));
                            } else if (DELETE.equalsIgnoreCase(rowtype)) {
                                String s = "delete from SCHEMEREASON where scheme='" + nextLine.getCell(0) + "'"
                                        + " and REASONCODE='" + nextLine.getCell(1) + "'"
                                        + " and REASONPROPRIETARYCODE='" + nextLine.getCell(2) + "';";
                                del.get("SCHEMEREASON").add(new StringBuilder(s));
                            }
                        } else if ("SchemeReasonCodeSet".equals(sheetName)) {
                            nextLine = iterator.next();
                            String scheme = nextLine.getCell(0).toString();
                            String reasonCode = nextLine.getCell(1).toString();
                            String reasonProprietaryCode = nextLine.getCell(2).toString();
                            rowtype = nextLine.getCell(8).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into SCHEMEREASONCODESET (SCHEMEREASONCODESETKEY,SCHEMEREASONKEY,CODESET,SUBSET,ADDITIONALINFORMATIONCOMPLEMEN,STARTSCHEMEVERSION,ENDSCHEMEVERSION,VERSION,WHENMODIFIED)"
                                                + "values ((" + getSchemeReasonCodeSetKey() + "),("
                                                + getSchemeReasonKey(scheme, reasonCode, reasonProprietaryCode) + "),'"
                                                + nextLine.getCell(3) + "','" + nextLine.getCell(4) + "','"
                                                + nextLine.getCell(5) + "','" + nextLine.getCell(6) + "','"
                                                + nextLine.getCell(7) + "',0, sysdate); ";
                                insert.get("SCHEMEREASONCODESET").add(new StringBuilder(s));
                            } else if (DELETE.equalsIgnoreCase(rowtype)) {
                                String s = "delete from SCHEMEREASONCODESET where CODESET='" + nextLine.getCell(3) + "'"
                                        + " and SUBSET='" + nextLine.getCell(4) + "'"
                                        + " and ADDITIONALINFORMATIONCOMPLEMEN='" + nextLine.getCell(5) + "'"
                                        + " and SCHEMEREASONKEY=(" + getSchemeReasonKey(reasonCode) + ");";
                                del.get("SCHEMEREASONCODESET").add(new StringBuilder(s));
                            }
                        } else if ("TechParameter".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(6).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into OPS_PARAMETER (NAME,NAMESPACE,APPLICATIONNAME,SERVERNAME,VALUE,DESCRIPTION)"
                                                + "values ('" + nextLine.getCell(0) + "','" + nextLine.getCell(1)
                                                + "','" + nextLine.getCell(2) + "','" + nextLine.getCell(3) + "','"
                                                + nextLine.getCell(4) + "','" + nextLine.getCell(5) + "'); ";
                                insert.get("OPS_PARAMETER").add(new StringBuilder(s));
                            } else if (DELETE.equalsIgnoreCase(rowtype)) {
                                String s = "delete from OPS_PARAMETER where NAME='" + nextLine.getCell(0) + "'"
                                        + " and NAMESPACE='" + nextLine.getCell(1) + "'" + " and APPLICATIONNAME='"
                                        + nextLine.getCell(2) + "'" + " and SERVERNAME='" + nextLine.getCell(3) + "';";
                                del.get("OPS_PARAMETER").add(new StringBuilder(s));
                            }
                        } else if ("BankFunction".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(9).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into BANKFUNCTION (BANKFUNCTIONKEY,BANKGROUPID,ENTITY,ACTION,MODULE,ENTITYDISPLAYNAME,VERSION, WHENMODIFIED) "
                                                + "values ((" + getBankFunctionKey() + "),'" + nextLine.getCell(0)
                                                + "','" + nextLine.getCell(1) + "','" + nextLine.getCell(2) + "','"
                                                + nextLine.getCell(3) + "','" + nextLine.getCell(4) + "',0, sysdate); ";
                                insert.get("BANKFUNCTION").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from BANKFUNCTION where BANKGROUPID=('" + nextLine.getCell(0)
                                        + "') and ENTITY=('" + nextLine.getCell(1) + "') and ACTION=('"
                                        + nextLine.getCell(2) + "') and MODULE=('" + nextLine.getCell(3) + "'); ";

                                del.get("BANKFUNCTION").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update BANKFUNCTION set ENTITYDISPLAYNAME=('" + nextLine.getCell(4)
                                        + "') where BANKGROUPID=('" + nextLine.getCell(0) + "') and ENTITY=('"
                                        + nextLine.getCell(1) + "') and ACTION=('" + nextLine.getCell(3)
                                        + "') and MODULE=('" + nextLine.getCell(4) + "'); ";
                                insert.get("BANKFUNCTION").add(new StringBuilder(s));
                            }
                        } else if ("BankDapEntity".equals(sheetName)) {
                            nextLine = iterator.next();
                            String dapBankGroupId = nextLine.getCell(0).toString();
                            String dapEntity = nextLine.getCell(1).toString();
                            String dapAction = nextLine.getCell(2).toString();
                            String dapModule = nextLine.getCell(3).toString();
                            String enableAll =
                                    nextLine.getCell(6) != null ? nextLine.getCell(6).toString().substring(0, 1) : null;
                            String enableAllParent =
                                    nextLine.getCell(7) != null ? nextLine.getCell(7).toString().substring(0, 1) : null;
                            String enableOwn =
                                    nextLine.getCell(8) != null ? nextLine.getCell(8).toString().substring(0, 1) : null;
                            String enableSelection =
                                    nextLine.getCell(9) != null ? nextLine.getCell(9).toString().substring(0, 1) : null;
                            String enableNone = nextLine.getCell(10) != null
                                    ? nextLine.getCell(10).toString().substring(0, 1) : null;
                            try {
                                rowtype = nextLine.getCell(11).getStringCellValue();
                                if ("New".equals(rowtype)) {
                                    String s =
                                            "insert into BANKDAPENTITY (BANKDAPENTITYKEY,BANKFUNCTIONKEY,BANKGROUPID,DAPENTITY,DAPVIEWFIELD,ENABLEALL,ENABLEALLINPARENT,ENABLEOWN,ENABLESELECTION,ENABLENONE,VERSION, WHENMODIFIED)"
                                                    + " values ((" + getBankDapEntityKey() + "),("
                                                    + getBankFunctionKey(dapEntity, dapAction, dapModule,
                                                            nextLine.getCell(0).toString())
                                                    + "),'" + dapBankGroupId + "','" + nextLine.getCell(4) + "','"
                                                    + nextLine.getCell(5) + "','" + enableAll + "','" + enableAllParent
                                                    + "','" + enableOwn + "','" + enableSelection + "','" + enableNone
                                                    + "',0, sysdate);";
                                    insert.get("BANKDAPENTITY").add(new StringBuilder(s));
                                } else if ("Delete".equals(rowtype)) {
                                    String s = "delete from BANKDAPENTITY where BANKFUNCTIONKEY=("
                                            + getBankFunctionKey(dapEntity, dapAction, dapModule,
                                                    nextLine.getCell(0).toString())
                                            + ") and BANKGROUPID=('" + dapBankGroupId + "') and DAPENTITY=('"
                                            + nextLine.getCell(4) + "') and DAPVIEWFIELD=('" + nextLine.getCell(5)
                                            + "');";

                                    del.get("BANKDAPENTITY").add(new StringBuilder(s));

                                } else if (rowtype.equalsIgnoreCase("Update")) {
                                    String s = "update BANKDAPENTITY " + "set " + " BANKFUNCTIONKEY=("
                                            + getBankFunctionKey(dapEntity, dapAction, dapModule,
                                                    nextLine.getCell(0).toString())
                                            + ")," + " BANKGROUPID='" + dapBankGroupId + "',DAPENTITY='"
                                            + nextLine.getCell(5) + "',DAPVIEWFIELD='" + nextLine.getCell(6)
                                            + "', WHENMODIFIED=sysdate where BANKFUNCTIONKEY=("
                                            + getBankFunctionKey(dapEntity, dapAction, dapModule,
                                                    nextLine.getCell(0).toString())
                                            + ") and BANKGROUPID=('" + dapBankGroupId + "') and DAPENTITY=('"
                                            + nextLine.getCell(5) + "') and DAPVIEWFIELD=('" + nextLine.getCell(6)
                                            + "');";
                                    insert.get("BANKDAPENTITY").add(new StringBuilder(s));

                                } else {

                                    System.out.println("BANKDAPENTITY******************************" + nextLine);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if ("BankDap".equals(sheetName)) {
                            nextLine = iterator.next();
                            String dapBankGroupId = nextLine.getCell(0).toString();
                            String fapEntity = nextLine.getCell(1).toString();
                            String dapAction = nextLine.getCell(2).toString();
                            String dapModule = nextLine.getCell(3).toString();
                            String bankRole = nextLine.getCell(6).toString();
                            String dapEntity = nextLine.getCell(10).toString();
                            String dapEntityBankFunction = nextLine.getCell(7).toString();
                            String dapValue = null;
                            String operator = nextLine.getCell(14).toString();

                            if ("entity".equalsIgnoreCase(operator) && null != nextLine.getCell(12)) {
                                String[] dapvalueArr = StringUtils.split(nextLine.getCell(12).toString(), "\",\"");
                                if ("BankPaymentType".equalsIgnoreCase(dapEntity)) {
                                    dapValue =
                                            "select BANKPAYMENTTYPEKEY from bankpaymenttype where bankkey = (select bankkey from bank where bankname = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKPAYMENTTYPE.BANK.bankName\":\"",
                                                            "\",\"BANKGROUP.bankGroupId\"")
                                                    + "' " + " and bankgroupid = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKGROUP.bankGroupId\":\"",
                                                            "\",\"BANKPAYMENTTYPE.bankPaymentType")
                                                    + "') AND BANKPAYMENTTYPE = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKPAYMENTTYPE.bankPaymentType\":\"",
                                                            "\",\"BANKPAYMENTTYPE.BANK")
                                                    + "'";
                                } else if ("BankDepartment".equalsIgnoreCase(dapEntity)) {
                                    dapValue =
                                            "select bankdepartmentkey from bankdepartment where bankkey = (select bankkey from bank where bankname = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKDEPARTMENT.BANK.bankName\":\"",
                                                            "\",\"BANKDEPARTMENT.bankDepartmentName")
                                                    + "' and bankgroupid = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKDEPARTMENT.BANKGROUP.bankGroupId\":\"",
                                                            "\",\"BANKGROUP.bankGroupId")
                                                    + "')	and bankdepartmentname = '"
                                                    + StringUtils.substringBetween(nextLine.getCell(12).toString(),
                                                            "BANKDEPARTMENT.bankDepartmentName\":\"", "\"}")
                                                    + "'";
                                }
                            }
                            rowtype = nextLine.getCell(16).toString();
                            if ("New".equalsIgnoreCase(rowtype)) {
                                String s =
                                        "insert into BANKDAP (BANKDAPKEY,BANKFAPKEY,BANKDAPENTITYKEY,DAPVIEWFIELDSET,DAPVALUE,DAPVALUETYPE,OPERATOR,VERSION, WHENMODIFIED)"
                                                + " values ((" + getBankDapKey() + "),("
                                                + getBankFapKey(fapEntity, dapAction, dapModule, dapBankGroupId,
                                                        bankRole)
                                                + "and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                                + nextLine.getCell(5).toString() + "') ),("
                                                + getBankDapEntityKey(dapEntityBankFunction,
                                                        nextLine.getCell(8).toString(), nextLine.getCell(9).toString(),
                                                        bankRole, dapEntity, nextLine.getCell(11).toString(),
                                                        nextLine.getCell(0).toString())
                                                + "and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                                + nextLine.getCell(5).toString() + "'))),0,(" + dapValue + "),'"
                                                + nextLine.getCell(13).toString() + "','"
                                                + nextLine.getCell(14).toString() + "',0, sysdate);";
                                insert.get("BANKDAP").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update BANKDAP set dapValue=(" + dapValue + "),dapValueType='"
                                        + nextLine.getCell(13).toString() + "',operator='"
                                        + nextLine.getCell(14).toString() + "', dapViewFieldSet='"
                                        + nextLine.getCell(15).toString() + "' where BANKFAPKEY=("
                                        + getBankFapKey(fapEntity, dapAction, dapModule, dapBankGroupId, bankRole)
                                        + "and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                        + nextLine.getCell(5).toString() + "') ) and BANKDAPENTITYKEY=("
                                        + getBankDapEntityKey(dapEntityBankFunction, nextLine.getCell(8).toString(),
                                                nextLine.getCell(9).toString(), bankRole, dapEntity,
                                                nextLine.getCell(11).toString(), nextLine.getCell(0).toString())
                                        + "and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                        + nextLine.getCell(5).toString() + "')));";
                                insert.get("BANKDAP").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from BANKDAP where BANKFAPKEY=("
                                        + getBankFapKey(fapEntity, dapAction, dapModule, dapBankGroupId, bankRole)
                                        + " and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                        + nextLine.getCell(5).toString() + "')) and BANKDAPENTITYKEY=("
                                        + getBankDapEntityKey(dapEntityBankFunction, nextLine.getCell(8).toString(),
                                                nextLine.getCell(9).toString(), bankRole, dapEntity,
                                                nextLine.getCell(11).toString(), nextLine.getCell(0).toString())
                                        + "and br.BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                                        + nextLine.getCell(5).toString() + "' and bankgroupid = '" + nextLine.getCell(0)
                                        + "')));";
                                del.get("BANKDAP").add(new StringBuilder(s));
                            }

                        } else if ("BankFap".equals(sheetName)) {
                            nextLine = iterator.next();
                            String fapEntity = nextLine.getCell(1).toString();
                            String fapAction = nextLine.getCell(2).toString();
                            String fapModule = nextLine.getCell(3).toString();
                            String bankDep = nextLine.getCell(5).toString();
                            String bankRole = nextLine.getCell(6).toString();
                            rowtype = nextLine.getCell(8).toString();
                            if ("New".equals(rowtype)) {
                                String s =
                                        "insert into BANKFAP (BANKFAPKEY,BANKROLEKEY,BANKFUNCTIONKEY,DENYFLAG,VERSION, WHENMODIFIED) "
                                                + "values ((" + getBankFapKey() + "),("
                                                + getBankRoleKey(bankRole, bankDep) + "),("
                                                + getBankFunctionKey(fapEntity, fapAction, fapModule,
                                                        nextLine.getCell(0).toString())
                                                + "),'F',0,sysdate); ";
                                insert.get("BANKFAP").add(new StringBuilder(s));

                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from BANKFAP where BANKROLEKEY=(" + getBankRoleKey(bankRole, bankDep)
                                        + ") and BANKFUNCTIONKEY in (" + getBankFunctionKey(fapEntity, fapAction,
                                                fapModule, nextLine.getCell(0).toString())
                                        + ");";
                                del.get("BANKFAP").add(new StringBuilder(s));
                            }
                        } else if ("ChargeConfiguration".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(11).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into CHARGECONFIGURATION (CHARGECONFIGURATIONKEY,CONFIGURATIONNAME,CHARGETYPE,BANKKEY,CURRENCY,CHARGEBY,FIXEDAMOUNT,RATEPERCENT,MINIMUMAMOUNT,MAXIMUMAMOUNT,BANKGROUPID,ENDORSESTATUS,WHENMODIFIED,VERSION) "
                                                + "values ((" + getchargeConfigurationKey() + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + nextLine.getCell(9).toString() + "'),('"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(10).toString() + "'),sysdate,0); ";
                                insert.get("CHARGECONFIGURATION").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from CHARGECONFIGURATION where CONFIGURATIONNAME='"
                                        + nextLine.getCell(2).toString() + "' and CHARGETYPE='"
                                        + nextLine.getCell(3).toString() + "' and BANKKEY =("
                                        + getBankKey(nextLine.getCell(1).toString()) + "); ";
                                del.get("CHARGECONFIGURATION").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update CHARGECONFIGURATION set FIXEDAMOUNT="
                                        + nextLine.getCell(6).toString() + ",ENDORSESTATUS=('"
                                        + nextLine.getCell(10).toString() + "') where CONFIGURATIONNAME='"
                                        + nextLine.getCell(2).toString() + "' and CHARGETYPE='"
                                        + nextLine.getCell(3).toString() + "' and BANKKEY =("
                                        + getBankKey(nextLine.getCell(1).toString()) + "); ";
                                del.get("CHARGECONFIGURATION").add(new StringBuilder(s));
                            }
                        }

                        else if ("ExchangeCriterionLiteral".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(4).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(4) == null ? "" : nextLine.getCell(4).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into ExchangeCriterionLiteral (EXCHANGECRITERIONLITERALKEY, EXCHANGECRITERIONKEY, LITERALVALUE, LITERALORDER, LITERALTYPE, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select nvl(min(EXCHANGECRITERIONLITERALKEY), 0)-1 from ExchangeCriterionLiteral"
                                                + "),('" + nextLine.getCell(3).toString() // to
                                                                                                                                                                                                                                                                                                                                                   // discuss
                                                + "'),('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),0, sysdate); ";
                                insert.get("EXCHANGECRITERIONLITERAL").add(new StringBuilder(s));
                            }
                            if ("Delete".equals(rowtype)) {
                                String s = "delete from ExchangeCriterionLiteral where EXCHANGECRITERIONKEY='"
                                        + nextLine.getCell(3).toString() + "' and  LITERALVALUE='"
                                        + nextLine.getCell(0).toString() + "' and" + " LITERALORDER='"
                                        + nextLine.getCell(1).toString() + "' and " + "LITERALTYPE='"
                                        + nextLine.getCell(2).toString() + "';";

                                del.get("EXCHANGECRITERIONLITERAL").add(new StringBuilder(s));
                            }
                        }

                        else if ("ParameterService".equalsIgnoreCase(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into PARAMETERSERVICE (SERVICEKEY, BANKGROUPID, SERVICENAME, SERVICEGROUP, DISPLAYSEQ, ENDORSESTATUS, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(SERVICEKEY)-1 from PARAMETERSERVICE"
                                                + "),('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString().replace(",", "") + "'),('"
                                                + nextLine.getCell(4).toString() + "'),0, sysdate); ";
                                insert.get("PARAMETERSERVICE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update PARAMETERSERVICE set DISPLAYSEQ=('"
                                        + nextLine.getCell(3).toString().replace(",", "") + "') where SERVICENAME=('"
                                        + nextLine.getCell(1).toString() + "');";
                                insert.get("PARAMETERSERVICE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = "delete from PARAMETERSERVICE where servicename = '"
                                        + nextLine.getCell(1).toString() + "' and servicegroup = '"
                                        + nextLine.getCell(2).toString() + "' and bankgroupid = '"
                                        + nextLine.getCell(0).toString() + "';";
                                del.get("PARAMETERSERVICE").add(new StringBuilder(s));
                            }
                        }

                        else if ("ParameterServiceAttribute".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            String canOverride = ("true".equalsIgnoreCase(nextLine.getCell(4).toString())) ? "T" : "F";

                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s = "";
                                if (nextLine.getCell(3).toString().length() < 1500) {
                                    s = "insert into PARAMSERVICEATTR (ATTRIBUTEVALUEKEY, BANKGROUPID, ATTRIBUTEDEFINITIONKEY, SERVICEKEY, VALUE, CANOVERRIDE, VERSION, WHENMODIFIED) "
                                            + "values (("
                                            + "select nvl(min(ATTRIBUTEVALUEKEY),0)-1 from PARAMSERVICEATTR" + "),('"
                                            + nextLine.getCell(0).toString() + "'),("
                                            + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                    nextLine.getCell(1).toString())
                                            + "),(" + getServiceKey(nextLine.getCell(5).toString()) + "),('"
                                            + nextLine.getCell(3).toString() + "'),('" + canOverride
                                            + "'),0, sysdate); ";
                                } else {

                                    s = "insert into PARAMSERVICEATTR (ATTRIBUTEVALUEKEY, BANKGROUPID, ATTRIBUTEDEFINITIONKEY, SERVICEKEY, VALUE, CANOVERRIDE, VERSION, WHENMODIFIED) "
                                            + "values (("
                                            + "select nvl(min(ATTRIBUTEVALUEKEY),0)-1 from PARAMSERVICEATTR" + "),('"
                                            + nextLine.getCell(0).toString() + "'),("
                                            + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                    nextLine.getCell(1).toString())
                                            + "),(" + getServiceKey(nextLine.getCell(5).toString()) + "),('"
                                            + nextLine.getCell(3).toString().substring(0, 1500) + "'),('" + canOverride
                                            + "'),0, sysdate); ";
                                    for (int i = 1500; i < nextLine.getCell(3).toString().length();) {
                                        int m = i + 1500;

                                        s = s + "\n" + "update PARAMSERVICEATTR set VALUE=value || ('"
                                                + (m < nextLine.getCell(3).toString().length()
                                                        ? nextLine.getCell(3).toString().substring(i, m)
                                                        : nextLine.getCell(3).toString().substring(i,
                                                                nextLine.getCell(3).toString().length()))
                                                + "') where (SERVICEKEY = ("
                                                + getServiceKey(nextLine.getCell(5).toString())
                                                + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                                                + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                        nextLine.getCell(1).toString())
                                                + ")); ";
                                        i = m;
                                    }

                                }
                                insert.get("PARAMSERVICEATTR").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("update")) {
                                String s = "";
                                if (nextLine.getCell(3).toString().length() < 1500) {
                                    s = "update PARAMSERVICEATTR set VALUE=('" + nextLine.getCell(3).toString()
                                            + "') where (SERVICEKEY = (" + getServiceKey(nextLine.getCell(5).toString())
                                            + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                                            + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                    nextLine.getCell(1).toString())
                                            + ")); ";
                                } else {
                                    s = "update PARAMSERVICEATTR set VALUE=('"
                                            + nextLine.getCell(3).toString().substring(0, 1500)
                                            + "') where (SERVICEKEY = (" + getServiceKey(nextLine.getCell(5).toString())
                                            + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                                            + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                    nextLine.getCell(1).toString())
                                            + ")); ";
                                    for (int i = 1500; i < nextLine.getCell(3).toString().length();) {
                                        int m = i + 1500;

                                        s = s + "\n" + "update PARAMSERVICEATTR set VALUE=value || ('"
                                                + (m < nextLine.getCell(3).toString().length()
                                                        ? nextLine.getCell(3).toString().substring(i, m)
                                                        : nextLine.getCell(3).toString().substring(i,
                                                                nextLine.getCell(3).toString().length()))
                                                + "') where (SERVICEKEY = ("
                                                + getServiceKey(nextLine.getCell(5).toString())
                                                + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                                                + getAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                        nextLine.getCell(1).toString())
                                                + ")); ";
                                        i = m;
                                    }
                                }
                                insert.get("PARAMSERVICEATTR").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s =
                                        "delete from PARAMSERVICEATTR pr where servicekey = (select servicekey from PARAMETERSERVICE where servicename = '"
                                                + nextLine.getCell(1).toString() + "') and "
                                                + " pr.ATTRIBUTEDEFINITIONKEY = (select attributedefinitionkey from PARAMSERVICEATTRDEFINITION where attributename = '"
                                                + nextLine.getCell(2).toString() + "' and "
                                                + " servicekey = (select servicekey from PARAMETERSERVICE where servicename = '"
                                                + nextLine.getCell(5).toString() + "')) and pr.BANKGROUPID = '"
                                                + nextLine.getCell(0).toString() + "'; ";
                                del.get("PARAMSERVICEATTR").add(new StringBuilder(s));
                            }
                        }

                        else if ("ParameterServiceAttributeDefini".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s = "";
                                if (nextLine.getCell(4).toString().length() < 1500) {
                                    s = "insert into PARAMSERVICEATTRDEFINITION (ATTRIBUTEDEFINITIONKEY, BANKGROUPID, SERVICEKEY, ATTRIBUTENAME, DISPLAYSEQ, METADATA, VERSION, WHENMODIFIED) "
                                            + "values (("
                                            + "select min(ATTRIBUTEDEFINITIONKEY)-1 from PARAMSERVICEATTRDEFINITION"
                                            + "),('" + nextLine.getCell(0).toString() + "'),("
                                            + getServiceKey(nextLine.getCell(1).toString()) + "),('"
                                            + nextLine.getCell(2).toString() + "'),('" + nextLine.getCell(3).toString()
                                            + "'),('" + nextLine.getCell(4).toString() + "'),0, sysdate); ";
                                } else {
                                    s = "insert into PARAMSERVICEATTRDEFINITION (ATTRIBUTEDEFINITIONKEY, BANKGROUPID, SERVICEKEY, ATTRIBUTENAME, DISPLAYSEQ, METADATA, VERSION, WHENMODIFIED) "
                                            + "values (("
                                            + "select min(ATTRIBUTEDEFINITIONKEY)-1 from PARAMSERVICEATTRDEFINITION"
                                            + "),('" + nextLine.getCell(0).toString() + "'),("
                                            + getServiceKey(nextLine.getCell(1).toString()) + "),('"
                                            + nextLine.getCell(2).toString() + "'),('" + nextLine.getCell(3).toString()
                                            + "'),('" + nextLine.getCell(4).toString().substring(0, 1500)
                                            + "'),0, sysdate); ";

                                    for (int i = 1500; i < nextLine.getCell(4).toString().length();) {
                                        int m = i + 1500;

                                        s = s + "\n" + "update PARAMSERVICEATTRDEFINITION set METADATA=metadata || ('"
                                                + (m < nextLine.getCell(4).toString().length()
                                                        ? nextLine.getCell(4).toString().substring(i, m)
                                                        : nextLine.getCell(4).toString().substring(i,
                                                                nextLine.getCell(3).toString().length()))
                                                + "')  where SERVICEKEY = ("
                                                + getServiceKey(nextLine.getCell(1).toString())
                                                + ") and ATTRIBUTENAME = ('" + nextLine.getCell(2).toString() + "'); ";
                                        i = m;
                                    }
                                }

                                insert.get("PARAMSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            }
                            if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "";

                                if (nextLine.getCell(4).toString().length() < 1500) {
                                    s = "update PARAMSERVICEATTRDEFINITION set METADATA=('"
                                            + nextLine.getCell(4).toString() + "'), DISPLAYSEQ=('"
                                            + nextLine.getCell(3).toString() + "') where SERVICEKEY = ("
                                            + getServiceKey(nextLine.getCell(1).toString()) + ") and ATTRIBUTENAME = ('"
                                            + nextLine.getCell(2).toString() + "'); ";
                                } else {
                                    s = "update PARAMSERVICEATTRDEFINITION set METADATA=('"
                                            + nextLine.getCell(4).toString().substring(0, 1500) + "'), DISPLAYSEQ=('"
                                            + nextLine.getCell(3).toString() + "') where SERVICEKEY = ("
                                            + getServiceKey(nextLine.getCell(1).toString()) + ") and ATTRIBUTENAME = ('"
                                            + nextLine.getCell(2).toString() + "'); ";
                                    for (int i = 1500; i < nextLine.getCell(4).toString().length();) {
                                        int m = i + 1500;

                                        s = s + "\n" + "update PARAMSERVICEATTRDEFINITION set METADATA=metadata || ('"
                                                + (m < nextLine.getCell(4).toString().length()
                                                        ? nextLine.getCell(4).toString().substring(i, m)
                                                        : nextLine.getCell(4).toString().substring(i,
                                                                nextLine.getCell(4).toString().length()))
                                                + "')  where SERVICEKEY = ("
                                                + getServiceKey(nextLine.getCell(1).toString())
                                                + ") and ATTRIBUTENAME = ('" + nextLine.getCell(2).toString() + "'); ";
                                        i = m;
                                    }

                                }

                                insert.get("PARAMSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s =
                                        "delete from PARAMSERVICEATTRDEFINITION where servicekey = (select servicekey from PARAMETERSERVICE where servicename = '"
                                                + nextLine.getCell(1).toString() + "') " + " and ATTRIBUTENAME = '"
                                                + nextLine.getCell(2).toString() + "' and bankgroupid = '"
                                                + nextLine.getCell(0).toString() + "'; ";
                                del.get("PARAMSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            }
                        } else if ("RoutingCriterion".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(4).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s = "insert into ROUTINGCRITERION (EXCHANGECRITERIONKEY, ROUTINGRULEKEY) "
                                        + "values (('" + nextLine.getCell(1).getStringCellValue() + "') , ("
                                        + getRoutingRuleKey(nextLine.getCell(2).getStringCellValue(),
                                                nextLine.getCell(3).getStringCellValue())
                                        + ") );";
                                insert.get("ROUTINGCRITERION").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from ROUTINGCRITERION " + "where EXCHANGECRITERIONKEY='"
                                        + nextLine.getCell(1).getStringCellValue() + "' and " + "ROUTINGRULEKEY=("
                                        + getRoutingRuleKey(nextLine.getCell(2).getStringCellValue(),
                                                nextLine.getCell(3).getStringCellValue())
                                        + ");";
                                del.get("ROUTINGCRITERION").add(new StringBuilder(s));
                            }

                        }

                        else if ("RoutingRule".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(14).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into ROUTINGRULE (ROUTINGRULEKEY, BANKGROUPID, BANKKEY, ROUTINGRULEDISPLAYID, ORDEROFUSAGE, RULEMATCHIDENTIFIER, ENDORSESTATUS, ENRICHROUTINGPATH, EXCHANGECONDITIONKEY, PARENTROUTINGRULEKEY, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(ROUTINGRULEKEY)-1 from ROUTINGRULE"
                                                + ") , ('" + nextLine.getCell(0).getStringCellValue() + "') , ("
                                                + getBankKey(nextLine.getCell(1).getStringCellValue()) + ") , ('"
                                                + nextLine.getCell(2).getStringCellValue() + "') , ('"
                                                + nextLine.getCell(3).getStringCellValue() + "') , ('"
                                                + nextLine.getCell(4).getStringCellValue() + "') , ('"
                                                + nextLine.getCell(5).getStringCellValue() + "') , ('"
                                                + nextLine.getCell(6).getStringCellValue() + "') , ("
                                                + getExchangeConditionKey(nextLine.getCell(10).getStringCellValue(),
                                                        nextLine.getCell(11).getStringCellValue(),
                                                        nextLine.getCell(9).getStringCellValue())
                                                + ") , ("
                                                + getParentRoutingRuleKey(nextLine.getCell(12).getStringCellValue(),
                                                        nextLine.getCell(13).getStringCellValue())
                                                + "), 0, sysdate );";
                                insert.get("ROUTINGRULE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update ROUTINGRULE set EXCHANGECONDITIONKEY=("
                                        + getExchangeConditionKey(nextLine.getCell(10).getStringCellValue(),
                                                nextLine.getCell(11).getStringCellValue(),
                                                nextLine.getCell(9).getStringCellValue())
                                        + "),ORDEROFUSAGE=('" + nextLine.getCell(3).toString()
                                        + "'), RULEMATCHIDENTIFIER= ('" + nextLine.getCell(4).getStringCellValue()
                                        + "') where ROUTINGRULEDISPLAYID='" + nextLine.getCell(2).getStringCellValue()
                                        + "' and BANKKEY= (" + getBankKey(nextLine.getCell(1).getStringCellValue())
                                        + "); ";
                                insert.get("ROUTINGRULE").add(new StringBuilder(s));

                            } else if (rowtype.toLowerCase().contains("delete")) {
                                String s = "delete from routingrule where ROUTINGRULEDISPLAYID='"
                                        + nextLine.getCell(2).getStringCellValue() + "' and BANKKEY= ("
                                        + getBankKey(nextLine.getCell(1).getStringCellValue()) + "); ";
                                del.get("ROUTINGRULE").add(new StringBuilder(s));
                            }

                        }

                        else if ("ProcessingAgreementService".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(7).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into PROCAGREEMENTSERVICE (AGREEMENTSERVICEKEY, BANKGROUPID, AGREEMENTKEY, SERVICEKEY, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select nvl(min(AGREEMENTSERVICEKEY),0)-1 from PROCAGREEMENTSERVICE"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getAgreementKeyWithDepartment(nextLine.getCell(5).toString(),
                                                        nextLine.getCell(4).toString(), nextLine.getCell(3).toString(),
                                                        nextLine.getCell(2).toString(), nextLine.getCell(1).toString())
                                                + "),(" + getProcServiceKey(nextLine.getCell(6).toString())
                                                + "),0, sysdate); ";
                                insert.get("PROCAGREEMENTSERVICE").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from PROCAGREEMENTSERVICE where AGREEMENTKEY=("
                                        + getAgreementKeyWithDepartment(nextLine.getCell(5).toString(),
                                                nextLine.getCell(4).toString(), nextLine.getCell(3).toString(),
                                                nextLine.getCell(2).toString(), nextLine.getCell(1).toString())
                                        + ") and SERVICEKEY=(" + getProcServiceKey(nextLine.getCell(6).toString())
                                        + ");";
                                del.get("PROCAGREEMENTSERVICE").add(new StringBuilder(s));
                            }
                        }

                        else if ("ProcessingAgreementServiceAttri".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            String canOverride = ("true".equalsIgnoreCase(nextLine.getCell(10).toString())) ? "T" : "F";
                            rowtype = nextLine.getCell(11).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into PROCAGREEMENTSERVICEATTR (ATTRIBUTEVALUEKEY, BANKGROUPID, AGREEMENTSERVICEKEY, ATTRIBUTEDEFINITIONKEY, VALUE, CANOVERRIDE, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select nvl(min(ATTRIBUTEVALUEKEY),0)-1 from PROCAGREEMENTSERVICEATTR"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getAgreementServiceKeyWithDepartment(nextLine.getCell(7).toString(),
                                                        nextLine.getCell(6).toString(), nextLine.getCell(8).toString(),
                                                        nextLine.getCell(5).toString(), nextLine.getCell(4).toString(),
                                                        nextLine.getCell(3).toString())
                                                + "),("
                                                + getProcServiceAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                        nextLine.getCell(1).toString())
                                                + "),('" + nextLine.getCell(9).toString() + "'),('" + canOverride
                                                + "'),0, sysdate); ";

                                insert.get("PROCAGREEMENTSERVICEATTR").add(new StringBuilder(s));
                            } else if ("Delete".equals(rowtype)) {
                                String s = "delete from PROCAGREEMENTSERVICEATTR where AGREEMENTSERVICEKEY=("
                                        + getAgreementServiceKeyWithDepartment(nextLine.getCell(7).toString(),
                                                nextLine.getCell(6).toString(), nextLine.getCell(8).toString(),
                                                nextLine.getCell(5).toString(), nextLine.getCell(4).toString(),
                                                nextLine.getCell(3).toString())
                                        + ") and ATTRIBUTEDEFINITIONKEY=(" + getProcServiceAttributeDefinitionKey(
                                                nextLine.getCell(2).toString(), nextLine.getCell(1).toString())
                                        + ") ;";
                                del.get("PROCAGREEMENTSERVICEATTR").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update PROCAGREEMENTSERVICEATTR set VALUE='"
                                        + nextLine.getCell(9).toString() + "' where AGREEMENTSERVICEKEY=("
                                        + getAgreementServiceKeyWithDepartment(nextLine.getCell(7).toString(),
                                                nextLine.getCell(6).toString(), nextLine.getCell(8).toString(),
                                                nextLine.getCell(5).toString(), nextLine.getCell(4).toString(),
                                                nextLine.getCell(3).toString())
                                        + ") and ATTRIBUTEDEFINITIONKEY=(" + getProcServiceAttributeDefinitionKey(
                                                nextLine.getCell(2).toString(), nextLine.getCell(1).toString())
                                        + ");";
                                insert.get("PROCAGREEMENTSERVICEATTR").add(new StringBuilder(s));
                            }
                        }

                        else if ("SecurityAuthentication".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(11) == null ? "" : nextLine.getCell(11).getStringCellValue();
                            String resetPassword =
                                    ("true".equalsIgnoreCase(nextLine.getCell(5).toString())) ? "T" : "F";
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into SECAUTHENTICATION (SECAUTHENTICATIONKEY, PASSWORDNUMBER, LASTLOGINTIME, PREVLOGINTIME, LASTSESSIONID, NUMBEROFRETRIES, MUSTRESETPASSWORD, LASTPASSWORDCHANGEDATE, PASSWORDEXPIRATIONPERIOD, LASTFAILEDLOGINTIME, BANDELAY, PERSONKEY, VERSION, WHENMODIFIED) "
                                                + " values (("
                                                + "select min(SECAUTHENTICATIONKEY)-1 from SECAUTHENTICATION" + "), ('"
                                                + nextLine.getCell(0) + "'), ('" + nextLine.getCell(1) + "'), ('"
                                                + nextLine.getCell(2) + "'), ('" + nextLine.getCell(3) + "'), ('"
                                                + nextLine.getCell(4) + "'), ('" + resetPassword + "'), (TO_DATE('"
                                                + nextLine.getCell(6) + "', 'DD-MM-YYYY hh24:mi:ss')), ('"
                                                + nextLine.getCell(7) + "'), ('" + nextLine.getCell(8) + "'), ('"
                                                + nextLine.getCell(9) + "'), ("
                                                + getPersonKey(nextLine.getCell(10).toString()) + "), 0, sysdate);";
                                insert.get("SECAUTHENTICATION").add(new StringBuilder(s));
                            }

                        }

                        else if ("SettlementTimeline".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(5).getStringCellValue();
                            String marketDefault =
                                    ("true".equalsIgnoreCase(nextLine.getCell(4).toString())) ? "T" : "F";
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into SETTLEMENTTIMELINE (SETTLEMENTTIMELINEKEY, BANKGROUPID, BANKKEY, NAME, ENDORSESTATUS, MARKETDEFAULT, VERSION, WHENMODIFIED) "
                                                + " values (("
                                                + "select min(SETTLEMENTTIMELINEKEY)-1 from SETTLEMENTTIMELINE"
                                                + "), ('" + nextLine.getCell(0) + "'), ("
                                                + getBankKey(nextLine.getCell(1).toString()) + "), ('"
                                                + nextLine.getCell(2) + "'), ('" + nextLine.getCell(3) + "'), ('"
                                                + marketDefault + "'), 0, sysdate);";
                                insert.get("SETTLEMENTTIMELINE").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                System.out.println("UPDATE SETTLEMENTTIMELINE SET MARKETDEFAULT='"
                                        + getCharValue(nextLine.getCell(4).toString()) + "' WHERE NAME='"
                                        + nextLine.getCell(2) + "' AND BANKKEY=("
                                        + getBankKey(nextLine.getCell(1).toString()) + ") AND BANKGROUPID='"
                                        + nextLine.getCell(0) + "';");
                            }
                        }

                        else if ("SettlementTimelineDetail".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(8).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into SETTLEMENTTIMELINEDETAIL (SETTLEMENTTIMELINEDETAILKEY, CURRENCY, CUTOFFTIME, CUTOFFTIMEZONE, OFFSET, OFFSETTYPE, SETTLEMENTTIMELINEKEY, VERSION, WHENMODIFIED) "
                                                + " values (("
                                                + "select min(SETTLEMENTTIMELINEDETAILKEY)-1 from SETTLEMENTTIMELINEDETAIL"
                                                + "), ('" + nextLine.getCell(3) + "'), ("
                                                + getTimeInMillis(nextLine.getCell(4).toString()) + "), ('"
                                                + nextLine.getCell(5) + "'), ('" + nextLine.getCell(6) + "'), ('"
                                                + nextLine.getCell(7) + "'), ("
                                                + getSettlementTimelineKey(nextLine.getCell(1).toString(),
                                                        nextLine.getCell(2).toString())
                                                + "), 0, sysdate);";
                                insert.get("SETTLEMENTTIMELINEDETAIL").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update SETTLEMENTTIMELINEDETAIL set CUTOFFTIME=('"
                                        + getTimeInMillis(nextLine.getCell(4).toString()) + "'), OFFSET='"
                                        + nextLine.getCell(6) + "',OFFSETTYPE='" + nextLine.getCell(7)
                                        + "' where SETTLEMENTTIMELINEKEY=("
                                        + getSettlementTimelineKey(nextLine.getCell(1).toString(),
                                                nextLine.getCell(2).toString())
                                        + ") and currency='" + nextLine.getCell(3).toString() + "';";
                                insert.get("SETTLEMENTTIMELINEDETAIL").add(new StringBuilder(s));
                            }
                        }

                        else if ("ProcessingProductServiceAttribu".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in ProcessingProductServiceAttribu to generate, Pls. check the file");
                                break;
                            }

                            rowtype = nextLine.getCell(8).getStringCellValue();
                            if ("New".equals(rowtype) || StringUtils.isBlank(rowtype)) {
                                String s =
                                        "insert into PROCPRODUCTSERVICEATTR (ATTRIBUTEVALUEKEY, BANKGROUPID, ATTRIBUTEDEFINITIONKEY, PRODUCTSERVICEKEY, VALUE, CANOVERRIDE, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select nvl(min(ATTRIBUTEVALUEKEY),0)-1 from PROCPRODUCTSERVICEATTR"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getProcServiceAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                        nextLine.getCell(1).toString())
                                                + "),("
                                                + getProductServiceKey(nextLine.getCell(3).toString(),
                                                        nextLine.getCell(4).toString(), nextLine.getCell(5).toString())
                                                + "),('" + nextLine.getCell(6).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(7).toString()) + "'),0, sysdate); ";
                                insert.get("PROCPRODUCTSERVICEATTR").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("DELETE FROM PROCPRODUCTSERVICEATTR where ATTRIBUTEDEFINITIONKEY=("
                                        + getProcServiceAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                nextLine.getCell(1).toString())
                                        + ") AND PRODUCTSERVICEKEY = ("
                                        + getProductServiceKey(nextLine.getCell(3).toString(),
                                                nextLine.getCell(4).toString(), nextLine.getCell(5).toString())
                                        + ") ;");
                                del.get("PROCPRODUCTSERVICEATTR").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s =
                                        "update PROCPRODUCTSERVICEATTR set VALUE = ('" + nextLine.getCell(6).toString()
                                                + "') where ATTRIBUTEDEFINITIONKEY = ("
                                                + getProcServiceAttributeDefinitionKey(nextLine.getCell(2).toString(),
                                                        nextLine.getCell(1).toString())
                                                + ") and PRODUCTSERVICEKEY = ("
                                                + getProductServiceKey(nextLine.getCell(3).toString(),
                                                        nextLine.getCell(4).toString(), nextLine.getCell(5).toString())
                                                + ");";
                                insert.get("PROCPRODUCTSERVICEATTR").add(new StringBuilder(s));

                            }
                        } else if ("ParticipationDirectory".equalsIgnoreCase(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equalsIgnoreCase("New")) {
                                String s =
                                        "insert into PARTICIPATIONDIRECTORY (PARTICIPATIONDIRECTORYKEY,BANKGROUPID,BANKKEY,DIRECTORYNAME,EXTERNALID,ENDORSESTATUS,BANKINGDIRECTORY,VERSION, WHENMODIFIED)"
                                                + "values (("
                                                + "select nvl(min(PARTICIPATIONDIRECTORYKEY),0)-1 from PARTICIPATIONDIRECTORY"
                                                + "),'" + nextLine.getCell(0) + "',("
                                                + "select bankkey from bank where bankname = '" + nextLine.getCell(1)
                                                + "'),'" + nextLine.getCell(2) + "','" + nextLine.getCell(3) + "','"
                                                + nextLine.getCell(4) + "','"
                                                + StringUtils.substring(nextLine.getCell(5).toString(), 0, 1)
                                                + "',0, sysdate);";

                                insert.get("ParticipationDirectory").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("delete")) {
                                String s = "DELETE from PARTICIPATIONDIRECTORY where BANKGROUPID = '"
                                        + nextLine.getCell(0) + "' and BANKINGDIRECTORY = '"
                                        + StringUtils.substring(nextLine.getCell(5).toString(), 0, 1)
                                        + "' and DIRECTORYNAME = '" + nextLine.getCell(2) + "' "
                                        + " and BANKKEY = (select bankkey from bank where bankname = '"
                                        + nextLine.getCell(1) + "');";
                                del.get("ParticipationDirectory").add(new StringBuilder(s));
                            }
                        } else if ("ExchangeCondition".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            String settlCutTime = null;
                            String subSartTime = null;
                            String subStopTime = null;
                            try {
                                settlCutTime = getTimeInMillis(nextLine.getCell(20).toString());
                                subSartTime = getTimeInMillis(nextLine.getCell(42).toString());
                                subStopTime = getTimeInMillis(nextLine.getCell(43).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            rowtype = nextLine.getCell(80).getStringCellValue();
                            if (rowtype.contains("New")) {
                                String s =
                                        "INSERT INTO EXCHANGECONDITION (EXCHANGECONDITIONKEY,PARTICIPANTKEY,DELEGATECLEARINGPARTICIPANTKEY,USECLEARINGDELEGATE,"
                                                + "BANKGROUPID,SUBMISSIONCALENDARKEY,PARTICIPATIONDIRECTORYKEY,EXCHANGECONDITIONEXTERNALID,EXCHANGECONDITIONNAME,INCOMING,ENDORSESTATUS,DETAILS,DELETED,VERSION,WHENMODIFIED,SETTLEMENTCUTOFFTIMEIDX,AVAILABILITYIDX,EXCHANGEFORMATIDX,SUBMISSIONSTARTTIMEIDX,SUBMISSIONSTOPTIMEIDX,SUBMISSIONTZIDX,STATUSREPORTEXPECTEDIDX) "
                                                + "VALUES (("
                                                + "select min(EXCHANGECONDITIONKEY)-1 from EXCHANGECONDITION" + "),("
                                                + getParticipantKeyWithDepartment(nextLine.getCell(1).toString(),
                                                        nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                        nextLine.getCell(4).toString())
                                                + "),("
                                                + getParticipantKeyWithDepartment(nextLine.getCell(12).toString(),
                                                        nextLine.getCell(13).toString(),
                                                        nextLine.getCell(14).toString(),
                                                        nextLine.getCell(15).toString())
                                                + "),'" + getCharValue(nextLine.getCell(7).toString()) + "','"
                                                + nextLine.getCell(0) + "',("
                                                + getBusinessCalendarKey(nextLine.getCell(11).toString()) + "),("
                                                + getParticipantDirectory(nextLine.getCell(16).toString(),
                                                        nextLine.getCell(17).toString(), nextLine.getCell(0).toString())
                                                + "),'" + nextLine.getCell(5) + "','" + nextLine.getCell(6) + "','"
                                                + getCharValue(nextLine.getCell(10).toString()) + "','"
                                                + nextLine.getCell(8) + "','','"
                                                + getCharValue(nextLine.getCell(9).toString()) + "','0',sysdate,'"
                                                + settlCutTime + "','" + getCharValue(nextLine.getCell(32).toString())
                                                + "','" + nextLine.getCell(35) + "','" + subSartTime + "','"
                                                + subStopTime + "','" + nextLine.getCell(41) + "','"
                                                + nextLine.getCell(59) + "');";
                                s = s + "\n" + "update exchangecondition set details="
                                        + "'<?xml version=\"1.0\" encoding=\"UTF-8\"?><ExchgCndtn><Sttlm><Out><CoT><Tm>"
                                        + settlCutTime + "</Tm></CoT></Out></Sttlm><Bsnss><Schm><Nm>"
                                        + nextLine.getCell(21) + "</Nm><DtrmntnId>" + nextLine.getCell(22)
                                        + "</DtrmntnId>" + "</Schm><chkUnqMsgDpl>" + nextLine.getCell(25)
                                        + "</chkUnqMsgDpl><chanlType>" + nextLine.getCell(29)
                                        + "</chanlType></Bsnss><Subm>" + "<DlvryHlr>" + "<Initfor>"
                                        + nextLine.getCell(36) + "</Initfor>" + "<Id>" + nextLine.getCell(37) + "</Id>"
                                        + "</DlvryHlr>" + "<Avlblty>" + nextLine.getCell(32) + "</Avlblty>"
                                        + "<TrsprtRcptReq>" + nextLine.getCell(40) + "</TrsprtRcptReq><StrtTm>"
                                        + subSartTime + "</StrtTm><StpTm>" + subStopTime + "</StpTm><Tz>"
                                        + nextLine.getCell(44) + "</Tz><ImmdtSubm>" + nextLine.getCell(48)
                                        + "</ImmdtSubm><volatileTrptDtFlg>" + nextLine.getCell(48)//done till here
                                        + "</volatileTrptDtFlg>" + "</Subm><PstSbmsn><SttlmRcptExpctd>"
                                        + nextLine.getCell(62) + "</SttlmRcptExpctd>" + "<VldtnRcptExpctd>"
                                        + nextLine.getCell(61) + "</VldtnRcptExpctd><DlvryRcptExpctd>"
                                        + nextLine.getCell(58) + "</DlvryRcptExpctd>" + "<NtwrkRcptExpctd>"
                                        + nextLine.getCell(57) + "</NtwrkRcptExpctd>" + "<GtwyRcptExpctd>"
                                        + nextLine.getCell(56) + "</GtwyRcptExpctd></PstSbmsn>" + "<Splt>" + "<Actv>"
                                        + nextLine.getCell(76) + "</Actv>" + "<ThrshhldLngth>" + nextLine.getCell(77)
                                        + "</ThrshhldLngth>" + "<OptmlSubIntchngLngth>" + nextLine.getCell(78)
                                        + "</OptmlSubIntchngLngth>" + "<MaxSubIntchngCnt>" + nextLine.getCell(79)
                                        + "</MaxSubIntchngCnt>" + "</Splt>" + "</ExchgCndtn>'"
                                        + "where EXCHANGECONDITIONNAME = ('" + nextLine.getCell(6).toString() + "');";
                                insert.get("EXCHANGECONDITION").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update EXCHANGECONDITION set DELEGATECLEARINGPARTICIPANTKEY=("
                                        + getParticipantKey(nextLine.getCell(12).toString(),
                                                nextLine.getCell(13).toString(), nextLine.getCell(14).toString(),
                                                nextLine.getCell(15).toString())
                                        + "),PARTICIPATIONDIRECTORYKEY=("
                                        + getParticipantDirectory(nextLine.getCell(16).toString(),
                                                nextLine.getCell(17).toString(), nextLine.getCell(0).toString())
                                        + ")" + ",AVAILABILITYIDX='" + getCharValue(nextLine.getCell(32).toString())
                                        + "', USECLEARINGDELEGATE='" + getCharValue(nextLine.getCell(7).toString())
                                        + "'," + " SETTLEMENTCUTOFFTIMEIDX = ('" + settlCutTime
                                        + "'), SUBMISSIONSTARTTIMEIDX = ('" + subSartTime
                                        + "'), SUBMISSIONSTOPTIMEIDX = ('" + subStopTime + "'), details="
                                        + "'<?xml version=\"1.0\" encoding=\"UTF-8\"?><ExchgCndtn><Sttlm><Out><CoT><Tm>"
                                        + settlCutTime + "</Tm></CoT></Out></Sttlm><Bsnss><Schm><Nm>"
                                        + nextLine.getCell(21) + "</Nm><DtrmntnId>" + nextLine.getCell(22)
                                        + "</DtrmntnId>" + "</Schm><chkUnqMsgDpl>" + nextLine.getCell(25)
                                        + "</chkUnqMsgDpl><chanlType>" + nextLine.getCell(29)
                                        + "</chanlType></Bsnss><Subm>" + "<DlvryHlr>" + "<Initfor>"
                                        + nextLine.getCell(36) + "</Initfor>" + "<Id>" + nextLine.getCell(37) + "</Id>"
                                        + "</DlvryHlr>" + "<Avlblty>" + nextLine.getCell(32) + "</Avlblty>"
                                        + "<TrsprtRcptReq>" + nextLine.getCell(40) + "</TrsprtRcptReq><StrtTm>"
                                        + subSartTime + "</StrtTm><StpTm>" + subStopTime + "</StpTm><Tz>"
                                        + nextLine.getCell(44) + "</Tz><ImmdtSubm>" + nextLine.getCell(48)
                                        + "</ImmdtSubm><volatileTrptDtFlg>" + nextLine.getCell(48)
                                        + "</volatileTrptDtFlg>" + "</Subm><PstSbmsn><SttlmRcptExpctd>"
                                        + nextLine.getCell(62) + "</SttlmRcptExpctd>" + "<VldtnRcptExpctd>"
                                        + nextLine.getCell(61) + "</VldtnRcptExpctd><DlvryRcptExpctd>"
                                        + nextLine.getCell(58) + "</DlvryRcptExpctd>" + "<NtwrkRcptExpctd>"
                                        + nextLine.getCell(57) + "</NtwrkRcptExpctd>" + "<GtwyRcptExpctd>"
                                        + nextLine.getCell(56) + "</GtwyRcptExpctd></PstSbmsn>" + "<Splt>" + "<Actv>"
                                        + nextLine.getCell(76) + "</Actv>" + "<ThrshhldLngth>" + nextLine.getCell(77)
                                        + "</ThrshhldLngth>" + "<OptmlSubIntchngLngth>" + nextLine.getCell(78)
                                        + "</OptmlSubIntchngLngth>" + "<MaxSubIntchngCnt>" + nextLine.getCell(79)
                                        + "</MaxSubIntchngCnt>" + "</Splt>" + "</ExchgCndtn>'"
                                        + " where EXCHANGECONDITIONNAME = ('" + nextLine.getCell(6).toString() + "');";
                                insert.get("EXCHANGECONDITION").add(new StringBuilder(s));
                            } else if (rowtype.toLowerCase().contains("delete")) {
                                String s = "delete from EXCHANGECONDITION where EXCHANGECONDITIONEXTERNALID='"
                                        + nextLine.getCell(5) + "' and EXCHANGECONDITIONNAME='" + nextLine.getCell(6)
                                        + "';";
                                del.get("EXCHANGECONDITION").add(new StringBuilder(s));
                            }
                        } else if ("ParameterProduct".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }

                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        "insert into PARAMETERPRODUCT (PRODUCTKEY, BANKGROUPID, BANKKEY, PRODUCTNAME, ENDORSESTATUS, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select nvl(min(PRODUCTKEY), 0)-1 from PARAMETERPRODUCT"
                                                + "),'" + nextLine.getCell(0) + "',("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),'"
                                                + nextLine.getCell(2) + "','" + nextLine.getCell(4)
                                                + "', 0, sysdate); ";
                                insert.get("PARAMETERPRODUCT").add(new StringBuilder(s));
                            }
                        } else if ("Participant".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(45).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        "INSERT INTO PARTICIPANT (PARTICIPANTKEY, BANKGROUPID, PARTICIPANTNAME, OWNINGBANKDEPARTMENTKEY, EXTERNALID, ADDRESSLINE1, ADDRESSLINE2, ADDRESSLINE3, USESETTLEMENTDELEGATE,"
                                                + " SETTLEMENTCALENDARKEY, REQUIREENDORSEMENT, ENDORSESTATUS, ACCOUNTINGEVENTCONSUMER, AGENTID, AVAILABILITY, ONUS, SUSPENDED, DELETED,"
                                                + " COUNTRYCODE, VERSION, WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(PARTICIPANTKEY), 0)-1 from PARTICIPANT),'"
                                                + nextLine.getCell(0) + "','" + nextLine.getCell(3) + "',("
                                                + getBankDepartmentKey(nextLine.getCell(2).toString()) + "),'"
                                                + nextLine.getCell(4) + "','" + nextLine.getCell(20) + "','"
                                                + nextLine.getCell(21) + "','" + nextLine.getCell(22) + "','"
                                                + getCharValue(nextLine.getCell(13).toString()) + "',("
                                                + getBusinessCalendarKey(nextLine.getCell(34).toString()) + "),'"
                                                + getCharValue(nextLine.getCell(33).toString()) + "','"
                                                + nextLine.getCell(7) + "','"
                                                + getCharValue(nextLine.getCell(9).toString()) + "','"
                                                + nextLine.getCell(31) + "','"
                                                + getCharValue(nextLine.getCell(6).toString()) + "','"
                                                + getCharValue(nextLine.getCell(10).toString()) + "','"
                                                + getCharValue(nextLine.getCell(5).toString()) + "','"
                                                + getCharValue(nextLine.getCell(32).toString()) + "','"
                                                + nextLine.getCell(30) + "',0, sysdate);";
                                insert.get("PARTICIPANT").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = "delete from PARTICIPANT " + "where BANKGROUPID='" + nextLine.getCell(0)
                                        + "' " + "and PARTICIPANTNAME='" + nextLine.getCell(3) + "' "
                                        + "and OWNINGBANKDEPARTMENTKEY=("
                                        + getBankDepartmentKey(nextLine.getCell(2).toString()) + " "
                                        + "and EXTERNALID='" + nextLine.getCell(4) + "'); ";

                                del.get("PARTICIPANT").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update PARTICIPANT set AGENTID='" + nextLine.getCell(31).toString()
                                        + "',SUSPENDED='" + getCharValue(nextLine.getCell(5).toString())
                                        + "',SETTLEMENTCALENDARKEY=("
                                        + getBusinessCalendarKey(nextLine.getCell(34).toString())
                                        + ") where BANKGROUPID='" + nextLine.getCell(0) + "' " + "and PARTICIPANTNAME='"
                                        + nextLine.getCell(3) + "' " + "and OWNINGBANKDEPARTMENTKEY=("
                                        + getBankDepartmentKey(nextLine.getCell(2).toString()) + " "
                                        + ") and EXTERNALID='" + nextLine.getCell(4) + "'; ";

                                insert.get("PARTICIPANT").add(new StringBuilder(s));
                            }
                        } else if ("VisibilityService".equals(sheetName)) {

                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            String displayQ = nextLine.getCell(5).toString();
                            displayQ = StringUtils.removeEnd(displayQ, ".0");
                            if (rowtype.equals("New")) {
                                String s =
                                        "insert into VisibilityService (SERVICEKEY,BANKGROUPID,SERVICENAME,SERVICEGROUP,"
                                                + "SERVICEMODULE,ENDORSESTATUS,DISPLAYSEQ,VERSION, WHENMODIFIED)"
                                                + " values ((" + "select min(SERVICEKEY)-1 from VISIBILITYSERVICE"
                                                + "),('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString().replace(",", "") + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('" + displayQ
                                                + "'),0, sysdate); ";
                                insert.get("VISIBILITYSERVICE").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = "delete from VISIBILITYSERVICE where SERVICENAME=('"
                                        + nextLine.getCell(1).toString() + "') AND DISPLAYSEQ = '" + displayQ
                                        + "' AND BANKGROUPID = ('" + nextLine.getCell(0).toString() + "');";

                                del.get("VISIBILITYSERVICE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update VISIBILITYSERVICE set DISPLAYSEQ=('" + displayQ
                                        + "') where SERVICENAME=('" + nextLine.getCell(1).toString() + "');";

                                insert.get("VISIBILITYSERVICE").add(new StringBuilder(s));
                            }
                        } else if ("VISSERVICEATTRDEFINITION".equals(sheetName)
                                || "VisibilityServiceAttributeDefin".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            String displayQ = StringUtils.isBlank(nextLine.getCell(3).toString()) ? null
                                    : StringUtils.removeEnd(nextLine.getCell(3).toString(), ".0");
                            if (rowtype.equals("New")) {
                                String s =
                                        "insert into VISSERVICEATTRDEFINITION (ATTRIBUTEDEFINITIONKEY,BANKGROUPID,SERVICEKEY,"
                                                + "ATTRIBUTENAME,DISPLAYSEQ,METADATA,VERSION, WHENMODIFIED) "
                                                + " values ((select min(ATTRIBUTEDEFINITIONKEY)-1 from VISSERVICEATTRDEFINITION),('"
                                                + nextLine.getCell(0).toString() + "')"
                                                + ",(select servicekey from VisibilityService where SERVICENAME=" + "('"
                                                + nextLine.getCell(1).toString() + "') AND BANKGROUPID = " + "('"
                                                + nextLine.getCell(0) + "'))," + "('" + nextLine.getCell(2).toString()
                                                + "'),('" + displayQ + "'),('" + nextLine.getCell(4).toString()
                                                + "'),0, sysdate); ";
                                insert.get("VISSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = "delete from VISSERVICEATTRDEFINITION where SERVICEKEY=("
                                        + "select servicekey from VisibilityService where SERVICENAME=" + "('"
                                        + nextLine.getCell(1).toString() + "') AND BANKGROUPID = " + "('"
                                        + nextLine.getCell(0) + "')) AND DISPLAYSEQ = '" + displayQ + "'"
                                        + " AND ATTRIBUTENAME = '" + nextLine.getCell(2).toString()
                                        + "' AND BANKGROUPID = " + "('" + nextLine.getCell(0) + "') ;";

                                del.get("VISSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update VISSERVICEATTRDEFINITION set DISPLAYSEQ=('" + displayQ
                                        + "'),METADATA = '" + nextLine.getCell(4).toString() + "', ATTRIBUTENAME = '"
                                        + nextLine.getCell(2).toString() + "' where   SERVICEKEY=("
                                        + "select servicekey from VisibilityService where SERVICENAME=" + "('"
                                        + nextLine.getCell(1).toString() + "'));";

                                insert.get("VISSERVICEATTRDEFINITION").add(new StringBuilder(s));
                            }
                        } else if ("VisibilityServiceAttribute".equals(sheetName)
                                || "VISSERVICEATTR".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            String value = StringUtils.isBlank(nextLine.getCell(3).toString()) ? null
                                    : nextLine.getCell(3).toString().substring(0, 1);
                            String canOverride = StringUtils.isBlank(nextLine.getCell(4).toString()) ? null
                                    : nextLine.getCell(4).toString().substring(0, 1);
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        "insert into VISSERVICEATTR (ATTRIBUTEVALUEKEY,ATTRIBUTEDEFINITIONKEY,BANKGROUPID,"
                                                + "SERVICEKEY,VALUE,CANOVERRIDE,VERSION, WHENMODIFIED) "
                                                + "values ((select min(ATTRIBUTEVALUEKEY)-1 from VISSERVICEATTR),"
                                                + "(select ATTRIBUTEDEFINITIONKEY from VISSERVICEATTRDEFINITION where attributename = "
                                                + "'" + nextLine.getCell(2).toString() + "' AND "
                                                + "servicekey = (select servicekey from VisibilityService where SERVICENAME=('"
                                                + nextLine.getCell(1).toString() + "') AND BANKGROUPID = " + "('"
                                                + nextLine.getCell(0) + "'))),'" + nextLine.getCell(0).toString() + "',"
                                                + "(select servicekey from VisibilityService where SERVICENAME=" + "('"
                                                + nextLine.getCell(1).toString() + "') AND BANKGROUPID = " + "('"
                                                + nextLine.getCell(0) + "')),'" + value + "','" + canOverride
                                                + "',0, sysdate);";

                                insert.get("VISSERVICEATTR").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = "delete from VISSERVICEATTR where ATTRIBUTEDEFINITIONKEY="
                                        + "(select ATTRIBUTEDEFINITIONKEY from VISSERVICEATTRDEFINITION where attributename = "
                                        + "'" + nextLine.getCell(2).toString() + "' AND "
                                        + "servicekey = (select servicekey from VisibilityService where SERVICENAME=('"
                                        + nextLine.getCell(1).toString() + "') AND BankGroupID =('"
                                        + nextLine.getCell(0).toString() + "'))) ;";

                                del.get("VISSERVICEATTR").add(new StringBuilder(s));
                            }
                        }

                        else if ("ProcessingAgreementAccount".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(14).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(14).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        "INSERT INTO PROCAGREEMENTACCOUNT (AGREEMENTACCOUNTKEY, ACCOUNTKEY, AGREEMENTKEY, VERSION, WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(AGREEMENTACCOUNTKEY), 0)-1 from PROCAGREEMENTACCOUNT),("
                                                + getAccountKey(nextLine.getCell(7).toString(),
                                                        nextLine.getCell(8).toString(), nextLine.getCell(9).toString(),
                                                        nextLine.getCell(0).toString(), nextLine.getCell(10).toString(),
                                                        nextLine.getCell(11).toString())
                                                + "),("
                                                + getAgreementKey(nextLine.getCell(1).toString(),
                                                        nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                        nextLine.getCell(4).toString(), nextLine.getCell(5).toString())
                                                + "),0, sysdate);";
                                insert.get("PROCAGREEMENTACCOUNT").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from PROCAGREEMENTACCOUNT where ACCOUNTKEY=("
                                        + getAccountKey(nextLine.getCell(7).toString(), nextLine.getCell(8).toString(),
                                                nextLine.getCell(9).toString(), nextLine.getCell(0).toString(),
                                                nextLine.getCell(10).toString(), nextLine.getCell(11).toString())
                                        + ") and AGREEMENTKEY=("
                                        + getAgreementKey(nextLine.getCell(1).toString(),
                                                nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                nextLine.getCell(4).toString(), nextLine.getCell(5).toString())
                                        + ");");
                                del.get("PROCAGREEMENTACCOUNT").add(new StringBuilder(s));
                            }
                        } else if ("BankDepartment".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in BANKDEPARTMENT to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(24).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO BANKDEPARTMENT (BANKDEPARTMENTKEY,BANKGROUPID,BANKKEY,BANKDEPARTMENTNAME,LOCALE,ENDORSESTATUS,DELETED,BANKINGISOLATIONREQUIRED,VERSION, WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(BANKDEPARTMENTKEY), 0)-1 from BANKDEPARTMENT),'"
                                                + nextLine.getCell(0).toString() + "',("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),'"
                                                + nextLine.getCell(2) + "','" + nextLine.getCell(3) + "','"
                                                + nextLine.getCell(20) + "','"
                                                + getCharValue(nextLine.getCell(21).toString()) + "','"
                                                + getCharValue(nextLine.getCell(22).toString()) + "',0, sysdate);");
                                insert.get("BANKDEPARTMENT").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("DELETE FROM BANKDEPARTMENT where BANKDEPARTMENTNAME='"
                                        + nextLine.getCell(2).toString() + "';");
                                del.get("BANKDEPARTMENT").add(new StringBuilder(s));
                            }
                        } else if ("BankRole".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("-- ERROR found in BANKROLE to generate, Pls. check the file --");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO BANKROLE (BANKROLEKEY, BANKGROUPID, BANKDEPARTMENTKEY, ROLENAME, ENDORSESTATUS, VERSION, WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(BANKROLEKEY), 0)-1 from BANKROLE),'"
                                                + nextLine.getCell(0) + "',("
                                                + getBankDepartmentKey(nextLine.getCell(1).toString(),
                                                        nextLine.getCell(2).toString())
                                                + "),'" + nextLine.getCell(3) + "','" + nextLine.getCell(5)
                                                + "',0, sysdate);");
                                insert.get("BANKROLE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("DELETE FROM BANKROLE where ROLENAME='" + nextLine.getCell(3).toString()
                                        + "';");
                                del.get("BANKROLE").add(new StringBuilder(s));
                            }
                        } else if ("BankPaymentType".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in BANKPAYMENTTYPE to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO BANKPAYMENTTYPE (BANKPAYMENTTYPEKEY,BANKGROUPID, BANKPAYMENTTYPE, TYPEDESCRIPTION, ENDORSESTATUS, DELETED, BANKKEY, VERSION, WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(BANKPAYMENTTYPEKEY), 0)-1 from BANKPAYMENTTYPE),'"
                                                + nextLine.getCell(0).toString() + "','"
                                                + nextLine.getCell(2).toString() + "','"
                                                + nextLine.getCell(3).toString() + "','" + nextLine.getCell(4) + "','"
                                                + getCharValue(nextLine.getCell(5).toString()) + "',("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),0, sysdate);");
                                insert.get("BANKPAYMENTTYPE").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("DELETE FROM BANKPAYMENTTYPE where BANKPAYMENTTYPE='"
                                        + nextLine.getCell(2).toString() + "' AND ENDORSESTATUS ='"
                                        + nextLine.getCell(4) + "';");
                                del.get("BANKPAYMENTTYPE").add(new StringBuilder(s));
                            }
                        } else if ("ProcessingAgreement".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        " ---- ERROR found in PROCESSINGAGREEMENT to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(14).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO PROCESSINGAGREEMENT (AGREEMENTKEY,AGREEMENTNAME,BANKGROUPID,PARTICIPANTKEY,PRODUCTKEY,DESCRIPTION,STARTDATE,ENDDATE,ENDORSESTATUS,SUSPENDED,DELETED,VERSION,WHENMODIFIED) "
                                                + "VALUES ((select nvl(min(AGREEMENTKEY), 0)-1 from PROCESSINGAGREEMENT),'"
                                                + nextLine.getCell(5) + "','" + nextLine.getCell(0) + "',("
                                                + getParticipantKeyWithDepartment(nextLine.getCell(1).toString(),
                                                        nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                        nextLine.getCell(4).toString())
                                                + "),("
                                                + getProcProductKey(nextLine.getCell(13).toString(),
                                                        nextLine.getCell(12).toString())
                                                + "),'" + nextLine.getCell(6) + "',(TO_DATE('" + nextLine.getCell(8)
                                                + "', 'DD-MM-YYYY hh24:mi:ss'))" + ",(TO_DATE('" + nextLine.getCell(9)
                                                + "', 'DD-MM-YYYY hh24:mi:ss'))" + ",'" + nextLine.getCell(7) + "','"
                                                + getCharValue(nextLine.getCell(10).toString()) + "','"
                                                + getCharValue(nextLine.getCell(11).toString()) + "','0',sysdate"
                                                + ");");
                                insert.get("PROCESSINGAGREEMENT").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from PROCESSINGAGREEMENT where AGREEMENTNAME='"
                                        + nextLine.getCell(5) + "' and PARTICIPANTKEY=("
                                        + getParticipantKey(nextLine.getCell(1).toString(),
                                                nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                nextLine.getCell(4).toString())
                                        + ");");
                                del.get("PROCESSINGAGREEMENT").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update PROCESSINGAGREEMENT set startDate= TO_DATE('" + nextLine.getCell(8)
                                        + "', 'DD-MM-YYYY hh24:mi:ss'),SUSPENDED = '"
                                        + getCharValue(nextLine.getCell(10).toString()) + "' where AGREEMENTNAME='"
                                        + nextLine.getCell(5) + "' and PARTICIPANTKEY=("
                                        + getParticipantKey(nextLine.getCell(1).toString(),
                                                nextLine.getCell(2).toString(), nextLine.getCell(3).toString(),
                                                nextLine.getCell(4).toString())
                                        + ");");
                                insert.get("PROCESSINGAGREEMENT").add(new StringBuilder(s));
                            }
                        } else if ("Account".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(29).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO ACCOUNT (ACCOUNTKEY,BANKGROUPID,BANKKEY,PARTICIPANTKEY,PARTICIPANTDEFAULT,DOMESTICACCOUNTNUMBER,"
                                                + "DOMESTICACCOUNTNUMBERTYPE,DOMESTICBANKIDENTIFIER,COUNTRYCODE,CURRENCY,CURRENCYDEFAULT,"
                                                + "ACCOUNTNAME,DISPLAYACCOUNTID,ACCOUNTTYPE,DEBITCREDITINDICATOR,ENDORSESTATUS,SUSPENDED,VERSION,"
                                                + "WHENMODIFIED,INTERNATIONALACCOUNTNUMBER,SWIFTBICCODE,PROPRIETARYACCOUNTNUMBER,BANKINTERNALACCOUNTNUMBER,"
                                                + "REPORTINGCYCLE,ACCOUNTNICKNAME,REPORTINGFORMAT,DOMESTICACCOUNTNUMBERISSUER,DOMESTICBANKIDENTIFIERTYPE,correspondentId) "
                                                + "VALUES((select nvl(min(ACCOUNTKEY), 0)-1 from ACCOUNT),'"
                                                + nextLine.getCell(0) + "',("
                                                + getBankKey(nextLine.getCell(24).toString()) + "),("
                                                + getParticipantKeyWithDepartment(nextLine.getCell(25).toString(),
                                                        nextLine.getCell(26).toString(),
                                                        nextLine.getCell(27).toString(),
                                                        nextLine.getCell(28).toString())
                                                + "),'" + getCharValue(nextLine.getCell(22).toString()) + "','"
                                                + nextLine.getCell(2) + "','" + nextLine.getCell(17) + "','"
                                                + nextLine.getCell(3) + "','" + nextLine.getCell(12) + "','"
                                                + nextLine.getCell(4) + "','"
                                                + getCharValue(nextLine.getCell(20).toString()) + "','"
                                                + nextLine.getCell(8) + "','" + nextLine.getCell(21) + "','"
                                                + nextLine.getCell(9) + "','" + nextLine.getCell(10) + "','"
                                                + nextLine.getCell(11) + "','"
                                                + getCharValue(nextLine.getCell(16).toString()) + "','0', sysdate"

                                                + ",'" + nextLine.getCell(1) + "','" + nextLine.getCell(5) + "','"
                                                + nextLine.getCell(6) + "','" + nextLine.getCell(7) + "','"
                                                + nextLine.getCell(13) + "','" + nextLine.getCell(14) + "','"
                                                + nextLine.getCell(15) + "','" + nextLine.getCell(18) + "','"
                                                + nextLine.getCell(19) + "','" + nextLine.getCell(23) + "');");
                                insert.get("ACCOUNT").add(new StringBuilder(s));
                            } else if (rowtype.equals("Delete")) {
                                String s = "delete from ACCOUNT where swiftBicCode='" + nextLine.getCell(5)
                                        + "'  and accountName='" + nextLine.getCell(8) + "' and accountType='"
                                        + nextLine.getCell(9) + "' and displayAccountId='" + nextLine.getCell(21)
                                        + "';";
                                del.get("ACCOUNT").add(new StringBuilder(s));
                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = "update ACCOUNT set correspondentId='" + nextLine.getCell(23).toString()
                                        + "',PARTICIPANTDEFAULT='" + getCharValue(nextLine.getCell(22).toString())
                                        + "', domesticAccountNumberType='" + nextLine.getCell(17)
                                        + "',displayAccountId='" + nextLine.getCell(21) + "', CURRENCYDEFAULT='"
                                        + getCharValue(nextLine.getCell(20).toString()) + "', participantkey=("
                                        + getParticipantKey(nextLine.getCell(28).toString(),
                                                nextLine.getCell(27).toString())
                                        + ") where swiftBicCode='" + nextLine.getCell(5) + "' and accountName='"
                                        + nextLine.getCell(8) + "' and accountType='" + nextLine.getCell(9) + "' ;";
                                insert.get("ACCOUNT").add(new StringBuilder(s));
                            }
                        }

                        // code added by varun yadav to handle extra tables of
                        // OPFBankgroup 2.1
                        else if ("BankGroup".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equalsIgnoreCase("New")) {
                                String s =
                                        "insert into BANKGROUP (BANKGROUPID,BANKGROUPNAME,DESCRIPTION,REQUIREENDORSEMENT,BASECURRENCYCODE,VERSION, WHENMODIFIED)"
                                                + "values ('" + nextLine.getCell(0) + "','" + nextLine.getCell(1)
                                                + "','" + nextLine.getCell(2) + "','"
                                                + StringUtils.substring(nextLine.getCell(3).toString(), 0, 1) + "','"
                                                + nextLine.getCell(4) + "',0, sysdate);";
                                insert.get("BANKGROUP").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {

                                String s = ("update BankGroup set requireEndorsement=('"
                                        + getCharValue(nextLine.getCell(3).toString()) + "') where bankGroupId=('"
                                        + nextLine.getCell(0) + "') and bankGroupName=('" + nextLine.getCell(1)
                                        + "') ; ");
                                insert.get("BANKGROUP").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {

                                String s = "delete from BANKGROUP where BANKGROUPID='" + nextLine.getCell(0)
                                        + "'  and BANKGROUPNAME='" + nextLine.getCell(1) + "' and DESCRIPTION='"
                                        + nextLine.getCell(2) + "' and BASECURRENCYCODE='" + nextLine.getCell(4) + "';";

                                del.get("BANKGROUP").add(new StringBuilder(s));

                            }
                        }

                        else if ("BusinessCalendar".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(4).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into BusinessCalendar (BUSINESSCALENDARKEY,bankGroupId, businessCalendarName, endorseStatus, currency, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select min(BUSINESSCALENDARKEY)-1 from BusinessCalendar" + "),('"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),0, sysdate); ");
                                insert.get("BUSINESSCALENDAR").add(new StringBuilder(s));

                            } else if (rowtype.contains("Delete")) {
                                String s = ("delete from BusinessCalendar where businessCalendarName='"
                                        + nextLine.getCell(1).toString() + "';");
                                del.get("BUSINESSCALENDAR").add(new StringBuilder(s));
                            }
                        }

                        else if ("CurrencyGroup".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(10).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into CurrencyGroup (CURRENCYGROUPKEY,bankGroupId, bankkey, groupName, rateType,referenceCurrency,fixingCutOff,isDirect,fixingCutOfftimeZone,endorseStatus,availableForPublishing, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "nvl((select min(CURRENCYGROUPKEY)-1 from CurrencyGroup),-1)" + "),('"
                                                + nextLine.getCell(0).toString() + "'),("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(6).toString()) + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(9).toString()) + "'),0, sysdate); ");
                                insert.get("CURRENCYGROUP").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from CURRENCYGROUP where bankGroupId='"
                                        + nextLine.getCell(0).toString() + "' and bankkey=("
                                        + getBankKey(nextLine.getCell(1).toString()) + ") and groupName=('"
                                        + nextLine.getCell(2).toString() + "') and rateType=('"
                                        + nextLine.getCell(3).toString() + "');");
                                del.get("CURRENCYGROUP").add(new StringBuilder(s));

                            }
                        }

                        else if ("CurrencyRate".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(15).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into CurrencyRate (CURRENCYRATEKEY,CURRENCYGROUPKEY,currencyCode,units,sellRate,buyRate,midRate,arbBuyRate,arbSellRate,midStopNote,sellStopNote,buyStopNote,currencyStopNote, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "nvl((select min(CURRENCYRATEKEY)-1 from CurrencyRate),-1)" + "),("
                                                + "select CURRENCYGROUPKEY from CurrencyGroup where bankgroupid='"
                                                + nextLine.getCell(0).toString() + "' and bankkey=("
                                                + getBankKey(nextLine.getCell(1).toString()) + ") and GROUPNAME='"
                                                + nextLine.getCell(2).toString() + "' and ratetype='"
                                                + nextLine.getCell(3).toString()

                                                + "'),('" + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + nextLine.getCell(9).toString() + "'),('"
                                                + nextLine.getCell(10).toString() + "'),('"
                                                + nextLine.getCell(11).toString() + "'),('"
                                                + nextLine.getCell(12).toString() + "'),('"
                                                + nextLine.getCell(13).toString() + "'),('"
                                                + nextLine.getCell(14).toString() + "'),0, sysdate); ");
                                insert.get("CURRENCYRATE").add(new StringBuilder(s));

                            }
                        }

                        else if ("ExchangeCriterion".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into ExchangeCriterion (EXCHANGECRITERIONKEY,criteriaEntity, criteriaEntityAttribute, operator, routedEntity,routedEntityAttribute, VERSION, WHENMODIFIED) "
                                                + "values ('"
                                                + (nextLine.getCell(0) == null ? "" : nextLine.getCell(0).toString())
                                                + "',('"
                                                + (nextLine.getCell(1) == null ? "" : nextLine.getCell(1).toString())
                                                + "'),('"
                                                + (nextLine.getCell(2) == null ? "" : nextLine.getCell(2).toString())
                                                + "'),('"
                                                + (nextLine.getCell(3) == null ? "" : nextLine.getCell(3).toString())
                                                + "'),('"
                                                + (nextLine.getCell(4) == null ? "" : nextLine.getCell(4).toString())
                                                + "'),('"
                                                + (nextLine.getCell(5) == null ? "" : nextLine.getCell(5).toString())
                                                + "'),0, sysdate); ");
                                insert.get("EXCHANGECRITERION").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from ExchangeCriterion " + "where EXCHANGECRITERIONKEY=" + "'"
                                        + nextLine.getCell(0).toString() + "'; ");
                                del.get("EXCHANGECRITERION").add(new StringBuilder(s));

                            }
                        }

                        else if ("HolidayDate".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(7).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into HolidayDate (HOLIDAYDATEKEY, BUSINESSCALENDARKEY,holidayDate,description, endorseStatus, holidayStartTime,holidayEndTime, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(HOLIDAYDATEKEY)-1 from HolidayDate" + "),("
                                                + "select BUSINESSCALENDARKEY from BUSINESSCALENDAR where bankgroupid='"
                                                + nextLine.getCell(0).toString() + "' and businessCalendarName = '"
                                                + nextLine.getCell(1).toString() + "'),(to_date('"
                                                + nextLine.getCell(2).toString() + "','dd-mm-yyyy hh24:mi:ss')),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + (nextLine.getCell(4).toString()) + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),0, sysdate); ");
                                insert.get("HOLIDAYDATE").add(new StringBuilder(s));

                            }
                        }

                        else if ("ProcessingProduct".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in ProcessingProduct to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(8).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO PROCESSINGPRODUCT (PRODUCTKEY,bankGroupId,bankkey, productName, description, endorseStatus, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select nvl(min(PRODUCTKEY),0)-1 from ProcessingProduct"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),0, sysdate); ");
                                insert.get("PROCESSINGPRODUCT").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("DELETE FROM PROCESSINGPRODUCT where PRODUCTNAME='"
                                        + nextLine.getCell(2).toString() + "' AND ENDORSESTATUS ='"
                                        + nextLine.getCell(4) + "';");
                                del.get("PROCESSINGPRODUCT").add(new StringBuilder(s));

                            }
                        }

                        else if ("ProcessingService".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in ProcessingService to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into ProcessingService (SERVICEKEY,bankGroupId,serviceName, serviceGroup, displaySeq, endorseStatus, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select nvl(min(SERVICEKEY),0)-1 from ProcessingService"
                                                + "),('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString().replace(",", "") + "'),('"
                                                + nextLine.getCell(4).toString() + "'),0, sysdate); ");
                                insert.get("PROCESSINGSERVICE").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s = ("delete from ProcessingService where SERVICENAME = '"
                                        + nextLine.getCell(1).toString() + "' and SERVICEGROUP = '"
                                        + nextLine.getCell(2).toString() + "' and BANKGROUPID = '"
                                        + nextLine.getCell(0).toString() + "';");
                                del.get("PROCESSINGSERVICE").add(new StringBuilder(s));

                            }
                        }

                        else if ("RateTypeDeterminationRule".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in RateTypeDeterminationRule to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(18).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into RateTypeDeterminationRule (RATETYPEDETERMINATIONRULEKEY,bankGroupId,bankkey, orderOfUsage, displayIdentifier, rateTypeDeterminationRuleGroup,rateType,source,targetEntity,entityClassification,priority,"
                                                + "minAmount,maxAmount,fxContractReference,forexCheckType,onUs,fixingRequired,outbound,endorseStatus,VERSION, WHENMODIFIED) "
                                                + "values (nvl(("
                                                + "select min(RATETYPEDETERMINATIONRULEKEY)-1 from RateTypeDeterminationRule),-1"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + nextLine.getCell(9).toString() + "'),('"
                                                + nextLine.getCell(10).toString().replace(",", "") + "'),('"
                                                + nextLine.getCell(11).toString().replace(",", "") + "'),('"
                                                + getCharValue(nextLine.getCell(12).toString()) + "'),('"
                                                + nextLine.getCell(13).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(14).toString()) + "'),('"
                                                + getCharValue(nextLine.getCell(15).toString()) + "'),('"
                                                + getCharValue(nextLine.getCell(16).toString()) + "'),('"
                                                + nextLine.getCell(17).toString() + "'),0, sysdate); ");
                                insert.get("RATETYPEDETERMINATIONRULE").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update RateTypeDeterminationRule " + "set forexCheckType='"
                                        + nextLine.getCell(13).toString() + "', ratetype='"
                                        + nextLine.getCell(5).toString() + "'" + ", outbound='"
                                        + getCharValue(nextLine.getCell(16).toString()) + "', targetEntity='"
                                        + nextLine.getCell(7).toString() + "'," + " source='"
                                        + nextLine.getCell(6).toString() + "', minAmount='"
                                        + nextLine.getCell(10).toString().replace(",", "") + "', entityClassification='"
                                        + nextLine.getCell(8).toString() + "', maxAmount='"
                                        + nextLine.getCell(11).toString().replace(",", "") + "', onUs='"
                                        + getCharValue(nextLine.getCell(14).toString()) + "'"
                                        + " where displayIdentifier='" + nextLine.getCell(3).toString() + "'; ");
                                insert.get("RATETYPEDETERMINATIONRULE").add(new StringBuilder(s));

                            } else if (rowtype.contains("Delete")) {
                                String s = ("delete from RateTypeDeterminationRule where displayIdentifier='"
                                        + nextLine.getCell(3).toString() + "'; ");
                                del.get("RATETYPEDETERMINATIONRULE").add(new StringBuilder(s));

                            }
                        }

                        else if ("RiskFilter".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in RiskFilter to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(7).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into RiskFilter (RISKFILTERKEY,bankGroupId,bankkey, riskFilterDisplayId, riskFilterType, endorseStatus,outcome,message, VERSION, WHENMODIFIED) "
                                                + "values (nvl((" + "select min(RISKFILTERKEY)-1 from RiskFilter),-1"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),0, sysdate); ");
                                insert.get("RISKFILTER").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update RiskFilter set endorseStatus=('" + nextLine.getCell(4).toString()
                                        + "') where riskFilterDisplayId=('" + nextLine.getCell(2).toString() + "');");
                                insert.get("RISKFILTER").add(new StringBuilder(s));
                            }
                        }

                        else if ("RiskFilterCriterion".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out
                                        .println("ERROR found in RiskFilterCriterion to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(7).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into RiskFilterCriterion (RISKFILTERKEY,RISKFILTERCRITERIONKEY, criterionCategory, operator, criterionOrder, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select RISKFILTERKEY from RiskFilter where bankGroupId='"
                                                + nextLine.getCell(0).toString() + "'and riskFilterDisplayId='"
                                                + nextLine.getCell(6).toString() + "' and  bankkey=("
                                                + getBankKey(nextLine.getCell(5).toString()) + ")),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),0, sysdate); ");
                                insert.get("RISKFILTERCRITERION").add(new StringBuilder(s));

                            }
                        }

                        else if ("RiskFilterCriterionLiteral".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in RiskFilterCriterionLiteral to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(3) == null ? "" : nextLine.getCell(3).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into RiskFilterCriterionLiteral (RISKFILTERCRITERIONLITERALKEY,literalValue,filteredLiteralValue, RISKFILTERCRITERIONKEY, VERSION, WHENMODIFIED) "
                                                + "values (nvl(("
                                                + "select min(RISKFILTERCRITERIONLITERALKEY)-1 from RiskFilterCriterionLiteral),-1"
                                                + "),('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),0, sysdate); ");
                                insert.get("RISKFILTERCRITERIONLITERAL").add(new StringBuilder(s));

                            }
                        } else if ("SSI".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in SSI to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(37).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into SSI (ssikey,bankGroupId,publishingSSIAgentBIC,publishingSSIAgentName, "
                                                + "publishingSSIAgentAccount,currency,agentBIC,agentAccount,viaAgentBIC,viaAgentAccount,validityFromDate,validityToDate,pubSSIAgentNationalId,"
                                                + "pubSSIAgentNationalIdType,pubSSIAgentAccountIdType,pubSSIAgentAccountCurrency,marketAreaIndicator,agentName,agentAccountIdType,"
                                                + "agentAccountCurrency,viaAgentName,viaAgentAccountIdType,viaAgentAccountCurrency,endorseStatus,deleted,recordKey,groupKey,ownerKey,"
                                                + "preferredCorrespondent,pubSSIAgentCity,pubSSIAgentCps,pubSSIAgentCountryName,pubSSIAgentCountryCode,source,uploadProtection,fieldA,"
                                                + "fieldB, VERSION, WHENMODIFIED) " + "values (nvl(("
                                                + "select min(ssikey)-1 from SSI),-1" + "),('"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),(to_date('"
                                                + nextLine.getCell(9).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss')),(to_date('"
                                                + nextLine.getCell(10).toString() + "','dd-mm-yyyy hh24:mi:ss')),('"
                                                + nextLine.getCell(11).toString() + "'),('"
                                                + nextLine.getCell(12).toString() + "'),('"
                                                + nextLine.getCell(13).toString() + "'),('"
                                                + nextLine.getCell(14).toString() + "'),('"
                                                + nextLine.getCell(15).toString() + "'),('"
                                                + nextLine.getCell(16).toString() + "'),('"
                                                + nextLine.getCell(17).toString() + "'),('"
                                                + nextLine.getCell(18).toString() + "'),('"
                                                + nextLine.getCell(19).toString() + "'),('"
                                                + nextLine.getCell(20).toString() + "'),('"
                                                + nextLine.getCell(21).toString() + "'),('"
                                                + nextLine.getCell(22).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(23).toString()) + "'),('"
                                                + nextLine.getCell(24).toString() + "'),('"
                                                + nextLine.getCell(25).toString() + "'),('"
                                                + nextLine.getCell(26).toString() + "'),('"
                                                + nextLine.getCell(27).toString() + "'),('"
                                                + nextLine.getCell(28).toString() + "'),('"
                                                + nextLine.getCell(29).toString() + "'),('"
                                                + nextLine.getCell(30).toString() + "'),('"
                                                + nextLine.getCell(31).toString() + "'),('"
                                                + nextLine.getCell(32).toString() + "'),('"
                                                + nextLine.getCell(33).toString() + "'),('"
                                                + nextLine.getCell(34).toString() + "'),('"
                                                + nextLine.getCell(35).toString() + "'),0, sysdate); ");
                                insert.get("SSI").add(new StringBuilder(s));

                            }
                        }

                        else if ("SSIBank".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(14).getStringCellValue();
                            if (rowtype.equals("New")) {// COLUMNS IMPROPER
                                String s = ("insert into SSIBank (SSIBANKKEY,SSIKEY,BANKKEY, VERSION, WHENMODIFIED) "
                                        + "values (nvl((" + "select min(SSIBANKKEY)-1 from SSIBank),-1" + "),("
                                        + "select ssikey from ssi where publishingSSIAgentBIC='"
                                        + nextLine.getCell(1).toString() + "' and (publishingSSIAgentName='"
                                        + nextLine.getCell(2).toString()
                                        + "' OR publishingSSIAgentName IS NULL) and (publishingSSIAgentAccount='"
                                        + nextLine.getCell(3).toString()
                                        + "' OR publishingSSIAgentAccount IS NULL) and currency='"
                                        + nextLine.getCell(4).toString() + "' and (agentBIC='"
                                        + nextLine.getCell(5).toString() + "' OR agentBIC IS NULL)  and (agentAccount='"
                                        + nextLine.getCell(6).toString()
                                        + "' OR agentAccount IS NULL)  and (viaAgentBIC='"
                                        + nextLine.getCell(7).toString()
                                        + "' OR viaAgentBIC IS NULL)  and (viaAgentAccount='"
                                        + nextLine.getCell(8).toString()
                                        + "' OR viaAgentAccount IS NULL) and (pubSSIAgentNationalId='"
                                        + (nextLine.getCell(11) == null ? ""
                                                : nextLine.getCell(11).getStringCellValue())
                                        + "' OR pubSSIAgentNationalId IS NULL)  and (pubSSIAgentNationalIdType='"
                                        + (nextLine.getCell(12) == null ? ""
                                                : nextLine.getCell(12).getStringCellValue())
                                        + "' OR pubSSIAgentNationalIdType IS NULL) ),("
                                        + getBankKey(nextLine.getCell(13).toString()) + "),0, sysdate); ");
                                insert.get("SSIBANK").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s =
                                        ("delete from SSIBank where SSIKEY=(select ssikey from ssi where publishingSSIAgentBIC='"
                                                + nextLine.getCell(1).toString() + "' and currency='"
                                                + nextLine.getCell(4).toString() + "' and (agentBIC='"
                                                + nextLine.getCell(5).toString()
                                                + "' OR agentBIC IS NULL)  and (agentAccount='"
                                                + nextLine.getCell(6).toString()
                                                + "' OR agentAccount IS NULL)  and (viaAgentBIC='"
                                                + nextLine.getCell(7).toString()
                                                + "' OR viaAgentBIC IS NULL) ) and BANKKEY=("
                                                + getBankKey(nextLine.getCell(13).toString()) + "); ");
                                del.get("SSIBANK").add(new StringBuilder(s));

                            }
                        }

                        else if ("ProcessingProductService".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in ProcessingProductService to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(4).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO PROCPRODUCTSERVICE (PRODUCTSERVICEKEY,PRODUCTKEY,SERVICEKEY, VERSION, WHENMODIFIED) "
                                                + "values (nvl(("
                                                + "select min(PRODUCTSERVICEKEY)-1 from PROCPRODUCTSERVICE),-1" + "),("
                                                + "select productkey from processingproduct where productname='"
                                                + nextLine.getCell(2).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString() + "'),("
                                                + "select SERVICEKEY from processingservice where servicename='"
                                                + nextLine.getCell(3).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString() + "'),0, sysdate); ");
                                insert.get("PROCPRODUCTSERVICE").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("DELETE FROM PROCPRODUCTSERVICE where PRODUCTKEY=("
                                        + "select productkey from processingproduct where productname='"
                                        + nextLine.getCell(2).toString() + "' and bankgroupid='"
                                        + nextLine.getCell(0).toString() + "') AND SERVICEKEY =("
                                        + "select SERVICEKEY from processingservice where servicename='"
                                        + nextLine.getCell(3).toString() + "' and bankgroupid='"
                                        + nextLine.getCell(0).toString() + "');");
                                del.get("PROCPRODUCTSERVICE").add(new StringBuilder(s));

                            }
                        } else if ("ProcessingServiceAttributeDefin".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in ProcessingServiceAttributeDefin to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into PROCSERVICEATTRDEFINITION (ATTRIBUTEDEFINITIONKEY,BANKGROUPID,SERVICEKEY,ATTRIBUTENAME,DISPLAYSEQ,METADATA, VERSION, WHENMODIFIED) "
                                                + "values (nvl(("
                                                + "select min(ATTRIBUTEDEFINITIONKEY)-1 from PROCSERVICEATTRDEFINITION),-1"
                                                + "),('" + nextLine.getCell(0).toString() + "'),("
                                                + "select SERVICEKEY from processingservice where servicename='"
                                                + nextLine.getCell(1).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),0, sysdate); ");
                                insert.get("PROCSERVICEATTRDEFINITION").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s =
                                        ("delete from PROCSERVICEATTRDEFINITION where SERVICEKEY = (select servicekey from processingservice where servicename = '"
                                                + nextLine.getCell(1).toString() + "' " + " and bankgroupid = '"
                                                + nextLine.getCell(0).toString() + "') and ATTRIBUTENAME = '"
                                                + nextLine.getCell(2).toString() + "' and METADATA = '"
                                                + nextLine.getCell(4).toString() + "';");
                                del.get("PROCSERVICEATTRDEFINITION").add(new StringBuilder(s));

                            }
                        }

                        else if ("ProcessingServiceAttribute".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        "ERROR found in ProcessingServiceAttribute to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into PROCSERVICEATTR (ATTRIBUTEVALUEKEY,BANKGROUPID,ATTRIBUTEDEFINITIONKEY,SERVICEKEY,VALUE,CANOVERRIDE, VERSION, WHENMODIFIED) "
                                                + "values (nvl(("
                                                + "select min(ATTRIBUTEVALUEKEY)-1 from PROCSERVICEATTR),-1" + "),('"
                                                + nextLine.getCell(0).toString() + "'),("
                                                + "select ATTRIBUTEDEFINITIONKEY from PROCSERVICEATTRDEFINITION where ATTRIBUTENAME='"
                                                + nextLine.getCell(2).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString()
                                                + "' and servicekey=(select SERVICEKEY from processingservice where servicename='"
                                                + nextLine.getCell(1).toString() + "')),("
                                                + "select SERVICEKEY from processingservice where servicename='"
                                                + nextLine.getCell(1).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(4).toString()) + "'),0, sysdate); ");
                                insert.get("PROCSERVICEATTR").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Delete")) {
                                String s =
                                        ("delete from  PROCSERVICEATTR where SERVICEKEY = (select servicekey from processingservice where servicename = '"
                                                + nextLine.getCell(5).toString() + "' " + " and bankgroupid = '"
                                                + nextLine.getCell(0).toString()
                                                + "') and ATTRIBUTEDEFINITIONKEY = (select PROCSERVICEATTRDEFINITION.ATTRIBUTEDEFINITIONKEY from PROCSERVICEATTRDEFINITION where attributename = '"
                                                + nextLine.getCell(2).toString() + "' "
                                                + " and SERVICEKEY = (select servicekey from processingservice where servicename = '"
                                                + nextLine.getCell(1).toString() + "' " + " and bankgroupid = '"
                                                + nextLine.getCell(0).toString() + "')) ;");
                                del.get("PROCSERVICEATTR").add(new StringBuilder(s));

                            }
                        }

                        else if ("AgentCode".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(13).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into AgentCode (AGENTCODEKEY,agentCodeValue,agentCodeValueIdType,agentCodeName,PARTICIPATIONDIRECTORYKEY, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(AGENTCODEKEY)-1 from AgentCode" + "),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),("
                                                + "select PARTICIPATIONDIRECTORYKEY from PARTICIPATIONDIRECTORY where directoryname='"
                                                + nextLine.getCell(2).toString() + "' and bankgroupid='"
                                                + nextLine.getCell(0).toString() + "'),0, sysdate); ");
                                insert.get("AGENTCODE").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("Delete from AgentCode where agentCodeValue ='"
                                        + nextLine.getCell(3).toString()
                                        + "' and PARTICIPATIONDIRECTORY=(select PARTICIPATIONDIRECTORYKEY from PARTICIPATIONDIRECTORY where directoryname='"
                                        + nextLine.getCell(2).toString() + "');");
                                del.get("AGENTCODE").add(new StringBuilder(s));

                            }
                        }

                        else if ("BICPlusIBAN".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(44).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into BICPlusIBAN (BICPLUSIBANKEY,BANKGROUPID,RECORDKEY,INSTITUTIONNAME,CITYHEADING,BRANCHINFORMATION,BICCODE,BRANCHCODE,UNIQUEBICCODE,UNIQUEBRANCHCODE,IBANBICCODE,IBANBRANCHCODE,ROUTINGBICCODE,ROUTINGBRANCHCODE,PARENTBANKCODE,COUNTRYCODE,NATIONALID,NATIONALIDTYPE,UNIQUENATIONALID,IBANCOUNTRYCODE,IBANNATIONALID,UNIQUEIBANNATIONALID,OTHERNATIONALID1,OTHERNATIONALID2,CHIPSUID,SUBTYPEINDICATOR,SERVICECODES,BRANCHQUALIFIER,SPECIALCODE,PHYSICALADDRESS1,PHYSICALADDRESS2,PHYSICALADDRESS3,PHYSICALADDRESS4,ZIPCODE,LOCATION,COUNTRYNAME,POBNUMBER,POBZIPCODE,POBLOCATION,POBCOUNTRYNAME,NATIONALIDEXPIRYDATE,UPDATEDATE,SOURCE,UPLOADPROTECTION,ENDORSESTATUS, VERSION, WHENMODIFIED) "
                                                + "values (" + "(select nvl(min(BICPLUSIBANKEY)-1,-1) from BICPlusIBAN)"
                                                + ",('" + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + nextLine.getCell(9).toString() + "'),('"
                                                + nextLine.getCell(10).toString() + "'),('"
                                                + nextLine.getCell(11).toString() + "'),('"
                                                + nextLine.getCell(12).toString() + "'),('"
                                                + nextLine.getCell(13).toString() + "'),('"
                                                + nextLine.getCell(14).toString() + "'),('"
                                                + nextLine.getCell(15).toString() + "'),('"
                                                + nextLine.getCell(16).toString() + "'),('"
                                                + nextLine.getCell(17).toString() + "'),('"
                                                + nextLine.getCell(18).toString() + "'),('"
                                                + nextLine.getCell(19).toString() + "'),('"
                                                + nextLine.getCell(20).toString() + "'),('"
                                                + nextLine.getCell(21).toString() + "'),('"
                                                + nextLine.getCell(22).toString() + "'),('"
                                                + nextLine.getCell(23).toString() + "'),('"
                                                + nextLine.getCell(24).toString() + "'),('"
                                                + nextLine.getCell(25).toString() + "'),('"
                                                + nextLine.getCell(26).toString() + "'),('"
                                                + nextLine.getCell(27).toString() + "'),('"
                                                + nextLine.getCell(28).toString() + "'),('"
                                                + nextLine.getCell(29).toString() + "'),('"
                                                + nextLine.getCell(30).toString() + "'),('"
                                                + nextLine.getCell(31).toString() + "'),('"
                                                + nextLine.getCell(32).toString() + "'),('"
                                                + nextLine.getCell(33).toString() + "'),('"
                                                + nextLine.getCell(34).toString() + "'),('"
                                                + nextLine.getCell(35).toString() + "'),('"
                                                + nextLine.getCell(36).toString() + "'),('"
                                                + nextLine.getCell(37).toString() + "'),('"
                                                + nextLine.getCell(38).toString() + "'),('"
                                                + nextLine.getCell(39).toString() + "'),(to_date('"
                                                + nextLine.getCell(40).toString() + "','dd-mm-yyyy hh24:mi:ss')),('"
                                                + nextLine.getCell(41).toString() + "'),('"
                                                + nextLine.getCell(42).toString() + "'),('"
                                                + nextLine.getCell(43).toString() + "'),0, sysdate); ");
                                insert.get("BICPLUSIBAN").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from BICPlusIBAN where BANKGROUPID = '"
                                        + nextLine.getCell(0).toString() + "'" + " and RECORDKEY = '"
                                        + nextLine.getCell(1).toString() + "';");
                                del.get("BICPLUSIBAN").add(new StringBuilder(s));

                            } else if (rowtype.contains("Update")) {
                                String s = ("update BICPlusIBAN set BANKGROUPID = '" + nextLine.getCell(0).toString()
                                        + "'" + ",INSTITUTIONNAME = '" + nextLine.getCell(2).toString() + "'"
                                        + ",CITYHEADING = '" + nextLine.getCell(3).toString() + "'"
                                        + ",BRANCHINFORMATION = '" + nextLine.getCell(4).toString() + "'"
                                        + ",BICCODE = '" + nextLine.getCell(5).toString() + "'" + ",BRANCHCODE = '"
                                        + nextLine.getCell(6).toString() + "'" + ",UNIQUEBICCODE = '"
                                        + nextLine.getCell(7).toString() + "'" + ",UNIQUEBRANCHCODE = '"
                                        + nextLine.getCell(8).toString() + "'" + ",IBANBICCODE = '"
                                        + nextLine.getCell(9).toString() + "'" + ",IBANBRANCHCODE = '"
                                        + nextLine.getCell(10).toString() + "'" + ",ROUTINGBICCODE = '"
                                        + nextLine.getCell(11).toString() + "'" + ",ROUTINGBRANCHCODE = '"
                                        + nextLine.getCell(12).toString() + "'" + ",PARENTBANKCODE = '"
                                        + nextLine.getCell(13).toString() + "'" + ",COUNTRYCODE = '"
                                        + nextLine.getCell(14).toString() + "'" + ",NATIONALID = '"
                                        + nextLine.getCell(15).toString() + "'" + ",NATIONALIDTYPE = '"
                                        + nextLine.getCell(16).toString() + "'" + ",UNIQUENATIONALID = '"
                                        + nextLine.getCell(17).toString() + "'" + ",IBANCOUNTRYCODE = '"
                                        + nextLine.getCell(18).toString() + "'" + ",IBANNATIONALID = '"
                                        + nextLine.getCell(19).toString() + "'" + ",UNIQUEIBANNATIONALID = '"
                                        + nextLine.getCell(20).toString() + "'" + ",OTHERNATIONALID1 = '"
                                        + nextLine.getCell(21).toString() + "'" + ",OTHERNATIONALID2 = '"
                                        + nextLine.getCell(22).toString() + "'" + ",CHIPSUID = '"
                                        + nextLine.getCell(23).toString() + "'" + ",SUBTYPEINDICATOR = '"
                                        + nextLine.getCell(24).toString() + "'" + ",SERVICECODES = '"
                                        + nextLine.getCell(25).toString() + "'" + ",BRANCHQUALIFIER = '"
                                        + nextLine.getCell(26).toString() + "'" + ",SPECIALCODE = '"
                                        + nextLine.getCell(27).toString() + "'" + ",PHYSICALADDRESS1 = '"
                                        + nextLine.getCell(28).toString() + "'" + ",PHYSICALADDRESS2 = '"
                                        + nextLine.getCell(29).toString() + "'" + ",PHYSICALADDRESS3 = '"
                                        + nextLine.getCell(30).toString() + "'" + ",PHYSICALADDRESS4 = '"
                                        + nextLine.getCell(31).toString() + "'" + ",ZIPCODE = '"
                                        + nextLine.getCell(32).toString() + "'" + ",LOCATION = '"
                                        + nextLine.getCell(33).toString() + "'" + ",COUNTRYNAME = '"
                                        + nextLine.getCell(34).toString() + "'" + ",POBNUMBER = '"
                                        + nextLine.getCell(35).toString() + "'" + ",POBZIPCODE = '"
                                        + nextLine.getCell(36).toString() + "'" + ",POBLOCATION = '"
                                        + nextLine.getCell(37).toString() + "'" + ",POBCOUNTRYNAME = '"
                                        + nextLine.getCell(38).toString() + "'" + ",NATIONALIDEXPIRYDATE = '"
                                        + nextLine.getCell(39).toString() + "'" + ",UPDATEDATE =  (to_date('"
                                        + nextLine.getCell(40).toString() + "','dd-mm-yyyy hh24:mi:ss'))"
                                        + ",SOURCE = '" + nextLine.getCell(41).toString() + "'"
                                        + ",UPLOADPROTECTION = '" + nextLine.getCell(42).toString() + "'"
                                        + ",ENDORSESTATUS = '" + nextLine.getCell(43).toString() + "'"
                                        + " where BANKGROUPID = '" + nextLine.getCell(0).toString() + "'"
                                        + " and RECORDKEY = '" + nextLine.getCell(1).toString() + "';");
                                insert.get("BICPLUSIBAN").add(new StringBuilder(s));

                            }
                        } else if ("BankUser".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in BankUser to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(17).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into BankUser (BANKUSERKEY,BANKGROUPID,BANKDEPARTMENTKEY,BANKUSERNAME,ENDORSESTATUS,LOCALE,STARTDATE,PERSONKEY,NOTIFICATIONTYPE, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(BANKUSERKEY)-1 from BankUser" + "),('"
                                                + nextLine.getCell(0).toString() + "'),("
                                                + getBankDepartmentKey(nextLine.getCell(2).toString()) + "),('"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),(to_date('"
                                                + nextLine.getCell(7).toString() + "','dd-mm-yyyy hh24:mi:ss')),("
                                                + "select personkey from person where personname='"
                                                + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(15).toString() + "'),0, sysdate); ");
                                insert.get("BANKUSER").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update BankUser set startDate=(to_date('" + nextLine.getCell(7).toString()
                                        + "','dd-mm-yyyy hh24:mi:ss')) ,PERSONKEY = " + "("
                                        + "select personkey from person where personname='"
                                        + nextLine.getCell(16).toString() + "')," + "NOTIFICATIONTYPE=('"
                                        + nextLine.getCell(15).toString() + "')" + " where BANKUSERNAME=('"
                                        + nextLine.getCell(3).toString() + "') and BANKDEPARTMENTKEY=("
                                        + getBankDepartmentKey(nextLine.getCell(2).toString()) + ");");
                                insert.get("BANKUSER").add(new StringBuilder(s));

                            }
                        } else if ("BankUserRole".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in BankUserRole to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(10).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into BankUserRole (BANKUSERROLEKEY,BANKUSERKEY,BANKROLEKEY,ENDORSESTATUS,STARTDATE, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(BANKUSERROLEKEY)-1 from BankUserRole"
                                                + "),(" + "SELECT BANKUSERKEY FROM BANKUSER WHERE BANKUSERNAME='"
                                                + nextLine.getCell(3).toString() + "' AND BANKDEPARTMENTKEY=("
                                                + getBankDepartmentKey(nextLine.getCell(2).toString()) + ")),("
                                                + "Select bankrolekey from bankrole where Rolename='"
                                                + nextLine.getCell(6).toString() + "' AND BANKDEPARTMENTKEY=("
                                                + getBankDepartmentKey(nextLine.getCell(2).toString()) + ")),('"
                                                + nextLine.getCell(7).toString() + "'),(to_date('"
                                                + nextLine.getCell(8).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss')),0, sysdate); ");
                                insert.get("BANKUSERROLE").add(new StringBuilder(s));

                            }
                        } else if ("Person".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println("ERROR found in Person to generate, Pls. check the file");
                                break;
                            }
                            rowtype = nextLine.getCell(23).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into Person (PERSONKEY,BANKGROUPID,personid,personname,locale,ENDORSESTATUS,SUSPENDED, VERSION, WHENMODIFIED) "
                                                + "values ((" + "select min(PERSONKEY)-1 from Person" + "),('"
                                                + nextLine.getCell(0).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(20).toString() + "'),('"
                                                + nextLine.getCell(21).toString() + "'),('"
                                                + getCharValue(nextLine.getCell(22).toString()) + "'),0, sysdate); ");
                                insert.get("PERSON").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update Person set personname=('" + nextLine.getCell(2).toString() + "'),"
                                        + "COUNTRYCODE=('" + nextLine.getCell(14).toString() + "'),LOCALE=('"
                                        + nextLine.getCell(20).toString() + "'),ENDORSESTATUS=('"
                                        + nextLine.getCell(21).toString() + "')" + " where PERSONID= ('"
                                        + nextLine.getCell(1).toString() + "');");
                                insert.get("PERSON").add(new StringBuilder(s));

                            }
                        } else if ("SecurityPasswordHistory".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(4).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into SECPASSWORDHISTORY (SECPASSWORDHISTORYKEY,PASSWORDNUMBER,CREDENTIALS,SALT,PERSONKEY, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select min(SECPASSWORDHISTORYKEY)-1 from SECPASSWORDHISTORY" + "),("
                                                + nextLine.getCell(1).toString() + "),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(3).toString() + "'),"
                                                + "(select personkey from person where personname='"
                                                + nextLine.getCell(0).toString() + "'),0, sysdate); ");
                                insert.get("SECPASSWORDHISTORY").add(new StringBuilder(s));

                            }
                        } else if ("IbanStructure".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(20).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into IBANSTRUCTURE (IBANSTRUCTUREKEY, IBANISOCOUNTRYCODE,COUNTRYCODEPOSITION,COUNTRYCODELENGTH,CHECKDIGITSPOSITION,"
                                                + "CHECKDIGITSLENGTH,BANKIDENTIFIERPOSITION,BANKIDENTIFIERLENGTH,BRANCHIDENTIFIERPOSITION,"
                                                + "BRANCHIDENTIFIERLENGTH,NATIONALIDLENGTH,ACCOUNTNUMBERPOSITION,ACCOUNTNUMBERLENGTH,"
                                                + "IBANTOTALLENGTH,USEDINSEPA,COMMENCEDATEOPTIONAL,COMMENCEDATEMANDATORY,VALIDFROM,"
                                                + "VALIDTO,UPDATEDATE,BANKGROUPID, VERSION, WHENMODIFIED) "
                                                + "values (("
                                                + "select nvl(min(IBANSTRUCTUREKEY)-1,-1) from IBANSTRUCTURE" + "),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(8).toString() + "'),('"
                                                + nextLine.getCell(9).toString() + "'),('"
                                                + nextLine.getCell(10).toString() + "'),('"
                                                + nextLine.getCell(11).toString() + "'),('"
                                                + nextLine.getCell(12).toString() + "'),('"
                                                + nextLine.getCell(13).toString() + "'),('"
                                                + nextLine.getCell(14).toString() + "'),('"
                                                + nextLine.getCell(15).toString() + "'),('"
                                                + nextLine.getCell(16).toString().charAt(0) + "'),TO_CHAR(to_date('"
                                                + nextLine.getCell(17).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss'),'yyyy-MM-dd')," + "TO_CHAR(to_date('"
                                                + nextLine.getCell(18).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss'),'yyyy-MM-dd')," + "TO_CHAR(to_date('"
                                                + nextLine.getCell(2).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss'),'yyyy-MM-dd')," + "TO_CHAR(to_date('"
                                                + nextLine.getCell(3).toString()
                                                + "','dd-mm-yyyy hh24:mi:ss'),'yyyy-MM-dd')," + "(to_date('"
                                                + nextLine.getCell(19).toString() + "','dd-mm-yyyy hh24:mi:ss')),('"
                                                + nextLine.getCell(0).toString() + "')," + "0, sysdate); ");
                                insert.get("IBANSTRUCTURE").add(new StringBuilder(s));

                            }
                        } else if ("TrustedAgent".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into TRUSTEDAGENT ( TRUSTEDAGENTKEY, TRUSTEDAGENTBIC, TRUSTEDAGENTNAME, ENDORSESTATUS, "
                                                + "BANKKEY,BANKGROUPID,VERSION, WHENMODIFIED) " + "values (("
                                                + "select nvl(min(TRUSTEDAGENTKEY), 0)-1 from TRUSTEDAGENT" + "), '"
                                                + nextLine.getCell(2).toString() + "','"
                                                + nextLine.getCell(3).toString() + "','"
                                                + nextLine.getCell(4).toString() + "',("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),'"
                                                + nextLine.getCell(0).toString() + "', 0, sysdate); ");
                                insert.get("TRUSTEDAGENT").add(new StringBuilder(s));

                            }
                        } else if ("BICNARMap".equals(sheetName)) {
                            nextLine = iterator.next();
                            rowtype = nextLine.getCell(11).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into BICNARMAP (BICNARMAPKEY,BANKGROUPID,BANKKEY,BICPLUSIBANKEY,AGENTNAME,addressLine1,addressLine2,addressline3,ZIPCODE,CITY,COUNTRYCODE,ENDORSESTATUS, VERSION, WHENMODIFIED) values (nvl((select min(BICNARMAPKEY)-1 from BICNARMAP),-1),'"
                                                + nextLine.getCell(0).toString() + "',("
                                                + getBankKey(nextLine.getCell(1).toString())
                                                + "),(SELECT BICPLUSIBANKEY FROM BICPLUSIBAN WHERE RECORDKEY='"
                                                + nextLine.getCell(2).toString() + "'),'"
                                                + nextLine.getCell(3).toString() + "','"
                                                + nextLine.getCell(4).toString() + "','"
                                                + nextLine.getCell(5).toString() + "','"
                                                + nextLine.getCell(6).toString() + "','"
                                                + nextLine.getCell(7).toString() + "','"
                                                + nextLine.getCell(8).toString() + "','"
                                                + nextLine.getCell(9).toString() + "','"
                                                + nextLine.getCell(10).toString() + "', 0, sysdate); ");
                                insert.get("BICNARMAP").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s =
                                        ("delete from BICNARMAP where BICPLUSIBANKEY=(SELECT BICPLUSIBANKEY FROM BICPLUSIBAN WHERE RECORDKEY='"
                                                + nextLine.getCell(2).toString() + "');");
                                del.get("BICNARMAP").add(new StringBuilder(s));
                            }
                        } else if ("SWIFTCounterpart".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(9).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into SWIFTCOUNTERPART (SWIFTCOUNTERPARTKEY,SERVICENAME,ISSUER,CORRESPONDENT,RMASTATUS,VALIDITYTODATE,VALIDITYFROMDATE,ISSUEDDATETIME,BANKGROUPID,ENDORSESTATUS,VERSION,WHENMODIFIED) "
                                                + "values (("
                                                + "nvl((select min(SWIFTCOUNTERPARTKEY)-1 from SWIFTCOUNTERPART),-1)"
                                                + "),('" + nextLine.getCell(3).toString() + "'),('"
                                                + nextLine.getCell(1).toString() + "'),('"
                                                + nextLine.getCell(2).toString() + "'),('"
                                                + nextLine.getCell(4).toString() + "'),(TO_DATE('" + nextLine.getCell(7)
                                                + "', 'DD-MM-YYYY hh24:mi:ss')),(TO_DATE('" + nextLine.getCell(6)
                                                + "', 'DD-MM-YYYY hh24:mi:ss')),(TO_DATE('" + nextLine.getCell(5)
                                                + "', 'DD-MM-YYYY hh24:mi:ss')),('" + nextLine.getCell(0).toString()
                                                + "'),('" + nextLine.getCell(8).toString() + "'),0, sysdate); ");
                                insert.get("SWIFTCOUNTERPART").add(new StringBuilder(s));

                            }
                        } else if ("SWIFTCounterpartPermission".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(8).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("insert into SWIFTCOUNTERPARTPERMISSION (SWIFTCOUNTERPARTPKEY,SWIFTCOUNTERPARTKEY,PERMISSIONTYPE,INCLUDES,EXCLUDES,ENDORSESTATUS,VERSION,WHENMODIFIED) "
                                                + "values (("
                                                + "nvl((select min(SWIFTCOUNTERPARTPKEY)-1 from SWIFTCOUNTERPARTPERMISSION),-1)"
                                                + "),(("
                                                + "select SWIFTCOUNTERPARTKEY from SWIFTCOUNTERPART where BANKGROUPID='"
                                                + nextLine.getCell(0).toString() + "' and ISSUER='"
                                                + nextLine.getCell(1).toString() + "' and CORRESPONDENT='"
                                                + nextLine.getCell(2).toString() + "' and SERVICENAME='"
                                                + nextLine.getCell(3).toString() + "')),('"
                                                + nextLine.getCell(4).toString() + "'),('"
                                                + nextLine.getCell(7).toString() + "'),('"
                                                + nextLine.getCell(6).toString() + "'),('"
                                                + nextLine.getCell(5).toString() + "'),0, sysdate); ");
                                insert.get("SWIFTCOUNTERPARTPERMISSION").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from SWIFTCOUNTERPARTPERMISSION where SWIFTCOUNTERPARTKEY = ("
                                        + "select SWIFTCOUNTERPARTKEY from SWIFTCOUNTERPART where BANKGROUPID='"
                                        + nextLine.getCell(0).toString() + "' and ISSUER='"
                                        + nextLine.getCell(1).toString() + "' and CORRESPONDENT='"
                                        + nextLine.getCell(2).toString() + "' and SERVICENAME='"
                                        + nextLine.getCell(3).toString() + "') and PERMISSIONTYPE='"
                                        + nextLine.getCell(4).toString() + "';");
                                del.get("SWIFTCOUNTERPARTPERMISSION").add(new StringBuilder(s));
                            }
                        } else if ("BankCurrency".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(3).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s = ("insert into bankcurrency (BANKKEY,CURRENCYCODE) " + "values (("
                                        + getBankKey(nextLine.getCell(1).toString()) + "),('"
                                        + nextLine.getCell(2).toString() + "')); ");
                                insert.get("BANKCURRENCY").add(new StringBuilder(s));

                            }
                        } else if ("WorkListQCriteria".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(20).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("Insert into WORKLISTQCRITERIA(WORKLISTQCRITERIAKEY,BANKKEY,WORKQUEUEKEY,bankGroupId,eventCode ,targetEntityType,minPaymentAmount,maxPaymentAmount,   paymentCurrency,    messageType,    productId,  processingScheme,   informationalFlag   ,category,  paymentPriority,    status, classification  ,minOffsetSettlementDay,    maxOffsetSettlementDay  ,priority   ,version,whenmodified) values ("
                                                + "(select nvl(min(WORKLISTQCRITERIAKEY), 0)-1 from WORKLISTQCRITERIA),("
                                                + getBankKey(nextLine.getCell(17).toString())
                                                + "),(select WORKQUEUEKEY from WORKQUEUE where name='"
                                                + nextLine.getCell(19).toString() + "'),'"
                                                + nextLine.getCell(0).toString() + "','"
                                                + (nextLine.getCell(1) == null ? "" : nextLine.getCell(1).toString())
                                                + "','"
                                                + (nextLine.getCell(2) == null ? "" : nextLine.getCell(2).toString())
                                                + "','"
                                                + (nextLine.getCell(3) == null ? "" : nextLine.getCell(3).toString())
                                                + "','"
                                                + (nextLine.getCell(4) == null ? "" : nextLine.getCell(4).toString())
                                                + "','"
                                                + (nextLine.getCell(5) == null ? "" : nextLine.getCell(5).toString())
                                                + "','" + nextLine.getCell(6).toString() + "','"
                                                + nextLine.getCell(7).toString() + "','"
                                                + nextLine.getCell(8).toString() + "','"
                                                + getCharValue(nextLine.getCell(9).toString()) + "','"
                                                + nextLine.getCell(10).toString() + "','"
                                                + nextLine.getCell(11).toString() + "','"
                                                + nextLine.getCell(12).toString() + "','"
                                                + nextLine.getCell(13).toString() + "','"
                                                + nextLine.getCell(14).toString() + "','"
                                                + nextLine.getCell(15).toString() + "','"
                                                + nextLine.getCell(16).toString() + "','" + "0',sysdate); ");
                                insert.get("WORKLISTQCRITERIA").add(new StringBuilder(s));

                            } else if (rowtype.equalsIgnoreCase("Update")) {
                                String s = ("update WORKLISTQCRITERIA " + "set productid='"
                                        + nextLine.getCell(7).toString() + "' " + "where " + "BANKKEY=("
                                        + getBankKey(nextLine.getCell(17).toString())
                                        + ") and WORKQUEUEKEY=(select WORKQUEUEKEY from WORKQUEUE where name='"
                                        + nextLine.getCell(19).toString() + "') and bankGroupId='"
                                        + nextLine.getCell(0).toString() + "' and eventCode='"
                                        + nextLine.getCell(1).toString() + "' and "
                                        + (nextLine.getCell(2) != null && !nextLine.getCell(2).toString().isEmpty()
                                                ? "targetEntityType='" + nextLine.getCell(2).toString() + "'"
                                                : "targetEntityType is null")
                                        + ";");
                                insert.get("WORKLISTQCRITERIA").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("Delete from WORKLISTQCRITERIA where " + "BANKKEY=("
                                        + getBankKey(nextLine.getCell(17).toString())
                                        + ") and WORKQUEUEKEY=(select WORKQUEUEKEY from WORKQUEUE where name='"
                                        + nextLine.getCell(19).toString() + "') and bankGroupId='"
                                        + nextLine.getCell(0).toString() + "' and eventCode='"
                                        + nextLine.getCell(1).toString() + "' and "
                                        + (nextLine.getCell(2) != null && !nextLine.getCell(2).toString().isEmpty()
                                                ? "targetEntityType='" + nextLine.getCell(2).toString() + "'"
                                                : "targetEntityType is null")
                                        + ";");
                                del.get("WORKLISTQCRITERIA").add(new StringBuilder(s));

                            }
                        } else if ("WorkQueue".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(" ---- ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(6).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO WORKQUEUE (WORKQUEUEKEY,BANKGROUPID,BANKKEY,DELETED,DESCRIPTION,ENDORSESTATUS,NAME,VERSION,WHENMODIFIED) VALUES ("
                                                + "(select nvl(min(WORKQUEUEKEY), 0)-1 from WORKQUEUE),'"
                                                + nextLine.getCell(0).toString() + "',("
                                                + getBankKey(nextLine.getCell(1).toString()) + "),'"
                                                + getCharValue(nextLine.getCell(5).toString()) + "','"
                                                + nextLine.getCell(3).toString() + "','"
                                                + nextLine.getCell(4).toString() + "','"
                                                + nextLine.getCell(2).toString() + "'," + "'0',sysdate); ");
                                insert.get("WORKQUEUE").add(new StringBuilder(s));

                            } else if (rowtype.equals("Delete")) {
                                String s = ("delete from WORKQUEUE where " + "BANKGROUPID='"
                                        + nextLine.getCell(0).toString() + "' and BANKKEY=("
                                        + getBankKey(nextLine.getCell(1).toString()) + ") and NAME='"
                                        + nextLine.getCell(2).toString() + "'; ");
                                del.get("WORKQUEUE").add(new StringBuilder(s));

                            }
                        } else if ("SwiftCurrency".equals(sheetName)) {
                            nextLine = iterator.next();
                            if (StringUtils.isBlank(nextLine.getCell(0).toString())) {
                                System.out.println(
                                        " ---- SWIFTCURRENCY ERROR found to generate, Pls. check the file ----");
                                break;
                            }
                            rowtype = nextLine.getCell(5).getStringCellValue();
                            if (rowtype.equals("New")) {
                                String s =
                                        ("INSERT INTO SWIFTCURRENCY (CURRENCYCODE,CURRENCYNAMEFIRST,CURRENCYNAMESECOND,FRACTIONALDIGIT,COUNTRYCODE,VERSION,WHENMODIFIED) VALUES ("

                                                + "'" + nextLine.getCell(0).toString() + "',('"
                                                + nextLine.getCell(1).toString() + "'),'"
                                                + nextLine.getCell(4).toString() + "','"
                                                + nextLine.getCell(3).toString() + "','"
                                                + nextLine.getCell(2).toString() + "','0',sysdate); ");
                                insert.get("SWIFTCURRENCY").add(new StringBuilder(s));

                            }
                        }

                        // code end by varun yadav

                        else {
                            nextLine = iterator.next();
                            System.out.println("No Implementation Available for the sheet - " + sheetName);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("|||||||||||||||||    PRINTING LISTS  ||||||||||||||||||||||||||||||");
            File inFile = new File(filepath);
            File outFile = new File(inFile.getParent() + File.separatorChar + "GeneratedIncrementalScript.sql");
            // creates the file
            outFile.createNewFile();
            PrintWriter writer = new PrintWriter(outFile);
            for (Map.Entry<String, List<StringBuilder>> e : del.entrySet()) {
                List<StringBuilder> list = del.get(e.getKey());
                if (!list.isEmpty()) {
                    for (StringBuilder sb : list) {
                        // System.out.println(sb.toString());
                        writer.println(sb.toString());
                    }
                    writer.println("\r\n");
                    // System.out.println();
                }
            }
            System.out.println("\n-- INSERT SCRIPTS");
            for (Map.Entry<String, List<StringBuilder>> e : insert.entrySet()) {
                List<StringBuilder> list = insert.get(e.getKey());
                if (!list.isEmpty()) {
                    for (StringBuilder sb : list) {
                        writer.println(sb.toString());
                        // System.out.println(sb.toString());
                    }
                    writer.println("\r\n");
                    // System.out.println();
                }
            }
            writer.flush();
            writer.close();
            System.out.println("Incremental Script File Path: " + outFile.getAbsolutePath());

        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }

    private static String getParticipantKey(String bankName, String bankDepartmentName, String participantName,
            String externalId) {
        return "SELECT P.PARTICIPANTKEY FROM PARTICIPANT P WHERE  P.PARTICIPANTNAME='" + participantName
                + "' AND P.EXTERNALID='" + externalId + "'";
    }

    private static String getParticipantKeyWithDepartment(String bankName, String bankDepartmentName,
            String participantName, String externalId) {
        return "SELECT P.PARTICIPANTKEY FROM PARTICIPANT P WHERE  P.PARTICIPANTNAME='" + participantName
                + "' AND P.EXTERNALID='" + externalId + "' AND p.owningbankdepartmentkey=("
                + getBankDepartmentKey(bankDepartmentName) + ")";
    }

    private static String getAgreementKey(String bankName, String bankDepartmentName, String participantName,
            String externalId, String agreementName) {
        return "SELECT AGREEMENTKEY FROM PROCESSINGAGREEMENT PA LEFT OUTER JOIN PARTICIPANT P ON PA.BANKGROUPID = P.BANKGROUPID "
                + "LEFT OUTER JOIN BANKDEPARTMENT BD ON P.OWNINGBANKDEPARTMENTKEY = BD.BANKDEPARTMENTKEY "
                + "LEFT OUTER JOIN BANK B ON BD.BANKKEY = B.BANKKEY WHERE B.BANKNAME='" + bankName
                + "' AND BD.BANKDEPARTMENTNAME='" + bankDepartmentName + "' AND P.PARTICIPANTNAME='" + participantName
                + "' AND P.EXTERNALID='" + externalId + "' AND PA.AGREEMENTNAME='" + agreementName + "'";
    }

    private static String getAccountKey(String domesticAccountNo, String domesticBankIndentifier, String currency,
            String bankBroupId, String swiftBicCode, String propAccountNo) {
        return "SELECT ACCOUNTKEY FROM ACCOUNT WHERE DOMESTICACCOUNTNUMBER='" + domesticAccountNo + "' AND CURRENCY='"
                + currency + "' and swiftBicCode='" + swiftBicCode + "' AND BANKGROUPID='" + bankBroupId + "'"
                + " AND PROPRIETARYACCOUNTNUMBER='" + propAccountNo + "'";
    }

    private static String getBusinessCalendarKey(String businessCalendarName) {
        return "SELECT BUSINESSCALENDARKEY FROM BUSINESSCALENDAR WHERE BUSINESSCALENDARNAME ='" + businessCalendarName
                + "'";
    }

    private static char getCharValue(String value) {
        if (value.equalsIgnoreCase("false")) {
            return Character.valueOf('F').charValue();
        } else if (value.equalsIgnoreCase("true")) {
            return Character.valueOf('T').charValue();
        }
        return 'F';
    }

    private static String getSettlementTimelineKey(String bankName, String timelineName) {
        return "select SETTLEMENTTIMELINEKEY from SETTLEMENTTIMELINE where BANKKEY=(" + getBankKey(bankName)
                + ") and NAME='" + timelineName + "'";
    }

    private static String getPersonKey(String personId) {
        return "select PERSONKEY from PERSON where PERSONID='" + personId + "'";
    }

    private static String getParentRoutingRuleKey(String bankName, String routingRuleDisplayId) {
        return "select ROUTINGRULEKEY from ROUTINGRULE where BANKKEY=(" + getBankKey(bankName)
                + ") and ROUTINGRULEDISPLAYID=('" + routingRuleDisplayId + "')";

    }

    private static String getExchangeConditionKey(String externalId, String exConExternalId, String participantName) {
        return "select EXCHANGECONDITIONKEY from EXCHANGECONDITION where PARTICIPANTKEY=("
                + getParticipantKey(externalId, participantName) + ") and EXCHANGECONDITIONEXTERNALID=('"
                + exConExternalId + "')";

    }

    private static String getRoutingRuleKey(String bankName, String routingDisplayId) {
        return "select ROUTINGRULEKEY from ROUTINGRULE where BANKKEY=(" + getBankKey(bankName)
                + ") and ROUTINGRULEDISPLAYID=('" + routingDisplayId + "')";

    }

    /**
     * @return String
     */
    private static String getProductServiceKey(String bankName, String productName, String serviceName) {
        return "select PRODUCTSERVICEKEY from PROCPRODUCTSERVICE where PRODUCTKEY=("
                + getProcProductKey(productName, bankName) + ") and SERVICEKEY=(" + getProcServiceKey(serviceName)
                + ")";
    }

    /**
     * @return String
     */
    private static String getProcServiceAttributeDefinitionKey(String attributeName, String serviceName) {
        return "select ATTRIBUTEDEFINITIONKEY from PROCSERVICEATTRDEFINITION where attributeName='" + attributeName
                + "' and SERVICEKEY=(" + getProcServiceKey(serviceName) + ")";
    }

    /**
     * @return String
     */
    private static String getAgreementServiceKeyWithDepartment(String agreementName, String externalId,
            String serviceName, String participantName, String department, String bankName) {
        return "select AGREEMENTSERVICEKEY from PROCAGREEMENTSERVICE where AGREEMENTKEY=("
                + getAgreementKeyWithDepartment(agreementName, externalId, participantName, department, bankName)
                + ") and SERVICEKEY=(" + getProcServiceKey(serviceName) + ")";
    }

    /**
     * @return String
     */
    private static String getAgreementKeyWithDepartment(String agreementName, String externalId, String participantName,
            String department, String bankName) {
        return "select AGREEMENTKEY from PROCESSINGAGREEMENT where AGREEMENTNAME='" + agreementName
                + "' and PARTICIPANTKEY=("
                + getParticipantKeyWithDepartment(bankName, department, participantName, externalId) + ")";
    }

    /**
     * @return String
     */
    private static String getAgreementKey(String agreementName, String externalId, String participantName) {
        return "select AGREEMENTKEY from PROCESSINGAGREEMENT where AGREEMENTNAME='" + agreementName
                + "' and PARTICIPANTKEY=(" + getParticipantKey(externalId, participantName) + ")";
    }

    /**
     * @return String
     */
    private static String getParticipantKey(String externalId) {
        return "select PARTICIPANTKEY from PARTICIPANT where EXTERNALID='" + externalId + "'";
    }

    /**
     * @return String
     */
    private static String getParticipantKey(String externalId, String participantName) {
        return "select PARTICIPANTKEY from PARTICIPANT where EXTERNALID='" + externalId + "' and participantname='"
                + participantName + "'";
    }

    /**
     * @return String
     */
    private static String getAttributeDefinitionKey(String attributeName, String serviceName) {
        return "select ATTRIBUTEDEFINITIONKEY from PARAMSERVICEATTRDEFINITION where ATTRIBUTENAME='" + attributeName
                + "' and SERVICEKEY=(" + getServiceKey(serviceName) + ")";
    }

    private static String getProcProductKey(String productName, String bankName) {
        return "select productkey from PROCESSINGPRODUCT where productName='" + productName + "' and BANKKEY=("
                + getBankKey(bankName) + ")";
    }

    /**
     * @return String
     */
    private static String getProcServiceKey(String serviceName) {
        return "select SERVICEKEY from PROCESSINGSERVICE where SERVICENAME='" + serviceName + "'";
    }

    /**
     * @return String
     */
    private static String getServiceKey(String serviceName) {
        return "select SERVICEKEY from PARAMETERSERVICE where SERVICENAME='" + serviceName + "'";
    }

    /**
     * @return String
     */
    private static String getBankKey(String bankName) {
        return "select BANKKEY from BANK where bankname='" + bankName + "'";
    }

    /**
     * @return String
     */
    private static String getchargeConfigurationKey() {
        return "select nvl(min(CHARGECONFIGURATIONKEY), 0)-1 from CHARGECONFIGURATION";
    }

    /**
     * @return String
     */
    public static String getBankFapKey() {
        return "select min(BANKFAPKEY)-1 from bankfap";
    }

    /**
     * @return String
     */
    public static String getBankDapKey() {
        return "select min(BANKDAPKEY)-1 from bankdap";
    }

    /**
     * @return String
     */
    public static String getBankDapEntityKey() {
        return "select min(BANKDAPENTITYKEY)-1 from BANKDAPENTITY";
    }

    /**
     * @return String
     */
    public static String getBankRoleKey(String bankRole, String dep) {
        return "select BANKROLEKEY from bankrole where rolename = '" + bankRole
                + "' and BANKDEPARTMENTKEY=(select BANKDEPARTMENTKEY from bankdepartment where bankdepartmentname='"
                + dep + "') ";
    }

    /**
     * @return String
     */
    public static String getBankFunctionKey(String entity, String action, String module, String bankgroupId) {
        return "select BANKFUNCTIONKEY from BANKFUNCTION where entity = '" + entity + "' and action = '" + action
                + "' and module = '" + module + "' and bankgroupid = '" + bankgroupId + "'";
    }

    /**
     * @return String
     */
    public static String getBankFapKey(String fapEntity, String action, String module, String bankGroupId) {
        return "select bankfapkey from bankfap b, bankfunction bf where b.bankfunctionkey = bf.bankfunctionkey and  bf.entity = '"
                + fapEntity + "' and bf.action = '" + action + "' and bf.module = '" + module
                + "' and bf.bankGroupId ='OPFBankgroup'";
    }

    /**
     * @return String
     */
    public static String getBankFapKey(String fapEntity, String action, String module, String bankGroupId,
            String roleName) {
        return "select b.bankfapkey from bankfap b, bankfunction bf, bankrole br where b.bankfunctionkey = bf.bankfunctionkey and br.bankrolekey = b.bankrolekey and br.rolename = '"
                + roleName + "' and  bf.entity = '" + fapEntity + "' and bf.action = '" + action + "' and bf.module = '"
                + module + "' and bf.bankGroupId ='OPFBankgroup'";
    }

    /**
     * @return String
     */
    public static String getBankDapEntityKey(String fapEntity, String action, String module, String roleName,
            String dapEntity, String dapviewfield, String bankGroupID) {
        return "select BANKDAPENTITYKEY from BANKDAPENTITY bd where bd.DAPENTITY ='" + dapEntity
                + "' and bd.BANKFUNCTIONKEY = (select bf.BANKFUNCTIONKEY from bankfunction bf, bankrole br where br.rolename = '"
                + roleName + "' and  bf.entity = '" + fapEntity + "' and bf.action = '" + action + "' and bf.module = '"
                + module + "' and bf.bankGroupId ='" + bankGroupID + "' and DAPVIEWFIELD = '" + dapviewfield + "' ";
    }

    /**
     * @return String
     */
    public static String getBankFunctionKey() {
        return "select min(BANKFUNCTIONKEY)-1 from bankfunction";

    }

    private static String getBankDepartmentKey(String bankDepartmentName) {

        return "SELECT BANKDEPARTMENTKEY FROM BANKDEPARTMENT WHERE BANKDEPARTMENTNAME ='" + bankDepartmentName + "'";
    }

    private static String getBankDepartmentKey(String bankName, String bankDepartmentName) {
        return "SELECT BANKDEPARTMENTKEY FROM BANKDEPARTMENT BD, BANK B WHERE B.BANKNAME='" + bankName
                + "' AND BD.BANKDEPARTMENTNAME='" + bankDepartmentName + "'";
    }

    private static String getParticipantDirectory(String bankName, String directoryname, String bankgroupId) {
        return "SELECT PARTICIPATIONDIRECTORYKEY FROM PARTICIPATIONDIRECTORY PD, BANK B WHERE B.BANKNAME='" + bankName
                + "' AND PD.DIRECTORYNAME='" + directoryname + "' AND PD.BANKGROUPID='" + bankgroupId + "'";
    }

    /**
     * @return
     */
    private static String getTimeInMillis(String timeStamp) {
        java.util.StringTokenizer st = new java.util.StringTokenizer(timeStamp, ":.");
        int var1 = Integer.parseInt(st.nextToken());
        int var2 = Integer.parseInt(st.nextToken());
        int var3 = Integer.parseInt(st.nextToken());
        int var4 = Integer.parseInt(st.nextToken());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1970, Calendar.JANUARY, 1);

        cal.set(Calendar.HOUR_OF_DAY, var1);
        cal.set(Calendar.MINUTE, var2);
        cal.set(Calendar.SECOND, var3);
        cal.set(Calendar.MILLISECOND, var4);

        return String.valueOf(cal.getTimeInMillis());
    }

    private static List<String> getData(String filePath) {
        List<String> dataList = new ArrayList<>();
        try {
            dataList = Files.readAllLines(Paths.get(filePath), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    /*This function is used to generate sql script for ParameterServiceAttribute table*/
    private static void generateSqlForParameterServiceAttribute(String attributeName, String serviceName, String action,
            String data, String filePath) throws IOException {
        if ("update".equalsIgnoreCase(action)) {
            StringBuilder queryStr = new StringBuilder();
            if (data.length() < 1500) {
                queryStr.append("update PARAMSERVICEATTR set VALUE=('" + data + "') where (SERVICEKEY = ("
                        + getServiceKey(serviceName) + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                        + getAttributeDefinitionKey(attributeName, serviceName) + ")); ");
            } else {
                queryStr.append(
                        "update PARAMSERVICEATTR set VALUE=('" + data.substring(0, 1500) + "') where (SERVICEKEY = ("
                                + getServiceKey(serviceName) + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                                + getAttributeDefinitionKey(attributeName, serviceName) + ")); ");
                for (int i = 1500; i < data.length();) {
                    int m = i + 1500;

                    queryStr.append("\n" + "update PARAMSERVICEATTR set VALUE=value || ('"
                            + (m < data.length() ? data.substring(i, m) : data.substring(i, data.length()))
                            + "') where (SERVICEKEY = (" + getServiceKey(serviceName)
                            + ") ) and (ATTRIBUTEDEFINITIONKEY = ("
                            + getAttributeDefinitionKey(attributeName, serviceName) + ")); ");
                    i = m;
                }
            }
            //write file
            createFile(queryStr.toString(), attributeName, filePath);
        }

    }

    /*This function is used for write  data into a output file*/
    private static void createFile(String data, String fileName, String filePath) {
        File inFile = new File(filePath);
        File outFile = new File(inFile.getParent() + File.separatorChar + fileName + ".sql");
        System.out.println("Incremental Script File Path: " + outFile.getAbsolutePath());
        try (PrintWriter writer = new PrintWriter(outFile)) {
            writer.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getFileExtension(String filePath) {
        String fileName = new File(filePath).getName();
        if (fileName.lastIndexOf('.') != -1 && fileName.lastIndexOf('.') != 0)
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        else
            return "";
    }

    /*This function is used for read data from .txt file*/
    private static void callReadTxtFile(String path) throws IOException {
        List<String> datalist = getData(path);
        if (!datalist.isEmpty()) {
            String[] attributArr = datalist.get(0).split("@");
            String attributeName = attributArr[0];
            String serviceName = attributArr[1];
            String action = attributArr[2];
            String data = datalist.get(1);
            generateSqlForParameterServiceAttribute(attributeName, serviceName, action, data, path);
        }

    }

    public static String getBusinessEventDefinitionKey() {
        return "select nvl(min(BUSINESSEVENTDEFINITIONKEY), 0)-1 from BUSINESSEVENTDEFINITION";

    }

    public static String getSchemeReasonKey() {
        return "select nvl(min(SCHEMEREASONKEY), 0)-1 from SCHEMEREASON";

    }

    public static String getSchemeReasonCodeSetKey() {
        return "select nvl(min(SCHEMEREASONCODESETKEY), 0)-1 from SCHEMEREASONCODESET";

    }

    private static String getSchemeReasonKey(String scheme, String reasonCode, String reasonProprietaryCode) {
        return "select SR.SCHEMEREASONKEY from SCHEMEREASON SR where SR.REASONCODE ='" + reasonCode
                + "' AND  SR.SCHEME ='" + scheme + "' AND  SR.REASONPROPRIETARYCODE = '" + reasonProprietaryCode + "'";
    }

    private static String getSchemeReasonKey(String reasonCode) {
        return "select SCHEMEREASONKEY from SCHEMEREASON where REASONCODE='" + reasonCode + "'";
    }

}