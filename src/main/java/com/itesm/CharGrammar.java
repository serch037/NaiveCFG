package com.itesm;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

//import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.model.Factory.*;

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
//        System.out.println("Done");
    }

    public void naiveBelongsUI(){
    }

    public boolean naiveBelongs(String target) {
        String current = ""+initial;
        String derivationTree = ""+initial;
        Pair<Boolean, String> tmp = naiveBelongsHelper(target, current, derivationTree);
        if (tmp.getLeft()){
            System.out.printf("La cadena %s es aceptada\n", target);
            drawDerivationTree(tmp.getRight());
        }
        return tmp.getLeft();
    }

    public void drawDerivationTree(String derivationTree) {
        System.out.println(derivationTree);
        try {
            createGraph(derivationTree);
        } catch(Exception e) {
            System.out.println("Exception");
        }
        
    }

    
    public void createGraph(String derivationTree) throws IOException{
        ArrayList<Node> nodes = createNodesArray(derivationTree);
        Graph graph = graph("ProyectoMates").directed().graphAttr().with(RankDir.LEFT_TO_RIGHT);
        for(Node n:nodes) {
            graph = graph.with(n);
        }
        File tmpImage = File.createTempFile("tmp", ".png", new File("images/"));
        Graphviz.fromGraph(graph).height(1000).render(Format.PNG).toFile(tmpImage);
        Desktop desktop = Desktop.getDesktop();
        desktop.open(tmpImage);
    }

    //S->0B|B->0BB|B->1|B->1S|S->0B|B->1S|S->0B|B->1
    public ArrayList<Node> createNodesArray(String derivationTree) {
        ArrayList<Node> nodes = new ArrayList<>();
        String[] splitOr = derivationTree.split("\\|");
        HashMap<Character, Integer> levelMap = new HashMap<>();
        int notFinalCount = 0;
        int idCount = 0;
        boolean afterArrow = false;
        for(int i = 1; i<splitOr.length; i++) {
            System.out.println(splitOr[i]+": ");
            String[] splitArrow = splitOr[i].split("->");

            if(!levelMap.containsKey(splitArrow[0].charAt(0))){
                levelMap.put(splitArrow[0].charAt(0),0);
            } else {
                if(notFinalCount>0){
                    //if(notFinalCount>1) {
                    //    levelMap.replace(splitArrow[0].charAt(0), levelMap.get(splitArrow[0].charAt(0)) - 1);
                    //} else {
                    //    notFinalCount--;
                    //}
                    notFinalCount--;
                } else {
                    levelMap.replace(splitArrow[0].charAt(0), levelMap.get(splitArrow[0].charAt(0)) + 1);
                }
            }
            Node parent = node(""+splitArrow[0].charAt(0)+"_"+levelMap.get(splitArrow[0].charAt(0)));
            for(int j = 0; j<splitArrow[1].length(); j++) {
                System.out.println(splitArrow[1].charAt(j));
                if(splitArrow[1].charAt(j) >= '0' && splitArrow[1].charAt(j) <= '9') {
                    Node son = node(""+splitArrow[1].charAt(j)+"_"+idCount);
                    idCount++;
                    parent = parent.link(to(son));
                } else {
                    notFinalCount++;
                    if(!levelMap.containsKey(splitArrow[1].charAt(j))){
                        levelMap.put(splitArrow[1].charAt(j),0);
                    } else {

                        levelMap.replace(splitArrow[1].charAt(j), levelMap.get(splitArrow[1].charAt(j)) + 1);
                    }
                    Node son = node(""+splitArrow[1].charAt(j)+"_"+levelMap.get(splitArrow[1].charAt(j)));
                    parent = parent.link(to(son));
                }
            }
            nodes.add(parent);
        }
        return nodes;
    }

    public Pair<Boolean, String> naiveBelongsHelper(String target, String accumulator, String derivationTree) {
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
                    Pair<Boolean, String> ans  = naiveBelongsHelper(target, newStr, derivationTreeTmp);
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
