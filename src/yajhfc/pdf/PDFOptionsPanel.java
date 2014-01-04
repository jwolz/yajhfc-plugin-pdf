/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2011 Jonas Wolz
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
package yajhfc.pdf;

import static yajhfc.pdf.i18n.Msgs._;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.file.FileConverters;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;
import yajhfc.options.PathAndViewPanel;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ComponentEnabler;
import yajhfc.util.ExampleFileFilter;

import com.itextpdf.text.Version;

/**
 * 
 * @author jonas
 *
 */
public class PDFOptionsPanel extends AbstractOptionsPanel<FaxOptions> {   
    private static final Logger log = Logger.getLogger(PDFOptionsPanel.class.getName());

    JCheckBox checkUseForTIFF, checkUseForPNG, checkUseForJPEG, checkUseForGIF, checkEnableNativeLibTIFF;
    
    JCheckBox checkFitToPage, checkAssumePortrait, checkChopLongPage;
    JTextField textChopThreshold, textChopFactor;
    JCheckBox checkUseSubstFont;
    FileTextField ftfSubstFont;
    
    NumberFormat floatFormat = NumberFormat.getNumberInstance();
    
    public PDFOptionsPanel() {
        super(false);
        PathAndViewPanel.requireTIFF2PDF = !EntryPoint.getOptions().useITextForTIFF;
    }
    
