/*
 * Decompiled with CFR 0.152.
 */
package javax.accessibility;

import java.util.Vector;
import javax.accessibility.AccessibleRelation;

public class AccessibleRelationSet {
    protected Vector<AccessibleRelation> relations = null;

    public AccessibleRelationSet() {
        this.relations = null;
    }

    public AccessibleRelationSet(AccessibleRelation[] relations) {
        if (relations.length != 0) {
            this.relations = new Vector(relations.length);
            for (int i = 0; i < relations.length; ++i) {
                this.add(relations[i]);
            }
        }
    }

    public boolean add(AccessibleRelation relation) {
        int i;
        AccessibleRelation existingRelation;
        if (this.relations == null) {
            this.relations = new Vector();
        }
        if ((existingRelation = this.get(relation.getKey())) == null) {
            this.relations.addElement(relation);
            return true;
        }
        Object[] existingTarget = existingRelation.getTarget();
        Object[] newTarget = relation.getTarget();
        int mergedLength = existingTarget.length + newTarget.length;
        Object[] mergedTarget = new Object[mergedLength];
        for (i = 0; i < existingTarget.length; ++i) {
            mergedTarget[i] = existingTarget[i];
        }
        i = existingTarget.length;
        int j = 0;
        while (i < mergedLength) {
            mergedTarget[i] = newTarget[j];
            ++i;
            ++j;
        }
        existingRelation.setTarget(mergedTarget);
        return true;
    }

    public void addAll(AccessibleRelation[] relations) {
        if (relations.length != 0) {
            if (this.relations == null) {
                this.relations = new Vector(relations.length);
            }
            for (int i = 0; i < relations.length; ++i) {
                this.add(relations[i]);
            }
        }
    }

    public boolean remove(AccessibleRelation relation) {
        if (this.relations == null) {
            return false;
        }
        return this.relations.removeElement(relation);
    }

    public void clear() {
        if (this.relations != null) {
            this.relations.removeAllElements();
        }
    }

    public int size() {
        if (this.relations == null) {
            return 0;
        }
        return this.relations.size();
    }

    public boolean contains(String key) {
        return this.get(key) != null;
    }

    public AccessibleRelation get(String key) {
        if (this.relations == null) {
            return null;
        }
        int len = this.relations.size();
        for (int i = 0; i < len; ++i) {
            AccessibleRelation relation = this.relations.elementAt(i);
            if (relation == null || !relation.getKey().equals(key)) continue;
            return relation;
        }
        return null;
    }

    public AccessibleRelation[] toArray() {
        if (this.relations == null) {
            return new AccessibleRelation[0];
        }
        AccessibleRelation[] relationArray = new AccessibleRelation[this.relations.size()];
        for (int i = 0; i < relationArray.length; ++i) {
            relationArray[i] = this.relations.elementAt(i);
        }
        return relationArray;
    }

    public String toString() {
        Object ret = "";
        if (this.relations != null && this.relations.size() > 0) {
            ret = this.relations.elementAt(0).toDisplayString();
            for (int i = 1; i < this.relations.size(); ++i) {
                ret = (String)ret + "," + this.relations.elementAt(i).toDisplayString();
            }
        }
        return ret;
    }
}

