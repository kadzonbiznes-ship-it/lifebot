/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class WbemcliUtil {
    public static final WbemcliUtil INSTANCE = new WbemcliUtil();
    public static final String DEFAULT_NAMESPACE = "ROOT\\CIMV2";

    public static boolean hasNamespace(String namespace) {
        String ns = namespace;
        if (namespace.toUpperCase().startsWith("ROOT\\")) {
            ns = namespace.substring(5);
        }
        WmiQuery<NamespaceProperty> namespaceQuery = new WmiQuery<NamespaceProperty>("ROOT", "__NAMESPACE", NamespaceProperty.class);
        WmiResult<NamespaceProperty> namespaces = namespaceQuery.execute();
        for (int i = 0; i < namespaces.getResultCount(); ++i) {
            if (!ns.equalsIgnoreCase((String)namespaces.getValue(NamespaceProperty.NAME, i))) continue;
            return true;
        }
        return false;
    }

    public static Wbemcli.IWbemServices connectServer(String namespace) {
        Wbemcli.IWbemLocator loc = Wbemcli.IWbemLocator.create();
        if (loc == null) {
            throw new COMException("Failed to create WbemLocator object.");
        }
        Wbemcli.IWbemServices services = loc.ConnectServer(namespace, null, null, null, 0, null, null);
        loc.Release();
        WinNT.HRESULT hres = Ole32.INSTANCE.CoSetProxyBlanket(services, 10, 0, null, 3, 3, null, 0);
        if (COMUtils.FAILED(hres)) {
            services.Release();
            throw new COMException("Could not set proxy blanket.", hres);
        }
        return services;
    }

    public static class WmiQuery<T extends Enum<T>> {
        private String nameSpace;
        private String wmiClassName;
        private Class<T> propertyEnum;

        public WmiQuery(String nameSpace, String wmiClassName, Class<T> propertyEnum) {
            this.nameSpace = nameSpace;
            this.wmiClassName = wmiClassName;
            this.propertyEnum = propertyEnum;
        }

        public WmiQuery(String wmiClassName, Class<T> propertyEnum) {
            this(WbemcliUtil.DEFAULT_NAMESPACE, wmiClassName, propertyEnum);
        }

        public Class<T> getPropertyEnum() {
            return this.propertyEnum;
        }

        public String getNameSpace() {
            return this.nameSpace;
        }

        public void setNameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
        }

        public String getWmiClassName() {
            return this.wmiClassName;
        }

        public void setWmiClassName(String wmiClassName) {
            this.wmiClassName = wmiClassName;
        }

        public WmiResult<T> execute() {
            try {
                return this.execute(-1);
            }
            catch (TimeoutException e) {
                throw new COMException("Got a WMI timeout when infinite wait was specified. This should never happen.");
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public WmiResult<T> execute(int timeout) throws TimeoutException {
            if (((Enum[])this.getPropertyEnum().getEnumConstants()).length < 1) {
                throw new IllegalArgumentException("The query's property enum has no values.");
            }
            Wbemcli.IWbemServices svc = WbemcliUtil.connectServer(this.getNameSpace());
            try {
                WmiResult<T> wmiResult;
                Wbemcli.IEnumWbemClassObject enumerator = WmiQuery.selectProperties(svc, this);
                try {
                    wmiResult = WmiQuery.enumerateProperties(enumerator, this.getPropertyEnum(), timeout);
                }
                catch (Throwable throwable) {
                    enumerator.Release();
                    throw throwable;
                }
                enumerator.Release();
                return wmiResult;
            }
            finally {
                svc.Release();
            }
        }

        private static <T extends Enum<T>> Wbemcli.IEnumWbemClassObject selectProperties(Wbemcli.IWbemServices svc, WmiQuery<T> query) {
            Enum[] props = (Enum[])query.getPropertyEnum().getEnumConstants();
            StringBuilder sb = new StringBuilder("SELECT ");
            sb.append(props[0].name());
            for (int i = 1; i < props.length; ++i) {
                sb.append(',').append(props[i].name());
            }
            sb.append(" FROM ").append(query.getWmiClassName());
            return svc.ExecQuery("WQL", sb.toString().replaceAll("\\\\", "\\\\\\\\"), 48, null);
        }

        private static <T extends Enum<T>> WmiResult<T> enumerateProperties(Wbemcli.IEnumWbemClassObject enumerator, Class<T> propertyEnum, int timeout) throws TimeoutException {
            WbemcliUtil wbemcliUtil = INSTANCE;
            Objects.requireNonNull(wbemcliUtil);
            WmiResult<T> values = wbemcliUtil.new WmiResult<T>(propertyEnum);
            Pointer[] pclsObj = new Pointer[1];
            IntByReference uReturn = new IntByReference(0);
            HashMap<Enum, WString> wstrMap = new HashMap<Enum, WString>();
            WinNT.HRESULT hres = null;
            for (Enum property : (Enum[])propertyEnum.getEnumConstants()) {
                wstrMap.put(property, new WString(property.name()));
            }
            while (enumerator.getPointer() != Pointer.NULL && (hres = enumerator.Next(timeout, pclsObj.length, pclsObj, uReturn)).intValue() != 1 && hres.intValue() != 262149) {
                if (hres.intValue() == 262148) {
                    throw new TimeoutException("No results after " + timeout + " ms.");
                }
                if (COMUtils.FAILED(hres)) {
                    throw new COMException("Failed to enumerate results.", hres);
                }
                Variant.VARIANT.ByReference pVal = new Variant.VARIANT.ByReference();
                IntByReference pType = new IntByReference();
                Wbemcli.IWbemClassObject clsObj = new Wbemcli.IWbemClassObject(pclsObj[0]);
                for (Enum property : (Enum[])propertyEnum.getEnumConstants()) {
                    clsObj.Get((WString)wstrMap.get(property), 0, pVal, pType, null);
                    int vtType = (pVal.getValue() == null ? Integer.valueOf(1) : pVal.getVarType()).intValue();
                    int cimType = pType.getValue();
                    switch (vtType) {
                        case 8: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.stringValue());
                            break;
                        }
                        case 3: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.intValue());
                            break;
                        }
                        case 17: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.byteValue());
                            break;
                        }
                        case 2: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.shortValue());
                            break;
                        }
                        case 11: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.booleanValue());
                            break;
                        }
                        case 4: {
                            ((WmiResult)values).add(vtType, cimType, property, Float.valueOf(pVal.floatValue()));
                            break;
                        }
                        case 5: {
                            ((WmiResult)values).add(vtType, cimType, property, pVal.doubleValue());
                            break;
                        }
                        case 0: 
                        case 1: {
                            ((WmiResult)values).add(vtType, cimType, property, null);
                            break;
                        }
                        default: {
                            if ((vtType & 0x2000) == 8192 || (vtType & 0xD) == 13 || (vtType & 9) == 9 || (vtType & 0x1000) == 4096) {
                                ((WmiResult)values).add(vtType, cimType, property, null);
                                break;
                            }
                            ((WmiResult)values).add(vtType, cimType, property, pVal.getValue());
                        }
                    }
                    OleAuto.INSTANCE.VariantClear(pVal);
                }
                clsObj.Release();
                ((WmiResult)values).incrementResultCount();
            }
            return values;
        }
    }

    private static enum NamespaceProperty {
        NAME;

    }

    public class WmiResult<T extends Enum<T>> {
        private Map<T, List<Object>> propertyMap;
        private Map<T, Integer> vtTypeMap;
        private Map<T, Integer> cimTypeMap;
        private int resultCount = 0;

        public WmiResult(Class<T> propertyEnum) {
            this.propertyMap = new EnumMap<T, List<Object>>(propertyEnum);
            this.vtTypeMap = new EnumMap<T, Integer>(propertyEnum);
            this.cimTypeMap = new EnumMap<T, Integer>(propertyEnum);
            for (Enum prop : (Enum[])propertyEnum.getEnumConstants()) {
                this.propertyMap.put(prop, new ArrayList());
                this.vtTypeMap.put(prop, 1);
                this.cimTypeMap.put(prop, 0);
            }
        }

        public Object getValue(T property, int index) {
            return this.propertyMap.get(property).get(index);
        }

        public int getVtType(T property) {
            return this.vtTypeMap.get(property);
        }

        public int getCIMType(T property) {
            return this.cimTypeMap.get(property);
        }

        private void add(int vtType, int cimType, T property, Object o) {
            this.propertyMap.get(property).add(o);
            if (vtType != 1 && this.vtTypeMap.get(property).equals(1)) {
                this.vtTypeMap.put(property, vtType);
            }
            if (this.cimTypeMap.get(property).equals(0)) {
                this.cimTypeMap.put(property, cimType);
            }
        }

        public int getResultCount() {
            return this.resultCount;
        }

        private void incrementResultCount() {
            ++this.resultCount;
        }
    }
}

