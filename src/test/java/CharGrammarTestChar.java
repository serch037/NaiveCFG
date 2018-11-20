import com.itesm.CharGrammar;
import org.junit.Test;
import static org.junit.Assert.*;

public class CharGrammarTestChar {
    CharGrammar charGrammar;


    @Test
    public void testisPartialMatch(){
        createTest1_1();
        assertFalse(charGrammar.isPartialMatchFromLeft("11", "110A"));
        createTest1();
        assertTrue(charGrammar.isPartialMatchFromLeft("0011", "00BB"));
        assertTrue(charGrammar.isPartialMatchFromLeft("0011", "001B"));
        assertTrue(charGrammar.isPartialMatchFromLeft("0011", "0011"));
        assertFalse(charGrammar.isPartialMatchFromLeft("0011", "0010"));
        assertTrue(charGrammar.isPartialMatchFromLeft("0011", "00BB"));
    }

    @Test
    public void acceptsStringTest1(){
        createTest1_1();
       assertTrue(charGrammar.naiveBelongs("1"));
        assertTrue(charGrammar.naiveBelongs("11"));
        assertTrue(charGrammar.naiveBelongs("111"));
        assertTrue(charGrammar.naiveBelongs("00110101"));
    }

    @Test
    public void acceptsStringTest2(){
        createTest2();
        assertFalse(charGrammar.naiveBelongs("aabbb"));
    }

     @Test
    public void testParseWorksMultiple(){
        ////createTest1();
        createTest1_1();
    }

    private void createTest1_1() {
        String f1 = "S -> 0B|1A";
        String f2 = "A -> 0|0S|1AA|$";
        String f3 = "B -> 1|1S|0BB";
        String[] tmp = new String[]{f1, f2, f3};
        charGrammar = new CharGrammar(tmp);
    }

     private void createTest2() {
        String f1 = "S -> aAb";
        String f2 = "A -> aAb|$";
        String[] tmp = new String[]{f1, f2};
        charGrammar = new CharGrammar(tmp);
    }

    public void createTest1() {
        String f1 = "S -> 0B";
        String f2 = "S -> 1A";

        String f3 = "A -> 0";
        String f4 = "A -> 0S";
        String f5 = "A -> 1AA";
        String f6 = "A -> $";


        String f7 = "B -> 1";
        String f8 = "B -> 1S";
        String f9 = "B -> 0BB";
        String[] tmp = new String[]{f1, f2, f3, f4, f5, f6, f7, f8};
        charGrammar = new CharGrammar(tmp);
    }
}
