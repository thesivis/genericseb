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

//        eq = "4*x+5.2024-(Log(x,y)^-z)-300.12";
        List<String> ret = tokenize(eq);
        for (String string : ret) {
            System.out.println(string);
        }

    }

    public static List<String> tokenize(String s) {
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
            tokenizer.ordinaryChar('-');  // Don't parse minus as part of numbers.
            List<String> tokBuf = new ArrayList<String>();
            char menos = '-';
            char c = ' ';
            boolean hasMenos = false;
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_NUMBER:
                        String number = String.valueOf(tokenizer.nval);
                        if (hasMenos) {
                            number = menos + number;
                        }
                        tokBuf.add(number);
                        hasMenos = false;
                        break;
                    case StreamTokenizer.TT_WORD:
                        number = tokenizer.sval;
                        if (hasMenos) {
                            number = menos + number;
                        }
                        tokBuf.add(number);
                        hasMenos = false;
                        break;
                    default:  // operator
                        if (hasMenos) {
                            tokBuf.add(String.valueOf(c));
                        }
                        c = (char) tokenizer.ttype;
                        if (c == menos) {
                            hasMenos = true;
                        } else {
                            hasMenos = false;
                            tokBuf.add(String.valueOf(c));
                        }
                }
            }
            return tokBuf;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
