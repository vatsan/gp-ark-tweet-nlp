package postagger.util;

/**
 * Class to marshal the tagged result
 * @author Srivatsan Ramanujam<vatsan.cs@utexas.edu>
 *
 */
public class TaggedResult {
	private int index;
	private String token;
	private String tag;
	
	public TaggedResult(int index, String tok, String tg) {
		this.index = index;
		this.token = tok;
		this.tag = tg;
	}
	
	/**
	 * Index of the token
	 * @return
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * The actual token
	 * @return
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * The part-of-speech tag corresponding to the token
	 * @return
	 */
	public String getTag() {
		return tag;
	}
}

