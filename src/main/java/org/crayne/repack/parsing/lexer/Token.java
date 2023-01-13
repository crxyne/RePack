package org.crayne.repack.parsing.lexer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@SuppressWarnings("unused")
public class Token {

    @NotNull
    private final String token;

    @Nullable
    private final File file;
    private final int line;
    private final int column;

    public Token(@NotNull final String token, final int line, final int column, final @Nullable File file) {
        this.token = token;
        this.line = line;
        this.column = column;
        this.file = file;
    }

    public Token(@NotNull final String token) {
        this.token = token;
        this.line = -1;
        this.column = -1;
        this.file = null;
    }

    @NotNull
    public static Token of(@NotNull final String token, final int line, final int column, final File file) {
        return new Token(token, line, column, file);
    }

    @NotNull
    public static Token of(@NotNull final String token) {
        return new Token(token);
    }

    @NotNull
    public String token() {
        return token;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    @Nullable
    public File file() {
        return file;
    }

    public boolean equals(@NotNull final Token other) {
        return token.equals(other.token);
    }

    @Override
    @NotNull
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }

    @NotNull
    public String noStringLiterals() {
        return Tokenizer.removeStringLiterals(token());
    }

}
