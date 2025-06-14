package com.example.proyecto_agenda.ActualizarNota;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_agenda.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Actualizar_Nota extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "Actualizar_Nota";

    // Vista de la interfaz de usuario
    TextView Id_nota_A, Uid_Usuario_A, Correo_Usuario_A, Fecha_registro_A, Fecha_A, Estado_A, Estado_nuevo;
    EditText Titulo_A, Descripcion_A;
    Button Btn_Calendario_A;
    ImageView Tarea_Finalizada, Tarea_No_Finalizada;
    Spinner Spinner_estado;

    // Datos de la nota
    String id_nota_R, uid_usuario_R, correo_usuario_R, fecha_registro_R, titulo_R, descripcion_R, fecha_R, estado_R;

    // Fechas
    int dia, mes, anio;

    // Dependencia para actualizar la nota (Inversión de dependencias)
    private NotaRepository notaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_nota);

        // Inicializamos el repositorio de notas (Firebase)
        notaRepository = new FirebaseNotaRepository();

        // Configurar ActionBar
        getSupportActionBar().setTitle("Actualizar nota");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar vistas
        InicializarVistas();
        RecuperarDatos();
        SetearDatos();
        ComprobarEstadoNota();
        Spinner_Estado();

        Btn_Calendario_A.setOnClickListener(v -> SeleccionarFecha());
    }

    private void InicializarVistas() {
        Id_nota_A = findViewById(R.id.Id_nota_A);
        Uid_Usuario_A = findViewById(R.id.Uid_Usuario_A);
        Correo_Usuario_A = findViewById(R.id.Correo_Usuario_A);
        Fecha_registro_A = findViewById(R.id.Fecha_registro_A);
        Fecha_A = findViewById(R.id.Fecha_A);
        Estado_A = findViewById(R.id.Estado_A);
        Titulo_A = findViewById(R.id.Titulo_A);
        Descripcion_A = findViewById(R.id.Descripcion_A);
        Btn_Calendario_A = findViewById(R.id.Btn_Calendario_A);
        Tarea_Finalizada = findViewById(R.id.Tarea_Finalizada);
        Tarea_No_Finalizada = findViewById(R.id.Tarea_No_Finalizada);
        Spinner_estado = findViewById(R.id.Spinner_estado);
        Estado_nuevo = findViewById(R.id.Estado_nuevo);
    }

    private void RecuperarDatos() {
        Bundle intent = getIntent().getExtras();
        id_nota_R = intent.getString("id_nota");
        uid_usuario_R = intent.getString("uid_usuario");
        correo_usuario_R = intent.getString("correo_usuario");
        fecha_registro_R = intent.getString("fecha_registro");
        titulo_R = intent.getString("titulo");
        descripcion_R = intent.getString("descripcion");
        fecha_R = intent.getString("fecha_nota");
        estado_R = intent.getString("estado");
        Log.d(TAG, "Datos recuperados exitosamente");
    }

    private void SetearDatos() {
        Id_nota_A.setText(id_nota_R);
        Uid_Usuario_A.setText(uid_usuario_R);
        Correo_Usuario_A.setText(correo_usuario_R);
        Fecha_registro_A.setText(fecha_registro_R);
        Titulo_A.setText(titulo_R);
        Descripcion_A.setText(descripcion_R);
        Fecha_A.setText(fecha_R);
        Estado_A.setText(estado_R);
        Log.d(TAG, "Datos establecidos en las vistas");
    }

    private void ComprobarEstadoNota() {
        String estado_nota = Estado_A.getText().toString();
        if (estado_nota.equals(EstadoNota.NO_FINALIZADO.toString())) {
            Tarea_No_Finalizada.setVisibility(View.VISIBLE);
            Log.d(TAG, "Estado de la nota: No finalizado");
        } else if (estado_nota.equals(EstadoNota.FINALIZADO.toString())) {
            Tarea_Finalizada.setVisibility(View.VISIBLE);
            Log.d(TAG, "Estado de la nota: Finalizado");
        }
    }

    private void SeleccionarFecha() {
        final Calendar calendario = Calendar.getInstance();
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        mes = calendario.get(Calendar.MONTH);
        anio = calendario.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(Actualizar_Nota.this, (view, AnioSeleccionado, MesSeleccionado, DiaSeleccionado) -> {
            String diaFormateado = DiaSeleccionado < 10 ? "0" + DiaSeleccionado : String.valueOf(DiaSeleccionado);
            int Mes = MesSeleccionado + 1;
            String mesFormateado = Mes < 10 ? "0" + Mes : String.valueOf(Mes);
            Fecha_A.setText(diaFormateado + "/" + mesFormateado + "/" + AnioSeleccionado);
            Log.d(TAG, "Fecha seleccionada: " + diaFormateado + "/" + mesFormateado + "/" + AnioSeleccionado);
        }, anio, mes, dia);
        datePickerDialog.show();
    }

    private void Spinner_Estado() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Estado_nota, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner_estado.setAdapter(adapter);
        Spinner_estado.setOnItemSelectedListener(this);
        Log.d(TAG, "Spinner de estado configurado");
    }

    private void ActualizaNotaBD() {
        String tituloActualizar = Titulo_A.getText().toString();
        String descripcionActualizar = Descripcion_A.getText().toString();
        String fechaActualizar = Fecha_A.getText().toString();
        String estadoActualizar = Estado_nuevo.getText().toString();

        notaRepository.actualizarNota(id_nota_R, tituloActualizar, descripcionActualizar, fechaActualizar, estadoActualizar, new NotaRepository.NotaRepositoryCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Actualizar_Nota.this, "Nota actualizada con éxito", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al actualizar la nota: " + error);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String estadoSeleccionado = adapterView.getItemAtPosition(i).toString();
        Estado_nuevo.setText(estadoSeleccionado);
        Log.d(TAG, "Estado seleccionado: " + estadoSeleccionado);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "Ningún estado seleccionado");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_actualizar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.Actualizar_Nota_BD) {
            ActualizaNotaBD();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    // Enum para los estados de las notas
    public enum EstadoNota {
        FINALIZADO("Finalizado"),
        NO_FINALIZADO("No finalizado");

        private final String estado;

        EstadoNota(String estado) {
            this.estado = estado;
        }

        @Override
        public String toString() {
            return estado;
        }
    }

    // Interface para el repositorio de notas
    public interface NotaRepository {
        void actualizarNota(String id, String titulo, String descripcion, String fecha, String estado, NotaRepositoryCallback callback);

        interface NotaRepositoryCallback {
            void onSuccess();
            void onError(String error);
        }
    }

    // Implementación del repositorio de notas para Firebase
    public class FirebaseNotaRepository implements NotaRepository {
        @Override
        public void actualizarNota(String id, String titulo, String descripcion, String fecha, String estado, NotaRepositoryCallback callback) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Notas_Publicadas");

            Query query = databaseReference.orderByChild("id_nota").equalTo(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ds.getRef().child("titulo").setValue(titulo);
                        ds.getRef().child("descripcion").setValue(descripcion);
                        ds.getRef().child("fecha_nota").setValue(fecha);
                        ds.getRef().child("estado").setValue(estado);
                    }
                    callback.onSuccess();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
    }
}

