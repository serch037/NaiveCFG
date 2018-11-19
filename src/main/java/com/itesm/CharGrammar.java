package com.itesm;


import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharGrammar {
    public Map<Character, List<String>> grammarMap;
    public Character initial;
    public HashSet<Character> variables;
    public String empty;

    public CharGrammar(){
        grammarMap = new HashMap<>();
        variables =  new HashSet<>();
        empty = "$";
    }

    public CharGrammar(String[] strs) {
        this();
        parseStrings(strs);
    }

    //S->0A|0B|^
    public void parseStrings(String[] strings) {
        Pattern pattern = Pattern.compile("(\\w+)\\s?(?:->)\\s?(.*)$");
        for (String line : strings) {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches()) continue;
            Character from = matcher.group(1).charAt(0);
            String[] to = matcher.group(2).split("\\|");

            grammarMap.putIfAbsent(from, new ArrayList<>());
            grammarMap.get(from).addAll(Arrays.asList(to));


            if (initial == null) {
                variables.add(from);
                initial = from;
            }

            Arrays.stream(to)
                    .map(c -> c.toCharArray())
                    .forEach(c -> {
                        for (char tmpChar :c ) {
                            if(Character.isUpperCase(tmpChar)){
                                variables.add(tmpChar);
                            }
                        }
                    });
        }
        System.out.println("Done");
    }

    public void naiveBelongsUI(){
    }

    public boolean naiveBelongs(String target) {
        String current = ""+initial;
        String derivationTree = ""+initial;
        MutableGraph<Character> treeMut = GraphBuilder.directed().build();
        treeMut.addNode('a');
        treeMut.addNode('a');
        treeMut.putEdge('a', 'a');
        Pair<Boolean, String> tmp = naiveBelongsHelper(target, current, derivationTree, treeMut);
        if (tmp.getLeft()){
            System.out.println("La cadena es aceptada");
            drawDerivationTree(tmp.getRight());
        }
        return tmp.getLeft();
    }

    public void drawDerivationTree(String derivationTree){
        System.out.println(derivationTree);
    }

    public Pair<Boolean, String> naiveBelongsHelper(String target, String accumulator, String derivationTree, MutableGraph<Character> tree) {
        if (target.equals(accumulator)) {
            return new ImmutablePair<>(true, derivationTree);
        }
        if (isPartialMatch(target, accumulator)) {
            ArrayList<Integer> positions = getVariablePositions(accumulator);
            for (Integer position: positions) {
                List<String> tmp =  grammarMap.get(accumulator.charAt(position));
                for (String str :tmp){
                    String newStr = substitute(accumulator, position,str);
                    String derivationTreeTmp = derivationTree +  "|" + accumulator.charAt(position)+ "->" + str;
                    Pair<Boolean, String> ans  = naiveBelongsHelper(target, newStr, derivationTreeTmp, tree);
                    if (ans.getLeft()) {
                        return ans;
                    }
                }
            }
        }
        return new ImmutablePair<>(false, "");
    }

    public ArrayList<Integer> getVariablePositions(String str) {
        ArrayList<Integer> positions = new ArrayList<>();
        char[] tmpChar = str.toCharArray();
        for (int i = 0; i < tmpChar.length; i++) {
            if (variables.contains(tmpChar[i])) {
                positions.add(i);
            }
        }
        return positions;
    }

    // TODO: Potential bug if current has no more variables and its length is greater than target
    public boolean isPartialMatch(String target, String current) {
        char[] currentChars = current.toCharArray();
        char[] targetChars = target.toCharArray();
        int minLength = Math.min(currentChars.length, targetChars.length);

        for (int i = 0; i < minLength; i++) {
            if (currentChars[i] != targetChars[i] && !variables.contains(currentChars[i])) {
                return false;
            }
        }
        return true;
    }

    public String substitute(String str, int index, String other) {
        if (other.equals(this.empty)) other = "";
        return str.substring(0, index) + other + str.substring(index + 1, str.length());
    }
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
