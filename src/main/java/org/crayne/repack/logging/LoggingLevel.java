package org.crayne.repack.logging;

import org.bes.stain.text.AnsiColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static org.bes.stain.text.AnsiColor.foreground;

public enum LoggingLevel {

    INFO             (foreground(Color.WHITE)),
    SUCCESS          (foreground(Color.GREEN)),
    HELP             (foreground(Color.CYAN)),
    WARN             (foreground(Color.YELLOW)),

    ERROR            (foreground(Color.RED)),
    LEXING_ERROR     (foreground(Color.RED)),
    PARSING_ERROR    (foreground(Color.RED)),
    ANALYZING_ERROR  (foreground(Color.RED)),
    CONVERTING_ERROR (foreground(Color.RED)),
    PACKING_ERROR    (foreground(Color.RED));


    @NotNull
    private final AnsiColor color;

    LoggingLevel(@NotNull final AnsiColor color) {
        this.color = color;
    }

    @NotNull
    public AnsiColor color() {
        return color;
    }

    public boolean error() {
        return switch (this) {
            case ERROR, LEXING_ERROR, PARSING_ERROR, CONVERTING_ERROR, PACKING_ERROR, ANALYZING_ERROR -> true;
            default -> false;
        };
    }

}
