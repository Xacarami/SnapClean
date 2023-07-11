package com.xstudio.snapclean.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.MainActivity;
import com.xstudio.snapclean.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelecionadosFragment extends Fragment implements ImagemAdapter.OnItemClickListener {
    private ArrayList<DocumentFile> listaDeExclusao;
    TextView quantidadeTotal;
    private boolean todasSelecionadas = false;
    SelecionadosViewModel viewModel;

    public SelecionadosFragment(ArrayList<DocumentFile> listaDeExclusao) {
        this.listaDeExclusao = listaDeExclusao;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SelecionadosViewModel.class);
    }

    private ImageView imagemTelaInteira;

    @Override
    public void onImagemClicked(DocumentFile imagem) {
        String caminhoArquivo = imagem.getUri().toString();
        Glide.with(this)
                .load(caminhoArquivo)
                .into(imagemTelaInteira);
        imagemTelaInteira.setVisibility(View.VISIBLE);
    }

    private ImagemAdapter adapter;
    String textificandoTotal;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button botaoRecuperar = view.findViewById(R.id.botao_recuperar);
        Button botaoApagarTudo = view.findViewById(R.id.botao_apagar);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ImagemAdapter(listaDeExclusao, this);
        adapter.setBotoes(botaoRecuperar, botaoApagarTudo);
        recyclerView.setAdapter(adapter);

        if (adapter.getListaDeSelecionados().isEmpty()) {
            botaoRecuperar.setText("Recuperar");
            botaoRecuperar.setEnabled(false);
            botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        } else {
            botaoRecuperar.setEnabled(true);
            botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(0xFF289500));
        }


        imagemTelaInteira = view.findViewById(R.id.imagem_tela_inteira);

        quantidadeTotal = getView().findViewById(R.id.quantidade_total);
        textificandoTotal = "Quantidade: " + listaDeExclusao.size();
        quantidadeTotal.setText(textificandoTotal);

        ImageButton selecionador = view.findViewById(R.id.selecionador);
        selecionador.setOnClickListener(v -> {
            if (todasSelecionadas) {
                adapter.desselecionarTodas();
                todasSelecionadas = false;
            } else {
                adapter.selecionarTodas();
                todasSelecionadas = true;
            }
        });

        botaoRecuperar.setOnClickListener(v -> {
            System.out.println("Recuperar");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Recuperar imagens");
            builder.setMessage("Tem certeza de que deseja recuperar as imagens selecionadas?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                // Coloque aqui o código que deve ser executado se o usuário escolher recuperar as imagens
                System.out.println("SIM");
                ArrayList<DocumentFile> listaDeSelecionados = adapter.getListaDeSelecionados();
                for (DocumentFile recuperado : listaDeSelecionados) {
                    listaDeExclusao.remove(recuperado);
                    atualizarListaDeExclusao(listaDeExclusao);
                    if (listener != null) {
                        listener.onArquivoRecuperado(recuperado);
                    } else {
                        Log.d("SelecionadosFragment", "Listener é nulo");
                    }
                }
                adapter.desselecionarTodas();
                textificandoTotal = "Quantidade: " + listaDeExclusao.size();
                quantidadeTotal.setText(textificandoTotal);
            });
            builder.setNegativeButton("Não", (dialog, which) -> {
                // Usuário escolheu não recuperar as imagens
            });
            builder.show();
        });

        botaoApagarTudo.setOnClickListener(v -> {
            System.out.println("Botao Apagar");

            if (adapter.getListaDeSelecionados().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Apagar arquivos selecionados");
                builder.setMessage("Tem certeza de que deseja apagar os arquivos selecionados?\nOs aquivos não poderão ser recuperados depois!");
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    // Usuário escolheu apagar as imagens selecionadas
                    System.out.println("Apagar mesmo");
                    for (DocumentFile arquivo : adapter.getListaDeSelecionados()) {
                        if (arquivo.exists()) {
                            listaDeExclusao.remove(arquivo);
                            arquivo.delete();
                            atualizarListaDeExclusao(listaDeExclusao);
                        }
                    }
                    if (listener != null) {
                        listener.onArquivoRecuperado(null);
                    }
                    adapter.desselecionarTodas();
                    textificandoTotal = "Quantidade: " + listaDeExclusao.size();
                    quantidadeTotal.setText(textificandoTotal);
                });

                builder.setNegativeButton("Não", (dialog, which) -> {
                    // Usuário escolheu não apagar as imagens selecionadas
                    System.out.println("Não Apagar");
                });
                builder.show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Apagar Tudo");
                builder.setMessage("Tem certeza de que deseja apagar TODOS os arquivos?");
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    // Usuário escolheu apagar todas as imagens
                    AlertDialog.Builder builderGarantia = new AlertDialog.Builder(getContext());
                    builderGarantia.setTitle("ALERTA!");
                    builderGarantia.setMessage("Os aquivos não poderão ser recuperados depois!");
                    builderGarantia.setPositiveButton("Sim", (dialogo, whichh) -> {
                        System.out.println("Apagar Tudo mesmo");
                        List<DocumentFile> temp = new ArrayList<>(listaDeExclusao);
                        for (DocumentFile arquivo : temp) {
                            if (arquivo.exists()) {
                                listaDeExclusao.remove(arquivo);
                                arquivo.delete();
                                atualizarListaDeExclusao(listaDeExclusao);
                            }
                        }
                        if (listener != null) {
                            listener.onArquivoRecuperado(null);
                        }
                        adapter.desselecionarTodas();
                        textificandoTotal = "Quantidade: " + listaDeExclusao.size();
                        quantidadeTotal.setText(textificandoTotal);
                    });
                    builderGarantia.setNegativeButton("Não", (dialogo, whichh) -> {
                        // Usuário escolheu não apagar todas as imagens
                        System.out.println("Não Apagar Tudo");
                    });
                    builderGarantia.show();
                });

                builder.setNegativeButton("Não", (dialog, which) -> {
                    // Usuário escolheu não apagar todas as imagens
                    System.out.println("Não Apagar Tudo");
                });
                builder.show();
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflar o layout do fragment
        View rootView = inflater.inflate(R.layout.fragment_selecionados, container, false);

        ConstraintLayout layoutPrincipal = getActivity().findViewById(R.id.layout_principal);
        ImageButton voltarSelecionados = rootView.findViewById(R.id.voltar_selecionados);

        voltarSelecionados.setOnClickListener(v -> {
            layoutPrincipal.setVisibility(View.VISIBLE);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(SelecionadosFragment.this);
            fragmentTransaction.commit();
            System.out.println("Botao voltar foi clicado");
        });
        return rootView;
    }


    //passar lista atualizada para a main
    public interface OnArquivoRecuperadoListener {
        void onArquivoRecuperado(DocumentFile arquivo);
    }

    private OnArquivoRecuperadoListener listener;

    public void setOnArquivoRecuperadoListener(OnArquivoRecuperadoListener listener) {
        this.listener = listener;
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

    ImageView imageView;
    VideoView videoView;
    ConstraintLayout constraintAudio;
    ConstraintLayout extensaoDesconhecida;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    ImageButton playButton;
    ImageButton pauseButton;
    TextView textoAviso;
    TextView textoNomeArquivo;
    ImageButton setaBaixo;
    FrameLayout layoutImagens;
    TextView nomeAudio;
    TextView tempoAudioMaximo;
    TextView tempoAudioPercorrido;

    private final Handler handler = new Handler(Looper.getMainLooper());

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


    @Override
    public void onBotaoImagemClicked(DocumentFile arquivo) {
        String caminhoArquivo = arquivo.getUri().toString();
        ConstraintLayout quadrosExcluidos = getView().findViewById(R.id.quadros_excluidos);
        quadrosExcluidos.setVisibility(View.GONE);

        String extensao = arquivo.getName();

        layoutImagens = getView().findViewById(R.id.layout_imagens);
        layoutImagens.setVisibility(View.VISIBLE);

        ImageButton selecionador = getView().findViewById(R.id.selecionador);
        selecionador.setVisibility(View.GONE);

        ImageButton voltarSelecionados = getView().findViewById(R.id.voltar_selecionados);

        imageView = getView().findViewById(R.id.imagem_tela_inteira);
        videoView = getView().findViewById(R.id.video_tela_inteira);
        constraintAudio = getView().findViewById(R.id.constraint_audio_tela_inteira);
        extensaoDesconhecida = getView().findViewById(R.id.constraint_extensao_desconhecida_tela_inteira);
        boolean autoplaySwitch = ((MainActivity) getActivity()).isAutoplayChecked();
        //autoplaySwitch = getView().findViewById(R.id.switchAutoPlay);
        seekBar = getView().findViewById(R.id.seek_bar_tela_inteira);
        textoAviso = getView().findViewById(R.id.texto_aviso_tela_inteira);
        setaBaixo = getView().findViewById(R.id.ic_seta_baixo);
        textoNomeArquivo = getView().findViewById(R.id.texto_nome_arquivo_tela_inteira);
        //numeroLixeira = getView().findViewById(R.id.numero_lixeira);
        tempoAudioMaximo = getView().findViewById(R.id.tempo_audio_maximo_tela_inteira);
        tempoAudioPercorrido = getView().findViewById(R.id.tempo_audio_percorrido_tela_inteira);
        nomeAudio = getView().findViewById(R.id.nome_audio_tela_inteira);

        setaBaixo.setVisibility(View.VISIBLE);
        voltarSelecionados.setVisibility(View.GONE);
        setaBaixo.setOnClickListener(v -> {
            quadrosExcluidos.setVisibility(View.VISIBLE);
            layoutImagens.setVisibility(View.GONE);
            selecionador.setVisibility(View.VISIBLE);
            voltarSelecionados.setVisibility(View.VISIBLE);
            setaBaixo.setVisibility(View.GONE);
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
            if (videoView != null) {
                videoView.pause();
            }
        });

        //Para a tela cheia
        if (arquivo.getType() != null && extensao != null) {
            if (isImagem(extensao)) {
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                constraintAudio.setVisibility(View.GONE);
                extensaoDesconhecida.setVisibility(View.GONE);

                Glide.with(imageView.getContext())
                        .load(caminhoArquivo)
                        .into(imageView);

            } else if (isVideo(extensao)) {
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                constraintAudio.setVisibility(View.GONE);
                extensaoDesconhecida.setVisibility(View.GONE);

                MediaController mediaController = new MediaController(getContext());
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);

                videoView.setVideoPath(caminhoArquivo);

                if (autoplaySwitch) {
                    videoView.start();
                } else {
                    videoView.pause();
                }

            } else if (isAudio(extensao)) {
                constraintAudio.setVisibility(View.VISIBLE);
                extensaoDesconhecida.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);

                try {
                    ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(arquivo.getUri(), "r");
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
                        mmr.setDataSource(getContext(), arquivo.getUri());
                        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                        //nomeAudio.setText(arquivo.getName());
                        nomeAudio.setText(title);

                        playButton = getView().findViewById(R.id.play_button_tela_inteira);
                        pauseButton = getView().findViewById(R.id.pause_button_tela_inteira);
                        seekBar = getView().findViewById(R.id.seek_bar_tela_inteira);

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
                            System.out.println(mediaPlayer.getDuration());
                            handler.post(updateSeekBarRunnable);

                            if (autoplaySwitch) {
                                mediaPlayer.start();
                                System.out.println("Play automatico");
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
            }
        } else {
            System.out.println("Não tem extensão");
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            constraintAudio.setVisibility(View.GONE);

            extensaoDesconhecida.setVisibility(View.VISIBLE);
            textoNomeArquivo.setText(arquivo.getName());
            textoAviso.setText("Extensão não encontrada");
        }
    }

    public void atualizarListaDeExclusao(ArrayList<DocumentFile> listaDeExclusao) {
        this.listaDeExclusao = listaDeExclusao;
        adapter.notifyDataSetChanged();
    }


}
