/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tokenizers.ja;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Japanese;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class JapaneseSRXSentenceTokenizerTest extends TestCase {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Japanese());


  public void testTokenize() {
    testSplit("これはテスト用の文です。");
    testSplit("これはテスト用の文です。", "追加のテスト用の文です。");
    testSplit("これは、テスト用の文です。");
    testSplit("テスト用の文です！", "追加のテスト用の文です。");
    testSplit("テスト用の文です... ", "追加のテスト用の文です。");
    testSplit("アドレスはhttp://www.test.deです。");
    
    testSplit("これは(!)の文です。");
    testSplit("これは(!!)の文です。");
    testSplit("これは(?)の文です。");
    testSplit("これは(??)の文です。");

  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
