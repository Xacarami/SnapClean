package com.xstudio.snapclean.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.R;

import java.util.ArrayList;

public class ImagemAdapter extends RecyclerView.Adapter<ImagemAdapter.ViewHolder> {
    private final ArrayList<DocumentFile> listaDeExclusao;
    private final ArrayList<DocumentFile> listaDeSelecionados = new ArrayList<>();

    public interface OnItemClickListener {
        void onImagemClicked(DocumentFile imagem);

        void onBotaoImagemClicked(DocumentFile imagem);
    }

    public ArrayList<DocumentFile> getListaDeSelecionados() {
        return listaDeSelecionados;
    }

    private Button botaoRecuperar;

    private Button botaoApagarTudo;

    public void setBotoes(Button botaoRecuperar, Button botaoApagarTudo) {
        this.botaoRecuperar = botaoRecuperar;
        this.botaoApagarTudo = botaoApagarTudo;
    }

    public void atualizarBotoes() {
        if (botaoRecuperar != null) {
            if (listaDeExclusao.size() == 0) {
                botaoRecuperar.setText("Recuperar");
                botaoRecuperar.setEnabled(false);
                botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            } else {
                botaoRecuperar.setEnabled(true);
                botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(0xFF289500));
                if (getListaDeSelecionados().isEmpty()) {
                    botaoRecuperar.setText("Recuperar tudo");
                } else {
                    botaoRecuperar.setText("Recuperar");
                }
            }
        }

        if (botaoApagarTudo != null) {
            if (listaDeExclusao.size() == 0) {
                botaoApagarTudo.setText("Apagar");
                botaoApagarTudo.setEnabled(false);
                botaoApagarTudo.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            } else {
                botaoApagarTudo.setEnabled(true);
                botaoApagarTudo.setBackgroundTintList(ColorStateList.valueOf(0xFF0101));
                if (listaDeSelecionados.isEmpty()) {
                    botaoApagarTudo.setText("Apagar tudo");
                } else {
                    botaoApagarTudo.setText("Apagar");
                }
            }
        }
    }

    private final OnItemClickListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView iconeSelecao;
        ImageButton botaoImagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagem);
            iconeSelecao = itemView.findViewById(R.id.icone_selecao);
            botaoImagem = itemView.findViewById(R.id.aumentar_imagem);
        }
    }

    public ImagemAdapter(ArrayList<DocumentFile> listaDeExclusao, OnItemClickListener listener) {
        this.listaDeExclusao = listaDeExclusao;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        atualizarBotoes();
        return new ViewHolder(view);
    }

    public void selecionarTodas() {
        listaDeSelecionados.clear();
        listaDeSelecionados.addAll(listaDeExclusao);
        notifyDataSetChanged();
        atualizarBotoes();
    }

    public void desselecionarTodas() {
        listaDeSelecionados.clear();
        notifyDataSetChanged();
        atualizarBotoes();
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentFile imagem = listaDeExclusao.get(position);
        String caminhoArquivo = imagem.getUri().toString();
        String extensao = imagem.getName();
        if (extensao != null) {
            if (isVideo(extensao) || isImagem(extensao)) {
                Glide.with(holder.imageView.getContext())
                        .load(caminhoArquivo)
                        .into(holder.imageView);
            } else if (isAudio(extensao)) {
                Glide.with(holder.imageView.getContext())
                        .load(R.mipmap.ic_musica)
                        .into(holder.imageView);
            } else {
                Glide.with(holder.imageView.getContext())
                        .load(R.mipmap.ic_alerta)
                        .into(holder.imageView);
            }
        }


        holder.botaoImagem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBotaoImagemClicked(imagem);
            }
        });


        // Adiciona um ouvinte de clique à imagem
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImagemClicked(imagem);
            }
        });

        // Define o estado selecionado da imagem
        holder.itemView.setSelected(listaDeSelecionados.contains(imagem));

        // Adiciona um ouvinte de clique à imagem
        holder.itemView.setOnClickListener(v -> {
            if (listaDeSelecionados.contains(imagem)) {
                listaDeSelecionados.remove(imagem);
            } else {
                listaDeSelecionados.add(imagem);
            }
            notifyItemChanged(holder.getAdapterPosition());
            atualizarBotoes();
        });
        // Define o estado selecionado da imagem
        boolean selecionado = listaDeSelecionados.contains(imagem);
        holder.itemView.setSelected(selecionado);
        holder.iconeSelecao.setVisibility(selecionado ? View.VISIBLE : View.GONE);

    }


    @Override
    public int getItemCount() {
        return listaDeExclusao.size();
    }

}
