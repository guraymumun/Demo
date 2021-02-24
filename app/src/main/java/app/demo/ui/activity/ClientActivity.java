package app.demo.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import app.demo.R;
import app.demo.asynctask.GetImageAsyncTask;
import app.demo.util.Constants;

public class ClientActivity extends AppCompatActivity {

    private EditText editTextAddress;
    private Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        initUI();
    }

    private void initUI() {
        editTextAddress = findViewById(R.id.address);
        buttonConnect = findViewById(R.id.connect);

        buttonConnect.setOnClickListener(v -> new GetImageAsyncTask(ClientActivity.this, editTextAddress.getText().toString(), Constants.SERVER_PORT).execute());
    }
}