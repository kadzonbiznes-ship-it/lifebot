/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.driver.windows.wmi.Win32Processor$VoltProperty
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.wmi.Win32Processor;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32Processor {
    private static final String WIN32_PROCESSOR = "Win32_Processor";

    private Win32Processor() {
    }

    public static WbemcliUtil.WmiResult<VoltProperty> queryVoltage() {
        WbemcliUtil.WmiQuery<VoltProperty> voltQuery = new WbemcliUtil.WmiQuery<VoltProperty>(WIN32_PROCESSOR, VoltProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(voltQuery);
    }

    public static WbemcliUtil.WmiResult<ProcessorIdProperty> queryProcessorId() {
        WbemcliUtil.WmiQuery<ProcessorIdProperty> idQuery = new WbemcliUtil.WmiQuery<ProcessorIdProperty>(WIN32_PROCESSOR, ProcessorIdProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(idQuery);
    }

    public static WbemcliUtil.WmiResult<BitnessProperty> queryBitness() {
        WbemcliUtil.WmiQuery<BitnessProperty> bitnessQuery = new WbemcliUtil.WmiQuery<BitnessProperty>(WIN32_PROCESSOR, BitnessProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(bitnessQuery);
    }

    public static enum ProcessorIdProperty {
        PROCESSORID;

    }

    public static enum BitnessProperty {
        ADDRESSWIDTH;

    }
}

