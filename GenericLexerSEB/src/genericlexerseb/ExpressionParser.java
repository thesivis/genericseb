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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Bartosz Borucki (babor@icm.edu.pl), University of Warsaw, ICM based
 * on:
 * http://www.technical-recipes.com/2011/a-mathematical-expression-parser-in-java-and-cpp
 *
 */
public class ExpressionParser {
    // Associativity constants for operators  

    private boolean debug = false;
    
    private static final int LEFT_ASSOC = 0;
    private static final int RIGHT_ASSOC = 1;
    // Operators      
    private static final Map<String, int[]> OPERATORS = new HashMap<String, int[]>();
    
    static {
        // Map<"token", []{precendence, associativity, number of arguments}>          
        OPERATORS.put("+", new int[]{1, LEFT_ASSOC, 2});
        OPERATORS.put("-", new int[]{1, LEFT_ASSOC, 2});        
        OPERATORS.put("*", new int[]{5, LEFT_ASSOC, 2});
        OPERATORS.put("/", new int[]{5, LEFT_ASSOC, 2});
        OPERATORS.put("^", new int[]{10, RIGHT_ASSOC, 2});
        OPERATORS.put("~", new int[]{12, LEFT_ASSOC, 1});
        OPERATORS.put("sqrt", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("ln", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("log", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("exp", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("abs", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("sin", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("cos", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("tan", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("asin", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("acos", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("atan", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("signum", new int[]{15, LEFT_ASSOC, 1});                
    }

    private Map<String, Double> VARIABLES = new HashMap<String, Double>();    
    
    public ExpressionParser() {
        
    }
    
    public ExpressionParser(Map<String, Double> variables) {
        this(variables, false);
    }
    
    public ExpressionParser(Map<String, Double> variables, boolean debug) {
        VARIABLES.put("PI", Math.PI);
        VARIABLES.put("E", Math.E);
        VARIABLES.putAll(variables);        
        this.debug = debug;
    }

    private static boolean isOperator(String token) {
        return OPERATORS.containsKey(token);
    }

    private static boolean isAssociative(String token, int type) {
        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }

        if (OPERATORS.get(token)[1] == type) {
            return true;
        }
        return false;
    }

    private static int cmpPrecedence(String token1, String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalid tokens: " + token1
                    + " " + token2);
        }
        return OPERATORS.get(token1)[0] - OPERATORS.get(token2)[0];
    }

    public static String[] infixToRPN(String[] inputTokens) {
        ArrayList<String> out = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();

        for (String token : inputTokens) {
            if (isOperator(token)) {
                while (!stack.empty() && isOperator(stack.peek())) {
                    if ((isAssociative(token, LEFT_ASSOC)
                            && cmpPrecedence(token, stack.peek()) <= 0)
                            || (isAssociative(token, RIGHT_ASSOC)
                            && cmpPrecedence(token, stack.peek()) < 0)) {
                        out.add(stack.pop());
                        continue;
                    }
                    break;
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);  //   
            } else if (token.equals(")")) {
                while (!stack.empty() && !stack.peek().equals("(")) {
                    out.add(stack.pop());
                }
                stack.pop();
            } else {
                out.add(token);
            }
        }
        while (!stack.empty()) {
            out.add(stack.pop());
        }
        String[] output = new String[out.size()];
        return out.toArray(output);
    }

    public static boolean isNumeric(String token) {
        try {
            double d = Double.parseDouble(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean isVariable(String token) {
        return VARIABLES.containsKey(token);
    }

    public double RPNtoDouble(String[] tokens) {
        Stack<String> stack = new Stack<String>();

        for (String token : tokens) {
            if (!isOperator(token)) {
                stack.push(token);
            } else {
                int[] op = OPERATORS.get(token);
                Double[] d = new Double[op[2]];
                String tmp;
                for (int i = 0; i < d.length; i++) {
                    tmp = stack.pop();
                    if (isVariable(tmp)) {
                        d[i] = VARIABLES.get(tmp);
                    } else {
                        d[i] = Double.valueOf(tmp);
                    }
                }

                Double result = 0.0;
                if (token.compareTo("+") == 0) {
                    result = d[1] + d[0];
                } else if (token.compareTo("-") == 0) {
                    result = d[1] - d[0];
                } else if (token.compareTo("*") == 0) {
                    result = d[1] * d[0];
                } else if (token.compareTo("/") == 0) {
                    result = d[1] / d[0];
                } else if (token.compareTo("^") == 0) {
                    result = Math.pow(d[1], d[0]);
                } else if (token.compareTo("sqrt") == 0) {
                    result = Math.sqrt(d[0]);
                } else if (token.compareTo("ln") == 0) {
                    result = Math.log(d[0]);
                } else if (token.compareTo("log") == 0) {
                    result = Math.log10(d[0]);
                } else if (token.compareTo("exp") == 0) {
                    result = Math.exp(d[0]);
                } else if (token.compareTo("abs") == 0) {
                    result = Math.abs(d[0]);
                } else if (token.compareTo("sin") == 0) {
                    result = Math.sin(d[0]);
                } else if (token.compareTo("cos") == 0) {
                    result = Math.cos(d[0]);
                } else if (token.compareTo("tan") == 0) {
                    result = Math.tan(d[0]);
                } else if (token.compareTo("asin") == 0) {
                    result = Math.asin(d[0]);
                } else if (token.compareTo("acos") == 0) {
                    result = Math.acos(d[0]);
                } else if (token.compareTo("atan") == 0) {
                    result = Math.atan(d[0]);
                } else if (token.compareTo("signum") == 0) {
                    result = Math.signum(d[0]);
                } else if (token.compareTo("~") == 0) {
                    result = -d[0];
                } else {
                    //
                }
                stack.push(String.valueOf(result));
            }
        }

        System.out.println("Pilha:"+stack.size());
        return Double.valueOf(stack.pop());
    }

    public double evaluateExpr(String expr) {        
        String str = preprocessExpr(expr);
        if(debug) System.out.println(str);
        String[] input = str.split(" ");        
        String[] output = infixToRPN(input);
        if(debug) {
            for (String token : output) {
                System.out.print(token + " ");
            }
            System.out.println("");
        }
        Double result = RPNtoDouble(output);
        return result;        
    }

    public static String[] mysplit(String text, String pattern) {
        List<String> s = new ArrayList<String>();
        Matcher m = Pattern.compile(pattern).matcher(text);
        while (m.find()) {
            s.add(m.group());
        }
        return s.toArray(new String[s.size()]);
    }

    private static String fixUnaryMinus(String input, String pattern) {
        String str = new String(input);
        String tmp2;
        Matcher m = Pattern.compile(pattern).matcher(str);
        while(m.find()) {
            tmp2 = m.group();
            tmp2 = tmp2.replaceFirst(pattern, pattern.substring(0, pattern.length()-1)+"~");
            str = str.substring(0,m.start()) + tmp2 + str.substring(m.end(), str.length());
        }
        return str;        
    }

    private String preprocessExpr(String input) {
        String str = new String(input);
        str = str.replaceAll("\\s", "");
        str = str.replaceAll(",", ".");
        if(debug) System.out.println(str);
        
        //case #1 - minus at the beginning
        if(str.startsWith("-")) {
            str = str.replaceFirst("-", "~");
        }
        
        //case #2 - minus after (
        str = str.replaceAll("\\(-", "\\(~");
        
        //case #3 - minus after operators
        str = fixUnaryMinus(str, "\\+-");
        str = fixUnaryMinus(str, "--");
        str = fixUnaryMinus(str, "\\*-");
        str = fixUnaryMinus(str, "/-");
        str = fixUnaryMinus(str, "\\^-");        
        if(debug) System.out.println(str);
                
        ArrayList<String> ops = new ArrayList<String>();
        ops.addAll(OPERATORS.keySet());
        Collections.sort(ops, new StringLengthComparator());

        ArrayList<String> vars = new ArrayList<String>();
        vars.addAll(VARIABLES.keySet());
        Collections.sort(vars, new StringLengthComparator());
        
        String tmp;
        int j;
        for (int i = 0; i < str.length(); i++) {
            tmp = str.substring(i);
            boolean op = false;
            for (j = 0; j < ops.size(); j++) {
                if(tmp.startsWith(ops.get(j))) {
                    op = true;
                    break;
                }
            }
            
            if(op) {                
                str = str.substring(0, i) + " "+str.substring(i, str.length());
                i+=ops.get(j).length();                
            } else {
                boolean var = false;
                for (j = 0; j < vars.size(); j++) {
                    if(tmp.startsWith(vars.get(j))) {
                        var = true;
                        break;
                    }
                }
                
                if(var) {
                    str = str.substring(0, i) + " "+str.substring(i, str.length());
                    i+=vars.get(j).length();                
                }                
            }
        }
        
        str = str.replace("(", " (");
        str = str.replace(")", " )");

        Matcher m = Pattern.compile("[\\.0-9]+").matcher(str);
        int c = 0;
        while(m.find()) {
            str = str.substring(0, m.start()+c) + " " + str.substring(m.start()+c,str.length());
            c++;
        }
        
        str = str.trim();
        return str;
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
            Logger.getLogger(ExpressionParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    public static void main(String[] args) {
        
        Map<String, Double> vars = new HashMap<String, Double>();            
        vars.put("a", new Double(1.0));
        vars.put("b", new Double(1.0));        
        String str = "-2*ln(2)-(a-(b^-2))";  
        //String str = "( 1.0 + 2.123)*(2*a/b)-sqrt(5.0+6)";                
        //String str = "a^-2";  
        System.out.println(str);
        ExpressionParser parser = new ExpressionParser(vars, true);
//        List<String> ret = ExpressionParser.tokenize(str);
//        System.out.println(Arrays.toString(ExpressionParser.infixToRPN(ret.toArray(new String[ret.size()]))));
        double result = parser.evaluateExpr(str);        
        System.out.println("result = " + result);
           
    }
    
    private class StringLengthComparator implements java.util.Comparator<String> {

        public StringLengthComparator() {
            super();
        }

        @Override
        public int compare(String s1, String s2) {
            if(s1.length() == s2.length())
                return s1.compareTo(s2);
            return s2.length() - s1.length();
        }
    }    
    
    
    
    
}
