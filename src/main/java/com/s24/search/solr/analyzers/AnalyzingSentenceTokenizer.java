package com.s24.search.solr.analyzers;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.AttributeFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

/**
 * Tokenizer which splits the input into sentences and emits only those sentences that do not contain too many
 * stopwords. Sentences that contain many commas are split into their comma-separated parts and analyzed per part. If
 * the input contains only a single sentence, it is always emitted.
 * 
 * @author Shopping24 GmbH
 */
public class AnalyzingSentenceTokenizer extends Tokenizer {

   // determine sentences
   private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.?!\\|;-])\\s+(?=\\p{Lu})");
   private static final Splitter SPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults();
   private static final CharMatcher SENTENCE_NOISE = CharMatcher.DIGIT.or(
         CharMatcher.anyOf(",;.:$!?%&/<>™®\\-–'\"|"));
   private static final Pattern COMMA_PATTERN = Pattern.compile("(,+(?=\\D))|((?<=\\D),+)|;");
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

   // register attributes to manipulate
   private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
   private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
   private final PositionIncrementAttribute positionIncrement = addAttribute(PositionIncrementAttribute.class);

   // this is the internal state
   private final StringBuilder inputBuffer = new StringBuilder();
   private final Matcher sentenceMatcher;
   private int index;
   private boolean lastSentenceFromCommaSplit = false;

   // configuration
   private final boolean removeBadSentences;
   private final CharArraySet stopWords;
   private final float commaWordThreshold;
   private final float maxStopwordRatio;
   private final int minSentenceLength;

   /**
    * Construct a token stream processing the given input using the given AttributeFactory.
    * 
    * @param factory
    *           the factory.
    * @param removeBadSentences
    *           if {@code true}, sentences with too many stopwords are filtered out.
    * @param stopWords
    *           the stopwords.
    * @param commaWordThreshold
    *           the threshold that defines the "comma density" that, if exceeded, causes a sentence to be split into
    *           sub-sentences that are analyzed individually.
    * @param maxStopwordRatio
    *           if the ratio of stopwords exceeds this threshold, the sentence is filtered out.
    * @param minSentenceLength
    *           a sentence must contain at least this many words, otherwise it is not analyzed and always emitted.
    */
   public AnalyzingSentenceTokenizer(AttributeFactory factory, boolean removeBadSentences, CharArraySet stopWords,
         float commaWordThreshold, float maxStopwordRatio, int minSentenceLength) {
      super(factory);

      this.removeBadSentences = removeBadSentences;
      this.stopWords = stopWords;
      this.commaWordThreshold = commaWordThreshold;

      this.maxStopwordRatio = maxStopwordRatio;
      this.minSentenceLength = minSentenceLength;

      // Use "" instead of str so don't consume chars
      // (fillBuffer) from the input on throwing IAE below:
      sentenceMatcher = SENTENCE_PATTERN.matcher("");
   }

   /**
    * {@inheritDoc}
    * 
    * Sets the final offset, does not reset internal state.
    */
   @Override
   public void end() throws IOException {
      super.end();

      // do a offset correction. Taken from PatternTokenzier
      final int ofs = correctOffset(inputBuffer.length());
      offsetAtt.setOffset(ofs, ofs);
   }

   /**
    * {@inheritDoc}
    * 
    * Method is called after the input has been set. This should reset all internal state and adjust to the new input.
    */
   @Override
   public void reset() throws IOException {
      super.reset();

      // read full input into string buffer. This is not very memory efficient
      // but we need a full view of the input document in order to do our
      // pattern matching
      inputBuffer.setLength(0);
      inputBuffer.append(CharStreams.toString(input));

      // reset matcher to the new input
      sentenceMatcher.reset(inputBuffer);

      // we're starting from 0
      index = 0;
   }

   /**
    * {@inheritDoc}
    * 
    * @return <code>true</code> to indicate to the caller to read the current attribute state and <code>false</code> to
    *         indicate the end of the token stream.
    */
   @Override
   public final boolean incrementToken() throws IOException {

      // increment the internal token state but check that we do not overrun the
      // input buffer length
      while (index < inputBuffer.length()) {

         // increment the internal token state until the current state should be
         // emitted from the token stream-
         if (incrementTokenInternal()) {
            return true;
         }
      }

      return false;
   }

   /**
    * 
    * @return <code>true</code> if the current attribute state should be emitted
    */
   protected boolean incrementTokenInternal() throws IOException {

      // find the next split sentence occurence from our current index
      String sentence = null;
      if (sentenceMatcher.find(index)) {

         // get sentence up to the current match
         sentence = inputBuffer.substring(index, sentenceMatcher.end());
      } else {

         // no more matches. Check the remaining chars as candidate
         sentence = inputBuffer.substring(index, inputBuffer.length());
      }

      Matcher commaMatcher = COMMA_PATTERN.matcher(sentence);

      // check for commas in the current sentence.
      if (commaMatcher.find()) {

         // update the comma to word ratio of the whole sentence. If we exceed
         // the threshold,
         int commaCount = 1;
         while (commaMatcher.find()) {
            commaCount++;
         }
         float commaToWordRatio = commaCount / (float) (CharMatcher.WHITESPACE.countIn(sentence) - 1);

         // comma to word ratio does not exceed threshold
         if (commaToWordRatio > commaWordThreshold || lastSentenceFromCommaSplit) {

            commaMatcher.reset();
            commaMatcher.find();
            sentence = sentence.substring(0, commaMatcher.end());

            lastSentenceFromCommaSplit = true;

         }
      } else {
         lastSentenceFromCommaSplit = false;
      }

      // Is this the only sentence in the input?
      boolean isOnlySentence = sentence.length() == inputBuffer.length();

      // should we emit the current sentence?
      boolean emit = isQualitySentence(sentence) || isOnlySentence || !removeBadSentences;
      if (emit) {
         emitSentence(sentence);
      }

      // emitted or not, increase the working index
      index = index + sentence.length();

      // return whether to emit the current sentence
      return emit;
   }

   /**
    * Returns true if the sentence has a high quality.
    * 
    * @param sentence
    *           the sentence.
    */
   private boolean isQualitySentence(CharSequence sentence) {
      SentenceStatistics sentenceStatistics = analyzeSentence(sentence);

      // check information gain
      boolean highInformationGain = sentenceStatistics.getStopwordsRatio() <= maxStopwordRatio;
      boolean shortSentence = sentenceStatistics.getWordCount() < minSentenceLength;

      return highInformationGain || shortSentence;
   }

   /**
    * Emits the given sentence as a token.
    * 
    * @param sentence
    *           Sentence to emit.
    */
   private void emitSentence(CharSequence sentence) {
      termAtt.setEmpty().append(sentence);
      offsetAtt
            .setOffset(correctOffset(index),
                  correctOffset(index + sentence.length()));
      positionIncrement.setPositionIncrement(1);
   }

   /**
    * Analyzes the sentence for stopwords appearances. It will remove whitespaces and symbols from the sentence to
    * guarantee a high stopwords match.
    * 
    * @param sentence
    *           Sentence to analyze.
    */
   private SentenceStatistics analyzeSentence(CharSequence sentence) {
      // remove noise: trim, noise(|<>:;...), multiple whitespace and to lower
      String cleanSentence = WHITESPACE_PATTERN.matcher(SENTENCE_NOISE.removeFrom(
            CharMatcher.WHITESPACE.trimFrom(sentence))).replaceAll(" ")
            .toLowerCase(Locale.GERMAN);

      // split sentence into words
      Iterable<String> words = SPACE_SPLITTER.split(cleanSentence);
      int stopWordCount = 0;
      int wordCount = 0;
      for (String w : words) {
         if (stopWords.contains(w)) {
            stopWordCount++;
         }

         wordCount++;
      }

      // calculate ratio
      return new SentenceStatistics(wordCount, stopWordCount);
   }

   private static class SentenceStatistics {

      private final int wordCount;
      private final int stopwordCount;

      public SentenceStatistics(int wordCount, int stopwordCount) {
         this.wordCount = wordCount;
         this.stopwordCount = stopwordCount;
      }

      public int getWordCount() {
         return wordCount;
      }

      public float getStopwordsRatio() {
         return wordCount > 0 ? stopwordCount / (float) wordCount : 0;
      }

   }
}
