package com.example.campusconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class update_std extends AppCompatActivity {

    EditText std_id,std_name,std_no;
    Spinner std_class;
    Button std_update_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_std);
        LoadingDialog loadingDialog = new LoadingDialog(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        std_id=findViewById(R.id.student_id);
        std_name=findViewById(R.id.student_name);
        std_no=findViewById(R.id.student_no);
        std_class=findViewById(R.id.class_list);

        std_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(std_no.length() ==10))
                {
                    std_no.setError("Invalid Mobile Number");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        std_update_btn=findViewById(R.id.update_std_btn);
        std_update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the student ID (stdid) from the input
                String stdid1 = std_id.getText().toString();
                String name = std_name.getText().toString().trim();
                String mobileNumber = std_no.getText().toString().trim();
                String studentClass = std_class.getSelectedItem().toString();

                // Check if all fields are provided
                if (stdid1.isEmpty() || name.isEmpty() || mobileNumber.isEmpty() || studentClass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingDialog.show();

                int stdid = Integer.parseInt(std_id.getText().toString());
                // Reference to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Find the student document by stdid
                db.collection("users")
                        .whereEqualTo("stdid", stdid) // Query Firestore for the given stdid
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                // Assume there is only one document with the given stdid
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String documentId = document.getId(); // Get the document ID

                                // Create a map with updated student data
                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("name", name);
                                updatedData.put("mobile_no", mobileNumber);
                                updatedData.put("class", studentClass);

                                // Update the student document in Firestore
                                db.collection("users").document(documentId)
                                        .update(updatedData)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                // Perform your login task here, and dismiss the dialog when done
                                                loadingDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Student updated successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Perform your login task here, and dismiss the dialog when done
                                                loadingDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Failed to update student.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {

                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "No student found with the given ID.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Perform your login task here, and dismiss the dialog when done
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error fetching student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }
}