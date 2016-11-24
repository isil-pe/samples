package com.isil.templateapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;

import java.text.DateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private View btnLogInDropbox,btnUpload;
    private TextView tviName;
    private TextView tviEmail;
    private TextView tviType;
    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;
    private String mPath;
    private String accessToken=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogInDropbox= findViewById(R.id.btnLogInDropbox);
        btnUpload= findViewById(R.id.btnUpload);
        tviName= (TextView) findViewById(R.id.tviName);
        tviEmail= (TextView) findViewById(R.id.tviEmail);
        tviType= (TextView) findViewById(R.id.tviType);

        btnLogInDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.startOAuth2Authentication(MainActivity.this, "svc2vr7355m1hos");
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFilePicker();
            }
        });

        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accessToken==null){
            accessToken= Auth.getOAuth2Token();
            if(accessToken!=null){
                loadData();
            }
            Log.v(TAG , "accessToken "+accessToken);
        }
    }


    private void loadData(){
        DropboxClientFactory.init(accessToken);
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
               // ((TextView) findViewById(R.id.email_text)).setText(result.getEmail());
               // ((TextView) findViewById(R.id.name_text)).setText(result.getName().getDisplayName());
               // ((TextView) findViewById(R.id.type_text)).setText(result.getAccountType().name());

                Log.v(TAG, "email "+result.getEmail());
                Log.v(TAG, "name "+result.getName().getDisplayName());
                Log.v(TAG, "type "+result.getAccountType().name());
                String email= result.getEmail();
                String name= result.getName().getDisplayName();
                String type= result.getAccountType().name();
                tviName.setText("Name "+name );
                tviEmail.setText("Email "+email);
                tviType.setText("Type "+type);
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }


    private void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // This is the result of a call to launchFilePicker
                uploadFile(data.getData().toString());
            }
        }
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
                        .show();

                // Reload the folder
                //loadData();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(MainActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, mPath);
    }
}
