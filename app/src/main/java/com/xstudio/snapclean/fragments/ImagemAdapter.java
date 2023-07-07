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
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.R;

import java.util.ArrayList;
import java.util.List;

public class ImagemAdapter extends RecyclerView.Adapter<ImagemAdapter.ViewHolder> {
    private ArrayList<DocumentFile> listaDeExclusao;
    private ArrayList<DocumentFile> listaDeSelecionados = new ArrayList<>();

    public interface OnItemClickListener {
        void onImagemClicked(DocumentFile imagem);
        void onBotaoImagemClicked(DocumentFile imagem);
    }

    public ArrayList<DocumentFile> getListaDeSelecionados(){
        return listaDeSelecionados;
    }

    private Button botaoRecuperar;

    private Button botaoApagarTudo;

    public void setBotoes(Button botaoRecuperar, Button botaoApagarTudo) {
        this.botaoRecuperar = botaoRecuperar;
        this.botaoApagarTudo = botaoApagarTudo;
    }

    private void atualizarBotoes() {
        if (botaoRecuperar != null) {
            if (listaDeSelecionados.isEmpty()) {
                botaoRecuperar.setText("Recuperar");
                botaoRecuperar.setEnabled(false);
                botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            } else {
                botaoRecuperar.setEnabled(true);
                botaoRecuperar.setBackgroundTintList(ColorStateList.valueOf(0xFF289500));
            }
        }
        if (botaoApagarTudo != null) {
            if (listaDeSelecionados.isEmpty()) {
                botaoApagarTudo.setText("Apagar tudo");
            } else {
                botaoApagarTudo.setText("Apagar");
            }
        }
    }


    private OnItemClickListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ImageView iconeSelecao;
        ImageButton botaoImagem;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imagem);
            iconeSelecao = itemView.findViewById(R.id.icone_selecao);
            botaoImagem = itemView.findViewById(R.id.aumentar_imagem);
        }
    }

    public ImagemAdapter(ArrayList<DocumentFile> listaDeExclusao, OnItemClickListener listener){
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
        return extensao.endsWith("jpg") || extensao.endsWith("png") || extensao.endsWith("jpeg");
    }

    private boolean isVideo(String extensao) {
        return extensao.endsWith("mp4") || extensao.endsWith("3gp") || extensao.equalsIgnoreCase(".wmv");
    }

    private boolean isAudio(String extensao) {
        return extensao.endsWith("wav") || extensao.endsWith("mp3") || extensao.endsWith("m4a") || extensao.endsWith("wma");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentFile imagem = listaDeExclusao.get(position);
        String caminhoArquivo = imagem.getUri().toString();
        String extensao = imagem.getName();
        if(isVideo(extensao) || isImagem(extensao)){
            Glide.with(holder.imageView.getContext())
                    .load(caminhoArquivo)
                    .into(holder.imageView);
        } else if(isAudio(extensao)){
            Glide.with(holder.imageView.getContext())
                    .load(R.mipmap.ic_musica)
                    .into(holder.imageView);
        } else {
            Glide.with(holder.imageView.getContext())
                    .load(R.mipmap.ic_alerta)
                    .into(holder.imageView);
        }

        holder.botaoImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBotaoImagemClicked(imagem);
                }
            }
        });


        // Adiciona um ouvinte de clique à imagem
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImagemClicked(imagem);
                }
            }
        });

        // Define o estado selecionado da imagem
        holder.itemView.setSelected(listaDeSelecionados.contains(imagem));

        // Adiciona um ouvinte de clique à imagem
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaDeSelecionados.contains(imagem)) {
                    listaDeSelecionados.remove(imagem);
                } else {
                    listaDeSelecionados.add(imagem);
                }
                notifyItemChanged(holder.getAdapterPosition());
                atualizarBotoes();
            }
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
