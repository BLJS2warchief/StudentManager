package com.meruvas.studentmanager;

import android.app.Activity;
import android.os.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.database.sqlite.*;
import android.database.*;
import android.text.*;
import android.content.Intent;

import java.io.File;
import java.util.*;

public class StudentManager extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

   ArrayList<String> resultItems;
   ArrayAdapter<String> resultsAdapter;
   ArrayAdapter<CharSequence> spnrAdapter;
   String dbName;
   String[] searchItems = {"name", "rollNo", "address"};
   TextView txtNameValue, txtAddressValue, txtRollValue, txtStatus;
   EditText txtSearch;
   ListView listView;
   Button btnPrev, btnNext;
   Spinner spinner;
   int dbCurrentPos, selectedIndex;
   boolean newSearch;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      initGUI();

      dbName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/manager/students.db");

      Intent i = getIntent();
      if (i.getAction() == "justDisplay") {
         Bundle extras = getIntent().getExtras();
         int ID = extras.getInt("ID");
         if (ID != 0) {
            displayInfo(ID);
         }
      }
   }
   

/**************************************************************************/
/*  click handler for search, next, previous buttons                      */
/**************************************************************************/
   public void onClick(View v) {
      switch (v.getId()) {
         case  R.id.btnSearch:
            fetchFromDB();
            resultsAdapter.notifyDataSetChanged();
//            Toast.makeText(getApplicationContext(), "selection=" + selectedIndex, Toast.LENGTH_LONG).show();
            if (selectedIndex == -1){
                  selectedIndex = 0;
                  displayInfo(0);
            }
            break;
         case  R.id.btnNext:
            if(selectedIndex < resultItems.size()-1){
               listView.setSelection(selectedIndex+1);
               selectedIndex = selectedIndex+1;
               displayInfo(selectedIndex);
            }
            break;
         case  R.id.btnPrev:
            if(selectedIndex > 0){
               listView.setSelection(selectedIndex-1);
               selectedIndex = selectedIndex-1;
               displayInfo(selectedIndex);
            }
            break;
      }
   }

/**************************************************************************/
/*  Item click handler for list view                                      */
/**************************************************************************/
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      selectedIndex = position;
      displayInfo(position);
   }

/**************************************************************************/
/*  Item selection handler for spinner                                    */
/**************************************************************************/
   public void onItemSelected(AdapterView parent, View view, int position, long id) {
      newSearch = true;
      updateStatusInfo("New Search");
   }

