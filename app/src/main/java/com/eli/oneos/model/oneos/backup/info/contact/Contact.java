package com.eli.oneos.model.oneos.backup.info.contact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class Contact {
    static final String NL = "\r\n";
    // static final String IMPROP = "X-IM-PROTO";
    static final String IMPROP = "X-";
    static final String TYPE_PARAM = "TYPE";
    static final String PROTO_PARAM = "PROTO";
    static final String[] PROTO = {"AIM", // ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM
            // = 0
            "MSN", // ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN = 1
            "YAHOO", // ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO = 2
            "SKYPE", // ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE = 3
            "QQ", // ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ = 4
            "GTALK", // ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK
            // = 5
            "ICQ", // ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ = 6
            "JABBER" // ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER = 7
    };

    // 联系人信息
    long parseLen;
    static final String BIRTHDAY_FIELD = "Birthday:";
    private List<RowData> phones;// 电话号码，多个
    private List<RowData> emails;// email地址
    private List<RowData> addrs;// 住址
    private List<RowData> ims;// 即时通讯，比如QQ,MSN
    private List<OrgData> orgs;// 工作单位
    private PeopleData peopleData; // id 、 姓名
    private String notes;// 备注

    /**
     * 构造函数
     */
    public Contact() {
        initialize();
    }

    static class RowData {
        public int type;
        public String data;
        public boolean preferred;
        public String customType;
        public String protocol;

        public RowData(int type, String data, boolean preferred, String customType) {
            this.type = type;
            this.data = data;
            this.preferred = preferred;
            this.customType = customType;
            this.protocol = null;
        }

        public RowData(int type, String data, boolean preferred) {
            this(type, data, preferred, null);
        }
    }

    static class PeopleData {
        public String _id;
        public String displayName;
        public String familyNmae;
        public String midName;
        public String givenName;

        public PeopleData() {
            clear();
        }

        public void clear() {
            this._id = null;
            this.displayName = null;
            this.familyNmae = null;
            this.midName = null;
            this.givenName = null;
        }
    }

    static class OrgData {
        public int type;
        public String title;
        public String company;
        public String customType;

        public OrgData(int type, String title, String company, String customType) {
            this.type = type;
            this.title = title;
            this.company = company;
            this.customType = customType;
        }
    }

    Hashtable<String, handleProp> propHandlers;

    interface handleProp {
        void parseProp(final String propName, final Vector<String> propVec, final String val);
    }

    private void initialize() {
        reset();
        propHandlers = new Hashtable<String, handleProp>();

        handleProp simpleValue = new handleProp() {
            public void parseProp(final String propName, final Vector<String> propVec,
                                  final String val) {
                if (propName.equals("FN")) {
                    peopleData.displayName = val;
                } else if (propName.equals("NOTE")) {
                    notes = val;
                } else if (propName.equals("N")) {
                    String[] names = ContactStrUtils.split(val, ";");
                    if (names[1] != null || names[1] != "") {
                        peopleData.familyNmae = names[0];
                        peopleData.givenName = names[1];
                    } else {
                        String[] names2 = ContactStrUtils.split(names[0], " ");
                        peopleData.familyNmae = names2[0];
                        if (names2.length > 1)
                            peopleData.givenName = names2[1];
                    }
                    if (peopleData.displayName == null) {
                        StringBuffer fullname = new StringBuffer();
                        if (peopleData.familyNmae != null)
                            fullname.append(peopleData.familyNmae);
                        if (peopleData.givenName != null) {
                            if (peopleData.familyNmae != null) {
                                fullname.append(" ");
                            }
                            fullname.append(peopleData.givenName);
                        }
                        peopleData.displayName = fullname.toString();
                    }
                }
            }
        };

        propHandlers.put("FN", simpleValue);
        propHandlers.put("NOTE", simpleValue);
        propHandlers.put("N", simpleValue);

        handleProp orgHandler = new handleProp() {

            @Override
            public void parseProp(String propName, Vector<String> propVec, String val) {
                String label = null;
                for (String prop : propVec) {
                    String[] propFields = ContactStrUtils.split(prop, "=");
                    if (propFields[0].equalsIgnoreCase(TYPE_PARAM) && propFields.length > 1) {
                        label = propFields[1];
                    }
                }
                if (propName.equals("TITLE")) {
                    boolean setTitle = false;
                    for (OrgData org : orgs) {
                        if (label == null && org.customType != null)
                            continue;
                        if (label != null && !label.equals(org.customType))
                            continue;

                        if (org.title == null) {
                            org.title = val;
                            setTitle = true;
                            break;
                        }
                    }
                    if (!setTitle) {
                        orgs.add(new OrgData(label == null ? Organization.TYPE_WORK
                                : Organization.TYPE_CUSTOM, val, null, label));
                    }
                } else if (propName.equals("ORG")) {
                    String[] orgFields = ContactStrUtils.split(val, ";");
                    boolean setCompany = false;
                    for (OrgData org : orgs) {
                        if (label == null && org.customType != null)
                            continue;
                        if (label != null && !label.equals(org.customType))
                            continue;

                        if (org.company == null) {
                            org.company = val;
                            setCompany = true;
                            break;
                        }
                    }
                    if (!setCompany) {
                        orgs.add(new OrgData(label == null ? Organization.TYPE_WORK
                                : Organization.TYPE_CUSTOM, null, orgFields[0], label));
                    }
                }
            }
        };

        propHandlers.put("ORG", orgHandler);
        propHandlers.put("TITLE", orgHandler);

        propHandlers.put("TEL", new handleProp() {
            public void parseProp(final String propName, final Vector<String> propVec,
                                  final String val) {
                String label = null;
                int subtype = Phone.TYPE_OTHER;
                boolean preferred = false;
                for (String prop : propVec) {
                    if (prop.equalsIgnoreCase("HOME") || prop.equalsIgnoreCase("VOICE")) {
                        if (subtype != Phone.TYPE_FAX_HOME)
                            subtype = Phone.TYPE_HOME;
                    } else if (prop.equalsIgnoreCase("WORK")) {
                        if (subtype == Phone.TYPE_FAX_HOME) {
                            subtype = Phone.TYPE_FAX_WORK;
                        } else
                            subtype = Phone.TYPE_WORK;
                    } else if (prop.equalsIgnoreCase("CELL")) {
                        subtype = Phone.TYPE_MOBILE;
                    } else if (prop.equalsIgnoreCase("FAX")) {
                        if (subtype == Phone.TYPE_WORK) {
                            subtype = Phone.TYPE_FAX_WORK;
                        } else
                            subtype = Phone.TYPE_FAX_HOME;
                    } else if (prop.equalsIgnoreCase("PAGER")) {
                        subtype = Phone.TYPE_PAGER;
                    } else if (prop.equalsIgnoreCase("PREF")) {
                        preferred = true;
                    } else {
                        String[] propFields = ContactStrUtils.split(prop, "=");

                        if (propFields.length > 1 && propFields[0].equalsIgnoreCase(TYPE_PARAM)) {
                            label = propFields[1];
                            subtype = Phone.TYPE_CUSTOM;
                        }
                    }
                }
                phones.add(new RowData(subtype, toCanonicalPhone(val), preferred, label));
            }
        });

        propHandlers.put("ADR", new handleProp() {
            public void parseProp(final String propName, final Vector<String> propVec,
                                  final String val) {
                boolean preferred = false;
                String label = null;
                int subtype = Email.TYPE_WORK;
                for (String prop : propVec) {
                    if (prop.equalsIgnoreCase("WORK")) {
                        subtype = Email.TYPE_WORK;
                    } else if (prop.equalsIgnoreCase("HOME")) {
                        subtype = Email.TYPE_HOME;
                    } else if (prop.equalsIgnoreCase("PREF")) {
                        preferred = true;
                    } else {
                        String[] propFields = ContactStrUtils.split(prop, "=");

                        if (propFields.length > 1 && propFields[0].equalsIgnoreCase(TYPE_PARAM)) {
                            label = propFields[1];
                            subtype = Email.TYPE_CUSTOM;
                        }
                    }
                }
                if (val != null) {
                    addrs.add(new RowData(subtype, val, preferred, label));
                }
            }
        });

        propHandlers.put("EMAIL", new handleProp() {
            public void parseProp(final String propName, final Vector<String> propVec,
                                  final String val) {
                boolean preferred = false;
                String label = null;
                int subtype = Email.TYPE_HOME;
                for (String prop : propVec) {
                    if (prop.equalsIgnoreCase("PREF")) {
                        preferred = true;
                    } else if (prop.equalsIgnoreCase("WORK")) {
                        subtype = Email.TYPE_WORK;
                    } else {
                        String[] propFields = ContactStrUtils.split(prop, "=");

                        if (propFields.length > 1 && propFields[0].equalsIgnoreCase(TYPE_PARAM)) {
                            label = propFields[1];
                            subtype = Email.TYPE_CUSTOM;
                        }
                    }
                }
                emails.add(new RowData(subtype, val, preferred, label));
            }
        });

        propHandlers.put(IMPROP, new handleProp() {
            public void parseProp(final String propName, final Vector<String> propVec,
                                  final String val) {
                boolean preferred = false;
                String label = null;
                String proto = null;
                int subtype = Im.TYPE_HOME;
                for (String prop : propVec) {
                    if (prop.equalsIgnoreCase("PREF")) {
                        preferred = true;
                    } else if (prop.equalsIgnoreCase("WORK")) {
                        subtype = Im.TYPE_WORK;
                    } else {
                        String[] propFields = ContactStrUtils.split(prop, "=");
                        if (propFields.length > 1) {
                            if (propFields[0].equalsIgnoreCase(PROTO_PARAM)) {
                                proto = propFields[1];
                            } else if (propFields[0].equalsIgnoreCase(TYPE_PARAM)) {
                                label = propFields[1];
                            }
                        }
                    }
                }
                RowData newRow = new RowData(subtype, val, preferred, label);
                newRow.protocol = proto;
                ims.add(newRow);
            }
        });
    }

    /**
     * 重置变量
     */
    private void reset() {
        if (peopleData == null) {
            peopleData = new PeopleData();
        } else {
            peopleData.clear();
        }
        parseLen = 0;
        notes = null;
        if (phones == null)
            phones = new ArrayList<RowData>();
        else
            phones.clear();
        if (emails == null)
            emails = new ArrayList<RowData>();
        else
            emails.clear();
        if (addrs == null)
            addrs = new ArrayList<RowData>();
        else
            addrs.clear();
        if (orgs == null)
            orgs = new ArrayList<OrgData>();
        else
            orgs.clear();
        if (ims == null)
            ims = new ArrayList<RowData>();
        else
            ims.clear();
    }

    final static Pattern[] phonePatterns = {
            Pattern.compile("[+](1)(\\d\\d\\d)(\\d\\d\\d)(\\d\\d\\d\\d.*)"),
            Pattern.compile("[+](972)(2|3|4|8|9|50|52|54|57|59|77)(\\d\\d\\d)(\\d\\d\\d\\d.*)"),};

    /**
     * 转换电话号码格式，以"-"分隔
     *
     * @param phone 电话号码
     * @return 分割后的电话号码
     */
    String toCanonicalPhone(String phone) {
        // for (final Pattern phonePattern : phonePatterns) {
        // Matcher m = phonePattern.matcher(phone);
        // if (m.matches()) {
        // return "+" + m.group(1) + "-" + m.group(2) + "-" + m.group(3) + "-" +
        // m.group(4);
        // }
        // }
        // Log.i("Contact", "CanonicalPhone: " + phone);
        return phone;
    }

    /**
     * 设置联系人标识
     */
    public void setId(String id) {
        peopleData._id = id;
    }

    /**
     * 获取联系人标识
     */
    public long getId() {
        return Long.parseLong(peopleData._id);
    }

    final static Pattern beginPattern = Pattern.compile("BEGIN:VCARD*", Pattern.CASE_INSENSITIVE);
    final static Pattern propPattern = Pattern.compile("([^:]+):(.*)");
    final static Pattern propParamPattern = Pattern.compile("([^;=]+)(=([^;]+))?(;|$)");
    final static Pattern base64Pattern = Pattern.compile("\\s*([a-zA-Z0-9+/]+={0,2})\\s*$");
    final static Pattern namePattern = Pattern.compile("(([^,]+),(.*))|((.*?)\\s+(\\S+))");
    final static Pattern birthdayPattern = Pattern.compile("^" + BIRTHDAY_FIELD
            + ":\\s*([^;]+)(;\\s*|\\s*$)", Pattern.CASE_INSENSITIVE);

    public long getParseLen() {
        return parseLen;
    }

    /**
     * 从手机中获取通讯录
     */
    public void getContactInfoFromPhone(String id, ContentResolver cResolver) {
        reset();
        peopleData._id = id;
        // 取出联系人姓名和_id
        getPeopleFields(cResolver);
        // 取出联系人NOTES
        getNoteFields(cResolver);
        // 取出电话号码
        getPhoneFields(cResolver);
        // 取出email地址
        getEmailFields(cResolver);
        // 取出邮政编码
        getAddressFields(cResolver);
        // 取出IMS(即时通讯方式)
        getImFields(cResolver);
        // 设置工作单位
        getOrgFields(cResolver);
    }

    /**
     * 将通讯录写入到vcf文件中
     */
    public void writeVCard(Appendable vCardBuff) throws IOException {
        vCardBuff.append("BEGIN:VCARD").append(NL);
        vCardBuff.append("VERSION:2.1").append(NL);

        formatPeople(vCardBuff, peopleData);
        for (RowData email : emails) {
            formatEmail(vCardBuff, email);
        }

        for (RowData phone : phones) {
            formatPhone(vCardBuff, phone);
        }

        for (RowData addr : addrs) {
            formatAddr(vCardBuff, addr);
        }

        for (RowData im : ims) {
            formatIM(vCardBuff, im);
        }
        for (OrgData org : orgs) {
            formatOrg(vCardBuff, org);
        }
        if (notes != null && notes.length() > 0) {
            appendField(vCardBuff, "NOTE", notes);
        }
        vCardBuff.append("END:VCARD").append(NL);
    }

    /**
     * 将email地址格式化为vcard字段
     *
     * @param cardBuff 将格式化后的email地址扩充到该缓冲
     * @param email    email地址原型
     */
    public static void formatEmail(Appendable cardBuff, RowData email) throws IOException {
        cardBuff.append("EMAIL");
        if (email.preferred)
            cardBuff.append(";PREF");

        if (email.customType != null) {
            cardBuff.append(";" + TYPE_PARAM + "=");
            cardBuff.append(email.customType);
        }
        switch (email.type) {
            case Email.TYPE_WORK:
                cardBuff.append(";WORK");
                break;
            case Email.TYPE_HOME:
                cardBuff.append(";HOME");
        }

        if (!ContactStrUtils.isASCII(email.data))
            cardBuff.append(";CHARSET=UTF-8");

        cardBuff.append(":").append(email.data.trim()).append(NL);
    }

    /**
     * 将电话号码格式化为vcard字段
     *
     * @param formatted 格式化后的电话号码字段追加到formatted缓冲
     * @param phone     电话号码原型
     */
    public static void formatPhone(Appendable formatted, RowData phone) throws IOException {
        formatted.append("TEL");
        if (phone.preferred)
            formatted.append(";PREF");

        if (phone.customType != null) {
            formatted.append(";" + TYPE_PARAM + "=");
            formatted.append(phone.customType);
        }
        switch (phone.type) {
            case Phone.TYPE_HOME:
                formatted.append(";VOICE");
                break;
            case Phone.TYPE_WORK:
                formatted.append(";VOICE;WORK");
                break;
            case Phone.TYPE_FAX_WORK:
                formatted.append(";FAX;WORK");
                break;
            case Phone.TYPE_FAX_HOME:
                formatted.append(";FAX;HOME");
                break;
            case Phone.TYPE_MOBILE:
                formatted.append(";CELL");
                break;
            case Phone.TYPE_PAGER:
                formatted.append(";PAGER");
                break;
        }

        if (!ContactStrUtils.isASCII(phone.data))
            formatted.append(";CHARSET=UTF-8");
        formatted.append(":").append(phone.data.trim()).append(NL);
    }

    /**
     * 将地址格式化为vcard字段
     *
     * @param formatted 格式化后的地址被追加到该缓冲
     * @param addr      地址
     */
    public static void formatAddr(Appendable formatted, RowData addr) throws IOException {
        formatted.append("ADR");
        if (addr.preferred)
            formatted.append(";PREF");

        if (addr.customType != null) {
            formatted.append(";" + TYPE_PARAM + "=");
            formatted.append(addr.customType);
        }

        switch (addr.type) {
            case StructuredPostal.TYPE_HOME:
                formatted.append(";HOME");
                break;
            case StructuredPostal.TYPE_WORK:
                formatted.append(";WORK");
                break;
        }
        if (!ContactStrUtils.isASCII(addr.data))
            formatted.append(";CHARSET=UTF-8");
        formatted.append(":").append(addr.data.replace(", ", ";").trim()).append(NL);
    }

    /**
     * 将IM格式化为vcard字段
     *
     * @param formatted 格式化后的IM被追加到该缓冲
     * @param im      即时通讯
     */
    public static void formatIM(Appendable formatted, RowData im) throws IOException {
        formatted.append(IMPROP);
        if (im.preferred)
            formatted.append(";PREF");

        if (im.customType != null) {
            formatted.append(";" + TYPE_PARAM + "=");
            formatted.append(im.customType);
        }

        switch (im.type) {
            case Im.TYPE_HOME:
                formatted.append(";HOME");
                break;
            case Im.TYPE_WORK:
                formatted.append(";WORK");
                break;
        }

        if (im.protocol != null) {
            // formatted.append(";").append(PROTO_PARAM).append("=").append(im.protocol);
            formatted.append(im.protocol);
        }

        if (!ContactStrUtils.isASCII(im.data)) {
            formatted.append(";CHARSET=UTF-8");
        }

        // formatted.append(";HOME");

        formatted.append(":").append(im.data.trim()).append(NL);
    }

    /**
     * 将联系人所在单位格式化为vcard字段
     *
     * @param formatted 格式化后的数据被追加到该缓冲
     * @param org      单位信息
     */
    public static void formatOrg(Appendable formatted, OrgData org) throws IOException {
        if (org.company != null) {
            formatted.append("ORG");
            if (org.customType != null) {
                formatted.append(";" + TYPE_PARAM + "=");
                formatted.append(org.customType);
            }
            if (!ContactStrUtils.isASCII(org.company))
                formatted.append(";CHARSET=UTF-8");
            formatted.append(":").append(org.company.trim()).append(NL);
            if (org.title == null)
                formatted.append("TITLE:").append(NL);
        }
        if (org.title != null) {
            if (org.company == null)
                formatted.append("ORG:").append(NL);
            formatted.append("TITLE");
            if (org.customType != null) {
                formatted.append(";" + TYPE_PARAM + "=");
                formatted.append(org.customType);
            }
            if (!ContactStrUtils.isASCII(org.title))
                formatted.append(";CHARSET=UTF-8");
            formatted.append(":").append(org.title.trim()).append(NL);
        }
    }

    /**
     * 将联系人格式为vcard字段
     *
     * @param formatted 格式化后的数据追加到缓冲
     * @param peoData   联系人信息
     * @throws IOException
     */
    public static void formatPeople(Appendable formatted, PeopleData peoData) throws IOException {
        formatted.append("N");
        if (!ContactStrUtils.isASCII(peoData.midName)
                || !ContactStrUtils.isASCII(peoData.familyNmae))
            formatted.append(";CHARSET=UTF-8");
        if (peoData.midName != null) {
            formatted.append(":")
                    .append((peoData.givenName != null) ? peoData.givenName.trim() : "")
                    .append(";")
                    .append((peoData.familyNmae != null) ? peoData.familyNmae.trim() : "")
                    .append(";").append((peoData.midName != null) ? peoData.midName.trim() : "")
                    .append(";").append(";").append(NL);
        } else {
            formatted.append(":")
                    .append((peoData.familyNmae != null) ? peoData.familyNmae.trim() : "")
                    .append(";")
                    .append((peoData.givenName != null) ? peoData.givenName.trim() : "")
                    .append(";").append(";").append(";").append(NL);
        }
        formatted.append("FN");
        if (peoData.displayName != null) {
            if (!ContactStrUtils.isASCII(peoData.displayName))
                formatted.append(";CHARSET=UTF-8");
            formatted.append(":").append(peoData.displayName.trim());
        } else {
            formatted.append("");
        }
        formatted.append(NL);
    }

    /**
     * 从手机中取出工作单位信息
     *
     * @param cResolver
     */
    private void getOrgFields(ContentResolver cResolver) {
        String customType = null;
        // 从Data中取出Organization数据
        String[] projection = {Organization.TITLE, Organization.COMPANY, Organization.TYPE,
                Organization.LABEL};
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);
        if (orgCur != null && orgCur.moveToFirst()) {
            do {
                String title = orgCur.getString(0);
                String company = orgCur.getString(1);
                int type = orgCur.getInt(2);
                if (type == Organization.TYPE_CUSTOM) {
                    customType = orgCur.getString(3);
                }
                OrgData newOrg = new OrgData(type, title, company, customType);
                orgs.add(newOrg);
            } while (orgCur.moveToNext());
        }
        orgCur.close();
    }

    /**
     * 从手机中取出IM(即时通讯方式)
     *
     * @param cResolver
     */
    private void getImFields(ContentResolver cResolver) {
        // 从Data中取出Im数据
        String customType = null;
        String[] projection = {Im.DATA, Im.TYPE, Im.IS_PRIMARY, Im.LABEL, Im.PROTOCOL,
                Im.CUSTOM_PROTOCOL};
        String imWhere = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] imWhereParams = new String[]{peopleData._id, Im.CONTENT_ITEM_TYPE};
        Cursor imCur = cResolver.query(Data.CONTENT_URI, projection, imWhere, imWhereParams, null);
        if (imCur != null && imCur.moveToFirst()) {
            do {
                String data = imCur.getString(0);
                int type = imCur.getInt(1);
                int primary = imCur.getInt(2);
                if (type == Im.TYPE_CUSTOM) {
                    customType = imCur.getString(3);
                }
                RowData newRow = new RowData(type, data, primary != 0, customType);
                String proNumber = imCur.getString(4);
                Log.i("proNumber", proNumber);
                if (proNumber != null) {
                    int proNum = Integer.parseInt(proNumber);
                    if (proNum >= 0 && proNum <= 7) {
                        newRow.protocol = PROTO[proNum];
                    } else {
                        newRow.protocol = "UNKNOW-PROTO";
                    }
                } else {
                    newRow.protocol = imCur.getString(5);
                }
                ims.add(newRow);
            } while (imCur.moveToNext());
        }
        imCur.close();
    }

    /**
     * 从手机中取出邮政和地址
     */
    private void getAddressFields(ContentResolver cResolver) {
        String customType = null;
        // 从Data中取出StructurePostal相互局
        String[] projection = {StructuredPostal.POBOX, StructuredPostal.STREET,
                StructuredPostal.CITY, StructuredPostal.REGION, StructuredPostal.POSTCODE,
                StructuredPostal.COUNTRY, StructuredPostal.IS_PRIMARY, StructuredPostal.TYPE,
                StructuredPostal.LABEL};
        StringBuffer dataBuff = new StringBuffer();
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor postalCur = cResolver
                .query(Data.CONTENT_URI, projection, selection, selParams, null);
        if (postalCur != null && postalCur.moveToFirst()) {
            do {
                customType = null;
                // 邮箱
                String poBox = postalCur.getString(0);
                if (poBox != null) {
                    dataBuff.append(poBox);
                }
                dataBuff.append(", ");
                // 邻居(未填写)
                dataBuff.append(", ");
                // 街道
                String street = postalCur.getString(1);
                if (street != null) {
                    dataBuff.append(street);
                }
                dataBuff.append(", ");
                // 城市
                String city = postalCur.getString(2);
                if (city != null) {
                    dataBuff.append(city);
                }
                dataBuff.append(", ");
                // 区域
                String region = postalCur.getString(3);
                if (region != null) {
                    dataBuff.append(region);
                }
                dataBuff.append(", ");
                // 邮政编码
                String postalCode = postalCur.getString(4);
                if (postalCode != null) {
                    dataBuff.append(postalCode);
                }
                dataBuff.append(", ");
                // 国家
                String country = postalCur.getString(5);
                if (country != null) {
                    dataBuff.append(country);
                }
                int primary = postalCur.getInt(6);
                int type = postalCur.getInt(7);
                if (type == StructuredPostal.TYPE_CUSTOM) {
                    customType = postalCur.getString(8);
                }
                addrs.add(new RowData(type, dataBuff.toString(), primary != 0, customType));
                Log.i("postal", dataBuff + "  addr.num=" + addrs.size());
            } while (postalCur.moveToNext());
        }
        postalCur.close();
    }

    /**
     * 从手机中取出email地址
     */
    private void getEmailFields(ContentResolver cResolver) {
        String customType = null;
        // 从Data中取出Email数据
        String[] projection = {Email.DATA, Email.TYPE, Email.IS_PRIMARY, Email.LABEL};
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, Email.CONTENT_ITEM_TYPE};
        Cursor emailCur = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);
        if (emailCur != null && emailCur.moveToFirst()) {
            do {
                String data = emailCur.getString(0);
                int type = emailCur.getInt(1);
                int primary = emailCur.getInt(2);
                if (type == Email.TYPE_CUSTOM) {
                    customType = emailCur.getString(3);
                }
                emails.add(new RowData(type, data, primary != 0, customType));
                Log.i("email", data);
            } while (emailCur.moveToNext());
        }
        emailCur.close();
    }

    /**
     * 从手机中取出联系人姓名
     */
    private void getPeopleFields(ContentResolver cResolver) {
        // 从Data中取出StructuredName数据
        String[] projection = {StructuredName.DISPLAY_NAME, StructuredName.FAMILY_NAME,
                StructuredName.MIDDLE_NAME, StructuredName.GIVEN_NAME};
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, StructuredName.CONTENT_ITEM_TYPE};
        Cursor cur = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);
        if (cur != null && cur.moveToFirst()) {
            peopleData.displayName = cur.getString(0);
            peopleData.familyNmae = cur.getString(1);
            peopleData.midName = null;
            peopleData.givenName = cur.getString(3);
        }
        cur.close();
    }

    /**
     * 从手机中取出Notes信息
     */
    private void getNoteFields(ContentResolver cResolver) {
        // 获取备注信息,(未解决)
        // 从Data中取出Note数据
        String[] projection = {Note.NOTE, Note.IS_PRIMARY};
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, Note.CONTENT_ITEM_TYPE};
        Cursor cursor = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);
        if (cursor != null && cursor.moveToFirst()) {
            notes = cursor.getString(0);
            Log.i("note", notes);
            if (notes != null) {
                Matcher ppm = birthdayPattern.matcher(notes);

                if (ppm.find()) {
                    notes = ppm.replaceFirst("");
                }
            }
        }
        cursor.close();
    }

    /**
     * 从手机中取出电话号码
     */
    private void getPhoneFields(ContentResolver cResolver) {
        String[] projection = {Phone.NUMBER, Phone.TYPE, Phone.IS_PRIMARY, Phone.LABEL};
        String selection = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] selParams = new String[]{peopleData._id, Phone.CONTENT_ITEM_TYPE};
        Cursor cur = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);
        Log.i("count = ", "" + cur.getCount());
        if (cur.moveToFirst()) {
            do {
                String customType = null;
                String phone = cur.getString(0);
                int phoneType = cur.getInt(1);
                boolean preferred = cur.getInt(2) != 0;
                if (phoneType == Phone.TYPE_CUSTOM) {
                    customType = cur.getString(3);
                }
                RowData rowData = new RowData(phoneType, phone, preferred, customType);
                phones.add(rowData);
                Log.i("phone", "" + phone);
            } while (cur.moveToNext());
        }
        cur.close();
    }

    /**
     * 追加到文件尾部
     */
    private static void appendField(Appendable out, String name, String val) throws IOException {
        if (val != null && val.length() > 0) {
            out.append(name);
            if (!ContactStrUtils.isASCII(val))
                out.append(";CHARSET=UTF-8");
            out.append(":").append(val).append(NL);
        }
    }

    /**
     * 解析vcard
     */
    public long parseVCard(BufferedReader vCard) throws IOException {
        reset();
        // 读取BEGIN
        String line = vCard.readLine();
        if (line == null) {
            return -1;
        }
        while (line != null && !beginPattern.matcher(line).matches()) {
            // Log.i("parse vCard", line +
            // getLineNum(Thread.currentThread().getStackTrace()[2]));
            parseLen += line.length();
            line = vCard.readLine();
        }
        // 读取内容
        while (line != null) {
            line = vCard.readLine();
            Log.i("parse vCard", line);
            if (line == null) {
                return -1;
            }
            vCard.mark(1);
            vCard.reset();
            parseLen += line.length();

            Matcher pm = propPattern.matcher(line);

            if (pm.matches()) {
                String prop = pm.group(1);
                String val = pm.group(2);

                // 判断是否完成一个联系人的解析
                if (prop.equalsIgnoreCase("END") && val.equalsIgnoreCase("VCARD")) {
                    return parseLen;
                }

                // 解析属性
                Matcher ppm = propParamPattern.matcher(prop);
                if (!ppm.find())
                    continue;

                String propName = ppm.group(1).toUpperCase();
                Vector<String> propVec = new Vector<String>();
                String charSet = "UTF-8";
                String encoding = "";
                // 字符集和编码方式处理
                while (ppm.find()) {
                    String param = ppm.group(1);
                    String paramVal = ppm.group(3);
                    propVec.add(param + (paramVal != null ? "=" + paramVal : ""));
                    if (param.equalsIgnoreCase("CHARSET")) {
                        charSet = paramVal;
                    } else if (param.equalsIgnoreCase("ENCODING")) {
                        encoding = paramVal;
                    }
                }
                if (encoding.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                    try {
                        val = QuotedPrintable.decode(val.getBytes(charSet), "UTF-8");
                    } catch (UnsupportedEncodingException uee) {

                    }
                }
                handleProp propHandler = propHandlers.get(propName);
                if (propHandler != null)
                    propHandler.parseProp(propName, propVec, val);
            }
        }
        return -1;
    }

    /**
     * 根据key删除联系人信息
     *
     * @param cResolver
     * @param key       key>0 根据id删除指定联系人,key=0删除所有联系人
     */
    private void RemoveContact(ContentResolver cResolver, long key) {
        // 移除已有联系人
        if (key != 0) {
            cResolver.delete(RawContacts.CONTENT_URI,
                    RawContacts._ID + " = " + key, null);
        } else {
            cResolver.delete(RawContacts.CONTENT_URI, null, null);
        }
    }

    /**
     * 加入新联系人信息
     *
     * @param context
     * @param key     存在的联系人所在行
     * @param replace 是否覆盖相同的联系人
     * @return 插入的列所在行
     */
    public long addContact(Context context, long key, boolean replace) {
        ContentResolver cResolver = context.getContentResolver();
        String[] projection = {StructuredName.DISPLAY_NAME, StructuredName.CONTACT_ID};
        String selection = Data.MIMETYPE + " = ? AND " + StructuredName.DISPLAY_NAME + " = ?";
        String[] selParams = new String[]{StructuredName.CONTENT_ITEM_TYPE,
                peopleData.displayName};
        Cursor cursor = cResolver.query(Data.CONTENT_URI, projection, selection, selParams, null);

        if (cursor != null && cursor.moveToFirst()) {
            Log.i("people.getString(0)", cursor.getString(0));
            if (replace) {
                do {
                    setId(cursor.getString(1));
                    key = getId();
                    RemoveContact(cResolver, key);
                    Log.i("Contact", "OverwriteContact");
                } while (cursor.moveToNext());
            } else {
                cursor.close();
                return 0;
            }
        }
        cursor.close();
        // 插入空值
        ContentValues values = new ContentValues();
        Uri rawContactUri = cResolver.insert(RawContacts.CONTENT_URI, values);
        // 获取_id值
        setId(ContentUris.parseId(rawContactUri) + "");
        // 插入联系人姓名
        insertPeopleValue(cResolver, Data.CONTENT_URI, getPeopleCV());

        // Log.i("Insert Contact", "insert phone number"
        // + getLineNum(Thread.currentThread().getStackTrace()[2]));
        // 插入电话号码
        for (RowData phone : phones) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getPhoneCV(phone));
        }
        Log.i("Insert Contact", "insert email");
        // email地址
        for (RowData email : emails) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getEmailCV(email));
        }
        Log.i("Insert Contact", "insert address");
        // 地址
        for (RowData addr : addrs) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getAddressCV(addr));
        }
        Log.i("Insert Contact", "insert ims");
        // IMs
        for (RowData im : ims) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getImCV(im));
        }
        // 工作单位
        Log.i("Insert Contact", "insert org");
        for (OrgData org : orgs) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getOrgnizeCV(org));
        }
        // 备注信息
        Log.i("Insert Contact", "insert note");
        if (notes != null) {
            insertPeopleValue(cResolver, Data.CONTENT_URI, getNotesCV(null));
        }
        return key;
    }

    /**
     * 将vcf文件中获取联系人姓名信息填入ContentValues
     *
     * @return ContentValues 联系人姓名信息
     */
    public ContentValues getPeopleCV() {
        ContentValues cv = new ContentValues();
        // 类型
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        cv.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        // 数据
        if (peopleData.familyNmae != null)
            cv.put(StructuredName.FAMILY_NAME, peopleData.familyNmae);
        if (peopleData.midName != null) {
            cv.put(StructuredName.MIDDLE_NAME, peopleData.midName);
        }
        if (peopleData.givenName != null) {
            cv.put(StructuredName.GIVEN_NAME, peopleData.givenName);
        }
        Log.i("peopleData.displayName", peopleData.displayName);
        cv.put(StructuredName.DISPLAY_NAME, peopleData.displayName);
        return cv;
    }

    /**
     * 将vcf文件中获取联系人工作单位信息填入ContentValues
     *
     * @return ContentValues 联系人工作单位信息
     */
    public ContentValues getOrgnizeCV(OrgData org) {

        if (ContactStrUtils.isNullOrEmpty(org.company) && ContactStrUtils.isNullOrEmpty(org.title)) {
            return null;
        }
        ContentValues cv = new ContentValues();
        // 类型
        cv.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        // 数据
        cv.put(Organization.COMPANY, org.company);
        cv.put(Organization.TITLE, org.title);
        cv.put(Organization.TYPE, org.type);
        if (org.customType != null) {
            cv.put(Organization.LABEL, org.customType);
        }
        return cv;
    }

    /**
     * 将vcf文件中获取电话号码信息填入ContentValues
     *
     * @return ContentValues 联系人电话信息
     */
    public ContentValues getPhoneCV(RowData data) {
        ContentValues cv = new ContentValues();

        // 类型
        cv.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        // 数据
        cv.put(Phone.NUMBER, data.data);
        cv.put(Phone.TYPE, data.type);
        cv.put(Phone.IS_PRIMARY, data.preferred ? 1 : 0);
        if (data.customType != null) {
            cv.put(Phone.LABEL, data.customType);
        }

        return cv;
    }

    /**
     * 将vcf文件中获取email地址信息填入ContentValues
     *
     * @return ContentValues 联系人Emial信息
     */
    public ContentValues getEmailCV(RowData data) {
        ContentValues cv = new ContentValues();

        // 类型
        cv.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        // 数据
        cv.put(Email.DATA, data.data);
        cv.put(Email.TYPE, data.type);
        cv.put(Email.IS_PRIMARY, data.preferred ? 1 : 0);
        if (data.customType != null) {
            cv.put(Email.LABEL, data.customType);
        }

        return cv;
    }

    /**
     * 将vcf文件中获取地址信息填入ContentValues
     *
     * @return ContentValues 联系人地址信息
     */
    public ContentValues getAddressCV(RowData data) {
        ContentValues cv = new ContentValues();
        // 类型
        cv.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        // 数据
        String[] addressFields = ContactStrUtils.split(data.data, ";");
        int maxLen = addressFields.length;
        // 国家
        if (addressFields[maxLen - 1] != null) {
            cv.put(StructuredPostal.COUNTRY, addressFields[maxLen - 1]);
        }
        // 邮政编码
        if (addressFields[maxLen - 2] != null) {
            cv.put(StructuredPostal.POSTCODE, addressFields[maxLen - 2]);
        }
        // 区域
        if (addressFields[maxLen - 3] != null) {
            cv.put(StructuredPostal.REGION, addressFields[maxLen - 3]);
        }
        // 城市
        if (addressFields[maxLen - 4] != null) {
            cv.put(StructuredPostal.CITY, addressFields[maxLen - 4]);
        }
        // 街道
        if (addressFields[maxLen - 5] != null) {
            cv.put(StructuredPostal.STREET, addressFields[maxLen - 5]);
        }
        if (addressFields[maxLen - 6] != null) {
            Log.i("getAddressCV", "no neighborhood");
        }
        // 邮箱
        if (addressFields[maxLen - 7] != null) {
            cv.put(StructuredPostal.POBOX, addressFields[maxLen - 7]);
        }
        cv.put(StructuredPostal.TYPE, data.type);
        cv.put(StructuredPostal.IS_PRIMARY, data.preferred ? 1 : 0);
        if (data.customType != null) {
            cv.put(StructuredPostal.LABEL, data.customType);
        }

        return cv;
    }

    /**
     * 将vcf文件中获取IM信息填入ContentValues
     *
     * @return ContentValues 联系人Im(即时通讯方式)信息
     */
    public ContentValues getImCV(RowData data) {
        ContentValues cv = new ContentValues();

        // 类型
        cv.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
        cv.put(Data.RAW_CONTACT_ID, peopleData._id);
        // 数据
        cv.put(Im.DATA, data.data);
        cv.put(Im.TYPE, data.type);
        cv.put(Im.IS_PRIMARY, data.preferred ? 1 : 0);
        if (data.customType != null) {
            cv.put(Im.LABEL, data.customType);
        }

        if (data.protocol != null) {
            int protoNum = -1;
            for (int i = 0; i < PROTO.length; ++i) {
                if (data.protocol.equalsIgnoreCase(PROTO[i])) {
                    protoNum = i;
                    break;
                }
            }
            if (protoNum >= 0) {
                cv.put(Im.PROTOCOL, protoNum);
            } else {
                cv.put(Im.CUSTOM_PROTOCOL, data.protocol);
            }
        }

        return cv;
    }

    /**
     * 将vcf文件中获取备注信息填入ContentValues
     *
     * @return ContentValues 联系人备注信息
     */
    public ContentValues getNotesCV(RowData data) {
        ContentValues cv = new ContentValues();

        // 类型
        cv.put(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
        cv.put(Note.RAW_CONTACT_ID, peopleData._id);
        // 数据
        cv.put(Note.NOTE, notes);
        return cv;
    }

    /**
     * 插入到电话本
     */
    private Uri insertPeopleValue(ContentResolver cResolver, Uri uri, ContentValues cv) {
        if (cv != null) {
            return cResolver.insert(uri, cv);
        }
        return null;
    }

    // /**
    // * return the line of log
    // * */
    // public String getLineNum(StackTraceElement statck) {
    // String line = " --LINE: ";
    // return line + statck.getLineNumber() + "--";
    // }
}
