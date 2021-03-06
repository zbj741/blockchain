package com.buaa.blockchain.contract.trie.datasource;

import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * LevelDB的封装类，用于支持trie模块中的持久化。
 * LevelDB的原生方法中insert、update、delete是线程安全的，但是close不是线程安全的。
 * 在此使用resetDbLock用于同步，保护init、close、delete方法。
 *
 * 在此可以拦截所有LevelDB操作。
 * 【注】trie中使用了rlp编码
 *
 * @author hitty
 * */


public class LevelDbDataSource implements KeyValueDataSource {

    private static final Logger logger = LoggerFactory.getLogger("leveldb");
    String dir;
    String name;
    public DB db;
    boolean alive;

    /* 同步锁 */
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    public LevelDbDataSource() {
        logger.info("New LevelDbDataSource: " + this.name);
    }

    public LevelDbDataSource(String name) {
        this.name = name;
        logger.info("New LevelDbDataSource: " + this.name);
    }

    public LevelDbDataSource(String dir,String name) {
        this.dir = dir;
        this.name = name;
        logger.info("New LevelDbDataSource: " + this.name);
    }

    @Override
    public void init() {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> LevelDbDataSource.init(): " + name);

            if (isAlive()) return;

            if (name == null) throw new NullPointerException("no name set to the db");

            Options options = new Options();
            options.createIfMissing(true);
            options.compressionType(CompressionType.NONE);
            options.blockSize(10 * 1024 * 1024);
            options.writeBufferSize(10 * 1024 * 1024);
            options.cacheSize(1024);
            options.paranoidChecks(true);
            options.verifyChecksums(true);
            options.maxOpenFiles(128);


            try {
                //Config config =
                logger.debug("Opening database");
                Path dbPath = Paths.get(dir, name);
                logger.debug("dbPath is " + dbPath.toString());
                Files.createDirectories(dbPath.getParent());

                logger.debug("Initializing new or existing database: '{}'", name);
                db = factory.open(dbPath.toFile(), options);

                alive = true;
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                throw new RuntimeException("Can't initialize database", ioe);
            }
            logger.debug("<~ LevelDbDataSource.init(): " + name);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void flushAll() {
        resetDbLock.writeLock().lock();
        try{

        }finally {
            resetDbLock.writeLock().unlock();
        }
    }

    public void destroyDB(File fileLocation) {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("Destroying existing database: " + fileLocation);
            Options options = new Options();
            try {
                factory.destroy(fileLocation, options);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] get(byte[] key) {
        if (isAlive() == false) {
            this.init();
        }
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled())
                logger.trace("~> LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key));
            try {
                byte[] ret = db.get(key);
                if (logger.isTraceEnabled())
                    logger.trace("<~ LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                return ret;
            } catch (DBException e) {
                logger.error("Exception. Retrying again...", e);
                byte[] ret = db.get(key);
                if (logger.isTraceEnabled())
                    logger.trace("<~ LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                return ret;
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled())
                logger.trace("~> LevelDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (value == null ? "null" : value.length));
            db.put(key, value);
            if (logger.isTraceEnabled())
                logger.trace("<~ LevelDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (value == null ? "null" : value.length));
            return value;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled())
                logger.trace("~> LevelDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
            db.delete(key);
            if (logger.isTraceEnabled())
                logger.trace("<~ LevelDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.keys(): " + name);
            try (DBIterator iterator = db.iterator()) {
                Set<byte[]> result = new HashSet<>();
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    result.add(iterator.peekNext().getKey());
                }
                if (logger.isTraceEnabled())
                    logger.trace("<~ LevelDbDataSource.keys(): " + name + ", " + result.size());
                return result;
            } catch (IOException e) {
                logger.error("Unexpected", e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws IOException {
        try (WriteBatch batch = db.createWriteBatch()) {
            for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                if (entry.getValue() == null) {
                    batch.delete(entry.getKey());
                } else {
                    // logger.info("~> LevelDbDataSource.updateBatchInternal: "+entry.getKey().toString()+" , "+entry.getValue().toString());
                    batch.put(entry.getKey(), entry.getValue());
                }
            }
            try {//add 08-23 18:32
                db.write(batch);
            } catch (DBException e) {
                logger.error("Exception. Retrying again...", e);
                db.write(batch);
            }//end add 08-23 18:32
        }
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled())
                logger.trace("~> LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
            try {
                updateBatchInternal(rows);
                if (logger.isTraceEnabled())
                    logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
            } catch (Exception e) {
                logger.error("Error, retrying one more time...", e);
                // try one more time
                try {
                    updateBatchInternal(rows);
                    if (logger.isTraceEnabled())
                        logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
                } catch (Exception e1) {
                    logger.error("Error", e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) return;

            try {
                logger.debug("Close db: {}", name);
                db.close();

                alive = false;
            } catch (IOException e) {
                logger.error("Failed to find the db file on the close: {} ", name);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }
}

