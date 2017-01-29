/**
 * Author red1@red1.org
 * Licensed Free as in Freedom. Anyone who does not share back will have bad karma.
 */
package org.red1.erp;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ScanWrite extends AppCompatActivity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    List<String> scannedList = new ArrayList<String>();
    ArrayAdapter itemsAdapter = null;
    SharedPreferences sp;
    public static final String ERP_DB_CONNECTOR = "ERP_DB_Connector" ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // We get the ListView component from the layout
        ListView lv = (ListView) findViewById(R.id.ListView);
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scannedList);
        lv.setAdapter(itemsAdapter);

        sp = getSharedPreferences(ERP_DB_CONNECTOR, Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_DB_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("DB Connector");

            Context context = builder.getContext();
            final LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText URLBox = new EditText(context);
            URLBox.setHint("DB Host URL");
            URLBox.setText(sp.getString("URL","jdbc:postgresql://10.0.2.2:5432/adempiere"), TextView.BufferType.EDITABLE);
            layout.addView(URLBox);

            final EditText userBox = new EditText(context);
            userBox.setHint("User");
            userBox.setText(sp.getString("User","adempiere"), TextView.BufferType.EDITABLE);
            layout.addView(userBox);

            final EditText passwordBox = new EditText(context);
            passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordBox.setHint("Password");
            passwordBox.setText(sp.getString("Pass","adempiere"), TextView.BufferType.EDITABLE);
            layout.addView(passwordBox);

            builder.setView(layout);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String url = URLBox.getEditableText().toString();
                    String user = userBox.getEditableText().toString();
                    String pass = passwordBox.getEditableText().toString();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("URL", url);
                    editor.putString("User", user);
                    editor.putString("Pass", pass);
                    editor.commit();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
        } else if(id == R.id.action_about){
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Best ERP Plugins In The World");
            builder.setView(inflater.inflate(R.layout.about_box, null));
            builder.setNegativeButton("Got It", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void scanBar(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_FORMATS", "CODE_39,CODE_93,CODE_128,DATA_MATRIX,ITF,CODABAR,EAN_13,EAN_8,UPC_A,QR_CODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(this, "No BarScanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void updateDB(View v) {
        asyncDBHandling as = new asyncDBHandling(this);
        as.execute(sp,scannedList);
        //new asyncDBHandling().execute(sp,scannedList);
    }

    private static AlertDialog showDialog(final AppCompatActivity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                scannedList.add(contents);
                itemsAdapter.notifyDataSetChanged();
                //Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                //toast.show();
            }
        }
    }

}