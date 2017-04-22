package io.objectbox;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.annotation.apihint.Internal;
import io.objectbox.ideasonly.ModelUpdate;

public class BoxStoreBuilder {

    public static final String DEFAULT_NAME = "objectbox";

    final byte[] model;

    /** BoxStore uses this */
    File directory;

    /** Ignored by BoxStore */
    private File baseDirectory;

    /** Ignored by BoxStore */
    private String name;

    // 512 MB
    long maxSizeInKByte = 512 * 1024;

    ModelUpdate modelUpdate;

    private boolean android;

    boolean debugTransactions;

    final List<Properties> entityInfoList = new ArrayList<>();

    public BoxStoreBuilder(byte[] model) {
        this.model = model;
        if (model == null) {
            throw new IllegalArgumentException("Model may not be null");
        }
    }

    public BoxStoreBuilder name(String name) {
        if (directory != null) {
            throw new IllegalArgumentException("Already has directory, cannot assign name");
        }
        if (name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Name may not contain (back) slashes. " +
                    "Use baseDirectory() or directory() to configure alternative directories");
        }
        this.name = name;
        return this;
    }

    public BoxStoreBuilder directory(File directory) {
        if (name != null) {
            throw new IllegalArgumentException("Already has name, cannot assign directory");
        }
        if (!android && baseDirectory != null) {
            throw new IllegalArgumentException("Already has base directory, cannot assign directory");
        }
        this.directory = directory;
        return this;
    }

    public BoxStoreBuilder baseDirectory(File baseDirectory) {
        if (directory != null) {
            throw new IllegalArgumentException("Already has directory, cannot assign base directory");
        }
        this.baseDirectory = baseDirectory;
        return this;
    }

    /**
     * For Android, ObjectBox needs the Context if you want to store your data in the files directory of your app.
     * If you have a java.io.File object with an absolute path, you can call {@link #directory(File)} instead.
     */
    public BoxStoreBuilder androidContext(Object context) {
        if (context == null) {
            throw new NullPointerException("Context may not be null");
        }
        File filesDir;
        try {
            Method getFilesDir = context.getClass().getMethod("getFilesDir");
            filesDir = (File) getFilesDir.invoke(context);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not init with given Android context (must be sub class of android.content.Context)", e);
        }
        if (filesDir == null) {
            throw new IllegalStateException("Android files dir is null");
        }
        File baseDir = new File(filesDir, "objectbox");
        if (!baseDir.exists()) {
            boolean ok = baseDir.mkdirs();
            if (!ok) {
                System.err.print("Could not create base dir");
            }
        }
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new RuntimeException("Could not init Android base dir at " + baseDir.getAbsolutePath());
        }
        baseDirectory = baseDir;
        android = true;
        return this;
    }

    @Internal
    public <T> void entity(Properties properties) {
        entityInfoList.add(properties);
    }

    // Not sure this will ever be implements
    BoxStoreBuilder modelUpdate(ModelUpdate modelUpdate) {
        throw new UnsupportedOperationException("Not yet implemented");
        //        this.modelUpdate = modelUpdate;
        //        return this;
    }

    public BoxStoreBuilder maxSizeInKByte(long maxSizeInKByte) {
        this.maxSizeInKByte = maxSizeInKByte;
        return this;
    }

    public BoxStoreBuilder debugTransactions() {
        this.debugTransactions = true;
        return this;
    }

    public BoxStore build() {
        if (directory != null) {
            if (directory.exists() && !directory.isDirectory()) {
                throw new IllegalArgumentException("Given directory file exists but actually is not a directory: " +
                        directory.getAbsolutePath());
            }
        } else {
            if (name == null) {
                name = DEFAULT_NAME;
            }
            if (baseDirectory != null) {
                directory = new File(baseDirectory, name);
            } else {
                directory = new File(name);
            }
        }
        return new BoxStore(this);
    }

    public BoxStore buildDefault() {
        BoxStore store = build();
        BoxStore.setDefault(store);
        return store;
    }
}
