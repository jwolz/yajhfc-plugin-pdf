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

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import yajhfc.Utils;
import yajhfc.pdf.i18n.Msgs;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Class to "print" printables to PDF
 * @author jonas
 *
 */
public class PdfPrinter extends PdfDocWriter {
    protected String subject = "Report";
    
    public void printToPDF(Printable printable, File outputFile) throws DocumentException, IOException, PrinterException {
        FileOutputStream outStream = new FileOutputStream(outputFile);
        
        // Print 2 times: First to determine number of pages, then "really"
        if (statusWorker != null) {
            statusWorker.updateNote(_("Calculating number of pages..."));
        }
        int numberOfPages = printDoc(printable, new NullOutputStream(), 0, new MessageFormat(Msgs._("Calculating page {0}...")));
        if (statusWorker != null) {
            statusWorker.updateNote(_("Writing PDF..."));
        }
        printDoc(printable, outStream, numberOfPages, new MessageFormat(Msgs._("Writing page {0}...")));
    }


    /**
     * Prints the document and returns the actual number of pages printed
     * @param printable
     * @param outputFile
     * @param pf
     * @param ps
     * @param numberOfPages the number of pages to print or 0
     * @return
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws PrinterException
     * @throws IOException
     */
    protected int printDoc(Printable printable, OutputStream outStream, int numberOfPages, MessageFormat statusMsg)
            throws FileNotFoundException, DocumentException, PrinterException,
            IOException {
        PageFormat pf = new PageFormat();
        pf.setPaper(getPaper());
        pf.setOrientation(orientation);
        
        Document document = createPdfDocument();
        PdfWriter writer = PdfWriter.getInstance(document, outStream);
        document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
        document.addSubject(subject);
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        
        int iPage = 0;
        int response = Printable.PAGE_EXISTS;
        while (response == Printable.PAGE_EXISTS && (numberOfPages == 0 || iPage < numberOfPages)) {
            if (statusWorker != null) {
                statusWorker.updateNote(statusMsg.format(new Object[] { iPage+1 }));
                if (numberOfPages > 0) {
                    statusWorker.setProgress(iPage * 100 / numberOfPages);
                }
            }
            Graphics2D graphics = cb.createGraphics(document.getPageSize().getWidth(), document.getPageSize().getHeight());
            response = printable.print(graphics, pf, iPage);
            graphics.dispose();
            if (response == Printable.PAGE_EXISTS) {
                document.newPage();
                iPage++;
            }
        }
        
        document.close();
        outStream.close();
        return iPage;
    }

    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getSubject() {
        return subject;
    }

    /**
     * Creates an AWT Paper using the PaperSize and margins
     * @return
     */
    protected Paper getPaper() {
        float width = paperSize.getWidthPoints();
        float height = paperSize.getHeightPoints();
        
        Paper p = new Paper();
        p.setSize(width, height);
        p.setImageableArea(marginLeft, marginTop, width - marginLeft - marginRight, height - marginTop - marginBottom);
        return p;
    }
}
