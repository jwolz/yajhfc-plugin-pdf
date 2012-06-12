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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.servconn.FaxJob;
import yajhfc.pdf.FilenameSanitizer;

/**
 * @author jonas
 *
 */
public class FilenameGenerator<T extends FmtItem> {
	protected Object[] parsedPattern;
	
	public FilenameGenerator(String patternString, FmtItemList<T> possibleValues) {
		setPatternString(patternString, possibleValues);
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
	public String getFilename(FaxJob<T> job) {
		StringBuilder sb = new StringBuilder();
		for (Object o : parsedPattern) {
			if (o instanceof FmtItem) {
				sb.append(job.getData((T)o));
			} else {
				sb.append(o);
			}
		}
		return FilenameSanitizer.replaceInvalidCharacters(sb.toString(), '_');
	}
}

