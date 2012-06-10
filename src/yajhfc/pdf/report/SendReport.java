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
package yajhfc.pdf.report;

import static yajhfc.pdf.i18n.Msgs._;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.UnknownFormatException;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author jonas
 *
 */
public class SendReport<T extends FmtItem> extends PdfDocWriter {    
    
    protected final List<T> columns = new ArrayList<T>();
    
    protected int startPage = 1;
    /**
     * End page. 0 means all pages
     */
    protected int endPage = 0; 
    /**
     * Number of thumbnails per page. 0 means unlimited (all on one page)
     */
    protected int thumbnailsPerPage = 0;
    
    protected BaseFont headerFont;
    protected float headerFontSize = 18;
    
    protected BaseFont normalFont;
    protected float normalFontSize = 10;
    
    protected String headLine = _("Fax send report");
    
    
    public SendReport() throws DocumentException, IOException {
        headerFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        normalFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }
    
    /**
     * Print a header
     * @param cb
     * @param x
     * @param y
     * @param width
     * @param nPage 
     * @param numPages
     * @return
     * @throws IOException 
     * @throws DocumentException 
     */
    protected float printHeader(PdfContentByte cb, float x, float y, float width, int nPage, int numPages) throws DocumentException, IOException {
        cb.beginText();
        cb.setFontAndSize(headerFont, headerFontSize);
        
        cb.setTextMatrix(x, y-headerFontSize);
        cb.showText(headLine);
        
        cb.setFontAndSize(normalFont, normalFontSize);
        String pageText = MessageFormat.format(_("Page {0} of {1}"), nPage, numPages);
        float textWidth = cb.getEffectiveStringWidth(pageText, false);
        cb.setTextMatrix(x+width-textWidth, y-headerFontSize);
        cb.showText(pageText);
 
        cb.endText();
        
        return y - headerFontSize*1.5f;
    }
    
    protected float printRows(PdfContentByte cb, float x, float y, float width, Row[] rows) throws DocumentException, IOException {
        y -= 2;
        cb.setLineWidth(0f);
        cb.moveTo(x, y);
        cb.lineTo(x+width, y);
        cb.stroke();
        y -= 2;
        
        cb.beginText();
        cb.setFontAndSize(normalFont, normalFontSize);
        
        float maxWidth = 0;
        for (Row row : rows) {
            row.width = cb.getEffectiveStringWidth(row.description, false);
            if (row.width > maxWidth)
                maxWidth = row.width;
        }
        for (Row row : rows) {
            cb.setTextMatrix(x + maxWidth - row.width, y-10);
            cb.showText(row.description + ": " + row.value);
            y -= 14;
        }
        cb.endText();
        
        y -= 2;
        cb.setLineWidth(0f);
        cb.moveTo(x, y);
        cb.lineTo(x+width, y);
        cb.stroke();
        y -= 2;
        
        
        return y;
    }
    
    /**
     * 
     * @param cb
     * @param writer
     * @param x
     * @param y
     * @param width
     * @param height
     * @param pdfFile
     * @param beginPage first page to output
     * @param maxPages the maximum number of pages to output or 0 for all pages
     * @return The last page processed
     * @throws IOException
     */
    protected int printNUp(PdfContentByte cb, PdfWriter writer, PdfReader reader, float x, float y, float width, float height, int beginPage, int maxPages) throws IOException {
        
        Rectangle pgSize = reader.getPageSize(1);
        int numPages = reader.getNumberOfPages() - beginPage + 1;
        if (maxPages > 0 && numPages > maxPages)
            numPages = maxPages;
            
        int numCols = numPages;
        int numRows = 1;
        
        float factor;
        
        while (true) {
            factor = Math.min(
                    width  / (pgSize.getWidth()  * numRows),
                    height / (pgSize.getHeight() * numCols)
                    );
        
            float scaledHeight = pgSize.getHeight() * factor;
            if (height / scaledHeight > (numRows + 1)) {
                numRows += 1;
                numCols  = (numPages + numRows - 1) / numRows; // set the number of rows to numPages / numRows, rounded up 
            } else {
                break;
            }
        }
        
        cb.setLineWidth(0);
        float cellWidth  = width  / numCols;
        float cellHeight = height / numRows;
        int iPage = 0;
        for (int row=0; row<numRows; row++) {
            for (int col = 0; col<numCols; col++) {
                iPage = row * numCols + col + beginPage;
                if (iPage >= (beginPage + numPages)) {
                    return iPage-1;
                }
                PdfImportedPage page = writer.getImportedPage(reader, iPage);
                Rectangle bbox = page.getBoundingBox();
                
                factor = Math.min(
                        cellWidth / bbox.getWidth(),
                        cellHeight / bbox.getHeight()
                        );
                
                float drawWidth  = bbox.getWidth() * factor;
                float drawHeight = bbox.getHeight() * factor;
                float drawX      = x + col * cellWidth  + (cellWidth  - drawWidth)  / 2;
                float drawY      = y - row * cellHeight - (cellHeight + drawHeight) / 2;
                
                cb.addTemplate(page, factor, 0, 0, factor, drawX, drawY);
                
                cb.rectangle(drawX, drawY, drawWidth, drawHeight);
                cb.stroke();
            }
        }
        
        return iPage;
    }
    
