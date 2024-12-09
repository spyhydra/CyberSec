package com.example.cybersec.ui.ftpServer;


import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import org.apache.ftpserver.ftplet.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AndroidFileSystemFactory implements FileSystemFactory {
    private final Context context;
    private final Uri rootUri;

    public AndroidFileSystemFactory(Context context, Uri rootUri) {
        this.context = context;
        this.rootUri = rootUri;
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {
        return new AndroidFileSystemView(context, rootUri, user);
    }
}

class AndroidFileSystemView implements FileSystemView {
    private final Context context;
    private final Uri rootUri;
    private final User user;
    private AndroidFtpFile currentDirectory;

    public AndroidFileSystemView(Context context, Uri rootUri, User user) {
        this.context = context;
        this.rootUri = rootUri;
        this.user = user;
        this.currentDirectory = new AndroidFtpFile(context, DocumentFile.fromTreeUri(context, rootUri), "/");
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return new AndroidFtpFile(context, DocumentFile.fromTreeUri(context, rootUri), "/");
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return currentDirectory;
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {
        AndroidFtpFile newDir = (AndroidFtpFile) getFile(dir);
        if (newDir != null && newDir.isDirectory()) {
            currentDirectory = newDir;
            return true;
        }
        return false;
    }

    @Override
    public FtpFile getFile(String file) throws FtpException {
        DocumentFile docFile = DocumentFile.fromTreeUri(context, rootUri);
        String[] parts = file.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals(".")) {
                docFile = docFile.findFile(part);
                if (docFile == null) {
                    return null;
                }
            }
        }
        return new AndroidFtpFile(context, docFile, file);
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        return false;
    }

    @Override
    public void dispose() {
        // No resources to dispose
    }
}

class AndroidFtpFile implements FtpFile {
    private final Context context;
    private final DocumentFile file;
    private final String path;

    public AndroidFtpFile(Context context, DocumentFile file, String path) {
        this.context = context;
        this.file = file;
        this.path = path;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean doesExist() {
        return file.exists();
    }

    @Override
    public boolean isReadable() {
        return true;  // Assume all files are readable
    }

    @Override
    public boolean isWritable() {
        return file.canWrite();
    }

    @Override
    public boolean isRemovable() {
        return true;  // Assume all files can be removed
    }

    @Override
    public String getOwnerName() {
        return "owner";
    }

    @Override
    public String getGroupName() {
        return "group";
    }

    @Override
    public int getLinkCount() {
        return 1;
    }

    @Override
    public long getLastModified() {
        return file.lastModified();
    }

    @Override
    public boolean setLastModified(long time) {
        return false;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public Object getPhysicalFile() {
        return null;
    }

    @Override
    public boolean mkdir() {
        return file.getParentFile().createDirectory(file.getName()) != null;
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public boolean move(FtpFile destination) {
        return false;
    }

    @Override
    public List<FtpFile> listFiles() {
        if (!file.isDirectory()) {
            return null;
        }
        List<FtpFile> files = new ArrayList<>();
        DocumentFile[] children = file.listFiles();
        if (children != null) {
            for (DocumentFile child : children) {
                files.add(new AndroidFtpFile(context, child, path + "/" + child.getName()));
            }
        }
        return files;
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        return context.getContentResolver().openOutputStream(file.getUri());
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        return context.getContentResolver().openInputStream(file.getUri());
    }
}
