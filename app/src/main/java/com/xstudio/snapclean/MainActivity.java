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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.fragments.SelecionadosFragment;

import org.w3c.dom.ls.LSOutput;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements SelecionadosFragment.OnFragmentInteractionListener {


    //TextView hello;
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
    ConstraintLayout constraintIconesBaixo;

    SharedPreferences sharedPreferences;

    float x1,x2;
    float MIN_DISTANCE = 150;

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Isso servirá para desativar os ícones ao abrir a sideBar
    List<View> icons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hello = findViewById(R.id.hello);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Solicitando permissões
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS);
            System.out.println("Permissão talvez negada");
            //hello.setText("Permissões ainda não aceitas");
        } else {
            System.out.println("Permissão concedida");
            //hello.setText("Permissões concedidas anteriormente");
        }

        //Gravar a decisão passada do usuário
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        autoplaySwitch = findViewById(R.id.switchAutoPlay);
        autoplaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("autoplay_switch_state", isChecked);
                editor.apply();
            }
        });

        boolean switchState = sharedPreferences.getBoolean("autoplay_switch_state", true);
        autoplaySwitch.setChecked(switchState);



        drawerLayout = findViewById(R.id.drawer_layout);

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

        ImageButton optionIcon = findViewById(R.id.opcoes);
        layoutPastaCentral = findViewById(R.id.constraintLayout_PastaCentral);

        optionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textoTeste = findViewById(R.id.textoTesteIcone);
                //textoTeste.setText("Apertou icone hamburger");
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

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
            public void onDrawerStateChanged(int newState) {}
        });


        ImageView icLixeira1 = findViewById(R.id.lixeira);
        icLixeira1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //abrirFragmentSelecionados();
                if (testeSeJaFoiCriado == null) {
                    SelecionadosFragment selecionadosFragment = new SelecionadosFragment(listaDeExclusao);
                    getSupportFragmentManager().beginTransaction()
                            //.replace(R.id.testando, selecionadosFragment)
                            .replace(R.id.container_selecionados, selecionadosFragment)
                            .commit();
                } else {
                    testeSeJaFoiCriado.atualizarListaDeExclusao(listaDeExclusao);
                }
                layoutPrincipal = findViewById(R.id.layout_principal);
                layoutPrincipal.setVisibility(View.GONE);
            }
        });



        ImageButton iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
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
        ImageButton botaoSelecionarPasta1 = findViewById(R.id.pasta_cima);
        ImageButton botaoSelecionarPasta2 = findViewById(R.id.pasta_central);

        View.OnClickListener selecionarPastaClickListener = v -> {
            System.out.println(pastaJaFoiSelecionada);

            if (!pastaJaFoiSelecionada) {
                selecionarPasta();
            } else {
                resetouPasta = true;
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

        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float scaleFactor = 1f;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
                layoutImagens.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(0).start();
                return true;
            }
        });

        layoutImagens.setOnTouchListener(new View.OnTouchListener() {
            private ObjectAnimator animator;
            private boolean isAnimating = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleDetector.onTouchEvent(event);

                switch(event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (animator != null && animator.isRunning()) {
                            animator.cancel();
                        }
                        float currentX = event.getX();
                        float deltaX = currentX - x1;
                        layoutImagens.setTranslationX(deltaX);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            if (x2 > x1) {
                                // Deslizou da esquerda para a direita
                                animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", layoutImagens.getWidth());
                            } else {
                                // Deslizou da direita para a esquerda - Excluir arquivo
                                animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", -layoutImagens.getWidth());
                                excluindoArquivo();
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
                        } else {
                            // Deslizou menos que MIN_DISTANCE, então volta para o centro
                            animator = ObjectAnimator.ofFloat(layoutImagens, "translationX", 0);
                            animator.setDuration(200);
                            animator.start();
                            isAnimating = true;
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
                            float alphaIcone = 0.02f;

                            animator = ObjectAnimator.ofFloat(constraintIconesCima, "alpha", alphaIcone);
                            animator.setDuration(200);
                            animator.start();
                            animator = ObjectAnimator.ofFloat(constraintIconesBaixo, "alpha", alphaIcone);
                            animator.setDuration(200);
                            animator.start();

                            //constraintIconesCima.setAlpha(alphaIcone);
                            //constraintIconesBaixo.setAlpha(alphaIcone);
                        }
                    }, ViewConfiguration.getLongPressTimeout());
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Cancela o pressionamento longo

                    animator = ObjectAnimator.ofFloat(constraintIconesCima, "alpha", 1);
                    animator.setDuration(200);
                    animator.start();
                    animator = ObjectAnimator.ofFloat(constraintIconesBaixo, "alpha", 1);
                    animator.setDuration(200);
                    animator.start();

                    //constraintIconesCima.setAlpha(1);
                    //constraintIconesBaixo.setAlpha(1);
                    v.removeCallbacks(null);
                }

                return true;
            }
        });

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
            numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));
            System.out.println(arquivoAtual.get().getUri());
        }

        System.out.println("Clicou no botao de excluir");
        System.out.println(listaDeExclusao);
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


                //pastaSelecionada = getFolderPathFromUri(data.getData());
                pastaSelecionada = data.getData();
                System.out.println("uma linha antes de chamar a função exibirImagem");
                //exibirImagemPastaSelecionada(pastaSelecionada, offsetImagens);
            }
        }
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
                //hello.setText("permissoes concedidas");
            } else {
                System.out.println("PERMISSÕES NEGADAS");
                //hello.setText("É necessário que você aceite as permissões");
            }
        }
    }

    private final ActivityResultLauncher<Intent> selecionarPastaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        //String pastaUri = .getPath();
                        //pastaSelecionada = getFolderPathFromUri(data.getData());
                        pastaSelecionada = data.getData();

                        getContentResolver().takePersistableUriPermission(pastaSelecionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Continuar com o processamento da pasta selecionada
                        layoutImagens = findViewById(R.id.layout_imagens);
                        layoutImagens.setVisibility(View.VISIBLE);
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
            selecionarPasta();
            System.out.println("Pasta ja selecionada, e clicou em abrir mais uma");

        }
    }


    //private ActivityResultLauncher<Intent> folderPickerLauncher;
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
            //numeroLixeira.setText(String.valueOf(quantidadeApagadas));

            int tamanhoLista = listaDeArquivos.length;


            View.OnClickListener onClickListener = v -> {
                if (v.getId() == R.id.negar_imagem) {
                    excluindoArquivo();

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

    ImageButton playButton;
    ImageButton pauseButton;
    SeekBar seekBar;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            //if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 200);
            }
        }
    };


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

        if (resetouPasta){
            imagensCarregadas = 0;
            System.out.println("testar if");
        }

        for (int i = imagensCarregadas + voltador; i < imagensCarregadas + quantidadeDeImagens && i < tamanhoDaLista; i++) {
            //for (DocumentFile arquivo : listaDeArquivos) {

            resetouPasta = false;
            System.out.println("i = " + i);
            System.out.println("Voltador = " + voltador);

            DocumentFile arquivo = arrayDeArquivos[i];

            String caminhoArquivo = arquivo.getUri().toString();

            //String extensao = arquivo.getType();
            String extensao = arquivo.getName();

            System.out.println(extensao);


            if (arquivo.getType() != null) {
                if (isImagem(extensao)) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    constraintAudio.setVisibility(View.GONE);
                    extensaoDesconhecida.setVisibility(View.GONE);

                    Glide.with(this)
                            .load(caminhoArquivo)
                            .into(imageView);

                } else if (isVideo(extensao)) {
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    constraintAudio.setVisibility(View.GONE);
                    extensaoDesconhecida.setVisibility(View.GONE);

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
                    System.out.println("um audio ai");
                    constraintAudio.setVisibility(View.VISIBLE);
                    extensaoDesconhecida.setVisibility(View.GONE);

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
                            //seekBar.setMax(mediaPlayer.getDuration());

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    System.out.println(mediaPlayer.getDuration());
                                    handler.post(updateSeekBarRunnable);

                                    if (autoplaySwitch.isChecked()) {
                                        mediaPlayer.start();
                                        System.out.println("Start audio checked");
                                        playButton.setVisibility(View.GONE);
                                        pauseButton.setVisibility(View.VISIBLE);
                                    } else {
                                        mediaPlayer.pause();
                                        System.out.println("Pause audio checked");
                                        playButton.setVisibility(View.VISIBLE);
                                        pauseButton.setVisibility(View.GONE);
                                    }
                                }
                            });

                            playButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.start();
                                    System.out.println("Start audio");
                                    playButton.setVisibility(View.GONE);
                                    pauseButton.setVisibility(View.VISIBLE);
                                }
                            });

                            pauseButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.pause();
                                    System.out.println("Pause audio");
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

                    extensaoDesconhecida.setVisibility(View.VISIBLE);
                    textoNomeArquivo.setText(arquivo.getName());
                    textoAviso.setText("Extensão não suportada");

                    System.out.println("---------------------");
                    continue;
                }
            } else {
                System.out.println("Não tem extensão");
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                constraintAudio.setVisibility(View.GONE);

                extensaoDesconhecida.setVisibility(View.VISIBLE);
                textoNomeArquivo.setText(arquivo.getName());
                textoAviso.setText("Extensão não encontrada");
                continue;
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
            // Atualiza a visibilidade dos layouts
            layoutPastaCentral.setVisibility(View.VISIBLE);
            //layoutPrincipal.setVisibility(View.VISIBLE);
            layoutImagens = findViewById(R.id.layout_imagens);
            layoutImagens.setVisibility(View.GONE);
            videoView.pause();
            pastaJaFoiSelecionada = false;
            //mediaPlayer.pause();

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