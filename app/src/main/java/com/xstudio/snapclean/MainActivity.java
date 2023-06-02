package com.xstudio.snapclean;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xstudio.snapclean.fragments.SelecionadosFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    TextView hello;
    TextView textoTeste;
    private DrawerLayout drawerLayout;
    private static final int REQUEST_STORAGE_PERMISSIONS = 1;

    private Uri pastaSelecionada;
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        hello = findViewById(R.id.hello);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            //Solicitando permissões
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS);
            System.out.println("Permissão talvez negada");
            hello.setText("Permissões ainda não aceitas");
        } else {
            System.out.println("Permissão concedida");
            hello.setText("Permissões concedidas anteriormente");
        }

        drawerLayout = findViewById(R.id.drawer_layout);


        ImageButton optionIcon = findViewById(R.id.voltar_selecionados);
        optionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                textoTeste = findViewById(R.id.textoTesteIcone);
                textoTeste.setText("Apertou icone hamburger");
                if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        ImageView icLixeira1 = findViewById(R.id.lixeira);
        icLixeira1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View viel){
                abrirFragmentSelecionados();
            }
        });

        ImageView pastaCima = findViewById(R.id.pasta_cima);
        pastaCima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarPasta();
            }
        });

        ImageView pastaCentral = findViewById(R.id.pasta_central);
        pastaCentral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarPasta();
            }
        });

    }
    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if ( result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data != null && data.getData() != null){
                        pastaSelecionada = data.getData();
                        //String folderPath = pastaSelecionada.getPath();
                    }
                }
            });
    private String exibirImagemPastaSelecionada(){
        if (pastaSelecionada != null){
            String folderPath = pastaSelecionada.getPath();
            return folderPath;
        } else {
            return null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent evento) {
        if (evento.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && view.getId() != R.id.navigation_drawer) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                if (!rect.contains((int) evento.getRawX(), (int) evento.getRawY())) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        }
        return super.dispatchTouchEvent(evento);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Verifica se as permissões foram concedidas
        if (requestCode == REQUEST_STORAGE_PERMISSIONS){
            //Verifica as permissões, dentro disso vai o que ta permitido
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                System.out.println("PERMISSÕES GARANTIDAS!!!!!!!!!!!!");
                hello.setText("permissoes concedidas");
            } else {
                System.out.println("PERMISSÕES NEGADAS");
                hello.setText("É necessário que você aceite as permissões");
            }
        }
    }

    private void abrirFragmentSelecionados(){
        ConstraintLayout layoutPrincipal = findViewById(R.id.layout_principal);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_selecionados, new SelecionadosFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        layoutPrincipal.setVisibility(View.GONE);
    }

    private static final int REQUEST_CODE_OPEN_FOLDER = 1;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    public void selecionarPasta(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        selecionarPastaLauncher.launch(intent);
    }

    public void testeInfo(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone info - Testa caminho da pasta\nCaminho da pasta: " + exibirImagemPastaSelecionada());

        System.out.println(exibirImagemPastaSelecionada());
    }

    public void testeHamburger(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone hamburger");
    }
    public void testeVoltarImagem(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone Voltar Imagem");
    }
    public void testeNegarImagem(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone Negar Imagem");
    }
    public void testeAceitarImagem(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone Aceitar Imagem");
    }
    public void testeVerImagens(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone Ver Imagens");
    }
}