import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends JFrame implements ComponentListener, DocumentListener, ActionListener {
    JPanel panel;
    JTextArea textarea;
    JLabel label;
    JScrollPane sc;
    Document doc;
    Matcher matcher;

    final Pattern  STRING_PTN = Pattern.compile("&quot;(.*?)&quot;");
    final Pattern  NUMBER_PTN = Pattern.compile("-?(0|[1-9]\\d*)(\\.\\d+|)");
    final Pattern  CLASS_PTN  = Pattern.compile(" [A-Z][a-zA-z]++");
    final String[] PROC_CONTROL_KEYWORD = {"break","continue","do","else","for","if","return","while","default"};
    final String[] TYPE_KEYWORD         = {"auto","case","char","const","double","enum","extern","float","goto","inline","int","long","register","restrict","short","signed","sizeof","struct","switch","typedef","union","unsigned","void","volatile"};
    final String[] COMMON_KEYWORD       = {"class","delete","false","import","operator","private","protected","public","this","throw","true","try","typeof","static"};
    final String[] JAVA_KEYWORD         = {"abstract","assert","boolean","byte","extends","finally","final","implements","import","instanceof","interface","null","native", "package","strictfp","super", "synchronized","throws","transient","new"};

    App (String title) {
        setTitle(title);
        setLocationRelativeTo(null);
        setSize(500, 500);
        addComponentListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menubar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
            JMenuItem exportItem = new JMenuItem("export to html");
            exportItem.addActionListener(this);
            fileMenu.add(exportItem);

        JMenu editMenu = new JMenu("Edit");

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
        textarea.setBounds(0, 0, getWidth(), getHeight());

        doc = textarea.getDocument();
        doc.addDocumentListener(this);

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

        
        // CSS の 設定
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("pre{margin-top: 0;}");                 // pre タグの表示位置調整
        css.addRule(".decimal{color: teal}");               // 実数
        css.addRule(".type-key{color: green;}");            // プログラミング言語全般のキーワード
        css.addRule(".common-key{color: blue;}");           // javaのキーワード
        css.addRule(".java-key{color: purple;}");           // プリミティブ型 + String型
        css.addRule(".proc-control-key{color: purple;}");   // 制御文キーワード
        css.addRule(".string{color: orange;}");             // ダブルコーテーションで囲まれたもの
        css.addRule(".class{color: green;}");
    }
    public static void main(String[] args) throws Exception {
        App frame = new App("TAKO-Editor");
        frame.setVisible(true);
    }

    private String sanitaizeEnc (String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("\n", "<br>");
    }

    private String highLight(String val){
        val = sanitaizeEnc(val);

        matcher = NUMBER_PTN.matcher(val);
        for (int i = 0; i < matcher.results().count(); i++){
            val = val.replaceAll("-?(0|[1-9]\\d*)(\\.\\d+|)",  "<span class=decimal>" + ("$" + i) + "</span>" );
        }

        for (String key: COMMON_KEYWORD){
            val = val.replace(key + " ", "<span class=common-key>" + key + "</span> ");
        }
        for (String key: JAVA_KEYWORD){
            val = val.replace(key + " ", "<span class=java-key>" + key + "</span> ");
        }
        for (String key: TYPE_KEYWORD) {
            val = val.replace(key + " ", "<span class=type-key>" + key + "</span> ");
        }
        for (String key: PROC_CONTROL_KEYWORD) {
            val = val.replace(key + " ", "<span class=proc-control-key>" + key + "</span> ");
        }

        matcher = STRING_PTN.matcher(val);
        for (int i = 0; i < matcher.results().count(); i++){
            val = val.replaceAll("&quot;(.*?)&quot;",  "<span class=string>" + ("$" + i) + "</span>" );
        }

        matcher = CLASS_PTN.matcher(val);
        for (int i = 0; i < matcher.results().count(); i++){
            val = val.replaceAll(" [A-Z][a-zA-Z]++", "<span class=class>" + ("$" + i) + "</span>");
        }

        return val;
    }
    // JFrame イベント
    @Override
    public void componentResized(ComponentEvent e) {
        // TODO Auto-generated method stub
        sc.setPreferredSize(new Dimension(getWidth(), getHeight()));
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    // JTextArea イベント
    @Override
    public void insertUpdate(DocumentEvent e) {
        // TODO Auto-generated method stub
        String val = highLight(textarea.getText());
        label.setText("<html><pre>" + val + "</pre></html>");

        panel.setPreferredSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
        label.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
        textarea.setSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
    }
    @Override
    public void removeUpdate(DocumentEvent e) {
        // TODO Auto-generated method stub
        String val = highLight(textarea.getText());
        label.setText("<html><pre>" + val + "</pre></html>");

        panel.setPreferredSize(new Dimension(textarea.getPreferredSize().width, textarea.getPreferredSize().height));
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
        // TODO Auto-generated method stub
    }

    // ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if (e.getActionCommand().equals("export to html")){
            try {
                File f = new File("output.html");
                FileWriter fw;
                fw = new FileWriter(f);
                fw.write("<head><style>pre{margin-top: 0;}.decimal{color: teal}.type-key{color: green;}.common-key{color: blue;}.java-key{color: purple;}.proc-control-key{color: purple;}.string{color: orange;}.class{color: green;}</style></head>" + label.getText());
                fw.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}

