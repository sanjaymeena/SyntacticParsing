package com.sanjaymeena.tutorials.stanfordparser.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import com.sanjaymeena.tutorials.stanfordparser.utilities.dom.SimpleDomParser;
import com.sanjaymeena.tutorials.stanfordparser.utilities.dom.SimpleElement;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

/**
 * @author Sanjay_Meena
 * 
 */
public class EnglishStanfordParserServer {

	private static HashMap<String, String> preferences = new HashMap<String, String>();
	private static String CONFIG_FILE = "resources" + File.separator+ "english_stanford_parser.xml";
	static MaxentTagger posTagger;
	static LexicalizedParser lexparser;
	
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
     * 
     */
	public static void parserServer() {

		// INITIALIZE PARSER
	
		String lexparserModel = null;
		int port = 5556;
		int maxLength = 40;
		// variables needed to process the files to be parse
		String sentenceDelimiter = null;
		String postaggerModelFile = "";

		// String filePath = "resources/testdata/Articles/test##test.txt";

		readconfig(CONFIG_FILE);

		// variables needed to process the files to be parse
		port = Integer.parseInt(preferences.get("port"));
		maxLength = Integer.parseInt(preferences.get("maxLength"));
		
		lexparserModel = preferences.get("lexparser");
		postaggerModelFile = preferences.get("postagger");

		sentenceDelimiter = preferences.get("sentence");

		System.err.println("maxlength = " + maxLength);
		System.err.println("port = " + port);
		
		// LexicalizedParser lp = null;
		// so we load a serialized parser
		String[] options = { "-maxLength", Integer.toString(maxLength),
				"-outputFormat", "oneline" };

		Options op = new Options();
		op.setOptions(options);
		
		try {
			posTagger = new MaxentTagger(postaggerModelFile);
			lexparser = LexicalizedParser.loadModel(lexparserModel);
			lexparser.setOptionFlags(options);
			

		} catch (IllegalArgumentException e) {
			System.err.println("Error loading parser, exiting...");
			System.exit(0);
		}
		

		// declare a server socket and a client socket for the server
		// declare an input and an output stream
		ServerSocket parseServer = null;
		BufferedReader br;
		PrintWriter outputWriter;
		Socket clientSocket = null;
		try {
			parseServer = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e);
		}

		// Create a socket object from the ServerSocket to listen and accept
		// connections.
		// Open input and output streams

		while (true) {
			System.err.println("Waiting for Connection on Port: " + port);
			try {
				clientSocket = parseServer.accept();
				System.err.println("Connection Accepted From: "
						+ clientSocket.getInetAddress());
				br = new BufferedReader(new InputStreamReader(
						new DataInputStream(clientSocket.getInputStream()),
						"UTF-8"));
				outputWriter = new PrintWriter(
						new OutputStreamWriter(clientSocket.getOutputStream(),
								StandardCharsets.UTF_8), true);

				String doc = "";

				do {
					doc += br.readLine();
				} while (br.ready());
				System.err.println("received: " + doc);

				// PARSE
				try {
					

					Reader sr = new StringReader(doc);
					List<List<HasWord>> sentences = MaxentTagger
							.tokenizeText(sr);

					List<TaggedWord> tSentence = posTagger.tagSentence(sentences
							.get(0));
					System.err.println("Taggedwords: " + tSentence);
					Tree tree = lexparser.apply(tSentence);

					String output = tree.toString();
					outputWriter.println(output);

					System.err.println("best factored parse:\n"
							+ tree.toString());

				} catch (Exception e) {
					outputWriter.println("(ROOT (. .))");
					outputWriter.println("-999999999.0");
					e.printStackTrace();
				}

				outputWriter.flush();
				outputWriter.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		parserServer();

	}

}
