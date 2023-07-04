package com.xstudio.snapclean.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Switch;
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
    //private List<DocumentFile> listaDeExclusao;
    private ArrayList<DocumentFile> listaDeExclusao;
    TextView quantidadeTotal;
    private boolean todasSelecionadas = false;


    public SelecionadosFragment(ArrayList<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
    }

    private SelecionadosViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
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


    //para que o ícone de pasta aqui possa executar uma função na MainActivity.java
    public interface OnFragmentInteractionListener {
        void onPastaCimaSelecionadosClicked();
    }

    private OnFragmentInteractionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " deve implementar OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    private ImagemAdapter adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ImagemAdapter(listaDeExclusao, this);
        recyclerView.setAdapter(adapter);

        imagemTelaInteira = view.findViewById(R.id.imagem_tela_inteira);

        adapter = new ImagemAdapter(listaDeExclusao, this);
        recyclerView.setAdapter(adapter);

        quantidadeTotal = getView().findViewById(R.id.quantidade_total);
        quantidadeTotal.setText("Quantidade: " + listaDeExclusao.size());

        ImageButton selecionador = view.findViewById(R.id.selecionador);
        selecionador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (todasSelecionadas) {
                    adapter.desselecionarTodas();
                    todasSelecionadas = false;
                } else {
                    adapter.selecionarTodas();
                    todasSelecionadas = true;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        //inflar o layout do fragment
        View rootView = inflater.inflate(R.layout.fragment_selecionados, container, false);

        ConstraintLayout layoutPrincipal = getActivity().findViewById(R.id.layout_principal);
        ImageButton voltarSelecionados = rootView.findViewById(R.id.voltar_selecionados);

        voltarSelecionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPrincipal.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(SelecionadosFragment.this);
                fragmentTransaction.commit();
                System.out.println("Botao voltar foi clicado");
            }
        });
        return rootView;
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

    ImageView imageView;
    VideoView videoView;
    ConstraintLayout constraintAudio;
    ConstraintLayout extensaoDesconhecida;
    Switch autoplaySwitch;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    ImageButton playButton;
    ImageButton pauseButton;
    TextView textoAviso;
    TextView textoNomeArquivo;
    ImageButton setaBaixo;
    FrameLayout layoutImagens;
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

        setaBaixo.setVisibility(View.VISIBLE);
        voltarSelecionados.setVisibility(View.GONE);
        setaBaixo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quadrosExcluidos.setVisibility(View.VISIBLE);
                layoutImagens.setVisibility(View.GONE);
                selecionador.setVisibility(View.VISIBLE);
                voltarSelecionados.setVisibility(View.VISIBLE);
                setaBaixo.setVisibility(View.GONE);
            }
        });

        //Para a tela cheia
        if (arquivo.getType() != null) {
            if (isImagem(extensao)) {
                System.out.println("É imagem");
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                constraintAudio.setVisibility(View.GONE);
                System.out.println("Audio era pra ter ido de base");
                extensaoDesconhecida.setVisibility(View.GONE);

                Glide.with(imageView.getContext())
                        .load(caminhoArquivo)
                        .into(imageView);

            } else if (isVideo(extensao)) {
                System.out.println("É Video");
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
                System.out.println("É audio");
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

                        playButton = getView().findViewById(R.id.play_button_tela_inteira);
                        pauseButton = getView().findViewById(R.id.pause_button_tela_inteira);
                        seekBar = getView().findViewById(R.id.seek_bar_tela_inteira);

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                seekBar.setMax(mediaPlayer.getDuration());
                                System.out.println(mediaPlayer.getDuration());
                                handler.post(updateSeekBarRunnable);

                                if (autoplaySwitch) {
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

    public void atualizarListaDeExclusao(ArrayList<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
        adapter.notifyDataSetChanged();
    }


}
