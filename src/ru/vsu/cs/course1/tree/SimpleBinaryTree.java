package ru.vsu.cs.course1.tree;

import java.util.function.Function;

/**
 * Реализация простейшего бинарного дерева
 */
public class SimpleBinaryTree<T> implements BinaryTree<T> {

    protected class SimpleTreeNode implements BinaryTree.TreeNode<T> {
        public T value;
        public SimpleTreeNode left;
        public SimpleTreeNode right;

        public SimpleTreeNode(T value, SimpleTreeNode left, SimpleTreeNode right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public SimpleTreeNode(T value) {
            this(value, null, null);
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public TreeNode<T> getLeft() {
            return left;
        }

        @Override
        public TreeNode<T> getRight() {
            return right;
        }
    }

    protected SimpleTreeNode root = null;

    protected Function<String, T> fromStrFunc;
    protected Function<T, String> toStrFunc;

    public SimpleBinaryTree(Function<String, T> fromStrFunc, Function<T, String> toStrFunc) {
        this.fromStrFunc = fromStrFunc;
        this.toStrFunc = toStrFunc;
    }

    public SimpleBinaryTree(Function<String, T> fromStrFunc) {
        this(fromStrFunc, Object::toString);
    }

    public SimpleBinaryTree() {
        this(null);
    }

    @Override
    public TreeNode<T> getRoot() {
        return root;
    }

    public void clear() {
        root = null;
    }

    private T fromStr(String s) throws Exception {
        s = s.trim();
        if (s.length() > 0 && s.charAt(0) == '"') {
            s = s.substring(1);
        }
        if (s.length() > 0 && s.charAt(s.length() - 1) == '"') {
            s = s.substring(0, s.length() - 1);
        }
        if (fromStrFunc == null) {
            throw new Exception("Не определена функция конвертации строки в T");
        }
        return fromStrFunc.apply(s);
    }

    private static class IndexWrapper {
        public int index = 0;
    }

    private void skipSpaces(String bracketStr, IndexWrapper iw) {
        while (iw.index < bracketStr.length() && Character.isWhitespace(bracketStr.charAt(iw.index))) {
            iw.index++;
        }
    }

    private T readValue(String bracketStr, IndexWrapper iw) throws Exception {
        // пропуcкаем возможные пробелы
        skipSpaces(bracketStr, iw);
        if (iw.index >= bracketStr.length()) {
            return null;
        }
        int from = iw.index;
        boolean quote = bracketStr.charAt(iw.index) == '"';
        if (quote) {
            iw.index++;
        }
        while (iw.index < bracketStr.length() && (
                    quote && bracketStr.charAt(iw.index) != '"' ||
                    !quote && !Character.isWhitespace(bracketStr.charAt(iw.index)) && "(),".indexOf(bracketStr.charAt(iw.index)) < 0
               )) {
            iw.index++;
        }
        if (quote && bracketStr.charAt(iw.index) == '"') {
            iw.index++;
        }
        String valueStr = bracketStr.substring(from, iw.index);
        T value = fromStr(valueStr);
        skipSpaces(bracketStr, iw);
        return value;
    }

    private SimpleTreeNode fromBracketStr(String bracketStr, IndexWrapper iw) throws Exception {
        T parentValue = readValue(bracketStr, iw);
        SimpleTreeNode parentNode = new SimpleTreeNode(parentValue);
        if (bracketStr.charAt(iw.index) == '(') {
            iw.index++;
            skipSpaces(bracketStr, iw);
            if (bracketStr.charAt(iw.index) != ',') {
                parentNode.left = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) == ',') {
                iw.index++;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                parentNode.right = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                throw new Exception(String.format("Ожидалось ')' [%d]", iw.index));
            }
            iw.index++;
        }

        return parentNode;
    }

    public void refactoringRightSubTree(int level)
    {

        SimpleTreeNode rightLastNode = root;
        SimpleTreeNode rightLastNodeChild = null;

        for(int i = 0; i < level && rightLastNode != null; i++)
        {
            rightLastNode = rightLastNode.right;
        }

        if(rightLastNode.right != null)
        {
            rightLastNodeChild = rightLastNode.right;
        }

        rightLastNode.right = root.left;
        SimpleTreeNode lastLeftSubTreeNode = findLastLeftNode(root.left);
        lastLeftSubTreeNode.right = rightLastNodeChild;

        root.left = null;
    }

    public void refactoringLeftSubTree(int level)
    {

        SimpleTreeNode leftLastNode = root;
        SimpleTreeNode leftLastNodeChild = null;

        for(int i = 0; i < level && leftLastNode != null; i++)
        {
            leftLastNode = leftLastNode.left;
        }

        if(leftLastNode.left != null)
        {
            leftLastNodeChild = leftLastNode.left;
        }

        leftLastNode.left = root.right;
        SimpleTreeNode lastRightSubTreeNode = findLastRightNode(root.right);
        lastRightSubTreeNode.left = leftLastNodeChild;

        root.right = null;
    }

    private SimpleTreeNode findLastLeftNode(SimpleTreeNode node)
    {
        SimpleTreeNode leftLastNode = node;
        while (leftLastNode.left != null)
        {
            leftLastNode = leftLastNode.left;
        }
        return leftLastNode;
    }

    private SimpleTreeNode findLastRightNode(SimpleTreeNode node)
    {
        SimpleTreeNode rightLastNode = node;
        while (rightLastNode.right != null)
        {
            rightLastNode = rightLastNode.right;
        }
        return rightLastNode;
    }


    public void fromBracketNotation(String bracketStr) throws Exception {
        IndexWrapper iw = new IndexWrapper();
        SimpleTreeNode root = fromBracketStr(bracketStr, iw);
        if (iw.index < bracketStr.length()) {
            throw new Exception(String.format("Ожидался конец строки [%d]", iw.index));
        }
        this.root = root;
    }

    public void refactorTree(){
        insideToTreeWithRef(this.root);
    }

    private void insideToTreeWithRef(SimpleTreeNode node){
        if(isLeaf(node)){
            addPotomok(node);
            return;
        }

        if(!(node.left == null)) insideToTreeWithRef(node.left);
        if(!(node.right == null)) insideToTreeWithRef(node.right);
    }

    private boolean isLeaf(SimpleTreeNode node){
        if (node.left == null && node.right == null) return true;
        return false;
    }

    private void addPotomok(SimpleTreeNode node){
        Integer value = (Integer) node.value;
        //toDo пофиксить красное подчёркивание
        if(value % 2 == 0) node.left = new SimpleTreeNode(1);
        else node.left = new SimpleTreeNode(-1);
    }



}
