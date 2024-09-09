package com.arquitectura.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        Button profileToolBarButton = findViewById(R.id.profileToolBarButton);
        Button logoutButton = findViewById(R.id.logoutButton); // Botón para cerrar sesión

        profileToolBarButton.setTextColor(getResources().getColor(R.color.special_green));
        profileToolBarButton.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.special_green));

        // Recuperar datos pasados desde LoginActivity
        String email = getIntent().getStringExtra("email");
        String displayName = getIntent().getStringExtra("displayName");
        String photoUrl = getIntent().getStringExtra("photoUrl");

        // Ahora puedes usar estos datos para mostrarlos en la UI
        TextView emailTextView = findViewById(R.id.profileEmail);
        TextView displayNameTextView = findViewById(R.id.profileName);
        ImageView photoImageView = findViewById(R.id.profileImage);

        emailTextView.setText(email);
        displayNameTextView.setText(displayName);

        // Cargar imagen usando Glide
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher_background) // Imagen de placeholder mientras se carga la imagen
                .error(R.drawable.ic_launcher_foreground) // Imagen a mostrar si hay un error al cargar la imagen
                .into(photoImageView);

        // Configurar botón de cerrar sesión
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // Cerrar sesión con Firebase

                // Redirigir al usuario de vuelta a la LoginActivity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finalizar la actividad actual
            }
        });
    }
}
