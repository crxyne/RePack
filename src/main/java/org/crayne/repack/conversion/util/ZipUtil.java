package org.crayne.repack.conversion.util;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private ZipUtil() {}

    public static void zipDirectory(@NotNull final File srcFolder, @NotNull final File destZipFile) throws IOException {
        try (final FileOutputStream fileWriter = new FileOutputStream(destZipFile);
            final ZipOutputStream zip = new ZipOutputStream(fileWriter)) {
            addDirectory(srcFolder, srcFolder, zip);
        }
    }

    private static void addFile(@NotNull final File rootPath, @NotNull final File srcFile, @NotNull final ZipOutputStream zip) throws IOException {
        if (srcFile.isDirectory()) {
            addDirectory(rootPath, srcFile, zip);
            return;
        }
        final byte[] buf = new byte[1024];
        int len;

        try (final FileInputStream in = new FileInputStream(srcFile)) {
            final String name = srcFile.getPath().replaceFirst(rootPath.getPath(), "");
            zip.putNextEntry(new ZipEntry(name));
            while ((len = in.read(buf)) > 0) zip.write(buf, 0, len);
        }
    }

    private static void addDirectory(@NotNull final File rootPath, @NotNull final File srcFolder, @NotNull final ZipOutputStream zip) {
        Arrays.stream(Objects.requireNonNull(srcFolder.listFiles()))
                .forEachOrdered(f -> {
                    try {
                        addFile(rootPath, f, zip);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