    public void createReport(FaxJob<T> job, File pdfFile) throws IOException, ServerResponseException, UnknownFormatException, ConversionException, DocumentException {
        if (statusWorker != null) {
            statusWorker.updateNote(_("Calculating job information..."));
        }
        // Calculate rows for status table
        Row[] rows = new Row[columns.size()];
        for (int i=0; i<rows.length; i++) {
            T col = columns.get(i);
            String desc = col.getDescription();
            Object oVal = job.getData(col);
            String sVal;
            if (oVal == null) {
                sVal = "";
            } else if (col.getDataType() == Date.class) {
                sVal = col.getDisplayDateFormat().format(oVal);
            } else if (col.getDataType() == Boolean.class) {
                sVal = ((Boolean)oVal).booleanValue() ? _("yes") : _("no");
            } else {
                sVal = oVal.toString();
            }
            
            rows[i] = new Row(desc, sVal);
        }
        
        // Create input PDF
        if (statusWorker != null) {
            statusWorker.updateNote(_("Retrieving list of documents..."));
        }
        Collection<FaxDocument> docs = job.getDocuments();
        if (statusWorker != null) {
            statusWorker.updateNote(_("Downloading documents..."));
        }
        List<FormattedFile> files = new ArrayList<FormattedFile>(docs.size());
        for (FaxDocument doc : docs) {
            files.add(doc.getDocument());
        }
        if (statusWorker != null) {
            statusWorker.updateNote(_("Converting documents to PDF..."));
        }
        File tempPDF = File.createTempFile("report", ".pdf");
        tempPDF.deleteOnExit();
        MultiFileConverter.convertMultipleFilesToSingleFile(files, tempPDF, MultiFileConvFormat.PDF, paperSize);
        
        if (statusWorker != null) {
            statusWorker.updateNote(_("Creating PDF..."));
        }
        // Create the output PDF
        Document document = createPdfDocument();
        FileOutputStream outStream = new FileOutputStream(pdfFile);
        PdfWriter writer = PdfWriter.getInstance(document, outStream);
        document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
        document.addSubject(headLine + " (fax job " + job.getIDValue() + ")");
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfReader reader = new PdfReader(tempPDF.getPath());
        
        int firstPage = startPage;
        int lastPage = reader.getNumberOfPages();
        if (endPage > 0 && lastPage > endPage)
            lastPage = endPage;
        int totalOutPages;
        if (thumbnailsPerPage == 0) {
            totalOutPages = 1;
        } else {
            totalOutPages = (reader.getNumberOfPages() + thumbnailsPerPage - 1) / thumbnailsPerPage;
        }
        int outPage = 1;
        while (firstPage <= lastPage) {
            if (statusWorker != null) {
                statusWorker.updateNote(MessageFormat.format(_("Writing PDF page {0}..."), outPage));
            }
            document.newPage();

            float x = document.left();
            float y = document.top();
            float width  = document.right() - document.left(); 

            y = printHeader(cb, x, y, width, outPage, totalOutPages);
            y = printRows(cb, x, y, width, rows);

            float height = y - document.bottom(); 
            
            firstPage = printNUp(cb, writer, reader, x, y, width, height, firstPage, Math.min(thumbnailsPerPage, lastPage-firstPage+1)) + 1;
            outPage++;
        }
        
        // Close document
        if (statusWorker != null) {
            statusWorker.updateNote(_("Finishing..."));
        }
        reader.close();
        document.close();
        
        outStream.close();
        
        tempPDF.delete();
    }

    public float getHeaderFontSize() {
        return headerFontSize;
    }

    public float getNormalFontSize() {
        return normalFontSize;
    }

    public List<T> getColumns() {
        return columns;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public int getThumbnailsPerPage() {
        return thumbnailsPerPage;
    }

    
    public void setHeaderFontSize(float headerFontSize) {
        this.headerFontSize = headerFontSize;
    }

    public void setNormalFontSize(float normalFontSize) {
        this.normalFontSize = normalFontSize;
    }


    public void setStartPage(int startPage) {
        if (startPage < 1)
            throw new IllegalArgumentException("startPage must be >= 1");
        this.startPage = startPage;
    }

    public void setEndPage(int endPage) {
        if (endPage < 0)
            throw new IllegalArgumentException("endPage must be >= 0");
        this.endPage = endPage;
    }

    public void setThumbnailsPerPage(int thumbnailsPerPage) {
        if (thumbnailsPerPage < 0)
            throw new IllegalArgumentException("thumbnailsPerPage must be >= 0");
        this.thumbnailsPerPage = thumbnailsPerPage;
    }

    public BaseFont getHeaderFont() {
        return headerFont;
    }

    public BaseFont getNormalFont() {
        return normalFont;
    }

    public void setHeaderFont(BaseFont headerFont) {
        this.headerFont = headerFont;
    }

    public void setNormalFont(BaseFont normalFont) {
        this.normalFont = normalFont;
    }
    
    public String getHeadLine() {
        return headLine;
    }
    
    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }

    static class Row {
        public final String description;
        public final String value;
        public float width;
        
        
        public Row(String description, String value) {
            super();
            this.description = description;
            this.value = value;
        }
    }
}
