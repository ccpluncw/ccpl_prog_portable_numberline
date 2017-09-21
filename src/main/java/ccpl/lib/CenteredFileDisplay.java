package ccpl.lib;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.JPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;


/**
 *This class allows a paragraph to be displayed (full screen) 
 * centered.
 * 
 * Used by most experiments as a way to display instructions at the 
 * beginning of the experiment.
 * 
 * The label of the panel uses html code to format the text.
 * @author programmer
 */
public class CenteredFileDisplay extends JPanel implements ActionListener{
    private Specification[] instructions;
    private boolean useV2 = false;
    
    public CenteredFileDisplay(){
        super();
        setBackground(Color.WHITE);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        setSize(d.width, d.height);
        setLocation(0,0);
        
        SpecificationArrayProcess sap = new SpecificationArrayProcess();
        //instructions = sap.readFromURL()
    }
    
    /**
     * Preferred constructor
     * @param fileName
     *  the file with the text to be displayed
     * @param x
     *  the experiment object (this is needed to read the URL and find the infiles directory
     * @param j 
     *  this should be 'left', 'center' or 'right'
     *  controls the alignment of the text
     *  this parameter is supplied verbatim to the HTML code
     */
    public CenteredFileDisplay(String fileName, Experiment x, String j){
        super();
        setBackground(Color.WHITE);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        setSize(d.width, d.height);
        setLocation(0,0);
        
        SpecificationArrayProcess sap = new SpecificationArrayProcess();
        instructions = sap.readFromURL(x.getURL(x.INFILES_PATH + fileName));
        
        //html code
        //so style='width:' controls the width of the paragaph on the screen
        //this is in pixels
        //it is set to 1000 right now 
        //'align=' controls alignment
        //alignment is controlled by parameter j in the constructor
        String html1, html2;
        html1 = "<html><body style='width:";
        html2 = "px'><p align=" + j + ">";
        
        String inst = html1 + 1000 + html2;
        for(int i = 0; i < instructions.length; i ++)
            inst += instructions[i].getAllSpecs();
        JLabel l = new JLabel();
        l.setForeground(Color.BLACK);
        l.setText(inst);
        l.setVisible(true);
        
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        
        gb.setConstraints(l, c);
        add(l);
        
        JButton okbutton = new JButton("OK");
        okbutton.addActionListener(this);
        
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        
        gb.setConstraints(okbutton, c);
        

        add(okbutton);
        
        x.frame.setContentPane(this);
        x.frame.getRootPane().setDefaultButton(okbutton);
        x.frame.setVisible(true);
        try {
          synchronized(this){
              wait();
          }
        }
        catch (InterruptedException ex) {
          Logger.getLogger(CenteredFileDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    public CenteredFileDisplay(String fileName, ExperimentV2 x, String j){
        super();
        useV2 = true;
        setBackground(Color.WHITE);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        setSize(d.width, d.height);
        setLocation(0,0);
        
        SpecificationArrayProcess sap = new SpecificationArrayProcess();
        instructions = sap.readFromURL(x.getURL(x.INFILES_PATH + fileName));
        
        //html code
        //so style='width:' controls the width of the paragaph on the screen
        //this is in pixels
        //it is set to 1000 right now 
        //'align=' controls alignment
        //alignment is controlled by parameter j in the constructor
        String html1, html2;
        html1 = "<html><body style='width:";
        html2 = "px'><p align=" + j + ">";
        
        String inst = html1 + 1000 + html2;
        for(int i = 0; i < instructions.length; i ++)
            inst += instructions[i].getAllSpecs();
        JLabel l = new JLabel();
        l.setForeground(Color.BLACK);
        l.setText(inst);
        l.setVisible(true);
        
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        
        gb.setConstraints(l, c);
        add(l);
        
        JButton okbutton = new JButton("OK");
        okbutton.addActionListener(this);
        
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        
        gb.setConstraints(okbutton, c);
        

        add(okbutton);
        
        
        
        x.frame.setContentPane(this);
        x.frame.getRootPane().setDefaultButton(okbutton);
        x.frame.setVisible(true);
        try {
          synchronized(this){
              wait();
          }
          
        }
        catch (InterruptedException ex) {
          Logger.getLogger(CenteredFileDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }  
        useV2 = true;
    }
    
    public void actionPerformed(ActionEvent e) {
        if(useV2) {
            ExperimentV2.presentBlankScreen(0);
        } else {
            Experiment.presentBlankScreen(0);
        }
        synchronized(this){
                notify();
    }
}
}
