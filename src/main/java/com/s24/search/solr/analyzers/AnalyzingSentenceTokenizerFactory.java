package com.s24.search.solr.analyzers;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * @see AnalyzingSentenceTokenizer
 * 
 * @author Shopping24 GmbH
 */
public class AnalyzingSentenceTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {

   private static final Logger logger = LoggerFactory.getLogger(AnalyzingSentenceTokenizerFactory.class);

   // remove bad sentences?
   private static final String FILTER_ARG = "filter";
   private boolean filter = false;

   // stopwords
   private static final String STOP_WORD_FILE = "stopwordfile";
   private String stopWordFilePath;
   private CharArraySet stopWords = null;

   // the magic threshold
   @VisibleForTesting
   static final float DEFAULT_COMMA_WORD_THRESHOLD = 0.2f;
   private static final String COMMA_WORD_THRESHOLD_ARG = "commaWordThreshold";
   private float commaWordThreshold = DEFAULT_COMMA_WORD_THRESHOLD;

   static final float DEFAULT_MAX_STOPWORD_RATIO = 0.21f;
   private static final String MAX_STOPWORD_RATIO_ARG = "maxStopwordRatio";
   private float maxStopwordRatio = DEFAULT_MAX_STOPWORD_RATIO;

   static final int DEFAULT_MIN_SENTENCE_LENGTH = 5;
   private static final String MIN_SENTENCE_LENGTH_ARG = "minSentenceLength";
   private int minSentenceLength = DEFAULT_MIN_SENTENCE_LENGTH;

   /**
    * Tokenizer gets constructed with the configured args.
    */
   public AnalyzingSentenceTokenizerFactory(Map<String, String> args) {
      super(args);

      if (args.containsKey(FILTER_ARG)) {
         filter = Boolean.parseBoolean(args.get(FILTER_ARG));
      }

      if (args.containsKey(COMMA_WORD_THRESHOLD_ARG)) {
         commaWordThreshold = Float.parseFloat(args.get(COMMA_WORD_THRESHOLD_ARG));
      }

      if (args.containsKey(MAX_STOPWORD_RATIO_ARG)) {
         maxStopwordRatio = Float.parseFloat(args.get(MAX_STOPWORD_RATIO_ARG));
      }

      if (args.containsKey(MIN_SENTENCE_LENGTH_ARG)) {
         minSentenceLength = Integer.parseInt(args.get(MIN_SENTENCE_LENGTH_ARG));
      }

      if (args.containsKey(STOP_WORD_FILE)) {
         stopWordFilePath = args.get(STOP_WORD_FILE);
      } else {
         logger.warn(
               "The {} param is not set. The sentences could not be analyzed (due to wrong calcuation of the information gain).",
               STOP_WORD_FILE);
      }
   }

   /**
    * Reload the stop words
    */
   @Override
   public void inform(ResourceLoader loader) throws IOException {
      if (stopWordFilePath != null) {
         try {
            stopWords = getWordSet(loader, stopWordFilePath, true);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      } else {
         stopWords = new CharArraySet(0, false);
      }
   }

   /**
    * Create the tokenizer
    */
   @Override
   public Tokenizer create(AttributeFactory factory) {
      return new AnalyzingSentenceTokenizer(factory, filter, stopWords, commaWordThreshold, maxStopwordRatio,
            minSentenceLength);
   }

}
