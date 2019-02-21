package com.uchain.core.consensus;

import java.util.Comparator;

public class MapKeyComparator<K> implements Comparator<K>{
    private String sortType;

    public MapKeyComparator(String sortType) {
        this.sortType = sortType;
    }

    @Override
    public int compare(K o1, K o2) {
        if ("reverse".equals(sortType)) {
            if ((o1 instanceof String) && (o2 instanceof String)) {
                return ((String) o2).compareTo((String) o1);
            } else if (o1 instanceof Integer && o2 instanceof Integer) {
                return ((Integer) o2).compareTo((Integer) o1);
            } else if (o1 instanceof Boolean && o2 instanceof Boolean) {
                return ((Boolean) o2).compareTo((Boolean) o1);
            }
            return 0;
        }else {
            if(o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            } else if(o1 instanceof Integer && o2 instanceof Integer) {
                return ((Integer) o1).compareTo((Integer) o2);
            }else if(o1 instanceof Boolean && o2 instanceof Boolean) {
                return ((Boolean) o1).compareTo((Boolean) o2);
            }
            return 0;
        }
    }
}