package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.location.Address;
import android.os.Bundle;

import com.tinyadvisor.geoadvisor.Constants;

import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;


/**
 * Created by tkhakimyanov on 12.07.2016.
 */

public class ResultsCollection {
    public static final long WRITE_INTERVAL = 60 * 1000; // 60 seconds
    public static final long TRACKED_PERIOD = 8 * 60 * 60 * 1000; // 8 hours

    HashMap<String, Integer> activityMap = new HashMap<>();
    HashMap<String, Integer> addressMap = new HashMap<>();

    void add(ActivityResult activityResult, AddressResult addressResult) {
        if(activityResult.getDefined()) {
            String activity = activityResult.getActivityAsText();
            Integer value = 1;
            if (activityMap.containsKey(activity))
                value = activityMap.get(activity) + 1;
            activityMap.put(activity, value);
        }

        if(addressResult.getDefined()) {
            Integer value = 1;
            String address = addressResult.getAddressAsText();
            if (addressMap.containsKey(address))
                value = addressMap.get(address) + 1;
            addressMap.put(address, value);
        }
    }

    private static ArrayList<String> sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        ArrayList<String> sortedEntries = new ArrayList<>();
        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        for (Object aList : list) {
            Map.Entry entry = (Map.Entry) aList;
            sortedEntries.add(String.format("%s %d", entry.getKey(), entry.getValue()));
        }
        return sortedEntries;
    }

    void saveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putStringArray(Constants.STATS_TOP_LOCATIONS, sortByValues(addressMap).toArray(new String[addressMap.keySet().size()]));
        savedInstanceState.putStringArray(Constants.STATS_TOP_ACTIVITIES, sortByValues(activityMap).toArray(new String[activityMap.keySet().size()]));
    /*    Map<String, Long> activitiesCountMap = activityCollections.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        ArrayList<String> topActivities = new ArrayList<String>();
        activitiesCountMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(3).forEachOrdered(e -> topActivities.add(e.getKey()));

        Map<String, Long> addressCountMap = addressCollections.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        ArrayList<String> topAddresses = new ArrayList<String>();
        addressCountMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(3).forEachOrdered(e -> topActivities.add(e.getKey()));

        savedInstanceState.putStringArray(Constants.STATS_TOP_LOCATIONS, topActivities.toArray(new String[topActivities.size()]));
        savedInstanceState.putStringArray(Constants.STATS_TOP_ACTIVITIES, topAddresses.toArray(new String[topAddresses.size()]));
    */
    }
}
