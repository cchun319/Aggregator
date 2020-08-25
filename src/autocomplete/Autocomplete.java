package autocomplete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Autocomplete implements IAutocomplete {

	private Node root;
	private int su;
	
	public Autocomplete()
	{
		this.root = new Node("", 0);
		this.root.setPrefixes(0);
	}
	
	@Override
	public void addWord(String word, long weight) {
//		System.out.println("add:" + word + "|w: " + String.valueOf(weight));
		for(int i = 0; i < word.length(); i++)
		{
			if(!Character.isLetter(word.charAt(i)))
			{
				return;
			}
		}
		String wordc = word;
		Node sentinal = root;
		int id = 0;
		while(word.length() > 0)
		{
			id = word.charAt(0) - 'a';
			sentinal.setPrefixes(sentinal.getPrefixes() + 1);

//			System.out.println(word);
			if(sentinal.getReferences()[id] == null) 
			{
				//set reference[id] 
				Node[] ref = sentinal.getReferences();
				if(word.length() == 1)//end of word 
				{
					ref[id] = new Node(wordc, weight);
					ref[id].setWords(1);
					ref[id].setPrefixes(1);
				}
				else // have internal node but not a word
				{
					ref[id] = new Node();
					ref[id].setReferences(new Node[26]);
				}
				sentinal.setReferences(ref);
			}
			else
			{
				if(word.length() == 1)//last character
				{
					sentinal = sentinal.getReferences()[id];
					sentinal.setPrefixes(sentinal.getPrefixes() + 1);
					sentinal.setWords(sentinal.getWords() + 1);
					sentinal.setTerm(new Term(wordc, weight));
					// put word into it		
				}
			}
			
			
			if(word.length() == 1)
			{
				word = "";
			}
			else
			{
				word = word.substring(1);
			}
			sentinal = sentinal.getReferences()[id];
		}		
	}

	@Override
	public Node buildTrie(String filename, int k) {
		this.su = k;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				String rl = null;
				br.readLine();
				// ignore first line
				while((rl = br.readLine()) != null)
				{
					String[] wAw = rl.split("[\t ]+", 0);
					long wei = -1;
					int count = 0;
					String wo = null;
					for(String s : wAw)
					{
						if(s.length()!= 0)
						{
							if(count == 0)
							{
								wei = Long.parseLong(s);
							}
							else
							{
								wo = s;
							}
							count++;
						}
					}
					boolean lettercheck = true;
					if(wo != null && wei >= 0)
					{
						for(int i = 0; i < wo.length(); i++)
						{
							if(!Character.isLetter(wo.charAt(i)))
							{
								lettercheck = false;
								break;
							}
						}
						if(lettercheck)
						{
							wo = wo.toLowerCase();
							addWord(wo, wei);
						}
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.root;
	}

	@Override
	public int numberSuggestions() {
		return this.su;
	}

	@Override
	public Node getSubTrie(String prefix) {
		if (prefix == null) // check if it is valid
		{
			return null;
		}
		Node sentinal = root;
		int id = 0;
		while(prefix.length() > 0)
		{
			id = prefix.charAt(0) - 'a';
			if( id < 0 || sentinal.getReferences()[id] == null)
			{
				return null;
			}
			if(prefix.length() > 1)
			{
				prefix = prefix.substring(1);

			}
			else
			{
				prefix = "";
			}
			sentinal = sentinal.getReferences()[id]; // go to subtrie
		}
		return sentinal;
	}

	@Override
	public int countPrefixes(String prefix) {
		Node st = getSubTrie(prefix);
		if (st == null)
		{
			return 0;
		}
		return getSubTrie(prefix).getPrefixes();
	}

	@Override
	public List<ITerm> getSuggestions(String prefix) {
		//using DFS to get the all the word in the subtrie
		Node start = getSubTrie(prefix);
		int size = 0; 
		if(start == null)
		{
			return new ArrayList<ITerm>();
		}
		size = start.getPrefixes();
		List<ITerm> sugs = new ArrayList<>();
		Node sen = null;
		Stack<Node> ex = new Stack<>();
		ex.add(start);
		while (!ex.isEmpty())
		{
			sen = ex.pop();
			for(int i = sen.getReferences().length - 1; i >= 0 ; i--)
			{
				if(sen.getReferences()[i] != null)
				{
					ex.add(sen.getReferences()[i]);
				}
			}
			if(sen.getWords() == 1)
			{
				sugs.add(sen.getTerm());
			}
		}
		
//		for(int i = 0; i < sugs.size(); i++)
//		{
//			System.out.println(sugs.get(i));
//		}
//		System.out.println();
		
		return sugs;
	}

}
