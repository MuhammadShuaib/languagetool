/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.de;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionString;
import org.languagetool.rules.Example;
import org.languagetool.tokenizers.WordTokenizer;

import java.util.ResourceBundle;

/**
 * @since 3.1
 */
public class GermanConfusionProbabilityRule extends ConfusionProbabilityRule {

  private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();
  
  public GermanConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }

  public GermanConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages, languageModel, language, grams);
    addExamplePair(Example.wrong("Während Sie das Ganze <marker>mir</marker> einem Holzlöffel rühren…"),
                   Example.fixed("Während Sie das Ganze <marker>mit</marker> einem Holzlöffel rühren…"));
  }

  @Override
  public String getDescription() {
    return "Mit Statistik mögliche Tippfehler erkennen";
  }
  
  @Override
  public String getMessage(ConfusionString textString, ConfusionString suggestion) {
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return "Ist möglicherweise '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") gemeint statt '"
              + textString.getString() + "' (" + textString.getDescription() + ")?";
    } else if (suggestion.getDescription() != null) {
      return "Ist möglicherweise '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") gemeint?";
    } else {
      return "Ist möglicherweise '" + suggestion.getString() + "' gemeint?";
    }
  }
  
  @Override
  protected WordTokenizer getTokenizer() {
    return WORD_TOKENIZER;
  }
}
