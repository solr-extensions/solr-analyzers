A repository for all open source tokenizers and filters from shopping24.
==================

![travis ci build status](https://travis-ci.org/shopping24/solr-analyzers.png)

## AnalyzingSentenceTokenizer

This analyzer will filter sentences from text in a efficient way that contains a lot (defined by a threshold) of stopwords. Could be used as a filter for SEO-text from product descriptions.

Example usage in your field types after you put the jar (`solr-analyzers-<VERSION>-jar-with-dependencies.jar`) into your solr `lib` dir:

     <!-- Use the sentence tokenizer, which removes "noise" sentences and keeps only "signal" -->
     <tokenizer class="com.s24.search.solr.analyzers.AnalyzingSentenceTokenizerFactory"
                stopwordfile="list_of_stopwords.txt"
                filter="true" />
                
Arguments:
* `stopwordfile (required)`: List of stopwords.
* `filter`: Set to true if the sentences should be filtered out.
* `commaWordThreshold`: Threshold that defines the "comma density" that, if exceeded, causes a sentence to be split into sub-sentences that are analyzed individually.
* `maxStopwordRatio`: Ratio of stopwords exceeds this threshold, the sentence is filtered out.
* `minSentenceLength`: Sentence must contain at least this many words, otherwise it is not analyzed and always emitted.
	

## Building the project

This should install the current version into your local repository

    $ mvn clean install
    
### Releasing the project to maven central
    
Define new versions
    
    $ export NEXT_VERSION=<version>
    $ export NEXT_DEVELOPMENT_VERSION=<version>-SNAPSHOT

Then execute the release chain

    $ mvn org.codehaus.mojo:versions-maven-plugin:2.8.1:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_VERSION
    $ git commit -a -m "pushes to release version $NEXT_VERSION"
    $ git tag -a v$NEXT_VERSION -m "`curl -s http://whatthecommit.com/index.txt`"
    $ mvn -P release
    
Then, increment to next development version:
    
    $ mvn org.codehaus.mojo:versions-maven-plugin:2.8.1:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_DEVELOPMENT_VERSION
    $ git commit -a -m "pushes to development version $NEXT_DEVELOPMENT_VERSION"
    $ git push origin tag v$NEXT_VERSION && git push origin

## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).