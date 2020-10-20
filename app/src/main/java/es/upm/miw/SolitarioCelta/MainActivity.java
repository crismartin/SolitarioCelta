package es.upm.miw.SolitarioCelta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import es.upm.miw.SolitarioCelta.model.SCeltaViewModel;
import es.upm.miw.SolitarioCelta.model.SCeltaViewModelFactory;

public class MainActivity extends AppCompatActivity {
    private static final String FILE_NAME = "config.txt";
    protected final String LOG_TAG = "MiW";
    protected final Integer ID = 2021;
    protected SCeltaViewModel miJuegoVM;
    // protected Game game;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        miJuegoVM = new ViewModelProvider(
                    this,
                    new SCeltaViewModelFactory(getApplication(), ID)
                    )
                .get(SCeltaViewModel.class);

        laodGame();
    }

    /**
     * Se ejecuta al pulsar una ficha
     * Las coordenadas (i, j) se obtienen a partir del nombre del recurso, ya que el botón
     * tiene un identificador en formato pXY, donde X es la fila e Y la columna
     * @param v Vista de la ficha pulsada
     */
    public void fichaPulsada(@NotNull View v) {
        String resourceName = getResources().getResourceEntryName(v.getId());
        int i = resourceName.charAt(1) - '0';   // fila
        int j = resourceName.charAt(2) - '0';   // columna

        Log.i(LOG_TAG, "fichaPulsada(" + i + ", " + j + ") - " + resourceName);
        miJuegoVM.jugar(i, j);
        Log.i(LOG_TAG, "#fichas=" + miJuegoVM.numeroFichas());

        mostrarTablero();
        if (miJuegoVM.juegoTerminado()) {
            // TODO guardar puntuación
            new AlertDialogFragment().show(getSupportFragmentManager(), "ALERT_DIALOG");
        }
    }

    /**
     * Visualiza el tablero
     */
    public void mostrarTablero() {
        RadioButton button;
        String strRId;
        String prefijoIdentificador = getPackageName() + ":id/p"; // formato: package:type/entry
        int idBoton;

        for (int i = 0; i < SCeltaViewModel.TAMANIO; i++)
            for (int j = 0; j < SCeltaViewModel.TAMANIO; j++) {
                strRId = prefijoIdentificador + i + j;
                idBoton = getResources().getIdentifier(strRId, null, null);
                if (idBoton != 0) { // existe el recurso identificador del botón
                    button = findViewById(idBoton);
                    button.setChecked(miJuegoVM.obtenerFicha(i, j) == SCeltaViewModel.FICHA);
                }
            }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcAjustes:
                startActivity(new Intent(this, SCeltaPrefs.class));
                return true;
            case R.id.opcAcercaDe:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.aboutTitle)
                        .setMessage(R.string.aboutMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;

            // TODO!!! resto opciones
            case R.id.opcReiniciarPartida:
                resetGame();
                return true;
            case R.id.opcGuardarPartida:
                saveFile();
                return true;
            case R.id.opcRecuperarPartida:
                laodGame();
                return true;
            default:
                Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.txtSinImplementar),
                        Snackbar.LENGTH_LONG
                        )
                        .show();
        }
        return true;
    }

    protected void resetGame(){
        new AlertDialog.Builder(this)
            .setTitle(R.string.txtAlertReiniciarTitle)
            .setMessage(R.string.txtAlertReiniciarBody)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    miJuegoVM.reiniciar();
                    mostrarTablero();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // nothing
                }
            })
            .show();
    }

    public void laodGame(){
        loadFile();
        mostrarTablero();
    }

    public void saveFile() {
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            String board = miJuegoVM.serializaTablero();
            fos.write(board.getBytes());

            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Saved to " + getFilesDir() + "/" + FILE_NAME,
                    Snackbar.LENGTH_LONG
            ).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void loadFile(){
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text);
            }

            miJuegoVM.deserializaTablero(sb.toString());

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File " + getFilesDir() + "/" + FILE_NAME
                    + " does not exist", Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
