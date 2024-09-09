package com.arquitectura.app;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox termsCheckBox;
    private Button loginButton;
    private Button registerWithGmailButton;
    private TextView forgotPasswordText;
    private TextView termsTextView;
    private TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupGoogleSignIn();
        setupWindowInsets();

        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> sendPasswordResetEmail());
        termsTextView.setOnClickListener(v -> openTermsAndConditions());
        registerWithGmailButton.setOnClickListener(v -> signInWithGoogle());
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        loginButton = findViewById(R.id.loginButton);
        registerWithGmailButton = findViewById(R.id.registerWithGmailButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        termsTextView = findViewById(R.id.termsTextView);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupGoogleSignIn() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, ingrese el correo y la contraseña");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        showToast("Inicio de sesión exitoso");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            navigateToProfile(user);
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        showToast("Inicio de sesión fallido, intente nuevamente");
                    }
                });
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            showToast("Por favor, ingrese el correo para recuperar la contraseña");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast("Correo de recuperación enviado");
                    } else {
                        showToast("Error al enviar el correo de recuperación");
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void saveUserData(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Datos que quieres guardar
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Referencia al documento del usuario
        DocumentReference userDocRef = db.collection("users").document(user.getUid());

        // Comprobar si el documento ya existe
        userDocRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // El documento ya existe, solo actualiza
                            userDocRef.update(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Actualización exitosa
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error al actualizar
                                        Log.w(TAG, "Error updating user data", e);
                                        showToast("Error al actualizar los datos del usuario");
                                    });
                        } else {
                            // El documento no existe, crea uno nuevo
                            userDocRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Creación exitosa
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error al crear
                                        Log.w(TAG, "Error writing user data", e);
                                        showToast("Error al guardar los datos del usuario");
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error checking user document", task.getException());
                        showToast("Error al verificar los datos del usuario");
                    }
                });
    }


    private void registerUser() {
        if (!termsCheckBox.isChecked()) {
            showToast("Debe aceptar los términos y condiciones");
            return;
        }

        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void openTermsAndConditions() {
        Intent intent = new Intent(LoginActivity.this, TyCActivity.class);
        startActivity(intent);
    }

    private void navigateToProfile(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        intent.putExtra("email", user.getEmail());
        intent.putExtra("displayName", user.getDisplayName());
        intent.putExtra("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        startActivity(intent);
        finish();
    }
    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this, task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d(TAG, "signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        saveUserData(user);
                                        navigateToProfile(user);
                                    }
                                } else {
                                    Log.w(TAG, "signInWithCredential:failure", task1.getException());
                                    showToast("Autenticación fallida: " + task1.getException().getMessage());
                                }
                            });
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                showToast("Autenticación fallida: " + e.getMessage());
            }
        }
    }
}
