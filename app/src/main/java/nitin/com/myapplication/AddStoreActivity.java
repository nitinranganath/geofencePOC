package nitin.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddStoreActivity extends Activity implements OnClickListener {

    private Button addTodoBtn;
    private EditText subjectEditText;
    private EditText latitudeET;
    private EditText longitudeET;

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Add Record");

        setContentView(R.layout.activity_add_record);

        subjectEditText = (EditText) findViewById(R.id.subject_edittext);
        latitudeET = (EditText) findViewById(R.id.et_latitude);
        longitudeET = (EditText) findViewById(R.id.et_longitude);

        addTodoBtn = (Button) findViewById(R.id.add_record);

        dbManager = new DBManager(this);
        dbManager.open();
        addTodoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_record:

                final String name = subjectEditText.getText().toString();
                final double lat = Double.parseDouble(latitudeET.getText().toString());
                final double lng = Double.parseDouble(longitudeET.getText().toString());

                dbManager.insert(name, lat, lng);

                Intent main = new Intent(AddStoreActivity.this, HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(main);
                break;
        }
    }

}