    @Override
    protected void createOptionsUI() {
        MessageFormat convFormat = new MessageFormat(_("Use iText to convert {0}→PDF"));
        checkUseForTIFF = new JCheckBox(convFormat.format(new Object[] {"TIFF"}));
        checkUseForTIFF.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                PathAndViewPanel.requireTIFF2PDF = !checkUseForTIFF.isSelected();
            }
        });
        checkUseForPNG = new JCheckBox(convFormat.format(new Object[] {"PNG"}));
        checkUseForGIF = new JCheckBox(convFormat.format(new Object[] {"GIF"}));
        checkUseForJPEG = new JCheckBox(convFormat.format(new Object[] {"JPEG"}));
        checkEnableNativeLibTIFF = new JCheckBox(_("Use libtiff to read TIFF files"));
        checkEnableNativeLibTIFF.setEnabled(EntryPoint.haveNativeLibTIFF);
        
        checkFitToPage = new JCheckBox(_("Fit TIFF to page size"));
        checkFitToPage.setSelected(true);
        final ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean enable = checkFitToPage.isSelected();
                boolean enableChop = enable && checkChopLongPage.isSelected();
                
                checkAssumePortrait.setEnabled(enable);
                checkChopLongPage.setEnabled(enable);
                textChopFactor.setEnabled(enableChop);
                textChopThreshold.setEnabled(enableChop);
            }
        };
        checkFitToPage.addItemListener(itemListener);
        checkAssumePortrait = new JCheckBox(_("Always use portrait orientation"));
        checkChopLongPage = new JCheckBox(_("Chop long TIFFs into several pages"));
        checkChopLongPage.setSelected(true);
        checkChopLongPage.addItemListener(itemListener);
        
        textChopFactor = new JTextField();
        textChopFactor.setToolTipText(_("The TIFF will be chopped into chunks [width] * [this value] heigh"));
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textChopFactor);
        textChopThreshold = new JTextField();
        textChopThreshold.setToolTipText(_("Chop TIFF into multiple pages if [height] > [width] * [this value]"));
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textChopThreshold);
        
        double[][] dLay = {
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border}
        };
    	setLayout(new TableLayout(dLay));
    	
    	JPanel checkBoxPanel = new JPanel();
    	checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
    	checkBoxPanel.setBorder(BorderFactory.createCompoundBorder(
    	        BorderFactory.createTitledBorder(_("File converters")),
    	        BorderFactory.createEmptyBorder(OptionsWin.border, OptionsWin.border, OptionsWin.border, OptionsWin.border)));
    	
    	Dimension spacer = new Dimension(OptionsWin.border, OptionsWin.border);
    	checkBoxPanel.add(checkUseForGIF);
    	checkBoxPanel.add(Box.createRigidArea(spacer));
        checkBoxPanel.add(checkUseForJPEG);
        checkBoxPanel.add(Box.createRigidArea(spacer));
        checkBoxPanel.add(checkUseForPNG);
        checkBoxPanel.add(Box.createRigidArea(spacer));
        checkBoxPanel.add(checkUseForTIFF);
        checkBoxPanel.add(Box.createRigidArea(spacer));
        checkBoxPanel.add(checkEnableNativeLibTIFF);
        
        JPanel tiffPanel = new JPanel();
        tiffPanel.setLayout(new BoxLayout(tiffPanel, BoxLayout.Y_AXIS));
        tiffPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(_("TIFF→PDF options")),
                BorderFactory.createEmptyBorder(OptionsWin.border, OptionsWin.border, OptionsWin.border, OptionsWin.border)));
        
        Dimension doubleSpace = new Dimension(OptionsWin.border*2, OptionsWin.border*2);
        Dimension halfSpace = new Dimension(OptionsWin.border/2, OptionsWin.border/2);
        tiffPanel.add(checkFitToPage);
        tiffPanel.add(Box.createRigidArea(spacer));
        tiffPanel.add(checkAssumePortrait);
        tiffPanel.add(Box.createRigidArea(spacer));
        tiffPanel.add(checkChopLongPage);
        tiffPanel.add(Box.createRigidArea(doubleSpace));
        tiffPanel.add(new JLabel(_("Long page threshold factor:")));
        tiffPanel.add(Box.createRigidArea(halfSpace));
        tiffPanel.add(textChopThreshold);
        tiffPanel.add(Box.createRigidArea(doubleSpace));
        tiffPanel.add(new JLabel(_("Height factor for chopped pages:")));
        tiffPanel.add(Box.createRigidArea(halfSpace));
        tiffPanel.add(textChopFactor);

        checkUseSubstFont = new JCheckBox(_("Use the following substitution font for fax cover fields"));
        ftfSubstFont = new FileTextField();
        ftfSubstFont.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        ftfSubstFont.setFileFilters(new ExampleFileFilter("ttf", _("TrueType fonts")));
        ftfSubstFont.setEnabled(false);
        
        ComponentEnabler.installOn(checkUseSubstFont, true, ftfSubstFont);
        
        add(checkBoxPanel, "1,1,1,1,f,t");
        add(tiffPanel, "3,1,3,1,f,t");
        add(checkUseSubstFont, "1,3,3,3,l,c");
        add(ftfSubstFont, "1,4,3,4,f,c");
        add(new JLabel(_("iText version used:")), "1,6,1,6,l,t");
        add(new JLabel(getITextVersion()), "1,7,3,7,l,t");
        add(new JLabel(_("libtiff version used:")), "1,9,1,9,l,t");
        String nativeTIFFVersion;
        if (EntryPoint.nativeTIFFVersion != null) {
            try {
                nativeTIFFVersion = EntryPoint.nativeTIFFVersion.get();
            } catch (Exception e1) {
                log.log(Level.WARNING, "Error getting TIFF Version", e1);
                nativeTIFFVersion = e1.toString();
            }
        } else {
            nativeTIFFVersion = "libtiff plugin not installed";
        }
        add(new JLabel("<html>" + nativeTIFFVersion.replace("\n", "<br>") + "</html>"), "1,10,3,10,l,t");
    }

    private String getITextVersion() {
        try {
            return Version.getInstance().getVersion();
        } catch (NoClassDefFoundError e) {
            log.log(Level.WARNING, "Error getting iText Version", e);
            return "ERROR: iText not found";
        } catch (Throwable e) {
            log.log(Level.WARNING, "Error getting iText Version", e);
            return e.getClass().getName() + " initializing iText";
        }
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        PDFOptions pdfOpt = EntryPoint.getOptions();
        
        checkUseForGIF.setSelected(pdfOpt.useITextForGIF);
        checkUseForPNG.setSelected(pdfOpt.useITextForPNG);
        checkUseForJPEG.setSelected(pdfOpt.useITextForJPEG);
        checkUseForTIFF.setSelected(pdfOpt.useITextForTIFF);
        checkUseSubstFont.setSelected(pdfOpt.useSubstitutionFont);
        checkEnableNativeLibTIFF.setSelected(pdfOpt.enableNativeLibTIFF);
        
        checkAssumePortrait.setSelected(pdfOpt.TIFFassumePortrait);
        checkChopLongPage.setSelected(pdfOpt.TIFFchopLongPage);
        checkFitToPage.setSelected(pdfOpt.TIFFfitToPaperSize);
        
        textChopFactor.setText(floatFormat.format(pdfOpt.TIFFchopFactor));
        textChopThreshold.setText(floatFormat.format(pdfOpt.TIFFchopThreshold));
        
        ftfSubstFont.setText(pdfOpt.substitutionFontPath);
        
        PathAndViewPanel.requireTIFF2PDF = !pdfOpt.useITextForTIFF;
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        PDFOptions pdfOpt = EntryPoint.getOptions();
        
        pdfOpt.useITextForGIF = checkUseForGIF.isSelected();
        pdfOpt.useITextForPNG = checkUseForPNG.isSelected();
        pdfOpt.useITextForJPEG = checkUseForJPEG.isSelected();
        pdfOpt.useITextForTIFF = checkUseForTIFF.isSelected();
        pdfOpt.useSubstitutionFont = checkUseSubstFont.isSelected();
        pdfOpt.enableNativeLibTIFF = checkEnableNativeLibTIFF.isSelected();
        
        pdfOpt.TIFFassumePortrait = checkAssumePortrait.isSelected();
        pdfOpt.TIFFchopLongPage = checkChopLongPage.isSelected();
        pdfOpt.TIFFfitToPaperSize = checkFitToPage.isSelected();
        
        pdfOpt.substitutionFontPath = ftfSubstFont.getText();
        
        try {
            pdfOpt.TIFFchopFactor = floatFormat.parse(textChopFactor.getText()).floatValue();
        } catch (ParseException e) {
            log.log(Level.WARNING, "Exception parsing chop factor", e);
        }
        try {
            pdfOpt.TIFFchopThreshold = floatFormat.parse(textChopThreshold.getText()).floatValue();
        } catch (ParseException e) {
            log.log(Level.WARNING, "Exception parsing chop threshold", e);
        }
        
        FileConverters.invalidateFileConverters();
    }

    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        try {
            float chopThreshold = floatFormat.parse(textChopThreshold.getText()).floatValue();
            if (chopThreshold <= 0f || chopThreshold > 1000f) {
                JOptionPane.showMessageDialog(optionsWin, _("The long page threshold must be between 0 and 1000."), _("PDF support (iText)"), JOptionPane.ERROR_MESSAGE);
                optionsWin.focusComponent(textChopThreshold);
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(optionsWin, _("The long page threshold must be a number."), _("PDF support (iText)"), JOptionPane.ERROR_MESSAGE);
            optionsWin.focusComponent(textChopThreshold);
            return false;
        }

        try {
            float chopFactor = floatFormat.parse(textChopFactor.getText()).floatValue();
            if (chopFactor <= 0f || chopFactor > 1000f) {
                JOptionPane.showMessageDialog(optionsWin, _("The height factor must be between 0 and 1000."), _("PDF support (iText)"), JOptionPane.ERROR_MESSAGE);
                optionsWin.focusComponent(textChopFactor);
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(optionsWin, _("The height factor must be a number."), _("PDF support (iText)"), JOptionPane.ERROR_MESSAGE);
            optionsWin.focusComponent(textChopFactor);
            return false;
        }
        
        if (checkUseSubstFont.isSelected() && !new File(ftfSubstFont.getText()).canRead()) {
            JOptionPane.showMessageDialog(optionsWin, _("The selected substitution font does not exist."), _("PDF support (iText)"), JOptionPane.ERROR_MESSAGE);
            optionsWin.focusComponent(ftfSubstFont.getJTextField());
            return false;
        }
        
        return true;
    }
}
