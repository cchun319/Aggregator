package indexing;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author chunchang
 *
 */
public class IndexBuilder implements IIndexBuilder {

	/**
	 * parseFeed takes list of feeds and go through every links in a feed 
	 * to generate a map of links to their words character by character
	 * return : map<llink of article, list of words in the article>
	 */
	
	@Override
	public Map<String, List<String>> parseFeed(List<String> feeds) {
		Map<String, List<String>> fileToWord = new HashMap<>();
		for(String f : feeds)
		{

			try {
				Document doc = Jsoup.connect(f).get();
				Elements links = doc.getElementsByTag("link");

				for(Element e : links)
				{
					//					System.out.println(e.html());
					Document art = Jsoup.connect(e.html()).get();
					Elements content = art.getElementsByTag("Body");
					String text = content.text();
					List<String> words = null;
					if(!fileToWord.containsKey(e.html()))
					{
						fileToWord.put(e.html(), new ArrayList<String>());
					}
					words = fileToWord.get(e.html());
					String word = "";
					for(int i = 0; i < text.length(); i++)
					{
						if((text.charAt(i) >= 'A' && text.charAt(i) <= 'Z') || (text.charAt(i) >= 'a' && text.charAt(i) <= 'z'))
						{
							word += Character.toLowerCase(text.charAt(i));
						}	
						
						if(text.charAt(i) == ' ' || i == text.length() - 1)
						{
							words.add(word);
							word = "";
						}
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("connection failed");
				e.printStackTrace();
			}

		}

		return fileToWord;
	}

	/**
	 * buildIndex go through the map returned from parseFeed and calculate the number of appearance of a word
	 * and use the quantitative data to summerize TFIDF and create a map by the link of document to terms to TFIDF
	 * return : Map of links of document to Map of terms to their TFIDF
	 */
	@Override
	public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
//		TF(t) = (Number of times term t appears in a document) --> wordscount / (Total number of terms in the document) docs.getsize
//		IDF(t) = log_e(Total number of documents --> numberOfDoc / Number of documents with term t in it).
		
//		a map of a document (key) and a map (value) of the tag terms and their TFIDF values (Map<String, Double>). 
//		The value maps are sorted by lexicographic order on the key (tag term).
		
		Map<String, Map<String, Double>> TFIDF = new HashMap<>(); // TFIDF(Value) of a word(Key2) in a doc(Key)
		
		int numberOfDoc = docs.size();
		Map<String, Map<String, Integer>> wordscount = new HashMap<>(); // number(Value) of word(Key2) appearing in a doc(Key)
		Map<String, Set<String>> wordIndocs = new HashMap<>(); // a set of docs(Value) having a word(Key) appearing in them 
		Map<String, Set<String>> pageToDistinctWord = new HashMap<>(); // set of words(Value) appearing in a doc(Key)

		for (String page : docs.keySet()) // go through every documents
		{
			if(!wordscount.containsKey(page))
			{
				wordscount.put(page, new HashMap<>());
				pageToDistinctWord.put(page, new HashSet<>());
			}
			for(int i = 0; i < docs.get(page).size(); i++) // go through every terms in a doc
			{
				String term = docs.get(page).get(i);
				wordscount.get(page).put(term, wordscount.get(page).getOrDefault(term, 0) + 1);
				pageToDistinctWord.get(page).add(term);
				if (!wordIndocs.containsKey(term))
				{
					wordIndocs.put(term, new HashSet<String>());
				}
				wordIndocs.get(term).add(page);
			}
		}

		for(String doc : pageToDistinctWord.keySet())
		{

			for(String uterm : pageToDistinctWord.get(doc))
			{
				if(!TFIDF.containsKey(doc))
				{
					TFIDF.put(doc, new TreeMap<String, Double>());

				}
				double tf = ((double)wordscount.get(doc).get(uterm)) / docs.get(doc).size();
				double idf = Math.log(((double)numberOfDoc) / wordIndocs.get(uterm).size());
				TFIDF.get(doc).put(uterm, tf * idf);

			}
		}

		return TFIDF;
	}
	
	/**
	 * buildInvertedIndex go through the map returned from buildIndex and generate a list from the previous map and sort it with frequency
	 * or alphabetical order
	 * return : Map of terms to what documents they appears in and TFIDF  
	 */
	@Override
	public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
//		An inverted index is a map of a tag term (the key) and a Collection (value) of entries consisting of a document 
//		and the TFIDF value of the term (in that document).
		
		Map<String, List<Entry<Comparable, Comparable>>> Iidx = new HashMap<>();
		for(String doc : index.keySet())
		{
			for(Map.Entry<String, Double> term : index.get(doc).entrySet())
			{
				if(!Iidx.containsKey(term.getKey()))
				{
					List<Entry<Comparable, Comparable>> mm = new ArrayList<>();
					Iidx.put(term.getKey(), mm);
				}
				Entry<Comparable, Comparable> docToTf = new AbstractMap.SimpleEntry<Comparable, Comparable>(doc, term.getValue()); 
				Iidx.get(term.getKey()).add(docToTf);
			}
		}
		
		for(String term : Iidx.keySet())
		{
			Collections.sort(Iidx.get(term), IndexBuilder.listcomp());
		}
		
		return Iidx;
	}
	
