package yajhfc.file.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter;
import yajhfc.file.FileFormat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

public class ITextTIFFFileConverter implements FileConverter {
    private static final Logger log = Logger.getLogger(ITextTIFFFileConverter.class.getName());
    
    @Override
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat)
    throws ConversionException, IOException {
        try {
            log.fine("Converting " + inFile + " to PDF using itext");
            Document document = new Document(PageSize.getRectangle(paperSize.name()), 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, destination);
            document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
            document.addSubject(inFile.getPath());
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            RandomAccessFileOrArray ra = new RandomAccessFileOrArray(inFile.getPath());
            int pages = TiffImage.getNumberOfPages(ra);
            log.fine("TIFF has " + pages + " pages");
            for (int pg = 0; pg < pages; ++pg) {
                Image img = TiffImage.getTiffImage(ra, pg + 1);
                if (img != null) {
                    log.fine("Writing page " + (pg + 1));
                    if (Utils.getFaxOptions().usePaperSizeForTIFF2Any) {
                        img.scaleToFit(document.right()-document.left(), document.top()-document.bottom());
                    } else {
                        img.scalePercent(7200f / img.getDpiX(), 7200f / img.getDpiY());
                        img.setAbsolutePosition(0, 0);
                        document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
                    }
                    cb.addImage(img);
                    document.newPage();
                }
            }
            ra.close();
            document.close();
        } catch (DocumentException e) {
            throw new ConversionException("DocumentException from itext received", e);
        }
    }

    @Override
    public boolean isOverridable() {
        return true;
    }

}
