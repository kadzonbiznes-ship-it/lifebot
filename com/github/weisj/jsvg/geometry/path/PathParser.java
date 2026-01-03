/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.geometry.path.CubicBezierCommand
 *  com.github.weisj.jsvg.geometry.path.LineToBezier
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.path.Arc;
import com.github.weisj.jsvg.geometry.path.BezierPathCommand;
import com.github.weisj.jsvg.geometry.path.Cubic;
import com.github.weisj.jsvg.geometry.path.CubicBezierCommand;
import com.github.weisj.jsvg.geometry.path.CubicSmooth;
import com.github.weisj.jsvg.geometry.path.Horizontal;
import com.github.weisj.jsvg.geometry.path.LineTo;
import com.github.weisj.jsvg.geometry.path.LineToBezier;
import com.github.weisj.jsvg.geometry.path.MoveTo;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import com.github.weisj.jsvg.geometry.path.Quadratic;
import com.github.weisj.jsvg.geometry.path.QuadraticSmooth;
import com.github.weisj.jsvg.geometry.path.Terminal;
import com.github.weisj.jsvg.geometry.path.Vertical;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PathParser {
    private final String input;
    private final int inputLength;
    private int index;
    private char currentCommand;

    public PathParser(@Nullable String input) {
        this.input = input;
        this.inputLength = input != null ? input.length() : 0;
    }

    @NotNull
    private String currentLocation() {
        return "(index=" + this.index + " in input=" + this.input + ")";
    }

    private boolean isCommandChar(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    private boolean isWhiteSpaceOrSeparator(char c) {
        return c == ',' || Character.isWhitespace(c);
    }

    private char peek() {
        return this.input.charAt(this.index);
    }

    private void consume() {
        ++this.index;
    }

    private boolean hasNext() {
        return this.index < this.inputLength;
    }

    private boolean isValidNumberChar(char c, NumberCharState state) {
        boolean valid;
        boolean bl = valid = '0' <= c && c <= '9';
        if (valid && state.iteration == 1 && this.input.charAt(this.index - 1) == '0') {
            return false;
        }
        boolean bl2 = state.signAllowed = state.signAllowed && !valid;
        if (state.dotAllowed && !valid) {
            valid = c == '.';
            boolean bl3 = state.dotAllowed = !valid;
        }
        if (state.signAllowed && !valid) {
            state.signAllowed = valid = c == '+' || c == '-';
        }
        if (state.exponentAllowed && !valid) {
            valid = c == 'e' || c == 'E';
            state.exponentAllowed = !valid;
            state.signAllowed = valid;
            state.dotAllowed = !valid;
        }
        ++state.iteration;
        return valid;
    }

    private void consumeWhiteSpaceOrSeparator() {
        while (this.hasNext() && this.isWhiteSpaceOrSeparator(this.peek())) {
            this.consume();
        }
    }

    private float nextFloatOrUnspecified() {
        if (!this.hasNext()) {
            return Float.NaN;
        }
        return this.nextFloat();
    }

    private float nextFloat() {
        int start = this.index;
        NumberCharState state = new NumberCharState();
        while (this.hasNext() && this.isValidNumberChar(this.peek(), state)) {
            this.consume();
        }
        int end = this.index;
        this.consumeWhiteSpaceOrSeparator();
        String token = this.input.substring(start, end);
        try {
            return Float.parseFloat(token);
        }
        catch (NumberFormatException e) {
            String msg = "Unexpected element while parsing cmd '" + this.currentCommand + "' encountered token '" + token + "' rest=" + this.input.substring(start, Math.min(this.input.length(), start + 10)) + this.currentLocation();
            throw new IllegalStateException(msg, e);
        }
    }

    private boolean nextFlag() {
        char c = this.peek();
        this.consume();
        this.consumeWhiteSpaceOrSeparator();
        if (c == '1') {
            return true;
        }
        if (c == '0') {
            return false;
        }
        throw new IllegalStateException("Invalid flag value '" + c + "' " + this.currentLocation());
    }

    @Nullable
    public BezierPathCommand parseMeshCommand() {
        if (this.input == null) {
            return null;
        }
        char peekChar = this.peek();
        this.currentCommand = (char)122;
        if (this.isCommandChar(peekChar)) {
            this.consume();
            this.currentCommand = peekChar;
        }
        this.consumeWhiteSpaceOrSeparator();
        switch (this.currentCommand) {
            case 'l': {
                return new LineToBezier(true, this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'L': {
                return new LineToBezier(false, this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'c': {
                return new CubicBezierCommand(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'C': {
                return new CubicBezierCommand(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
        }
        throw new IllegalStateException("Only commands c C l L allowed");
    }

    public PathCommand[] parsePathCommand() {
        if (this.input == null || "none".equals(this.input)) {
            return new PathCommand[0];
        }
        ArrayList<Terminal> commands = new ArrayList<Terminal>();
        this.currentCommand = (char)90;
        while (this.hasNext()) {
            PathCommand cmd;
            char peekChar = this.peek();
            if (this.isCommandChar(peekChar)) {
                this.consume();
                this.currentCommand = peekChar;
            }
            this.consumeWhiteSpaceOrSeparator();
            switch (this.currentCommand) {
                case 'M': {
                    cmd = new MoveTo(false, this.nextFloat(), this.nextFloat());
                    this.currentCommand = (char)76;
                    break;
                }
                case 'm': {
                    cmd = new MoveTo(true, this.nextFloat(), this.nextFloat());
                    this.currentCommand = (char)108;
                    break;
                }
                case 'L': {
                    cmd = new LineTo(false, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'l': {
                    cmd = new LineTo(true, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'H': {
                    cmd = new Horizontal(false, this.nextFloat());
                    break;
                }
                case 'h': {
                    cmd = new Horizontal(true, this.nextFloat());
                    break;
                }
                case 'V': {
                    cmd = new Vertical(false, this.nextFloat());
                    break;
                }
                case 'v': {
                    cmd = new Vertical(true, this.nextFloat());
                    break;
                }
                case 'A': {
                    cmd = new Arc(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFlag(), this.nextFlag(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'a': {
                    cmd = new Arc(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFlag(), this.nextFlag(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'Q': {
                    cmd = new Quadratic(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'q': {
                    cmd = new Quadratic(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'T': {
                    cmd = new QuadraticSmooth(false, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 't': {
                    cmd = new QuadraticSmooth(true, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'C': {
                    cmd = new Cubic(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'c': {
                    cmd = new Cubic(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'S': {
                    cmd = new CubicSmooth(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 's': {
                    cmd = new CubicSmooth(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'Z': 
                case 'z': {
                    cmd = new Terminal();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid path element " + this.currentCommand + this.currentLocation());
                }
            }
            commands.add((Terminal)cmd);
        }
        return commands.toArray(new PathCommand[0]);
    }

    private static final class NumberCharState {
        int iteration = 0;
        boolean dotAllowed = true;
        boolean signAllowed = true;
        boolean exponentAllowed = true;

        private NumberCharState() {
        }
    }
}

