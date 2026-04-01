package com.intuit.sbd.payroll.psp.common.utils.mockutil;

import com.intuit.sbg.shared.filestore.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class MockS3FileStore implements FileStore {

    private final Logger logger = LoggerFactory.getLogger(MockS3FileStore.class);

    @Override
    public Set<String> listFiles(String rootPath, String filterRegex) throws Exception {
        logger.info("Parallel Env Mock Method listFiles(String rootPath, String filterRegex)");
        return new HashSet<>();
    }

    @Override
    public Set<String> listFiles(String bucket, String rootPath, String filterRegex) throws Exception {
        logger.info("Parallel Env Mock Method listFiles(String bucket, String rootPath, String filterRegex)");
        return new HashSet<>();
    }

    @Override
    public String readFile(String path) throws Exception {
        logger.info("Parallel Env Mock Method readFile(String path)");
        return "";
    }

    @Override
    public String readFile(String bucket, String path) throws Exception {
        logger.info("Parallel Env Mock Method readFile(String bucket, String path)");
        return "";
    }

    @Override
    public InputStream readFileAsStream(String bucket, String path) {
        logger.info("Parallel Env Mock Method readFileAsStream(String bucket, String path)");
        return new ByteArrayInputStream("".getBytes());
    }

    @Override
    public InputStream readFileAsStream(String path) {
        logger.info("Parallel Env Mock Method readFileAsStream(String path)");
        return new ByteArrayInputStream("".getBytes());
    }

    @Override
    public void writeFile(String path, String content) throws Exception {
        logger.info("Parallel Env Mock Method writeFile(String path, String content)");
        return;
    }

    @Override
    public void writeFile(String bucket, String path, String content) throws Exception {
        logger.info("Parallel Env Mock Method writeFile(String bucket, String path, String content)");
        return;
    }

    @Override
    public void writeFile(String path, InputStream inputStream) throws Exception {
        logger.info("Parallel Env Mock Method writeFile(String path, InputStream inputStream)");
        return;
    }

    @Override
    public void writeFile(String bucket, String path, InputStream inputStream) throws Exception {
        logger.info("Parallel Env Mock Method writeFile(String bucket, String path, InputStream inputStream)");
        return;
    }

    @Override
    public void writeFile(String bucket, String path, File file) throws Exception {
        logger.info("Parallel Env Mock Method writeFile(String bucket, String path, File file)");
        return;
    }

    @Override
    public void moveFile(String existingPath, String newPath) throws Exception {
        logger.info("Parallel Env Mock Method moveFile(String existingPath, String newPath)");
        return;
    }

    @Override
    public void moveFile(String bucket, String existingPath, String newPath) throws Exception {
        logger.info("Parallel Env Mock Method moveFile(String bucket, String existingPath, String newPath)");
        return;
    }

    @Override
    public void deleteFile(String path) throws Exception {
        logger.info("Parallel Env Mock Method deleteFile(String path)");
        return;
    }

    @Override
    public void deleteFile(String bucket, String path) throws Exception {
        logger.info("Parallel Env Mock Method deleteFile(String bucket, String path)");
        return;
    }

    @Override
    public void close() throws Exception {
        logger.info("Parallel Env Mock Method close()");
        return;
    }
}