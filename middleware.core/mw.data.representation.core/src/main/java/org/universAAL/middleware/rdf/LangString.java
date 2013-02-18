/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.rdf;

/**
 * A String literal with optional language tag. Predefined language tags are
 * according to ISO 639-1.
 * 
 * @author Carsten Stockloew
 */
public class LangString {

    public static final String LANG_ABKHAZ = "ab";
    public static final String LANG_AFAR = "aa";
    public static final String LANG_AFRIKAANS = "af";
    public static final String LANG_AKAN = "ak";
    public static final String LANG_ALBANIAN = "sq";
    public static final String LANG_AMHARIC = "am";
    public static final String LANG_ARABIC = "ar";
    public static final String LANG_ARAGONESE = "an";
    public static final String LANG_ARMENIAN = "hy";
    public static final String LANG_ASSAMESE = "as";
    public static final String LANG_AVARIC = "av";
    public static final String LANG_AVESTAN = "ae";
    public static final String LANG_AYMARA = "ay";
    public static final String LANG_AZERBAIJANI = "az";
    public static final String LANG_BAMBARA = "bm";
    public static final String LANG_BASHKIR = "ba";
    public static final String LANG_BASQUE = "eu";
    public static final String LANG_BELARUSIAN = "be";
    public static final String LANG_BENGALI = "bn";
    public static final String LANG_BIHARI = "bh";
    public static final String LANG_BISLAMA = "bi";
    public static final String LANG_BOSNIAN = "bs";
    public static final String LANG_BRETON = "br";
    public static final String LANG_BULGARIAN = "bg";
    public static final String LANG_BURMESE = "my";
    public static final String LANG_CATALAN = "ca";
    public static final String LANG_CHAMORRO = "ch";
    public static final String LANG_CHECHEN = "ce";
    public static final String LANG_CHICHEWA = "ny";
    public static final String LANG_CHINESE = "zh";
    public static final String LANG_CHUVASH = "cv";
    public static final String LANG_CORNISH = "kw";
    public static final String LANG_CORSICAN = "co";
    public static final String LANG_CREE = "cr";
    public static final String LANG_CROATIAN = "hr";
    public static final String LANG_CZECH = "cs";
    public static final String LANG_DANISH = "da";
    public static final String LANG_DIVEHI = "dv";
    public static final String LANG_DUTCH = "nl";
    public static final String LANG_DZONGKHA = "dz";
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_ESPERANTO = "eo";
    public static final String LANG_ESTONIAN = "et";
    public static final String LANG_EWE = "ee";
    public static final String LANG_FAROESE = "fo";
    public static final String LANG_FIJIAN = "fj";
    public static final String LANG_FINNISH = "fi";
    public static final String LANG_FRENCH = "fr";
    public static final String LANG_FULA = "ff";
    public static final String LANG_GALICIAN = "gl";
    public static final String LANG_GANDA = "lg";
    public static final String LANG_GEORGIAN = "ka";
    public static final String LANG_GERMAN = "de";
    public static final String LANG_GREEK = "el";
    public static final String LANG_GUARANI = "gn";
    public static final String LANG_GUJARATI = "gu";
    public static final String LANG_HAITIAN = "ht";
    public static final String LANG_HAUSA = "ha";
    public static final String LANG_HEBREW = "he";
    public static final String LANG_HERERO = "hz";
    public static final String LANG_HINDI = "hi";
    public static final String LANG_HIRI_MOTU = "ho";
    public static final String LANG_HUNGARIAN = "hu";
    public static final String LANG_ICELANDIC = "is";
    public static final String LANG_IDO = "io";
    public static final String LANG_IGBO = "ig";
    public static final String LANG_INDONESIAN = "id";
    public static final String LANG_INTERLINGUA = "ia";
    public static final String LANG_INTERLINGUE = "ie";
    public static final String LANG_INUKTITUT = "iu";
    public static final String LANG_INUPIAQ = "ik";
    public static final String LANG_IRISH = "ga";
    public static final String LANG_ITALIAN = "it";
    public static final String LANG_JAPANESE = "ja";
    public static final String LANG_JAVANESE = "jv";
    public static final String LANG_KALAALLISUT = "kl";
    public static final String LANG_KANNADA = "kn";
    public static final String LANG_KANURI = "kr";
    public static final String LANG_KASHMIRI = "ks";
    public static final String LANG_KAZAKH = "kk";
    public static final String LANG_KHMER = "km";
    public static final String LANG_KIKUYU = "ki";
    public static final String LANG_KINYARWANDA = "rw";
    public static final String LANG_KIRUNDI = "rn";
    public static final String LANG_KOMI = "kv";
    public static final String LANG_KONGO = "kg";
    public static final String LANG_KOREAN = "ko";
    public static final String LANG_KURDISH = "ku";
    public static final String LANG_KWANYAMA = "kj";
    public static final String LANG_KYRGYZ = "ky";
    public static final String LANG_LAO = "lo";
    public static final String LANG_LATIN = "la";
    public static final String LANG_LATVIAN = "lv";
    public static final String LANG_LIMBURGISH = "li";
    public static final String LANG_LINGALA = "ln";
    public static final String LANG_LITHUANIAN = "lt";
    public static final String LANG_LUBA_KATANGA = "lu";
    public static final String LANG_LUXEMBOURGISH = "lb";
    public static final String LANG_MACEDONIAN = "mk";
    public static final String LANG_MALAGASY = "mg";
    public static final String LANG_MALAY = "ms";
    public static final String LANG_MALAYALAM = "ml";
    public static final String LANG_MALTESE = "mt";
    public static final String LANG_MANX = "gv";
    public static final String LANG_MAORI = "mi";
    public static final String LANG_MARATHI = "mr";
    public static final String LANG_MARSHALLESE = "mh";
    public static final String LANG_MONGOLIAN = "mn";
    public static final String LANG_NAURU = "na";
    public static final String LANG_NAVAJO = "nv";
    public static final String LANG_NDONGA = "ng";
    public static final String LANG_NEPALI = "ne";
    public static final String LANG_NORTH_NDEBELE = "nd";
    public static final String LANG_NORTHERN_SAMI = "se";
    public static final String LANG_NORWEGIAN = "no";
    public static final String LANG_NORWEGIAN_BOKMAL = "nb";
    public static final String LANG_NORWEGIAN_NYNORSK = "nn";
    public static final String LANG_NUOSU = "ii";
    public static final String LANG_OCCITAN = "oc";
    public static final String LANG_OJIBWE = "oj";
    public static final String LANG_OLD_CHURCH_SLAVONIC = "cu";
    public static final String LANG_ORIYA = "or";
    public static final String LANG_OROMO = "om";
    public static final String LANG_OSSETIAN = "os";
    public static final String LANG_PALI = "pi";
    public static final String LANG_PANJABI = "pa";
    public static final String LANG_PASHTO = "ps";
    public static final String LANG_PERSIAN = "fa";
    public static final String LANG_POLISH = "pl";
    public static final String LANG_PORTUGUESE = "pt";
    public static final String LANG_QUECHUA = "qu";
    public static final String LANG_ROMANIAN = "ro";
    public static final String LANG_ROMANSH = "rm";
    public static final String LANG_RUSSIAN = "ru";
    public static final String LANG_SAMOAN = "sm";
    public static final String LANG_SANGO = "sg";
    public static final String LANG_SANSKRIT = "sa";
    public static final String LANG_SARDINIAN = "sc";
    public static final String LANG_SCOTTISH_GAELIC = "gd";
    public static final String LANG_SERBIAN = "sr";
    public static final String LANG_SHONA = "sn";
    public static final String LANG_SINDHI = "sd";
    public static final String LANG_SINHALA = "si";
    public static final String LANG_SLOVAK = "sk";
    public static final String LANG_SLOVENE = "sl";
    public static final String LANG_SOMALI = "so";
    public static final String LANG_SOUTH_NDEBELE = "nr";
    public static final String LANG_SOUTHERN_SOTHO = "st";
    public static final String LANG_SPANISH = "es";
    public static final String LANG_SUNDANESE = "su";
    public static final String LANG_SWAHILI = "sw";
    public static final String LANG_SWATI = "ss";
    public static final String LANG_SWEDISH = "sv";
    public static final String LANG_TAGALOG = "tl";
    public static final String LANG_TAHITIAN = "ty";
    public static final String LANG_TAJIK = "tg";
    public static final String LANG_TAMIL = "ta";
    public static final String LANG_TATAR = "tt";
    public static final String LANG_TELUGU = "te";
    public static final String LANG_THAI = "th";
    public static final String LANG_TIBETAN_STANDARD = "bo";
    public static final String LANG_TIGRINYA = "ti";
    public static final String LANG_TONGA = "to";
    public static final String LANG_TSONGA = "ts";
    public static final String LANG_TSWANA = "tn";
    public static final String LANG_TURKISH = "tr";
    public static final String LANG_TURKMEN = "tk";
    public static final String LANG_TWI = "tw";
    public static final String LANG_UIGHUR = "ug";
    public static final String LANG_UKRAINIAN = "uk";
    public static final String LANG_URDU = "ur";
    public static final String LANG_UZBEK = "uz";
    public static final String LANG_VENDA = "ve";
    public static final String LANG_VIETNAMESE = "vi";
    public static final String LANG_VOLAPUK = "vo";
    public static final String LANG_WALLOON = "wa";
    public static final String LANG_WELSH = "cy";
    public static final String LANG_WESTERN_FRISIAN = "fy";
    public static final String LANG_WOLOF = "wo";
    public static final String LANG_XHOSA = "xh";
    public static final String LANG_YIDDISH = "yi";
    public static final String LANG_YORUBA = "yo";
    public static final String LANG_ZHUANG = "za";
    public static final String LANG_ZULU = "zu";

    private String str = "";
    private String lang = "";

    public LangString(String str, String lang) {
	if (str == null)
	    this.str = "";
	else
	    this.str = str;

	if (lang == null)
	    this.lang = "";
	else
	    this.lang = lang;
    }

    public String getString() {
	return str;
    }

    public String getLang() {
	return lang;
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof LangString)) {
	    if (obj instanceof String && lang.isEmpty())
		return true;
	    return false;
	}
	LangString l = (LangString) obj;
	if (str.equals(l.str) && lang.equals(l.lang))
	    return true;
	return false;
    }

    public String toString() {
	return super.toString();
    }
}
