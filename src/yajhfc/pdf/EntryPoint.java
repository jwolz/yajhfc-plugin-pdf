/*
 * YAJHFC - Yet another Java Hylafax client
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


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.pdf.PDFFaxcover;
import yajhfc.file.FileConverter;
import yajhfc.file.FileConverterSource;
import yajhfc.file.FileConverters;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.file.pdf.ITextImageFileConverter;
import yajhfc.file.pdf.ITextTIFFFileConverter;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.file.textextract.pdf.ITextPDFToTextConverter;
import yajhfc.launch.Launcher2;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.options.PanelTreeNode;
import yajhfc.pdf.i18n.Msgs;
import yajhfc.pdf.report.PdfPrinter;
import yajhfc.pdf.report.SendReport;
import yajhfc.pdf.report.ui.PdfPrinterDialog;
import yajhfc.phonebook.ui.NewPhoneBookWin;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.print.FaxTablePrinter;
import yajhfc.print.PhonebooksPrinter;
import yajhfc.print.tableprint.TablePrintable;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressWorker;

import com.itextpdf.text.Document;

/**
 * Example initialization class for a YajHFC plugin.
 * 
 * The name of this class can be chosen freely, but must match the name
 * set in the YajHFC-Plugin-InitClass entry in the jar file.
 * @author jonas
 *
 */
public class EntryPoint {

	private static final String PROP_PHONEBOOKWIN = "YajHFC-PhoneBookWin";
	
    /**
	 * Plugin initialization method.
	 * The name and signature of this method must be exactly as follows 
	 * (i.e. it must always be "public static boolean init(int)" )
	 * @param startupMode the mode YajHFC is starting up in. The possible
	 *    values are one of the STARTUP_MODE_* constants defined in yajhfc.plugin.PluginManager
	 * @return true if the initialization was successful, false otherwise.
	 */
	public static boolean init(int startupMode) {
	    if (Utils.debugMode) {
	        Logger.getLogger(EntryPoint.class.getName()).info("iText version: " + Document.getVersion());
	    }
	    
	    Faxcover.supportedCoverFormats.put(FileFormat.PDF, PDFFaxcover.class);
	    
	    FileConverters.addFileConverterSource(new FileConverterSource() {
	        @Override
	        public void addFileConvertersTo(Map<FileFormat, FileConverter> converters) {	            
	            try {
                    Document.getVersion();
                } catch (Throwable e) {
                    Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, "Could not initialize iText", e);
                    return;
                }
	            
	            PDFOptions options = getOptions();
	            if (options.useITextForTIFF)
	                converters.put(FileFormat.TIFF, new ITextTIFFFileConverter(options));
	            
	            ITextImageFileConverter ifc = new ITextImageFileConverter();
	            if (options.useITextForPNG)
	                converters.put(FileFormat.PNG, ifc);
	            if (options.useITextForJPEG)
	                converters.put(FileFormat.JPEG, ifc);
	            if (options.useITextForGIF)
	                converters.put(FileFormat.GIF, ifc);
	        }
        });
	    
	    HylaToTextConverter.availableConverters.add(new ITextPDFToTextConverter());

