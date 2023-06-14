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

import java.util.List;

public class ImagemAdapter extends RecyclerView.Adapter<ImagemAdapter.ViewHolder> {
    private List<DocumentFile> listaDeExclusao;

    public ImagemAdapter(List<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentFile imagem = listaDeExclusao.get(position);
        String caminhoArquivo = imagem.getUri().toString();
        Glide.with(holder.imageView.getContext())
                .load(caminhoArquivo)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return listaDeExclusao.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imagem);
        }
    }
}
