package com.intuit.spc.foundations.portability.text.regularExpressions;

/**
 * A regular expression utility class.
 */
public final class SpcfRegexUtil 
{
	private SpcfRegexUtil() {}
	
	/**
	 * Determines if a mata-character at the specified index location in the input regex is escaped. <p>
	 * 
	 * The method does not care if the input meta-character is really a meta-character.
	 * @param inputRegex regular expression which needs to be inspceted
	 * @param metaChar meta-character to be inspected
	 * @param metaCharIdx meta-character index in the input regex
	 * @return true if the meta-character is escaped
	 */
	public static boolean isEscaped(String inputRegex, char metaChar, int metaCharIdx)
    {
         boolean escaped = false;
         int escapeCount = backtrackContinuousEscapeCount(inputRegex, metaCharIdx-1);
         if(escapeCount > 0 && escapeCount%2 == 1)
         {  
        	 escaped = true;
         }
         return escaped;
     }
	
	/**
	  * Returns number of continuous escape characters (\) from a given index position. 
	  * The escape characters are searched backward from the given index position(inclusive).
	  * @param inputRegex regular expression which needs to be inspceted
	  * @param fromIdx index position from where to start the search, this position must contain the first 
      * escape character.
	  * @return -1 if any of the input parameters are incorrect else returns the count.
	  */
	public static int backtrackContinuousEscapeCount(String inputRegex, int fromIdx)
	{	 
		 if( inputRegex == null || inputRegex.length() == 0 || fromIdx < 0 || 
				 fromIdx >= inputRegex.length() || inputRegex.charAt(fromIdx) != '\\')
		 {
			 return -1;
		 }
		 
		 int escapeCount = 0;		  
		 while(fromIdx >= 0)
		 {
			 if(inputRegex.charAt(fromIdx) == '\\')
			 {
				 escapeCount++;
				 --fromIdx;
			 }
			 else
			 {
				 break;
			 }
		 }
		 return escapeCount;
	 }	 
	 
	 /**
	  * Returns the corresponding opening bracket for a given closing bracket. <p>
	  * 
	  * Acceptable opening brackets are [, (, { and their corresponding closing brackets are ], ), }.
	  * @param closingBracket closing bracket for which opening bracket needs to be found
	  * @return opening bracket or an ampty character-'\\0', if the input character is not valid.
	  */
	public static char getOpeningBracket(char closingBracket)
     {
         char openingBracket = '\0';
         switch (closingBracket)
         {
             case ']':
                 openingBracket = '[';
                 break;
             case ')':
                 openingBracket = '(';
                 break;
             case '}':
                 openingBracket = '{';
                 break;
         }
         return openingBracket;
     }
	 
	 /**
	  * Returns the corresponding closing bracket for a given opening bracket. <p>
	  * 
	  * Acceptable closing brackets are ], ), } and their corresponding opening brackets are [, (, {.
	  * @param openingBracket opening bracket for which closing bracket needs to be found
	  * @return closing bracket or an ampty character-'\\0', if the input character is not valid.
	  */
	public static char getClosingBracket(char openingBracket)
     {
         char closingBracket = '\0';
         switch (openingBracket)
         {
             case '[':
                 closingBracket = ']';
                 break;
             case '(':
                 closingBracket = ')';
                 break;
             case '{':
                 closingBracket = '}';
                 break;
         }
         return closingBracket;
     }
	 
	 /**
	  * Returns index of a opening bracket for a corresponding closing bracket.
	  * @param inputRegex regex in which the opening bracket needs to be searched
	  * @param openingBracket opening bracket which needs to be searched
	  * @param closingBracketIdx index of the closing bracket for which corresponding opening bracket needs to be searched 
	  * @return index of the opening bracket or -1 if any of the input params are invalid.
	  * @throws SpcfPatternSyntaxException if brackets are unbalanced
	  */
	 public static int findOpeningBracketIdx(String inputRegex, char openingBracket, int closingBracketIdx)
     {            
         char closingBracket = getClosingBracket(openingBracket);
         if (closingBracket == '\0' || inputRegex == null || inputRegex.length() == 0 ||
             closingBracketIdx < 0 || (closingBracketIdx >= inputRegex.length()) ||
             inputRegex.charAt(closingBracketIdx) != closingBracket)
         {
             return -1;
         }

         int openingBracketIdx = -1;
         int openingBracketsCount = 0;
         int closingBracketsCount = 1;
         int i = closingBracketIdx - 1;
         while (i >= 0)
         {
             if ((inputRegex.charAt(i) == openingBracket) && !isEscaped(inputRegex, inputRegex.charAt(i), i))
             {
                 if (openingBracket == '(' || openingBracket == '[')
                 {
                     ++openingBracketsCount;
                     if ((openingBracketsCount - closingBracketsCount) == 0)
                     {
                         openingBracketIdx = i;
                         break;
                     }
                 }
                 else
                 {
                     openingBracketIdx = i;
                     break;
                 }
             }
             else if ((inputRegex.charAt(i) == closingBracket) && !isEscaped(inputRegex, inputRegex.charAt(i), i))
             {
                 if (closingBracket == ')' || closingBracket == ']')
                 {
                     ++closingBracketsCount;
                     if ((openingBracketsCount - closingBracketsCount) == 0)
                     {
                         openingBracketIdx = i;
                         break;
                     }
                 }
                 else
                 {
                     throw new SpcfPatternSyntaxException("Unbalanced brackets count");
                 }
             }                             
             --i;
         }

         if (openingBracketIdx == -1)
         {
             throw new SpcfPatternSyntaxException("Unbalanced brackets count");
         }
         return openingBracketIdx;
     }
	 
