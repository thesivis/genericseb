/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericlexerseb;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raphael
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        String eq = "z0m=exp(-5.809+5.62*SAVI)";

        eq = "4*x+5.2024-(log(x,y)^-z)-300.12";
        List<String> ret = tokenize(eq);
        for (String string : ret) {
            System.out.println(string);
        }
        
        ExpressionParser ex = new ExpressionParser();
        System.out.println(ret.get(0).getClass());
        Object[] t = (Object[])ret.toArray();
        String[] a = new String[t.length];
        for (int i = 0; i < a.length; i++) {
//            System.out.println(i);
            a[i]=(String)t[i];
            
        }
        System.out.println(Arrays.toString(ExpressionParser.infixToRPN(a)));

    }

    public static List<String> tokenize(String s) {
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
            tokenizer.ordinaryChar('-');  // Don't parse minus as part of numbers.
            List<String> tokBuf = new ArrayList<String>();
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_NUMBER:
                        tokBuf.add(String.valueOf(tokenizer.nval));
                        break;
                    case StreamTokenizer.TT_WORD:
                        tokBuf.add(tokenizer.sval);
                        break;
                    default:  // operator
                        tokBuf.add(String.valueOf((char) tokenizer.ttype));
                }
            }
            return tokBuf;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    

}
