package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import org.junit.Test;

import indexing.IndexBuilder;

/**
 * @author ericfouh
 */
public class TestIndexBuilder
{
//	- Your map has the correct number of files
//	- Your map contains the names of the documents (URLs/keys)
//	- Your map contains the correct number of terms in the lists (values)

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void testparseFeed()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		assertEquals(5, docs.size());
		   
		assertTrue(docs.containsKey("http://localhost:8090/page1.html"));
		assertTrue(docs.containsKey("http://localhost:8090/page2.html"));
		assertTrue(docs.containsKey("http://localhost:8090/page3.html"));
		assertTrue(docs.containsKey("http://localhost:8090/page4.html"));
		assertTrue(docs.containsKey("http://localhost:8090/page5.html"));

		assertEquals(10, docs.get("http://localhost:8090/page1.html").size());
		assertEquals(55, docs.get("http://localhost:8090/page2.html").size());
		assertEquals(33, docs.get("http://localhost:8090/page3.html").size());
		assertEquals(22, docs.get("http://localhost:8090/page4.html").size());
		assertEquals(18, docs.get("http://localhost:8090/page5.html").size());
		
	}
	
	@Test
	public void testbuildIndex()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		Map<String, Map<String, Double>> TFIDF = id.buildIndex(docs);
		assertEquals(TFIDF.get("http://localhost:8090/page1.html").get("data"), 0.10,0.01);
		assertEquals(TFIDF.get("http://localhost:8090/page2.html").get("data"), 0.04,0.01);
		assertEquals(TFIDF.get("http://localhost:8090/page3.html").get("data"), 0.01,0.01);
		assertEquals(TFIDF.get("http://localhost:8090/page4.html").get("do"), 0.14,0.01);
		assertEquals(TFIDF.get("http://localhost:8090/page5.html").get("lets"), 0.08,0.01);
	}
	
	@Test
	public void testbuildInvertedIndex()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		Map<String, Map<String, Double>> TFIDF = id.buildIndex(docs);
		Map<?, ?> invertedIndex = id.buildInvertedIndex(TFIDF);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("linear")).get(0).getValue(), 0.09,0.01);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("linear")).get(0).getKey().compareTo("http://localhost:8090/page1.html"), 0);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("linear")).get(1).getValue(), 0.01,0.01);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("linear")).get(1).getKey().compareTo("http://localhost:8090/page2.html"), 0);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("with")).get(0).getValue(), 0.04,0.01);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("with")).get(0).getKey().compareTo("http://localhost:8090/page4.html"), 0);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("with")).get(1).getValue(), 0.02,0.01);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("with")).get(1).getKey().compareTo("http://localhost:8090/page3.html"), 0);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("categorization")).get(0).getValue(), 0.08,0.01);
		assertEquals(((List<Entry<String, Double>>)invertedIndex.get("categorization")).get(0).getKey().compareTo("http://localhost:8090/page5.html"), 0);

	}
	
	@Test
	public void testbuildHomePage()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		Map<String, Map<String, Double>> TFIDF = id.buildIndex(docs);
		Map<?, ?> invertedIndex = id.buildInvertedIndex(TFIDF);
		Collection<Entry<String, List<String>>> pages = id.buildHomePage(invertedIndex);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(0).getValue().get(0).compareTo("http://localhost:8090/page1.html"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(0).getValue().get(1).compareTo("http://localhost:8090/page2.html"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(0).getValue().get(2).compareTo("http://localhost:8090/page3.html"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(0).getKey().compareTo("data"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(1).getKey().compareTo("trees"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(1).getValue().get(0).compareTo("http://localhost:8090/page3.html"), 0);
		assertEquals(((List<Entry<String, List<String>>>)pages).get(1).getValue().get(1).compareTo("http://localhost:8090/page2.html"), 0);

	}
	
	@Test
	public void testcreateAutocompleteFile()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		Map<String, Map<String, Double>> TFIDF = id.buildIndex(docs);
		Map<?, ?> invertedIndex = id.buildInvertedIndex(TFIDF);
		Collection<Entry<String, List<String>>> pages = id.buildHomePage(invertedIndex);
		Collection<?> auc = id.createAutocompleteFile(pages);
		assertEquals(((List<String>)auc).get(0).compareTo("allows"), 0);
		assertEquals(((List<String>)auc).get(1).compareTo("arraylist"), 0);
		assertEquals(((List<String>)auc).get(2).compareTo("binary"), 0);
		assertEquals(((List<String>)auc).get(3).compareTo("categorization"), 0);
		assertEquals(((List<String>)auc).get(4).compareTo("cit"), 0);
		
	}
	
	@Test
	public void testsearchArticles()
	{
		IndexBuilder id = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> docs = id.parseFeed(feeds);
		Map<String, Map<String, Double>> TFIDF = id.buildIndex(docs);
		Map<?, ?> invertedIndex = id.buildInvertedIndex(TFIDF);
		List<String> sug = id.searchArticles("data", invertedIndex);
		assertEquals(sug.get(0).compareTo("http://localhost:8090/page1.html"), 0);
		assertEquals(sug.get(1).compareTo("http://localhost:8090/page2.html"), 0);
		assertEquals(sug.get(2).compareTo("http://localhost:8090/page3.html"), 0);
		sug = id.searchArticles("trees", invertedIndex);
		assertEquals(sug.get(0).compareTo("http://localhost:8090/page3.html"), 0);
		assertEquals(sug.get(1).compareTo("http://localhost:8090/page2.html"), 0);
	}
}
