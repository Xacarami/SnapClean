package com.xstudio.snapclean;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResultLauncher;
import com.xstudio.snapclean.R;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.fragments.SelecionadosFragment;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements SelecionadosFragment.OnArquivoRecuperadoListener {

    //TextView hello;
    private DrawerLayout drawerLayout;
    private static final int REQUEST_STORAGE_PERMISSIONS = 1;

    private static final int REQUEST_CODE_OPEN_FOLDER = 1;
    private Uri pastaSelecionada;
    private ImageView imageView;
    private VideoView videoView;
    MediaPlayer mediaPlayer;
    private TextView numeroLixeira;
    private ConstraintLayout layoutPastaCentral;
    private Boolean pastaJaFoiSelecionada = false;
    private int imagensCarregadas = 0;

    ImageButton botaoAvancar;
    ImageButton botaoExcluir;
    ImageButton botaoVoltar;
    ImageButton zoomIn;
    ImageButton zoomOut;
    Button restaurarTamanho;
    Switch autoplaySwitch;

    //O testeSeJaFoiCriado diz que não foi usado, mas é importante
    SelecionadosFragment testeSeJaFoiCriado;
    Boolean resetouPasta = false;
    private final ArrayList<DocumentFile> listaDeExclusao = new ArrayList<>();
    FrameLayout layoutImagens;
    ConstraintLayout constraintIconesCima;
    ImageButton iconeMaisImagens;
    Switch switchBackup;
    ConstraintLayout botaoWhatsapp;
    private float currentTranslationX = 0f;
    private float currentTranslationY = 0f;
    ConstraintLayout constraintIconesBaixo;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_EXCLUSION_LIST = "exclusion_list";
    private static final String PREF_SWITCH_AUTOPLAY = "switch_autoplay";
    private static final String PREF_SWITCH_BACKUP = "switch_backup";
    private SharedPreferences sharedPreferences;
    private SharedPreferences settings;
    private boolean acessoNaPasta = false;
    private boolean isZooming;
    private boolean permitirArrasto = true;

    float x1, x2;
    float y1;
    float MIN_DISTANCE = 150;
    float alphaIcone = 0.05f;

    private static final String[] STORAGE_PERMISSIONS = {
            READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Isso servirá para desativar os ícones ao abrir a sideBar
    ArrayList<View> icons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hello = findViewById(R.id.hello);

        //muda a cor da barra de status, pretendo comentado até adaptar para apis menores
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getWindow().setStatusBarColor(Color.parseColor("#161618"));
        //}

        //Já garante a permissão de primeira
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Solicitando permissões
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permissão concedida
                    System.out.println("Permissão concedida");
                    acessoNaPasta = true;
                } else {
                    // Permissão negada
                    System.out.println("Permissão negada");
                    acessoNaPasta = false;
                    Toast.makeText(this, "É necessário aceitar as permissões de acesso às pastas!", Toast.LENGTH_SHORT).show();
                    requestManageExternalStoragePermission();
                }
            } else {
                System.out.println("Permissão negada");
                acessoNaPasta = false;
                Toast.makeText(this, "É necessário aceitar as permissões de acesso às pastas!", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS);
            }

            //hello.setText("Permissões ainda não aceitas");
        } else {
            System.out.println("Permissão concedida");
            acessoNaPasta = true;
            //hello.setText("Permissões concedidas anteriormente");
        }


        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);

        // Gravar a decisão passada do usuário sobre autoplay
        autoplaySwitch = findViewById(R.id.switchAutoPlay);
        autoplaySwitch.setChecked(sharedPreferences.getBoolean(PREF_SWITCH_AUTOPLAY, true));
        autoplaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_SWITCH_AUTOPLAY, isChecked);
            editor.apply();
        });

        // Gravar decisão sobre backup
        switchBackup = findViewById(R.id.switchBackup);
        switchBackup.setChecked(sharedPreferences.getBoolean(PREF_SWITCH_BACKUP, true));
        switchBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_SWITCH_BACKUP, isChecked);
            editor.apply();
        });


        drawerLayout = findViewById(R.id.drawer_layout);

        ImageView pastaCima = findViewById(R.id.pasta_cima);
        ImageView pastaCentral = findViewById(R.id.pasta_central);

        ImageButton optionIcon = findViewById(R.id.opcoes);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

        optionIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                layoutPastaCentral.setVisibility(View.GONE);
                for (View icon : icons) {
                    icon.setEnabled(false);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                for (View icon : icons) {
                    icon.setEnabled(true);
                }
                if (!pastaJaFoiSelecionada) {
                    layoutPastaCentral.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });


        ImageView icLixeira1 = findViewById(R.id.lixeira);
        icLixeira1.setOnClickListener(view -> {
            if (testeSeJaFoiCriado == null) {
                SelecionadosFragment selecionadosFragment = new SelecionadosFragment(listaDeExclusao);
                selecionadosFragment.setOnArquivoRecuperadoListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_selecionados, selecionadosFragment)
                        .commit();
            } else {
                testeSeJaFoiCriado.setOnArquivoRecuperadoListener(this);
                testeSeJaFoiCriado.atualizarListaDeExclusao(listaDeExclusao);
            }
        });


        iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
        iconeMaisImagens.setOnClickListener(view -> {
            System.out.println("Ta clicando Mais Imagens");
            colocarToast();
        });

        ImageButton iconeInfo = findViewById(R.id.info_button);
        iconeInfo.setOnClickListener(view -> {
            System.out.println("Ta clicando Infos");
            Toast.makeText(MainActivity.this, "Ainda não disponível. Espere uma atualização.", Toast.LENGTH_SHORT).show();
        });

        botaoWhatsapp = findViewById(R.id.botao_whatsapp);
        botaoWhatsapp.setOnClickListener(view -> {
            System.out.println("Ta indo");
            coisaDeAndroidR();
        });

        //Selecionador de pastas
        View.OnClickListener selecionarPastaClickListener = v -> {
            if (!pastaJaFoiSelecionada) {
                if (acessoNaPasta) {
                    selecionarPasta();
                } else {
                    showPermissionExplanationDialog();
                }
            } else {
                resetouPasta = true;
                imagensCarregadas = 0;
                selecionarPasta();
                System.out.println("Pasta ja selecionada, e clicou em abrir mais uma");
            }
        };

        pastaCima.setOnClickListener(selecionarPastaClickListener);
        pastaCentral.setOnClickListener(selecionarPastaClickListener);


        //Isso servirá para desativar os ícones ao abrir a sideBar
        icons.add(findViewById(R.id.opcoes));
        icons.add(findViewById(R.id.pasta_cima));
        icons.add(findViewById(R.id.info_button));
        icons.add(findViewById(R.id.lixeira));
        icons.add(findViewById(R.id.voltar_imagem));
        icons.add(findViewById(R.id.negar_imagem));
        icons.add(findViewById(R.id.aceitar_imagem));
        icons.add(findViewById(R.id.abrir_mais_imagens));


        layoutImagens = findViewById(R.id.layout_imagens);
        imageView = findViewById(R.id.view_imagem);

        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);
        restaurarTamanho = findViewById(R.id.restaurar_tamanho);

        //private ObjectAnimator animator;
        zoomIn.setOnClickListener(v -> {
            if (layoutImagens.getScaleX() <= 8) {

                layoutImagens.setScaleX(layoutImagens.getScaleX() * 2f);
                layoutImagens.setScaleY(layoutImagens.getScaleY() * 2f);
                if (layoutImagens.getScaleX() > 1 || layoutImagens.getScaleY() > 1) {
                    isZooming = true;
                    permitirArrasto = true;
                    restaurarTamanho.setVisibility(View.VISIBLE);
                    esconderIcones();

                } else if (layoutImagens.getScaleX() == 1 || layoutImagens.getScaleY() == 1) {
                    restaurarTamanho.setVisibility(View.GONE);
                    layoutImagens.setTranslationX(0);
                    layoutImagens.setTranslationY(0);
                    isZooming = false;
                    permitirArrasto = true;
                    voltarIconesAparecer();

                } else {
                    isZooming = true;
                    permitirArrasto = false;
                    restaurarTamanho.setVisibility(View.VISIBLE);
                    layoutImagens.setTranslationX(0);
                    layoutImagens.setTranslationY(0);
                }
            }
        });

        //private ObjectAnimator animator;
        zoomOut.setOnClickListener(v -> {
            if (layoutImagens.getScaleX() >= 0.125) {
                layoutImagens.setScaleX(layoutImagens.getScaleX() * 0.5f);
                layoutImagens.setScaleY(layoutImagens.getScaleY() * 0.5f);

                if (layoutImagens.getScaleX() > 1 || layoutImagens.getScaleY() > 1) {
                    isZooming = true;
                    permitirArrasto = true;
                    restaurarTamanho.setVisibility(View.VISIBLE);
                    esconderIcones();

                } else if (layoutImagens.getScaleX() == 1 || layoutImagens.getScaleY() == 1) {
                    restaurarTamanho.setVisibility(View.GONE);
                    layoutImagens.setTranslationX(0);
                    layoutImagens.setTranslationY(0);
                    permitirArrasto = true;
                    isZooming = false;
                    voltarIconesAparecer();

                } else {
                    isZooming = true;
                    permitirArrasto = false;
                    restaurarTamanho.setVisibility(View.VISIBLE);
                    layoutImagens.setTranslationX(0);
                    layoutImagens.setTranslationY(0);
                }
            }
        });

        restaurarTamanho.setOnClickListener(v -> voltarAoNormalTamanhoETransaltion());
        settings = getSharedPreferences(PREFS_NAME, 0);

        // Recuperar a lista de exclusão salva, transforma de uri para arquivo
        switchBackup = findViewById(R.id.switchBackup);
        Set<String> exclusionList = settings.getStringSet(PREF_EXCLUSION_LIST, new LinkedHashSet<>());
        if (switchBackup.isChecked()) {
            for (String uriString : exclusionList) {
                Uri uri = Uri.parse(uriString);
                DocumentFile file = DocumentFile.fromSingleUri(this, uri);
                if (file != null && file.exists()) {
                    listaDeExclusao.add(0, file);
                }
            }
        } else {
            listaDeExclusao.clear();
        }

        numeroLixeira = findViewById(R.id.numero_lixeira);
        numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));

        layoutImagens.setOnTouchListener(new View.OnTouchListener() {
            private ObjectAnimator animator;
            boolean isAnimating = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //scaleDetector.onTouchEvent(event);
                if (isZooming && permitirArrasto) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            float currentX = event.getX();
                            //estava (currentX - x1) * 1f + currentTranslationX; esse f poderia acelerar o arrasto, mas não funcionou
                            float deltaX = (currentX - x1) + currentTranslationX;
                            layoutImagens.setTranslationX(deltaX);

                            float currentY = event.getY();
                            float deltaY = (currentY - y1) + currentTranslationY;
                            layoutImagens.setTranslationY(deltaY);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            x1 = event.getX();
                            y1 = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            currentTranslationX = layoutImagens.getTranslationX();
                            currentTranslationY = layoutImagens.getTranslationY();
                            break;
                    }
                    return true;
                }
                if (permitirArrasto) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            if (animator != null && animator.isRunning()) {
                                animator.cancel();
                            }
                            float currentX = event.getX();
                            float deltaX = currentX - x1;
                            layoutImagens.setTranslationX(deltaX);
                            esconderIcones();
                            break;
                        case MotionEvent.ACTION_DOWN:
                            x1 = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            x2 = event.getX();
                            deltaX = x2 - x1;
                            if (Math.abs(deltaX) > MIN_DISTANCE) {
                                if (x2 > x1) {
                                    // Deslizou da esquerda para a direita - Guardar arquivo
                                    animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", layoutImagens.getWidth());
                                    voltarIconesAparecer();
                                    guardandoArquivo();
                                } else {
                                    // Deslizou da direita para a esquerda - Excluir arquivo
                                    animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", -layoutImagens.getWidth());
                                    excluindoArquivo();
                                    voltarIconesAparecer();
                                }
                                animator.setDuration(200);
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        isAnimating = false;
                                        layoutImagens.setTranslationX(0);
                                        continuarLoop(quantidadeDeImagens + voltador, resultado.get());
                                    }
                                });
                            } else {
                                // Deslizou menos que MIN_DISTANCE, então volta para o centro
                                animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", 0);
                                animator.setDuration(200);
                            }
                            animator.start();
                            isAnimating = true;
                            voltarIconesAparecer();
                            break;
                    }
                    v.performClick();

                    // Aqui você pode adicionar o código para detectar o pressionamento longo
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Inicia o pressionamento longo
                        v.postDelayed(() -> {
                            // Aqui você pode adicionar o código para tornar os ícones mais transparentes
                            constraintIconesCima = findViewById(R.id.constraint_icones_cima);
                            constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);

                        }, ViewConfiguration.getLongPressTimeout());
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        // Cancela o pressionamento longo

                        voltarIconesAparecer();
                        v.removeCallbacks(null);
                    }
                    return true;
                }
                return true;
            }
        });
    }

    //Gerencia a permissão de visualizar e escrever os arquivos do usuário
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Verifica se as permissões foram concedidas
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            //Verifica as permissões, dentro disso vai o que ta permitido
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("PERMISSÕES GARANTIDAS!!!!!!!!!");
                acessoNaPasta = true;
                //hello.setText("permissoes concedidas");
            } else {
                System.out.println("PERMISSÕES NEGADAS");
                acessoNaPasta = false;
                showPermissionExplanationDialog();
                //hello.setText("É necessário que você aceite as permissões");
            }
        }
    }

    //Mensagem que aparece quando a pessoa nega as permissões
    private void showPermissionExplanationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            System.out.println("Permissão negada");
            acessoNaPasta = false;
            Toast.makeText(this, "É necessário aceitar as permissões de acesso às pastas!", Toast.LENGTH_SHORT).show();
            requestManageExternalStoragePermission();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Este aplicativo precisa de permissão para acessar suas pastas para funcionar corretamente. Por favor, conceda a permissão\nCaso tenha apertado em Não perguntar novamente, terá de reinstalar o aplicativo.")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS))
                    .create()
                    .show();
        }
    }

    //toda vez que saveExclusionList é chamado, o backup se atualiza com a listaDeExclusao
    Set<String> exclusionList = new LinkedHashSet<>();

    private void saveExclusionList() {
        switchBackup = findViewById(R.id.switchBackup);
        if (switchBackup.isChecked()) {
            exclusionList.clear();
            for (DocumentFile file : listaDeExclusao) {
                exclusionList.add(file.getUri().toString());
            }

            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet(PREF_EXCLUSION_LIST, exclusionList);
            editor.apply();
        } else {
            exclusionList.clear();
        }
    }


    public void excluindoArquivo() {
        boolean arquivoJaExcluido = false;
        for (DocumentFile arquivoExcluido : listaDeExclusao) {
            if (arquivoExcluido.getUri().equals(arquivoAtual.get().getUri())) {
                arquivoJaExcluido = true;
                break;
            }
        }
        if (!arquivoJaExcluido) {
            listaDeExclusao.add(0, arquivoAtual.get());
            numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
        }
        saveExclusionList();
    }

    public void guardandoArquivo() {
        for (DocumentFile arquivoExcluido : listaDeExclusao) {
            if (arquivoExcluido.getUri().equals(arquivoAtual.get().getUri())) {
                listaDeExclusao.remove(arquivoExcluido);
                saveExclusionList();
                numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
                break;
            }
        }
        saveExclusionList();
    }

    //Recebendo a lista manipulada pela SelecionadosFragment
    private void exibirSelecionados() {
        SelecionadosFragment fragment = new SelecionadosFragment(listaDeExclusao);
        fragment.setOnArquivoRecuperadoListener(this);
    }

    @Override
    public void onArquivoRecuperado(DocumentFile arquivo) {
        // Atualizar a lista de exclusão
        if (arquivo != null) {
            // Atualizar a lista de exclusão
            listaDeExclusao.remove(arquivo);
            saveExclusionList();
            listaDeExclusao.remove(arquivo);
            exibirSelecionados();
        }
        saveExclusionList();
        numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
    }
    // Fim Recebendo a lista manipulada pela SelecionadosFragment

    public void voltarAoNormalTamanhoETransaltion() {
        isZooming = false;
        layoutImagens.setTranslationX(0);
        layoutImagens.setTranslationY(0);
        layoutImagens.setScaleX(1);
        layoutImagens.setScaleY(1);
        restaurarTamanho.setVisibility(View.GONE);
        voltarIconesAparecer();
    }

    //Apenas uma mensagem padrão para algo que ainda não foi implementado
    public void colocarToast() {
        Toast.makeText(MainActivity.this, "Ainda não disponível. Espere uma atualização.", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode);
        System.out.println(resultCode);
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                pastaSelecionada = data.getData();
            }
        }
    }

    private void requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                manageExternalStorageLauncher.launch(intent);
            }
        }
    }

    TextView quantidadeImagens;

    private void coisaDeAndroidR(){
        /*
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        String selection = MediaStore.Images.Media.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{"WhatsApp/Media/WhatsApp Images"};
        //String[] selectionArgs = new String[]{"/Android/media/com.whatsapp/WhatsApp/Media"};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            System.out.println(cursor);
            System.out.println(cursor.moveToNext());
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    // Acessar o arquivo aqui
                    System.out.println(name);
                    //exibirImagemPastaSelecionada(uri);
                }
            }
        }

         */
        int i = 0;
        quantidadeImagens = findViewById(R.id.quantidade_imagens);
        //quantidadeImagens.setText("Número de arquivos\n" + String.valueOf(i));

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE
        };

        String selection = MediaStore.Images.Media.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{"WhatsApp/Media/WhatsApp Images"};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null) {
                System.out.println(cursor.moveToNext());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    // Acessar o arquivo aqui
                    //exibirImagemPastaSelecionada(uri);
                    System.out.println(i);
                    i++;
                }
                quantidadeImagens.setText("Número de arquivos\n" + String.valueOf(i));
            }
        }

        layoutImagens = findViewById(R.id.layout_imagens);
        constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);

        layoutImagens.setVisibility(View.VISIBLE);
        constraintIconesBaixo.setVisibility(View.VISIBLE);
        zoomIn.setVisibility(View.VISIBLE);
        zoomOut.setVisibility(View.VISIBLE);
        arrayDeArquivos = null;

        Uri confio;

        /*
        try (Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null)) {
            System.out.println(cursor);
            if (cursor != null) {
                System.out.println(cursor.moveToNext());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    System.out.println(id);
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    // Acessar o arquivo aqui
                    System.out.println(uri.buildUpon());
                    exibirImagemPastaSelecionada(uri);
                }
            }

        }

         */
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

        } else {
            confio = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AWhatsApp%2FMedia%2FWhatsApp%20Images");
            System.out.println(confio);
            exibirImagemPastaSelecionada(confio);
        }

         */

    }

    private final ActivityResultLauncher<Intent> manageExternalStorageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permissão concedida
                        acessoNaPasta = true;
                    } else {
                        // Permissão negada
                        acessoNaPasta = false;
                        requestManageExternalStoragePermission();
                    }
                }
            }
    );

    TextView textUri;

    TextView cuidadoPastaRaiz;
    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                cuidadoPastaRaiz = findViewById(R.id.cuidado_pasta_raiz);
                layoutImagens = findViewById(R.id.layout_imagens);
                constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);
                layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

                textUri = findViewById(R.id.text_uri);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permissão concedida
                        if (result.getData() != null && result.getData().toString().equals("Intent { dat=content://com.android.externalstorage.documents/tree/primary: flg=0xc3 }")) {
                            cuidadoPastaRaiz.setVisibility(View.VISIBLE);
                            layoutImagens.setVisibility(View.GONE);
                            constraintIconesBaixo.setVisibility(View.GONE);
                            zoomIn.setVisibility(View.GONE);
                            zoomOut.setVisibility(View.GONE);
                            layoutPastaCentral.setVisibility(View.VISIBLE);
                        } else {
                            cuidadoPastaRaiz.setVisibility(View.GONE);
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null && data.getData() != null) {
                                    pastaSelecionada = data.getData();

                                    getContentResolver().takePersistableUriPermission(pastaSelecionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                    // Continuar com o processamento da pasta selecionada
                                    layoutImagens.setVisibility(View.VISIBLE);
                                    constraintIconesBaixo.setVisibility(View.VISIBLE);
                                    zoomIn.setVisibility(View.VISIBLE);
                                    zoomOut.setVisibility(View.VISIBLE);

                                    System.out.println(data.getData().toString());
                                    String sla = data.getData().toString();
                                    textUri.setText(sla);
                                    exibirImagemPastaSelecionada(data.getData());
                                }
                            }
                        }
                    } else {
                        // Permissão negada
                        requestManageExternalStoragePermission();
                    }
                } else {
                    System.out.println("API menor que Android 11");
                    //Proibindo acesso à pasta raiz, evitando que delete arquivos essenciais
                    if (result.getData() != null && result.getData().toString().equals("Intent { dat=content://com.android.externalstorage.documents/tree/primary: flg=0xc3 }")) {
                        cuidadoPastaRaiz.setVisibility(View.VISIBLE);
                        layoutImagens.setVisibility(View.GONE);
                        constraintIconesBaixo.setVisibility(View.GONE);
                        zoomIn.setVisibility(View.GONE);
                        zoomOut.setVisibility(View.GONE);
                        layoutPastaCentral.setVisibility(View.VISIBLE);
                    } else {
                        cuidadoPastaRaiz.setVisibility(View.GONE);
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                pastaSelecionada = data.getData();
                                System.out.println(pastaSelecionada);

                                getContentResolver().takePersistableUriPermission(pastaSelecionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                // Continuar com o processamento da pasta selecionada

                                layoutImagens.setVisibility(View.VISIBLE);
                                constraintIconesBaixo.setVisibility(View.VISIBLE);
                                zoomIn.setVisibility(View.VISIBLE);
                                zoomOut.setVisibility(View.VISIBLE);

                                System.out.println(data.getData().toString());
                                String sla = data.getData().toString();
                                textUri.setText(sla);

                                exibirImagemPastaSelecionada(data.getData());
                            }
                        }
                    }
                }
            });


    @Override
    public boolean dispatchTouchEvent(MotionEvent evento) {
        if (evento.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && view.getId() != R.id.navigation_drawer) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                if (!rect.contains((int) evento.getRawX(), (int) evento.getRawY())) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);
                    layoutPastaCentral.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.dispatchTouchEvent(evento);
    }

    DocumentFile[] arrayDeArquivos;

    public void selecionarPasta() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        arrayDeArquivos = null;
        selecionarPastaLauncher.launch(intent);
    }

    int tamanhoDaLista;

    AtomicReference<DocumentFile> arquivoAtual = new AtomicReference<>();
    AtomicInteger resultado = new AtomicInteger(0);
    int voltador = 0;
    TextView carregado;
    DocumentFile arquivos;
    int quantidadeDeImagens = 1;

    //Controla e ordena os arquivos da pasta
    private void exibirImagemPastaSelecionada(Uri pastaSelecionada) {

        carregado = findViewById(R.id.carregado);
        if (pastaSelecionada != null) {

            arquivos = DocumentFile.fromTreeUri(this, pastaSelecionada);

            assert arquivos != null;
            DocumentFile[] listaDeArquivos;
            listaDeArquivos = arquivos.listFiles();

            arrayDeArquivos = listaDeArquivos;
            tamanhoDaLista = listaDeArquivos.length;

            //Ordena a listaDeArquivos do último modificado ao primeiro (pastas ficam no fim)
            pastaJaFoiSelecionada = true;

            AlertDialog.Builder builderDois = new AlertDialog.Builder(this);
            builderDois.setTitle("Ordenar arquivos");
            builderDois.setMessage("Deseja ordenar os arquivos por data de modificação?\n\nCaso não, carregará instantâneamente!");
            builderDois.setPositiveButton("Sim", (dialog, which) -> {
                // Criar e exibir o ProgressBar
                ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setMax(tamanhoDaLista * 7);

                // Ordenar a lista de arquivos
                Handler handler = new Handler(Looper.getMainLooper());
                Executor executor = Executors.newSingleThreadExecutor();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ordenando arquivos");
                builder.setMessage("Aguarde enquanto os arquivos são ordenados...");
                builder.setView(progressBar);
                AlertDialog dialogo = builder.create();
                dialogo.show();

                executor.execute(() -> {

                    // Ordenar a lista de arquivos
                    Arrays.sort(listaDeArquivos, (file1, file2) -> {
                        handler.post(() -> progressBar.incrementProgressBy(1));
                        return Long.compare(file2.lastModified(), file1.lastModified());
                    });

                    handler.post(() -> {
                        dialogo.dismiss();
                        mostrarArquivosManipulaveis();
                    });
                });
            });
            builderDois.setNegativeButton("Não", (dialog, which) -> {
                // Coloque aqui o código que deve ser executado se o usuário escolher não ordenar os arquivos
                mostrarArquivosManipulaveis();
            });
            builderDois.show();

        }
    }

    public void mostrarArquivosManipulaveis() {
        imageView = findViewById(R.id.view_imagem);
        videoView = findViewById(R.id.view_video);
        botaoAvancar = findViewById(R.id.aceitar_imagem);
        botaoExcluir = findViewById(R.id.negar_imagem);
        botaoVoltar = findViewById(R.id.voltar_imagem);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);
        numeroLixeira = findViewById(R.id.numero_lixeira);

        View.OnClickListener onClickListener = v -> {
            if (v.getId() == R.id.negar_imagem) {
                excluindoArquivo();
            } else if (v.getId() == R.id.aceitar_imagem) {
                guardandoArquivo();
            } else if (v.getId() == R.id.voltar_imagem) {
                int indexImagemAnterior = imagensCarregadas - 2;
                if (indexImagemAnterior >= 0) {
                    DocumentFile imagemAnterior = arrayDeArquivos[indexImagemAnterior];
                    int indexArquivoExcluido = -1;
                    for (int i = 0; i < listaDeExclusao.size(); i++) {
                        DocumentFile arquivoExcluido = listaDeExclusao.get(i);
                        if (arquivoExcluido.getUri().equals(imagemAnterior.getUri())) {
                            indexArquivoExcluido = i;
                            break;
                        }
                    }
                    if (indexArquivoExcluido != -1) {
                        listaDeExclusao.remove(indexArquivoExcluido);
                        saveExclusionList();
                        numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
                    }
                }
                if (imagensCarregadas > 1) {
                    resultado.set(-2);
                    voltador = -2;
                } else {
                    resultado.set(-1);
                    voltador = -1;
                }
                iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
                botaoAvancar = findViewById(R.id.aceitar_imagem);
                botaoExcluir = findViewById(R.id.negar_imagem);

                layoutPastaCentral.setVisibility(View.GONE);
                iconeMaisImagens.setVisibility(View.VISIBLE);
                botaoAvancar.setVisibility(View.VISIBLE);
                botaoExcluir.setVisibility(View.VISIBLE);
            }
            continuarLoop(quantidadeDeImagens + voltador, resultado.get());
        };

        botaoAvancar.setOnClickListener(onClickListener);
        botaoExcluir.setOnClickListener(onClickListener);
        botaoVoltar.setOnClickListener(onClickListener);

        if (arquivos.length() > 0) {
            layoutPastaCentral.setVisibility(View.GONE);
            continuarLoop(quantidadeDeImagens, voltador);
        }
    }

    private ObjectAnimator animator;

    public void voltarIconesAparecer() {
        animator = ObjectAnimator.ofFloat(constraintIconesCima, "alpha", 1);
        animator.setDuration(200);
        animator.start();
        animator = ObjectAnimator.ofFloat(constraintIconesBaixo, "alpha", 1);
        animator.setDuration(200);
        animator.start();
    }

    public void voltarIconesCimaAparecer() {
        animator = ObjectAnimator.ofFloat(constraintIconesCima, "alpha", 1);
        animator.setDuration(200);
        animator.start();
    }

    public void esconderIcones() {
        animator = ObjectAnimator.ofFloat(constraintIconesCima, "alpha", alphaIcone);
        animator.setDuration(200);
        animator.start();
        animator = ObjectAnimator.ofFloat(constraintIconesBaixo, "alpha", alphaIcone);
        animator.setDuration(200);
        animator.start();
    }

    ImageButton playButton;
    ImageButton pauseButton;
    SeekBar seekBar;

    private final Handler handler = new Handler(Looper.getMainLooper());
    //Feito para atualizar a bolinha do player de áudio
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

                // Atualizar o TextView com o tempo percorrido
                int duracao = mediaPlayer.getCurrentPosition();
                int segundos = (duracao / 1000) % 60;
                int minutos = (duracao / (1000 * 60)) % 60;
                int horas = (duracao / (1000 * 60 * 60)) % 24;
                String textoAudioTempoAtual;
                if (horas > 0) {
                    textoAudioTempoAtual = String.format("%02d:%02d:%02d", horas, minutos, segundos);
                } else {
                    textoAudioTempoAtual = String.format("%02d:%02d", minutos, segundos);
                }
                tempoAudioPercorrido.setText(textoAudioTempoAtual);

                handler.postDelayed(this, 200);
            }
        }
    };

    //serve para passar o estado do switch para o SelecionadosFragment.java
    public boolean isAutoplayChecked() {
        Switch autoplaySwitch = findViewById(R.id.switchAutoPlay);
        return autoplaySwitch.isChecked();
    }

    ConstraintLayout constraintAudio;
    ConstraintLayout extensaoDesconhecida;
    TextView textoNomeArquivo;
    TextView textoAviso;
    TextView nomeAudio;
    TextView tempoAudioMaximo;
    TextView tempoAudioPercorrido;

    //Toda vez que apertar um botão ou selecionar uma pasta, continuaLoop é chamado
    private void continuarLoop(int quantidadeDeImagens, int ajuste) {

        autoplaySwitch = findViewById(R.id.switchAutoPlay);
        constraintAudio = findViewById(R.id.constraint_audio);
        layoutImagens.setVisibility(View.VISIBLE);
        extensaoDesconhecida = findViewById(R.id.constraint_extensao_desconhecida);
        textoNomeArquivo = findViewById(R.id.texto_nome_arquivo);
        textoAviso = findViewById(R.id.texto_aviso);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);
        numeroLixeira = findViewById(R.id.numero_lixeira);
        tempoAudioMaximo = findViewById(R.id.tempo_audio_maximo);
        tempoAudioPercorrido = findViewById(R.id.tempo_audio_percorrido);
        nomeAudio = findViewById(R.id.nome_audio);

        iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
        botaoAvancar = findViewById(R.id.aceitar_imagem);
        botaoExcluir = findViewById(R.id.negar_imagem);

        botaoVoltar.setVisibility(View.VISIBLE);
        iconeMaisImagens.setVisibility(View.VISIBLE);
        botaoAvancar.setVisibility(View.VISIBLE);
        botaoExcluir.setVisibility(View.VISIBLE);
        constraintIconesBaixo.setVisibility(View.VISIBLE);

        if (resetouPasta) {
            imagensCarregadas = 0;
        }

        voltarIconesAparecer();
        numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));

        //Para não sobrecarregar com quantidade de imagens, ele carrega apenas a atual
        for (int i = imagensCarregadas + voltador; i < imagensCarregadas + quantidadeDeImagens && i < tamanhoDaLista; i++) {

            resetouPasta = false;
            DocumentFile arquivo = arrayDeArquivos[i];
            String caminhoArquivo = arquivo.getUri().toString();
            String extensao = arquivo.getName();

            if (arquivo.getType() != null && extensao != null) {
                if (isImagem(extensao)) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    constraintAudio.setVisibility(View.GONE);
                    extensaoDesconhecida.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.VISIBLE);
                    zoomOut.setVisibility(View.VISIBLE);

                    Glide.with(this)
                            .load(caminhoArquivo)
                            .into(imageView);

                } else if (isVideo(extensao)) {
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    constraintAudio.setVisibility(View.GONE);
                    extensaoDesconhecida.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.GONE);
                    zoomOut.setVisibility(View.GONE);

                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);

                    videoView.setVideoPath(caminhoArquivo);

                    if (autoplaySwitch.isChecked()) {
                        videoView.start();
                    } else {
                        videoView.pause();
                    }

                } else if (isAudio(extensao)) {
                    constraintAudio.setVisibility(View.VISIBLE);
                    extensaoDesconhecida.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.GONE);
                    zoomOut.setVisibility(View.GONE);

                    try {
                        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(arquivo.getUri(), "r");
                        if (parcelFileDescriptor != null) {
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                            if (mediaPlayer == null) {
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setOnPreparedListener(mp -> {
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    handler.post(updateSeekBarRunnable);
                                });
                            } else {
                                mediaPlayer.reset();
                            }

                            mediaPlayer.setDataSource(fileDescriptor);
                            mediaPlayer.prepareAsync();

                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(this, arquivo.getUri());
                            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                            nomeAudio.setText(title);

                            playButton = findViewById(R.id.play_button);
                            pauseButton = findViewById(R.id.pause_button);
                            seekBar = findViewById(R.id.seek_bar);

                            playButton.setVisibility(View.VISIBLE);
                            pauseButton.setVisibility(View.GONE);

                            mediaPlayer.setOnPreparedListener(mp -> {
                                int duracao = mediaPlayer.getDuration();
                                int segundos = (duracao / 1000) % 60;
                                int minutos = (duracao / (1000 * 60)) % 60;
                                int horas = (duracao / (1000 * 60 * 60)) % 24;
                                String textoTempoDuracaoTotal;
                                if (horas > 0) {
                                    textoTempoDuracaoTotal = String.format("%02d:%02d:%02d", horas, minutos, segundos);
                                } else {
                                    textoTempoDuracaoTotal = String.format("%02d:%02d", minutos, segundos);
                                }
                                tempoAudioMaximo.setText(textoTempoDuracaoTotal);

                                seekBar.setMax(mediaPlayer.getDuration());
                                handler.post(updateSeekBarRunnable);

                                if (autoplaySwitch.isChecked()) {
                                    mediaPlayer.start();
                                    playButton.setVisibility(View.GONE);
                                    pauseButton.setVisibility(View.VISIBLE);
                                }
                            });

                            playButton.setOnClickListener(v -> {
                                mediaPlayer.start();
                                playButton.setVisibility(View.GONE);
                                pauseButton.setVisibility(View.VISIBLE);
                            });

                            pauseButton.setOnClickListener(v -> {
                                mediaPlayer.pause();
                                playButton.setVisibility(View.VISIBLE);
                                pauseButton.setVisibility(View.GONE);
                            });

                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser) {
                                        mediaPlayer.seekTo(progress);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //Se for uma extensão diferente das de dentro de isImagem, isAudio e isVideo, mas existe uma extensão.
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.GONE);
                    constraintAudio.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.GONE);
                    zoomOut.setVisibility(View.GONE);

                    extensaoDesconhecida.setVisibility(View.VISIBLE);
                    textoNomeArquivo.setText(arquivo.getName());
                    textoAviso.setText("Extensão não suportada\nNão recomendamos que exclua.");
                }
            } else {
                //Caso não tenha Extensão
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                constraintAudio.setVisibility(View.GONE);
                zoomIn.setVisibility(View.GONE);
                zoomOut.setVisibility(View.GONE);

                extensaoDesconhecida.setVisibility(View.VISIBLE);
                textoNomeArquivo.setText(arquivo.getName());
                textoAviso.setText("Extensão não encontrada.\nPode ser uma pasta ou arquivo sem extensão.\nNão recomendamos que exclua.");
            }

            arquivoAtual.set(arquivo);
            resultado.set(0);
            ajuste = resultado.get();
            voltador = 0;
        }

        if (ajuste >= 0) {
            imagensCarregadas += quantidadeDeImagens;
        } else {
            imagensCarregadas += ajuste;
            if (imagensCarregadas < 0) {
                imagensCarregadas = 0;
            }
        }

        // Verifica se é a última imagem da pasta
        if (imagensCarregadas > tamanhoDaLista) {
            // Exibe uma mensagem ao usuário
            Toast.makeText(this, "A pasta terminou! Sua lista de excluídos ainda está lá. Você pode procurar outra pasta.", Toast.LENGTH_SHORT).show();

            voltarAoNormalTamanhoETransaltion();
            voltarIconesCimaAparecer();
            videoView.pause();
            pastaJaFoiSelecionada = false;
            imagensCarregadas = 0;

            constraintIconesBaixo.setVisibility(View.GONE);
            layoutPastaCentral.setVisibility(View.VISIBLE);
            layoutImagens.setVisibility(View.GONE);
            zoomIn.setVisibility(View.GONE);
            zoomOut.setVisibility(View.GONE);
        }
    }

    private boolean isImagem(String extensao) {
        return extensao.endsWith("jpg") || extensao.endsWith("png") || extensao.endsWith("jpeg") || extensao.endsWith("webp");
    }

    private boolean isVideo(String extensao) {
        return extensao.endsWith("mp4") || extensao.endsWith("3gp") || extensao.equalsIgnoreCase(".wmv");
    }

    private boolean isAudio(String extensao) {
        return extensao.endsWith("wav") || extensao.endsWith("mp3") || extensao.endsWith("m4a") || extensao.endsWith("wma");
    }

}