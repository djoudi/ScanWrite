/**
 * Author red1@red1.org
 * Licensed Free as in Freedom. Anyone who does not share back will have bad karma.
 */
package org.red1.erp;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by red1 on 1/27/17.
 */

public class asyncDBHandling extends AsyncTask {

    private SharedPreferences sp;
    private List<String>scannedList;

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
                } catch (SQLException e){
                    continue;
                }


            }
           /* int i = pstmt.executeUpdate("UPDATE BedRegistration SET DocStatus = 'IP' WHERE Name='PatientOne'");
            System.out.println("DB result = "+i);*/
        }
        finally{
            conn.close();
        }

    }
}
