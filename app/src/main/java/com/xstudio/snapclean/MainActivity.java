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

import android.Manifest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements SelecionadosFragment.OnFragmentInteractionListener {


    TextView hello;
    //TextView textoTeste;
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
    private int offsetImagens = 10;

    ImageButton botaoAvancar;
    ImageButton botaoExcluir;
    ImageButton botaoVoltar;
    ImageButton zoomIn;
    ImageButton zoomOut;
    Button restaurarTamanho;
    Switch autoplaySwitch;
    int tirando = 0;
    int continuando = 0;
    private SelecionadosFragment testeSeJaFoiCriado;
    ConstraintLayout containerSelecionados;
    Boolean resetouPasta = false;
    private ArrayList<DocumentFile> listaDeExclusao = new ArrayList<>();
    FrameLayout layoutImagens;
    ConstraintLayout layoutPrincipal;
    ConstraintLayout constraintIconesCima;
    ImageButton iconeMaisImagens;
    Switch switchBackup;
    private float currentTranslationX = 0f;
    private float currentTranslationY = 0f;
    private float moveScaleFactor = 2f;
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
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Isso servirá para desativar os ícones ao abrir a sideBar
    ArrayList<View> icons = new ArrayList<>();

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
            Toast.makeText(this, "É necessário aceitar as permissões de acesso às pastas!", Toast.LENGTH_SHORT).show();
            hello.setText("Permissões ainda não aceitas");
        } else {
            System.out.println("Permissão concedida");
            acessoNaPasta = true;
            hello.setText("Permissões concedidas anteriormente");
        }


        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);

        // Gravar a decisão passada do usuário sobre autoplay
        Switch autoplaySwitch = findViewById(R.id.switchAutoPlay);
        autoplaySwitch.setChecked(sharedPreferences.getBoolean(PREF_SWITCH_AUTOPLAY, true));
        autoplaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_SWITCH_AUTOPLAY, isChecked);
                editor.apply();
            }
        });

        // Gravar decisão sobre backup
        Switch switchBackup = findViewById(R.id.switchBackup);
        switchBackup.setChecked(sharedPreferences.getBoolean(PREF_SWITCH_BACKUP, true));
        switchBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_SWITCH_BACKUP, isChecked);
                editor.apply();
            }
        });



        drawerLayout = findViewById(R.id.drawer_layout);

        ImageView pastaCima = findViewById(R.id.pasta_cima);
        ImageView pastaCentral = findViewById(R.id.pasta_central);

        ImageButton optionIcon = findViewById(R.id.opcoes);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

        optionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
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
        icLixeira1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (testeSeJaFoiCriado == null) {
                    SelecionadosFragment selecionadosFragment = new SelecionadosFragment(listaDeExclusao);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_selecionados, selecionadosFragment)
                            .commit();
                } else {
                    testeSeJaFoiCriado.atualizarListaDeExclusao(listaDeExclusao);
                }
                layoutPrincipal = findViewById(R.id.layout_principal);
                layoutPrincipal.setVisibility(View.GONE);
            }
        });


        iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
        iconeMaisImagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Ta clicando Mais Imagens");
                colocarToast();
            }
        });

        ImageButton iconeInfo = findViewById(R.id.info_button);
        iconeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Ta clicando Infos");
                Toast.makeText(MainActivity.this, "Ainda não disponível. Espere uma atualização.", Toast.LENGTH_SHORT).show();
            }
        });


        //Selecionador de pastas
        View.OnClickListener selecionarPastaClickListener = v -> {
            System.out.println(pastaJaFoiSelecionada);

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

        zoomIn.setOnClickListener(new View.OnClickListener() {
            private ObjectAnimator animator;

            @Override
            public void onClick(View v) {
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
                        System.out.println("Zoom Dentro");
                        restaurarTamanho.setVisibility(View.VISIBLE);
                        layoutImagens.setTranslationX(0);
                        layoutImagens.setTranslationY(0);
                    }
                }
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            private ObjectAnimator animator;

            @Override
            public void onClick(View v) {
                if (layoutImagens.getScaleX() >= 0.125) {
                    layoutImagens.setScaleX(layoutImagens.getScaleX() * 0.5f);
                    layoutImagens.setScaleY(layoutImagens.getScaleY() * 0.5f);
                    System.out.println(layoutImagens.getScaleX());
                    System.out.println(layoutImagens.getScaleY());

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
            }
        });

        restaurarTamanho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voltarAoNormalTamanhoETransaltion();
            }
        });

        settings = getSharedPreferences(PREFS_NAME, 0);

        // Recuperar a lista de exclusão salva
        switchBackup = findViewById(R.id.switchBackup);
        Set<String> exclusionList = settings.getStringSet(PREF_EXCLUSION_LIST, new LinkedHashSet<String>());
        if (switchBackup.isChecked()){
            for (String uriString : exclusionList) {
                Uri uri = Uri.parse(uriString);
                DocumentFile file = DocumentFile.fromSingleUri(this, uri);
                listaDeExclusao.add(file);
            }
        } else {
            listaDeExclusao.clear();
            exclusionList.clear();
        }

        numeroLixeira = findViewById(R.id.numero_lixeira);
        numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));


        layoutImagens.setOnTouchListener(new View.OnTouchListener() {
            private ObjectAnimator animator;
            private boolean isAnimating = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //scaleDetector.onTouchEvent(event);
                if (isZooming && permitirArrasto) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            float currentX = event.getX();
                            float deltaX = (currentX - x1) * 1f + currentTranslationX;
                            layoutImagens.setTranslationX(deltaX);

                            float currentY = event.getY();
                            float deltaY = (currentY - y1) * 1f + currentTranslationY;
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
                if (permitirArrasto){
                    System.out.println("Ta fora do isZooming");
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
                                    System.out.println("Arrastou para guardar");
                                    voltarIconesAparecer();
                                    guardandoArquivo();
                                } else {
                                    // Deslizou da direita para a esquerda - Excluir arquivo
                                    animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", -layoutImagens.getWidth());
                                    System.out.println("Arrastou para excluir");
                                    excluindoArquivo();
                                    voltarIconesAparecer();
                                }
                                animator.setDuration(200);
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        isAnimating = false;
                                        layoutImagens.setTranslationX(0);
                                        continuarLoop(1 + voltador, resultado.get());
                                    }
                                });
                                animator.start();
                                isAnimating = true;
                                voltarIconesAparecer();
                            } else {
                                // Deslizou menos que MIN_DISTANCE, então volta para o centro
                                animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", 0);
                                animator.setDuration(200);
                                animator.start();
                                isAnimating = true;
                                voltarIconesAparecer();
                            }
                            break;
                    }
                    v.performClick();

                    // Aqui você pode adicionar o código para detectar o pressionamento longo
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Inicia o pressionamento longo
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Aqui você pode adicionar o código para tornar os ícones mais transparentes
                                System.out.println("Segurando o dedo no layout");
                                constraintIconesCima = findViewById(R.id.constraint_icones_cima);
                                constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);

                            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Verifica se as permissões foram concedidas
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            //Verifica as permissões, dentro disso vai o que ta permitido
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("PERMISSÕES GARANTIDAS!!!!!!!!!!!!");
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
        new AlertDialog.Builder(this)
                .setMessage("Este aplicativo precisa de permissão para acessar suas pastas para funcionar corretamente. Por favor, conceda a permissão\nCaso tenha apertado em Não perguntar novamente, terá de reinstalar o aplicativo.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS);
                    }
                })
                .create()
                .show();
    }

    Set<String> exclusionList = new LinkedHashSet<>();
    private void saveExclusionList(DocumentFile documentFile) {
        switchBackup = findViewById(R.id.switchBackup);
        if (switchBackup.isChecked()){
            exclusionList.add(documentFile.getUri().toString());
            /*
            for (DocumentFile file : listaDeExclusao) {
                exclusionList.add(file.getUri().toString());
            }

             */
            for(DocumentFile file : listaDeExclusao){
                System.out.println("listaDeExclusao -> "+file.getName());
            }
            for(String file : exclusionList){
                System.out.println("exclusionList -> "+file);
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
                System.out.println("Já tem");
                System.out.println(arquivoExcluido.getUri());
                break;
            }
        }
        if (!arquivoJaExcluido) {
            listaDeExclusao.add(0, arquivoAtual.get());
            saveExclusionList(arquivoAtual.get());
            numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
            System.out.println(arquivoAtual.get().getUri());
        }

        System.out.println("Clicou no botao de excluir");
        System.out.println(listaDeExclusao);
    }

    public void guardandoArquivo(){
        for (DocumentFile arquivoExcluido : listaDeExclusao) {
            if (arquivoExcluido.getUri().equals(arquivoAtual.get().getUri())) {
                System.out.println("Já tem");
                listaDeExclusao.remove(arquivoExcluido);
                exclusionList.remove(arquivoExcluido);
                numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
                System.out.println(arquivoExcluido.getUri());
                break;
            }
        }
        saveExclusionList(arquivoAtual.get());
    }


    public void voltarAoNormalTamanhoETransaltion() {
        isZooming = false;
        layoutImagens.setTranslationX(0);
        layoutImagens.setTranslationY(0);
        layoutImagens.setScaleX(1);
        layoutImagens.setScaleY(1);
        restaurarTamanho.setVisibility(View.GONE);
        voltarIconesAparecer();
    }


    public void colocarToast() {
        Toast.makeText(MainActivity.this, "Ainda não disponível. Espere uma atualização.", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode);
        System.out.println(resultCode);
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
            System.out.println("dentro de onActivityResult o primeiro if passou");
            if (data != null && data.getData() != null) {

                pastaSelecionada = data.getData();
                System.out.println("uma linha antes de chamar a função exibirImagem");
            }
        }
    }

    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        pastaSelecionada = data.getData();

                        getContentResolver().takePersistableUriPermission(pastaSelecionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Continuar com o processamento da pasta selecionada
                        layoutImagens = findViewById(R.id.layout_imagens);
                        layoutImagens.setVisibility(View.VISIBLE);
                        constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);
                        constraintIconesBaixo.setVisibility(View.VISIBLE);
                        zoomIn.setVisibility(View.VISIBLE);
                        zoomOut.setVisibility(View.VISIBLE);
                        exibirImagemPastaSelecionada(data.getData(), 1);
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

    @Override
    public void onPastaCimaSelecionadosClicked() {
        if (!pastaJaFoiSelecionada) {
            selecionarPasta();
            System.out.println(listaDeExclusao);
        } else {
            resetouPasta = true;
            imagensCarregadas = 0;
            selecionarPasta();
            System.out.println("Pasta ja selecionada, e clicou em abrir mais uma");

        }
    }

    DocumentFile[] arrayDeArquivos;

    public void selecionarPasta() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER);
        arrayDeArquivos = null;
        selecionarPastaLauncher.launch(intent);
    }


    int tamanhoDaLista;

    AtomicReference<DocumentFile> arquivoAtual = new AtomicReference<>();
    AtomicInteger resultado = new AtomicInteger(0);
    int voltador = 0;

    //int quantidadeApagadas = 0;
    private void exibirImagemPastaSelecionada(Uri pastaSelecionada, int quantidadeDeImagens) {
        if (pastaSelecionada != null) {
            System.out.println("Ta dentro da função exibirImagemPastaSelecionada");
            DocumentFile arquivos = DocumentFile.fromTreeUri(this, pastaSelecionada);
            DocumentFile[] listaDeArquivos = arquivos.listFiles();

            arrayDeArquivos = listaDeArquivos;
            tamanhoDaLista = listaDeArquivos.length;

            //Ordena a listaDeArquivos do último modificado ao primeiro (pastas ficam no fim)
            Arrays.sort(listaDeArquivos, new Comparator<DocumentFile>() {
                @Override
                public int compare(DocumentFile file1, DocumentFile file2) {
                    return Long.compare(file2.lastModified(), file1.lastModified());
                }
            });

            System.out.println("Acabou de selecionar a pasta");
            pastaJaFoiSelecionada = true;

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
                } else if (v.getId() == R.id.aceitar_imagem){
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
                            exclusionList.remove(listaDeExclusao.get(indexArquivoExcluido));
                            numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
                        }
                    }
                    if (imagensCarregadas > 1) {
                        resultado.set(-2);
                        voltador = -2;
                    } else if (imagensCarregadas <= 1) {
                        resultado.set(-1);
                        voltador = -1;
                    }
                    System.out.println("Clicou no botao de voltar");
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

            if (arquivos != null && arquivos.length() > 0) {
                layoutPastaCentral.setVisibility(View.GONE);
                continuarLoop(quantidadeDeImagens, voltador);
            }
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
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
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


    private void continuarLoop(int quantidadeDeImagens, int ajuste) {

        autoplaySwitch = findViewById(R.id.switchAutoPlay);
        constraintAudio = findViewById(R.id.constraint_audio);
        layoutImagens.setVisibility(View.VISIBLE);
        extensaoDesconhecida = findViewById(R.id.constraint_extensao_desconhecida);
        textoNomeArquivo = findViewById(R.id.texto_nome_arquivo);
        textoAviso = findViewById(R.id.texto_aviso);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);

        iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
        botaoAvancar = findViewById(R.id.aceitar_imagem);
        botaoExcluir = findViewById(R.id.negar_imagem);

        botaoVoltar.setVisibility(View.VISIBLE);
        iconeMaisImagens.setVisibility(View.VISIBLE);
        botaoAvancar.setVisibility(View.VISIBLE);
        botaoExcluir.setVisibility(View.VISIBLE);

        if (resetouPasta) {
            imagensCarregadas = 0;
            System.out.println("testar if");
        }

        voltarIconesAparecer();

        for (int i = imagensCarregadas + voltador; i < imagensCarregadas + quantidadeDeImagens && i < tamanhoDaLista; i++) {
            //for (DocumentFile arquivo : listaDeArquivos) {

            resetouPasta = false;
            System.out.println("i = " + i);
            System.out.println("Voltador = " + voltador);

            DocumentFile arquivo = arrayDeArquivos[i];

            String caminhoArquivo = arquivo.getUri().toString();

            String extensao = arquivo.getName();

            System.out.println(extensao);


            if (arquivo.getType() != null) {
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
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        seekBar.setMax(mediaPlayer.getDuration());
                                        handler.post(updateSeekBarRunnable);
                                    }
                                });
                            } else {
                                mediaPlayer.reset();
                            }

                            mediaPlayer.setDataSource(fileDescriptor);
                            mediaPlayer.prepareAsync();

                            playButton = findViewById(R.id.play_button);
                            pauseButton = findViewById(R.id.pause_button);
                            seekBar = findViewById(R.id.seek_bar);

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    System.out.println(mediaPlayer.getDuration());
                                    handler.post(updateSeekBarRunnable);

                                    if (autoplaySwitch.isChecked()) {
                                        mediaPlayer.start();
                                        playButton.setVisibility(View.GONE);
                                        pauseButton.setVisibility(View.VISIBLE);
                                    } else {
                                        mediaPlayer.pause();
                                        playButton.setVisibility(View.VISIBLE);
                                        pauseButton.setVisibility(View.GONE);
                                    }
                                }
                            });

                            playButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.start();
                                    playButton.setVisibility(View.GONE);
                                    pauseButton.setVisibility(View.VISIBLE);
                                }
                            });

                            pauseButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.pause();
                                    playButton.setVisibility(View.VISIBLE);
                                    pauseButton.setVisibility(View.GONE);
                                }
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
                    System.out.println("---------------------");
                    System.out.println("Arquivo de extensão desconhecida");
                    System.out.println("Tipo: " + arquivo.getType());
                    System.out.println("Nome: " + arquivo.getName());
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.GONE);
                    constraintAudio.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.GONE);
                    zoomOut.setVisibility(View.GONE);

                    extensaoDesconhecida.setVisibility(View.VISIBLE);
                    textoNomeArquivo.setText(arquivo.getName());
                    textoAviso.setText("Extensão não suportada");

                    System.out.println("---------------------");
                }
            } else {
                System.out.println("Não tem extensão");
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                constraintAudio.setVisibility(View.GONE);
                zoomIn.setVisibility(View.GONE);
                zoomOut.setVisibility(View.GONE);

                extensaoDesconhecida.setVisibility(View.VISIBLE);
                textoNomeArquivo.setText(arquivo.getName());
                textoAviso.setText("Extensão não encontrada");
            }

            arquivoAtual.set(arquivo);
            resultado.set(0);
            ajuste = resultado.get();
            //i += voltador;
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

            botaoVoltar = findViewById(R.id.voltar_imagem);
            layoutImagens = findViewById(R.id.layout_imagens);
            iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
            botaoAvancar = findViewById(R.id.aceitar_imagem);
            botaoExcluir = findViewById(R.id.negar_imagem);

            layoutPastaCentral.setVisibility(View.VISIBLE);
            botaoVoltar.setVisibility(View.VISIBLE);
            iconeMaisImagens.setVisibility(View.GONE);
            botaoAvancar.setVisibility(View.GONE);
            botaoExcluir.setVisibility(View.GONE);
            layoutImagens.setVisibility(View.GONE);
            zoomIn.setVisibility(View.GONE);
            zoomOut.setVisibility(View.GONE);

        }
    }

    private boolean isImagem(String extensao) {
        return extensao.endsWith("jpg") || extensao.endsWith("png") || extensao.endsWith("jpeg");
    }

    private boolean isVideo(String extensao) {
        return extensao.endsWith("mp4") || extensao.endsWith("3gp") || extensao.equalsIgnoreCase(".wmv");
    }

    private boolean isAudio(String extensao) {
        return extensao.endsWith("wav") || extensao.endsWith("mp3") || extensao.endsWith("m4a") || extensao.endsWith("wma");
    }

}