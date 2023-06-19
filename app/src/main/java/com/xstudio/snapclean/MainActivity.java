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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.View;
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private TextView numeroLixeira;
    private ConstraintLayout layoutPastaCentral;
    private Boolean pastaJaFoiSelecionada = false;
    private int imagensCarregadas = 0;
    private int offsetImagens = 10;
    ImageButton botaoAvancar;
    ImageButton botaoExcluir;
    ImageButton botaoVoltar;
    int tirando = 0;
    int continuando = 0;
    private SelecionadosFragment testeSeJaFoiCriado;
    Boolean resetouPasta = false;
    private ArrayList<DocumentFile> listaDeExclusao = new ArrayList<>();
    FrameLayout layoutImagens;
    ConstraintLayout layoutPrincipal;

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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

        drawerLayout = findViewById(R.id.drawer_layout);


        ImageButton optionIcon = findViewById(R.id.opcoes);
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

        ImageButton iconeMaisImagens = findViewById(R.id.abrir_mais_imagens);
        iconeMaisImagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Ta clicando");
                colocarToast();
            }
        });

        ImageButton iconeInfo = findViewById(R.id.info_button);
        iconeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Ta clicando");
                Toast.makeText(MainActivity.this, "Ainda não disponível. Espere uma atualização.", Toast.LENGTH_SHORT).show();
            }
        });


        //Selecionador de pastas
        ImageButton botaoSelecionarPasta1 = findViewById(R.id.pasta_cima);
        ImageButton botaoSelecionarPasta2 = findViewById(R.id.pasta_central);

        System.out.println(listaDeExclusao);
        View.OnClickListener selecionarPastaClickListener = v -> {
            System.out.println(pastaJaFoiSelecionada);

            if (!pastaJaFoiSelecionada) {
                selecionarPasta();
                System.out.println(listaDeExclusao);
            } else {
                resetouPasta = true;
                selecionarPasta();
                System.out.println("Pasta ja selecionada, e clicou em abrir mais uma");

            }

        };

        pastaCima.setOnClickListener(selecionarPastaClickListener);
        pastaCentral.setOnClickListener(selecionarPastaClickListener);


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
                    listaDeExclusao.add(0, arquivoAtual.get());
                    //quantidadeApagadas++;
                    numeroLixeira.setText(String.valueOf(listaDeExclusao.size()));

                    System.out.println("Clicou no botao de excluir");
                    System.out.println(listaDeExclusao);
                } else if (v.getId() == R.id.voltar_imagem) {
                    int indexImagemAnterior = imagensCarregadas - 2;
                    if (indexImagemAnterior >= 0) {
                        DocumentFile imagemAnterior = arrayDeArquivos[indexImagemAnterior];
                        if (listaDeExclusao.contains(imagemAnterior)) {
                            listaDeExclusao.remove(imagemAnterior);
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

    private void continuarLoop(int quantidadeDeImagens, int ajuste) {

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

            String extensao = arquivo.getType();

            if (arquivo.getType() != null) {
                if (isImagem(extensao)) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);

                    Glide.with(this)
                            .load(caminhoArquivo)
                            .into(imageView);

                } else if (isVideo(extensao)) {
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);

                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);

                    videoView.setVideoPath(caminhoArquivo);

                    Switch autoplaySwitch = findViewById(R.id.switchAutoPlay);

                    if (autoplaySwitch.isChecked()) {
                        videoView.start();
                    } else {
                        videoView.pause();
                    }

                } else if (isAudio(extensao)) {
                    System.out.println("um audio ai");

                    try {
                        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(arquivo.getUri(), "r");
                        if (parcelFileDescriptor != null) {
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                            MediaPlayer mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(fileDescriptor);
                            mediaPlayer.prepare();

                            playButton = findViewById(R.id.play_button);
                            playButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.start();
                                    System.out.println("Start audio");
                                }
                            });

                            pauseButton = findViewById(R.id.pause_button);
                            pauseButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mediaPlayer.pause();
                                    System.out.println("Pause audio");
                                }
                            });

                            seekBar = findViewById(R.id.seek_bar);
                            seekBar.setMax(mediaPlayer.getDuration());
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

                    /*
                    try {


                    } catch (IOException e) {
                        System.out.println("--------ERRO AQUI ---------------");
                        System.out.println("--------ERRO AQUI ---------------");
                        System.out.println("--------ERRO AQUI ---------------");
                        System.out.println("--------ERRO AQUI ---------------");
                        System.out.println("--------ERRO AQUI ---------------");
                        e.printStackTrace();
                    }

                     */

                } else {
                    System.out.println("---------------------");
                    System.out.println("Aquivo de extensão desconhecida");
                    System.out.println("Tipo: " + arquivo.getType());
                    System.out.println("Nome: " + arquivo.getName());
                    System.out.println("---------------------");
                    continue;
                }
            } else {
                System.out.println("Não tem extensão");
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

        }

    }

    private boolean isImagem(String extensao) {
        return extensao.endsWith("jpg") || extensao.endsWith("png") || extensao.endsWith("jpeg");
    }

    private boolean isVideo(String extensao) {
        return extensao.endsWith("mp4") || extensao.endsWith("3gp") || extensao.equalsIgnoreCase(".wmv");
    }

    private boolean isAudio(String extensao) {
        return extensao.endsWith("wav") || extensao.endsWith("audio/mpeg") || extensao.endsWith("mp3") || extensao.endsWith("m4a");
    }

}