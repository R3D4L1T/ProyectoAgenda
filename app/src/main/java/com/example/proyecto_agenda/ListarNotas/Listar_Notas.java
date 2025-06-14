package com.example.proyecto_agenda.ListarNotas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_agenda.ActualizarNota.Actualizar_Nota;
import com.example.proyecto_agenda.Detalle.Detalle_Nota;
import com.example.proyecto_agenda.Objetos.AppDatabase;
import com.example.proyecto_agenda.Objetos.Nota;
import com.example.proyecto_agenda.R;
import com.example.proyecto_agenda.ViewHolder.ViewHolder_Nota;

import java.util.List;

public class Listar_Notas extends AppCompatActivity {

    private static final String TAG = "Listar_Notas";
    private RecyclerView recyclerviewNotas;
    private NotaAdapter notaAdapter;
    private String uidUsuario, correoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_notas);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Mis Notas");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        recyclerviewNotas = findViewById(R.id.recyclerviewNotas);
        recyclerviewNotas.setHasFixedSize(true);
        recyclerviewNotas.setLayoutManager(new LinearLayoutManager(this));

        uidUsuario = getIntent().getStringExtra("Uid");
        correoUsuario = getIntent().getStringExtra("Correo");

        if (uidUsuario != null) {
            loadNotas();
        } else {
            Toast.makeText(this, "Error: UID del usuario no disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotas() {
        new LoadNotasTask().execute();
    }

    private class LoadNotasTask extends AsyncTask<Void, Void, List<Nota>> {
        @Override
        protected List<Nota> doInBackground(Void... voids) {
            return AppDatabase.getInstance(getApplicationContext()).notaDao().getNotasByUsuario(uidUsuario);
        }

        @Override
        protected void onPostExecute(List<Nota> notas) {
            notaAdapter = new NotaAdapter(notas);
            recyclerviewNotas.setAdapter(notaAdapter);
        }
    }

    private class NotaAdapter extends RecyclerView.Adapter<ViewHolder_Nota> {
        private List<Nota> notaList;

        NotaAdapter(List<Nota> notaList) {
            this.notaList = notaList;
        }

        @NonNull
        @Override
        public ViewHolder_Nota onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nota, parent, false);
            return new ViewHolder_Nota(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder_Nota holder, int position) {
            final Nota nota = notaList.get(position);
            holder.SetearDatos(
                    getApplicationContext(),
                    String.valueOf(nota.getId()),
                    nota.getUidUsuario(),
                    nota.getCorreoUsuario(),
                    nota.getFechaHoraActual(),
                    nota.getTitulo(),
                    nota.getDescripcion(),
                    nota.getFechaNota(),
                    nota.getEstado()
            );

            holder.setOnClickListener(new ViewHolder_Nota.ClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(Listar_Notas.this, Detalle_Nota.class);
                    intent.putExtra("id", String.valueOf(nota.getId()));
                    intent.putExtra("uid_usuario", nota.getUidUsuario());
                    intent.putExtra("correo_usuario", nota.getCorreoUsuario());
                    intent.putExtra("fecha_registro", nota.getFechaHoraActual());
                    intent.putExtra("titulo", nota.getTitulo());
                    intent.putExtra("descripcion", nota.getDescripcion());
                    intent.putExtra("fecha_nota", nota.getFechaNota());
                    intent.putExtra("estado", nota.getEstado());
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    showDialogOptions(nota);
                }
            });
        }

        @Override
        public int getItemCount() {
            return notaList.size();
        }
    }

    private void showDialogOptions(Nota nota) {
        final Button CD_Eliminar, CD_Actualizar;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones");
        builder.setMessage("¿Qué deseas hacer con esta nota?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> eliminarNota(nota.getId()));
        builder.setNegativeButton("Actualizar", (dialog, which) -> {
            Intent intent = new Intent(Listar_Notas.this, Actualizar_Nota.class);
            intent.putExtra("nota", nota);
            startActivity(intent);
        });

        builder.create().show();
    }

    private void eliminarNota(int idNota) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Listar_Notas.this);
        builder.setTitle("Eliminar Nota");
        builder.setMessage("¿Desea eliminar la nota?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            new DeleteNotaTask(idNota).execute();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            Toast.makeText(Listar_Notas.this, "Cancelado", Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }

    private class DeleteNotaTask extends AsyncTask<Void, Void, Void> {
        private final int idNota;

        DeleteNotaTask(int idNota) {
            this.idNota = idNota;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).notaDao().deleteNotaById(idNota);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(Listar_Notas.this, "Nota eliminada", Toast.LENGTH_SHORT).show();
            loadNotas();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotas();
    }
}