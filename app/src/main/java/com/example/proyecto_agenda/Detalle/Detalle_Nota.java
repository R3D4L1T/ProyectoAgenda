package com.example.proyecto_agenda.Detalle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_agenda.Objetos.AppDatabase;
import com.example.proyecto_agenda.Objetos.Nota;
import com.example.proyecto_agenda.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Detalle_Nota extends AppCompatActivity {

    private static final String TAG = "Detalle_Nota";
    private Button botonImportante;
    private TextView idNotaDetalle, uidUsuarioDetalle, correoUsuarioDetalle, tituloDetalle, descripcionDetalle,
            fechaRegistroDetalle, fechaNotaDetalle, estadoDetalle;

    private String idNota, uidUsuario, correoUsuario, fechaRegistro, titulo, descripcion, fechaNota, estado;
    private boolean comprobarNotaImportante = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Detalle de nota");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        inicializarVistas();
        recuperarDatos();
        setearDatosRecuperados();
        verificarNotaImportante();

        botonImportante.setOnClickListener(v -> {
            if (comprobarNotaImportante) {
                eliminarNotaImportante();
            } else {
                agregarNotaImportante();
            }
        });
    }

    private void inicializarVistas() {
        idNotaDetalle = findViewById(R.id.Id_nota_Detalle);
        uidUsuarioDetalle = findViewById(R.id.Uid_usuario_Detalle);
        correoUsuarioDetalle = findViewById(R.id.Correo_usuario_Detalle);
        descripcionDetalle = findViewById(R.id.Descripcion_Detalle);
        tituloDetalle = findViewById(R.id.Titulo_Detalle);
        fechaRegistroDetalle = findViewById(R.id.Fecha_Registro_Detalle);
        fechaNotaDetalle = findViewById(R.id.Fecha_Nota_Detalle);
        estadoDetalle = findViewById(R.id.Estado_Detalle);
        botonImportante = findViewById(R.id.Boton_Importante);
    }

    private void recuperarDatos() {
        Bundle intent = getIntent().getExtras();

        if (intent != null) {
            uidUsuario = intent.getString("uid_usuario");
            correoUsuario = intent.getString("correo_usuario");
            fechaRegistro = intent.getString("fecha_registro");
            titulo = intent.getString("titulo");
            descripcion = intent.getString("descripcion");
            fechaNota = intent.getString("fecha_nota");
            estado = intent.getString("estado");
            idNota = intent.getString("id_nota");

            if (uidUsuario == null) {
                Log.e(TAG, "UID de usuario es nulo después de recuperar datos del Intent");
                showToast("Error: UID de usuario nulo");
                finish();
            }
        } else {
            showToast("Error al recibir datos del intent");
            finish();
        }
    }

    private void setearDatosRecuperados() {
        idNotaDetalle.setText(idNota);
        uidUsuarioDetalle.setText(uidUsuario);
        correoUsuarioDetalle.setText(correoUsuario);
        descripcionDetalle.setText(descripcion);
        tituloDetalle.setText(titulo);
        fechaRegistroDetalle.setText(fechaRegistro);
        fechaNotaDetalle.setText(fechaNota);
        estadoDetalle.setText(estado);
    }

    private void agregarNotaImportante() {
        new Thread(() -> {
            try {
                List<Nota> notasUsuario = AppDatabase.getInstance(getApplicationContext())
                        .notaDao().getNotasByUsuario(uidUsuario);

                for (Nota nota : notasUsuario) {
                    if (String.valueOf(nota.getId()).equals(idNota)) {
                        showToast("La nota ya es importante");
                        return;
                    }
                }

                Nota nota = new Nota(uidUsuario, correoUsuario, fechaRegistro, titulo, descripcion, fechaNota, estado);
                long idNota = AppDatabase.getInstance(getApplicationContext()).notaDao().insert(nota);
                nota.setId((int) idNota);

                DatabaseReference notasImportantesRef = FirebaseDatabase.getInstance().getReference("Notas_Importantes").child(uidUsuario);
                notasImportantesRef.child(String.valueOf(nota.getId())).setValue(nota);

                runOnUiThread(() -> {
                    showToast("Se ha añadido a notas importantes");
                    comprobarNotaImportante = true;
                    actualizarBotonImportante();
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error al agregar nota importante", e);
            }
        }).start();
    }

    private void eliminarNotaImportante() {
        new Thread(() -> {
            try {
                if (idNota == null) {
                    showToast("Error: ID de nota nulo");
                    return;
                }

                int idNotaInt = Integer.parseInt(idNota);
                AppDatabase.getInstance(getApplicationContext()).notaDao().deleteNotaById(idNotaInt);

                runOnUiThread(() -> {
                    showToast("La nota ya no es importante");
                    comprobarNotaImportante = false;
                    actualizarBotonImportante();
                });
            } catch (NumberFormatException e) {
                showError("Error al convertir ID de nota a entero", e);
            } catch (Exception e) {
                showError("Error al eliminar nota importante", e);
            }
        }).start();
    }

    private void verificarNotaImportante() {
        if (uidUsuario == null || idNota == null) {
            return;
        }

        new Thread(() -> {
            try {
                Nota nota = AppDatabase.getInstance(getApplicationContext()).notaDao().getNotaById(Integer.parseInt(idNota));

                if (nota != null && nota.getUidUsuario().equals(uidUsuario)) {
                    comprobarNotaImportante = true;
                } else {
                    comprobarNotaImportante = false;
                }

                runOnUiThread(this::actualizarBotonImportante);
            } catch (Exception e) {
                runOnUiThread(() -> showError("Error al verificar nota importante", e));
            }
        }).start();
    }

    private void actualizarBotonImportante() {
        runOnUiThread(() -> {
            if (comprobarNotaImportante) {
                botonImportante.setText("Eliminar de importantes");
            } else {
                botonImportante.setText("Agregar a importantes");
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(Detalle_Nota.this, message, Toast.LENGTH_SHORT).show());
    }

    private void showError(String message, Exception e) {
        Log.e(TAG, message + ": " + e.getMessage());
        showToast(message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}


