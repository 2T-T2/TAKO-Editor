import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class App extends JFrame implements ComponentListener, DocumentListener, ActionListener, UndoableEditListener, KeyListener {
    JPanel panel;
    JTextArea textarea;
    JLabel label;
    JScrollPane sc;
    Document doc;
    Matcher matcher;
    JMenuItem exportItem;
    JMenuItem redoItem;
    JMenuItem undoItem;
    JMenuItem commentoutItem;
    UndoableEdit undo;
    boolean isCtrl = false;
    boolean isZ    = false;
    boolean isY    = false;
    final Pattern  STRING_PTN = Pattern.compile("&quot;(.*?)&quot;");
    final Pattern  NUMBER_PTN = Pattern.compile("-?(0|[1-9]\\d*)(\\.\\d+|)");
    final Pattern  CLASS_PTN  = Pattern.compile("[ \\(][A-Z][a-zA-z]++");
    final Pattern  METHOD_PTN = Pattern.compile("[ .][a-z][a-zA-Z]++\\(");
    final String[] PROC_CONTROL_KEYWORD = {"break","continue","do","else","for","if","return","while","default","case","switch","goto"};
    final String[] TYPE_KEYWORD         = {"char","const","double","enum","float","int","long","restrict","short","void","volatile"};
    final String[] COMMON_KEYWORD       = {"class","false","import","private","protected","public","this","throw","true","try","static","catch"};
    final String[] JAVA_KEYWORD         = {"abstract","assert","boolean","byte","extends","finally","final","implements","import","instanceof","interface","null","native", "package","strictfp","super", "synchronized","throws","transient","new"};
    private boolean isSlash;
    App (String title) {
        setTitle(title);
        setLocationRelativeTo(null);
        setSize(500, 500);
        addComponentListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
            exportItem = new JMenuItem("export to html");
            exportItem.addActionListener(this);
            fileMenu.add(exportItem);
        JMenu editMenu = new JMenu("Edit");
            undoItem = new JMenuItem("undo");
            undoItem.addActionListener(this);
            redoItem = new JMenuItem("redo");
            redoItem.addActionListener(this);
            commentoutItem = new JMenuItem("comment out current line");
            commentoutItem.addActionListener(this);
            editMenu.add(undoItem);
            editMenu.add(redoItem);
            editMenu.add(commentoutItem);
        menubar.add(fileMenu);
        menubar.add(editMenu);
        ImageIcon icon = new ImageIcon("textEditor/res/img/TAKO.png");
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(getWidth(), getHeight()));
        sc = new JScrollPane(panel);
        sc.setPreferredSize(new Dimension(getWidth(), getHeight()));
        textarea = new JTextArea();
        textarea.setFont(font);
        textarea.setTabSize(8);
        textarea.setForeground(new Color(0, 0 ,0 , 100));
        textarea.setOpaque(false);
        textarea.addKeyListener(this);
        textarea.setBounds(0, 0, getWidth(), getHeight());
        doc = textarea.getDocument();
        doc.addDocumentListener(this);
        doc.addUndoableEditListener(this);
        label = new JLabel("");
        label.setFont(font);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setVerticalAlignment(JLabel.TOP);
        label.setBounds(0, 0, getWidth(), getHeight());
        panel.add(label);
        panel.add(textarea);
        setIconImage(icon.getImage());
        setJMenuBar(menubar);
        add(sc, BorderLayout.CENTER);
        pack();
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("pre{margin-top: 0;}");
        css.addRule(".decimal{color: teal}");
        css.addRule(".type-key{color: green;}");
        css.addRule(".common-key{color: blue;}");
        css.addRule(".java-key{color: purple;}");
        css.addRule(".proc-control-key{color: purple;}");
        css.addRule(".string{color: orange;}");
        css.addRule(".class{color: green;}");
        css.addRule(".method{color: #008b8b;}");
    }
    public static void main(String[] args) throws Exception {
        App frame = new App("TAKO-Editor");
        frame.setVisible(true);
    }
    private void showDialog(String mes) {
        JOptionPane.showMessageDialog(null, mes);
    }
    private void commentOut() {
        int cpos = textarea.getCaretPosition();
        String befor = textarea.getText().substring(0, cpos);
        String[] lines = befor.split("\\n");
        int lineNum = lines.length;
        lines = textarea.getText().split("\\n");
        StringBuilder sb = new StringBuilder(lines[lineNum-1]);
        int cnt = 0;
        for (char c: sb.toString().toCharArray()){
            if (c == ' '){
                cnt++;
            }else{
                break;
            }
        }
        System.out.println(sb.toString());
        textarea.setText(textarea.getText().replace(lines[lineNum-1], sb.insert(cnt, "// ").toString()));
        textarea.setCaretPosition(cpos);
    }
    private void unDo () {
        undo.undo();
        undoItem.setEnabled(undo.canUndo());
        redoItem.setEnabled(undo.canRedo());
    }
    private void reDo () {
        undo.redo();
        undoItem.setEnabled(undo.canUndo());
        redoItem.setEnabled(undo.canRedo());
    }
    private String sanitaizeEnc (String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("\n", "<br>");
    }
    private String highLight(String val){
        val = sanitaizeEnc(val);
        for (String key: PROC_CONTROL_KEYWORD) {
            val = val.replace(key + " ", "<span class=proc-control-key>" + key + "</span> ");
            val = val.replace(key + "(", "<span class=proc-control-key>" + key + "</span>(");
        }
        matcher = METHOD_PTN.matcher(val);
        while (matcher.find()) {
            val = val.replace(matcher.group(), "<span class=method>" + matcher.group().substring(0, matcher.group().length()-1) + "</span>(");
        }
        matcher = NUMBER_PTN.matcher(val);
        while (matcher.find()) {
            val = val.replace(matcher.group(), "<span class=decimal>" + matcher.group() + "</span>" );
        }
        for (String key: COMMON_KEYWORD){
            val = val.replace(key + " ", "<span class=common-key>" + key + "</span> ");
            val = val.replace(key + ".", "<span class=common-key>" + key + "</span>.");
            val = val.replace("(" + key, "(<span class=common-key>" + key + "</span>");
        }
        for (String key: JAVA_KEYWORD){
            val = val.replace(key + " ", "<span class=java-key>" + key + "</span> ");
        }
        for (String key: TYPE_KEYWORD) {
            val = val.replace(key + " ", "<span class=type-key>" + key + "</span> ");
            val = val.replace(key + "<", "<span class=type-key>" + key + "</span><");
        }
        matcher = CLASS_PTN.matcher(val);
        while (matcher.find()) {
            val = val.replace(matcher.group(), matcher.group().substring(0, 1) + "<span class=class>" + matcher.group().substring(1) + "</span>");
        }
        matcher = STRING_PTN.matcher(val);
        while (matcher.find()) {
            val = val.replace(matcher.group(), "<span class=string>" + matcher.group().replaceAll("<span class=(.*?)>", "").replace("</span>", "") + "</span>" );
        }
        return val;
    }
    @Override
    public void componentResized(ComponentEvent e) {
        sc.setPreferredSize(new Dimension(getWidth(), getHeight()));
    }
    @Override
    public void componentMoved(ComponentEvent e) {
    }
    @Override
    public void componentShown(ComponentEvent e) {
    }
    @Override
    public void componentHidden(ComponentEvent e) {
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
            isCtrl = true;
        }else if ( e.getKeyCode() == KeyEvent.VK_Z ){
            isZ = true;
        }else if (e.getKeyCode() == KeyEvent.VK_Y ) {
            isY = true;
        }else if (e.getKeyCode() == KeyEvent.VK_SLASH ) {
            isSlash = true;
        }
        if (isCtrl && isZ && undo.canUndo()) {
            unDo();
        }
        if (isCtrl && isY && undo.canRedo()) {
            reDo();
        }
        if (isCtrl && isSlash ) {
            commentOut();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
            isCtrl = false;
        }else if ( e.getKeyCode() == KeyEvent.VK_Z ){
            isZ = false;
        }else if (e.getKeyCode() == KeyEvent.VK_Y ) {
            isY = false;
        }else if (e.getKeyCode() == KeyEvent.VK_SLASH ) {
            isSlash = false;
        }
    }
    @Override
    public void insertUpdate(DocumentEvent e) {
        String val = highLight(textarea.getText());
        label.setText("<html><pre>" + val + "</pre></html>");
        panel.setPreferredSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
        label.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
        textarea.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
    }
    @Override
    public void removeUpdate(DocumentEvent e) {
        String val = highLight(textarea.getText());
        label.setText("<html><pre>" + val + "</pre></html>");
        panel.setPreferredSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        undo = e.getEdit();
        undoItem.setEnabled(undo.canUndo());
        redoItem.setEnabled(undo.canRedo());
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals( exportItem.getText() )){
            try {
                File f = new File("output.html");
                FileWriter fw;
                fw = new FileWriter(f);
                fw.write("<head><style>pre{margin-top: 0;}.decimal{color: teal}.type-key{color: green;}.common-key{color: blue;}.java-key{color: purple;}.proc-control-key{color: purple;}.string{color: orange;}.class{color: green;}</style></head>" + label.getText());
                fw.close();
                showDialog("htmlファイルを保存しました");
            } catch (IOException e1) {
                e1.printStackTrace();
                showDialog("ファイル保存時にエラーが発生しました");
            }
        }else if (e.getActionCommand().equals( redoItem.getText() )){
            reDo();
        }else if (e.getActionCommand().equals( undoItem.getText() )){
            unDo();
        }else if (e.getActionCommand().equals( commentoutItem.getText() )){
            commentOut();
        }
    }
}
