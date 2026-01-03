/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.parser;

import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.List;
import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BaseType;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.ReturnType;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;

public class SignatureParser {
    private String input;
    private int index;
    private int mark;
    private static final char EOI = ':';
    private static final boolean DEBUG = false;

    private SignatureParser() {
    }

    private void init(String s) {
        this.input = s;
        this.index = 0;
        this.mark = 0;
    }

    private char current() {
        assert (this.index <= this.input.length());
        return this.index < this.input.length() ? this.input.charAt(this.index) : (char)':';
    }

    private void advance() {
        assert (this.index <= this.input.length());
        if (this.index < this.input.length()) {
            ++this.index;
        }
    }

    private void mark() {
        this.mark = this.index;
    }

    private String remainder() {
        return this.input.substring(this.index);
    }

    private String markToCurrent() {
        return this.input.substring(this.mark, this.index);
    }

    private Error error(String errorMsg) {
        return new GenericSignatureFormatError("Signature Parse error: " + errorMsg + "\n\tRemaining input: " + this.remainder());
    }

    private void progress(int startingPosition) {
        if (this.index <= startingPosition) {
            throw this.error("Failure to make progress!");
        }
    }

    public static SignatureParser make() {
        return new SignatureParser();
    }

    public ClassSignature parseClassSig(String s) {
        this.init(s);
        return this.parseClassSignature();
    }

    public MethodTypeSignature parseMethodSig(String s) {
        this.init(s);
        return this.parseMethodTypeSignature();
    }

    public TypeSignature parseTypeSig(String s) {
        this.init(s);
        return this.parseTypeSignature();
    }

