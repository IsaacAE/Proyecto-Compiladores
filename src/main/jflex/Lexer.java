/* The following code was generated by JFlex 1.7.0 */


package main.jflex;

import main.java.ClaseLexica;
import main.java.Token;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>src/main/jflex/Lexer.flex</tt>
 */
public class Lexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\10\0\1\63\1\65\1\70\1\72\1\71\1\71\22\0\1\1\1\40"+
    "\1\66\2\63\1\35\1\36\1\62\1\47\1\50\1\33\1\31\1\43"+
    "\1\32\1\46\1\34\12\56\1\45\1\44\1\41\1\30\1\42\2\63"+
    "\3\64\1\61\1\57\1\60\24\64\1\53\1\67\1\54\1\0\1\55"+
    "\1\0\1\6\1\24\1\14\1\23\1\16\1\11\1\5\1\22\1\15"+
    "\1\64\1\25\1\17\1\7\1\13\1\4\1\2\1\64\1\3\1\20"+
    "\1\10\1\12\1\27\1\21\1\26\2\64\1\51\1\37\1\52\7\0"+
    "\1\72\73\0\1\64\7\0\1\64\3\0\1\64\3\0\1\64\1\0"+
    "\1\64\2\0\1\64\3\0\1\64\1\0\1\64\4\0\1\64\7\0"+
    "\1\64\3\0\1\64\3\0\1\64\1\0\1\64\2\0\1\64\3\0"+
    "\1\64\1\0\1\64\u1f2b\0\1\72\1\72\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\udfe6\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\15\2\1\3\1\4\1\5\1\6\1\7"+
    "\1\10\2\0\1\11\1\12\1\13\1\14\1\15\1\16"+
    "\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26"+
    "\2\0\12\2\1\27\6\2\1\30\3\2\1\31\1\0"+
    "\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\0"+
    "\1\41\2\0\1\42\1\0\2\2\1\43\10\2\1\44"+
    "\11\2\1\0\1\45\1\0\2\40\1\46\3\2\1\47"+
    "\1\2\1\50\1\2\1\51\2\2\1\52\1\53\2\2"+
    "\1\54\5\2\1\55\1\0\1\56\1\40\1\0\1\41"+
    "\1\0\1\40\1\2\1\57\1\60\1\2\1\61\1\62"+
    "\4\2\1\63\2\2\1\64\1\0\1\40\1\0\1\40"+
    "\1\2\1\65\1\2\1\66\1\67\1\70\1\71\1\2"+
    "\1\0\1\40\1\2\1\72\1\73\1\0\1\40\1\74"+
    "\1\0\1\75\2\0\1\40\3\0\1\40\1\0\1\41"+
    "\2\0\1\41\2\0\2\41\1\0\1\41\1\0\1\41"+
    "\2\0\1\41\3\0\1\41\1\0\1\41\1\0\1\41"+
    "\4\0";

  private static int [] zzUnpackAction() {
    int [] result = new int[201];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\73\0\166\0\261\0\354\0\u0127\0\u0162\0\u019d"+
    "\0\u01d8\0\u0213\0\u024e\0\u0289\0\u02c4\0\u02ff\0\u033a\0\u0375"+
    "\0\u03b0\0\u03b0\0\u03b0\0\u03eb\0\u03b0\0\u0426\0\u0461\0\u049c"+
    "\0\u04d7\0\u0512\0\u03b0\0\u03b0\0\u03b0\0\u03b0\0\u03b0\0\u03b0"+
    "\0\u03b0\0\u03b0\0\u03b0\0\u03b0\0\u054d\0\u0588\0\u05c3\0\u05fe"+
    "\0\u0639\0\u0674\0\u06af\0\u06ea\0\u0725\0\u0760\0\u079b\0\u07d6"+
    "\0\u0811\0\354\0\u084c\0\u0887\0\u08c2\0\u08fd\0\u0938\0\u0973"+
    "\0\u09ae\0\u09e9\0\u0a24\0\u0a5f\0\u03b0\0\u0a9a\0\u0ad5\0\u03b0"+
    "\0\u03b0\0\u03b0\0\u03b0\0\u03b0\0\u03b0\0\u0b10\0\u03b0\0\u0b4b"+
    "\0\u0b86\0\u03b0\0\u0bc1\0\u0bfc\0\u0c37\0\354\0\u0c72\0\u0cad"+
    "\0\u0ce8\0\u0d23\0\u0d5e\0\u0d99\0\u0dd4\0\u0e0f\0\354\0\u0e4a"+
    "\0\u0e85\0\u0ec0\0\u0efb\0\u0f36\0\u0f71\0\u0fac\0\u0fe7\0\u1022"+
    "\0\u105d\0\u1098\0\u10d3\0\u110e\0\u1149\0\u03b0\0\u1184\0\u11bf"+
    "\0\u11fa\0\354\0\u1235\0\354\0\u1270\0\354\0\u12ab\0\u12e6"+
    "\0\354\0\354\0\u1321\0\u135c\0\354\0\u1397\0\u13d2\0\u140d"+
    "\0\u1448\0\u1483\0\354\0\u14be\0\u03b0\0\u14f9\0\u1534\0\u14f9"+
    "\0\u156f\0\u15aa\0\u15e5\0\354\0\354\0\u1620\0\354\0\354"+
    "\0\u165b\0\u1696\0\u16d1\0\u170c\0\354\0\u1747\0\u1782\0\354"+
    "\0\u17bd\0\u17f8\0\u1833\0\u186e\0\u18a9\0\354\0\u18e4\0\354"+
    "\0\354\0\354\0\354\0\u191f\0\u195a\0\u1995\0\u19d0\0\354"+
    "\0\354\0\u1a0b\0\u1a46\0\354\0\u1a81\0\u03b0\0\u1abc\0\u1af7"+
    "\0\u1b32\0\u1b6d\0\u1ba8\0\u1be3\0\u1c1e\0\u1c59\0\u1c94\0\u1ccf"+
    "\0\u1d0a\0\u1d45\0\u1d80\0\u1dbb\0\u1df6\0\u1e31\0\u1e6c\0\u1ea7"+
    "\0\u1ee2\0\u1f1d\0\u1f58\0\u1f93\0\u1fce\0\u2009\0\u2044\0\u207f"+
    "\0\u20ba\0\u20f5\0\u2130\0\u216b\0\u21a6\0\u21e1\0\u221c\0\u2257"+
    "\0\u2292";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[201];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\0\1\2\1\3\1\4\4\5\1\6\1\7\2\5"+
    "\1\10\1\11\1\12\1\5\1\13\1\14\1\5\1\15"+
    "\1\16\2\5\1\17\1\20\1\21\1\22\1\23\1\24"+
    "\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34"+
    "\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44"+
    "\1\5\1\45\3\5\1\46\1\0\1\5\1\2\1\47"+
    "\1\0\2\2\2\0\1\2\63\0\1\2\2\0\2\2"+
    "\3\0\1\5\1\50\4\5\1\51\17\5\25\0\5\5"+
    "\2\0\1\5\10\0\10\5\1\52\3\5\1\53\11\5"+
    "\25\0\5\5\2\0\1\5\10\0\26\5\25\0\5\5"+
    "\2\0\1\5\10\0\1\5\1\54\24\5\25\0\5\5"+
    "\2\0\1\5\10\0\4\5\1\55\3\5\1\56\4\5"+
    "\1\57\10\5\25\0\5\5\2\0\1\5\10\0\2\5"+
    "\1\60\1\5\1\61\21\5\25\0\5\5\2\0\1\5"+
    "\10\0\7\5\1\62\1\5\1\63\14\5\25\0\5\5"+
    "\2\0\1\5\10\0\15\5\1\64\10\5\25\0\5\5"+
    "\2\0\1\5\10\0\6\5\1\65\3\5\1\66\4\5"+
    "\1\67\6\5\25\0\5\5\2\0\1\5\10\0\20\5"+
    "\1\70\5\5\25\0\5\5\2\0\1\5\10\0\2\5"+
    "\1\71\11\5\1\72\11\5\25\0\5\5\2\0\1\5"+
    "\10\0\1\5\1\73\24\5\25\0\5\5\2\0\1\5"+
    "\10\0\2\5\1\74\23\5\25\0\5\5\2\0\1\5"+
    "\36\0\1\75\170\0\1\76\1\77\74\0\1\100\73\0"+
    "\1\101\63\0\1\102\72\0\1\103\72\0\1\104\53\0"+
    "\1\105\4\0\1\106\4\0\1\107\22\0\1\110\7\0"+
    "\1\45\1\106\1\105\1\107\13\0\26\111\5\0\4\111"+
    "\16\0\3\111\1\0\3\111\1\0\1\111\3\0\66\47"+
    "\1\112\1\113\3\47\2\0\2\5\1\114\10\5\1\115"+
    "\12\5\25\0\5\5\2\0\1\5\10\0\1\5\1\116"+
    "\24\5\25\0\5\5\2\0\1\5\10\0\11\5\1\117"+
    "\14\5\25\0\5\5\2\0\1\5\10\0\6\5\1\120"+
    "\17\5\25\0\5\5\2\0\1\5\10\0\10\5\1\121"+
    "\15\5\25\0\5\5\2\0\1\5\10\0\15\5\1\122"+
    "\10\5\25\0\5\5\2\0\1\5\10\0\11\5\1\123"+
    "\14\5\25\0\5\5\2\0\1\5\10\0\2\5\1\124"+
    "\23\5\25\0\5\5\2\0\1\5\10\0\5\5\1\125"+
    "\20\5\25\0\5\5\2\0\1\5\10\0\16\5\1\126"+
    "\7\5\25\0\5\5\2\0\1\5\10\0\6\5\1\127"+
    "\17\5\25\0\5\5\2\0\1\5\10\0\16\5\1\130"+
    "\7\5\25\0\5\5\2\0\1\5\10\0\1\5\1\131"+
    "\24\5\25\0\5\5\2\0\1\5\10\0\4\5\1\132"+
    "\21\5\25\0\5\5\2\0\1\5\10\0\13\5\1\133"+
    "\12\5\25\0\5\5\2\0\1\5\10\0\13\5\1\134"+
    "\12\5\25\0\5\5\2\0\1\5\10\0\10\5\1\135"+
    "\15\5\25\0\5\5\2\0\1\5\10\0\7\5\1\136"+
    "\16\5\25\0\5\5\2\0\1\5\10\0\14\5\1\137"+
    "\11\5\25\0\5\5\2\0\1\5\10\0\13\5\1\140"+
    "\12\5\25\0\5\5\2\0\1\5\6\0\33\76\1\141"+
    "\37\76\34\0\1\142\67\0\2\143\23\0\1\144\72\0"+
    "\1\145\76\0\1\146\10\0\70\47\5\0\3\5\1\147"+
    "\2\5\1\150\17\5\25\0\5\5\2\0\1\5\10\0"+
    "\11\5\1\151\14\5\25\0\5\5\2\0\1\5\10\0"+
    "\14\5\1\152\11\5\25\0\5\5\2\0\1\5\10\0"+
    "\10\5\1\153\15\5\25\0\5\5\2\0\1\5\10\0"+
    "\14\5\1\154\11\5\25\0\5\5\2\0\1\5\10\0"+
    "\16\5\1\155\7\5\25\0\5\5\2\0\1\5\10\0"+
    "\12\5\1\156\13\5\25\0\5\5\2\0\1\5\10\0"+
    "\4\5\1\157\21\5\25\0\5\5\2\0\1\5\10\0"+
    "\1\160\25\5\25\0\5\5\2\0\1\5\10\0\14\5"+
    "\1\161\11\5\25\0\5\5\2\0\1\5\10\0\14\5"+
    "\1\162\11\5\25\0\5\5\2\0\1\5\10\0\10\5"+
    "\1\163\2\5\1\164\12\5\25\0\5\5\2\0\1\5"+
    "\10\0\11\5\1\165\14\5\25\0\5\5\2\0\1\5"+
    "\10\0\6\5\1\166\17\5\25\0\5\5\2\0\1\5"+
    "\10\0\15\5\1\167\10\5\25\0\5\5\2\0\1\5"+
    "\10\0\22\5\1\170\3\5\25\0\5\5\2\0\1\5"+
    "\10\0\4\5\1\171\21\5\25\0\5\5\2\0\1\5"+
    "\10\0\4\5\1\172\21\5\25\0\5\5\2\0\1\5"+
    "\10\0\21\5\1\173\4\5\25\0\5\5\2\0\1\5"+
    "\6\0\33\174\1\141\1\175\36\174\70\142\1\0\2\142"+
    "\56\0\1\144\25\0\1\105\11\0\1\107\32\0\1\144"+
    "\1\0\1\105\1\107\22\0\1\176\4\0\1\177\4\0"+
    "\1\200\5\0\2\201\23\0\1\202\1\177\1\176\1\200"+
    "\13\0\1\5\1\203\24\5\25\0\5\5\2\0\1\5"+
    "\10\0\2\5\1\204\23\5\25\0\5\5\2\0\1\5"+
    "\10\0\6\5\1\205\17\5\25\0\5\5\2\0\1\5"+
    "\10\0\1\5\1\206\24\5\25\0\5\5\2\0\1\5"+
    "\10\0\14\5\1\207\11\5\25\0\5\5\2\0\1\5"+
    "\10\0\6\5\1\210\17\5\25\0\5\5\2\0\1\5"+
    "\10\0\15\5\1\211\10\5\25\0\5\5\2\0\1\5"+
    "\10\0\12\5\1\212\13\5\25\0\5\5\2\0\1\5"+
    "\10\0\11\5\1\213\14\5\25\0\5\5\2\0\1\5"+
    "\10\0\12\5\1\214\13\5\25\0\5\5\2\0\1\5"+
    "\10\0\14\5\1\215\11\5\25\0\5\5\2\0\1\5"+
    "\10\0\15\5\1\216\10\5\25\0\5\5\2\0\1\5"+
    "\10\0\10\5\1\217\15\5\25\0\5\5\2\0\1\5"+
    "\10\0\23\5\1\220\2\5\25\0\5\5\2\0\1\5"+
    "\6\0\33\174\1\141\1\0\36\174\31\0\2\201\71\0"+
    "\2\221\23\0\1\222\72\0\1\223\25\0\1\176\4\0"+
    "\1\177\4\0\1\200\5\0\2\201\23\0\1\224\1\177"+
    "\1\176\1\200\13\0\4\5\1\225\21\5\25\0\5\5"+
    "\2\0\1\5\10\0\11\5\1\226\14\5\25\0\5\5"+
    "\2\0\1\5\10\0\14\5\1\227\11\5\25\0\5\5"+
    "\2\0\1\5\10\0\6\5\1\230\17\5\25\0\5\5"+
    "\2\0\1\5\10\0\3\5\1\231\22\5\25\0\5\5"+
    "\2\0\1\5\10\0\20\5\1\232\5\5\25\0\5\5"+
    "\2\0\1\5\10\0\14\5\1\233\11\5\25\0\5\5"+
    "\2\0\1\5\10\0\15\5\1\234\10\5\25\0\5\5"+
    "\2\0\1\5\64\0\1\222\25\0\1\176\11\0\1\200"+
    "\5\0\2\201\23\0\1\222\1\0\1\176\1\200\57\0"+
    "\1\235\7\0\1\223\25\0\1\176\4\0\1\177\4\0"+
    "\1\200\5\0\2\201\23\0\1\236\1\177\1\176\1\200"+
    "\13\0\5\5\1\237\20\5\25\0\5\5\2\0\1\5"+
    "\10\0\24\5\1\240\1\5\25\0\5\5\2\0\1\5"+
    "\10\0\6\5\1\241\17\5\25\0\5\5\2\0\1\5"+
    "\64\0\1\242\25\0\1\176\4\0\1\177\4\0\1\200"+
    "\5\0\2\201\23\0\1\243\1\177\1\176\1\200\13\0"+
    "\4\5\1\244\21\5\25\0\5\5\2\0\1\5\17\0"+
    "\1\245\3\0\1\246\1\247\4\0\1\245\32\0\1\250"+
    "\1\247\2\245\22\0\1\176\4\0\1\177\4\0\1\200"+
    "\5\0\2\201\23\0\1\251\1\177\1\176\1\200\26\0"+
    "\1\246\106\0\2\252\23\0\1\253\25\0\1\245\3\0"+
    "\1\246\1\247\4\0\1\245\32\0\1\254\1\247\2\245"+
    "\22\0\1\176\4\0\1\177\4\0\1\200\5\0\2\201"+
    "\23\0\1\255\1\177\1\176\1\200\67\0\1\253\25\0"+
    "\1\245\3\0\1\246\5\0\1\245\32\0\1\253\1\0"+
    "\2\245\22\0\1\245\3\0\1\246\1\247\4\0\1\245"+
    "\32\0\1\256\1\247\2\245\22\0\1\176\4\0\1\177"+
    "\4\0\1\200\5\0\2\201\23\0\1\257\1\177\1\176"+
    "\1\200\22\0\1\245\3\0\1\246\1\247\4\0\1\245"+
    "\32\0\1\260\1\247\2\245\27\0\1\261\4\0\1\200"+
    "\5\0\2\201\23\0\1\262\1\261\1\0\1\200\22\0"+
    "\1\245\3\0\1\246\1\247\4\0\1\245\32\0\1\263"+
    "\1\247\2\245\42\0\2\264\23\0\1\265\32\0\1\261"+
    "\4\0\1\200\5\0\2\201\23\0\1\266\1\261\1\0"+
    "\1\200\22\0\1\245\3\0\1\246\1\247\4\0\1\245"+
    "\32\0\1\267\1\247\2\245\67\0\1\265\37\0\1\200"+
    "\5\0\2\201\23\0\1\265\2\0\1\200\27\0\1\261"+
    "\4\0\1\200\5\0\2\201\23\0\1\270\1\261\1\0"+
    "\1\200\22\0\1\245\3\0\1\246\1\247\4\0\1\245"+
    "\32\0\1\271\1\247\2\245\27\0\1\261\4\0\1\200"+
    "\5\0\2\201\23\0\1\272\1\261\1\0\1\200\26\0"+
    "\1\246\1\273\4\0\1\245\32\0\1\274\1\273\1\0"+
    "\1\245\27\0\1\261\4\0\1\200\5\0\2\201\23\0"+
    "\1\275\1\261\1\0\1\200\42\0\2\276\23\0\1\277"+
    "\31\0\1\246\1\273\4\0\1\245\32\0\1\300\1\273"+
    "\1\0\1\245\27\0\1\261\4\0\1\200\5\0\2\201"+
    "\23\0\1\301\1\261\1\0\1\200\67\0\1\277\31\0"+
    "\1\246\5\0\1\245\32\0\1\277\2\0\1\245\26\0"+
    "\1\246\1\273\4\0\1\245\32\0\1\302\1\273\1\0"+
    "\1\245\27\0\1\261\4\0\1\200\5\0\2\201\23\0"+
    "\1\303\1\261\1\0\1\200\26\0\1\246\1\273\4\0"+
    "\1\245\32\0\1\304\1\273\1\0\1\245\27\0\1\261"+
    "\4\0\1\200\5\0\2\201\23\0\1\305\1\261\1\0"+
    "\1\200\26\0\1\246\1\273\4\0\1\245\32\0\1\306"+
    "\1\273\1\0\1\245\27\0\1\261\4\0\1\200\5\0"+
    "\2\201\24\0\1\261\1\0\1\200\26\0\1\246\1\273"+
    "\4\0\1\245\32\0\1\307\1\273\1\0\1\245\26\0"+
    "\1\246\1\273\4\0\1\245\32\0\1\310\1\273\1\0"+
    "\1\245\26\0\1\246\1\273\4\0\1\245\32\0\1\311"+
    "\1\273\1\0\1\245\26\0\1\246\1\273\4\0\1\245"+
    "\33\0\1\273\1\0\1\245\11\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[8909];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\17\1\3\11\1\1\1\11\2\0\3\1\12\11"+
    "\1\1\2\0\25\1\1\11\1\0\1\1\6\11\1\0"+
    "\1\11\2\0\1\11\1\0\25\1\1\0\1\1\1\0"+
    "\2\1\1\11\25\1\1\0\1\11\1\1\1\0\1\1"+
    "\1\0\17\1\1\0\1\1\1\0\11\1\1\0\4\1"+
    "\1\0\2\1\1\0\1\11\2\0\1\1\3\0\1\1"+
    "\1\0\1\1\2\0\1\1\2\0\2\1\1\0\1\1"+
    "\1\0\1\1\2\0\1\1\3\0\1\1\1\0\1\1"+
    "\1\0\1\1\4\0";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[201];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true iff the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true iff the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;
  
  /** 
   * The number of occupied positions in zzBuffer beyond zzEndRead.
   * When a lead/high surrogate has been read from the input stream
   * into the final zzBuffer position, this will have a value of 1;
   * otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /* user code: */

public Token actual;



  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public Lexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x110000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 246) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException("Reader returned 0 characters. See JFlex examples for workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      /* If numRead == requested, we might have requested to few chars to
         encode a full Unicode character. We assume that a Reader would
         otherwise never return half characters. */
      if (numRead == requested) {
        if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    zzFinalHighSurrogate = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE)
      zzBuffer = new char[ZZ_BUFFERSIZE];
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
              {
                return new Token(ClaseLexica.EOF, yytext());
              }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { /* ignorar */
            } 
            // fall through
          case 62: break;
          case 2: 
            { System.out.println("Token encontrado: ID (" + yytext() + ")");
    return new Token(ClaseLexica.ID, yytext());
            } 
            // fall through
          case 63: break;
          case 3: 
            { System.out.println("Token encontrado: ASIGNACION (" + yytext() + ")"); return new Token(ClaseLexica.ASIGNACION, yytext());
            } 
            // fall through
          case 64: break;
          case 4: 
            { System.out.println("Token encontrado: MAS (" + yytext() + ")"); return new Token(ClaseLexica.MAS, yytext());
            } 
            // fall through
          case 65: break;
          case 5: 
            { System.out.println("Token encontrado: MENOS (" + yytext() + ")"); return new Token(ClaseLexica.MENOS, yytext());
            } 
            // fall through
          case 66: break;
          case 6: 
            { System.out.println("Token encontrado: MULTIPLICACION (" + yytext() + ")"); return new Token(ClaseLexica.MULTIPLICACION, yytext());
            } 
            // fall through
          case 67: break;
          case 7: 
            { System.out.println("Token encontrado: DIVISION (" + yytext() + ")"); return new Token(ClaseLexica.DIVISION, yytext());
            } 
            // fall through
          case 68: break;
          case 8: 
            { System.out.println("Token encontrado: MODULO (" + yytext() + ")"); return new Token(ClaseLexica.MODULO, yytext());
            } 
            // fall through
          case 69: break;
          case 9: 
            { System.out.println("Token encontrado: NOT (" + yytext() + ")"); return new Token(ClaseLexica.NOT, yytext());
            } 
            // fall through
          case 70: break;
          case 10: 
            { System.out.println("Token encontrado: MENOR (" + yytext() + ")"); return new Token(ClaseLexica.MENOR, yytext());
            } 
            // fall through
          case 71: break;
          case 11: 
            { System.out.println("Token encontrado: MAYOR (" + yytext() + ")"); return new Token(ClaseLexica.MAYOR, yytext());
            } 
            // fall through
          case 72: break;
          case 12: 
            { System.out.println("Token encontrado: COMA (" + yytext() + ")"); return new Token(ClaseLexica.COMA, yytext());
            } 
            // fall through
          case 73: break;
          case 13: 
            { System.out.println("Token encontrado: PUNTO_Y_COMA (" + yytext() + ")"); return new Token(ClaseLexica.PUNTO_Y_COMA, yytext());
            } 
            // fall through
          case 74: break;
          case 14: 
            { System.out.println("Token encontrado: DOS_PUNTOS (" + yytext() + ")"); return new Token(ClaseLexica.DOS_PUNTOS, yytext());
            } 
            // fall through
          case 75: break;
          case 15: 
            { System.out.println("Token encontrado: PUNTO (" + yytext() + ")"); return new Token(ClaseLexica.PUNTO, yytext());
            } 
            // fall through
          case 76: break;
          case 16: 
            { System.out.println("Token encontrado: PARENTESIS_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.PARENTESIS_ABRE, yytext());
            } 
            // fall through
          case 77: break;
          case 17: 
            { System.out.println("Token encontrado: PARENTESIS_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.PARENTESIS_CIERRA, yytext());
            } 
            // fall through
          case 78: break;
          case 18: 
            { System.out.println("Token encontrado: LLAVE_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.LLAVE_ABRE, yytext());
            } 
            // fall through
          case 79: break;
          case 19: 
            { System.out.println("Token encontrado: LLAVE_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.LLAVE_CIERRA, yytext());
            } 
            // fall through
          case 80: break;
          case 20: 
            { System.out.println("Token encontrado: CORCHETE_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.CORCHETE_ABRE, yytext());
            } 
            // fall through
          case 81: break;
          case 21: 
            { System.out.println("Token encontrado: CORCHETE_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.CORCHETE_CIERRA, yytext());
            } 
            // fall through
          case 82: break;
          case 22: 
            { System.out.println("Token encontrado: LITERAL_ENTERA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_ENTERA, yytext());
            } 
            // fall through
          case 83: break;
          case 23: 
            { System.out.println("Token encontrado: IF (" + yytext() + ")"); return new Token(ClaseLexica.IF, yytext());
            } 
            // fall through
          case 84: break;
          case 24: 
            { System.out.println("Token encontrado: DO (" + yytext() + ")"); return new Token(ClaseLexica.DO, yytext());
            } 
            // fall through
          case 85: break;
          case 25: 
            { System.out.println("Token encontrado: IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.IGUAL, yytext());
            } 
            // fall through
          case 86: break;
          case 26: 
            { System.out.println("Token encontrado: DIVISION_ENTERA (" + yytext() + ")"); return new Token(ClaseLexica.DIVISION_ENTERA, yytext());
            } 
            // fall through
          case 87: break;
          case 27: 
            { System.out.println("Token encontrado: AND (" + yytext() + ")"); return new Token(ClaseLexica.AND, yytext());
            } 
            // fall through
          case 88: break;
          case 28: 
            { System.out.println("Token encontrado: OR (" + yytext() + ")"); return new Token(ClaseLexica.OR, yytext());
            } 
            // fall through
          case 89: break;
          case 29: 
            { System.out.println("Token encontrado: DIFERENTE (" + yytext() + ")"); return new Token(ClaseLexica.DIFERENTE, yytext());
            } 
            // fall through
          case 90: break;
          case 30: 
            { System.out.println("Token encontrado: MENOR_IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.MENOR_IGUAL, yytext());
            } 
            // fall through
          case 91: break;
          case 31: 
            { System.out.println("Token encontrado: MAYOR_IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.MAYOR_IGUAL, yytext());
            } 
            // fall through
          case 92: break;
          case 32: 
            { System.out.println("Token encontrado: LITERAL_FLOTANTE (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_FLOTANTE, yytext());
            } 
            // fall through
          case 93: break;
          case 33: 
            { System.out.println("Token encontrado: LITERAL_DOUBLE (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_DOUBLE, yytext());
            } 
            // fall through
          case 94: break;
          case 34: 
            { System.out.println("Token encontrado: LITERAL_CADENA (" + yytext() + ")"); return new Token(ClaseLexica.LITERAL_CADENA, yytext());
            } 
            // fall through
          case 95: break;
          case 35: 
            { System.out.println("Token encontrado: PTR (" + yytext() + ")"); return new Token(ClaseLexica.PTR, yytext());
            } 
            // fall through
          case 96: break;
          case 36: 
            { System.out.println("Token encontrado: INT (" + yytext() + ")"); return new Token(ClaseLexica.INT, yytext());
            } 
            // fall through
          case 97: break;
          case 37: 
            { /* ignorar comentarios de una línea */
            } 
            // fall through
          case 98: break;
          case 38: 
            { System.out.println("Token encontrado: LITERAL_RUNA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_RUNA, yytext());
            } 
            // fall through
          case 99: break;
          case 39: 
            { System.out.println("Token encontrado: RUNE (" + yytext() + ")"); return new Token(ClaseLexica.RUNE, yytext());
            } 
            // fall through
          case 100: break;
          case 40: 
            { System.out.println("Token encontrado: TRUE (" + yytext() + ")"); return new Token(ClaseLexica.TRUE, yytext());
            } 
            // fall through
          case 101: break;
          case 41: 
            { System.out.println("Token encontrado: FUNC (" + yytext() + ")"); return new Token(ClaseLexica.FUNC, yytext());
            } 
            // fall through
          case 102: break;
          case 42: 
            { System.out.println("Token encontrado: CASE (" + yytext() + ")"); return new Token(ClaseLexica.CASE, yytext());
            } 
            // fall through
          case 103: break;
          case 43: 
            { System.out.println("Token encontrado: ELSE (" + yytext() + ")"); return new Token(ClaseLexica.ELSE, yytext());
            } 
            // fall through
          case 104: break;
          case 44: 
            { System.out.println("Token encontrado: SCAN (" + yytext() + ")"); return new Token(ClaseLexica.SCAN, yytext());
            } 
            // fall through
          case 105: break;
          case 45: 
            { System.out.println("Token encontrado: VOID (" + yytext() + ")"); return new Token(ClaseLexica.VOID, yytext());
            } 
            // fall through
          case 106: break;
          case 46: 
            { /* ignorar comentarios de varias líneas */
            } 
            // fall through
          case 107: break;
          case 47: 
            { System.out.println("Token encontrado: PROTO (" + yytext() + ")"); return new Token(ClaseLexica.PROTO, yytext());
            } 
            // fall through
          case 108: break;
          case 48: 
            { System.out.println("Token encontrado: PRINT (" + yytext() + ")"); return new Token(ClaseLexica.PRINT, yytext());
            } 
            // fall through
          case 109: break;
          case 49: 
            { System.out.println("Token encontrado: FALSE (" + yytext() + ")"); return new Token(ClaseLexica.FALSE, yytext());
            } 
            // fall through
          case 110: break;
          case 50: 
            { System.out.println("Token encontrado: FLOAT (" + yytext() + ")"); return new Token(ClaseLexica.FLOAT, yytext());
            } 
            // fall through
          case 111: break;
          case 51: 
            { System.out.println("Token encontrado: WHILE (" + yytext() + ")"); return new Token(ClaseLexica.WHILE, yytext());
            } 
            // fall through
          case 112: break;
          case 52: 
            { System.out.println("Token encontrado: BREAK (" + yytext() + ")"); return new Token(ClaseLexica.BREAK, yytext());
            } 
            // fall through
          case 113: break;
          case 53: 
            { System.out.println("Token encontrado: RETURN (" + yytext() + ")"); return new Token(ClaseLexica.RETURN, yytext());
            } 
            // fall through
          case 114: break;
          case 54: 
            { System.out.println("Token encontrado: STRUCT (" + yytext() + ")"); return new Token(ClaseLexica.STRUCT, yytext());
            } 
            // fall through
          case 115: break;
          case 55: 
            { System.out.println("Token encontrado: STRING (" + yytext() + ")"); return new Token(ClaseLexica.STRING, yytext());
            } 
            // fall through
          case 116: break;
          case 56: 
            { System.out.println("Token encontrado: SWITCH (" + yytext() + ")"); return new Token(ClaseLexica.SWITCH, yytext());
            } 
            // fall through
          case 117: break;
          case 57: 
            { System.out.println("Token encontrado: DOUBLE (" + yytext() + ")"); return new Token(ClaseLexica.DOUBLE, yytext());
            } 
            // fall through
          case 118: break;
          case 58: 
            { System.out.println("Token encontrado: COMPLEX (" + yytext() + ")"); return new Token(ClaseLexica.COMPLEX, yytext());
            } 
            // fall through
          case 119: break;
          case 59: 
            { System.out.println("Token encontrado: DEFAULT (" + yytext() + ")"); return new Token(ClaseLexica.DEFAULT, yytext());
            } 
            // fall through
          case 120: break;
          case 60: 
            { System.out.println("Token encontrado: PROGRAMA (" + yytext() + ")"); return new Token(ClaseLexica.PROGRAMA, yytext());
            } 
            // fall through
          case 121: break;
          case 61: 
            { System.out.println("Token encontrado: LITERAL_COMPLEJA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_COMPLEJA, yytext());
            } 
            // fall through
          case 122: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
