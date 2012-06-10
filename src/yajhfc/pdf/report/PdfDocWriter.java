package yajhfc.pdf.report;

import java.awt.print.PageFormat;

import yajhfc.util.ProgressWorker;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;

/**
 * Common properties of a PDF document
 * 
 * @author jonas
 *
 */
public class PdfDocWriter {

    protected yajhfc.PaperSize paperSize = yajhfc.PaperSize.A4;
    /**
     * Top margin in PostScript points
     */
    protected float marginTop    = 20;
    /**
     * Left margin in PostScript points
     */
    protected float marginLeft   = 20;
    /**
     * Right margin in PostScript points
     */
    protected float marginRight  = 20;
    /**
     * Bottom margin in PostScript points
     */
    protected float marginBottom = 20;
    /**
     * Orientation using the PageFormat constants
     */
    protected int orientation = PageFormat.PORTRAIT;
    protected ProgressWorker statusWorker = null;

    public PdfDocWriter() {
        super();
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    public float getMarginTopMM() {
        return Utilities.pointsToMillimeters(marginTop);
    }

    public float getMarginLeftMM() {
        return Utilities.pointsToMillimeters(marginLeft);
    }

    public float getMarginRightMM() {
        return Utilities.pointsToMillimeters(marginRight);
    }

    public float getMarginBottomMM() {
        return Utilities.pointsToMillimeters(marginBottom);
    }

    public void setMarginTopMM(float marginTop) {
        this.marginTop = Utilities.millimetersToPoints(marginTop);
    }

    public void setMarginLeftMM(float marginLeft) {
        this.marginLeft = Utilities.millimetersToPoints(marginLeft);
    }

    public void setMarginRightMM(float marginRight) {
        this.marginRight = Utilities.millimetersToPoints(marginRight);
    }

    public void setMarginBottomMM(float marginBottom) {
        this.marginBottom = Utilities.millimetersToPoints(marginBottom);
    }

    public yajhfc.PaperSize getPaperSize() {
        return paperSize;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setPaperSize(yajhfc.PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setStatusWorker(ProgressWorker statusWorker) {
        this.statusWorker = statusWorker;
    }

    public ProgressWorker getStatusWorker() {
        return statusWorker;
    }

    protected Document createPdfDocument() {
        Rectangle pageSize = PageSize.getRectangle(paperSize.name());
        switch (orientation) {
        case PageFormat.LANDSCAPE:
        case PageFormat.REVERSE_LANDSCAPE:
            pageSize = pageSize.rotate();
            break;
        }
        return new Document(pageSize, marginLeft, marginRight, marginTop, marginBottom);
    }

}