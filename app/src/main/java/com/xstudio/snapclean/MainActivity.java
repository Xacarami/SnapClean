package com.xstudio.snapclean;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.xstudio.snapclean.fragments.SelecionadosFragment;
import com.xstudio.snapclean.fragments.Tutorial;

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
    private static final int REQUEST_MEDIA_PERMISSIONS = 1;
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

    private final ArrayList<DocumentFile> listaDeExclusao = new ArrayList<>();
    //O testeSeJaFoiCriado diz que não foi usado, mas é importante
    SelecionadosFragment testeSeJaFoiCriado;
    Boolean resetouPasta = false;
    FrameLayout layoutImagens;
    ConstraintLayout constraintIconesCima;
    ImageButton iconeMaisImagens;
    Switch switchBackup;
    //ConstraintLayout botaoWhatsapp;
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
    float MIN_DISTANCE = 120;
    float alphaIcone = 0.05f;
    SelecionadosFragment selecionadosFragment;

    private static final String[] STORAGE_PERMISSIONS = {READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] MEDIA_PERMISSIONS = {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO};

    //Isso servirá para desativar os ícones ao abrir a sideBar
    ArrayList<View> icons = new ArrayList<>();
    //private String meuEstado;
    DocumentFile[] listaDeArquivos;
    //private ArrayList<Uri> listaDeImagens;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ads do AdMob
        MobileAds.initialize(this, initializationStatus -> {});

        carregaAd();

        //hello = findViewById(R.id.hello);

        //muda a cor da barra de status, pretendo comentado até adaptar para apis menores
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getWindow().setStatusBarColor(Color.parseColor("#161618"));
        //}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Verifica se as permissões foram concedidas
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                acessoNaPasta = false;

                // Solicita as permissões
                ActivityCompat.requestPermissions(this, MEDIA_PERMISSIONS, REQUEST_MEDIA_PERMISSIONS);
            } else {
                // As permissões já foram concedidas
                System.out.println("Permissão concedida Android 13+");
                acessoNaPasta = true;
            }

        } else {
            //Já garante a permissão de primeira
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                System.out.println("Permissão negada");
                acessoNaPasta = false;
                Toast.makeText(this, R.string.pedindoAcesso, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS);

                //hello.setText("Permissões ainda não aceitas");
            } else {
                System.out.println("Permissão concedida");
                acessoNaPasta = true;
                //hello.setText("Permissões concedidas anteriormente");
            }
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
            ConstraintLayout layoutPrincipal = findViewById(R.id.layout_principal);
            layoutPrincipal.setVisibility(View.GONE);
            if (testeSeJaFoiCriado == null) {
                selecionadosFragment = new SelecionadosFragment(listaDeExclusao);
                selecionadosFragment.setOnArquivoRecuperadoListener(this);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_selecionados, selecionadosFragment).addToBackStack(null) // Adiciona o fragmento à pilha de retorno
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
            Toast.makeText(MainActivity.this, R.string.aindaNaoDisponivel, Toast.LENGTH_LONG).show();
        });

        //Adição futura para selecionar de forma mais rápida pastas específicas sem precisar navegar pelo gerenciador de arquivos
        /*
        botaoWhatsapp = findViewById(R.id.botao_whatsapp);
        botaoWhatsapp.setOnClickListener(view -> {
            System.out.println("Ta indo");
            if (acessoNaPasta) {
                coisaDeAndroidR();
            } else {
                showPermissionExplanationDialog();
            }
        });

         */

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

        switchBackup.setOnClickListener(v -> saveExclusionList());

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

    AdRequest adRequest = new AdRequest.Builder().build();

    public void carregaAd(){
        InterstitialAd.load(this,"ca-app-pub-6308102348315332/3641604482", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                }
        );
    }

    public void mostraAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
            carregaAd();
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
        controladorDeAd = 0;
    }

    //Gerencia a permissão de visualizar e escrever os arquivos do usuário
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("PERMISSÕES ANDROID 13+ GARANTIDAS!!!!!!!!!");
                    acessoNaPasta = true;
                } else {
                    System.out.println("PERMISSÕES ANDROID 13+ NEGADAS");
                    acessoNaPasta = false;
                    showPermissionExplanationDialog();
                }
            }
        } else {
            //Verifica se as permissões foram concedidas
            if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
                //Verifica as permissões, dentro disso vai o que ta permitido
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
    }

    //Mensagem que aparece quando a pessoa nega as permissões
    private void showPermissionExplanationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Solicita as permissões
            new AlertDialog.Builder(this).setMessage(R.string.mensagemAposRecusaDePermissoes).setPositiveButton(R.string.botaoPositivoOk, (dialog, which) -> ActivityCompat.requestPermissions(this, MEDIA_PERMISSIONS, REQUEST_MEDIA_PERMISSIONS)).create().show();
        } else {
            new AlertDialog.Builder(this).setMessage(R.string.mensagemAposRecusaDePermissoes).setPositiveButton(R.string.botaoPositivoOk, (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS)).create().show();
        }
    }

    //toda vez que saveExclusionList é chamado, o backup se atualiza com a listaDeExclusao
    Set<String> exclusionList = new LinkedHashSet<>();

    public void saveExclusionList() {
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
        Toast.makeText(MainActivity.this, R.string.aindaNaoDisponivel, Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                pastaSelecionada = data.getData();
            }
        }
    }

    private void exibidorDePasta(Uri data) {
        cuidadoPastaProtegida = findViewById(R.id.cuidado_pasta_raiz);
        layoutImagens = findViewById(R.id.layout_imagens);
        constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

        // Continuar com o processamento da pasta selecionada
        layoutImagens.setVisibility(View.VISIBLE);
        constraintIconesBaixo.setVisibility(View.VISIBLE);
        zoomIn.setVisibility(View.VISIBLE);
        zoomOut.setVisibility(View.VISIBLE);

        exibirImagemPastaSelecionada(data);
    }

    //Código criado para no futuro criar botões que carreguem direto em uma pasta sem navegar
    //pelo gerenciador de arquivo, e se preparar para caso o android seja 10- ou 11+
    /*
    private void coisaDeAndroidR(){

        arrayDeArquivos = null;
        String minhaString = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2FWhatsApp%20Images";
        Uri minhaUri = Uri.parse(minhaString);

        exibidorDePasta(minhaUri);
    }

     */


    //TextView textUri;

    TextView cuidadoPastaProtegida;
    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        cuidadoPastaProtegida = findViewById(R.id.cuidado_pasta_raiz);
        layoutImagens = findViewById(R.id.layout_imagens);
        constraintIconesBaixo = findViewById(R.id.constraint_icones_baixo);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

        //Serve para debug a linha abaixo
        //textUri = findViewById(R.id.text_uri);

        System.out.println("API menor que Android 11");
        //System.out.println(result.getData().toString());
        //Proibindo acesso à pasta raiz, evitando que delete arquivos essenciais
        Intent data = result.getData();
        if (data != null) {
            String dataString = data.toString();
            if (dataString.equals("Intent { dat=content://com.android.externalstorage.documents/tree/primary: flg=0xc3 }") || dataString.equals("Intent { dat=content://com.android.providers.downloads.documents/tree/downloads flg=0xc3 }")) {
                cuidadoPastaProtegida.setVisibility(View.VISIBLE);
                layoutImagens.setVisibility(View.GONE);
                constraintIconesBaixo.setVisibility(View.GONE);
                zoomIn.setVisibility(View.GONE);
                zoomOut.setVisibility(View.GONE);
                layoutPastaCentral.setVisibility(View.VISIBLE);

            } else {
                cuidadoPastaProtegida.setVisibility(View.GONE);
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Intent data = result.getData();
                    if (data.getData() != null) {
                        pastaSelecionada = data.getData();

                        getContentResolver().takePersistableUriPermission(pastaSelecionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        exibidorDePasta(pastaSelecionada);
                        //textUri.setText(data.getData().toString());
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
        System.out.println(intent);
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

        System.out.println("Pasta certa -> " + pastaSelecionada);
        carregado = findViewById(R.id.carregado);
        if (pastaSelecionada != null) {

            arquivos = DocumentFile.fromTreeUri(this, pastaSelecionada);

            assert arquivos != null;
            listaDeArquivos = arquivos.listFiles();

            arrayDeArquivos = listaDeArquivos;
            tamanhoDaLista = listaDeArquivos.length;

            //Ordena a listaDeArquivos do último modificado ao primeiro (pastas ficam no fim)
            pastaJaFoiSelecionada = true;

            AlertDialog.Builder builderDois = new AlertDialog.Builder(this);
            builderDois.setTitle(R.string.tituloOrdenarArquivos);
            builderDois.setMessage(R.string.mensagemDesejaOrdenarPorData);
            builderDois.setPositiveButton("Sim", (dialog, which) -> {
                // Criar e exibir o ProgressBar
                ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setMax(tamanhoDaLista * 7);

                // Ordenar a lista de arquivos
                Handler handler = new Handler(Looper.getMainLooper());
                Executor executor = Executors.newSingleThreadExecutor();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.tituloOrdenandoArquivos);
                builder.setMessage(R.string.mensagemAguardeEnquantoOrdena);
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
            builderDois.setNegativeButton(R.string.botaoNegativoNao, (dialog, which) -> {
                // Coloque aqui o código que deve ser executado se o usuário escolher não ordenar os arquivos
                mostrarArquivosManipulaveis();
            });
            builderDois.show();

        }
    }

    int controladorDeAd = 0;

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
                controladorDeAd -= 2;
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

    //Não fechar de primeira ao apertar o botão de volar do celular
    private long backPressedTime;
    private Toast backToast;
    ImageView imagemTelaInteira;
    ImageButton setaBaixo;

    @Override
    public void onBackPressed() {
        imagemTelaInteira = findViewById(R.id.imagem_tela_inteira);
        setaBaixo = findViewById(R.id.ic_seta_baixo);

        //Fechar tela de arquivo aumentado
        if (setaBaixo != null && setaBaixo.getVisibility() == View.VISIBLE) {
            selecionadosFragment.minimizarTelaCheia();
        } else {
            //Fechar a selecionadosFragment
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                ConstraintLayout layoutPrincipal = findViewById(R.id.layout_principal);
                layoutPrincipal.setVisibility(View.VISIBLE);
                getSupportFragmentManager().popBackStack();
                System.out.println("Fragment aberto?");
            } else {
                //Fechar a sidebar
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    //super.onBackPressed();
                    //fechar a MainActivity
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        backToast.cancel();
                        super.onBackPressed();
                        return;
                    } else {
                        backToast = Toast.makeText(getBaseContext(), R.string.pressioneNovamenteParaSair, Toast.LENGTH_SHORT);
                        backToast.show();
                    }
                    backPressedTime = System.currentTimeMillis();
                }

            }
        }

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

        controladorDeAd++;

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
        botaoVoltar = findViewById(R.id.voltar_imagem);

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
            arquivoAtual.set(arquivo);

            if (controladorDeAd % 25 == 0 && controladorDeAd != 0){
                mostraAd();
            }

            if (arquivo.getType() != null && extensao != null) {
                if (isImagem(extensao)) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    constraintAudio.setVisibility(View.GONE);
                    extensaoDesconhecida.setVisibility(View.GONE);
                    zoomIn.setVisibility(View.VISIBLE);
                    zoomOut.setVisibility(View.VISIBLE);

                    Glide.with(this).load(caminhoArquivo).into(imageView);

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
                    textoAviso.setText(R.string.extensaoNaoSuportada);
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
                textoAviso.setText(R.string.extensaoNaoEncontrada);
            }

            //Tutorial para a primeira vez, somente a primeira vez
            SharedPreferences prefs = getSharedPreferences("my_preferences", MODE_PRIVATE);
            boolean tutorialShown = prefs.getBoolean("tutorial_shown", false);

            if (!tutorialShown) {
                Tutorial tutorial = new Tutorial(this);
                tutorial.show();

                // Depois que o tutorial for exibido, atualize o valor nas preferências compartilhadas
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("tutorial_shown", true);
                editor.apply();
            }

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
            Toast.makeText(this, R.string.aPastaTerminou, Toast.LENGTH_SHORT).show();

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
        String[] extensoesImagem = {"jpg", "png", "jpeg", "webp", "gif", "bmp", "raw", "svg"};
        extensao = extensao.toLowerCase();

        for (String extensaoImagem : extensoesImagem) {
            if (extensao.endsWith(extensaoImagem)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideo(String extensao) {
        String[] extensoesVideo = {"mp4", "3gp", "wmv", "mov", "avi", "mkv", "avchd", "webm", "mpeg-2"};
        extensao = extensao.toLowerCase();

        for (String extensaoVideo : extensoesVideo) {
            if (extensao.endsWith(extensaoVideo)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAudio(String extensao) {
        String[] extensoesAudio = {"wav", "mp3", "m4a", "wma", "pcm", "flac"};
        extensao = extensao.toLowerCase();

        for (String extensaoAudio : extensoesAudio) {
            if (extensao.endsWith(extensaoAudio)) {
                return true;
            }
        }
        return false;
    }

}