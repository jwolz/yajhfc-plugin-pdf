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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.servconn.FaxJob;
import yajhfc.pdf.FilenameSanitizer;
import yajhfc.pdf.i18n.Msgs;

/**
 * @author jonas
 *
 */
public class FilenameGenerator<T extends FmtItem> {
    protected String directory;
	protected Object[] parsedPattern;
	
	public FilenameGenerator() {
    }
	
	public FilenameGenerator(String directory, String patternString, FmtItemList<T> possibleValues) {
		setPatternString(patternString, possibleValues);
		this.directory = directory;
	}
	
	public String getDirectory() {
        return directory;
    }
	
	public void setDirectory(String directory) {
        this.directory = directory;
    }
	
	public void setPatternString(String patternString, FmtItemList<T> possibleValues) {
    	Pattern percentPattern = Pattern.compile("%(?:(.)|\\{(.+?)\\})");
    	
    	List<Object> parsedList = new ArrayList<Object>();
    	
    	Matcher m = percentPattern.matcher(patternString);
    	int pos = 0;
    	while (m.find()) {
    		if (m.start() > pos) {
    			parsedList.add(patternString.substring(pos, m.start()));
    		}
    		String name = m.group(1);
    		if (name == null) {
    			name = m.group(2);
    		}
    		
    		if ("%".equals(name)) {
    			parsedList.add("%");
    		} else {
    			T col = possibleValues.getKeyForName(name);
    			if (col != null) {
    				parsedList.add(col);
    			} else {
    				parsedList.add(name);
    			}
    		}
    		pos = m.end();
    	}
    	if (pos < patternString.length()-1) {
    		parsedList.add(patternString.substring(pos));
    	}
    	parsedPattern = parsedList.toArray();
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Generates a file name for the given fax job
	 * @param job
	 * @return
	 */
	public String getFilename(FaxJob<T> job) {
		StringBuilder sb = new StringBuilder();
		for (Object o : parsedPattern) {
			if (o instanceof FmtItem) {
			    T col = (T)o;
			    Object data = job.getData(col);
			    
			    // Check if we need to reformat data
			    if (data == null) {
			        data = "";
			    } else if (col.getDataType() == Date.class) {
			        data = col.getDisplayDateFormat().format(data);
			    } else if (col.getDataType() == Boolean.class) {
			        data = ((Boolean)data).booleanValue() ? Msgs._("yes") : Msgs._("no");
			    } 
			        
				sb.append(data);
			} else {
				sb.append(o);
			}
		}
		return FilenameSanitizer.replaceInvalidCharacters(sb.toString(), '_');
	}
	
	public File getFile(FaxJob<T> job) {
	    return getFile(job, false);
	}
	
	/**
	 * Returns a File object for the given FaxJob
	 * @param job
	 * @param checkCollision false to check for "collision", i.e. to generate an unique name
	 * @return
	 */
	public File getFile(FaxJob<T> job, boolean allowCollision) {
	    String fileName = getFilename(job);
	    File result = new File(directory, fileName);
	    
	    if (!allowCollision && result.exists()) {
	        int pos = fileName.lastIndexOf('.'); // Find the position of the extension
	        String prefix, suffix;
	        if (pos >= 0) {
	            prefix = fileName.substring(0,pos);
	            suffix = fileName.substring(pos);
	        } else {
	            prefix = fileName;
	            suffix = "";
	        }
	        
	        int num = 1;
	        do {
	            // Generate a file name in the form file-2.pdf
	            result = new File(directory, prefix + '-' + (++num) + suffix);
	        } while (result.exists());
	    }
	    return result;
	}
}

