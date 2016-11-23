package ru.ncom.groupingrvexample.launcher;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.ncom.groupingrvexample.BaseActivity;


/**
 * Created by ncom on 05.10.2016.
 */

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(..); // override default view

        ArrayAdapter adapter = new ArrayAdapter<String>(
                this, // Context.
                android.R.layout.simple_list_item_1,  // Specify the row template to use
                new String[] {"Grouping Recycler View : Keep data."
                        ," Grouping Recycler View : Regenerate data"}
        );
        // Bind to our new adapter.
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, BaseActivity.class);
        switch (position) {
            case 1:
                Bundle b = new Bundle();
                b.putInt(BaseActivity.REGENERATE, 1); //
                i.putExtras(b); //
            break;
        }
        startActivity(i);

    }
}
