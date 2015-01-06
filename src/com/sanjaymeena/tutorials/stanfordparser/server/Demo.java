package com.sanjaymeena.tutorials.stanfordparser.server;

import com.sanjaymeena.tutorials.stanfordparser.utilities.ParserUtilities;

import edu.stanford.nlp.trees.Tree;

public class Demo {

	public static void main(String[] args) {
		
		String str="Diane felt manipulated by her beagle Santana , whose big , brown eyes pleaded for another cookie .";
		Tree tree=ParserUtilities.getInstance().parseEnglishSentence(str).parse;
		System.out.println(tree);
	}

}
