/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2012 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yajhfc.pdf.report.ui;

import static yajhfc.options.OptionsWin.border;
import static yajhfc.pdf.i18n.Msgs._;
import info.clearthought.layout.TableLayout;

import java.awt.GridLayout;
import java.awt.print.PageFormat;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.pdf.EntryPoint;
import yajhfc.pdf.PDFOptions;
import yajhfc.pdf.report.PdfDocWriter;
import yajhfc.util.IntVerifier;

/**
 * @author jonas
 *
 */
public class PdfDocWriterPanel extends JPanel {

    JTextField textMarginLeft, textMarginRight, textMarginTop, textMarginBottom;
    JRadioButton radPortrait, radLandscape;
    ButtonGroup groupOrientation;
    JComboBox comboPaperSize;

    public PdfDocWriterPanel() {
        super(false);
        initialize();
    }

    private void initialize() {
        textMarginLeft = new JTextField(4);
        textMarginLeft.setInputVerifier(new IntVerifier(0, 9999));
        
        textMarginRight = new JTextField(4);
        textMarginRight.setInputVerifier(new IntVerifier(0, 9999));
        
        textMarginTop = new JTextField(4);
        textMarginTop.setInputVerifier(new IntVerifier(0, 9999));
        
        textMarginBottom = new JTextField(4);
        textMarginBottom.setInputVerifier(new IntVerifier(0, 9999));
        
        radPortrait = new JRadioButton(_("Portrait"));
        radLandscape = new JRadioButton(_("Landscape"));
        
        groupOrientation = new ButtonGroup();
        groupOrientation.add(radPortrait);
        groupOrientation.add(radLandscape);
        
        comboPaperSize = new JComboBox(PaperSize.values());
        
        double[][] dLay = {
                {border, TableLayout.PREFERRED, border/2, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        JPanel panelMargins = new JPanel(new TableLayout(dLay), false);
        panelMargins.setBorder(BorderFactory.createTitledBorder(_("Page Margins")));
        Utils.addWithLabelHorz(panelMargins, textMarginLeft, _("Left margin (mm):"), "3,1,f,c");
        Utils.addWithLabelHorz(panelMargins, textMarginRight, _("Right margin (mm):"), "3,3,f,c");
        Utils.addWithLabelHorz(panelMargins, textMarginTop, _("Top margin (mm):"), "3,5,f,c");
        Utils.addWithLabelHorz(panelMargins, textMarginBottom, _("Bottom margin (mm):"), "3,7,f,c");
        
        dLay = new double[][] {
                {border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, border*2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        JPanel panelPaperSize = new JPanel(new TableLayout(dLay), false);
        panelPaperSize.setBorder(BorderFactory.createTitledBorder(_("Paper size")));
        panelPaperSize.add(comboPaperSize, "1,1,f,c");
        panelPaperSize.add(radPortrait, "1,3,l,c");
        panelPaperSize.add(radLandscape, "1,5,l,c");
        
        setLayout(new GridLayout(1, 2));
        add(panelMargins);
        add(panelPaperSize);
    }
    
    private boolean validateMargin(JTextField textMargin) {
        String sMargin = textMargin.getText();
        try {
            int iMargin = Integer.valueOf(sMargin);
            if (iMargin < 0 || iMargin > 9999) {
                textMargin.requestFocusInWindow();
                JOptionPane.showMessageDialog(this,
                        _("The margin must be between 0 and 9999"));
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            textMargin.requestFocusInWindow();
            JOptionPane.showMessageDialog(this,
                    _("The margin must be between 0 and 9999"));
            return false;
        }
    }
    
    public boolean validateInput() {
        return  validateMargin(textMarginLeft)  && 
                validateMargin(textMarginRight) &&
                validateMargin(textMarginTop)   &&
                validateMargin(textMarginBottom);
    }
    
    public void loadDefaults() {
        PDFOptions opts = EntryPoint.getOptions();
        textMarginBottom.setText(String.valueOf(opts.marginBottom));
        textMarginLeft.setText(String.valueOf(opts.marginLeft));
        textMarginRight.setText(String.valueOf(opts.marginRight));
        textMarginTop.setText(String.valueOf(opts.marginTop));
        
        comboPaperSize.setSelectedItem(opts.paperSize);
        
        switch (opts.orientation) {
        case PageFormat.PORTRAIT:
        default:
            radPortrait.setSelected(true);
            break;
        case PageFormat.LANDSCAPE:
        case PageFormat.REVERSE_LANDSCAPE:
            radLandscape.setSelected(true);
            break;
        }
    }

    
    public void writeToAndSaveDefaults(PdfDocWriter pdw) {
        int marginBottom = Integer.valueOf(textMarginBottom.getText());
        int marginLeft = Integer.valueOf(textMarginLeft.getText());
        int marginRight = Integer.valueOf(textMarginRight.getText());
        int marginTop = Integer.valueOf(textMarginTop.getText());
        PaperSize paperSize = (PaperSize)comboPaperSize.getSelectedItem();
        int orientation = radPortrait.isSelected() ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
        
        pdw.setMarginBottomMM(marginBottom);
        pdw.setMarginLeftMM(marginLeft);
        pdw.setMarginRightMM(marginRight);
        pdw.setMarginTopMM(marginTop);
        pdw.setPaperSize(paperSize);
        pdw.setOrientation(orientation);        
        
        PDFOptions opts = EntryPoint.getOptions();
        opts.marginBottom = marginBottom;
        opts.marginLeft = marginLeft;
        opts.marginRight = marginRight;
        opts.marginTop = marginTop;
        opts.paperSize = paperSize;
        opts.orientation = orientation;
    }
}
