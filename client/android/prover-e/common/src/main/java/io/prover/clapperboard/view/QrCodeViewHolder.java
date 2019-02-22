package io.prover.clapperboard.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.nayuki.qrcodegen.QrCode;
import io.prover.common.R;
import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.util.BigIntegers;

/**
 * Created by babay on 23.12.2017.
 */

public class QrCodeViewHolder {
    private final ImageView qrCodeImage;
    private final View generatedBy;
    private final View proverLogo;
    private final TextView originalText;
    private final ConstraintLayout root;
    private boolean show = false;

    public QrCodeViewHolder(ConstraintLayout root) {
        this.root = root;
        qrCodeImage = root.findViewById(R.id.qrCodeImage);
        generatedBy = root.findViewById(R.id.generatedBy);
        proverLogo = root.findViewById(R.id.proverLogo);
        originalText = root.findViewById(R.id.originalText);
    }

    public void setCode(IQrCodeOrderResult result) {
        originalText.setText(result.getMessage());

        String digits = BigIntegers.fromUnsignedByteArray(result.getQrCodeBytes()).toString(10);
        QrCode code = QrCode.encodeNumeric(digits, QrCode.Ecc.QUARTILE);

        DisplayMetrics dm = root.getResources().getDisplayMetrics();
        int scrSize = Math.min(dm.widthPixels, dm.heightPixels);
        int pad = (int) (dm.density * 16);
        int border = 0;
        int scale = (scrSize - 2 * pad) / (code.size + border * 2);

        VectorDrawableCompat dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_qrcode, null);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        //VectorDrawableCompat dr2 = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_qrcode_light, null);
        //dr2.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        Bitmap bitmap = code.toImage(scale, border, Bitmap.Config.ARGB_8888, dr, null);
        qrCodeImage.setImageBitmap(bitmap);

        /*if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("set qr-code transaction: 0x%s, data: 0x%s", Hex.toHexString(result.getTxHash()), Hex.toHexString(result.getQrCodeBytes())));
        }*/
    }

    public void show() {
        show = true;
        root.setVisibility(View.INVISIBLE);
        new Handler().post(() -> {
            int w = root.getWidth() / 2;
            int h = root.getHeight() / 2;
            root.setClipBounds(new Rect(w, h, w, h));
            root.setVisibility(View.VISIBLE);
            TransitionManager.beginDelayedTransition((ViewGroup) root.getParent(), changeRootBoundsTransaction());
            root.setClipBounds(null);
        });
    }

    public void hide() {
        show = false;
        TransitionManager.beginDelayedTransition((ViewGroup) root.getParent(), changeRootBoundsTransaction());
        int w = root.getWidth() / 2;
        int h = root.getHeight() / 2;
        root.setClipBounds(new Rect(w, h, w, h));
    }

    private Transition changeRootBoundsTransaction() {
        ChangeBounds tr = new ChangeBounds();
        tr.addTarget(root).setDuration(400);
        tr.setResizeClip(true);
        tr.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                root.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
        });
        return tr;
    }
}
