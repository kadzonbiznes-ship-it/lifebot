/*
 * Decompiled with CFR 0.152.
 */
package javax.print;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;

public interface PrintService {
    public String getName();

    public DocPrintJob createPrintJob();

    public void addPrintServiceAttributeListener(PrintServiceAttributeListener var1);

    public void removePrintServiceAttributeListener(PrintServiceAttributeListener var1);

    public PrintServiceAttributeSet getAttributes();

    public <T extends PrintServiceAttribute> T getAttribute(Class<T> var1);

    public DocFlavor[] getSupportedDocFlavors();

    public boolean isDocFlavorSupported(DocFlavor var1);

    public Class<?>[] getSupportedAttributeCategories();

    public boolean isAttributeCategorySupported(Class<? extends Attribute> var1);

    public Object getDefaultAttributeValue(Class<? extends Attribute> var1);

    public Object getSupportedAttributeValues(Class<? extends Attribute> var1, DocFlavor var2, AttributeSet var3);

    public boolean isAttributeValueSupported(Attribute var1, DocFlavor var2, AttributeSet var3);

    public AttributeSet getUnsupportedAttributes(DocFlavor var1, AttributeSet var2);

    public ServiceUIFactory getServiceUIFactory();

    public boolean equals(Object var1);

    public int hashCode();
}

