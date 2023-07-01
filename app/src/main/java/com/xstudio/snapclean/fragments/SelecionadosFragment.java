package com.xstudio.snapclean.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

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

import java.util.List;

public class SelecionadosFragment extends Fragment implements ImagemAdapter.OnItemClickListener {

    //public SelecionadosFragment(){}

    private List<DocumentFile> listaDeExclusao;

    private boolean todasSelecionadas = false;


    public SelecionadosFragment(List<DocumentFile> listaDeExclusao){
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

        //ImageView pastaCimaSelecionados = view.findViewById(R.id.pasta_cima_selecionados);

        imagemTelaInteira = view.findViewById(R.id.imagem_tela_inteira);

        adapter = new ImagemAdapter(listaDeExclusao, this);
        recyclerView.setAdapter(adapter);

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
        /*
        ImageView pastaCimaSelecionados = rootView.findViewById(R.id.pasta_cima_selecionados);
        pastaCimaSelecionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPastaCimaSelecionadosClicked();
                }
            }
        });

         */



        voltarSelecionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                layoutPrincipal.setVisibility(View.VISIBLE);
                requireActivity().getSupportFragmentManager().popBackStack();
                 */
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

    public void atualizarListaDeExclusao(List<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
        adapter.notifyDataSetChanged();
    }

}
