package org.crayne.repack.parsing.lexer;

import org.apache.commons.text.StringEscapeUtils;
import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.NodeType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class Tokenizer {

    private final Logger logger;

    private final List<String> multiSpecial;
    private final List<Token> result = new ArrayList<>();
    private char currentQuotes = 0;
    private Token beganString;
    private String previous = null;
    private boolean singleLineCommented = false;
    private boolean multilineCommented = false;
    private StringBuilder currentToken = new StringBuilder();
    private int line = 1;
    private int column = 0;
    private File currentFile;
    private List<String> currentFileContent;

    private char atPos = 0;
    private boolean encounteredError = false;
    private boolean unfinishedTextLiteral = false;

    public Tokenizer(@NotNull final Logger logger) {
        this.multiSpecial = new ArrayList<>();
        this.logger = logger;
    }

    public Tokenizer(@NotNull final Logger logger, @NotNull final Collection<String> multiSpecial) {
        this.multiSpecial = new ArrayList<>();
        for (final String special : multiSpecial)
            if (!isMultiToken(special)) throw new IllegalArgumentException("Not a multi special token: '" + special + "'");

        this.multiSpecial.addAll(multiSpecial);
        this.logger = logger;
    }

    private static final String specials = ",(){}='\"";
    private static final String stringEscapeRegex = splitKeepDelim("\\\\u[\\dA-Fa-f]{4}|\\\\\\d|\\\\[\"\\\\'tnbfr]").substring(1);

    public static boolean isSpecialToken(@NotNull final String s) {
        return specials.contains(s);
    }

    private static <T> T isAnyType(@NotNull final Callable<T> typeCheck) {
        try {
            return typeCheck.call();
        } catch (final Exception e) {
            return null;
        }
    }

    public static Boolean isBool(@NotNull final String s) {
        final String lower = s.toLowerCase();
        return isAnyType(() -> lower.equals("1b") ? Boolean.TRUE : lower.equals("0b") ? Boolean.FALSE : null);
    }

    public static Double isDouble(@NotNull final String s) {
        final String lower = s.toLowerCase();
        if (lower.endsWith("f")) return null;
        return isAnyType(() -> Double.parseDouble(lower.endsWith("d") ? s.substring(0, s.length() - 1) : s));
    }

    public static Integer isInt(@NotNull final String s) {
        return isAnyType(() -> Integer.decode(s));
    }

    public static Long isLong(@NotNull final String s) {
        return isAnyType(() -> Long.parseLong(s.toLowerCase().endsWith("l") ? s.substring(0, s.length() - 1) : s));
    }

    public static Float isFloat(@NotNull final String s) {
        return isAnyType(() -> Float.parseFloat(s));
    }

    public static Character isChar(@NotNull final String s) {
        return isAnyType(() -> s.startsWith("'") && s.endsWith("'") && s.length() == 3 ? s.charAt(1) : null);
    }

    public static String isString(@NotNull final String s) {
        return isAnyType(() -> s.startsWith("\"") && s.endsWith("\"") ? s : null);
    }

    private void lexerError(@NotNull final String message, @NotNull final String... help) {
        logger.traceback(message, LoggingLevel.LEXING_ERROR, help);
        encounteredError = true;
    }

    private void lexerError(@NotNull final String message, @NotNull final Token at, @NotNull final String... help) {
        logger.traceback(message, at, currentFileContent.get(at.line() - 1), LoggingLevel.LEXING_ERROR, help);
        encounteredError = true;
    }

    public static String removeStringLiterals(@NotNull final String string) {
        if ((!string.startsWith("\"") || !string.endsWith("\"")) && (!string.startsWith("'") || !string.endsWith("'"))) return string;
        return string.substring(1, string.length() - 1);
    }

    public static String addStringLiterals(@NotNull final String string) {
        return "\"" + string + "\"";
    }

    private String fixEscapeCodes(@NotNull final String string) {
        final boolean isLiteralString = isString(string) != null;
        final String strippedQuotes = removeStringLiterals(string);
        final String escapedCodes = strippedQuotes
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
        return isLiteralString ? addStringLiterals(escapedCodes) : escapedCodes;
    }

    private static String splitKeepDelim(@NotNull final String delimRegex) {
        return "|((?=" + delimRegex + ")|(?<=" + delimRegex + "))";
    }

    public static boolean validEscapeSeq(@NotNull final String seq) {
        return seq.matches("\\\\u[\\dA-Fa-f]{4}|\\\\\\d|\\\\[\"\\\\'tnbfr]");
    }

    public boolean validEscapeSequences(@NotNull final String seq) {
        for (final String escapeSequence : seq.split(stringEscapeRegex)) {
            if (!validEscapeSeq(escapeSequence) && escapeSequence.startsWith("\\")) return false;
        }
        return true;
    }

    private static <T> T getLast(@NotNull final List<T> list) {
        return getLast(list, 1);
    }

    private static <T> T getLast(@NotNull final List<T> list, final int backSeek) {
        return list.get(list.size() - backSeek);
    }

    private static <T> void pop(@NotNull final List<T> list) {
        list.remove(list.size() - 1);
    }

    private static <T> void pop(@NotNull final List<T> list, final int amount) {
        for (int i = 0; i < amount; i++) pop(list);
    }

    private Token tokenOf(@NotNull final String token) {
        return new Token(token, line, Math.max(column - token.length(), 0), currentFile);
    }

    private Token currentToken() {
        return tokenOf(currentToken.toString());
    }

    private boolean notInComment() {
        return !singleLineCommented && !multilineCommented;
    }

    private boolean appendToCurrentString() {
        if (currentQuotes != 0) {
            currentToken.append(atPos);
            previous = "" + atPos;
            return true;
        }
        return false;
    }

    private boolean isPreviousEscape() {
        return previous != null && previous.equals("\\");
    }

    private void beginString() {
        beganString = currentToken();
        addCurrent();
        setCurrent(atPos + "");
        currentQuotes = atPos;
    }

    private boolean endString() {
        if (currentQuotes != atPos) return false;

        currentToken.append(atPos);
        final String str = !validEscapeSequences(currentToken.toString()) ? currentToken.toString() : StringEscapeUtils.unescapeJava(currentToken.toString());

        setCurrent(str);
        addCurrent();
        clearCurrent();
        currentQuotes = 0;
        return true;
    }

    private boolean beginOrEndString() {
        switch (currentQuotes) {
            case 0 -> {
                beginString();
                return true;
            }
            case '\'', '"' -> {
                if (endString()) return true;
                return appendToCurrentString();
            }
        }
        return false;
    }

    private boolean handleQuoted() {
        if (notInComment()) {
            if (currentQuotes != 0) handleNewlines();
            if (atPos == '\'' || atPos == '"') {
                if (isPreviousEscape() && appendToCurrentString()) return true;
                if (beginOrEndString()) return true;
            }
            return appendToCurrentString();
        }
        return false;
    }

    private char nextChar = 0;
    private char nextNextChar = 0;

    private char lastCharCurrent() {
        return currentToken.charAt(currentToken.toString().length() - 1);
    }

    private char nextCharCurrent() {
        return nextChar;
    }

    private char nextNextCharCurrent() {
        return nextNextChar;
    }

    private void handleNewlines() {
        if (atPos != '\n') return;

        if (currentQuotes != 0 && !unfinishedTextLiteral) {
            lexerError("Expected text literal to end at the same line", beganString, "All string literals (anything between \" or ') has to end with that same character.");
            unfinishedTextLiteral = true;
        }
        column = 0;
        line++;
        singleLineCommented = false;
    }

    private boolean handleWhitespaces() {
        if (!Character.isWhitespace(atPos)) return false;

        if (!currentToken.isEmpty()) {
            addCurrent();
            clearCurrent();
        }
        handleNewlines();
        return true;
    }

    private boolean handleComments(@NotNull final String multiTok) {
        switch (multiTok) {
            case "//" -> {
                singleLineCommented = true;
                clearCurrent();
                return true;
            }
            case "/*" -> {
                multilineCommented = true;
                clearCurrent();
                return true;
            }
        }
        if ((previous + atPos).equals("*/") && multilineCommented) {
            multilineCommented = false;
            clearCurrent();
            return true;
        }
        return false;
    }

    private boolean isCurrentMultiToken() {
        return isMultiToken(currentToken.toString());
    }

    private boolean isMultiToken(@NotNull final String multiTok) {
        return Arrays.stream(multiTok.split("")).allMatch(Tokenizer::isSpecialToken);
    }

    private boolean isCurrentNotBlank() {
        return !currentToken.toString().isBlank();
    }

    private void setCurrent(@NotNull final String s) {
        currentToken = new StringBuilder(s);
    }

    private void clearCurrent() {
        setCurrent("");
    }

    private void addCurrent() {
        if (currentToken.isEmpty()) return;
        result.add(currentToken());
        previous = currentToken.toString();
    }

    private boolean addCurrentMultiToken() {
        if (notInComment() && isCurrentNotBlank() && isCurrentMultiToken()) {
            addCurrent();
            setCurrent("" + atPos);
            return true;
        }
        return false;
    }

    private boolean doesMultiTokenExist(@NotNull final String multiTok) {
        return multiSpecial.stream().anyMatch(s -> s.startsWith(multiTok));
    }

    private boolean handleSpecialTokens() {
        if (!Tokenizer.isSpecialToken(atPos + "")) return addCurrentMultiToken();

        final String multiTok = currentToken.toString() + atPos;
        if (handleComments(multiTok)) return true;

        if (notInComment()) {
            if (isCurrentMultiToken() && doesMultiTokenExist(multiTok) && !NodeType.of(multiTok).isKeyword()) {
                currentToken.append(atPos);
                return true;
            }
            if (isCurrentNotBlank()) addCurrent();
            setCurrent("" + atPos);
            return true;
        }
        previous = "" + atPos;
        return true;

    }

    public List<Token> tokenize(@NotNull final File file, @NotNull final Collection<String> contentCollection, @NotNull final String code) {
        this.currentFile = file;
        return tokenize(contentCollection, code);
    }

    public void reset() {
        this.currentFileContent = new ArrayList<>();
        this.column = 0;
        this.line = -1;
        this.currentFile = null;
        this.encounteredError = false;
        this.beganString = null;
        this.atPos = 0;
        this.currentQuotes = 0;
        this.currentToken = new StringBuilder();
        this.previous = null;
        this.singleLineCommented = false;
        this.multilineCommented = false;
    }

    public List<Token> tokenize(@NotNull final Collection<String> contentList, @NotNull final String code) {
        result.clear();
        currentFileContent = new ArrayList<>(contentList);
        for (int i = 0; i < code.length(); i++) {
            this.atPos = code.charAt(i);

            for (int j = i + 1; j < code.length() && i + 1 < code.length(); j++) {
                nextChar = code.charAt(j);
                if (Character.isWhitespace(nextChar)) continue;

                for (int k = j + 1; k < code.length() && j + 1 < code.length(); k++) {
                    nextNextChar = code.charAt(k);
                    if (!Character.isWhitespace(nextNextChar)) break;
                }
                break;
            }
            column++;
            if (encounteredError) {
                reset();
                return new ArrayList<>();
            }

            if (handleQuoted() || handleWhitespaces() || handleSpecialTokens()) continue;
            if (notInComment()) currentToken.append(atPos);
        }
        reset();
        return result;
    }

    public boolean encounteredError() {
        return encounteredError;
    }

}
