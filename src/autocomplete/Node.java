package autocomplete;
/**
 * ==== Attributes ====
 * - words: number of words
 * - term: the ITerm object
 * - prefixes: number of prefixes 
 * - references: Array of references to next/children Nodes
 * 
 * ==== Constructor ====
 * Node(String word, long weight)
 * 
 * @author Your_Name
 */
public class Node
{
    //TODO
	 private int words;//: number of words 0 / 1
	 private ITerm term;//: the ITerm object
	 private int prefixes;//: number of prefixes 
	 private Node[] references;// Array of references to next/children Nodes


	 public Node()
	 {
		
	 }
	 
	 public Node(String word, long weight)
	 {
		 super();
		 if(word == null || weight < 0)
			{
				throw new IllegalArgumentException();
			}
		 this.prefixes = 0;
		 this.term = new Term(word, weight);
		 this.words = 0;
		 this.references = new Node[26];
	 }

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public Term getTerm() {
		return (Term) term;
	}

	public void setTerm(ITerm term) {
		this.term = term;
	}

	public int getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(int prefixes) {
		this.prefixes = prefixes;
	}

	public Node[] getReferences() {
		return references;
	}

	public void setReferences(Node[] references) {
		this.references = references;
	}

}
