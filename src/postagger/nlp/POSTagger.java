package postagger.nlp;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.pljava.ResultSetProvider;
import postagger.util.TaggedResult;
import postagger.util.TaggedResultProvider;

//CMU Ark Tweet NLP package (GPL v2 license)
import cmu.arktweetnlp.*;
import cmu.arktweetnlp.Tagger.TaggedToken;

/**
 * Demonstrate part-of-speech tagging for tweets using the CMU Ark-Tweet-NLP package.
 * Refer to : http://www.ark.cs.cmu.edu/TweetNLP/ for more information about the toolkit and the papers
 * @author Srivatsan Ramanujam <vatsan.cs@utexas.edu>
 *
 */
public class POSTagger {
	//The model is already packaged with the arktweetnlp.jar
	public static final String modelFileName =  "/cmu/arktweetnlp/model.20120919";
	public static final Tagger tagger = new Tagger();
	
	static {
        //Load the pre-trained model
        try {
        	tagger.loadModel(modelFileName);
        } catch(IOException e) {
        	//ignore
        	e.printStackTrace();
        }
	}
	
	/**
	 * Return a tuple containing the tokenized tweet and the corresponding part-of-speech tags
	 * @param tweet
	 * @return
	 */
    public static ResultSetProvider tagTweet(String tweet) {
        if (tweet == null) {
            return null;
        }
        
		//Tokenize and tag the input tweet
		List<TaggedToken> taggedTokens = tagger.tokenizeAndTag(tweet);
		List<TaggedResult> result = new ArrayList<TaggedResult>();
		int idx=0;
		// Return a set of [token index, token, tag] tuples
		for (TaggedToken tt:taggedTokens) {
		     result.add(new TaggedResult(idx, removeSurrogates(tt.token), tt.tag));
		     idx++;
		}
		return new TaggedResultProvider(result);
    }
    
    /**
     * Tokenize tag and return the results as a string.
     * @param tweet
     * @return
     */
    public static String tagTweetStr(String tweet) {
        if (tweet == null) {
            return null;
        }
        
		//Tokenize and tag the input tweet
		List<TaggedToken> taggedTokens = tagger.tokenizeAndTag(tweet);
		StringBuffer result = new StringBuffer();
		// Return a set of [token index, token, tag] tuples
		int idx=0;
		for (TaggedToken tt:taggedTokens) {
			 result.append(String.valueOf(idx)+":"+tt.token+":"+tt.tag+"\n");
			 idx++;
		}
		return result.toString();
    }
    
    /**
     * Any invalid UTF-8 chars have to be removed from the output.
     * This can happen if there are unmatched surrogates in the string (the tokenizer seems to create this).
     * @param tweet
     * @return
     */
    protected static String removeSurrogates(String text) {
    	if(text==null) {
    		return text;
    	}
    	StringBuffer result = new StringBuffer();
    	for(int indx=0; indx<text.length();indx++) {
            char c = text.charAt(indx);
            //Low or high surrogates will be stripped out (the input is expected to not contain surrogates
            //as PL/Java mangles non-BMP chars. As a result we also check that the ark-tweet-nlp tokenizer doesn't create any invalid 
            //or unmatched surrogates.
            if(!(Character.isLowSurrogate(c) || Character.isHighSurrogate(c))) {
            	result.append(c);
            }
    	}
    	return result.toString();
    }
    
    
    /**
     * Show invocation from command line
     * @param args
     */
    public static void main(String[] args) {
    	if(args.length != 1) {
    		System.out.println("\nUsage: java -jar <jar filename> <Tweet in double quotes>");
    	} else {
    		String result = tagTweetStr(args[0]);
    		System.out.println("\nResults:"+result);
    	}
    }
}