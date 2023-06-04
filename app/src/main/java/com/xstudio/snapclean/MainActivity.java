package com.xstudio.snapclean;

import static java.nio.file.Files.isDirectory;

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
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.fragments.SelecionadosFragment;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    TextView hello;
    TextView textoTeste;
    private DrawerLayout drawerLayout;
    private static final int REQUEST_STORAGE_PERMISSIONS = 1;

    private static final int REQUEST_CODE_OPEN_FOLDER = 1;
    private Uri pastaSelecionada;
    private ImageView imageView;
    private VideoView videoView;

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
                != PackageManager.PERMISSION_GRANTED) {
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
            public void onClick(View view) {
                textoTeste = findViewById(R.id.textoTesteIcone);
                textoTeste.setText("Apertou icone hamburger");
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        ImageView icLixeira1 = findViewById(R.id.lixeira);
        icLixeira1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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


        //Selecionador de pastas
        ImageButton botaoSelecionarPasta1 = findViewById(R.id.pasta_cima);
        ImageButton botaoSelecionarPasta2 = findViewById(R.id.pasta_central);

        View.OnClickListener selecionarPastaClickListener = v -> selecionarPasta();

        botaoSelecionarPasta1.setOnClickListener(selecionarPastaClickListener);
        botaoSelecionarPasta2.setOnClickListener(selecionarPastaClickListener);


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode);
        System.out.println(resultCode);
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
            System.out.println("dentro de onActivityResult o primeiro if passou");
            if (data != null && data.getData() != null) {


                //pastaSelecionada = getFolderPathFromUri(data.getData());
                pastaSelecionada = data.getData();
                System.out.println("uma linha antes de chamar a função exibirImagem");
                exibirImagemPastaSelecionada(pastaSelecionada);
            }
        }
    }

    private String getFolderPathFromUri(Uri contentUri) {

        String path = null;
        if (contentUri != null) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(columnIndex);
            cursor.close();
            return path;
            /*
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
            if(documentFile != null && documentFile.isDirectory()){
                folderPath = documentFile.getUri();
            }
             */

        }
        return path;
    }

    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        //String pastaUri = .getPath();
                        //pastaSelecionada = getFolderPathFromUri(data.getData());
                        pastaSelecionada = data.getData();
                        exibirImagemPastaSelecionada(data.getData());
                    }
                }
            });


    private void exibirImagemPastaSelecionada(Uri pastaSelecionada) {
        System.out.println("função exibirImagemPastaSelecionada foi acionada");
        if (pastaSelecionada != null) {

            System.out.println("pastaSelecionada não é null");
            System.out.println(pastaSelecionada);

            DocumentFile arquivos = DocumentFile.fromTreeUri(this, pastaSelecionada);


            /*
            File pasta = new File(pastaSelecionada);
            File[] arquivos = pasta.listFiles();

            boolean teste = pasta.isDirectory();

            System.out.println("testando diretório: " + teste);
            System.out.println("arquivos: " + arquivos);
            */
            //if (arquivos != null && arquivos.length > 0){
            if (arquivos != null && arquivos.length() > 0)
                //for (File arquivo : arquivos){
                for (DocumentFile arquivo : arquivos.listFiles()) {
                    System.out.println(arquivo.getName());
                    System.out.println(arquivo.getUri());
                    System.out.println(arquivo.getType());
                    System.out.println("---------------------");

                    String caminhoArquivo = arquivo.getUri().toString();
                    String extensao = arquivo.getType();

                    System.out.println(isVideo(extensao));
                    System.out.println(isImagem(extensao));
                    imageView = findViewById(R.id.view_imagem);
                    videoView = findViewById(R.id.view_video);


                    if (isImagem(extensao)){
                        imageView.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.GONE);
                        System.out.println("É uma imagem");
                        System.out.println(caminhoArquivo);

                        Glide.with(this)
                                .load(caminhoArquivo)
                                .into(imageView);
                    } else if (isVideo(extensao)) {
                        imageView.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);

                        System.out.println("É um vídeo");
                        videoView.setVideoPath(caminhoArquivo);
                        videoView.start();
                    }
                }
            }

            /*
            System.out.println(arquivos);

            if (arquivos != null && arquivos.length > 0){
                String caminhoArquivo = arquivos[0].getAbsolutePath();
                System.out.println("Entrou no segundo if de exibirImagemPastaSelecionada");

                boolean isImagem = caminhoArquivo.toLowerCase().endsWith(".jpg") || caminhoArquivo.toLowerCase().endsWith(".png") || caminhoArquivo.toLowerCase().endsWith(".jpeg");
                boolean isVideo = caminhoArquivo.toLowerCase().endsWith(".mp4") || caminhoArquivo.toLowerCase().endsWith(".3gp") || caminhoArquivo.toLowerCase().endsWith(".mp3") || caminhoArquivo.toLowerCase().endsWith(".wmv");
                VideoView videoView = findViewById(R.id.view_video);
                ImageView imageView = findViewById(R.id.view_imagem);

                //if (isImagem)
            }
            */
    }

    private String getFileExtension(String filePath){
        System.out.println("Dentro do getFileExtension");
        if (filePath != null && !filePath.isEmpty()){
            System.out.println("primeiro if do getFileExtension");
            int dotIndex = filePath.lastIndexOf(".");
            if (dotIndex > 0 && dotIndex < filePath.length() - 1){
                return filePath.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }

    private boolean isImagem(String extensao){
        return extensao.endsWith("jpg") || extensao.endsWith("png") || extensao.endsWith("jpeg");
    }

    private boolean isVideo(String extensao){
        return extensao.endsWith("mp4") || extensao.endsWith("3gp") || extensao.endsWith("mp3") || extensao.equalsIgnoreCase(".wmv");
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


    private ActivityResultLauncher<Intent> folderPickerLauncher;

    public void selecionarPasta(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER);
        selecionarPastaLauncher.launch(intent);
    }

    public void testeInfo(View view){
        textoTeste = findViewById(R.id.textoTesteIcone);
        textoTeste.setText("Apertou icone info - Testa caminho da pasta");

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