package com.itesm;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.mutNode;

//import java.awt.*;

public class CharGrammar {
    public Map<Character, List<String>> grammarMap;
    public Character initial;
    public HashSet<Character> variables;
    public char empty;

    public CharGrammar(){
        grammarMap = new HashMap<>();
        variables =  new HashSet<>();
        empty = '$';
    }

    public CharGrammar(String[] strs) {
        this();
        parseStrings(strs);
    }

    public class ValueNode{
        MutableNode node;
        char value;
        List<ValueNode> children;
        List<ValueNode> variableChildren;


        public ValueNode(char value) {
            this.value = value;
            this.node = mutNode(UUID.randomUUID().toString()).add(Label.of(""+value));
        }

        public void setChildren(String value) {
            children = new ArrayList<>();
            variableChildren =  new ArrayList<>();
            for( char c : value.toCharArray()){
                ValueNode tmpNode = new ValueNode(c);
                this.node.addLink(tmpNode.node);
                children.add(tmpNode);
                if (Character.isUpperCase(c)) {
                    variableChildren.add(tmpNode);
                }
            }
        }
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


    public Pair<Boolean, String> naiveBelongs(String target) {
        String current = ""+initial;
        String derivationTree = ""+initial;
        Pair<Boolean, String> tmp = naiveBelongsHelper(target, current, derivationTree);
        String message = "";
        if (tmp.getLeft()){
            message = String.format("String %s is accepted\n", target);
            System.out.print(message);
            System.out.println(tmp.getRight());
            drawDerivationTree(tmp.getRight());
        }
        else {
            message = String.format("String %s is not accepted\n", target);
            System.out.print(message);
        }
        return new ImmutablePair<>(tmp.getLeft(), message);
    }

    public void drawDerivationTree(String derivaitionTree){
        Stack<ValueNode> nodeQueue = new Stack<>();
        String[] steps = derivaitionTree.split("\\|");
        int count = 1;
        ValueNode root = new ValueNode(steps[0].charAt(0));
        nodeQueue.add(root);
        while (!nodeQueue.isEmpty()){
            ValueNode head = nodeQueue.pop();
            String[] values = steps[count].split("\\->");
            String variables = values[1].replaceAll("[^A-Z]", "");
            head.setChildren(values[1]);
            for (int i = head.variableChildren.size()-1; i >=0 ; i--) {
                nodeQueue.add(head.variableChildren.get(i));
            }
            count++;
        }
        Graph graph = graph("ProyectoMates").directed().graphAttr().with(RankDir.TOP_TO_BOTTOM);
        graph = graph.with(root.node);
        try {
            File tmpImage = File.createTempFile("tmp", ".png", new File("images/"));
            Graphviz.fromGraph(graph).height(1000).render(Format.PNG).toFile(tmpImage);
            Desktop desktop = Desktop.getDesktop();
            desktop.open(tmpImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pair<Boolean, String> naiveBelongsHelper(String target, String accumulator, String derivationTree) {
        String accumulatorCompare = accumulator.replaceAll("\\$", "");
        if (target.equals(accumulatorCompare)) {
            return new ImmutablePair<>(true, derivationTree);
        }
        if (isPartialMatchFromLeft(target, accumulator)) {
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

    public boolean isPartialMatchFromLeft(String target, String current) {
        ArrayList<Character> tmpCurrentChars = new ArrayList<>();
        String currentCompare = current.replaceAll("[A-Z\\$]", "");
        if (currentCompare.length() > target.length()) return false;
        return  isSubsequence(target, currentCompare);
    }

    public boolean isSubsequence(String target, String current) {
        if (current.length() == 0) return true;
        int indexC = 0, indexT= 0;
        while (indexT < target.length()) {
            if (target.charAt(indexT) == current.charAt(indexC)) {
                indexC++;
                if (indexC == current.length()) return true;
            }
            indexT++;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Hello");
        //Input GUI set up
        JFrame frame = new JFrame("Grammar GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,200);
        //center middle of the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();

        JLabel label1 = new JLabel("Enter transition S->a|aA");
        JTextField txtfield1 = new JTextField(10);
        JLabel label2 = new JLabel("Enter string to check");
        JTextField txtfield2 = new JTextField(10);
        JButton addBtn = new JButton("Add grammar");
        JButton endBtn = new JButton("Check if accepts");
        //addBtn.addActionListener(this);
        //endBtn.addActionListener(this);

        panel1.add(label1);
        panel1.add(txtfield1);
        panel2.add(label2);
        panel2.add(txtfield2);
        panel3.add(addBtn);
        panel3.add(endBtn);

        frame.getContentPane().add(BorderLayout.NORTH,panel1);
        frame.getContentPane().add(BorderLayout.CENTER,panel2);
        frame.getContentPane().add(BorderLayout.SOUTH,panel3);

        frame.setVisible(true);

        List<String> fs = new ArrayList<>();
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String trn = txtfield1.getText();
                System.out.println("Add transition pressed and read: "+trn);
                fs.add(trn);
            }
        });

        endBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent w) {
                System.out.println("Check if accepts");
                String strToCheck = txtfield2.getText();
                String[] test = fs.toArray(new String[fs.size()]);
                CharGrammar charGrammar = new CharGrammar(test);
                System.out.println();
                String message = charGrammar.naiveBelongs(strToCheck).getRight();
                JOptionPane.showMessageDialog(frame, message);
            }
        });
    }

    public String substitute(String str, int index, String other) {
        if (other.equals("" + this.empty)) other = "";
        return str.substring(0, index) + other + str.substring(index + 1);
    }
}
