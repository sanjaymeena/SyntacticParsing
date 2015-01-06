package com.sanjaymeena.tutorials.stanfordparser.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashMap;

import com.sanjaymeena.tutorials.stanfordparser.utilities.dom.SimpleDomParser;
import com.sanjaymeena.tutorials.stanfordparser.utilities.dom.SimpleElement;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;

/**
 * Utility Class which provides many functions useful for question generation.
 * 
 * @author Sanjay_Meena
 */
public class ParserUtilities {

	private ParserUtilities() {
		englishStanfordParser = null;
		tree_factory = new LabeledScoredTreeFactory();

	}

	/**
	 * Return instance of this class
	 * 
	 * @return AnalysisUtilities
	 */
	public static ParserUtilities getInstance() {
		if (instance == null) {
			instance = new ParserUtilities();
		}
		return instance;
	}

	/**
	 * function which sends the sentence to the stanford parser and retrieves
	 * the parse tree from it.
	 * 
	 * @param sentence
	 *            source sentence
	 * @return the parsed result
	 */
	public ParseResult parseEnglishSentence(String sentence) {
		String result = "";
		// Tree parse = null;
		// double parseScore = Double.MIN_VALUE;
		//
		// System.err.println(sentence);
		// see if a parser socket server is available
		int port = 5556;
		String host = "127.0.0.1";
		Socket client;
		PrintWriter pw;
		BufferedReader br;
		String line;
		Tree parse = null;
		double parseScore = Double.MIN_VALUE;

		try {
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			pw.println(sentence);
			pw.flush(); // flush to complete the transmission

			/**
			 * 1)Removed the ready method. It was giving issues 2)Removed the
			 * else condition and parseScore method
			 */
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("\n", "");
				line = line.replaceAll("\\s+", " ");
				result += line + " ";

			}

			br.close();
			pw.close();
			client.close();

			if (parse == null) {
				parse = readTreeFromString("(ROOT (. .))");
				parseScore = -99999.0;
			}

			System.err.println("result (parse):" + result);
			parse = readTreeFromString(result);
			return new ParseResult(true, parse, parseScore);

		} catch (Exception ex) {

			System.err.println("Could not connect to parser server.");
			// ex.printStackTrace();
		}

		System.err.println("parsing:" + sentence);

		// if socket server not available, then use a local parser object
		if (englishStanfordParser == null) {
			try {
				readconfig(CONFIG_FILE);
				port = Integer.parseInt(preferences.get("port"));
				int maxLength = Integer.parseInt(preferences.get("maxLength"));
				String lexparserModel = preferences.get("lexparser");

				Options op = new Options();

				String[] options = { "-maxLength", Integer.toString(maxLength),
						"-outputFormat", "oneline" };

				op.setOptions(options);
				englishStanfordParser = LexicalizedParser.loadModel(
						lexparserModel, op);

				/**
				 * Not applicable in the new version of Stanford.
				 */
				// parser.setMaxLength();
				// parser.setOptionFlags("maxLength",
				// Integer.toString(maxLength),"-outputFormat", "oneline");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {

			parse = englishStanfordParser.parse(sentence);
			if (parse != null) {

				// remove all the parent annotations (this is a hacky way to do
				// it)
				String ps = parse.toString().replaceAll(
						"\\[[^\\]]+/[^\\]]+\\]", "");
				// System.out.println("Hello......   " + ps);
				parse = ParserUtilities.getInstance().readTreeFromString(ps);

				parseScore = 0.0;
				return new ParseResult(true, parse, parseScore);
			}
		} catch (Exception e) {
		}

		parse = readTreeFromString("(ROOT (. .))");
		parseScore = -99999.0;
		return new ParseResult(false, parse, parseScore);
	}

	/**
	 * Read tree from a string
	 * 
	 * @param parseStr
	 *            input tree in form a string
	 * @return tree
	 */
	public Tree readTreeFromString(String parseStr) {
		// read in the input into a Tree data structure
		TreeReader treeReader = new PennTreeReader(new StringReader(parseStr),
				tree_factory);
		Tree inputTree = null;
		try {
			inputTree = treeReader.readTree();
			treeReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputTree;
	}

	/**
	 * @param config
	 */
	private static void readconfig(String config) {
		SimpleElement configuration = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(config));
			SimpleDomParser sdp = new SimpleDomParser();
			configuration = sdp.parse(br);

			if (configuration == null)
				throw new Exception("Error reading configuration file");
			// System.out.println( "configuration == " + configuration );
			preferencesFromXML(configuration);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void preferencesFromXML(SimpleElement configuration) {

		for (int i = 0; i < configuration.getChildElements().size(); i++) {
			SimpleElement element = configuration.getChildElements().get(i);
			if (element.getTagName().equals("preference")) {
				String name = element.getAttribute("name");
				String value = element.getAttribute("value");
				preferences.put(name, value);
			}
		}
	}

	
	/**
	 * This class is to represent the parse tree of a sentence received from stanford parser
	 * 
	 * 
	 * @author Sanjay_Meena
	 * @see  ParserUtilities
	 */
	public class ParseResult {
		/**
		 * 
		 */
		public boolean success;
		/**
		 * 
		 */
		public Tree parse;
		/**
		 * 
		 */
		public double score;
		/**
		 * @param s
		 * @param p
		 * @param sc
		 */
		public ParseResult(boolean s, Tree p, double sc) { success=s; parse=p; score=sc; }
	}
	
	
	
	
	
	
	
	private static HashMap<String, String> preferences = new HashMap<String, String>();
	private static String CONFIG_FILE = "resources" + File.separator
			+ "english_stanford_parser.xml";
	

	private LexicalizedParser englishStanfordParser;
	private static ParserUtilities instance;
	private LabeledScoredTreeFactory tree_factory;

}
