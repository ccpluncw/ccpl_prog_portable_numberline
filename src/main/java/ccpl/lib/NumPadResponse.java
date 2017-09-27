package ccpl.lib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class NumPadResponse {
    private static final String APP_IMG_DIR = "images/";

    private VirtualKeyboard keyboard;
    private ResponseFields fields;
    private JPanel estTaskRespPanel;
    private JDialog parentDialog;
    
    public NumPadResponse(String fieldFormat, JDialog parent){
        estTaskRespPanel = new JPanel(new BorderLayout());

        keyboard = new VirtualKeyboard();
        fields = new ResponseFields(fieldFormat);
        createResponsePanel();
        parentDialog = parent;
    }

    private void createResponsePanel(){
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(fields.responsePanel);
        
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(keyboard.keyboardPanel);
        
        estTaskRespPanel.add(northPanel, BorderLayout.NORTH);
        estTaskRespPanel.add(centerPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel(){
        estTaskRespPanel.setSize(estTaskRespPanel.getPreferredSize());
        return estTaskRespPanel;
    }

    public String getResponse(){
        return fields.getFieldResponse();
    }

    public boolean validateResponse(String response){
        return fields.isValidResponse(response);
    }
    
    public void setActiveField(String text)
    {
        fields.setActiveFieldIdx(text);
    }
    
    private class VirtualKeyboard{
        private JPanel keyboardPanel;
        private JButton[] buttons;
        private ActionListener keyBoardHandler;
        
        public VirtualKeyboard(){
            buttons = new JButton[12];
            keyboardPanel = new JPanel();
            keyboardPanel.setLayout(new GridLayout(4,3));
            createKeyboardButtons();
        }

        private void createKeyboardButtons(){
            for(int i=0;i < 10;++i){
                buttons[i] = new JButton(Integer.toString(i));
                buttons[i].setActionCommand(Integer.toString(i));
            }

            // TODO: Fix this
            ClassLoader cl = this.getClass().getClassLoader();
            URL leftArrow = cl.getResource(APP_IMG_DIR + "left_arrow.gif");
            URL rightArrow = cl.getResource(APP_IMG_DIR + "images/right_arrow.gif");

            buttons[10] = new JButton(new ImageIcon(leftArrow));
            buttons[10].setToolTipText("Previous Field");
            buttons[10].setActionCommand("P");
            buttons[11] = new JButton(new ImageIcon(rightArrow));
            buttons[11].setToolTipText("Next Field");
            buttons[11].setActionCommand("N");

            setKeyboardHandler();
            
            for(int i=1;i<10;++i){
                keyboardPanel.add(buttons[i]);
                buttons[i].addActionListener(keyBoardHandler);
            }
            keyboardPanel.add(buttons[0]);
            keyboardPanel.add(buttons[10]);
            keyboardPanel.add(buttons[11]);

            buttons[0].addActionListener(keyBoardHandler);
            buttons[10].addActionListener(keyBoardHandler);
            buttons[11].addActionListener(keyBoardHandler);
        }

        private void setKeyboardHandler(){
             keyBoardHandler = new ActionListener (){
                public void actionPerformed (ActionEvent ae){
                    String command = ae.getActionCommand();
                    if (command.equals("N")) {
                        fields.setNextFieldAsActive();
                    } else if (command.equals("P")) {
                        fields.setPrevFieldAsActive();
                    } else {
                        fields.setActiveFieldIdx(command);
                        fields.setNextFieldAsActive();
			keyboardPanel.requestFocus();
                    }
                }
            };
        }

    }

    private class ResponseFields {

        private JPanel responsePanel;
        private ArrayList<JTextField> responseFields;
        private FieldFormat fieldFormat;
        private int activeFieldIdx;
        private final Color DEFAULT_FIELD_COLOR;
        private String responseStr;

        public ResponseFields(String format){
            responsePanel = new JPanel();
            //responsePanel.setBackground(Color.BLACK);
            responseFields = new ArrayList<JTextField>();
            activeFieldIdx = 0;
            fieldFormat = new FieldFormat(format);
            createPanel();
            DEFAULT_FIELD_COLOR = responsePanel.getBackground();
        }

        public void setActiveFieldIdx(String respText){
            JTextField activeField = responseFields.get(activeFieldIdx);
            activeField.setText(respText);
        }

        private void createFields(int numFields){
               for(int i=0;i<numFields;++i){
                   JTextField field = new JTextField(1);
                   field.setEditable(false);
                   field.setText("0");
                   field.setHorizontalAlignment(JTextField.CENTER);
                   responseFields.add(field);
               }
               setActiveFieldFocus();
        }

        private void createPanel(){
            switch(fieldFormat.getType()){
                case DECI:
                    createDeciPanel();
                    break;
                case FRACT:
                    createFractPanel();
                    break;
                case ODD:
                    createOddsPanel();
                    break;
                default:
                    createIntPanel();
            }
        }

        private void createIntPanel(){
             responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
             JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
             int numFields = fieldFormat.getNumLeftVals();
             createFields(numFields);
             for(int i=0;i<numFields;++i)
                fieldPanel.add(responseFields.get(i));
             responsePanel.add(fieldPanel);
        }

        private void createDeciPanel(){
            responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            JPanel leftIdxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            JPanel rightIdxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

            JLabel label = new JLabel(".");
            int numLeftFields = fieldFormat.getNumLeftVals();
            createFields(numLeftFields);
            for(int i=0;i<numLeftFields;++i)
                leftIdxPanel.add(responseFields.get(i));

            int numRightFields = fieldFormat.getNumRightVals();
            createFields(numRightFields);
            int totalFields = numLeftFields+numRightFields;
            for(int i=numLeftFields;i<totalFields;++i)
                rightIdxPanel.add(responseFields.get(i));

            responsePanel.add(leftIdxPanel);
            responsePanel.add(label);
            responsePanel.add(rightIdxPanel);
        }

        private void createFractPanel(){
            responsePanel.setLayout(new GridLayout(3, 1));
            JPanel leftIdxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            JPanel rightIdxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            JPanel fractLinePanel = new FractLinePanel();

            int numLeftFields = fieldFormat.getNumLeftVals();
            createFields(numLeftFields);
            for(int i=0;i<numLeftFields;++i)
                leftIdxPanel.add(responseFields.get(i));

            int numRightFields = fieldFormat.getNumRightVals();
            createFields(numRightFields);
            int totalFields = numLeftFields+numRightFields;
            for(int i=numLeftFields;i<totalFields;++i)
                rightIdxPanel.add(responseFields.get(i));

            responsePanel.add(leftIdxPanel);
            responsePanel.add(fractLinePanel);
            responsePanel.add(rightIdxPanel);
        }

        private void createOddsPanel(){
            responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            JPanel leftIdxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            JPanel rightIdxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

            JLabel label = new JLabel(" in ");
            int numLeftFields = fieldFormat.getNumLeftVals();
            createFields(numLeftFields);
            for(int i=0;i<numLeftFields;++i)
                leftIdxPanel.add(responseFields.get(i));

            int numRightFields = fieldFormat.getNumRightVals();
            createFields(numRightFields);
            int totalFields = numLeftFields+numRightFields;
            for(int i=numLeftFields;i<totalFields;++i)
                rightIdxPanel.add(responseFields.get(i));

            responsePanel.add(leftIdxPanel);
            responsePanel.add(label);
            responsePanel.add(rightIdxPanel);
        }

        public int getFieldCount(){
            return responseFields.size();
        }

        public String getFieldResponse(){
            StringBuilder response = new StringBuilder();
            int numLeftVals = fieldFormat.getNumLeftVals();
            int numRightVals = fieldFormat.getNumRightVals();

            for(int i=0;i<numLeftVals;++i){
                response.append(responseFields.get(i).getText());
            }
            switch(fieldFormat.getType()){
                case DECI:
                    response.append(".");
                    break;
                case FRACT:
                    response.append("/");
                    break;
                case ODD:
                    response.append(" in ");
            }
            if(fieldFormat.getType() != FIELDTYPE.INT){
                int totalVals = numLeftVals+numRightVals;
                for(int i=numLeftVals;i<totalVals;++i){
                    response.append(responseFields.get(i).getText());
                }
            }
            responseStr = response.toString();
            return responseStr;
        }
        
        public boolean isValidResponse(String response){
            boolean isGreaterZero = true;
            boolean isValidFraction = true;
            String fract[];
             switch(fieldFormat.getType()){
                case DECI:
                    if(Double.parseDouble(response) <= 0.0)
                        isGreaterZero = false;
                    break;
                case FRACT:
                    fract = response.split("/");
                    try{
                         Fraction f = new Fraction(Integer.parseInt(fract[0]), Integer.parseInt(fract[1]));
                         if(f.toDouble() <= 0.0)
                             isGreaterZero = false;
                    }catch(IllegalArgumentException e){ 
                        isValidFraction = false;
                    }
                    break;
                case ODD:
                    fract = response.split("in");
                    try{
                         Fraction f = new Fraction(Integer.parseInt(fract[0].trim()), Integer.parseInt(fract[1].trim()));
                         if(f.toDouble() <= 0.0)
                             isGreaterZero = false;
                    }catch(IllegalArgumentException e){
                        isValidFraction = false;
                    }
                    break;
                default:
                    if(Integer.parseInt(response) <= 0)
                        isGreaterZero = false;
            }
             if(!isGreaterZero)
                JOptionPane.showMessageDialog(parentDialog, "Your response must be greater than zero.");
             else if(!isValidFraction)
                 JOptionPane.showMessageDialog(parentDialog, "The second field of the response must be greater than zero.");
            return (isGreaterZero && isValidFraction);
        }


        private void setNextFieldAsActive() {
            removeActiveFieldFocus();
            if(activeFieldIdx >= (getFieldCount()-1))
                activeFieldIdx = 0;
            else
                ++activeFieldIdx;
            setActiveFieldFocus();
        }

        private void setPrevFieldAsActive() {
            removeActiveFieldFocus();
            if(activeFieldIdx <= 0)
                activeFieldIdx = getFieldCount()-1;
            else
                --activeFieldIdx;
            setActiveFieldFocus();
        }

        private void setActiveFieldFocus(){
            JTextField focusField = responseFields.get(activeFieldIdx);
            focusField.setBackground(new Color(135, 206, 250));
            focusField.revalidate();
        }
        
        private void removeActiveFieldFocus(){
            JTextField focusField = responseFields.get(activeFieldIdx);
            focusField.setBackground(DEFAULT_FIELD_COLOR);
            focusField.revalidate();
        }

        private class FractLinePanel extends JPanel{
            public FractLinePanel(){
                super(null);
            }
            @Override
            public void paintComponent(Graphics g){
                int hPos = getHeight()/2;
                g.drawLine(0, hPos, getWidth(), hPos);
            }
        }
        
    }

    private enum FIELDTYPE {INT, DECI, FRACT, ODD}
    private static FIELDTYPE findType(String formatStr){
            FIELDTYPE TYPE = FIELDTYPE.INT;
            if(formatStr.contains("."))
                TYPE = FIELDTYPE.DECI;
            else if(formatStr.contains("/"))
                TYPE = FIELDTYPE.FRACT;
            else if(formatStr.contains("in"))
                TYPE = FIELDTYPE.ODD;
            return TYPE;
   }
    
    private class FieldFormat{
        private final String format;
        private final FIELDTYPE TYPE;
        
        public FieldFormat(String fieldFormat){
            if(isValid(fieldFormat)){
                format = fieldFormat;
                TYPE = findType(fieldFormat);
            } else{
                format = null;
                TYPE = null;
                System.err.println("Invalid target format" + fieldFormat);
                System.exit(1);
            }
        }

        public int getNumLeftVals(){
            int left = 1;
            switch(TYPE){
                case DECI:
                    left = Integer.parseInt((format.substring(0, format.indexOf(".")).trim()));
                    break;
                case FRACT:
                    left = Integer.parseInt((format.substring(0, format.indexOf("/")).trim()));
                    break;
                case ODD:
                    left = Integer.parseInt((format.substring(0, format.indexOf("in")).trim()));
                    break;
                default:
                    left = Integer.parseInt(format.trim());
            }
            return left;
        }

        public int getNumRightVals(){
            int right = 1;
            int lastIdx = format.length();
            switch(TYPE){
                case DECI:
                    right = Integer.parseInt((format.substring(format.indexOf(".")+1, lastIdx).trim()));
                    break;
                case FRACT:
                    right = Integer.parseInt((format.substring(format.indexOf("/")+1, lastIdx).trim()));
                    break;
                case ODD:
                    right = Integer.parseInt((format.substring(format.indexOf("in")+2,lastIdx).trim()));
                    break;
                default:
                    right = Integer.parseInt(format.trim());
            }
            return right;
        }

        public FIELDTYPE getType(){
            return TYPE;
        }
        
        private boolean isValid(String aFormat){
            String regex = "^(\\d)*(\\s)*((.|/|(in))(\\s)*(\\d)+)?$";
            boolean isValid = false;
            if(aFormat.matches(regex))
                isValid = true;
            return isValid;
        }

    }

}
