package com.nutiteq.filepicker;

import java.io.FileFilter;

/**
 * Interface for activities that need custom input file.
 */
public interface FilePickerActivity {

    String getFileSelectMessage();

    FileFilter getFileFilter();
}
