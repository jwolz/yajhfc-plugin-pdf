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

import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import yajhfc.AbstractFaxOptions;
import yajhfc.PaperSize;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;

/**
 * Example class to save options in the YajHFC settings file if you need to.
 * 
 * To load/save options, you just need to create a subclass of AbstractFaxOptions
 * and add public fields for your options to it.
 * 
 * Then you can call the loadFromProperties/storeToProperties methods 
 * to load/store them to a properties file (e.g. the one returned
 * by Utils.getSettingsProperties()).
 * @author jonas
 *
 */
public class PDFOptions extends AbstractFaxOptions {
    public boolean useITextForTIFF = true;
    public boolean useITextForGIF = true;
    public boolean useITextForPNG = true;
    public boolean useITextForJPEG = true;
	
    
    public boolean TIFFfitToPaperSize = true;
    public boolean TIFFassumePortrait = true;
    public boolean TIFFchopLongPage = true;
    public float TIFFchopThreshold = 2f;
    public float TIFFchopFactor = 1.41f;
    
    
    public String lastSaveLocation = "";
    
    public int marginTop = 20;
    public int marginBottom = 20;
    public int marginLeft = 20;
    public int marginRight = 20;
    
    public PaperSize paperSize = PaperSize.A4;
    public int orientation = PageFormat.PORTRAIT;
    
    public String reportDir = "";
    public String reportRecvPattern = "received-fax-%s-%Y.pdf";
    public String reportSentPattern = "sent-fax-%e-%Y.pdf";
    public final List<RecvFormat> reportRecvColumns = new ArrayList<RecvFormat>();
    public final List<JobFormat> reportSentColumns = new ArrayList<JobFormat>();

    public boolean reportPrintAllPages = true;
    public String reportSelectedPages = "";
    public boolean reportUnlimitedThumbs = true;
    public int reportThumbsPerPage = 4;
    
    
	/**
	 * Call the super constructor with the prefix that should be prepended
	 * to the options name.
	 */
	public PDFOptions() {
		super("pdf");
	}
	
	@Override
	public void loadFromProperties(Properties p) {
	    if (!p.containsKey("pdf-TIFFfitToPaperSize") 
	            && p.containsKey("usePaperSizeForTIFF2Any")) {
	        p.setProperty("pdf-TIFFfitToPaperSize", p.getProperty("usePaperSizeForTIFF2Any"));
	    }
	    super.loadFromProperties(p);
	}
}
