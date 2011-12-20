/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2009 Jonas Wolz
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

import static yajhfc.pdf.EntryPoint._;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;

import com.itextpdf.text.Document;

/**
 * Implements a crude and simple UI to set the three example options.
 * 
 * @author jonas
 *
 */
public class PDFOptionsPanel extends AbstractOptionsPanel<FaxOptions> {   
    
    public PDFOptionsPanel() {
		super(false);
	}

    JCheckBox checkUseForTIFF, checkUseForPNG, checkUseForJPEG, checkUseForGIF;
    
    @Override
    protected void createOptionsUI() {    
        checkUseForTIFF = new JCheckBox(_("Use iText to convert TIFF->PDF"));
        checkUseForPNG = new JCheckBox(_("Use iText to convert PNG->PDF"));
        checkUseForGIF = new JCheckBox(_("Use iText to convert GIF->PDF"));
        checkUseForJPEG = new JCheckBox(_("Use iText to convert JPEG->PDF"));
        
    	setLayout(new FlowLayout(FlowLayout.LEFT, OptionsWin.border, OptionsWin.border));
    	
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
        
        Box versionInfo = Box.createVerticalBox();
        versionInfo.add(new JLabel("iText version:"));
        versionInfo.add(new JLabel(Document.getVersion()));
        
        add(checkBoxPanel);
        add(versionInfo);
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
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        return true;
    }

}
