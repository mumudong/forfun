package jframe;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ImageTextPaneController {


    public static void add2(JTextPane inputJTpane,
                            JTextPane outputJTpane,
                            JButton insertButton,
                            JButton senon,
                            JFrame jf){

        insertButton.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = chooser.showOpenDialog(jf);
            if (result == JFileChooser.APPROVE_OPTION) {
                inputJTpane.insertIcon(new ImageIcon(chooser.getSelectedFile().toString()));
            }
        });



        senon.addActionListener((ActionEvent e) -> {
            StyledDocument inputSDoc = inputJTpane.getStyledDocument(); //获取读取的StyledDocument
            StyledDocument outSDoc = outputJTpane.getStyledDocument(); //获取欲输出的StyledDocument
            for (int i = 0; i < inputSDoc.getLength(); i++) { //遍历读取的StyledDocument
                if (inputSDoc.getCharacterElement(i).getName().equals("icon")) { //如果发现是icon元素，那么：
                    Element ele = inputSDoc.getCharacterElement(i);
                    ImageIcon icon = (ImageIcon) StyleConstants.getIcon(ele.getAttributes());
                    outputJTpane.insertIcon(new ImageIcon(icon.toString()));//插入icon元素
                } else {//如果不是icon（可以判断是文字，因为目前只有图片元素插入）
                    try {
                        String s = inputSDoc.getText(i, 1);
                        outSDoc.insertString(outputJTpane.getCaretPosition(), s, null);//从光标处插入文字
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        //===========End of senon

    }
}
