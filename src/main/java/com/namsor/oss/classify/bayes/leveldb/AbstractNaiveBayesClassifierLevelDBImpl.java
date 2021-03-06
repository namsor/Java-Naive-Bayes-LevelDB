package com.namsor.oss.classify.bayes.leveldb;

import com.google.common.primitives.Longs;
import com.namsor.oss.classify.bayes.AbstractNaiveBayesClassifierImpl;
import com.namsor.oss.classify.bayes.PersistentClassifierException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
//import static org.fusesource.leveldbjni.JniDBFactory.*;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;

/**
 * A persistent Naive Bayes Classifier, based on LevelDB key-value store.
 * Persistence methods : you can switch between using 
 * - the JNI Level DB implementation (import static org.fusesource.leveldbjni.JniDBFactory.*;) or 
 * - the pure Java port (import static org.iq80.leveldb.impl.Iq80DBFactory.*;)
 *
 * @author elian
 */
public abstract class AbstractNaiveBayesClassifierLevelDBImpl extends AbstractNaiveBayesClassifierImpl {

    private static final int CACHE_SIZE_DEFAULT = 100; //100mb
    private static final int MB = 1048576;
    private final String rootPathWritable;
    private final DB db;

    /**
     * Create a persistent Naive Bayes Classifier using LevelDB
     * @param classifierName The classifier name
     * @param categories The immutable classification categories
     * @param cacheSizeMb LevelDB cache size (ex 100mb)
     * @param rootPathWritable The writable directory for LevelDB storage
     * @throws PersistentClassifierException The persistence error and cause
     */
    public AbstractNaiveBayesClassifierLevelDBImpl(String classifierName, String[] categories, int cacheSizeMb, String rootPathWritable) throws PersistentClassifierException {
        super(classifierName, categories);
        this.rootPathWritable = rootPathWritable;
        Options options = new Options();
        options.createIfMissing(true);
        options.cacheSize(cacheSizeMb * MB); 
        options.compressionType(CompressionType.NONE);
        
        try {
            db = factory.open(new File(rootPathWritable + "/" + classifierName), options); 
        } catch (IOException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    /**
     * Create a persistent Naive Bayes Classifier using LevelDB, with default cache size
     * @param classifierName The classifier name
     * @param categories The immutable classification categories
     * @param rootPathWritable The writable directory for LevelDB storage
     * @throws PersistentClassifierException The persistence error and cause
     */
    public AbstractNaiveBayesClassifierLevelDBImpl(String classifierName, String[] categories, String rootPathWritable) throws PersistentClassifierException {
        this(classifierName, categories, CACHE_SIZE_DEFAULT, rootPathWritable);
    }

    
    @Override
    public long dbSize() throws PersistentClassifierException {
        long dbSize = 0;
        ReadOptions ro = new ReadOptions();
        ro.snapshot(getDb().getSnapshot());
        DBIterator iterator = getDb().iterator(ro);
        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                //Map.Entry<byte[],byte[]> nextEntry = iterator.peekNext(); // no need to create the nextEntry object since it's never used.
                dbSize++;
            }
            return dbSize;
        } catch (Throwable ex) {
            throw new PersistentClassifierException(ex);
        } finally {
            try {
                // Make sure you close the snapshot to avoid resource leaks.
                ro.snapshot().close();
            } catch (IOException ex) {
                throw new PersistentClassifierException(ex);
            }
        }
    }

    @Override
    public void dbClose() throws PersistentClassifierException {
        try {
            getDb().close();
        } catch (IOException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    @Override
    public void dbCloseAndDestroy() throws PersistentClassifierException {
        try {
            db.close();
            Options options = new Options();
            factory.destroy(new File(rootPathWritable + "/" + getClassifierName()), options);
        } catch (IOException ex) {
            throw new PersistentClassifierException(ex);
        }
    }

    protected byte[] bytes(String key) {
        return key.getBytes();
    }

    /**
     * @return the db
     */
    public DB getDb() {
        return db;
    }

    @Override
    public synchronized void dumpDb(Writer w) throws PersistentClassifierException {
        ReadOptions ro = new ReadOptions();
        ro.snapshot(getDb().getSnapshot());
        DBIterator iterator = getDb().iterator(ro);
        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                Map.Entry<byte[],byte[]> nextEntry = iterator.peekNext();
                String key = asString(nextEntry.getKey());
                long value = Longs.fromByteArray(nextEntry.getValue());
                w.append(key + "|" + value + "\n");
            }
        } catch (IOException ex) {
            throw new PersistentClassifierException(ex);
        } finally {
            try {
                // Make sure you close the snapshot to avoid resource leaks.
                ro.snapshot().close();
            } catch (IOException ex) {
                throw new PersistentClassifierException(ex);
            }
        }
    }
}
