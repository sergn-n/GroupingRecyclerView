package ru.ncom.groupingrvadapter;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {


    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    enum Callbacks {
        onClear, onDataSorted, onGroupedItemAdded, onUngroupedItemsAdded, onHeaderAdded
        , onHeaderRemoved,onGroupedItemRemoved,onUngroupedItemRemoved
        ,LAST
    }

    @Test
    public void testGroupedList01() throws Exception {
        final int[] callbacksRun = new int[Callbacks.LAST.ordinal()];
        // sample callback, creates sample comparator for String
        GroupedList.Callback<String> cbFirstChar = new GroupedList.Callback<String>() {
            @Override
            public ComparatorGrouper<String> getComparatorGrouper(String sortField) {
                return new ComparatorGrouper<String>() {
                    @Override
                    public String getSortKey(String m) {
                        return m.substring(0,1);
                    }
                    // no title specified - it's the same as sort key.
                };
            }

            // count method calls

            @Override
            public void onClear() {
                callbacksRun[Callbacks.onClear.ordinal()]++;
            }

            @Override
            public void onDataSorted(GroupedList<String> gl) {
                callbacksRun[Callbacks.onDataSorted.ordinal()]++;
            }

            @Override
            public void onGroupedItemAdded(int hpos, String item, int pos) {
                callbacksRun[Callbacks.onGroupedItemAdded.ordinal()]++;
            }

            @Override
            public void onUngroupedItemsAdded(List<String> items) {
                callbacksRun[Callbacks.onUngroupedItemsAdded.ordinal()]++;
            }

            @Override
            public void onHeaderAdded(Header<String> h, int pos) {
                // Fires only at add(), addAll() calls onDataSorted()
                callbacksRun[Callbacks.onHeaderAdded.ordinal()]++;
            }

            @Override
            public void onHeaderRemoved(int hpos) {
                callbacksRun[Callbacks.onHeaderRemoved.ordinal()]++;
            }

            @Override
            public void onGroupedItemRemoved(int hpos, int tpos) {
                callbacksRun[Callbacks.onGroupedItemRemoved.ordinal()]++;
            }

            @Override
            public void onUngroupedItemRemoved(int tpos) {
                callbacksRun[Callbacks.onUngroupedItemRemoved.ordinal()]++;
            }
        };

        GroupedList<String> gl = new GroupedList<>(cbFirstChar);
        gl.addAll("ab","bc");
        assertEquals(2,gl.size());
        gl.add("ax");
        gl.add("ax");
        gl.remove("ab");
        assertEquals(3,gl.size());
        // no matter what field, cbFirstChar only implements sort/group by 1st char of the string
        // doesn't call callback
        gl.doSort("");
        assertEquals(0,callbacksRun[Callbacks.onDataSorted.ordinal()]);

        // calls callback
        gl.sort("");
        assertEquals(1,callbacksRun[Callbacks.onDataSorted.ordinal()]);
        assertEquals(2,gl.getHeaders().size());

        gl.remove("ax");
        assertEquals(1,callbacksRun[Callbacks.onGroupedItemRemoved.ordinal()]);

        gl.addAll("px","py","qx");
        assertEquals(4,gl.getHeaders().size());   }
}