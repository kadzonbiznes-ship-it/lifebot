/*
 * Decompiled with CFR 0.152.
 */
package java.util.regex;

import java.util.Locale;
import java.util.regex.ASCII;
import java.util.regex.Pattern;

class CharPredicates {
    CharPredicates() {
    }

    static final Pattern.CharPredicate ALPHABETIC() {
        return Character::isAlphabetic;
    }

    static final Pattern.CharPredicate DIGIT() {
        return Character::isDigit;
    }

    static final Pattern.CharPredicate LETTER() {
        return Character::isLetter;
    }

    static final Pattern.CharPredicate IDEOGRAPHIC() {
        return Character::isIdeographic;
    }

    static final Pattern.CharPredicate LOWERCASE() {
        return Character::isLowerCase;
    }

    static final Pattern.CharPredicate UPPERCASE() {
        return Character::isUpperCase;
    }

    static final Pattern.CharPredicate TITLECASE() {
        return Character::isTitleCase;
    }

    static final Pattern.CharPredicate WHITE_SPACE() {
        return ch -> (28672 >> Character.getType(ch) & 1) != 0 || ch >= 9 && ch <= 13 || ch == 133;
    }

    static final Pattern.CharPredicate CONTROL() {
        return ch -> Character.getType(ch) == 15;
    }

    static final Pattern.CharPredicate PUNCTUATION() {
        return ch -> (1643118592 >> Character.getType(ch) & 1) != 0;
    }

    static final Pattern.CharPredicate HEX_DIGIT() {
        return CharPredicates.DIGIT().union(ch -> ch >= 48 && ch <= 57 || ch >= 65 && ch <= 70 || ch >= 97 && ch <= 102 || ch >= 65296 && ch <= 65305 || ch >= 65313 && ch <= 65318 || ch >= 65345 && ch <= 65350);
    }

    static final Pattern.CharPredicate ASSIGNED() {
        return ch -> Character.getType(ch) != 0;
    }

    static final Pattern.CharPredicate NONCHARACTER_CODE_POINT() {
        return ch -> (ch & 0xFFFE) == 65534 || ch >= 64976 && ch <= 65007;
    }

    static final Pattern.CharPredicate ALNUM() {
        return CharPredicates.ALPHABETIC().union(CharPredicates.DIGIT());
    }

    static final Pattern.CharPredicate BLANK() {
        return ch -> Character.getType(ch) == 12 || ch == 9;
    }

    static final Pattern.CharPredicate GRAPH() {
        return ch -> (585729 >> Character.getType(ch) & 1) == 0;
    }

    static final Pattern.CharPredicate PRINT() {
        return CharPredicates.GRAPH().union(CharPredicates.BLANK()).and(CharPredicates.CONTROL().negate());
    }

    static final Pattern.CharPredicate JOIN_CONTROL() {
        return ch -> ch == 8204 || ch == 8205;
    }

    static final Pattern.CharPredicate WORD() {
        return CharPredicates.ALPHABETIC().union(ch -> (8389568 >> Character.getType(ch) & 1) != 0, CharPredicates.JOIN_CONTROL());
    }

    private static Pattern.CharPredicate getPosixPredicate(String name, boolean caseIns) {
        return switch (name) {
            case "ALPHA" -> CharPredicates.ALPHABETIC();
            case "LOWER" -> {
                if (caseIns) {
                    yield CharPredicates.LOWERCASE().union(CharPredicates.UPPERCASE(), CharPredicates.TITLECASE());
                }
                yield CharPredicates.LOWERCASE();
            }
            case "UPPER" -> {
                if (caseIns) {
                    yield CharPredicates.UPPERCASE().union(CharPredicates.LOWERCASE(), CharPredicates.TITLECASE());
                }
                yield CharPredicates.UPPERCASE();
            }
            case "SPACE" -> CharPredicates.WHITE_SPACE();
            case "PUNCT" -> CharPredicates.PUNCTUATION();
            case "XDIGIT" -> CharPredicates.HEX_DIGIT();
            case "ALNUM" -> CharPredicates.ALNUM();
            case "CNTRL" -> CharPredicates.CONTROL();
            case "DIGIT" -> CharPredicates.DIGIT();
            case "BLANK" -> CharPredicates.BLANK();
            case "GRAPH" -> CharPredicates.GRAPH();
            case "PRINT" -> CharPredicates.PRINT();
            default -> null;
        };
    }