    private ClassSignature parseClassSignature() {
        assert (this.index == 0);
        return ClassSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseClassTypeSignature(), this.parseSuperInterfaces());
    }

    private FormalTypeParameter[] parseZeroOrMoreFormalTypeParameters() {
        if (this.current() == '<') {
            return this.parseFormalTypeParameters();
        }
        return new FormalTypeParameter[0];
    }

    private FormalTypeParameter[] parseFormalTypeParameters() {
        ArrayList<FormalTypeParameter> ftps = new ArrayList<FormalTypeParameter>(3);
        assert (this.current() == '<');
        if (this.current() != '<') {
            throw this.error("expected '<'");
        }
        this.advance();
        ftps.add(this.parseFormalTypeParameter());
        while (this.current() != '>') {
            int startingPosition = this.index;
            ftps.add(this.parseFormalTypeParameter());
            this.progress(startingPosition);
        }
        this.advance();
        return ftps.toArray(new FormalTypeParameter[ftps.size()]);
    }

    private FormalTypeParameter parseFormalTypeParameter() {
        String id = this.parseIdentifier();
        FieldTypeSignature[] bs = this.parseBounds();
        return FormalTypeParameter.make(id, bs);
    }

    private String parseIdentifier() {
        this.mark();
        this.skipIdentifier();
        return this.markToCurrent();
    }

    private void skipIdentifier() {
        char c = this.current();
        while (c != ';' && c != '.' && c != '/' && c != '[' && c != ':' && c != '>' && c != '<' && !Character.isWhitespace(c)) {
            this.advance();
            c = this.current();
        }
    }

    private FieldTypeSignature parseFieldTypeSignature() {
        return this.parseFieldTypeSignature(true);
    }

    private FieldTypeSignature parseFieldTypeSignature(boolean allowArrays) {
        switch (this.current()) {
            case 'L': {
                return this.parseClassTypeSignature();
            }
            case 'T': {
                return this.parseTypeVariableSignature();
            }
            case '[': {
                if (allowArrays) {
                    return this.parseArrayTypeSignature();
                }
                throw this.error("Array signature not allowed here.");
            }
        }
        throw this.error("Expected Field Type Signature");
    }

    private ClassTypeSignature parseClassTypeSignature() {
        assert (this.current() == 'L');
        if (this.current() != 'L') {
            throw this.error("expected a class type");
        }
        this.advance();
        ArrayList<SimpleClassTypeSignature> scts = new ArrayList<SimpleClassTypeSignature>(5);
        scts.add(this.parsePackageNameAndSimpleClassTypeSignature());
        this.parseClassTypeSignatureSuffix(scts);
        if (this.current() != ';') {
            throw this.error("expected ';' got '" + this.current() + "'");
        }
        this.advance();
        return ClassTypeSignature.make(scts);
    }

    private SimpleClassTypeSignature parsePackageNameAndSimpleClassTypeSignature() {
        this.mark();
        this.skipIdentifier();
        while (this.current() == '/') {
            this.advance();
            this.skipIdentifier();
        }
        String id = this.markToCurrent().replace('/', '.');
        switch (this.current()) {
            case ';': {
                return SimpleClassTypeSignature.make(id, false, new TypeArgument[0]);
            }
            case '<': {
                return SimpleClassTypeSignature.make(id, false, this.parseTypeArguments());
            }
        }
        throw this.error("expected '<' or ';' but got " + this.current());
    }

    private SimpleClassTypeSignature parseSimpleClassTypeSignature(boolean dollar) {
        String id = this.parseIdentifier();
        char c = this.current();
        switch (c) {
            case '.': 
            case ';': {
                return SimpleClassTypeSignature.make(id, dollar, new TypeArgument[0]);
            }
            case '<': {
                return SimpleClassTypeSignature.make(id, dollar, this.parseTypeArguments());
            }
        }
        throw this.error("expected '<' or ';' or '.', got '" + c + "'.");
    }

    private void parseClassTypeSignatureSuffix(List<SimpleClassTypeSignature> scts) {
        while (this.current() == '.') {
            this.advance();
            scts.add(this.parseSimpleClassTypeSignature(true));
        }
    }

    private TypeArgument[] parseTypeArguments() {
        ArrayList<TypeArgument> tas = new ArrayList<TypeArgument>(3);
        assert (this.current() == '<');
        if (this.current() != '<') {
            throw this.error("expected '<'");
        }
        this.advance();
        tas.add(this.parseTypeArgument());
        while (this.current() != '>') {
            tas.add(this.parseTypeArgument());
        }
        this.advance();
        return tas.toArray(new TypeArgument[tas.size()]);
    }

    private TypeArgument parseTypeArgument() {
        FieldTypeSignature[] ub = new FieldTypeSignature[1];
        FieldTypeSignature[] lb = new FieldTypeSignature[1];
        TypeArgument[] ta = new TypeArgument[]{};
        char c = this.current();
        switch (c) {
            case '+': {
                this.advance();
                ub[0] = this.parseFieldTypeSignature();
                lb[0] = BottomSignature.make();
                return Wildcard.make(ub, lb);
            }
            case '*': {
                this.advance();
                ub[0] = SimpleClassTypeSignature.make("java.lang.Object", false, ta);
                lb[0] = BottomSignature.make();
                return Wildcard.make(ub, lb);
            }
            case '-': {
                this.advance();
                lb[0] = this.parseFieldTypeSignature();
                ub[0] = SimpleClassTypeSignature.make("java.lang.Object", false, ta);
                return Wildcard.make(ub, lb);
            }
        }
        return this.parseFieldTypeSignature();
    }

    private TypeVariableSignature parseTypeVariableSignature() {
        assert (this.current() == 'T');
        if (this.current() != 'T') {
            throw this.error("expected a type variable usage");
        }
        this.advance();
        TypeVariableSignature ts = TypeVariableSignature.make(this.parseIdentifier());
        if (this.current() != ';') {
            throw this.error("; expected in signature of type variable named" + ts.getIdentifier());
        }
        this.advance();
        return ts;
    }

    private ArrayTypeSignature parseArrayTypeSignature() {
        if (this.current() != '[') {
            throw this.error("expected array type signature");
        }
        this.advance();
        return ArrayTypeSignature.make(this.parseTypeSignature());
    }

    private TypeSignature parseTypeSignature() {
        switch (this.current()) {
            case 'B': 
            case 'C': 
            case 'D': 
            case 'F': 
            case 'I': 
            case 'J': 
            case 'S': 
            case 'Z': {
                return this.parseBaseType();
            }
        }
        return this.parseFieldTypeSignature();
    }

    private BaseType parseBaseType() {
        switch (this.current()) {
            case 'B': {
                this.advance();
                return ByteSignature.make();
            }
            case 'C': {
                this.advance();
                return CharSignature.make();
            }
            case 'D': {
                this.advance();
                return DoubleSignature.make();
            }
            case 'F': {
                this.advance();
                return FloatSignature.make();
            }
            case 'I': {
                this.advance();
                return IntSignature.make();
            }
            case 'J': {
                this.advance();
                return LongSignature.make();
            }
            case 'S': {
                this.advance();
                return ShortSignature.make();
            }
            case 'Z': {
                this.advance();
                return BooleanSignature.make();
            }
        }
        assert (false);
        throw this.error("expected primitive type");
    }

    private FieldTypeSignature[] parseBounds() {
        ArrayList<FieldTypeSignature> fts = new ArrayList<FieldTypeSignature>(3);
        if (this.current() == ':') {
            this.advance();
            switch (this.current()) {
                case ':': {
                    break;
                }
                default: {
                    fts.add(this.parseFieldTypeSignature());
                }
            }
            while (this.current() == ':') {
                this.advance();
                fts.add(this.parseFieldTypeSignature());
            }
        } else {
            this.error("Bound expected");
        }
        return fts.toArray(new FieldTypeSignature[fts.size()]);
    }

    private ClassTypeSignature[] parseSuperInterfaces() {
        ArrayList<ClassTypeSignature> cts = new ArrayList<ClassTypeSignature>(5);
        while (this.current() == 'L') {
            cts.add(this.parseClassTypeSignature());
        }
        return cts.toArray(new ClassTypeSignature[cts.size()]);
    }

    private MethodTypeSignature parseMethodTypeSignature() {
        assert (this.index == 0);
        return MethodTypeSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseFormalParameters(), this.parseReturnType(), this.parseZeroOrMoreThrowsSignatures());
    }

    private TypeSignature[] parseFormalParameters() {
        if (this.current() != '(') {
            throw this.error("expected '('");
        }
        this.advance();
        TypeSignature[] pts = this.parseZeroOrMoreTypeSignatures();
        if (this.current() != ')') {
            throw this.error("expected ')'");
        }
        this.advance();
        return pts;
    }

    private TypeSignature[] parseZeroOrMoreTypeSignatures() {
        ArrayList<TypeSignature> ts = new ArrayList<TypeSignature>();
        boolean stop = false;
        block3: while (!stop) {
            switch (this.current()) {
                case 'B': 
                case 'C': 
                case 'D': 
                case 'F': 
                case 'I': 
                case 'J': 
                case 'L': 
                case 'S': 
                case 'T': 
                case 'Z': 
                case '[': {
                    ts.add(this.parseTypeSignature());
                    continue block3;
                }
            }
            stop = true;
        }
        return ts.toArray(new TypeSignature[ts.size()]);
    }

    private ReturnType parseReturnType() {
        if (this.current() == 'V') {
            this.advance();
            return VoidDescriptor.make();
        }
        return this.parseTypeSignature();
    }

    private FieldTypeSignature[] parseZeroOrMoreThrowsSignatures() {
        ArrayList<FieldTypeSignature> ets = new ArrayList<FieldTypeSignature>(3);
        while (this.current() == '^') {
            ets.add(this.parseThrowsSignature());
        }
        return ets.toArray(new FieldTypeSignature[ets.size()]);
    }

    private FieldTypeSignature parseThrowsSignature() {
        assert (this.current() == '^');
        if (this.current() != '^') {
            throw this.error("expected throws signature");
        }
        this.advance();
        return this.parseFieldTypeSignature(false);
    }
}

