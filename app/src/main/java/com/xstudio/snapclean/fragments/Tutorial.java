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

        // Mostra o tutorial
        new TapTargetSequence(activity)
                .targets(
                        TapTarget.forView(activity.findViewById(R.id.negar_imagem), "Excluir imagem", "Se você não quer mais essa imagem, basta clicar neste botão e ela será enviada para a lixeira. Você ainda pode mudar de ideia depois, mas só até esvaziar a lixeira.")
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
                        TapTarget.forView(activity.findViewById(R.id.aceitar_imagem), "Guardar imagem", "Se você gostou dessa imagem e quer mantê-la no seu dispositivo, clique neste botão e passe para a próxima. Você pode voltar e ver as imagens guardadas a qualquer momento.")
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
                        TapTarget.forView(activity.findViewById(R.id.duas_setas), "Deslizar imagem", "Você também pode usar o gesto de deslizar para decidir o que fazer com as imagens. Deslize para a esquerda se quiser excluir, ou para a direita se quiser guardar.")
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
                        TapTarget.forView(activity.findViewById(R.id.voltar_imagem), "Desfazer ação", "Ops, você se arrependeu de ter excluído uma imagem? Não se preocupe, você pode clicar neste botão e voltar para a imagem anterior. Assim, você pode escolher um novo destino para ela.")
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
                        TapTarget.forView(activity.findViewById(R.id.lixeira), "Lixeira", "Aqui estão as imagens que você excluiu. Você pode revisá-las e decidir se quer apagá-las definitivamente do seu aparelho ou recuperá-las. Mas cuidado: uma vez que você esvaziar a lixeira, não poderá mais recuperar as imagens.")
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