	    final Action actShowReport = new ExcDialogAbstractAction() {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                try {
                    MainWin mw = (MainWin)Launcher2.application;
                    TooltipJTable<? extends FmtItem> table = mw.getSelectedTable();
                    final SendReport rpt = new SendReport<FmtItem>();
                    rpt.setThumbnailsPerPage(4);
                    rpt.getColumns().addAll(table.getRealModel().getColumns());
                    final FaxJob[] selectedJobs = table.getSelectedJobs();
                    
                    
                    ProgressWorker pw = new ProgressWorker() {
                        @Override
                        public void doWork() {
                            try {
                                rpt.setStatusWorker(this);
                                for (FaxJob<? extends FmtItem> job : selectedJobs) {
                                    File outFile = new File("/tmp/test.pdf");
                                    rpt.createReport(job, outFile);
                                    
                                    FormattedFile ff = new FormattedFile(outFile);
                                    ff.view();
                                }
                            } catch (Exception e2) {
                                ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error generating report:"), e2);
                            }
                        }
                    };
                    pw.setProgressMonitor(mw.getTablePanel());
                    pw.startWork(mw, Msgs._("Generating report..."));
                } catch (Exception e2) {
                    ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error generating report:"), e2);
                }
            }
        };
        actShowReport.putValue(Action.NAME, Msgs._("Generate report") + "...");
        actShowReport.putValue(Action.SHORT_DESCRIPTION, Msgs._("Generates a send or receive report for the fax"));
        
       final Action actPrintTableToPDF = new ExcDialogAbstractAction() {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                try {
                    MainWin mw = (MainWin)Launcher2.application;
                    TooltipJTable<? extends FmtItem> table = mw.getSelectedTable();
                    final String caption = mw.getTabMain().getToolTipTextAt(mw.getTabMain().getSelectedIndex());
                    
                    final TablePrintable tp = FaxTablePrinter.getFaxTablePrintable(mw, table, caption);
                    if (tp == null) // User hit cancel
                        return;
                    final PdfPrinter pp = new PdfPrinter();
                    final File outFile = PdfPrinterDialog.showPdfPrinterDialog(mw, pp, Msgs._("Print to PDF"));
                    if (outFile == null) // User hit cancel
                        return;
                    
                    pp.setSubject(caption);
                    
                    ProgressWorker pw = new ProgressWorker() {
                        @Override
                        public void doWork() {
                            try {
                                pp.setStatusWorker(this);
                                pp.printToPDF(tp, outFile);

                                updateNote(Msgs._("Starting viewer..."));
                                FormattedFile ff = new FormattedFile(outFile);
                                ff.view();
                            } catch (Exception e2) {
                                ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error printing to PDF:"), e2);
                            }
                        }
                    };
                    pw.setProgressMonitor(mw.getTablePanel());
                    pw.startWork(mw, Msgs._("Printing to PDF..."));
                } catch (Exception e2) {
                    ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error printing to PDF:"), e2);
                }
            }
        };
        actPrintTableToPDF.putValue(Action.NAME, Msgs._("Print to PDF") + "...");
        actPrintTableToPDF.putValue(Action.SHORT_DESCRIPTION, Msgs._("Print the selected table to PDF"));
        
       final Action actPrintPhonebookToPDF = new ExcDialogAbstractAction() {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                try {
                    JMenuItem item = (JMenuItem)e.getSource();
                    NewPhoneBookWin npbw = (NewPhoneBookWin)item.getClientProperty(PROP_PHONEBOOKWIN);
                    
                    final TablePrintable tp = PhonebooksPrinter.getPhonebooksTablePrintable(npbw, npbw.getTreeModel().getPhoneBooks(), npbw.getCurrentPhoneBook(), npbw.getRawSelectedEntries(), npbw.getTreeModel().isShowingFilteredResults());
                    if (tp == null) // User hit cancel
                        return;
                    final PdfPrinter pp = new PdfPrinter();
                    final File outFile = PdfPrinterDialog.showPdfPrinterDialog(npbw, pp, Msgs._("Print to PDF"));
                    if (outFile == null) // User hit cancel
                        return;
                    
                    pp.setSubject("Phone book");
                    
                    ProgressWorker pw = new ProgressWorker() {
                        @Override
                        public void doWork() {
                            try {
                                pp.setStatusWorker(this);
                                pp.printToPDF(tp, outFile);

                                updateNote(Msgs._("Starting viewer..."));
                                FormattedFile ff = new FormattedFile(outFile);
                                ff.view();
                            } catch (Exception e2) {
                                ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error printing to PDF:"), e2);
                            }
                        }
                    };
                    pw.setProgressMonitor(npbw.getProgressPanel());
                    pw.startWork(npbw, Msgs._("Printing to PDF..."));
                } catch (Exception e2) {
                    ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Msgs._("Error printing to PDF:"), e2);
                }
            }
        };
        actPrintPhonebookToPDF.putValue(Action.NAME, Msgs._("Print to PDF") + "...");
        actPrintPhonebookToPDF.putValue(Action.SHORT_DESCRIPTION, Msgs._("Print the selected phone book to PDF"));
        
	    PluginManager.pluginUIs.add(new PluginUI() {
	        @Override
	        public int getOptionsPanelParent() {
	            return OPTION_PANEL_PATHS_VIEWERS;
	        }
	        
	        @Override
	        public void configureMainWin(MainWin mainWin) {
	            insertAfter(mainWin.getMenuFax(), "ViewLog",  new JMenuItem(actShowReport));
	            mainWin.getMenuTable().add(new JMenuItem(actPrintTableToPDF));
	        }
	        
	        @Override
	        public void configurePhoneBookWin(NewPhoneBookWin phoneBookWin) {
	            JMenuItem itemPrintToPDF = new JMenuItem(actPrintPhonebookToPDF);
	            itemPrintToPDF.putClientProperty(PROP_PHONEBOOKWIN, phoneBookWin);
	            insertAfter(phoneBookWin.getPhonebookMenu(), "Print", itemPrintToPDF);
	        }
	        
	        @Override
	        public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
                /*
                 * This method must return a PanelTreeNode as shown below
                 * or null to not create an options page
                 */
                return new PanelTreeNode(
                        parent, // Always pass the parent as first parameter
                        new PDFOptionsPanel(), // The actual UI component that implements the options panel. 
                                                // This object *must* implement the OptionsPage interface.
                        Msgs._("PDF support (iText)"), // The text displayed in the tree view for this options page
                        loadIcon("pdf.png"));            // The icon displayed in the tree view for this options page
	        }
	        
	        @Override
	        public void saveOptions(Properties p) {
                getOptions().storeToProperties(p);
	        }
        });
	    
	    FileConverters.invalidateFileConverters();
		return true;
	}
	
    public static ImageIcon loadIcon(String fileName) {
        URL imgURL = EntryPoint.class.getResource("/yajhfc/pdf/images/" + fileName);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }
	
	private static PDFOptions options;
	/**
	 * Lazily load some options (optional, only if you want to save settings)
	 * @return
	 */
	public static PDFOptions getOptions() {
	    if (options == null) {
	        options = new PDFOptions();
	        options.loadFromProperties(Utils.getSettingsProperties());
	    }
	    return options;
	}
    
	public static void insertAfter(JMenu menu, String actionCommandAfter, JMenuItem menuItem) {
        int insertPos = indexOfAction(menu, actionCommandAfter);
        if (insertPos < 0) {
            insertPos = menu.getMenuComponentCount()-3;
        }
        menu.insert(menuItem, insertPos+1);
	}
	
	public static int indexOfAction(JMenu menu, String actionCommand) {
	    for (int i=0; i<menu.getMenuComponentCount(); i++) {
	        Component comp = menu.getMenuComponent(i);
	        if (comp instanceof JMenuItem) {
	            JMenuItem item = (JMenuItem)comp;
	            if (actionCommand.equals(item.getActionCommand())) {
	                return i;
	            }
	        }
	    }
	    return -1;
	}
	
    /**
     * Launches YajHFC including this plugin (for debugging purposes)
     * @param args
     */
    public static void main(String[] args) {
		PluginManager.internalPlugins.add(EntryPoint.class);
		Launcher2.main(args);
	}
}
