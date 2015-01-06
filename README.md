SyntacticParsing 
======================

This project is very useful for using the stanford parser to generate syntantic parse tree from sentences.  

- Can run the  parser as a socket server
- Can run the  parser by loading from a file
- Can read the parse tree from a string 
- Utility code to read a xml configuration file

Using the code
-----------------

- Look at com.sanjaymeena.tutorials.stanfordparser.Demo file for using the code to generate syntactic parse tree. 
- The function, "ParserUtilities.getInstance().parseEnglishSentence(String sentence)"  produces the syntactic parse trees. 
- It tries to connect to the socket server first. If not found, this function will load the required files by itself. 


Running the parser on a socket server
---------------------------------------------------
- Stanford parser can be run on a socket server. 
- The configuration XML file is kept at "resources/english_stanford_parser.xml"
- You can use the shell-script/bat files provided with this project to run the  parser on a socket server. 

