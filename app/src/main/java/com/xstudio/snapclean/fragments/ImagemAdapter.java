package com.xstudio.snapclean.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xstudio.snapclean.R;

import java.util.ArrayList;
import java.util.List;

public class ImagemAdapter extends RecyclerView.Adapter<ImagemAdapter.ViewHolder> {
    private List<DocumentFile> listaDeExclusao;
    private List<DocumentFile> listaDeSelecionados = new ArrayList<>();


    public interface OnItemClickListener {
        void onImagemClicked(DocumentFile imagem);
    }

    private OnItemClickListener listener;

    //ImageView imageView;
    //ImageView iconeSelecao;

    public ImagemAdapter(List<DocumentFile> listaDeExclusao, OnItemClickListener listener){
        this.listaDeExclusao = listaDeExclusao;
        this.listener = listener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    public void selecionarTodas() {
        listaDeSelecionados.clear();
        listaDeSelecionados.addAll(listaDeExclusao);
        notifyDataSetChanged();
    }

    public void desselecionarTodas() {
        listaDeSelecionados.clear();
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentFile imagem = listaDeExclusao.get(position);
        String caminhoArquivo = imagem.getUri().toString();
        Glide.with(holder.imageView.getContext())
                .load(caminhoArquivo)
                .into(holder.imageView);

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
            }
        });
        // Define o estado selecionado da imagem
        boolean selecionado = listaDeSelecionados.contains(imagem);
        holder.itemView.setSelected(selecionado);
        holder.iconeSelecao.setVisibility(selecionado ? View.VISIBLE : View.GONE);

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ImageView iconeSelecao;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imagem);
            iconeSelecao = itemView.findViewById(R.id.icone_selecao);
        }
    }


    @Override
    public int getItemCount() {
        return listaDeExclusao.size();
    }

}
