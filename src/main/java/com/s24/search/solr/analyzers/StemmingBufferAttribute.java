package com.s24.search.solr.analyzers;

import org.apache.lucene.util.Attribute;

public interface StemmingBufferAttribute extends Attribute {
   
   public void setOriginalToken(char[] buffer, int length);

   public char[] getOriginalToken();

   public int getOriginalTokenLength();
   
   public void setStemmedToken(char[] buffer, int length);

   public char[] getStemmedToken();

   public int getStemmedTokenLength();

   boolean isStemmedTokenHasBeenEmitted();

   void setStemmedTokenHasBeenEmitted(boolean stemmedTokenHasBeenEmitted);
   
   void clear();

}
