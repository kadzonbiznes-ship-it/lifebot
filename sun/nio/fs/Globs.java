/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.util.regex.PatternSyntaxException;

public class Globs {
    private static final String regexMetaChars = ".^$+{[]|()";
    private static final String globMetaChars = "\\*?[{";
    private static char EOL = '\u0000';

    private Globs() {
    }

    private static boolean isRegexMeta(char c) {
        return regexMetaChars.indexOf(c) != -1;
    }

    private static boolean isGlobMeta(char c) {
        return globMetaChars.indexOf(c) != -1;
    }

    private static char next(String glob, int i) {
        if (i < glob.length()) {
            return glob.charAt(i);
        }
        return EOL;
    }

    private static String toRegexPattern(String globPattern, boolean isDos) {
        boolean inGroup = false;
        StringBuilder regex = new StringBuilder("^");
        int i = 0;
        block10: while (i < globPattern.length()) {
            char c = globPattern.charAt(i++);
            switch (c) {
                case '\\': {
                    char next;
                    if (i == globPattern.length()) {
                        throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
                    }
                    if (Globs.isGlobMeta(next = globPattern.charAt(i++)) || Globs.isRegexMeta(next)) {
                        regex.append('\\');
                    }
                    regex.append(next);
                    continue block10;
                }
                case '/': {
                    if (isDos) {
                        regex.append("\\\\");
                        continue block10;
                    }
                    regex.append(c);
                    continue block10;
                }
                case '[': {
                    if (isDos) {
                        regex.append("[[^\\\\]&&[");
                    } else {
                        regex.append("[[^/]&&[");
                    }
                    if (Globs.next(globPattern, i) == '^') {
                        regex.append("\\^");
                        ++i;
                    } else {
                        if (Globs.next(globPattern, i) == '!') {
                            regex.append('^');
                            ++i;
                        }
                        if (Globs.next(globPattern, i) == '-') {
                            regex.append('-');
                            ++i;
                        }
                    }
                    boolean hasRangeStart = false;
                    char last = '\u0000';
                    while (i < globPattern.length() && (c = globPattern.charAt(i++)) != ']') {
                        if (c == '/' || isDos && c == '\\') {
                            throw new PatternSyntaxException("Explicit 'name separator' in class", globPattern, i - 1);
                        }
                        if (c == '\\' || c == '[' || c == '&' && Globs.next(globPattern, i) == '&') {
                            regex.append('\\');
                        }
                        regex.append(c);
                        if (c == '-') {
                            if (!hasRangeStart) {
                                throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
                            }
                            if ((c = Globs.next(globPattern, i++)) == EOL || c == ']') break;
                            if (c < last) {
                                throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
                            }
                            regex.append(c);
                            hasRangeStart = false;
                            continue;
                        }
                        hasRangeStart = true;
                        last = c;
                    }
                    if (c != ']') {
                        throw new PatternSyntaxException("Missing ']", globPattern, i - 1);
                    }
                    regex.append("]]");
                    continue block10;
                }
                case '{': {
                    if (inGroup) {
                        throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
                    }
                    regex.append("(?:(?:");
                    inGroup = true;
                    continue block10;
                }
                case '}': {
                    if (inGroup) {
                        regex.append("))");
                        inGroup = false;
                        continue block10;
                    }
                    regex.append('}');
                    continue block10;
                }
                case ',': {
                    if (inGroup) {
                        regex.append(")|(?:");
                        continue block10;
                    }
                    regex.append(',');
                    continue block10;
                }
                case '*': {
                    if (Globs.next(globPattern, i) == '*') {
                        regex.append(".*");
                        ++i;
                        continue block10;
                    }
                    if (isDos) {
                        regex.append("[^\\\\]*");
                        continue block10;
                    }
                    regex.append("[^/]*");
                    continue block10;
                }
                case '?': {
                    if (isDos) {
                        regex.append("[^\\\\]");
                        continue block10;
                    }
                    regex.append("[^/]");
                    continue block10;
                }
            }
            if (Globs.isRegexMeta(c)) {
                regex.append('\\');
            }
            regex.append(c);
        }
        if (inGroup) {
            throw new PatternSyntaxException("Missing '}", globPattern, i - 1);
        }
        return regex.append('$').toString();
    }

    static String toUnixRegexPattern(String globPattern) {
        return Globs.toRegexPattern(globPattern, false);
    }

    static String toWindowsRegexPattern(String globPattern) {
        return Globs.toRegexPattern(globPattern, true);
    }
}