    private static Pattern.CharPredicate getUnicodePredicate(String name, boolean caseIns) {
        return switch (name) {
            case "ALPHABETIC" -> CharPredicates.ALPHABETIC();
            case "ASSIGNED" -> CharPredicates.ASSIGNED();
            case "CONTROL" -> CharPredicates.CONTROL();
            case "EMOJI" -> CharPredicates.EMOJI();
            case "EMOJI_PRESENTATION" -> CharPredicates.EMOJI_PRESENTATION();
            case "EMOJI_MODIFIER" -> CharPredicates.EMOJI_MODIFIER();
            case "EMOJI_MODIFIER_BASE" -> CharPredicates.EMOJI_MODIFIER_BASE();
            case "EMOJI_COMPONENT" -> CharPredicates.EMOJI_COMPONENT();
            case "EXTENDED_PICTOGRAPHIC" -> CharPredicates.EXTENDED_PICTOGRAPHIC();
            case "HEXDIGIT", "HEX_DIGIT" -> CharPredicates.HEX_DIGIT();
            case "IDEOGRAPHIC" -> CharPredicates.IDEOGRAPHIC();
            case "JOINCONTROL", "JOIN_CONTROL" -> CharPredicates.JOIN_CONTROL();
            case "LETTER" -> CharPredicates.LETTER();
            case "LOWERCASE" -> {
                if (caseIns) {
                    yield CharPredicates.LOWERCASE().union(CharPredicates.UPPERCASE(), CharPredicates.TITLECASE());
                }
                yield CharPredicates.LOWERCASE();
            }
            case "NONCHARACTERCODEPOINT", "NONCHARACTER_CODE_POINT" -> CharPredicates.NONCHARACTER_CODE_POINT();
            case "TITLECASE" -> {
                if (caseIns) {
                    yield CharPredicates.TITLECASE().union(CharPredicates.LOWERCASE(), CharPredicates.UPPERCASE());
                }
                yield CharPredicates.TITLECASE();
            }
            case "PUNCTUATION" -> CharPredicates.PUNCTUATION();
            case "UPPERCASE" -> {
                if (caseIns) {
                    yield CharPredicates.UPPERCASE().union(CharPredicates.LOWERCASE(), CharPredicates.TITLECASE());
                }
                yield CharPredicates.UPPERCASE();
            }
            case "WHITESPACE", "WHITE_SPACE" -> CharPredicates.WHITE_SPACE();
            case "WORD" -> CharPredicates.WORD();
            default -> null;
        };
    }

    public static Pattern.CharPredicate forUnicodeProperty(String propName, boolean caseIns) {
        Pattern.CharPredicate p = CharPredicates.getUnicodePredicate(propName = propName.toUpperCase(Locale.ROOT), caseIns);
        if (p != null) {
            return p;
        }
        return CharPredicates.getPosixPredicate(propName, caseIns);
    }

    public static Pattern.CharPredicate forPOSIXName(String propName, boolean caseIns) {
        return CharPredicates.getPosixPredicate(propName.toUpperCase(Locale.ENGLISH), caseIns);
    }

