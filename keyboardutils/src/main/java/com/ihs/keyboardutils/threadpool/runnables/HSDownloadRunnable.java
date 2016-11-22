package com.ihs.keyboardutils.threadpool.runnables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ihandysoft on 16/11/21.
 */

public class HSDownloadRunnable extends HSRunnable {

    private String filePath;
    private String url;
    private long fileSize;

    private HttpURLConnection connection;
    private int responseCode;
    private FileChannel fileChannel;
    private DownloadListener downloadListener;

    public HSDownloadRunnable(String url, String filePath, DownloadListener downloadListener) {
        this(url, filePath, Thread.NORM_PRIORITY, downloadListener);
    }

    public HSDownloadRunnable(String url, String filePath, int threadPriory, DownloadListener downloadListener) {
        super(threadPriory);
        this.url = url;
        this.filePath = filePath;
        this.downloadListener = downloadListener;
    }

    @Override
    public void run() {
        File downloadFile = new File(filePath);
        if (downloadFile.exists()) {
            return;
        }
        download(downloadFile);
    }

    private void download(File downloadFile) {

        try {
            connection = (HttpURLConnection) new URL(this.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000 * 6);
            if ((responseCode = connection.getResponseCode()) == 200) {
                fileSize = connection.getContentLength();
                writeToFile(connection.getInputStream(), downloadFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            downloadListener.downloadFailed();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeToFile(InputStream inputStream, File downloadFile) throws IOException {

        double downLoadFileSize = 0;

        byte[] b = new byte[1024];
        int readSize;

        FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
        fileChannel = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while ((readSize = inputStream.read(b)) > 0) {
            if (isCanncelled()) {
                break;
            }
            downLoadFileSize += readSize;
            byteBuffer.put(b);
            byteBuffer.flip();
            fileChannel.write(byteBuffer);
            byteBuffer.clear();
            if (downLoadFileSize > 0 && fileSize > 0) {
                downloadListener.downloadProgress(downLoadFileSize / (double) fileSize);
            }
        }
        fileChannel.force(true);
        fileChannel.close();

        if (isCanncelled()) {
            new File(filePath).delete();
        } else {
            downloadListener.downloadSuccess();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if(responseCode != 200) {
            connection.disconnect();
        }
    }

    public interface DownloadListener {
        void downloadProgress(double downloadPercent);

        void downloadSuccess();

        void downloadFailed();
    }
}
