/**
 * Author red1@red1.org
 * Licensed Free as in Freedom. Anyone who does not share back will have bad karma.
 */
package org.red1.erp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by red1 on 1/27/17.
 */

public class asyncDBHandling extends AsyncTask {

    private static Activity activity;
    private SharedPreferences sp;
    private List<String>scannedList;

    public asyncDBHandling(ScanWrite scanWrite) {
        activity = (Activity)scanWrite;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        sp = (SharedPreferences)params[0];
        scannedList = (List)params[1];
        try {
            updateDB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateDB() throws ClassNotFoundException, SQLException {
        int OKcnt = 0;
        int NotOKcnt = 0;
        Class.forName("org.postgresql.Driver");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    sp.getString("URL","jdbc:postgresql://localhost:5432/adempiere"),
                    sp.getString("User","adempiere"), sp.getString("Pass","adempiere")
            );
            for (String scanned : scannedList) {
                String[] splits = scanned.split(",");
                String table = "BedRegistration";
                String set = "DocStatus";
                String set2 = "IP";
                String where = "Name";
                String where2 = scanned;
                if (splits.length > 4) {
                    table = splits[0].equals("") ? table : splits[0];
                    set = splits[1].equals("") ? set : splits[1];
                    set2 = splits[2].equals("") ? set2 : splits[2];
                    where = splits[3].equals("") ? where : splits[3];
                    where2 = splits[4].equals("") ? where2 : splits[4];
                }
                String SQL = "UPDATE " + table + " SET " + set + " ='" + set2 + "' WHERE " + where + "='" + where2 + "'";
                Statement pstmt = conn.createStatement();
                try {
                    int i = pstmt.executeUpdate(SQL);
                    System.out.println(SQL+"DB result = "+i);
                    OKcnt++;
                } catch (SQLException e){
                    NotOKcnt++;
                    continue;
                }
           }
           /* int i = pstmt.executeUpdate("UPDATE BedRegistration SET DocStatus = 'IP' WHERE Name='PatientOne'");
            System.out.println("DB result = "+i);*/
        }
        finally{
            if (conn!=null){
                conn.close();
                final int OK = OKcnt;
                final int NotOK = NotOKcnt;
                String msg = "";
                if (OKcnt+NotOKcnt==0)
                    msg = "No Records Scanned";
                if(OKcnt>0)
                    msg = OKcnt + " OK";
                if (NotOKcnt>0)
                    msg = NotOKcnt + " FAIL / others OK";
                final String popup = msg;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(activity, popup, Toast.LENGTH_LONG);
                        LinearLayout toastLayout = (LinearLayout) toast.getView();
                        TextView toastTV = (TextView) toastLayout.getChildAt(0);
                        toastTV.setTextSize(30);
                        toast.show();
                    }
                });
            }
        }
    }
}
