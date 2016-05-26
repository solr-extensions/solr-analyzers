package com.s24.search.solr.analyzers;

import java.util.Arrays;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

/**
 * Attribute that stores the original and the stemmed token.
 * 
 * @author Shopping24 GmbH
 */
public class StemmingBufferAttributeImpl extends AttributeImpl implements StemmingBufferAttribute {

   char[] stemmedToken = new char[16];
   int stemmedTokenLength = 0;

   char[] originalToken = new char[16];
   int originalTokenLength = 0;

   private boolean stemmedTokenHasBeenEmitted = false;

   @Override
   public void clear() {
      stemmedTokenLength = 0;
      originalTokenLength = 0;
      setStemmedTokenHasBeenEmitted(false);
   }

   /**
    * We just want to create a new array if the length differs.
    */
   @Override
   public void copyTo(AttributeImpl input) {
      StemmingBufferAttributeImpl copyAttributeImpl = (StemmingBufferAttributeImpl) input;
      if (copyAttributeImpl.stemmedToken.length < stemmedTokenLength) {
         copyAttributeImpl.stemmedToken = new char[stemmedTokenLength];
      }
      System.arraycopy(stemmedToken, 0, copyAttributeImpl.stemmedToken, 0, stemmedTokenLength);

      if (copyAttributeImpl.originalToken.length < originalTokenLength) {
         copyAttributeImpl.originalToken = new char[originalTokenLength];
      }
      System.arraycopy(originalToken, 0, copyAttributeImpl.originalToken, 0, originalTokenLength);

      copyAttributeImpl.stemmedTokenLength = stemmedTokenLength;
      copyAttributeImpl.originalTokenLength = originalTokenLength;
      copyAttributeImpl.stemmedTokenHasBeenEmitted = stemmedTokenHasBeenEmitted;
   }

   @Override
   public void setOriginalToken(char[] buffer, int length) {
      originalToken = assureArrayLenth(originalToken, length);
      System.arraycopy(buffer, 0, originalToken, 0, length);
      originalTokenLength = length;
   }

   @Override
   public void setStemmedToken(char[] buffer, int length) {
      stemmedToken = assureArrayLenth(stemmedToken, length);
      System.arraycopy(buffer, 0, stemmedToken, 0, length);
      stemmedTokenLength = length;
   }

   @Override
   public char[] getOriginalToken() {
      return originalToken;
   }

   @Override
   public int getOriginalTokenLength() {
      return originalTokenLength;
   }

   @Override
   public char[] getStemmedToken() {
      return stemmedToken;
   }

   @Override
   public int getStemmedTokenLength() {
      return stemmedTokenLength;
   }

   @Override
   public boolean isStemmedTokenHasBeenEmitted() {
      return stemmedTokenHasBeenEmitted;
   }

   @Override
   public void setStemmedTokenHasBeenEmitted(boolean stemmedTokenHasBeenEmitted) {
      this.stemmedTokenHasBeenEmitted = stemmedTokenHasBeenEmitted;
   }

   protected char[] assureArrayLenth(char[] array, int length) {
      if (array.length < length) {
         char[] newArray = new char[length];
         System.arraycopy(array, 0, newArray, 0, array.length);
         return newArray;
      } else {
         return array;
      }
   }

   @Override
   public String toString() {
      return "StemmingBufferAttributeImpl [stemmedToken=" + Arrays.toString(stemmedToken) + ", stemmedTokenLength="
            + stemmedTokenLength + ", originalToken=" + Arrays.toString(originalToken) + ", originalTokenLength="
            + originalTokenLength + "]";
   }

   @Override
   public void reflectWith(AttributeReflector reflector) {
      reflector.reflect(StemmingBufferAttribute.class, "stemmedToken", stemmedToken);
      reflector.reflect(StemmingBufferAttribute.class, "stemmedTokenLength", stemmedTokenLength);
      reflector.reflect(StemmingBufferAttribute.class, "originalToken", originalToken);
      reflector.reflect(StemmingBufferAttribute.class, "originalTokenLength", originalTokenLength);
   }
}
