/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import com.sun.org.apache.xerces.internal.util.PrimeNumberSequenceGenerator;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.xml.internal.stream.XMLBufferListener;

public class XMLAttributesImpl
implements XMLAttributes,
XMLBufferListener {
    protected static final int TABLE_SIZE = 101;
    protected static final int MAX_HASH_COLLISIONS = 40;
    protected static final int MULTIPLIERS_SIZE = 32;
    protected static final int MULTIPLIERS_MASK = 31;
    protected static final int SIZE_LIMIT = 20;
    protected boolean fNamespaces = true;
    protected int fLargeCount = 1;
    protected int fLength;
    protected Attribute[] fAttributes = new Attribute[4];
    protected Attribute[] fAttributeTableView;
    protected int[] fAttributeTableViewChainState;
    protected int fTableViewBuckets;
    protected boolean fIsTableViewConsistent;
    protected int[] fHashMultipliers;

    public XMLAttributesImpl() {
        this(101);
    }

    public XMLAttributesImpl(int tableSize) {
        this.fTableViewBuckets = tableSize;
        for (int i = 0; i < this.fAttributes.length; ++i) {
            this.fAttributes[i] = new Attribute();
        }
    }

    public void setNamespaces(boolean namespaces) {
        this.fNamespaces = namespaces;
    }

    @Override
    public int addAttribute(QName name, String type, String value) {
        return this.addAttribute(name, type, value, null);
    }

    public int addAttribute(QName name, String type, String value, XMLString valueCache) {
        int index;
        if (this.fLength < 20) {
            int n = index = name.uri != null && name.uri.length() != 0 ? this.getIndexFast(name.uri, name.localpart) : this.getIndexFast(name.rawname);
            if (index == -1) {
                index = this.fLength;
                if (this.fLength++ == this.fAttributes.length) {
                    Attribute[] attributes = new Attribute[this.fAttributes.length + 4];
                    System.arraycopy(this.fAttributes, 0, attributes, 0, this.fAttributes.length);
                    for (int i = this.fAttributes.length; i < attributes.length; ++i) {
                        attributes[i] = new Attribute();
                    }
                    this.fAttributes = attributes;
                }
            }
        } else if (name.uri == null || name.uri.length() == 0 || (index = this.getIndexFast(name.uri, name.localpart)) == -1) {
            int bucket;
            if (!this.fIsTableViewConsistent || this.fLength == 20 || this.fLength > 20 && this.fLength > this.fTableViewBuckets) {
                this.prepareAndPopulateTableView();
                this.fIsTableViewConsistent = true;
            }
            if (this.fAttributeTableViewChainState[bucket = this.getTableViewBucket(name.rawname)] != this.fLargeCount) {
                index = this.fLength;
                if (this.fLength++ == this.fAttributes.length) {
                    Attribute[] attributes = new Attribute[this.fAttributes.length << 1];
                    System.arraycopy(this.fAttributes, 0, attributes, 0, this.fAttributes.length);
                    for (int i = this.fAttributes.length; i < attributes.length; ++i) {
                        attributes[i] = new Attribute();
                    }
                    this.fAttributes = attributes;
                }
                this.fAttributeTableViewChainState[bucket] = this.fLargeCount;
                this.fAttributes[index].next = null;
                this.fAttributeTableView[bucket] = this.fAttributes[index];
            } else {
                int collisionCount = 0;
                Attribute found = this.fAttributeTableView[bucket];
                while (found != null && found.name.rawname != name.rawname) {
                    found = found.next;
                    ++collisionCount;
                }
                if (found == null) {
                    index = this.fLength;
                    if (this.fLength++ == this.fAttributes.length) {
                        Attribute[] attributes = new Attribute[this.fAttributes.length << 1];
                        System.arraycopy(this.fAttributes, 0, attributes, 0, this.fAttributes.length);
                        for (int i = this.fAttributes.length; i < attributes.length; ++i) {
                            attributes[i] = new Attribute();
                        }
                        this.fAttributes = attributes;
                    }
                    if (collisionCount >= 40) {
                        this.fAttributes[index].name.setValues(name);
                        this.rebalanceTableView(this.fLength);
                    } else {
                        this.fAttributes[index].next = this.fAttributeTableView[bucket];
                        this.fAttributeTableView[bucket] = this.fAttributes[index];
                    }
                } else {
                    index = this.getIndexFast(name.rawname);
                }
            }
        }
        Attribute attribute = this.fAttributes[index];
        attribute.name.setValues(name);
        attribute.type = type;
        attribute.value = value;
        attribute.xmlValue = valueCache;
        attribute.nonNormalizedValue = value;
        attribute.specified = false;
        if (attribute.augs != null) {
            attribute.augs.removeAllItems();
        }
        return index;
    }

    @Override
    public void removeAllAttributes() {
        this.fLength = 0;
    }

    @Override
    public void removeAttributeAt(int attrIndex) {
        this.fIsTableViewConsistent = false;
        if (attrIndex < this.fLength - 1) {
            Attribute removedAttr = this.fAttributes[attrIndex];
            System.arraycopy(this.fAttributes, attrIndex + 1, this.fAttributes, attrIndex, this.fLength - attrIndex - 1);
            this.fAttributes[this.fLength - 1] = removedAttr;
        }
        --this.fLength;
    }

    @Override
    public void setName(int attrIndex, QName attrName) {
        this.fAttributes[attrIndex].name.setValues(attrName);
    }

    @Override
    public void getName(int attrIndex, QName attrName) {
        attrName.setValues(this.fAttributes[attrIndex].name);
    }

    @Override
    public void setType(int attrIndex, String attrType) {
        this.fAttributes[attrIndex].type = attrType;
    }

    @Override
    public void setValue(int attrIndex, String attrValue) {
        this.setValue(attrIndex, attrValue, null);
    }

    @Override
    public void setValue(int attrIndex, String attrValue, XMLString value) {
        Attribute attribute = this.fAttributes[attrIndex];
        attribute.value = attrValue;
        attribute.nonNormalizedValue = attrValue;
        attribute.xmlValue = value;
    }

    @Override
    public void setNonNormalizedValue(int attrIndex, String attrValue) {
        if (attrValue == null) {
            attrValue = this.fAttributes[attrIndex].value;
        }
        this.fAttributes[attrIndex].nonNormalizedValue = attrValue;
    }

    @Override
    public String getNonNormalizedValue(int attrIndex) {
        String value = this.fAttributes[attrIndex].nonNormalizedValue;
        return value;
    }

    @Override
    public void setSpecified(int attrIndex, boolean specified) {
        this.fAttributes[attrIndex].specified = specified;
    }

    @Override
    public boolean isSpecified(int attrIndex) {
        return this.fAttributes[attrIndex].specified;
    }

    @Override
    public int getLength() {
        return this.fLength;
    }

    @Override
    public String getType(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        return this.getReportableType(this.fAttributes[index].type);
    }

    @Override
    public String getType(String qname) {
        int index = this.getIndex(qname);
        return index != -1 ? this.getReportableType(this.fAttributes[index].type) : null;
    }

    @Override
    public String getValue(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        if (this.fAttributes[index].value == null && this.fAttributes[index].xmlValue != null) {
            this.fAttributes[index].value = this.fAttributes[index].xmlValue.toString();
        }
        return this.fAttributes[index].value;
    }

    @Override
    public String getValue(String qname) {
        int index = this.getIndex(qname);
        if (index == -1) {
            return null;
        }
        if (this.fAttributes[index].value == null) {
            this.fAttributes[index].value = this.fAttributes[index].xmlValue.toString();
        }
        return this.fAttributes[index].value;
    }

    public String getName(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        return this.fAttributes[index].name.rawname;
    }

    @Override
    public int getIndex(String qName) {
        for (int i = 0; i < this.fLength; ++i) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.rawname == null || !attribute.name.rawname.equals(qName)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int getIndex(String uri, String localPart) {
        for (int i = 0; i < this.fLength; ++i) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart == null || !attribute.name.localpart.equals(localPart) || uri != attribute.name.uri && (uri == null || attribute.name.uri == null || !attribute.name.uri.equals(uri))) continue;
            return i;
        }
        return -1;
    }

    public int getIndexByLocalName(String localPart) {
        for (int i = 0; i < this.fLength; ++i) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart == null || !attribute.name.localpart.equals(localPart)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public String getLocalName(int index) {
        if (!this.fNamespaces) {
            return "";
        }
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        return this.fAttributes[index].name.localpart;
    }

    @Override
    public String getQName(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        String rawname = this.fAttributes[index].name.rawname;
        return rawname != null ? rawname : "";
    }

    @Override
    public QName getQualifiedName(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        return this.fAttributes[index].name;
    }

    @Override
    public String getType(String uri, String localName) {
        if (!this.fNamespaces) {
            return null;
        }
        int index = this.getIndex(uri, localName);
        return index != -1 ? this.getType(index) : null;
    }

    public int getIndexFast(String qName) {
        for (int i = 0; i < this.fLength; ++i) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.rawname != qName) continue;
            return i;
        }
        return -1;
    }

    public void addAttributeNS(QName name, String type, String value) {
        int index = this.fLength;
        if (this.fLength++ == this.fAttributes.length) {
            Attribute[] attributes = this.fLength < 20 ? new Attribute[this.fAttributes.length + 4] : new Attribute[this.fAttributes.length << 1];
            System.arraycopy(this.fAttributes, 0, attributes, 0, this.fAttributes.length);
            for (int i = this.fAttributes.length; i < attributes.length; ++i) {
                attributes[i] = new Attribute();
            }
            this.fAttributes = attributes;
        }
        Attribute attribute = this.fAttributes[index];
        attribute.name.setValues(name);
        attribute.type = type;
        attribute.value = value;
        attribute.nonNormalizedValue = value;
        attribute.specified = false;
        attribute.augs.removeAllItems();
    }

    public QName checkDuplicatesNS() {
        int length = this.fLength;
        if (length <= 20) {
            Attribute[] attributes = this.fAttributes;
            for (int i = 0; i < length - 1; ++i) {
                Attribute att1 = attributes[i];
                for (int j = i + 1; j < length; ++j) {
                    Attribute att2 = attributes[j];
                    if (att1.name.localpart != att2.name.localpart || att1.name.uri != att2.name.uri) continue;
                    return att2.name;
                }
            }
            return null;
        }
        return this.checkManyDuplicatesNS();
    }

    private QName checkManyDuplicatesNS() {
        this.fIsTableViewConsistent = false;
        this.prepareTableView();
        int length = this.fLength;
        Attribute[] attributes = this.fAttributes;
        Attribute[] attributeTableView = this.fAttributeTableView;
        int[] attributeTableViewChainState = this.fAttributeTableViewChainState;
        int largeCount = this.fLargeCount;
        for (int i = 0; i < length; ++i) {
            Attribute attr = attributes[i];
            int bucket = this.getTableViewBucket(attr.name.localpart, attr.name.uri);
            if (attributeTableViewChainState[bucket] != largeCount) {
                attributeTableViewChainState[bucket] = largeCount;
                attr.next = null;
                attributeTableView[bucket] = attr;
                continue;
            }
            int collisionCount = 0;
            Attribute found = attributeTableView[bucket];
            while (found != null) {
                if (found.name.localpart == attr.name.localpart && found.name.uri == attr.name.uri) {
                    return attr.name;
                }
                found = found.next;
                ++collisionCount;
            }
            if (collisionCount >= 40) {
                this.rebalanceTableViewNS(i + 1);
                largeCount = this.fLargeCount;
                continue;
            }
            attr.next = attributeTableView[bucket];
            attributeTableView[bucket] = attr;
        }
        return null;
    }

    public int getIndexFast(String uri, String localPart) {
        for (int i = 0; i < this.fLength; ++i) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart != localPart || attribute.name.uri != uri) continue;
            return i;
        }
        return -1;
    }

    private String getReportableType(String type) {
        if (type.charAt(0) == '(') {
            return "NMTOKEN";
        }
        return type;
    }

    protected int getTableViewBucket(String qname) {
        return (this.hash(qname) & Integer.MAX_VALUE) % this.fTableViewBuckets;
    }

    protected int getTableViewBucket(String localpart, String uri) {
        if (uri == null) {
            return (this.hash(localpart) & Integer.MAX_VALUE) % this.fTableViewBuckets;
        }
        return (this.hash(localpart, uri) & Integer.MAX_VALUE) % this.fTableViewBuckets;
    }

    private int hash(String localpart) {
        if (this.fHashMultipliers == null) {
            return localpart.hashCode();
        }
        return this.hash0(localpart);
    }

    private int hash(String localpart, String uri) {
        if (this.fHashMultipliers == null) {
            return localpart.hashCode() + uri.hashCode() * 31;
        }
        return this.hash0(localpart) + this.hash0(uri) * this.fHashMultipliers[32];
    }

    private int hash0(String symbol) {
        int code = 0;
        int length = symbol.length();
        int[] multipliers = this.fHashMultipliers;
        for (int i = 0; i < length; ++i) {
            code = code * multipliers[i & 0x1F] + symbol.charAt(i);
        }
        return code;
    }

    protected void cleanTableView() {
        if (++this.fLargeCount < 0) {
            if (this.fAttributeTableViewChainState != null) {
                for (int i = this.fTableViewBuckets - 1; i >= 0; --i) {
                    this.fAttributeTableViewChainState[i] = 0;
                }
            }
            this.fLargeCount = 1;
        }
    }

    private void growTableView() {
        int length = this.fLength;
        int tableViewBuckets = this.fTableViewBuckets;
        do {
            if ((tableViewBuckets = (tableViewBuckets << 1) + 1) >= 0) continue;
            tableViewBuckets = Integer.MAX_VALUE;
            break;
        } while (length > tableViewBuckets);
        this.fTableViewBuckets = tableViewBuckets;
        this.fAttributeTableView = null;
        this.fLargeCount = 1;
    }

    protected void prepareTableView() {
        if (this.fLength > this.fTableViewBuckets) {
            this.growTableView();
        }
        if (this.fAttributeTableView == null) {
            this.fAttributeTableView = new Attribute[this.fTableViewBuckets];
            this.fAttributeTableViewChainState = new int[this.fTableViewBuckets];
        } else {
            this.cleanTableView();
        }
    }

    protected void prepareAndPopulateTableView() {
        this.prepareAndPopulateTableView(this.fLength);
    }

    private void prepareAndPopulateTableView(int count) {
        this.prepareTableView();
        for (int i = 0; i < count; ++i) {
            Attribute attr = this.fAttributes[i];
            int bucket = this.getTableViewBucket(attr.name.rawname);
            if (this.fAttributeTableViewChainState[bucket] != this.fLargeCount) {
                this.fAttributeTableViewChainState[bucket] = this.fLargeCount;
                attr.next = null;
                this.fAttributeTableView[bucket] = attr;
                continue;
            }
            attr.next = this.fAttributeTableView[bucket];
            this.fAttributeTableView[bucket] = attr;
        }
    }

    @Override
    public String getPrefix(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        String prefix = this.fAttributes[index].name.prefix;
        return prefix != null ? prefix : "";
    }

    @Override
    public String getURI(int index) {
        if (index < 0 || index >= this.fLength) {
            return null;
        }
        String uri = this.fAttributes[index].name.uri;
        return uri;
    }

    @Override
    public String getValue(String uri, String localName) {
        int index = this.getIndex(uri, localName);
        return index != -1 ? this.getValue(index) : null;
    }

    @Override
    public Augmentations getAugmentations(String uri, String localName) {
        int index = this.getIndex(uri, localName);
        return index != -1 ? this.fAttributes[index].augs : null;
    }

    @Override
    public Augmentations getAugmentations(String qName) {
        int index = this.getIndex(qName);
        return index != -1 ? this.fAttributes[index].augs : null;
    }

    @Override
    public Augmentations getAugmentations(int attributeIndex) {
        if (attributeIndex < 0 || attributeIndex >= this.fLength) {
            return null;
        }
        return this.fAttributes[attributeIndex].augs;
    }

    @Override
    public void setAugmentations(int attrIndex, Augmentations augs) {
        this.fAttributes[attrIndex].augs = augs;
    }

    public void setURI(int attrIndex, String uri) {
        this.fAttributes[attrIndex].name.uri = uri;
    }

    @Override
    public void refresh() {
        if (this.fLength > 0) {
            for (int i = 0; i < this.fLength; ++i) {
                this.getValue(i);
            }
        }
    }

    @Override
    public void refresh(int pos) {
    }

    private void prepareAndPopulateTableViewNS(int count) {
        this.prepareTableView();
        for (int i = 0; i < count; ++i) {
            Attribute attr = this.fAttributes[i];
            int bucket = this.getTableViewBucket(attr.name.localpart, attr.name.uri);
            if (this.fAttributeTableViewChainState[bucket] != this.fLargeCount) {
                this.fAttributeTableViewChainState[bucket] = this.fLargeCount;
                attr.next = null;
                this.fAttributeTableView[bucket] = attr;
                continue;
            }
            attr.next = this.fAttributeTableView[bucket];
            this.fAttributeTableView[bucket] = attr;
        }
    }

    private void rebalanceTableView(int count) {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[33];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        this.prepareAndPopulateTableView(count);
    }

    private void rebalanceTableViewNS(int count) {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[33];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        this.prepareAndPopulateTableViewNS(count);
    }

    static class Attribute {
        public final QName name = new QName();
        public String type;
        public String value;
        public XMLString xmlValue;
        public String nonNormalizedValue;
        public boolean specified;
        public Augmentations augs = new AugmentationsImpl();
        public Attribute next;

        Attribute() {
        }
    }
}