/**************************************************************************/
/*  Using the position of selection, display the                          */
/*             information of the student from the database               */
/**************************************************************************/
   public void displayInfo(int position){
      int name, address, roll;
      SQLiteDatabase dbStudents;
      Cursor cursor;

//      Toast.makeText(getApplicationContext(), resultItems.get(position)+"", Toast.LENGTH_SHORT).show();
      if (! new File(dbName).exists()){
         Toast.makeText(getApplicationContext(), "students db file not found at : " + dbName, Toast.LENGTH_SHORT).show();
         return;
      }

      dbStudents = SQLiteDatabase.openDatabase(dbName, null, SQLiteDatabase.OPEN_READONLY);
      if (dbStudents == null){
         Toast.makeText(getApplicationContext(), "Error opening DB", Toast.LENGTH_SHORT).show();
         return;
      }
      try{
         cursor = dbStudents.rawQuery("SELECT * FROM students WHERE id=" + (position+1) + ";", null);
         if (cursor.moveToFirst()) {
            name = cursor.getColumnIndex("name");
            roll = cursor.getColumnIndex("rollNo");
            address = cursor.getColumnIndex("address");
            do {
               clearInfoPanel();
               txtRollValue.setText(cursor.getString(roll));
               txtNameValue.setText(cursor.getString(name));
               txtAddressValue.setText(new String(cursor.getBlob(address)));
            } while (cursor.moveToNext());
         }
         cursor.close();
      }
      catch(Exception e){
         Toast.makeText(getApplicationContext(), e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
      }

      dbStudents.close();

      btnNext.setEnabled(true);
      btnPrev.setEnabled(true);
   
      if(selectedIndex == resultItems.size()-1)
         btnNext.setEnabled(false);
      if(selectedIndex == 0)
         btnPrev.setEnabled(false);
   }

/**************************************************************************/
/*  Using dbCurrentPos and text in search bar, display the                */
/*                                  list of students from the database    */
/**************************************************************************/
   public void fetchFromDB(){
      SQLiteDatabase dbStudents;
      Cursor cursor;
      String sqlQuery;
      int name, id, resultCount;

      if (! new File(dbName).exists()){
         Toast.makeText(getApplicationContext(), "students db file not found at : " + dbName, Toast.LENGTH_SHORT).show();
         return;
      }

      dbStudents = SQLiteDatabase.openDatabase(dbName, null, SQLiteDatabase.OPEN_READONLY);
      if (dbStudents == null){
         Toast.makeText(getApplicationContext(), "Error opening DB", Toast.LENGTH_SHORT).show();
         return;
      }
      
      if (newSearch){
         resultItems.clear();
         resultsAdapter.notifyDataSetChanged();
         updateStatusInfo("Displaying first 5 results");
         clearInfoPanel();
         dbCurrentPos = -1;
      } else {
            updateStatusInfo("Displaying next 5 search ");
      }
      resultCount = 0;
      try{
         sqlQuery = "SELECT id, name FROM students WHERE id>" + dbCurrentPos;
         if (!TextUtils.isEmpty(txtSearch.getText()))
            sqlQuery += " AND " + searchItems[spinner.getSelectedItemPosition()] + " like '%" + txtSearch.getText() + "%' COLLATE NOCASE";
         sqlQuery +=  " ORDER BY id LIMIT 5;";
         cursor = dbStudents.rawQuery(sqlQuery, null);
         if (cursor.moveToFirst()) {
            id = cursor.getColumnIndex("id");
            name = cursor.getColumnIndex("name");
            do {
               resultItems.add(cursor.getString(name));
               dbCurrentPos = cursor.getInt(id);
               resultCount++;
            } while (cursor.moveToNext());
         }
         cursor.close();
      }
      catch(Exception e){
         Toast.makeText(getApplicationContext(), e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
      }

      dbStudents.close();
      if (resultCount > 0){
         if (selectedIndex == -1)
            listView.setSelection(0);
         listView.requestFocus();
         newSearch = false;
     } else {
         updateStatusInfo("No results found");
     }
     
     btnNext.setEnabled(true);
     btnPrev.setEnabled(true);
  
     if(selectedIndex == resultItems.size()-1)
        btnNext.setEnabled(false);
     if(selectedIndex == 0)
        btnPrev.setEnabled(false);
   }

/**************************************************************************/
/*  Update status in the status bar                                       */
/**************************************************************************/
   public void updateStatusInfo(String updateString){
      txtStatus.setText(updateString);
  }

/**************************************************************************/
/*  clear the displayed student information if a new search is requested  */
/**************************************************************************/
   public void clearInfoPanel(){
      txtNameValue.setText("");
      txtAddressValue.setText("");
      txtRollValue.setText("");
   }

/**************************************************************************/
/*  Initialize the Graphical user inteface                                */
/**************************************************************************/
   public void initGUI(){
      ImageButton btnSearch;

      spinner = (Spinner) findViewById(R.id.spnrSearch);
      spnrAdapter = ArrayAdapter.createFromResource(this, R.array.spnrItems, android.R.layout.simple_spinner_item);
      spnrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(spnrAdapter);
      
      resultItems = new ArrayList<String>();
      resultsAdapter = new ArrayAdapter<String>(this, R.layout.list, resultItems);
      
      listView = (ListView) findViewById(R.id.lstResult);
      listView.setAdapter(resultsAdapter);

      btnSearch = (ImageButton)findViewById(R.id.btnSearch);
      btnNext = (Button) findViewById(R.id.btnNext);
      btnPrev = (Button) findViewById(R.id.btnPrev);
      txtStatus = (TextView) findViewById(R.id.txtStatus);
      txtRollValue = (TextView) findViewById(R.id.txtRollValue);
      txtNameValue = (TextView) findViewById(R.id.txtNameValue);
      txtAddressValue = (TextView) findViewById(R.id.txtAddressValue);
      txtSearch = (EditText) findViewById(R.id.txtSearch);

      btnSearch.setOnClickListener(this);
      btnNext.setOnClickListener(this);
      btnNext.setEnabled(false);
      btnPrev.setOnClickListener(this);
      btnPrev.setEnabled(false);
      txtSearch.addTextChangedListener(this);
      spinner.setOnItemSelectedListener(this);
      listView.setOnItemClickListener(this);

      dbCurrentPos = -1;
      selectedIndex = -1;
      newSearch = true;
   }

   public void onNothingSelected(AdapterView<?> arg0) {
   }
   public void onTextChanged(CharSequence s, int start, int before, int count) {
      newSearch = true;
      updateStatusInfo("New Search");
   }
   public void afterTextChanged (Editable s){
   }
   public void beforeTextChanged (CharSequence s, int start, int count, int after){
   }
}