// Copyright 2010 Google Inc. All Rights Reserved.

package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ArrayAdapterTest {
    private ArrayAdapter<Integer> arrayAdapter;

    @Before public void setUp() throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        arrayAdapter = new ArrayAdapter<Integer>(Robolectric.application, 0, list);
    }

    @Test
    public void verifyContext() {
        assertEquals(Robolectric.application, arrayAdapter.getContext());
    }

    @Test
    public void verifyListContent() {
        assertEquals(3, arrayAdapter.getCount());
        assertEquals(new Integer(1), arrayAdapter.getItem(0));
        assertEquals(new Integer(2), arrayAdapter.getItem(1));
        assertEquals(new Integer(3), arrayAdapter.getItem(2));
    }

    @Test
    public void usesTextViewResourceIdToSetTextWithinListItemView() throws Exception {
        ListView parent = new ListView(Robolectric.application);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Robolectric.application, R.layout.main, R.id.title, new String[] { "first value" });
        View listItemView = arrayAdapter.getView(0, null, parent);
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        assertEquals("first value", titleTextView.getText().toString());
    }
    
    @Test
    public void hasTheCorrectConstructorResourceIDs() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Robolectric.application, R.id.title, new String[] { "first value" });
        
        //this assertion may look a little backwards since R.id.title is labeled
        //textViewResourceId in the constructor parameter list, but the output is correct.
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter).getResourceId()==R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter).getTextViewResourceId()!=R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter).getTextViewResourceId()==0);
        
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(Robolectric.application, R.id.title);
        
        //this assertion may look a little backwards since R.id.title is labeled
        //textViewResourceId in the constructor parameter list, but the output is correct.
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter2).getResourceId()==R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter2).getTextViewResourceId()!=R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter2).getTextViewResourceId()==0);
        
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(Robolectric.application, R.id.title, Arrays.asList(new String[] { "first value" }));
        
        //this assertion may look a little backwards since R.id.title is labeled
        //textViewResourceId in the constructor parameter list, but the output is correct.
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter3).getResourceId()==R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter3).getTextViewResourceId()!=R.id.title);
        Assert.assertTrue(Robolectric.shadowOf(arrayAdapter3).getTextViewResourceId()==0);
    }
}
