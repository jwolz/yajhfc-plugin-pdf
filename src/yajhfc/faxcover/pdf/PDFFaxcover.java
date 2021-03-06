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
package yajhfc.faxcover.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Logger;

import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.tag.ConditionState;
import yajhfc.faxcover.tag.ConditionalTag;
import yajhfc.faxcover.tag.Tag;
import yajhfc.pdf.EntryPoint;
import yajhfc.pdf.PDFOptions;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author jonas
 *
 */
public class PDFFaxcover extends Faxcover {
    private static final Logger log = Logger.getLogger(PDFFaxcover.class.getName());
        
    public PDFFaxcover(URL coverTemplate) {
        super(coverTemplate);
    }
    
    protected PdfReader getPdfReader() throws IOException {
        return new PdfReader(this.coverTemplate); 
    }

    /* (non-Javadoc)
     * @see yajhfc.faxcover.Faxcover#makeCoverSheet(java.io.OutputStream)
     */
    @Override
    public void makeCoverSheet(OutputStream out) throws IOException {
        try {
            log.fine("Opening " + this.coverTemplate);
            PdfStamper filledOutForm = new PdfStamper(getPdfReader(), out);

            BaseFont bf = null;
            PDFOptions po = EntryPoint.getOptions();
            if (po.useSubstitutionFont) {
                bf = BaseFont.createFont(po.substitutionFontPath, BaseFont.IDENTITY_H, true);
            }
            
            AcroFields form = filledOutForm.getAcroFields();
            
            if (bf != null)
                form.addSubstitutionFont(bf);
            form.setGenerateAppearances(true);
            
            for (String field : form.getFields().keySet()) {
                Tag tag = Tag.availableTags.get(field.toLowerCase());
                if (tag != null) {
                    if (tag instanceof ConditionalTag) {
                        log.warning("Conditional tags not supported for PDF: " + field);
                    } else {
                        form.setField(field, tag.getValue(this, Collections.<ConditionState>emptyList(), null));
                    }
                } else {
                    log.info("Unknown form field \"" + field + "\" found, ignoring it.");
                }
            }
            
            filledOutForm.setFormFlattening(true);
            filledOutForm.close();
        } catch (DocumentException e) {
            throw (IOException)new IOException("Error from iText").initCause(e);
        } catch (NoClassDefFoundError e) {
            throw (IOException)new IOException("iText not found").initCause(e);
        }
    }

}