	/**
	 * buildHomePage go through the intertedIndex map and create a collection to store sorted data of map by TFIDF and number of appearance in all
	 * documents
	 * return : a collection of word to their appearance in documents 
	 */

	@Override
	public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
		Set<String> stop = IIndexBuilder.STOPWORDS;

		//invertedIndex --> term, list<page, TF>
		List<Entry<String, List<String>>> pagepool = new ArrayList<>();

		for(Object term : invertedIndex.keySet())
		{
			if(!stop.contains((String)term))
			{
				List<String> pages = new ArrayList<>(); 
				List<Entry<Comparable, Comparable>> PTFS = (List<Entry<Comparable, Comparable>>) invertedIndex.get(term);
				for(Map.Entry<Comparable, Comparable> ptf : PTFS)
				{
					pages.add((String)ptf.getKey());
				}
				Entry<String, List<String>> pagesofterm = new AbstractMap.SimpleEntry<String, List<String>>((String)term, pages);
				pagepool.add(pagesofterm);
			}
		}

		Collections.sort(pagepool, IndexBuilder.listcompForHP());
		

		return pagepool;
	}

	/**
	 * go through the collection generated by homepage and write every key of the collection to outputfile
	 * return : set of terms appearing in all documents
	 */
	@Override
	public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {
		// TODO Auto-generated method stub
		List<String> allwords = new ArrayList<>();
		for(Map.Entry<String, List<String>> words : homepage)
		{
			allwords.add(words.getKey());
		}
		Collections.sort(allwords, IndexBuilder.listcompForAC());

		Path p = Paths.get("./src/autocomplete.txt");
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE));
			String writeTO = "";
			writeTO += String.valueOf(allwords.size());
			writeTO += "\n";
			out.write(writeTO.getBytes(), 0, writeTO.getBytes().length);
			for(String w : allwords)
			{
				writeTO = "0\t" + w + "\n";
				out.write(writeTO.getBytes(), 0, writeTO.getBytes().length);
				out.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
				}
			}
			catch (IOException e) {
				System.out.println("reader does not exist");
			}
		}
		return allwords;
	}
	
	/**
	 * use the query as key to get the collection of articles which have the query appearing in them
	 * return : a list of documents having the query in them
	 */
	@Override
	public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
		// TODO Auto-generated method stub
		List<String> arts = new ArrayList<>();
		List<Entry<Comparable, Comparable>> tflist = (List<Entry<Comparable, Comparable>>) invertedIndex.get(queryTerm);
		for(int i = 0; i < tflist.size() ; i++)
		{
			arts.add((String)tflist.get(i).getKey());
		}
		return arts;
	}

	/**
	 * return : comparator compare TFIDF, if TFIDF is the same, then compare name of the documents
	 */
	public static Comparator<Entry<Comparable, Comparable>> listcomp()
	{
		Comparator<Entry<Comparable, Comparable>> newcomp = new Comparator<Entry<Comparable, Comparable>>() {
			@Override
			public int compare(Entry<Comparable, Comparable> e1, Entry<Comparable, Comparable> e2)
			{
				if(Double.compare((double)e1.getValue(), (double)e2.getValue())!= 0)
				{
					return -1 * Double.compare((double)e1.getValue(), (double)e2.getValue());
				}
				else
				{
					return ((Comparable)e1.getKey()).compareTo((Comparable)e2.getKey());
				}
			}
		};
		return newcomp;
	}

	/**
	 * return : comparator compare number of appearance in all documents of a term, if those are same, compare alphabetical order
	 */
	public static Comparator<Entry<String, List<String>>> listcompForHP()
	{
		Comparator<Entry<String, List<String>>> newcomp = new Comparator<Entry<String, List<String>>>() {
			@Override
			public int compare(Entry<String, List<String>> e1, Entry<String, List<String>> e2)
			{
				if(e1.getValue().size() != e2.getValue().size())
				{
					return e2.getValue().size() - e1.getValue().size();
				}
				else
				{
					return - 1 *((Comparable)e1.getKey()).compareTo((Comparable)e2.getKey());
				}
			}
		};
		return newcomp;
	}

	/**
	 * return : comparator compare alphabetical order
	 */
	public static Comparator<String> listcompForAC()
	{
		Comparator<String> newcomp = new Comparator<String>() {
			@Override
			public int compare(String e1, String e2)
			{
				return e1.compareTo(e2);
			}
		};
		return newcomp;
	}


}
