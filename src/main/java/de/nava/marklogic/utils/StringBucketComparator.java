package de.nava.marklogic.utils;

import java.util.Comparator;

/**
 * Compares string taking
 * <ul>
 *   <li>numerical ordering</li>
 *   <li>"greater/less then" symbols into account</li>
 * </ul>
 *
 * @author Niko Schmuck
 */
public class StringBucketComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        String s1 = cutOffLeading(o1);
        String s2 = cutOffLeading(o2);

        if (s1.length() > 0 && s2.length() > 0) {
            if (Character.isDigit(s1.charAt(0)) && Character.isDigit(s2.charAt(0))) {
                try {
                    int i1 = Integer.parseInt(getStartingDigits(s1));
                    int i2 = Integer.parseInt(getStartingDigits(s2));
                    if (i1 != i2) {
                        return i1 - i2;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return s1.compareTo(s2);
    }

    private String getStartingDigits(String str) {
        String result = "";
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                result += c;
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Gets rid of a greater then / less then at the very beginning
     */
    private String cutOffLeading(String s) {
        return (s != null) && (s.startsWith(">") || s.startsWith("<")) ?
                s.substring(1) : s;
    }

}
