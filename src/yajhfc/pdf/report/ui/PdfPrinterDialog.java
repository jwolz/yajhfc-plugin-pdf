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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.pdf.EntryPoint;
import yajhfc.pdf.report.PdfDocWriter;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class PdfPrinterDialog extends SafeJFileChooser {

    PdfDocWriterPanel pdwPanel; 
    
    public PdfDocWriterPanel getPDWPanel() {
        if (pdwPanel == null) {
            pdwPanel = new PdfDocWriterPanel();
        }
        return pdwPanel;
    }
    
    @Override
    protected JDialog createDialog(Component parent)
            throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        JPanel newContentPane = new JPanel(new BorderLayout(), false);
        newContentPane.add(dialog.getContentPane(), BorderLayout.CENTER);
        newContentPane.add(getPDWPanel(), BorderLayout.SOUTH);
        dialog.setContentPane(newContentPane);
        dialog.pack();
        return dialog;
    }
    
    @Override
    public void approveSelection() {
        if (pdwPanel.validateInput()) {
            super.approveSelection();
        }
    }
    
    public static File showPdfPrinterDialog(Component parent, PdfDocWriter pdw, String title) {
        PdfPrinterDialog ppd = new PdfPrinterDialog();
        ppd.setDialogTitle(title);
        ppd.setDialogType(JFileChooser.SAVE_DIALOG);
       
        ppd.removeChoosableFileFilter(ppd.getAcceptAllFileFilter());
        FileFilter ff = FileFormat.PDF.createFileFilter();
        ppd.addChoosableFileFilter(ff);
        ppd.addChoosableFileFilter(ppd.getAcceptAllFileFilter());
        ppd.setFileFilter(ff);
        
        ppd.getPDWPanel().loadDefaults();
        String lastSaveLocation = EntryPoint.getOptions().lastSaveLocation;
        if (lastSaveLocation != null && lastSaveLocation.length() > 0)
            ppd.setSelectedFile(new File(lastSaveLocation));
        
        if (ppd.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            ppd.getPDWPanel().writeToAndSaveDefaults(pdw);
            File selFile = Utils.getSelectedFileFromSaveChooser(ppd);
                    
            EntryPoint.getOptions().lastSaveLocation = selFile.getPath();
            return selFile;
        } else {
            return null;
        }
    }
}
