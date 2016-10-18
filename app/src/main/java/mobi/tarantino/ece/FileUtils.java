package mobi.tarantino.ece;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kolipass on 17.10.16.
 */

public class FileUtils {
    public static int fileCount(File folder, final String extension) {
        final List<File> files = new ArrayList<>();
        Collections.addAll(files, folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().contains(extension);
            }
        }));
        return files.size();
    }

    public static int folderCount(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            return 0;
        }

        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        return files.length;
    }
}
