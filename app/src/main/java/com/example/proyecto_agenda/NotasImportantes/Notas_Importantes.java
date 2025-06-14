package com.example.proyecto_agenda.NotasImportantes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_agenda.MenuPrincipal;
import com.example.proyecto_agenda.Objetos.Nota;
import com.example.proyecto_agenda.R;
import com.example.proyecto_agenda.ViewHolder.ViewHolder_Nota_Importante;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Notas_Importantes extends AppCompatActivity {

    private RecyclerView recyclerViewNotasImportantes;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference misNotasImportantes;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseRecyclerAdapter<Nota, ViewHolder_Nota_Importante> firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas_archivadas);

        // Configura la barra de acción con la flecha hacia atrás
        setupActionBar();

        initializeFirebaseComponents();
        setupRecyclerView();
        loadNotasImportantes();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Notas Importantes");
            // Habilitar la flecha hacia atrás
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void initializeFirebaseComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            misNotasImportantes = firebaseDatabase.getReference("Notas_Importantes").child(user.getUid());
        }
    }

    private void setupRecyclerView() {
        recyclerViewNotasImportantes = findViewById(R.id.RecyclerViewNotasImportantes);
        recyclerViewNotasImportantes.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewNotasImportantes.setLayoutManager(linearLayoutManager);
    }

    private void loadNotasImportantes() {
        if (user == null) {
            showToast("Error: Usuario no autenticado");
            return;
        }

        FirebaseRecyclerOptions<Nota> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Nota>()
                .setQuery(misNotasImportantes, Nota.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Nota, ViewHolder_Nota_Importante>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Nota_Importante viewHolder, int position, @NonNull Nota nota) {
                bindDataToViewHolder(viewHolder, nota);
            }

            @NonNull
            @Override
            public ViewHolder_Nota_Importante onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nota_importante, parent, false);
                return new ViewHolder_Nota_Importante(view);
            }
        };

        recyclerViewNotasImportantes.setAdapter(firebaseRecyclerAdapter);
    }

    private void bindDataToViewHolder(ViewHolder_Nota_Importante viewHolder, Nota nota) {
        viewHolder.setItemData(getApplicationContext(),
                String.valueOf(nota.getId()),
                nota.getUidUsuario(),
                nota.getCorreoUsuario(),
                nota.getFechaHoraActual(),
                nota.getTitulo(),
                nota.getDescripcion(),
                nota.getFechaNota(),
                nota.getEstado());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    private void showToast(String message) {
        Toast.makeText(Notas_Importantes.this, message, Toast.LENGTH_SHORT).show();
    }

    // Método para manejar el clic en la flecha hacia atrás
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navegar hacia la actividad principal (MenuPrincipal)
            Intent intent = new Intent(Notas_Importantes.this, MenuPrincipal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Limpia la pila de actividades anteriores
            startActivity(intent);
            finish(); // Finaliza la actividad actual
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
