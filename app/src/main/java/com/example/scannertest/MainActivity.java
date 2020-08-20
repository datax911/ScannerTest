package com.example.scannertest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn;
    TextView tv;
    Button generateButton;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private ListView listView;
    private ArrayList<String> partList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);







        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }

        tv = findViewById(R.id.textView2);

        listView = findViewById(R.id.listView);

        generateButton = (Button) findViewById(R.id.button2);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenQrActivity.class);
                startActivity(intent);
            }
        });






        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseHelper = new DatabaseHelper(MainActivity.this, "suivi_prod.db");
                database = databaseHelper.getReadableDatabase();

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scanner un MO");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setOrientationLocked(false);
                integrator.setTorchEnabled(true);
                integrator.initiateScan();
            }
        });





    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                tv.setText(result.getContents().toString());
                getPartList(result.getContents().toString());

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, partList);
                listView.setAdapter(arrayAdapter);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            listView.setAdapter(null);
        }
    }


    public ArrayList<String> getPartList(String mo) {
        partList = new ArrayList<>();

        String query="SELECT * FROM SauvegardeimportMO WHERE [order] = '" + mo + "'";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do{
                partList.add(cursor.getString(cursor.getColumnIndex("MaterielDescription2")) + "\n" + cursor.getString(cursor.getColumnIndex("Materiel")) + "\n" + cursor.getString(cursor.getColumnIndex("SortString")));
            } while (cursor.moveToNext());
        }

        return partList;
    }
}
