/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class StrikeMetrics {
    public float ascentX;
    public float ascentY;
    public float descentX;
    public float descentY;
    public float baselineX;
    public float baselineY;
    public float leadingX;
    public float leadingY;
    public float maxAdvanceX;
    public float maxAdvanceY;

    StrikeMetrics() {
        this.ascentY = 2.1474836E9f;
        this.ascentX = 2.1474836E9f;
        this.leadingY = -2.1474836E9f;
        this.leadingX = -2.1474836E9f;
        this.descentY = -2.1474836E9f;
        this.descentX = -2.1474836E9f;
        this.maxAdvanceY = -2.1474836E9f;
        this.maxAdvanceX = -2.1474836E9f;
        this.baselineY = -2.1474836E9f;
        this.baselineX = -2.1474836E9f;
    }

    StrikeMetrics(float ax, float ay, float dx, float dy, float bx, float by, float lx, float ly, float mx, float my) {
        this.ascentX = ax;
        this.ascentY = ay;
        this.descentX = dx;
        this.descentY = dy;
        this.baselineX = bx;
        this.baselineY = by;
        this.leadingX = lx;
        this.leadingY = ly;
        this.maxAdvanceX = mx;
        this.maxAdvanceY = my;
    }

    public float getAscent() {
        return -this.ascentY;
    }

    public float getDescent() {
        return this.descentY;
    }

    public float getLeading() {
        return this.leadingY;
    }

    public float getMaxAdvance() {
        return this.maxAdvanceX;
    }

    void merge(StrikeMetrics other) {
        if (other == null) {
            return;
        }
        if (other.ascentX < this.ascentX) {
            this.ascentX = other.ascentX;
        }
        if (other.ascentY < this.ascentY) {
            this.ascentY = other.ascentY;
        }
        if (other.descentX > this.descentX) {
            this.descentX = other.descentX;
        }
        if (other.descentY > this.descentY) {
            this.descentY = other.descentY;
        }
        if (other.baselineX > this.baselineX) {
            this.baselineX = other.baselineX;
        }
        if (other.baselineY > this.baselineY) {
            this.baselineY = other.baselineY;
        }
        if (other.leadingX > this.leadingX) {
            this.leadingX = other.leadingX;
        }
        if (other.leadingY > this.leadingY) {
            this.leadingY = other.leadingY;
        }
        if (other.maxAdvanceX > this.maxAdvanceX) {
            this.maxAdvanceX = other.maxAdvanceX;
        }
        if (other.maxAdvanceY > this.maxAdvanceY) {
            this.maxAdvanceY = other.maxAdvanceY;
        }
    }

    void convertToUserSpace(AffineTransform invTx) {
        Point2D.Float pt2D = new Point2D.Float();
        pt2D.x = this.ascentX;
        pt2D.y = this.ascentY;
        invTx.deltaTransform(pt2D, pt2D);
        this.ascentX = pt2D.x;
        this.ascentY = pt2D.y;
        pt2D.x = this.descentX;
        pt2D.y = this.descentY;
        invTx.deltaTransform(pt2D, pt2D);
        this.descentX = pt2D.x;
        this.descentY = pt2D.y;
        pt2D.x = this.baselineX;
        pt2D.y = this.baselineY;
        invTx.deltaTransform(pt2D, pt2D);
        this.baselineX = pt2D.x;
        this.baselineY = pt2D.y;
        pt2D.x = this.leadingX;
        pt2D.y = this.leadingY;
        invTx.deltaTransform(pt2D, pt2D);
        this.leadingX = pt2D.x;
        this.leadingY = pt2D.y;
        pt2D.x = this.maxAdvanceX;
        pt2D.y = this.maxAdvanceY;
        invTx.deltaTransform(pt2D, pt2D);
        this.maxAdvanceX = pt2D.x;
        this.maxAdvanceY = pt2D.y;
    }

    public String toString() {
        return "ascent:x=" + this.ascentX + " y=" + this.ascentY + " descent:x=" + this.descentX + " y=" + this.descentY + " baseline:x=" + this.baselineX + " y=" + this.baselineY + " leading:x=" + this.leadingX + " y=" + this.leadingY + " maxAdvance:x=" + this.maxAdvanceX + " y=" + this.maxAdvanceY;
    }
}

