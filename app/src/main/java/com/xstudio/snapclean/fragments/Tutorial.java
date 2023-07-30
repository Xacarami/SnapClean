package com.xstudio.snapclean.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.xstudio.snapclean.R;

public class Tutorial {
    private final Activity activity;

    public Tutorial(Activity activity) {
        this.activity = activity;
    }

    public void show() {

        ImageView duasSetas = activity.findViewById(R.id.duas_setas);
        duasSetas.setVisibility(View.VISIBLE);

        // Obter a string a partir do contexto da Activity
        String tituloExcluirImagem = activity.getString(R.string.tituloExcluirImagem);
        String descricaoExcluirImagem = activity.getString(R.string.descricaoExcluirImagem);
        String tituloGuardarImagem = activity.getString(R.string.tituloGuardarImagem);
        String descricaoGuardarImagem = activity.getString(R.string.descricaoGuardarImagem);
        String tituloDeslizarImagem = activity.getString(R.string.tituloDeslizarImagem);
        String descricaoDeslizarImagem = activity.getString(R.string.descricaoDeslizarImagem);
        String tituloDesfazerAcao = activity.getString(R.string.tituloDesfazerAcao);
        String descricaoDesfazerAcao = activity.getString(R.string.descricaoDesfazerAcao);
        String tituloLixeira = activity.getString(R.string.tituloLixeira);
        String descricaoLixeira = activity.getString(R.string.descricaoLixeira);

        // Mostra o tutorial
        new TapTargetSequence(activity)
                .targets(
                        TapTarget.forView(
                                activity.findViewById(R.id.negar_imagem),
                                        tituloExcluirImagem,
                                        descricaoExcluirImagem
                                )
                                // All options below are optional
                                .outerCircleColor(R.color.fundoTutorial)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.red)   // Specify a color for the target circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .drawShadow(false)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40),                  // Specify the target radius (in dp)
                        TapTarget.forView(activity.findViewById(R.id.aceitar_imagem), tituloGuardarImagem, descricaoGuardarImagem)
                                // All options below are optional
                                .outerCircleColor(R.color.fundoTutorial)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.green)   // Specify a color for the target circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text

                                .drawShadow(false)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40),                  // Specify the target radius (in dp)
                        TapTarget.forView(activity.findViewById(R.id.duas_setas), tituloDeslizarImagem, descricaoDeslizarImagem)
                                // All options below are optional
                                .outerCircleColor(R.color.fundoTutorial)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.gray)   // Specify a color for the target circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .drawShadow(false)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40),                  // Specify the target radius (in dp)
                        TapTarget.forView(activity.findViewById(R.id.voltar_imagem), tituloDesfazerAcao, descricaoDesfazerAcao)
                                // All options below are optional
                                .outerCircleColor(R.color.fundoTutorial)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.gray)   // Specify a color for the target circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .drawShadow(false)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(false)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40),                  // Specify the target radius (in dp)
                        TapTarget.forView(activity.findViewById(R.id.lixeira), tituloLixeira, descricaoLixeira)
                                // All options below are optional
                                .outerCircleColor(R.color.fundoTutorial)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.gray)   // Specify a color for the target circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .drawShadow(false)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(false)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40)                  // Specify the target radius (in dp)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        duasSetas.setVisibility(View.GONE);
                        System.out.println("Omg");
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                })
                .start();
    }
}
