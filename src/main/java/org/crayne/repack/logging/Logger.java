package org.crayne.repack.logging;

import org.bes.stain.text.AnsiColor;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

public class Logger extends PrintStream {

    @NotNull
    private final PrintStream out;

    @NotNull
    private final String format;

    public Logger() {
        super(nullOutputStream());
        out = System.out;
        format = "(%c%%ll%%cr%): %c%%m%%cr%";
    }

    public Logger(@NotNull final String format) {
        super(nullOutputStream());
        out = System.out;
        this.format = format;
    }

    public Logger(@NotNull final String format, @NotNull final PrintStream out) {
        super(nullOutputStream());
        this.out = out;
        this.format = format;
    }

    public void println(@Nullable final Object x) {
        if (x == null) return;
        info(String.valueOf(x));
    }

    public void log(@NotNull final String message, @NotNull final LoggingLevel level) {
        final String l = level.name().replace("_", " ");
        final String ll = l.toLowerCase();
        final String c = String.valueOf(level.color());

        out.println(format
                .replace("%l%", l)
                .replace("%ll%", ll)
                .replace("%c%", c)
                .replace("%cr%", AnsiColor.RESET)
                .replace("%m%", message));
    }

    public void info(@NotNull final String message) {
        log(message, LoggingLevel.INFO);
    }

    public void warn(@NotNull final String message) {
        log(message, LoggingLevel.WARN);
    }

    public void error(@NotNull final String message) {
        log(message, LoggingLevel.ERROR);
    }

    public void error(@NotNull final String message, @NotNull final LoggingLevel level) {
        if (!level.error()) throw new IllegalArgumentException("Level is not an error level!");
        log(message, LoggingLevel.ERROR);
    }

    public void traceback(@NotNull final String message, @NotNull final LoggingLevel level, @NotNull final String... hints) {
        log(message, level);
        Arrays.stream(hints).forEach(h -> log(h, LoggingLevel.HELP));
    }

    public void traceback(@NotNull final String message, @NotNull final Token at, @NotNull final String line, final boolean skipToEnd, @NotNull final LoggingLevel level, @NotNull final String... hints) {
        traceback(message, at, level, hints);
        log(line.replace("\t", " "), LoggingLevel.HELP);
        log(" ".repeat(Math.max(0, skipToEnd ? line.length() : at.column() - 1)) + "^", LoggingLevel.HELP);
    }

    public void traceback(@NotNull final String message, @NotNull final Token at, @NotNull final LoggingLevel level, @NotNull final String... hints) {
        if (at.line() == -1 || at.column() == -1) {
            traceback(message, level, hints);
            return;
        }
        log("at line " + at.line() + ", " +
                "column " + at.column() +
                (at.file() != null ?" in file " + Objects.requireNonNull(at.file()).getAbsolutePath() : ""), level);
        log(message, level);
        Arrays.stream(hints).forEach(h -> log(h, LoggingLevel.HELP));
    }


}
