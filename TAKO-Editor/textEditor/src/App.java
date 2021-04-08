import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class App extends JFrame implements ComponentListener, DocumentListener, ActionListener, UndoableEditListener, KeyListener {
    JPanel panel;
    JTextArea textarea;
    JLabel label;
    JLabel statusBar;
    JScrollPane sc;
    Document doc;
    Matcher matcher;
    JMenuItem exportItem;
    JMenuItem redoItem;
    JMenuItem undoItem;
    JMenuItem commentoutItem;
    JMenuItem newFlieItem;
    JMenuItem overWriteItem;
    JMenuItem openItem;
    UndoableEdit undo;
    String cssRule;
    String openedFile = null;
    Map<Character, Character> pairChar = new HashMap<Character, Character>();
    Map<String, Color> cssStandert     = new HashMap<String, Color>();
    final int indentSize = 2;
    final Pattern  STRING_PTN = Pattern.compile("&quot;(.*?)&quot;");
    final Pattern  NUMBER_PTN = Pattern.compile("[^a-zA-Z\\^!\"#\\$%'\\(\\)\\*\\+\\-,\\/:;<=>?@\\[\\]\\_\\{\\}\\|~\\\\][0-9]++");
    final Pattern  CLASS_PTN  = Pattern.compile("[ \\(][A-Z][a-zA-z]++");
    final Pattern  METHOD_PTN = Pattern.compile("[ .][a-z][a-zA-Z]++(\\(| \\()");
    final String[] PROC_CONTROL_KEYWORD = {"break","continue","do","else","for","if","return","while","default","case","switch","goto"};
    final String[] TYPE_KEYWORD         = {"char","const","double","enum","float","int","long","restrict","short","void","volatile"};
    final String[] COMMON_KEYWORD       = {"class","false","import","private","protected","public","this","throw","true","try","static","catch"};
    final String[] JAVA_KEYWORD         = {"abstract","assert","boolean","byte","extends","finally","final","implements","import","instanceof","interface","null","native", "package","strictfp","super", "synchronized","throws","transient","new", "@Override"};
    App (String title, String path) {
        super(title);
        setLocation(50, 50);
        setSize(500, 500);
        addComponentListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPairCharRule();
        setCssStandert();
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
            openItem = new JMenuItem("Open");
            openItem.addActionListener(this);
            newFlieItem = new JMenuItem("Save as ...");
            newFlieItem.addActionListener(this);
            overWriteItem = new JMenuItem("Save");
            overWriteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            overWriteItem.addActionListener(this);
            exportItem = new JMenuItem("export to html");
            exportItem.addActionListener(this);
            fileMenu.add(openItem);
            fileMenu.add(newFlieItem);
            fileMenu.add(overWriteItem);
            fileMenu.add(exportItem);
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
            undoItem = new JMenuItem("undo");
            undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            undoItem.addActionListener(this);
            redoItem = new JMenuItem("redo");
            redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            redoItem.addActionListener(this);
            commentoutItem = new JMenuItem("comment out current line");
            commentoutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK));
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
        textarea.setForeground(new Color(0, 0 ,0 , 0));
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
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(0, 2, 0, 0));
        statusBar = new JLabel("");
        statusBar.setHorizontalAlignment(JLabel.RIGHT);
        setStatusBar();
        panel.add(textarea);
        panel.add(label);
        setIconImage(icon.getImage());
        setJMenuBar(menubar);
        add(sc, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
        pack();
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        File cssFile = new File("textEditor/res/style/style.css");
        if (cssFile.exists()){
            cssRule = readTxtFile(cssFile);
            css.addRule(cssRule);
            Enumeration<?> i = css.getStyle("pre").getAttributeNames();
            while (i.hasMoreElements()) {
                Object k = i.nextElement();
                if(k.toString().equals("background-color")){
                    panel.setBackground(cssStandert.get( css.getStyle("pre").getAttribute(k).toString() ));
                }
            }
        }else{
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
        if (path != null){
            File f = new File(path);
            if (f.exists()) {
                if (f.canRead()){
                    if (f.isFile()){
                        textarea.setText( readTxtFile(f) );
                        openedFile =  f.getAbsolutePath();
                    }else {
                        showDialog("開こうとしているのはフォルダではありませんか？");
                    }
                }else {
                    showDialog("ファイルが読み込める状況にありませんでした");
                }
            }else{
                if (showYesNoDialog("開こうとしたファイルが存在しませんでした。\nファイルを作成しますか？", "TAKOからの質問") ){
                    try {
                        if (f.createNewFile()) {
                            openedFile = f.getAbsolutePath();
                        }else {
                            showDialog("ファイルの作成に失敗しました");
                        }
                    } catch (IOException e) {
                        showDialog("ファイルの作成に失敗しました");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        App frame;
        if (args.length == 1){
            frame = new App("TAKO-Editor", args[0]);
        }else {
            frame = new App("TAKO-Editor", null);
        }
        frame.setVisible(true);
    }
    private String readTxtFile(File f){
        String str = "";
        try {
            FileReader reader = new FileReader(f);
            BufferedReader br = new  BufferedReader(reader);
            String tmp = "";
            str = br.readLine();
            while ((tmp = br.readLine()) != null) {
                str = str + "\n" + tmp;
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        return str;
    }
    private void save() {
        if (openedFile == null ) {
            showDialog("ファイルを新規作成します");
            saveAs();
        } else {
            File f = new File(openedFile);
            if (f.exists()) {
                if (f.canWrite()) {
                    if (f.isFile()) {
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                            for (String line: textarea.getText().split("\\n")){
                                bw.write(line);
                                bw.newLine();
                            }
                            bw.close();
                            setStatusBar("ファイルを保存しました");
                        } catch (IOException e) {
                            showDialog("上書き処理に失敗しました");
                            e.printStackTrace();
                        }
                    }else {
                        showDialog("ファイルを新規作成します");
                        saveAs();
                    }
                }else {
                    showDialog("ファイルが書き込める状況にありませんでした");
                }
            }else {
                showDialog("ファイルを新規作成します");
                saveAs();
            }
        }
    }
    private void saveAs() {
        String path = showSaveAsDialog();
        if (path != null ){
            File f = new File(path);
            if ( !(f.exists()) ) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    showDialog("ファイルの作成に失敗しました");
                    e.printStackTrace();
                }
                openedFile = path;
                save();
            }
        }
    }
    private String fileOpen() {
        String path = showOpenDialog();
        if (path != null ){
            File f = new File(path);
            if (f.exists()) {
                if (f.canRead()){
                    if (f.isFile()){
                        openedFile =  f.getAbsolutePath();
                        return readTxtFile(f);
                    }else {
                        showDialog("開こうとしているのはフォルダではありませんか？");
                    }
                }else {
                    showDialog("ファイルが読み込める状況にありませんでした");
                }
            }else {
                showDialog("開こうとしたファイルが存在しませんでした。");
            }
        }
        return null;
    }
    private void showDialog(String mes) {
        JOptionPane.showMessageDialog(null, mes);
    }
    private boolean showYesNoDialog(String mes, String title) {
        int opinion = JOptionPane.showConfirmDialog(null, mes, title, JOptionPane.YES_NO_OPTION);
        if (opinion == JOptionPane.YES_OPTION){
            return true;
        }else {
            return false;
        }
    }
    private String showSaveAsDialog(){
        FileDialog fDialog = new FileDialog(this);
        do {
            fDialog.setMode(FileDialog.SAVE);
            fDialog.setVisible(true);
            if (fDialog.getDirectory() == null){
                showDialog("ファイルを選択をキャンセルしたか、エラーが発生しました");
                return null;
            }
        } while (fDialog.getFile() == null);
        return fDialog.getDirectory() + fDialog.getFile();
    }
    private String showOpenDialog() {
        FileDialog fDialog = new FileDialog(this);
        do {
            fDialog.setMode(FileDialog.LOAD);
            fDialog.setVisible(true);
            if (fDialog.getDirectory() == null){
                showDialog("ファイル保存をキャンセルしました。");
                return null;
            }
        }while (fDialog.getFile() == null);
        return fDialog.getDirectory() + fDialog.getFile();
    }
    private int countCurlineBlank(){
        int cpos = textarea.getCaretPosition();
        String befor = textarea.getText().substring(0, cpos);
        String[] lines = befor.split("\\n");
        int lineNum = lines.length;
        lines = textarea.getText().split("\\n");
        int cnt = 0;
        if (lines.length != 0){
            String line = lines[lineNum-1];
            for (char c: line.toCharArray()){
                if (c == ' '){
                    cnt++;
                }else{
                    break;
                }
            }
        }
        return cnt;
    }
    private int getCurRow() {
        if (textarea.getText().equals("")){return 0;}
        return textarea.getText().substring(0, textarea.getCaretPosition()).split("\\n").length;
    }
    private int getCurColumn() {
        if (textarea.getText().replace("\n", "").equals("")){return textarea.getText().substring(0, textarea.getCaretPosition()).length();}
        String val[] = textarea.getText().substring(0, textarea.getCaretPosition()).split("\\n");
        return val[val.length-1].length();
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
        textarea.setText(textarea.getText().replace(lines[lineNum-1], sb.insert(cnt, "// ").toString()));
        textarea.setCaretPosition(cpos);
    }
    private void setStatusBar() {
        statusBar.setText("行"+getCurRow()+"，列"+getCurColumn());
    }
    private void setStatusBar(String mes) {
        statusBar.setText(mes);
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
    private void setPairCharRule() {
        pairChar.put('{', '}');
        pairChar.put('"', '"');
        pairChar.put('(', ')');
        pairChar.put('[', ']');
        pairChar.put('\'', '\'');
    }
    private void setCssStandert() {
        cssStandert.put("black"     , new Color( 0 ,  0 ,  0 ));
        cssStandert.put("silver"    , new Color(192, 192, 192));
        cssStandert.put("gray"      , new Color(128, 128, 128));
        cssStandert.put("white"     , new Color(255, 255, 255));
        cssStandert.put("maroon"    , new Color(128,  0 ,  0 ));
        cssStandert.put("red"       , new Color(255,  0 ,  0 ));
        cssStandert.put("purple"    , new Color(128,  0 , 128));
        cssStandert.put("fuchsia"   , new Color(255,  0 , 255));
        cssStandert.put("green"     , new Color( 0 , 128,  0 ));
        cssStandert.put("lime"      , new Color( 0 , 255,  0 ));
        cssStandert.put("olive"     , new Color(128, 128,  0 ));
        cssStandert.put("yellow"    , new Color(255, 255,  0 ));
        cssStandert.put("navy"      , new Color( 0 ,  0 , 128));
        cssStandert.put("blue"      , new Color( 0 ,  0 , 255));
        cssStandert.put("teal"      , new Color( 0 , 128, 128));
        cssStandert.put("aqua"      , new Color( 0 , 255, 255));
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
            val = val.replace(key + ")", "<span class=common-key>" + key + "</span>)");
            val = val.replace(key + ";", "<span class=common-key>" + key + "</span>;");
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
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentShown(ComponentEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        setStatusBar();
        if (pairChar.containsKey(e.getKeyChar())) {
            int cpos = textarea.getCaretPosition();
            textarea.setText(textarea.getText().substring(0,cpos) + pairChar.get(e.getKeyChar()) + textarea.getText().substring(cpos));
            textarea.setCaretPosition(cpos);
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            int cpos = textarea.getCaretPosition();
            int indent = countCurlineBlank();
            String indentStr = "";
            if (indent != 0) {
                if(textarea.getText().substring(cpos-2,cpos-1).equals("{")) {
                    indent = indent + indentSize;
                    for (int i = 0; i < indent; i++){
                        indentStr = indentStr + " ";
                    }
                    indentStr = indentStr + "\n" + indentStr.substring(2);
                }else {
                    for (int i = 0; i < indent; i++){
                        indentStr = indentStr + " ";
                    }
                }
                textarea.setText(textarea.getText().substring(0,cpos) + indentStr + textarea.getText().substring(cpos));
                textarea.setCaretPosition(cpos+indent);
            }
            panel.setPreferredSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
            label.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
            textarea.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
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
        label.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
        textarea.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
    }
    @Override
    public void changedUpdate(DocumentEvent e) {}
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
                String path = showSaveAsDialog();
                if (path != null){
                    File f = new File( path );
                    FileWriter fw;
                    fw = new FileWriter(f);
                    fw.write("<head><style>pre{margin-top: 0;}.decimal{color: teal}.type-key{color: green;}.common-key{color: blue;}.java-key{color: purple;}.proc-control-key{color: purple;}.string{color: orange;}.class{color: green;}</style></head>" + label.getText());
                    fw.close();
                    showDialog("htmlファイルを保存しました");
                }
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
        }else if (e.getActionCommand().equals( newFlieItem.getText() )){
            saveAs();
        }else if (e.getActionCommand().equals( overWriteItem.getText() )){
            save();
        }else if (e.getActionCommand().equals( openItem.getText() )) {
            String txt;
            if ( ( txt = fileOpen()) != null ){
                textarea.setText(txt);
            }
        }
    }
}
