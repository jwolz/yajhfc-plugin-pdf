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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.TableType;
import yajhfc.pdf.EntryPoint;
import yajhfc.pdf.PDFOptions;
import yajhfc.pdf.report.SendReport;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.IntVerifier;
import yajhfc.util.SelectionTableModel;

/**
 * @author jonas
 *
 */
public class SendReportDialog<T extends FmtItem> extends JDialog {

	PdfDocWriterPanel panelPageSettings;
	FileTextField ftfDirectory;
	JTextField textFileNamePattern, textSelectedPages, textThumbnailsPerPage;
	JRadioButton radAllPages, radSelectedPages, radUnlimitedThumbs, radLimitThumbs;
	JCheckBox checkViewAfterGeneration;
	
	SelectionTableModel<T> colsModel;
	
	public boolean modalResult = false;
	
	public SendReportDialog(Frame owner, T[] columns) {
	    super(owner, true);
	    initialize(columns);
	}
	public SendReportDialog(Dialog owner, T[] columns) {
	    super(owner, true);
	    initialize(columns);
	}
	
	private void initialize(T[] columns) {
	    panelPageSettings = new PdfDocWriterPanel();
	    
	    ftfDirectory = new FileTextField();
	    ftfDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    
	    textFileNamePattern = new JTextField();
	    
	    textSelectedPages = new JTextField();
	    textSelectedPages.getDocument().addDocumentListener(new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            radSelectedPages.setSelected(true);
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            radSelectedPages.setSelected(true);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        }
	    });

	    textThumbnailsPerPage = new JTextField(4);
	    textThumbnailsPerPage.setInputVerifier(new IntVerifier(1, 9999));
	    textThumbnailsPerPage.getDocument().addDocumentListener(new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            radLimitThumbs.setSelected(true);
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            radLimitThumbs.setSelected(true);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        }
	    });
	    
	    radAllPages = new JRadioButton(_("All pages"));
	    radSelectedPages = new JRadioButton(_("Only the following pages:"));
	    ButtonGroup groupPages = new ButtonGroup();
	    groupPages.add(radAllPages);
        groupPages.add(radSelectedPages);
	    
	    radUnlimitedThumbs = new JRadioButton(_("Unlimited (put all on one page)"));
	    radLimitThumbs = new JRadioButton(_("At most:"));
	    ButtonGroup groupThumbs = new ButtonGroup();
	    groupThumbs.add(radUnlimitedThumbs);
	    groupThumbs.add(radLimitThumbs);
	    
	    checkViewAfterGeneration = new JCheckBox(_("Open generated PDFs in viewer"));

	    colsModel = new SelectionTableModel<T>(columns);

	    JTable tableCols = createSelectionTable(colsModel);
	    colsModel.addTableModelListener(new TableModelListener() {
	        public void tableChanged(TableModelEvent e) {
	            //checkEnabled();
	        }
	    });
	    tableCols.setDefaultRenderer(FmtItem.class, new DefaultTableCellRenderer() {
	        @Override
	        public Component getTableCellRendererComponent(JTable table,
	                Object value, boolean isSelected, boolean hasFocus,
	                int row, int column) {
	            String text;
	            if (value != null) {
	                FmtItem fi = (FmtItem)value;
	                
	                StringBuilder sb = new StringBuilder();
	                sb.append(fi.getDescription());
	                sb.append(" (%");
	                if (fi.name().length() > 1) 
	                    sb.append('{');
	                sb.append(fi.name());
	                if (fi.name().length() > 1) 
	                    sb.append('}');
	                sb.append(')');
	                   
	                text = sb.toString();
	            } else {
	                text = "";
	            }
	            
	            return super.getTableCellRendererComponent(table, text, isSelected, hasFocus,
	                    row, column);
	        }
	    });
	    
	    JScrollPane scrollCols = new JScrollPane(tableCols);
	    scrollCols.getViewport().setBackground(tableCols.getBackground());
	    scrollCols.getViewport().setOpaque(true);
	    
	    Action actOK = new ExcDialogAbstractAction(_("OK")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (validateInput()) {
                    modalResult = true;
                    setVisible(false);
                }
            }
        };
	    CancelAction actCancel = new CancelAction(this);

	    double[][] dLay;
	    dLay = new double[][] {
	            {border, TableLayout.PREFERRED, border/2, TableLayout.FILL, border},
	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
	    };
	    JPanel panelThumbs = new JPanel(new TableLayout(dLay), false);
	    panelThumbs.setBorder(BorderFactory.createTitledBorder(_("Number of thumbnails per page")));
	    panelThumbs.add(radUnlimitedThumbs, "1,1,3,1,l,c");
	    panelThumbs.add(radLimitThumbs, "1,3,l,c");
	    panelThumbs.add(textThumbnailsPerPage, "1,4,f,f");
	    panelThumbs.add(new JLabel(_("thumbnails per page")), "3,4,l,c");
	    
	    dLay = new double[][] {
	            {border, TableLayout.FILL, border},
	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
	    };
	    JPanel panelPages = new JPanel(new TableLayout(dLay), false);
	    panelPages.setBorder(BorderFactory.createTitledBorder(_("Fax pages to print thumbnails for")));
	    panelPages.add(radAllPages, "1,1,1,1,l,c");
	    panelPages.add(radSelectedPages, "1,3,l,c");
	    panelPages.add(textSelectedPages, "1,4,f,f");
	    
	    JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    panelButtons.add(new JButton(actOK));
	    panelButtons.add(actCancel.createCancelButton());
	    
	    dLay = new double[][] {
	      {border, TableLayout.FILL, border, 0.33, border, 0.33, border},
	      {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
	    };
	    JPanel contentPane = new JPanel(new TableLayout(dLay));
	    Utils.addWithLabel(contentPane, ftfDirectory, _("Directory to save the reports in:"), "1,2,3,2,f,f");
	    Utils.addWithLabel(contentPane, textFileNamePattern, _("File name pattern:"), "5,2,5,2,f,f");
	    
	    Utils.addWithLabel(contentPane, scrollCols, _("Information to print on report"), "1,5,1,13,f,f");
	    
	    contentPane.add(panelPages, "3,5,3,5,f,f");
	    contentPane.add(panelThumbs, "5,5,5,5,f,f");
	    contentPane.add(panelPageSettings, "3,7,5,7,f,t");
	    contentPane.add(checkViewAfterGeneration, "3,9,5,9,l,t");
	    contentPane.add(panelButtons, "3,13,5,13,f,f");
	    
	    setContentPane(contentPane);
	    setLocationByPlatform(true);
	    setSize(900, 500);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private JTable createSelectionTable(SelectionTableModel<?> model) {
	    JTable res = new JTable(model);
	    res.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    res.setShowGrid(false);
	    res.setRowSelectionAllowed(true);
	    res.setTableHeader(null);
	    res.getColumnModel().getColumn(0).setMaxWidth(15);
	    return res;
	}
	
	protected boolean validateInput() {
	    if (ftfDirectory.getText().length() == 0) {
	        JOptionPane.showMessageDialog(this, _("Please enter a existing directory to save the reports in."));
	        ftfDirectory.getJTextField().requestFocusInWindow();
	        return false;
	    }
	    if (! (new File(ftfDirectory.getText()).isDirectory()) ) {
	        JOptionPane.showMessageDialog(this, _("Please enter a existing directory to save the reports in."));
	        ftfDirectory.getJTextField().requestFocusInWindow();
	        return false;
	    }
	    
	    if (textFileNamePattern.getText().length() == 0) {
	        JOptionPane.showMessageDialog(this, _("Please enter a file name pattern to save the reports under."));
	        textFileNamePattern.requestFocusInWindow();
	        return false;
	    }
	    
        try {
            int iThumbsPerPage = Integer.valueOf(textThumbnailsPerPage.getText());
            if (iThumbsPerPage < 1 || iThumbsPerPage > 9999) {
                textThumbnailsPerPage.requestFocusInWindow();
                JOptionPane.showMessageDialog(this,
                        _("The number of thumbnails per page must be between 1 and 9999."));
                return false;
            }
        } catch (NumberFormatException e) {
            textThumbnailsPerPage.requestFocusInWindow();
            JOptionPane.showMessageDialog(this,
                    _("The number of thumbnails per page must be between 1 and 9999."));
            return false;
        }
	    
	    return panelPageSettings.validateInput();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void initializeValues(TableType tt) {
	    String title;
	    List selectedColumns;
	    String fileNamePattern;
	    PDFOptions opts = EntryPoint.getOptions();
	    
	    switch (tt) {
	    case RECEIVED:
	        title = _("Fax receive report");
	        selectedColumns = opts.reportRecvColumns;
	        fileNamePattern = opts.reportRecvPattern;
	        break;
	    case SENT:
	        title = _("Fax send report");
	        selectedColumns = opts.reportSentColumns;
	        fileNamePattern = opts.reportSentPattern;
	        break;
	    default:
	        throw new IllegalArgumentException("Only TableType SENT and RECEIVED supported!");
	    }
	    
	    setTitle(title);
	    if (selectedColumns.size() > 0) {
	        colsModel.setSelectedObjects(selectedColumns);
	    } else {
	        colsModel.selectAll();
	    }
	    
	    textFileNamePattern.setText(fileNamePattern);
	    textSelectedPages.setText(opts.reportSelectedPages);
	    textThumbnailsPerPage.setText(String.valueOf(opts.reportThumbsPerPage));
	    
	    if (opts.reportUnlimitedThumbs) {
	        radUnlimitedThumbs.setSelected(true);
	    } else {
	        radLimitThumbs.setSelected(true);
	    }
	    if (opts.reportPrintAllPages) {
	        radAllPages.setSelected(true);
	    } else {
	        radSelectedPages.setSelected(true);
	    }
	    
	    checkViewAfterGeneration.setSelected(opts.reportViewAfterGeneration);
	    
	    panelPageSettings.loadDefaults();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeToAndSaveDefaults(SendReport<T> sr, TableType tt) {
	    String directory = ftfDirectory.getText();
	    String fileNamePattern = textFileNamePattern.getText();
	    String selectedPages = textSelectedPages.getText();
	    
	    boolean viewAfterGeneration = checkViewAfterGeneration.isSelected();
	    boolean unlimitedThumbs = radUnlimitedThumbs.isSelected();
	    boolean printAllPages = radAllPages.isSelected();
	    
	    int thumbnailsPerPage = Integer.parseInt(textThumbnailsPerPage.getText());
	    
	    List<T> selectedCols = Arrays.asList(colsModel.getSelectedObjects());
	    
	    PDFOptions opts = EntryPoint.getOptions();
	    
	    opts.reportDir = directory;
	    opts.reportPrintAllPages = printAllPages;
	    opts.reportSelectedPages = selectedPages;
	    opts.reportThumbsPerPage = thumbnailsPerPage;
	    opts.reportUnlimitedThumbs = unlimitedThumbs;
	    opts.reportViewAfterGeneration = viewAfterGeneration;
	    String reportTitle;
	    switch (tt) {
	    case RECEIVED:
	    	reportTitle = _("Fax receive report");
	        opts.reportRecvColumns.clear();
	        opts.reportRecvColumns.addAll((List)selectedCols);
	        opts.reportRecvPattern = fileNamePattern;
	        break;
	    case SENT:
	    	reportTitle = _("Fax send report");
	        opts.reportSentColumns.clear();
	        opts.reportSentColumns.addAll((List)selectedCols);
	        opts.reportSentPattern = fileNamePattern;
	        break;
	    default:
	        throw new IllegalArgumentException("Only TableType SENT and RECEIVED supported!");
	    }
	    
	    sr.setHeadLine(reportTitle);
	    sr.setColumns(selectedCols);
	    sr.setDirectory(directory);
	    sr.setFileNamePattern(fileNamePattern);
	    if (printAllPages) {
	    	sr.setSelectedPages(null);
	    } else {
	    	sr.setSelectedPages(selectedPages);
	    }
	    if (unlimitedThumbs) {
	    	sr.setThumbnailsPerPage(0);
	    } else {
	    	sr.setThumbnailsPerPage(thumbnailsPerPage);
	    }
	    
	    panelPageSettings.writeToAndSaveDefaults(sr);
	}
	
	public static <T extends FmtItem> SendReport<T> showSendReportDialog(Frame owner, T[] columns, TableType tt) {
	    SendReportDialog<T> srd = new SendReportDialog<T>(owner, columns);
	    srd.initializeValues(tt);
	    srd.setVisible(true);
	    srd.dispose();
	    return null;
	}
}
