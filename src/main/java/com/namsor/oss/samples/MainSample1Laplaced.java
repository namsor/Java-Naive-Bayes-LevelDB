package com.namsor.oss.samples;

import com.namsor.oss.classify.bayes.ClassifyException;
import com.namsor.oss.classify.bayes.IClassification;
import com.namsor.oss.classify.bayes.NaiveBayesClassifierLevelDBImpl;
import com.namsor.oss.classify.bayes.NaiveBayesClassifierLevelDBLaplacedImpl;
import com.namsor.oss.classify.bayes.NaiveBayesClassifierRocksDBImpl;
import com.namsor.oss.classify.bayes.NaiveBayesClassifierRocksDBLaplacedImpl;
import com.namsor.oss.classify.bayes.NaiveBayesClassifierTransientLaplacedImpl;
import com.namsor.oss.classify.bayes.PersistentClassifierException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple test inspired by
 * http://ai.fon.bg.ac.rs/wp-content/uploads/2015/04/ML-Classification-NaiveBayes-2014.pdf
 *
 * @author elian
 */
public class MainSample1Laplaced {

    public static final String YES = "Yes";
    public static final String NO = "No";
    /**
     * Header table as per https://taylanbil.github.io/boostedNB or
     * http://ai.fon.bg.ac.rs/wp-content/uploads/2015/04/ML-Classification-NaiveBayes-2014.pdf
     */
    public static final String[] colName = {
        "outlook", "temp", "humidity", "wind", "play"
    };

    /**
     * Data table as per https://taylanbil.github.io/boostedNB or
     * http://ai.fon.bg.ac.rs/wp-content/uploads/2015/04/ML-Classification-NaiveBayes-2014.pdf
     */
    public static final String[][] data = {
        {"Sunny", "Hot", "High", "Weak", "No"},
        {"Sunny", "Hot", "High", "Strong", "No"},
        {"Overcast", "Hot", "High", "Weak", "Yes"},
        {"Rain", "Mild", "High", "Weak", "Yes"},
        {"Rain", "Cool", "Normal", "Weak", "Yes"},
        {"Rain", "Cool", "Normal", "Strong", "No"},
        {"Overcast", "Cool", "Normal", "Strong", "Yes"},
        {"Sunny", "Mild", "High", "Weak", "No"},
        {"Sunny", "Cool", "Normal", "Weak", "Yes"},
        {"Rain", "Mild", "Normal", "Weak", "Yes"},
        {"Sunny", "Mild", "Normal", "Strong", "Yes"},
        {"Overcast", "Mild", "High", "Strong", "Yes"},
        {"Overcast", "Hot", "Normal", "Weak", "Yes"},
        {"Rain", "Mild", "High", "Strong", "No"},};

    
    
    public static final void main(String[] args) {
        
        try {
            String[] cats = {YES, NO};
            // Create a new bayes classifier with string categories and string features.
            //NaiveBayesClassifierTransientLaplacedImpl bayes = new NaiveBayesClassifierTransientLaplacedImpl ("tennis", cats, 1, false);
            //NaiveBayesClassifierRocksDBLaplacedImpl bayes = new NaiveBayesClassifierRocksDBLaplacedImpl("tennis", cats, ".", 100);
            NaiveBayesClassifierLevelDBLaplacedImpl bayes = new NaiveBayesClassifierLevelDBLaplacedImpl("tennis", cats, ".", 100);

// Examples to learn from.
            for (int i = 0; i < data.length; i++) {
                Map<String,String> features = new HashMap();
                for (int j = 0; j < colName.length - 1; j++) {
                    features.put(colName[j], data[i][j]);
                }
                bayes.learn(data[i][colName.length - 1], features);
            }
            /**
             * Calculate the likelihood that: Outlook = sunny (0.22) Temperature
             * = cool (0.33) Humidity = high (0.33) Windy = true (0.33) Play =
             * yes (0.64)
             */

// Here are is X(B,S) to classify.
            Map<String,String> features = new HashMap();
            features.put("outlook","Overcast");
            features.put("temp","Cool");
            features.put("humidity","High");
            features.put("wind","Strong");

            IClassification[] predict = bayes.classify(features);
            StringWriter sw = new StringWriter();
            bayes.dumpDb(sw);
            System.out.println(sw);
            for (int i = 0; i < predict.length; i++) {
                System.out.println("P(" + predict[i].getCategory() + ")=" + predict[i].getProbability());
            }
        } catch (PersistentClassifierException ex) {
            Logger.getLogger(MainSample1Laplaced.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassifyException ex) {
            Logger.getLogger(MainSample1Laplaced.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            Logger.getLogger(MainSample1Laplaced.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}