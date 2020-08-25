package autocomplete;

public class Term implements ITerm{
	
	private long Weight;
	private String Term;

	public long getWeight() {
		return Weight;
	}

	public void setWeight(long weight) {
		Weight = weight;
	}

	public String getTerm() {
		return this.Term;
	}

	public void setTerm(String term) {
		Term = term;
	}

	@Override
	public int compareTo(ITerm that) {		

		return this.Term.compareTo(((Term)that).getTerm());
	}

	public Term(String query, long weight) {
		if(query == null || weight < 0)
		{
			throw new IllegalArgumentException();
		}
		Weight = weight;
		Term = query;
	}
	
	@Override
    public String toString()
    {
		return String.valueOf(this.Weight) + "\t" + this.Term;
    }


}
