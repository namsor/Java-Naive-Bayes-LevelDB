package com.namsor.oss.classify.bayes;

import com.google.common.primitives.Longs;
import org.rocksdb.*;

import java.io.IOException;
import java.io.Writer;

/**
 * A persistent Naive Bayes Classifier, based on RocksDB key-value store.
 *
 * @author elian
 */
public abstract class AbstractNaiveBayesClassifierRocksDBImpl extends AbstractNaiveBayesClassifierImpl {

    private final String rootPathWritable;
    private final RocksDB db;
    private final int ROCKSDB_MaxBackgroundFlushes = 4;

    /**
     * Create a persistent Naive Bayes Classifier using RocksDB
     *
     * @param classifierName   The classifier name
     * @param categories       The immutable classification categories
     * @param rootPathWritable The writable directory for LevelDB storage
     * @throws PersistentClassifierException The persistence error and cause
     */
    public AbstractNaiveBayesClassifierRocksDBImpl(String classifierName, String[] categories, String rootPathWritable) throws PersistentClassifierException {
        super(classifierName, categories);
        this.rootPathWritable = rootPathWritable;
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMaxBackgroundFlushes(ROCKSDB_MaxBackgroundFlushes);
        try {
            db = RocksDB.open(options, rootPathWritable + "/" + classifierName);
        } catch (RocksDBException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    @Override
    public long dbSize() throws PersistentClassifierException {
        try {
            String dbSize = getDb().getProperty("rocksdb.estimate-num-keys");
            if (dbSize != null) {
                return Long.parseLong(dbSize);
            } else {
                return -1;
            }
        } catch (RocksDBException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    @Override
    public void dbClose() throws PersistentClassifierException { //todo PersistentClassifierException is never thrown
        getDb().close();
    }

    @Override
    public void dbCloseAndDestroy() throws PersistentClassifierException {
        try {
            getDb().close();
            Options options = new Options();
            RocksDB.destroyDB(rootPathWritable + "/" + getClassifierName(), options);
        } catch (RocksDBException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            dbClose();
        } catch (Throwable t) {
            // ignore
        }
    }

    protected byte[] bytes(String key) {
        return key.getBytes();
    }

    /**
     * @return the db
     */
    protected RocksDB getDb() {
        return db;
    }


    @Override
    public synchronized void dumpDb(Writer w) throws PersistentClassifierException {
        ReadOptions ro = new ReadOptions();
        ro.setSnapshot(getDb().getSnapshot());

        RocksIterator iterator = getDb().newIterator(ro);
        try {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                String key = new String(iterator.key());
                long value = Longs.fromByteArray(iterator.value());
                w.append(key + "|" + value + "\n");
            }
        } catch (IOException ex) {
            throw new PersistentClassifierException(ex);
        } finally {
            // Make sure you close the snapshot to avoid resource leaks.
            ro.snapshot().close();
        }
    }

}