    static Pattern.CharPredicate forUnicodeScript(String name) {
        try {
            Character.UnicodeScript script = Character.UnicodeScript.forName(name);
            return ch -> script == Character.UnicodeScript.of(ch);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    static Pattern.CharPredicate forUnicodeBlock(String name) {
        try {
            Character.UnicodeBlock block = Character.UnicodeBlock.forName(name);
            return ch -> block == Character.UnicodeBlock.of(ch);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    static Pattern.CharPredicate forProperty(String name, boolean caseIns) {
        return switch (name) {
            case "Cn" -> CharPredicates.category(1);
            case "Lu" -> CharPredicates.category(caseIns ? 14 : 2);
            case "Ll" -> CharPredicates.category(caseIns ? 14 : 4);
            case "Lt" -> CharPredicates.category(caseIns ? 14 : 8);
            case "Lm" -> CharPredicates.category(16);
            case "Lo" -> CharPredicates.category(32);
            case "Mn" -> CharPredicates.category(64);
            case "Me" -> CharPredicates.category(128);
            case "Mc" -> CharPredicates.category(256);
            case "Nd" -> CharPredicates.category(512);
            case "Nl" -> CharPredicates.category(1024);
            case "No" -> CharPredicates.category(2048);
            case "Zs" -> CharPredicates.category(4096);
            case "Zl" -> CharPredicates.category(8192);
            case "Zp" -> CharPredicates.category(16384);
            case "Cc" -> CharPredicates.category(32768);
            case "Cf" -> CharPredicates.category(65536);
            case "Co" -> CharPredicates.category(262144);
            case "Cs" -> CharPredicates.category(524288);
            case "Pd" -> CharPredicates.category(0x100000);
            case "Ps" -> CharPredicates.category(0x200000);
            case "Pe" -> CharPredicates.category(0x400000);
            case "Pc" -> CharPredicates.category(0x800000);
            case "Po" -> CharPredicates.category(0x1000000);
            case "Sm" -> CharPredicates.category(0x2000000);
            case "Sc" -> CharPredicates.category(0x4000000);
            case "Sk" -> CharPredicates.category(0x8000000);
            case "So" -> CharPredicates.category(0x10000000);
            case "Pi" -> CharPredicates.category(0x20000000);
            case "Pf" -> CharPredicates.category(0x40000000);
            case "L" -> CharPredicates.category(62);
            case "M" -> CharPredicates.category(448);
            case "N" -> CharPredicates.category(3584);
            case "Z" -> CharPredicates.category(28672);
            case "C" -> CharPredicates.category(884737);
            case "P" -> CharPredicates.category(1643118592);
            case "S" -> CharPredicates.category(0x1E000000);
            case "LC" -> CharPredicates.category(14);
            case "LD" -> CharPredicates.category(574);
            case "L1" -> CharPredicates.range(0, 255);
            case "all" -> Pattern.ALL();
            case "ASCII" -> CharPredicates.range(0, 127);
            case "Alnum" -> CharPredicates.ctype(1792);
            case "Alpha" -> CharPredicates.ctype(768);
            case "Blank" -> CharPredicates.ctype(16384);
            case "Cntrl" -> CharPredicates.ctype(8192);
            case "Digit" -> CharPredicates.range(48, 57);
            case "Graph" -> CharPredicates.ctype(5888);
            case "Lower" -> {
                if (caseIns) {
                    yield CharPredicates.ctype(768);
                }
                yield CharPredicates.range(97, 122);
            }
            case "Print" -> CharPredicates.range(32, 126);
            case "Punct" -> CharPredicates.ctype(4096);
            case "Space" -> CharPredicates.ctype(2048);
            case "Upper" -> {
                if (caseIns) {
                    yield CharPredicates.ctype(768);
                }
                yield CharPredicates.range(65, 90);
            }
            case "XDigit" -> CharPredicates.ctype(32768);
            case "javaLowerCase" -> {
                if (caseIns) {
                    yield c -> Character.isLowerCase(c) || Character.isUpperCase(c) || Character.isTitleCase(c);
                }
                yield Character::isLowerCase;
            }
            case "javaUpperCase" -> {
                if (caseIns) {
                    yield c -> Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isTitleCase(c);
                }
                yield Character::isUpperCase;
            }
            case "javaAlphabetic" -> Character::isAlphabetic;
            case "javaIdeographic" -> Character::isIdeographic;
            case "javaTitleCase" -> {
                if (caseIns) {
                    yield c -> Character.isTitleCase(c) || Character.isLowerCase(c) || Character.isUpperCase(c);
                }
                yield Character::isTitleCase;
            }
            case "javaDigit" -> Character::isDigit;
            case "javaDefined" -> Character::isDefined;
            case "javaLetter" -> Character::isLetter;
            case "javaLetterOrDigit" -> Character::isLetterOrDigit;
            case "javaJavaIdentifierStart" -> Character::isJavaIdentifierStart;
            case "javaJavaIdentifierPart" -> Character::isJavaIdentifierPart;
            case "javaUnicodeIdentifierStart" -> Character::isUnicodeIdentifierStart;
            case "javaUnicodeIdentifierPart" -> Character::isUnicodeIdentifierPart;
            case "javaIdentifierIgnorable" -> Character::isIdentifierIgnorable;
            case "javaSpaceChar" -> Character::isSpaceChar;
            case "javaWhitespace" -> Character::isWhitespace;
            case "javaISOControl" -> Character::isISOControl;
            case "javaMirrored" -> Character::isMirrored;
            default -> null;
        };
    }

    private static Pattern.CharPredicate category(int typeMask) {
        return ch -> (typeMask & 1 << Character.getType(ch)) != 0;
    }

    private static Pattern.CharPredicate range(int lower, int upper) {
        return ch -> lower <= ch && ch <= upper;
    }

    private static Pattern.CharPredicate ctype(int ctype) {
        return ch -> ch < 128 && ASCII.isType(ch, ctype);
    }

    static final Pattern.BmpCharPredicate ASCII_DIGIT() {
        return ch -> ch < 128 && ASCII.isDigit(ch);
    }

    static final Pattern.BmpCharPredicate ASCII_WORD() {
        return ch -> ch < 128 && ASCII.isWord(ch);
    }

    static final Pattern.BmpCharPredicate ASCII_SPACE() {
        return ch -> ch < 128 && ASCII.isSpace(ch);
    }

    static final Pattern.CharPredicate EMOJI() {
        return Character::isEmoji;
    }

    static final Pattern.CharPredicate EMOJI_PRESENTATION() {
        return Character::isEmojiPresentation;
    }

    static final Pattern.CharPredicate EMOJI_MODIFIER() {
        return Character::isEmojiModifier;
    }

    static final Pattern.CharPredicate EMOJI_MODIFIER_BASE() {
        return Character::isEmojiModifierBase;
    }

    static final Pattern.CharPredicate EMOJI_COMPONENT() {
        return Character::isEmojiComponent;
    }

    static final Pattern.CharPredicate EXTENDED_PICTOGRAPHIC() {
        return Character::isExtendedPictographic;
    }
}

