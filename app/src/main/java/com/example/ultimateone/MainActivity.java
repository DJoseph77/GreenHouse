package com.example.ultimateone;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ultimateone.Model.DHT11Data;
import com.example.ultimateone.Model.DataResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // UI components
    private TextView ResultTextview,welcometxt, humiditeTextView, humiditeSoilTextView, temperatureTextView, adviceVon, progressTempuratureTxtView, adviceHum, progressHumdityTxtPomp;
    private Spinner spinner;
    private ProgressBar progressTemperature, progressHumiditySoil, progressHumidityEnv, progressBarVon, progressTemperatureVon, progressHumidityPomp;
    private RelativeLayout relGeneralCondition, relVontilateur, relPompe, relIa;
    private Button runVontilateur, runPompe, BSelectImage;
    private ImageView IVPreviewImage;
    private Animation animationProgress, animationwelcome;


    private static final int SELECT_PICTURE = 200;

    // Firebase components
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeUIComponents();
        initializeFirebase();
        setupSpinner();
        setupImageChooser();

        animationbegin();

        retrieveDataFromFirebase();
    }

    private void initializeUIComponents() {
        ResultTextview=findViewById(R.id.resultdisease);
        relIa = findViewById(R.id.Ia);
        BSelectImage = findViewById(R.id.buttonChoose);
        IVPreviewImage = findViewById(R.id.Planeteimg);
        runPompe = findViewById(R.id.PompeBtn);
        relPompe = findViewById(R.id.Pompe);
        progressHumdityTxtPomp = findViewById(R.id.progresshumdityTxtPompe);
        progressHumidityPomp = findViewById(R.id.progresshumdityPompe);
        adviceHum = findViewById(R.id.advicePompe);
        progressHumidityPomp = findViewById(R.id.progresshumdityPompe);
        progressTemperatureVon = findViewById(R.id.progresstempuratureVon);
        progressTempuratureTxtView = findViewById(R.id.progressTempuraturetxtVon);
        adviceVon = findViewById(R.id.adviceVon);
        animationProgress = AnimationUtils.loadAnimation(this, R.anim.combine_rotate_fade);
        animationwelcome = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        welcometxt = findViewById(R.id.welcometxt);
        spinner = findViewById(R.id.customSpinner);
        progressTemperature = findViewById(R.id.progresstempurature);
        progressHumiditySoil = findViewById(R.id.progresshumiditysoil);
        progressHumidityEnv = findViewById(R.id.progresshumidityenv);
        humiditeTextView = findViewById(R.id.progresshumiditytxt);
        humiditeSoilTextView = findViewById(R.id.progresshumiditysoiltxt);
        temperatureTextView = findViewById(R.id.progressTempuraturetxt);
        relGeneralCondition = findViewById(R.id.roomcondition);
        relVontilateur = findViewById(R.id.Vontilateur);
        progressBarVon = findViewById(R.id.progresstempuratureVon);
        runVontilateur = findViewById(R.id.VontilateurBtn);
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupSpinner() {
        String[] items = {"Room condition", "Vontilateur", "Pompe d'eau", "Planete Conditions"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items[position];
                switch (selectedItem) {
                    case "Room condition":
                        animationGeneralCondition();
                        break;
                    case "Vontilateur":
                        animationVontilateur();
                        break;
                    case "Pompe d'eau":
                        animationPomp();
                        break;
                    case "Planete Conditions":
                        animationIa();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupImageChooser() {
        BSelectImage.setOnClickListener(v -> imageChooser());
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                IVPreviewImage.setImageURI(selectedImageUri);
                uploadImageToFirebase(selectedImageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("predictions");
        String uniqueID = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(uniqueID + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("predictions/image");
                    databaseReference.setValue(downloadUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show());
    }

    private void retrieveDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataResponse dataResponse = dataSnapshot.getValue(DataResponse.class);

                if (dataResponse != null) {
                    DHT11Data dht11Data = dataResponse.getDHT11();
                    if (dataResponse != null && dataResponse.getPredictions() != null) {
                        // Update the TextView with the "result" value from the database
                        String result = dataResponse.getPredictions().getResult();
                        ResultTextview.setText("Etat : "+result);
                    }

                    if (dht11Data != null) {
                        updateUIWithDHT11Data(dht11Data);
                        updateProgressBar(dht11Data);
                        changeAdvice(dht11Data);
                    }
                }

                runVontilateurDependData(dataResponse);
                runPompDependData(dataResponse);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error retrieving data from Firebase", databaseError.toException());
            }
        });
    }

    private void updateUIWithDHT11Data(DHT11Data dht11Data) {
        humiditeTextView.setText(dht11Data.getHumidite() + "%");
        humiditeSoilTextView.setText(dht11Data.getHumiditesoil() + "%");
        temperatureTextView.setText(dht11Data.getTemperature() + "°C");
        progressTempuratureTxtView.setText(dht11Data.getTemperature() + "°C");
        progressHumdityTxtPomp.setText(dht11Data.getHumidite() + "%");
    }

    private void updateProgressBar(DHT11Data dht11Data) {
        int humidite = dht11Data.getHumidite();
        int humiditesoil = dht11Data.getHumiditesoil();
        int temperature = (int) dht11Data.getTemperature();

        progressTemperature.setProgress(temperature, true);
        progressHumidityEnv.setProgress(humidite, true);
        progressHumiditySoil.setProgress(humiditesoil, true);
        progressTemperatureVon.setProgress(temperature, true);
        progressHumidityPomp.setProgress(humidite, true);
    }

    private void changeAdvice(DHT11Data dht11Data) {
        if (dht11Data.getTemperature() > 28) {
            adviceVon.setText("The temperature is high !");
            adviceVon.setTextColor(Color.RED);
        } else {
            adviceVon.setText("Temperature is Good");
            adviceVon.setTextColor(Color.BLUE);
        }

        if (dht11Data.getHumidite() > 75) {
            adviceHum.setText("The humidity is high !");
            adviceHum.setTextColor(Color.RED);
        } else {
            adviceHum.setText("The humidity is Good");
            adviceHum.setTextColor(Color.BLUE);
        }
    }

    private void runVontilateurDependData(DataResponse dataResponse) {
        DatabaseReference vontilateurRef = databaseReference.child("vontilateurRun");
        if (dataResponse != null && "0".equals(dataResponse.getVontilateurRun())) {
            runVontilateur.setText("Run Vontilateur");
            runVontilateur.setBackgroundColor(Color.BLUE);
        } else {
            runVontilateur.setText("Stop Vontilateur");
            runVontilateur.setBackgroundColor(Color.RED);
        }
        runVontilateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataResponse != null && "1".equals(dataResponse.getVontilateurRun())) {
                    runVontilateur.setText("Run Vontilateur");
                    runVontilateur.setBackgroundColor(Color.BLUE);
                    vontilateurRef.setValue("0"); // Change value to 1
                } else {
                    runVontilateur.setText("Stop Vontilateur");
                    runVontilateur.setBackgroundColor(Color.RED);
                    vontilateurRef.setValue("1"); // Change value to 0
                }
            }
        });
    }

    private void runPompDependData(DataResponse dataResponse) {
        DatabaseReference pompRef = databaseReference.child("pompeRun");
        if (dataResponse != null && "0".equals(dataResponse.getPompeRun())) {
            runPompe.setText("Run Pompe");
            runPompe.setBackgroundColor(Color.BLUE);
        } else {
            runPompe.setText("Stop Pompe");
            runPompe.setBackgroundColor(Color.RED);
        }
        runPompe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataResponse != null && "1".equals(dataResponse.getPompeRun())) {
                    runPompe.setText("Run Pompe");
                    runPompe.setBackgroundColor(Color.RED);
                    pompRef.setValue("0"); // Change value to 1
                } else {
                    runPompe.setText("Stop Pompe");
                    runPompe.setBackgroundColor(Color.BLUE);
                    pompRef.setValue("1"); // Change value to 0
                }
            }
        });
    }


    public void animationbegin() {
        spinner.startAnimation(animationwelcome);
        welcometxt.startAnimation(animationwelcome);
    }

    public void animationGeneralCondition() {
        visibiltyVontilateur(false);
        visibiltyPompe(false);
        visibiltyIa(false);
        visibiltyGeneralCondition(true);
        progressHumidityEnv.startAnimation(animationProgress);
        progressTemperature.startAnimation(animationProgress);
        progressHumiditySoil.startAnimation(animationProgress);
    }

    public void animationVontilateur() {
        visibiltyGeneralCondition(false);
        visibiltyPompe(false);
        visibiltyIa(false);
        visibiltyVontilateur(true);
        progressBarVon.startAnimation(animationProgress);
        runVontilateur.startAnimation(animationwelcome);
    }

    public void animationPomp() {
        visibiltyGeneralCondition(false);
        visibiltyVontilateur(false);
        visibiltyIa(false);
        visibiltyPompe(true);
        progressHumidityPomp.startAnimation(animationProgress);
        runPompe.startAnimation(animationwelcome);
    }

    public void animationIa() {
        visibiltyGeneralCondition(false);
        visibiltyPompe(false);
        visibiltyVontilateur(false);
        visibiltyIa(true);
        progressBarVon.startAnimation(animationProgress);
        runVontilateur.startAnimation(animationwelcome);
    }
    public void visibiltyVontilateur(boolean b) {
        relVontilateur.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void visibiltyGeneralCondition(boolean b) {
        relGeneralCondition.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void visibiltyPompe(boolean b) {
        relPompe.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void visibiltyIa(boolean b) {
        relIa.setVisibility(b ? View.VISIBLE : View.GONE);
    }

}

