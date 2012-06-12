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
package yajhfc.pdf;

import java.util.Arrays;

/**
 * @author jonas
 *
 */
public class FilenameSanitizer {
    /**
     * A sorted array of invalid characters for Windows file names
     */
    private static final char[] INVALID_FILE_NAME_CHARS_WIN = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, '"', '*', '/', ':', '<', '>', '?', '\\', '|'  
    };
    
    /**
     * Returns the aorted array of invalid file name characters for the current platform.
     * 
     * Note: Currently this returns the list for Windows for all platforms since this is most restrctive and therefore "safe". This may change in the future, however.
     * @return
     */
    public static char[] getInvalidFileNameCharacters() {
        return INVALID_FILE_NAME_CHARS_WIN;
    }
    
    /**
     * Replaces invalid file name characters with the specified replacement character
     * @param fileName
     * @param replacement
     * @return
     */
    public static String replaceInvalidCharacters(String fileName, char replacement) {
        char[] invalidChars = getInvalidFileNameCharacters();
        char[] res = fileName.toCharArray();
        boolean didReplace = false;
        
        for (int i=0; i<res.length; i++) {
            if (Arrays.binarySearch(invalidChars, res[i]) >= 0) {
                res[i] = replacement;
                didReplace = true;
            }
        }
        return (didReplace ? new String(res) : fileName);
    }
}