	 /**
	  * Returns index of a closing bracket for a corresponding opening bracket.
	  * @param inputRegex regex in which the closing bracket needs to be searched
	  * @param closingBracket closing bracket which needs to be searched
	  * @param openingBracketIdx index of the opening bracket for which corresponding closing bracket needs to 
      * be searched 
	  * @return index of the closing bracket or -1 if any of the input params are invalid.
	  * @throws SpcfPatternSyntaxException if brackets are unbalanced
	  */
	 public static int findClosingBracketIdx(String inputRegex, char closingBracket, int openingBracketIdx)
     {            
         char openingBracket = getOpeningBracket(closingBracket);
         if (openingBracket == '\0' || inputRegex == null || inputRegex.length() == 0 ||
              openingBracketIdx < 0 || (openingBracketIdx >= inputRegex.length()) ||
             (inputRegex.charAt(openingBracketIdx) != openingBracket))
         {
             return -1;
         }

         int closingBracketIdx = -1;
         int openingBracketsCount = 1;
         int closingBracketsCount = 0;
         int i = openingBracketIdx + 1;
         while (i < inputRegex.length())
         {
             if ((inputRegex.charAt(i) == closingBracket) && !isEscaped(inputRegex, inputRegex.charAt(i), i))
             {
                 if (closingBracket == ')' || closingBracket == ']')
                 {
                     ++closingBracketsCount;
                     if ((openingBracketsCount - closingBracketsCount) == 0)
                     {
                         closingBracketIdx = i;
                         break;
                     }
                 }
                 else
                 {
                     closingBracketIdx = i;
                     break;
                 }
             }
             else if ((inputRegex.charAt(i) == openingBracket) && !isEscaped(inputRegex, inputRegex.charAt(i), i))
             {
                 if (openingBracket == '(' || openingBracket == '[')
                 {
                     ++openingBracketsCount;
                     if ((openingBracketsCount - closingBracketsCount) == 0)
                     {
                         closingBracketIdx = i;
                         break;
                     }
                 }
                 else
                 {
                     throw new SpcfPatternSyntaxException("Unbalanced brackets count");
                 }
             }
             ++i;
         }

         if (closingBracketIdx == -1)
         {
             throw new SpcfPatternSyntaxException("Unbalanced brackets count");
         }
         return closingBracketIdx;
     }
	 
	 /**
	  * Determines if a given character index in the inputRegex falls withing the character class. 
	  * @param inputRegex regex to be searched
	  * @param idx index to be searched
	  * @return true if the input index is part of the character class, else returns false
	  * @throws SpcfPatternSyntaxException if brackets are unbalanced
	  */
	 public static boolean isWithinCharacterClass(String inputRegex, int idx)
	 {
		 if(inputRegex == null || idx < 0 || idx >= inputRegex.length())
		 {
			 return false;
		 }		 
		 //
		 boolean ret = false;
		 int closingClassBracketIdx = -1;
		 for(int i=idx; i<inputRegex.length(); i++)
		 {
			 char c = inputRegex.charAt(i);
			 if(c == ']' && !isEscaped(inputRegex, ']', i))
			 {
				 closingClassBracketIdx = i;
				 break;
			 }
		 }
		 //
		 if(closingClassBracketIdx != -1)
		 {
			 int openingClassBracketIdx = findOpeningBracketIdx(inputRegex, '[', closingClassBracketIdx);
			 if(openingClassBracketIdx >= 0 && idx > openingClassBracketIdx && idx < closingClassBracketIdx)
			 {
				 ret = true; 
			 }
		 }	
		 //
		 return ret;
	 }
}
