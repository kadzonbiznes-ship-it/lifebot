/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.util.platform.windows;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinNT;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.GlobalConfig;
import oshi.util.platform.windows.WmiUtil;

@ThreadSafe
public class WmiQueryHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WmiQueryHandler.class);
    private static int globalTimeout = GlobalConfig.get("oshi.util.wmi.timeout", -1);
    private int wmiTimeout = globalTimeout;
    private final Set<String> failedWmiClassNames = new HashSet<String>();
    private int comThreading = 0;
    private boolean securityInitialized = false;
    private static final Class<?>[] EMPTY_CLASS_ARRAY;
    private static final Object[] EMPTY_OBJECT_ARRAY;
    private static Class<? extends WmiQueryHandler> customClass;

    protected WmiQueryHandler() {
    }

    public static synchronized WmiQueryHandler createInstance() {
        if (customClass == null) {
            return new WmiQueryHandler();
        }
        try {
            return customClass.getConstructor(EMPTY_CLASS_ARRAY).newInstance(EMPTY_OBJECT_ARRAY);
        }
        catch (NoSuchMethodException | SecurityException e) {
            LOG.error("Failed to find or access a no-arg constructor for {}", (Object)customClass);
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            LOG.error("Failed to create a new instance of {}", (Object)customClass);
        }
        return null;
    }

    public static synchronized void setInstanceClass(Class<? extends WmiQueryHandler> instanceClass) {
        customClass = instanceClass;
    }

    public <T extends Enum<T>> WbemcliUtil.WmiResult<T> queryWMI(WbemcliUtil.WmiQuery<T> query) {
        return this.queryWMI(query, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T extends Enum<T>> WbemcliUtil.WmiResult<T> queryWMI(WbemcliUtil.WmiQuery<T> query, boolean initCom) {
        WbemcliUtil wbemcliUtil = WbemcliUtil.INSTANCE;
        Objects.requireNonNull(wbemcliUtil);
        WbemcliUtil.WmiResult<T> result = wbemcliUtil.new WbemcliUtil.WmiResult<T>(query.getPropertyEnum());
        if (this.failedWmiClassNames.contains(query.getWmiClassName())) {
            return result;
        }
        boolean comInit = false;
        try {
            if (initCom) {
                comInit = this.initCOM();
            }
            result = query.execute(this.wmiTimeout);
        }
        catch (COMException e) {
            if (!"ROOT\\OpenHardwareMonitor".equals(query.getNameSpace())) {
                int hresult = e.getHresult() == null ? -1 : e.getHresult().intValue();
                switch (hresult) {
                    case -2147217394: {
                        LOG.warn("COM exception: Invalid Namespace {}", (Object)query.getNameSpace());
                        break;
                    }
                    case -2147217392: {
                        LOG.warn("COM exception: Invalid Class {}", (Object)query.getWmiClassName());
                        break;
                    }
                    case -2147217385: {
                        LOG.warn("COM exception: Invalid Query: {}", (Object)WmiUtil.queryToString(query));
                        break;
                    }
                    default: {
                        this.handleComException(query, e);
                    }
                }
                this.failedWmiClassNames.add(query.getWmiClassName());
            }
        }
        catch (TimeoutException e) {
            LOG.warn("WMI query timed out after {} ms: {}", (Object)this.wmiTimeout, (Object)WmiUtil.queryToString(query));
        }
        finally {
            if (comInit) {
                this.unInitCOM();
            }
        }
        return result;
    }

    protected void handleComException(WbemcliUtil.WmiQuery<?> query, COMException ex) {
        LOG.warn("COM exception querying {}, which might not be on your system. Will not attempt to query it again. Error was {}: {}", query.getWmiClassName(), ex.getHresult() == null ? null : Integer.valueOf(ex.getHresult().intValue()), ex.getMessage());
    }

    public boolean initCOM() {
        boolean comInit = false;
        comInit = this.initCOM(this.getComThreading());
        if (!comInit) {
            comInit = this.initCOM(this.switchComThreading());
        }
        if (comInit && !this.isSecurityInitialized()) {
            WinNT.HRESULT hres = Ole32.INSTANCE.CoInitializeSecurity(null, -1, null, null, 0, 3, null, 0, null);
            if (COMUtils.FAILED(hres) && hres.intValue() != -2147417831) {
                Ole32.INSTANCE.CoUninitialize();
                throw new COMException("Failed to initialize security.", hres);
            }
            this.securityInitialized = true;
        }
        return comInit;
    }

    protected boolean initCOM(int coInitThreading) {
        WinNT.HRESULT hres = Ole32.INSTANCE.CoInitializeEx(null, coInitThreading);
        switch (hres.intValue()) {
            case 0: 
            case 1: {
                return true;
            }
            case -2147417850: {
                return false;
            }
        }
        throw new COMException("Failed to initialize COM library.", hres);
    }

    public void unInitCOM() {
        Ole32.INSTANCE.CoUninitialize();
    }

    public int getComThreading() {
        return this.comThreading;
    }

    public int switchComThreading() {
        this.comThreading = this.comThreading == 2 ? 0 : 2;
        return this.comThreading;
    }

    public boolean isSecurityInitialized() {
        return this.securityInitialized;
    }

    public int getWmiTimeout() {
        return this.wmiTimeout;
    }

    public void setWmiTimeout(int wmiTimeout) {
        this.wmiTimeout = wmiTimeout;
    }

    static {
        if (globalTimeout == 0 || globalTimeout < -1) {
            throw new GlobalConfig.PropertyException("oshi.util.wmi.timeout");
        }
        EMPTY_CLASS_ARRAY = new Class[0];
        EMPTY_OBJECT_ARRAY = new Object[0];
        customClass = null;
    }
}

