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

import static yajhfc.pdf.i18n.Msgs._;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.file.FileConverters;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;
import yajhfc.options.PathAndViewPanel;

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
        
        double[][] dLay = {
                {OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED,OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border}
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
        
        add(checkBoxPanel, "1,1,1,1,f,t");
        add(new JLabel(_("iText version used:")), "1,3,1,3,l,t");
        add(new JLabel(Document.getVersion()), "1,4,1,4,l,t");
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
        
        FileConverters.invalidateFileConverters();
    }

}
